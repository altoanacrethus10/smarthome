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

public class TenantCategoryAdapter extends RecyclerView.Adapter<TenantCategoryAdapter.CategoryVH> {

    public static class CategoryItem {
        public final String label;
        public final int iconRes;

        public CategoryItem(String label, int iconRes) {
            this.label = label;
            this.iconRes = iconRes;
        }
    }

    public interface OnCategoryClickListener {
        void onClick(CategoryItem item);
    }

    private final Context context;
    private final List<CategoryItem> items;
    private final OnCategoryClickListener listener;

    public TenantCategoryAdapter(Context context, List<CategoryItem> items, OnCategoryClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_tenant_category, parent, false);
        return new CategoryVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryVH holder, int position) {
        CategoryItem item = items.get(position);
        holder.tvLabel.setText(item.label);
        holder.ivIcon.setImageResource(item.iconRes);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class CategoryVH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvLabel;

        CategoryVH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvLabel = itemView.findViewById(R.id.tv_category_label);
        }
    }
}

