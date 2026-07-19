package com.example.smarthome.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.models.Conversation;
import com.example.smarthome.utils.DateFormatter;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    private final Context context;
    private List<Conversation> conversations;
    private final OnConversationClickListener listener;

    public ConversationAdapter(Context context, List<Conversation> conversations, OnConversationClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.tvName.setText(conversation.getOtherUserName() != null ? conversation.getOtherUserName() : "Unknown user");
        holder.tvLastMessage.setText(conversation.getLastMessage() != null ? conversation.getLastMessage() : "");

        if (conversation.getPropertyTitle() != null && !conversation.getPropertyTitle().isEmpty()) {
            holder.tvProperty.setVisibility(View.VISIBLE);
            holder.tvProperty.setText(conversation.getPropertyTitle());
        } else {
            holder.tvProperty.setVisibility(View.GONE);
        }

        if (conversation.getLastTimestamp() != null) {
            try {
                holder.tvTime.setText(DateFormatter.formatTimeAgo(Long.parseLong(conversation.getLastTimestamp())));
            } catch (NumberFormatException e) {
                holder.tvTime.setText("");
            }
        } else {
            holder.tvTime.setText("");
        }

        Glide.with(context)
                .load(conversation.getOtherAvatarUrl() != null && !conversation.getOtherAvatarUrl().isEmpty()
                        ? conversation.getOtherAvatarUrl() : R.drawable.ic_user)
                .circleCrop()
                .placeholder(R.drawable.ic_user)
                .into(holder.ivAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onConversationClick(conversation);
        });
    }

    @Override
    public int getItemCount() {
        return conversations != null ? conversations.size() : 0;
    }

    public void updateList(List<Conversation> newList) {
        this.conversations = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvProperty, tvLastMessage, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_conversation_avatar);
            tvName = itemView.findViewById(R.id.tv_conversation_name);
            tvProperty = itemView.findViewById(R.id.tv_conversation_property);
            tvLastMessage = itemView.findViewById(R.id.tv_conversation_last_message);
            tvTime = itemView.findViewById(R.id.tv_conversation_time);
        }
    }
}
