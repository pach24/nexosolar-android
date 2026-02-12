package com.nexosolar.android.ui.smartsolar

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nexosolar.android.NexoSolarApplication
import com.nexosolar.android.R
import com.nexosolar.android.databinding.FragmentDetailsBinding
import com.nexosolar.android.domain.models.Installation
import kotlinx.coroutines.launch

/**
 * Fragment que muestra información detallada de la instalación solar.
 * Migrado a Flow y StateFlow (MVI-ish).
 */
class DetailsFragment : Fragment() {

    // ===== Variables de instancia =====
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    // Inyección manual con Factory actualizado
    private val viewModel: InstallationViewModel by activityViewModels {
        val app = requireActivity().application as NexoSolarApplication
        val repository = app.dataModule.provideInstallationRepository()
        InstallationViewModelFactory(repository)
    }

    // ===== Métodos del ciclo de vida =====
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ===== Métodos privados =====

    private fun setupUI() {
        // Configurar botón de info
        binding.ivInfo.setOnClickListener { showInfoDialog() }

        // Configurar SwipeRefresh (si tienes uno en el XML, asumo que sí o deberías añadirlo)
        /*
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }
        */
    }

    /**
     * Observa el StateFlow del ViewModel de forma segura para el ciclo de vida.
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }

        // Si usaras eventos de un solo disparo (Toast, Nav), iría aquí otro collect
    }

    /**
     * Renderiza el estado de la UI (Patrón MVI: State -> UI)
     */
    private fun renderState(state: InstallationUIState) {
        when (state) {
            is InstallationUIState.Loading -> {
                showLoading(true)
                // Ocultar error si había
                hideError()
            }
            is InstallationUIState.Success -> {
                showLoading(false)
                bindInstallationData(state.installation)
                // Detener refresh si estuviera activo
                // binding.swipeRefresh.isRefreshing = false
            }
            is InstallationUIState.Error -> {
                showLoading(false)
                // binding.swipeRefresh.isRefreshing = false
                showError(state.message)
            }
            is InstallationUIState.Empty -> {
                showLoading(false)
                // Mostrar vista vacía si tienes
                Toast.makeText(context, "No hay datos de instalación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.shimmerViewContainer.startShimmer()
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.GONE
        } else {
            binding.shimmerViewContainer.stopShimmer()
            binding.shimmerViewContainer.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE
        }
    }

    private fun bindInstallationData(installation: Installation) {
        with(binding) {
            tvCau.text = installation.selfConsumptionCode
            tvStatus.text = installation.installationStatus
            tvType.text = installation.installationType
            tvCompensation.text = installation.compensation
            tvPower.text = installation.power
        }
    }

    private fun showError(message: String) {
        // Opción 1: Toast
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        // Opción 2: Snackbar (Recomendado)
        // Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun hideError() {
        // Si tuvieras una vista de error persistente, ocúltala aquí
    }

    private fun showInfoDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_info, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptar)
        btnAceptar.setOnClickListener { dialog.dismiss() }

        dialog.show()

        dialog.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
