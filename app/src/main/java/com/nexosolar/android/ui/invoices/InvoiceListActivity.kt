package com.nexosolar.android.ui.invoices

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.nexosolar.android.databinding.ActivityInvoiceListBinding
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import kotlinx.coroutines.launch

/**
 * Activity principal de listado de facturas.
 * MIGRADO A FLOW: Consume el estado unificado InvoiceUiState.
 */
class InvoiceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceListBinding
    private lateinit var adapter: InvoiceAdapter

    // ViewModel Factory manual para inyectar dependencias
    private val invoiceViewModel: InvoiceViewModel by viewModels {
        val app = application as NexoSolarApplication
        // Nota: Asegúrate de que provideInvoiceRepository() devuelve el Repo actualizado
        val repository = app.dataModule.provideInvoiceRepository()
        InvoiceViewModelFactory(GetInvoicesUseCase(repository), FilterInvoicesUseCase())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupObservers() // ✅ Aquí está el cambio clave a Flow
        setupListeners()
        setupBackPressHandler()

        // Listener para restaurar UI al cerrar filtros
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

    private fun renderUiState(state: InvoiceViewModel.InvoiceUiState) {
        // Si hay un filtro activo, no tocamos la visibilidad principal
        if (isFilteringActive()) return

        // 1. Resetear visibilidades comunes
        ocultarShimmer()
        binding.layoutErrorState.isVisible = false
        binding.layoutEmptyState.isVisible = false
        binding.recyclerView.isVisible = false

        // 2. Manejar cada estado
        when (state) {
            is InvoiceViewModel.InvoiceUiState.Loading -> {
                mostrarShimmer()
            }
            is InvoiceViewModel.InvoiceUiState.Success -> {
                binding.recyclerView.isVisible = true
                adapter.submitList(state.invoices)
            }
            is InvoiceViewModel.InvoiceUiState.Empty -> {
                binding.layoutEmptyState.isVisible = true
                // Opcional: Limpiar adapter
                adapter.submitList(emptyList())
            }
            is InvoiceViewModel.InvoiceUiState.Error -> {
                configurarVistaError(state.type)
                binding.layoutErrorState.isVisible = true
            }
        }

        // Actualizar menú (habilitar/deshabilitar filtros)
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
        // Nota: cargarFacturas() ya no existe como tal en VM reactivo,
        // pero puedes crear refresh() o simplemente volver a observar si fuera manual.
        // Asumiendo que has creado un método refresh() en VM que llama a repo.refreshInvoices()
        binding.btnRetry.setOnClickListener {
            // Opción A: Relanzar observación (simple)
            // Opción B: Llamar a método refresh explícito
            invoiceViewModel.refresh()
        }
        binding.btnVolver.setOnClickListener { finish() }
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

        // Lógica simplificada con StateFlow
        val currentState = invoiceViewModel.uiState.value
        val canFilter = currentState is InvoiceViewModel.InvoiceUiState.Success
                || (currentState is InvoiceViewModel.InvoiceUiState.Empty && false) // No filtrar si está vacío

        filtroItem.isEnabled = canFilter
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filters) {
            val currentState = invoiceViewModel.uiState.value
            if (currentState is InvoiceViewModel.InvoiceUiState.Success) {
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
