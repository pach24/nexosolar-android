package com.nexosolar.android.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nexosolar.android.ui.invoices.InvoiceListActivity
import com.nexosolar.android.ui.smartsolar.SmartSolarActivity
import com.nexosolar.android.ui.theme.NexoSolarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NexoSolarTheme {
                MainRoute(
                    onNavigateToInvoices = {
                        startActivity(Intent(this, InvoiceListActivity::class.java))
                    },
                    onNavigateToSmartSolar = {
                        startActivity(Intent(this, SmartSolarActivity::class.java))
                    }
                )
            }
        }
    }
}
