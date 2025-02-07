package com.example.qrscanner

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var clipboardManager: ClipboardManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_history)

        // Load data from SharedPreferences
        val historyList = loadScannedHistory()

        // Get ClipboardManager
        clipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Set up RecyclerView
        historyAdapter = HistoryAdapter(historyList, clipboardManager, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = historyAdapter

        return view
    }

    // Function to retrieve QR code history from SharedPreferences
    private fun loadScannedHistory(): List<String> {
        val sharedPreferences =
            requireActivity().getSharedPreferences("QR_HISTORY", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("history_list", setOf())?.toList() ?: emptyList()
    }
}