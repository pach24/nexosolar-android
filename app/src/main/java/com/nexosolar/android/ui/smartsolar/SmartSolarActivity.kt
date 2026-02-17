package com.nexosolar.android.ui.smartsolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nexosolar.android.ui.theme.NexoSolarTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal del módulo SmartSolar.
 *
 * Antes: AppCompatActivity con Fragments + ViewPager2 + XML.
 * Ahora: ComponentActivity con Compose + SmartSolarScreen.
 */
@AndroidEntryPoint
class SmartSolarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexoSolarTheme {
                SmartSolarRoute(onBackClick = { finish() })
            }
        }
    }
}
