package com.example.gassistance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<String> eventList;
    private OnItemLongClickListener longClickListener;

    public EventAdapter(List<String> eventList) {
        this.eventList = eventList;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        String event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView eventTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.eventTextView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            longClickListener.onItemLongClick(position);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        public void bind(String event) {
            eventTextView.setText(event);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
}
