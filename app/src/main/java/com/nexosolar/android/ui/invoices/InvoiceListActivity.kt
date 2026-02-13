package com.nexosolar.android.ui.invoices

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexosolar.android.NexoSolarApplication
import com.nexosolar.android.R
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.Logger
import com.nexosolar.android.databinding.ActivityInvoiceListBinding
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.RefreshInvoicesUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity principal de listado de facturas.
 * MIGRADO A FLOW: Consume el estado unificado InvoiceUiState.
 */
@AndroidEntryPoint
class InvoiceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceListBinding
    private lateinit var adapter: InvoiceAdapter


    private val invoiceViewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupBackPressHandler()


        supportFragmentManager.addOnBackStackChangedListener {
            val isFiltering = isFilteringActive()
            binding.toolbar.isVisible = !isFiltering
            binding.fragmentContainer.isVisible = isFiltering

            // Si cerramos el filtro, re-renderizamos el estado actual
            if (!isFiltering) {
                renderCurrentState()
            }
        }
    }
    override fun onRestart() {
        super.onRestart()
        // Forzamos refresco al volver por si cambió la config en MainActivity
        invoiceViewModel.refresh()
    }

    private fun setupRecyclerView() {
        adapter = InvoiceAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InvoiceListActivity)
            adapter = this@InvoiceListActivity.adapter
        }
    }

    // ========== OBSERVERS (MIGRADO A FLOW) ==========

    private fun setupObservers() {
        lifecycleScope.launch {
            // repeatOnLifecycle suspende la ejecución cuando la app va a background
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observamos el estado unificado UI
                invoiceViewModel.uiState.collect { state ->
                    renderUiState(state)
                }
            }
        }
    }

    // ========== UI RENDERING ==========

    private fun renderUiState(state: InvoiceUIState) {
        if (isFilteringActive()) return

        // 1. Calculamos flags booleanos para saber qué mostrar
        val isLoading = state is InvoiceUIState.Loading
        val isError = state is InvoiceUIState.Error
        val isEmpty = state is InvoiceUIState.Empty
        val isSuccess = state is InvoiceUIState.Success

        // 2. Asignamos visibilidades ATÓMICAMENTE (sin apagar primero)
        // El Shimmer solo sale si es Loading puro (Hard Refresh)
        binding.shimmerViewContainer.isVisible = isLoading
        if (isLoading) binding.shimmerViewContainer.startShimmer() else binding.shimmerViewContainer.stopShimmer()

        binding.layoutErrorState.isVisible = isError

        // IMPORTANTE: Aquí evitamos el parpadeo.
        // Si pasas de Empty(refresca=true) -> Empty(refresca=false),
        // 'isEmpty' siempre es true, así que la vista NUNCA se oculta.
        binding.layoutEmptyState.isVisible = isEmpty
        binding.recyclerView.isVisible = isSuccess

        // 3. Configuración específica de cada estado
        when (state) {
            is InvoiceUIState.Loading -> {
                binding.swipeRefresh.isEnabled = false
            }
            is InvoiceUIState.Success -> {
                binding.swipeRefresh.isEnabled = true
                binding.swipeRefresh.isRefreshing = state.isRefreshing
                adapter.submitList(state.invoices)
            }
            is InvoiceUIState.Empty -> {
                binding.swipeRefresh.isEnabled = true
                binding.swipeRefresh.isRefreshing = state.isRefreshing
                adapter.submitList(emptyList())

            }
            is InvoiceUIState.Error -> {
                binding.swipeRefresh.isEnabled = true
                binding.swipeRefresh.isRefreshing = false
                configurarVistaError(state.type)
            }
        }

        invalidateOptionsMenu()
    }



    /**
     * Helper para re-renderizar el último estado conocido.
     * Útil al volver del fragmento de filtros.
     */
    private fun renderCurrentState() {
        renderUiState(invoiceViewModel.uiState.value)
    }

    private fun mostrarShimmer() {
        binding.shimmerViewContainer.isVisible = true
        binding.shimmerViewContainer.startShimmer()
    }

    private fun ocultarShimmer() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.isVisible = false
    }

    private fun configurarVistaError(type: ErrorClassifier.ErrorType) {
        val isNetworkError = type is ErrorClassifier.ErrorType.Network
        binding.ivError.setImageResource(
            if (isNetworkError) R.drawable.ic_wifi_off_24 else R.drawable.ic_server_off_24
        )
        binding.tvError.setText(
            if (isNetworkError) R.string.error_conexion else R.string.error_conexion_servidor
        )
        binding.tvErrorDescription.setText(
            if (isNetworkError) R.string.error_conexion_description_message else R.string.error_conexion_servidor_description_message
        )
    }

    // ========== LISTENERS & INTERACTIONS ==========

    private fun setupListeners() {


        binding.btnRetry.setOnClickListener {
            invoiceViewModel.refresh()
        }
        binding.btnVolver.setOnClickListener { finish() }

        binding.swipeRefresh.setOnRefreshListener {

            invoiceViewModel.onSwipeRefresh()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFilteringActive()) {
                    supportFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun isFilteringActive(): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag("FILTRO_FRAGMENT")
        return fragment != null && fragment.isVisible
    }

    // ========== MENU ==========

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)
        val filtroItem = menu.findItem(R.id.action_filters)

        val currentState = invoiceViewModel.uiState.value

        val canFilter = currentState is InvoiceUIState.Success
         || currentState is InvoiceUIState.Empty

        filtroItem.isEnabled = canFilter
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filters) {
            val currentState = invoiceViewModel.uiState.value

            if (currentState is InvoiceUIState.Success || currentState is InvoiceUIState.Empty) {
                mostrarFiltroFragment()
            } else {
                Toast.makeText(this, R.string.no_data_to_filter, Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun mostrarFiltroFragment() {
        binding.fragmentContainer.isVisible = true
        binding.toolbar.isVisible = false

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, FilterFragment(), "FILTRO_FRAGMENT")
            .addToBackStack(null)
            .commit()
    }
}
