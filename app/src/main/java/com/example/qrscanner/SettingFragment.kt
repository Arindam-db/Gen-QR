package com.example.qrscanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class SettingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Initialize modeRadioGroup and buyCoffeeButton after inflating the layout
        val modeRadioGroup = view.findViewById<RadioGroup>(R.id.modeRadioGroup)
        val buyCoffeeButton = view.findViewById<Button>(R.id.buyCoffeeButton)

        // Set the theme mode based on the selected radio button
        modeRadioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            if (checkedId == R.id.radioDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else if (checkedId == R.id.radioLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else if (checkedId == R.id.radioDefault) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            // Save the selected mode in shared preferences (optional)
            val sharedPreferences =
                requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("ThemeMode", checkedId)
            editor.apply()
        })

        // Set up the "Buy a Coffee" button to open the UPI link directly
        buyCoffeeButton.setOnClickListener(View.OnClickListener { v: View? ->
            // Define the UPI payment URI using Uri.Builder
            val uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "manjitchakraborty9-1@okhdfcbank")
                .appendQueryParameter("pn", "Manjit Chakraborty")
                .appendQueryParameter("aid", "uGICAgICjt7ruag")
                .build()

            // Create an intent to open the UPI payment link directly
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent) // Start the intent without any additional checks
        })

        return view // Return the inflated view at the end
    }
}
