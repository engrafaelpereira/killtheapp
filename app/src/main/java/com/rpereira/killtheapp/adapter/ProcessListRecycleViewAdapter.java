package com.rpereira.killtheapp.adapter;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rpereira.killtheapp.R;
import com.rpereira.killtheapp.model.Process;

import java.util.List;

/**
 * ProcessListRecycleViewAdapter
 *
 * @author rpereira
 * @since 19/01/17
 */

public class ProcessListRecycleViewAdapter extends RecyclerView.Adapter<ProcessListRecycleViewAdapter.ViewHolder> {

    private final SortedList<Process> processSortedList;
    private final View.OnClickListener itemListener;

    public ProcessListRecycleViewAdapter(View.OnClickListener itemListener) {
        super();
        this.processSortedList = new SortedList<>(Process.class, new ProcessSortedListCallback(this));
        this.itemListener = itemListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_row_icon_text, parent, false);
        rowView.setOnClickListener(itemListener);
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imageView.setImageDrawable(processSortedList.get(position).getIcon());
        holder.textView.setText(processSortedList.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return processSortedList == null ? 0 : processSortedList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        private ViewHolder(View rowView) {
            super(rowView);
            this.imageView = (ImageView) rowView.findViewById(R.id.item_icon);
            this.textView = (TextView) rowView.findViewById(R.id.item_title);
        }
    }

    public Process get(int position) {
        return processSortedList.get(position);
    }

    public void updateList(List<Process> newProcessList) {
        processSortedList.beginBatchedUpdates();
        removeKilledProcesses(newProcessList);
        processSortedList.addAll(newProcessList);
        processSortedList.endBatchedUpdates();
    }

    private void removeKilledProcesses(List<Process> newProcessList) {
        for (int i = 0; i < processSortedList.size(); i++) {
            if (!newProcessList.contains(processSortedList.get(i))) {
                processSortedList.removeItemAt(i);
                i--;
            }
        }
    }

    private class ProcessSortedListCallback extends SortedList.Callback<Process> {
        private ProcessListRecycleViewAdapter processListRecycleViewAdapter;

        private ProcessSortedListCallback(ProcessListRecycleViewAdapter processListRecycleViewAdapter) {
            this.processListRecycleViewAdapter = processListRecycleViewAdapter;
        }

        @Override
        public int compare(Process p1, Process p2) {
            return p2.compareTo(p1);
        }

        @Override
        public void onInserted(int position, int count) {
            processListRecycleViewAdapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            processListRecycleViewAdapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            processListRecycleViewAdapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            processListRecycleViewAdapter.notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Process oldItem, Process newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areItemsTheSame(Process item1, Process item2) {
            return item1.equals(item2);
        }
    }
}
