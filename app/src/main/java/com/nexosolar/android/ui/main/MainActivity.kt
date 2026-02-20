package com.nexosolar.android.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nexosolar.android.ui.invoices.InvoiceRoute
import com.nexosolar.android.ui.smartsolar.SmartSolarRoute
import com.nexosolar.android.ui.theme.NexoSolarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NexoSolarTheme {
                val navController = rememberNavController()

                // CAMBIO CLAVE: El startDestination debe ser "main_menu"
                NavHost(
                    navController = navController,
                    startDestination = "main_menu"
                ) {
                    // 1. Pantalla de Inicio
                    composable("main_menu") {
                        MainRoute(
                            onNavigateToInvoices = { navController.navigate("invoices") },
                            onNavigateToSmartSolar = { navController.navigate("smart_solar") }
                        )
                    }

                    // 2. Pantalla de Smart Solar (Ahora encapsulada en su Route)
                    composable("smart_solar") {
                        SmartSolarRoute(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // 3. Pantalla de Facturas
                    composable("invoices") {
                        InvoiceRoute(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

            }
        }
    }
}
