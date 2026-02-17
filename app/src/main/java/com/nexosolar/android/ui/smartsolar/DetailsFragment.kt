package com.nexosolar.android.ui.smartsolar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.nexosolar.android.ui.theme.NexoSolarTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * **Fragment de Detalles migrado a Compose**
 *
 * ANTES: XML + ViewBinding + ViewModel con StateFlow
 * AHORA: ComposeView + DetailsRoute con hiltViewModel()
 */
@AndroidEntryPoint
class DetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Estrategia: libera recursos cuando se destruye la vista del fragmento
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                NexoSolarTheme {
                    DetailsRoute()
                }
            }
        }
    }
}
