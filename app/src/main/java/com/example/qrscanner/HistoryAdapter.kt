package com.example.qrscanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val historyList: List<String>,
    private val clipboardManager: ClipboardManager,
    private val context: Context
) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textHistoryItem: TextView = itemView.findViewById(R.id.text_history_item)
        val copyIcon: ImageView = itemView.findViewById(R.id.copy_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]
        holder.textHistoryItem.text = currentItem

        // Set click listener for the copy icon
        holder.copyIcon.setOnClickListener {
            val textToCopy = holder.textHistoryItem.text.toString()
            copyToClipboard(textToCopy)
        }
    }

    override fun getItemCount() = historyList.size

    private fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("Scanned QR Code", text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}