package com.nexosolar.android.ui.smartsolar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nexosolar.android.databinding.FragmentInstallationBinding

/**
 * Fragment que muestra información general de la instalación solar.
 *
 * Responsabilidades:
 * - Renderizar la vista principal con gráficos y métricas de la instalación
 * - Futuro: integrar ViewModel para cargar datos dinámicos
 */
class InstallationFragment : Fragment() {
    // ===== Variables de instancia =====
    private var binding: FragmentInstallationBinding? = null

    // ===== Métodos del ciclo de vida =====
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInstallationBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Posinle mejora Conectar ViewModel para cargar datos de producción solar y autoconsumo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
