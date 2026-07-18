package com.example.smarthome.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.models.House;

import java.util.ArrayList;
import java.util.List;

public class SavedPropertiesAdapter extends RecyclerView.Adapter<SavedPropertiesAdapter.ViewHolder> {

    public interface Listener {
        void onOpenDetails(House house);
        void onOpenCard(House house);
        void onUnsave(House house);
    }

    private final Context context;
    private final Listener listener;
    private final List<House> items = new ArrayList<>();

    public SavedPropertiesAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<House> houses) {
        items.clear();
        if (houses != null) items.addAll(houses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_saved_property, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        House house = items.get(position);

        holder.tvTitle.setText(house.getTitle());
        holder.tvLocation.setText(house.getLocation());
        holder.tvPrice.setText(house.getFormattedPrice());
        holder.tvSpecs.setText(house.getBedrooms() + " Bed • " + house.getBathrooms() + " Bath");

        if (house.getImages() != null && !house.getImages().isEmpty()) {
            Glide.with(context)
                    .load(house.getImages().get(0))
                    .placeholder(R.drawable.ic_house_placeholder)
                    .error(R.drawable.ic_house_placeholder)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_house_placeholder);
        }

        holder.btnUnsave.setImageResource(R.drawable.ic_favorite);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpenCard(house);
        });

        holder.btnUnsave.setOnClickListener(v -> {
            if (listener != null) listener.onUnsave(house);
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onOpenDetails(house);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Used by SavedPropertiesActivity for count/subtitle.
    public int getItemCountForCount() {
        return getItemCount();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvTitle;
        TextView tvLocation;
        TextView tvPrice;
        TextView tvSpecs;
        ImageButton btnUnsave;
        com.google.android.material.button.MaterialButton btnViewDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_property_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvSpecs = itemView.findViewById(R.id.tv_specs);
            btnUnsave = itemView.findViewById(R.id.btn_unsave);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}

