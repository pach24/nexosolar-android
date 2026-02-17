package com.nexosolar.android.ui.smartsolar

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nexosolar.android.NexoSolarApplication
import com.nexosolar.android.R
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.core.Logger
import com.nexosolar.android.databinding.FragmentDetailsBinding
import com.nexosolar.android.domain.models.Installation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!


    private val viewModel: InstallationViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

    // =========================================================================
    // CONFIGURACIÓN UI
    // =========================================================================

    private fun setupUI() {
        binding.ivInfo.setOnClickListener { showInfoDialog() }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }

        binding.errorView.btnRetry.setOnClickListener {
            // Aquí forzamos estado Loading visual para feedback inmediato al pulsar botón
            // (El viewModel luego decidirá si hace soft o hard refresh, pero visualmente ayuda)
            renderState(InstallationUIState.Loading)
            viewModel.onRefresh()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }


    private fun renderState(state: InstallationUIState) {


        val isLoading = state is InstallationUIState.Loading
        val isError = state is InstallationUIState.Error
        val isSuccess = state is InstallationUIState.Success
        val isEmpty = state is InstallationUIState.Empty

        // 1. Visibilidad de contenedores principales
        binding.shimmerViewContainer.isVisible = isLoading
        binding.errorView.root.isVisible = isError
        binding.contentLayout.isVisible = isSuccess

        // Control del Shimmer
        if (isLoading) {
            binding.shimmerViewContainer.startShimmer()
        } else {
            binding.shimmerViewContainer.stopShimmer()
        }

        // 2. Control del SwipeRefresh (Spinner nativo)
        binding.swipeRefresh.isEnabled = isSuccess || isEmpty || isError // Deshabilitar en carga inicial

        when (state) {
            is InstallationUIState.Loading -> {
                binding.swipeRefresh.isRefreshing = false
            }
            is InstallationUIState.Success -> {
                binding.swipeRefresh.isRefreshing = state.isRefreshing
                bindInstallationData(state.installation)
            }
            is InstallationUIState.Empty -> {
                binding.swipeRefresh.isRefreshing = state.isRefreshing
                showErrorView(getString(R.string.error_message_generic), ErrorClassifier.ErrorType.Unknown(null))
                binding.errorView.root.isVisible = true // Reusamos la vista de error para empty
                binding.errorView.btnRetry.isVisible = true // Permitir reintentar
            }
            is InstallationUIState.Error -> {
                binding.swipeRefresh.isRefreshing = false

                // Si venimos de un estado con datos y falló el refresh silencioso,
                // el ViewModel habrá vuelto a Success(isRefreshing=false), así que
                // normalmente no caeremos aquí.
                // Caeremos aquí solo si falló la carga INICIAL.
                showErrorView(state.message, state.type)
            }
        }
    }

    // =========================================================================
    // HELPERS (SIMPLIFICADOS)
    // =========================================================================

    private fun showErrorView(message: String, type: ErrorClassifier.ErrorType) {
        // Hacemos visible el include del error
        binding.errorView.root.isVisible = true

        binding.errorView.tvErrorMessage.text = message

        if (type is ErrorClassifier.ErrorType.Network) {
            binding.errorView.tvErrorTitle.text = getString(R.string.error_network_title)
        } else {
            binding.errorView.tvErrorTitle.text = getString(R.string.error_generic_title)
        }
    }

    // He eliminado showShimmer() y stopLoadingEffects() como funciones separadas
    // para evitar el "efecto secundario" de activar el contentLayout sin querer.
    // La lógica ahora está explícita dentro del `when`.
    private fun showShimmer(show: Boolean) {
        binding.shimmerViewContainer.isVisible = show
        binding.contentLayout.isVisible = !show
        if (show) binding.shimmerViewContainer.startShimmer()
        else binding.shimmerViewContainer.stopShimmer()
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

    private fun showInfoDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_info, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btnAceptar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


}
