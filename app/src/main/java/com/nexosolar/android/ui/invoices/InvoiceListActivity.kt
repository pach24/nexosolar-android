package com.nexosolar.android.ui.invoices

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexosolar.android.NexoSolarApplication
import com.nexosolar.android.R
import com.nexosolar.android.databinding.ActivityInvoiceListBinding
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase
import com.nexosolar.android.ui.invoices.managers.InvoiceStateManager

class InvoiceListActivity : AppCompatActivity() {


    private lateinit var binding: ActivityInvoiceListBinding
    private lateinit var adapter: InvoiceAdapter


    private val invoiceViewModel: InvoiceViewModel by viewModels {
        val app = application as NexoSolarApplication
        val repository = app.dataModule.provideInvoiceRepository()
        InvoiceViewModelFactory(GetInvoicesUseCase(repository), FilterInvoicesUseCase())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupBackPressHandler()
    }

    private fun setupRecyclerView() {
        adapter = InvoiceAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InvoiceListActivity)
            adapter = this@InvoiceListActivity.adapter
            //itemAnimator = null
        }
    }

    private fun setupObservers() {
        invoiceViewModel.facturas.observe(this) { facturas ->
            adapter.submitList(facturas)
            actualizarEstadoUI()


        }

        invoiceViewModel.viewState.observe(this) { actualizarEstadoUI() }

        invoiceViewModel.showEmptyError.observe(this) { actualizarEstadoUI() }
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener { invoiceViewModel.cargarFacturas() }
        binding.btnVolver.setOnClickListener { finish() }
    }

    private fun actualizarEstadoUI() {
        val state = invoiceViewModel.viewState.value ?: return
        val facturas = invoiceViewModel.facturas.value
        val isError = invoiceViewModel.esEstadoError() && invoiceViewModel.showEmptyError.value == true
        val isFiltering = supportFragmentManager.findFragmentByTag("FILTRO_FRAGMENT")?.isVisible == true

        // 1. Manejo del Shimmer
        if (state == InvoiceStateManager.ViewState.LOADING) {
            mostrarShimmer()
            return
        }
        ocultarShimmer()

        // 2. Visibilidad Atómica (Si estamos filtrando, el fragment manda sobre el resto)
        binding.fragmentContainer.isVisible = isFiltering
        binding.recyclerView.isVisible = !isError && !facturas.isNullOrEmpty() && !isFiltering
        binding.layoutErrorState.isVisible = isError && !isFiltering
        binding.layoutEmptyState.isVisible = !isError && facturas.isNullOrEmpty() && !isFiltering

        // 3. Configuración extra
        if (isError) configurarVistaError(state)

        invalidateOptionsMenu()
    }

    private fun mostrarShimmer() {
        with(binding) {
            layoutErrorState.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
            fragmentContainer.visibility = View.GONE
            recyclerView.visibility = View.GONE

            shimmerViewContainer.visibility = View.VISIBLE
            shimmerViewContainer.startShimmer()
        }
    }

    private fun ocultarShimmer() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
    }

    private fun mostrarError(tipo: InvoiceStateManager.ViewState) {
        configurarVistaError(tipo)
        with(binding) {
            layoutErrorState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun configurarVistaError(tipo: InvoiceStateManager.ViewState) {
        val isNetworkError = tipo == InvoiceStateManager.ViewState.ERROR_NETWORK
        binding.ivError.setImageResource(if (isNetworkError) R.drawable.ic_wifi_off_24 else R.drawable.ic_server_off_24)
        binding.tvError.setText(if (isNetworkError) R.string.error_conexion else R.string.error_conexion_servidor)
        binding.tvErrorDescription.setText(if (isNetworkError) R.string.error_conexion_description_message else R.string.error_conexion_servidor_description_message)
    }


    private fun mostrarLista() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun mostrarEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)
        val filtroItem = menu.findItem(R.id.action_filters)

        val state = invoiceViewModel.viewState.value
        val hasData = invoiceViewModel.hayDatosCargados()
        val isNotLoading = state != InvoiceStateManager.ViewState.LOADING

        filtroItem.isEnabled = hasData && isNotLoading
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filters) {
            if (invoiceViewModel.hayDatosCargados()) {
                mostrarFiltroFragment()
            } else {
                Toast.makeText(this, R.string.no_data_to_filter, Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun mostrarFiltroFragment() {
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.toolbar.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, FilterFragment(), "FILTRO_FRAGMENT")
            .addToBackStack(null)
            .commit()
    }

    fun restoreMainView() {
        binding.toolbar.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        actualizarEstadoUI()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val filterFragment = supportFragmentManager.findFragmentByTag("FILTRO_FRAGMENT")

                if (filterFragment != null && filterFragment.isVisible) {
                    restoreMainView()
                    supportFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}