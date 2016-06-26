package com.dan.rec.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dan.rec.R;
import com.dan.rec.db.LogItem;

import java.util.ArrayList;

/**
 * Adapter that is backed by an array of {@code DetectedActivity} objects. Finds UI elements in the
 * detected_activity layout and populates each element with data from a DetectedActivity
 * object.
 */
public class DetectedHistoryLogAdapter extends ArrayAdapter<LogItem> {

    public DetectedHistoryLogAdapter(Context context,
                                     ArrayList<LogItem> logItems) {
        super(context, 0, logItems);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LogItem logItem = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_history_log, parent, false);
        }

        // Find the UI widgets.
        TextView logTv = (TextView) view.findViewById(R.id.logInfoTv);

        // Populate widgets with values.
        String name = logItem.getLog();
        logTv.setText(name);

        return view;
    }
}
