package com.boarbeard.ui;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boarbeard.R;
import com.boarbeard.audio.MissionLog;

import java.util.List;

public class MissionCardsAdapter extends RecyclerView.Adapter<MissionCardsAdapter.ViewHolder> {
    private List<MissionLog> missionLogs;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MissionCardsAdapter(List<MissionLog> missionLogs) {
        this.missionLogs = missionLogs;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MissionCardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mission_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MissionLog missionLog = missionLogs.get(position);

        TextView missionTextView = (TextView) holder.cardView.findViewById(R.id.missionTextView);
        missionTextView.setTextColor(missionLog.getColor());
        missionTextView.setText(missionLog.getActionText());

        TextView clockTextView = (TextView) holder.cardView.findViewById(R.id.clockTextView);
        if (missionLog.getClockText() != null) {
            clockTextView.setText(missionLog.getClockText());
        }
        else {
            clockTextView.setText("");
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return missionLogs.size();
    }
}