package com.example.gassistance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gassistance.Message;
import com.example.gassistance.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_BOT_MESSAGE = 2;

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.isSentByUser() ? VIEW_TYPE_USER_MESSAGE : VIEW_TYPE_BOT_MESSAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            view = inflater.inflate(R.xml.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            view = inflater.inflate(R.xml.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).tvQuestion.setText(message.getContent());
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).tvAnswer.setText(message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvQuestion;

        public UserMessageViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
        }
    }

    public static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAnswer;

        public BotMessageViewHolder(View itemView) {
            super(itemView);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
        }
    }
}


