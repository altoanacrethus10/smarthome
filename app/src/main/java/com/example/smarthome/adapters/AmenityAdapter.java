package com.example.smarthome.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;

import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.ViewHolder> {

    private final List<Amenity> amenities;

    public static class Amenity {
        public String name;
        public int iconRes;

        public Amenity(String name, int iconRes) {
            this.name = name;
            this.iconRes = iconRes;
        }
    }

    public AmenityAdapter(List<Amenity> amenities) {
        this.amenities = amenities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_amenity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Amenity amenity = amenities.get(position);
        holder.tvName.setText(amenity.name);
        holder.ivIcon.setImageResource(amenity.iconRes);
    }

    @Override
    public int getItemCount() {
        return amenities != null ? amenities.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_amenity_icon);
            tvName = itemView.findViewById(R.id.tv_amenity_name);
        }
    }
}
