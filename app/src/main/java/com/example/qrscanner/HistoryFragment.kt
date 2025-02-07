package com.example.qrscanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var database: ScannedQRDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_history)
        val buttonClearHistory: ImageView = view.findViewById(R.id.button_clear_history)

        // Initialize Database
        database = ScannedQRDatabase.getDatabase(requireContext())

        // Observe Live Data from Room
        lifecycleScope.launch {
            database.scannedQRDao().getAllScannedQR().collect { historyList ->
                historyAdapter = HistoryAdapter(historyList)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = historyAdapter
            }
        }

        // Clear History Button Action
        buttonClearHistory.setOnClickListener {
            lifecycleScope.launch {
                database.scannedQRDao().clearHistory()
            }
        }

        return view
    }
}
