package com.example.qrscanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Initialize switches
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val switchDefaultMode = view.findViewById<SwitchMaterial>(R.id.switch_default_mode)
        val switchLightMode = view.findViewById<SwitchMaterial>(R.id.switch_light_mode)
        val buyCoffeeButton = view.findViewById<Button>(R.id.buyCoffeeButton)

        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val savedThemeMode = sharedPreferences.getInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Ensure only the correct switch is checked
        switchDarkMode.isChecked = savedThemeMode == AppCompatDelegate.MODE_NIGHT_YES
        switchLightMode.isChecked = savedThemeMode == AppCompatDelegate.MODE_NIGHT_NO
        switchDefaultMode.isChecked = savedThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        // Set up switch listeners
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
                switchLightMode.isChecked = false
                switchDefaultMode.isChecked = false
            }
        }

        switchDefaultMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                switchDarkMode.isChecked = false
                switchLightMode.isChecked = false
            }
        }

        switchLightMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
                switchDarkMode.isChecked = false
                switchDefaultMode.isChecked = false
            }
        }

        // Buy Coffee Button
        buyCoffeeButton.setOnClickListener {
            val uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "8413035575@airtel")
                .appendQueryParameter("pn", "Arindam Deb")
                .appendQueryParameter("aid", "uGICAgICjt7ruag")
                .build()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        return view
    }

    private fun updateTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        saveThemeMode(themeMode)
    }

    private fun saveThemeMode(themeMode: Int) {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("ThemeMode", themeMode).apply()
    }
}
