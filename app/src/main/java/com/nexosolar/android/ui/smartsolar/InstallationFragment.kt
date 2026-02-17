package com.nexosolar.android.ui.smartsolar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexosolar.android.ui.theme.NexoSolarTheme // Asegúrate de importar tu tema
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint // ¡CRUCIAL! Sin esto, hiltViewModel() dentro de Route fallará
class InstallationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Estrategia recomendada para Fragments: destruye la composición cuando muere la vista del fragmento
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                // Envuelve todo en tu tema para que los colores y tipografías funcionen
                NexoSolarTheme {
                    // Llamamos a la "Route" que gestiona el ViewModel
                    InstallationRoute()
                }
            }
        }
    }

}
