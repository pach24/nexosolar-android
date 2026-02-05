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
import com.nexosolar.android.NexoSolarApplication
import com.nexosolar.android.R
import com.nexosolar.android.databinding.FragmentDetailsBinding
import com.nexosolar.android.ui.smartsolar.managers.InstallationStateManager

/**
 * Fragment que muestra información detallada de la instalación solar.
 * Responsabilidades:
 * - Consumir datos del ViewModel mediante LiveData
 * - Gestionar estados de carga con Shimmer
 * - Mostrar diálogo informativo sobre los detalles de la instalación
 */
class DetailsFragment : Fragment() {

    // ===== Variables de instancia =====

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

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

        setupObservers()
        loadDataIfNeeded()
        setupInfoButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ===== Métodos privados =====

    /**
     * Configura los observadores de LiveData del ViewModel.
     */
    private fun setupObservers() {
        observeLoadingState()
        observeInstallationData()
        observeErrors()
    }

    /**
     * Observa el estado de carga para mostrar/ocultar el efecto Shimmer.
     */
    private fun observeLoadingState() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            val isLoading = state == InstallationStateManager.ViewState.LOADING

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
    }

    /**
     * Observa los datos de instalación y actualiza la UI cuando están disponibles.
     */
    private fun observeInstallationData() {
        viewModel.installation.observe(viewLifecycleOwner) { installation ->
            installation?.let {
                binding.tvCau.text = it.selfConsumptionCode
                binding.tvStatus.text = it.installationStatus
                binding.tvType.text = it.installationType
                binding.tvCompensation.text = it.compensation
                binding.tvPower.text = it.power
            }
        }
    }

    /**
     * Observa errores del ViewModel y notifica al usuario.
     */
    private fun observeErrors() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Solicita la carga de datos solo si no están previamente disponibles.
     * Evita recargas innecesarias durante rotaciones de pantalla.
     */
    private fun loadDataIfNeeded() {
        if (viewModel.installation.value == null) {
            viewModel.loadInstallationDetails()
        }
    }

    /**
     * Configura el listener del botón de información.
     */
    private fun setupInfoButton() {
        binding.ivInfo.setOnClickListener { showInfoDialog() }
    }

    /**
     * Muestra un diálogo modal con información adicional sobre los detalles mostrados.
     * Configura fondo transparente para respetar las esquinas redondeadas del diseño.
     */
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