package com.example.rpereira.killtheapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rpereira.killtheapp.R;
import com.example.rpereira.killtheapp.model.Process;

import java.util.List;

/**
 * ProcessToIconTextAdapter
 * Created by rpereira on 18/11/16.
 */

public class ProcessToIconTextAdapter extends ArrayAdapter<Process> {

    private final Context context;
    private final List<Process> processes;

    public ProcessToIconTextAdapter(Context context, List<Process> processes) {
        super(context, R.layout.row_icon_text, processes);
        this.context = context;
        this.processes = processes;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_icon_text, parent, false);
        }

        ImageView imageView = (ImageView) rowView.findViewById(R.id.item_icon);
        imageView.setImageDrawable(processes.get(position).getIcon());

        TextView textView = (TextView) rowView.findViewById(R.id.item_title);
        textView.setText(processes.get(position).toString());

        return rowView;
    }
}
