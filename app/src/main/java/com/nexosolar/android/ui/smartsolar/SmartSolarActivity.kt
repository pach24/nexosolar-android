package com.nexosolar.android.ui.smartsolar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.nexosolar.android.databinding.ActivitySmartSolarBinding

/**
 * Pantalla principal del módulo SmartSolar.
 * Gestiona la navegación entre las pestañas de información de la instalación
 * mediante ViewPager2 y TabLayout.
 */
class SmartSolarActivity : AppCompatActivity() {

    // ===== Variables de instancia =====

    private lateinit var binding: ActivitySmartSolarBinding

    // ===== Métodos del ciclo de vida =====

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartSolarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupTabs()
    }

    // ===== Métodos privados =====

    /**
     * Configura el comportamiento del botón de navegación hacia atrás.
     */
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Inicializa el ViewPager2 y sincroniza las pestañas con TabLayoutMediator.
     * Se definen tres pestañas: Mi instalación, Energía y Detalles.
     */
    private fun setupTabs() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Mi instalación"
                1 -> "Energía"
                2 -> "Detalles"
                else -> ""
            }
        }.attach()
    }

    // ===== Clases internas =====

    /**
     * Adaptador que gestiona la creación de fragments para cada pestaña del ViewPager2.
     */
    private class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        companion object {
            private const val TAB_COUNT = 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> InstallationFragment()
                1 -> EnergyFragment()
                2 -> DetailsFragment()
                else -> InstallationFragment()
            }
        }

        override fun getItemCount(): Int = TAB_COUNT
    }
}