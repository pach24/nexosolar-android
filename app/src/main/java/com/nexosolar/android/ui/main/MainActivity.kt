package com.nexosolar.android.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nexosolar.android.NexoSolarApplication
import com.nexosolar.android.R
import com.nexosolar.android.databinding.ActivityMainBinding
import com.nexosolar.android.ui.invoices.InvoiceListActivity
import com.nexosolar.android.ui.smartsolar.SmartSolarActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Usamos lateinit para evitar el tipado anulable (ActivityMainBinding?)
    private lateinit var binding: ActivityMainBinding

    // Variables de configuración de la API
    private var useMock = true
    private var useAltUrl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupUserGreeting()
        setupCardListeners()
        setupApiControls()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUserGreeting() {
        val nombreUsuario = "USUARIO"

        // Uso de strings con placeholders directamente
        binding.tvGreeting.text = getString(R.string.greeting_user, nombreUsuario)

        val direccionGuardada = getString(R.string.avenida_de_la_constituci_n_45)
        val miDireccion = getString(R.string.direccion_con_formato, direccionGuardada)

        binding.tvAddress.text = HtmlCompat.fromHtml(miDireccion, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setupCardListeners() {
        binding.cardFacturas.setOnClickListener { navigateToInvoices() }
        binding.cardSmartSolar.setOnClickListener { navigateToSmartSolar() }
    }

    private fun setupApiControls() {
        // Estado inicial
        binding.btToggleApi.isChecked = useMock
        binding.switchAltUrl.isChecked = useAltUrl
        updateUrlSwitchState()

        // Listener Mock vs Real
        binding.btToggleApi.setOnCheckedChangeListener { _, isChecked ->
            useMock = isChecked
            if (useMock) {
                useAltUrl = false
                // Actualizamos el switch de URL para que sea coherente visualmente
                binding.switchAltUrl.isChecked = false
            }
            updateUrlSwitchState()
            updateDataModule()
        }

        // Listener URL Alt
        binding.switchAltUrl.setOnCheckedChangeListener { _, isChecked ->
            useAltUrl = isChecked
            updateDataModule()
        }

        updateDataModule()
    }

    private fun updateUrlSwitchState() {
        // En Kotlin podemos usar propiedades calculadas o aplicar cambios en bloque
        binding.switchAltUrl.apply {
            isEnabled = !useMock
            alpha = if (!useMock) 1.0f else 0.5f
        }
    }

    private fun updateDataModule() {
        // Guardamos la preferencia
        val prefs = getSharedPreferences("RepoPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("last_mode_was_mock", useMock)
            putBoolean("last_url_was_alt", useAltUrl)
            apply()
        }

        // Feedback visual
        val modeMsg = if (useMock) "Modo: Mock" else "Modo: Real"
        Toast.makeText(this, "Guardado: $modeMsg", Toast.LENGTH_SHORT).show()

        // Opcional: Forzar recarga de datos si estás en una pantalla de lista
    }

    // Funciones de navegación con sintaxis simplificada
    private fun navigateToInvoices() {
        val intent = Intent(this, InvoiceListActivity::class.java).apply {
            putExtra("USE_RETROMOCK", useMock)
        }
        startActivity(intent)
    }

    private fun navigateToSmartSolar() {
        startActivity(Intent(this, SmartSolarActivity::class.java))
    }
}