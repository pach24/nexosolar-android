package com.nexosolar.android.ui.invoices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nexosolar.android.ui.theme.NexoSolarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NexoSolarTheme {
                // Punto de entrada a nuestra pantalla Compose
                InvoiceRoute(
                    onBackClick = { finish() } // Cierra la activity al volver
                )
            }
        }
    }
}
