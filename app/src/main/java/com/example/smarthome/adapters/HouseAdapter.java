package com.example.smarthome.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.models.House;

import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.HouseViewHolder> {

    private final Context context;
    private List<House> houseList;
    private final OnHouseClickListener listener;
    private FavoriteStateProvider favoriteStateProvider;
    private DistanceTextProvider distanceTextProvider;

    public interface OnHouseClickListener {
        void onHouseClick(House house);
        void onRentClick(House house);
        default void onFavoriteClick(House house) {}
        default void onContactClick(House house) {}
    }

    public interface FavoriteStateProvider {
        boolean isFavorite(House house);
    }

    public interface DistanceTextProvider {
        String getDistanceText(House house);
    }

    public HouseAdapter(Context context, List<House> houseList, OnHouseClickListener listener) {
        this.context = context;
        this.houseList = houseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_house, parent, false);
        return new HouseViewHolder(view);
    }

    public void setDistanceTextProvider(DistanceTextProvider distanceTextProvider) {
        this.distanceTextProvider = distanceTextProvider;
    }

    @Override
    public void onBindViewHolder(@NonNull HouseViewHolder holder, int position) {
        House house = houseList.get(position);

        holder.tvTitle.setText(capitalize(house.getTitle()));
        holder.tvLocation.setText(house.getLocationShort());
        holder.tvPrice.setText(house.getFormattedPrice());
        holder.tvBedrooms.setText(house.getBedrooms() + " Bed");
        holder.tvBathrooms.setText(house.getBathrooms() + " Bath");
        holder.tvArea.setText(house.getArea() > 0 ? (int)house.getArea() + " m²" : "N/A");
        holder.tvPropertyType.setText(house.getPropertyType());

        if (holder.tvDistance != null) {
            String dist = distanceTextProvider != null ? distanceTextProvider.getDistanceText(house) : "";
            if (dist != null && !dist.isEmpty()) {
                holder.tvDistance.setVisibility(View.VISIBLE);
                holder.tvDistance.setText(dist);
            } else {
                holder.tvDistance.setVisibility(View.GONE);
            }
        }

        boolean favorite = favoriteStateProvider != null && favoriteStateProvider.isFavorite(house);
        holder.btnFavorite.setImageResource(favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        if (house.getImages() != null && !house.getImages().isEmpty()) {
            Glide.with(context)
                .load(house.getImages().get(0))
                .placeholder(R.drawable.ic_house_placeholder)
                .centerCrop()
                .into(holder.ivHouseImage);
        } else {
            holder.ivHouseImage.setImageResource(R.drawable.ic_house_placeholder);
        }

        if (house.isAvailable()) {
            holder.tvAvailable.setText("Available");
            holder.tvAvailable.setBackgroundResource(R.drawable.bg_status_success);
        } else {
            holder.tvAvailable.setText("Rented");
            holder.tvAvailable.setBackgroundResource(R.drawable.bg_status_error);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHouseClick(house);
        });
        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(house);
        });
        if (holder.btnDetails != null) {
            holder.btnDetails.setOnClickListener(v -> {
                if (listener != null) listener.onHouseClick(house);
            });
        }
        if (holder.btnContact != null) {
            holder.btnContact.setOnClickListener(v -> {
                if (listener != null) listener.onContactClick(house);
            });
        }
        if (holder.btnSave != null) {
            holder.btnSave.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(house);
            });
        }
    }

    @Override
    public int getItemCount() {
        return houseList != null ? houseList.size() : 0;
    }

    public void updateList(List<House> newList) {
        this.houseList = newList;
        notifyDataSetChanged();
    }

    public void setFavoriteStateProvider(FavoriteStateProvider favoriteStateProvider) {
        this.favoriteStateProvider = favoriteStateProvider;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static class HouseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHouseImage;
        ImageButton btnFavorite;
        TextView tvTitle, tvLocation, tvPrice, tvBedrooms, tvBathrooms, tvArea, tvAvailable, tvDistance, tvPropertyType;
        View btnDetails, btnContact, btnSave;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHouseImage = itemView.findViewById(R.id.iv_house_image);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            tvTitle = itemView.findViewById(R.id.tv_house_title);
            tvLocation = itemView.findViewById(R.id.tv_house_location);
            tvPrice = itemView.findViewById(R.id.tv_house_price);
            tvBedrooms = itemView.findViewById(R.id.tv_house_bedrooms);
            tvBathrooms = itemView.findViewById(R.id.tv_house_bathrooms);
            tvArea = itemView.findViewById(R.id.tv_house_area);
            tvAvailable = itemView.findViewById(R.id.tv_house_available);
            tvDistance = itemView.findViewById(R.id.tv_house_distance);
            tvPropertyType = itemView.findViewById(R.id.tv_house_type);
            btnDetails = itemView.findViewById(R.id.btn_view_details);
            btnContact = itemView.findViewById(R.id.btn_contact_owner);
            btnSave = itemView.findViewById(R.id.btn_save_property);
        }
    }
}
