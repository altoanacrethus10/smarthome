package com.example.smarthome.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;

import java.util.List;

public class OwnerActivityAdapter extends RecyclerView.Adapter<OwnerActivityAdapter.ViewHolder> {

    public static class Item {
        public final int iconRes;
        public final String title;
        public final String subtitle;
        public final String time;

        public Item(int iconRes, String title, String subtitle, String time) {
            this.iconRes = iconRes;
            this.title = title;
            this.subtitle = subtitle;
            this.time = time;
        }
    }

    private final Context context;
    private List<Item> items;

    public OwnerActivityAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_owner_activity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.ivIcon.setImageResource(item.iconRes);
        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);
        holder.tvTime.setText(item.time);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateList(List<Item> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvSubtitle, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvSubtitle = itemView.findViewById(R.id.tv_activity_subtitle);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
        }
    }
}
