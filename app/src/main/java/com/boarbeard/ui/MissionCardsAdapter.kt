package com.boarbeard.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.boarbeard.R
import com.boarbeard.audio.MissionLog

class MissionCardsAdapter
    (private val missionLogs: List<MissionLog>) :
    RecyclerView.Adapter<MissionCardsAdapter.ViewHolder>() {
    class ViewHolder(
        val cardView: CardView
    ) : RecyclerView.ViewHolder(cardView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // create a new view
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.mission_card_view, parent, false) as CardView
        return ViewHolder(cardView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val missionLog = missionLogs[position]
        val missionTextView = holder.cardView.findViewById<View>(R.id.missionTextView) as TextView
        missionTextView.text = missionLog.actionText
        val clockTextView = holder.cardView.findViewById<View>(R.id.clockTextView) as TextView
        clockTextView.text = missionLog.clockText ?: ""
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return missionLogs.size
    }
}