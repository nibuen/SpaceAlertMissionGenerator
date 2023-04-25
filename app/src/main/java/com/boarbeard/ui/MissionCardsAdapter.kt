package com.boarbeard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.boarbeard.R
import com.boarbeard.audio.MissionLog

class MissionCardsAdapter     // Provide a suitable constructor (depends on the kind of dataset)
    (private val missionLogs: List<MissionLog>) :
    RecyclerView.Adapter<MissionCardsAdapter.ViewHolder>() {
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(  // each data item is just a string in this case
        var cardView: CardView
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
        if (missionLog.clockText != null) {
            clockTextView.text = missionLog.clockText
        } else {
            clockTextView.text = ""
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return missionLogs.size
    }
}