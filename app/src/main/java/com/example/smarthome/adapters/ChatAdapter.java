package com.example.smarthome.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.models.ChatMessage;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private Context context;
    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        String time = formatTime(message.getTimestamp());

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).tvBody.setText(message.getMessage());
            ((SentMessageViewHolder) holder).tvTime.setText(time);
        } else {
            ((ReceivedMessageViewHolder) holder).tvBody.setText(message.getMessage());
            ((ReceivedMessageViewHolder) holder).tvTime.setText(time);
        }
    }

    private String formatTime(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(time);
            return DateFormat.format("hh:mm a", cal).toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvBody, tvTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBody = itemView.findViewById(R.id.tv_message_body);
            tvTime = itemView.findViewById(R.id.tv_message_time);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvBody, tvTime;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBody = itemView.findViewById(R.id.tv_message_body);
            tvTime = itemView.findViewById(R.id.tv_message_time);
        }
    }
}
