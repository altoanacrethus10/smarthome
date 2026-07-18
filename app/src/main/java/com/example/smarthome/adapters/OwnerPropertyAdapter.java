package com.example.smarthome.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.activities.AddListingActivity;
import com.example.smarthome.activities.HouseDetailActivity;
import com.example.smarthome.models.House;

import java.util.List;

public class OwnerPropertyAdapter extends RecyclerView.Adapter<OwnerPropertyAdapter.ViewHolder> {

    public interface Listener {
        void onView(House house);
        void onEdit(House house);
        void onDelete(House house);
    }

    private final Context context;
    private List<House> houseList;
    private final Listener listener;

    public OwnerPropertyAdapter(Context context, List<House> houseList, Listener listener) {
        this.context = context;
        this.houseList = houseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_owner_property, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        House house = houseList.get(position);
        String title = safe(house.getTitle(), "Unnamed Property");
        holder.tvTitle.setText(capitalize(title));

        holder.tvLocation.setText(safe(house.getLocation(), "Location not set"));

        // Price: ensure formatting with commas and the expected suffix.
        // House#getFormattedPrice already uses: Tsh %,.0f
        holder.tvPrice.setText(house.getFormattedPrice() + "/mo");

        // Meta: Only show fields that have values (Beds/Baths) and include realistic area.
        holder.tvMeta.setText(buildBedsBathsAreaText(house));

        holder.tvMetrics.setText("Views: " + house.getViews() + " • Saves: " + Math.max(0, house.getFavorites()));

        // Status
        boolean isAvailable = house.isAvailable();
        holder.tvStatus.setText(isAvailable ? "Vacant" : "Rented");
        holder.tvStatus.setBackgroundResource(isAvailable ? R.drawable.bg_status_success : R.drawable.bg_status_error);
        holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, isAvailable ? R.color.success : R.color.error));


        if (house.getImages() != null && !house.getImages().isEmpty()) {
            Glide.with(context)
                    .load(house.getImages().get(0))
                    .placeholder(R.drawable.ic_house_placeholder)
                    .error(R.drawable.ic_house_placeholder)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_house_placeholder);
        }

        holder.btnView.setOnClickListener(v -> {
            if (listener != null) listener.onView(house);
            else {
                Intent intent = new Intent(context, HouseDetailActivity.class);
                intent.putExtra("house_id", house.getId());
                context.startActivity(intent);
            }
        });
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(house);
            else {
                Intent intent = new Intent(context, AddListingActivity.class);
                intent.putExtra("house_id", house.getId());
                context.startActivity(intent);
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(house);
        });
    }

    @Override
    public int getItemCount() {
        return houseList != null ? houseList.size() : 0;
    }

    public void updateList(List<House> newList) {
        this.houseList = newList;
        notifyDataSetChanged();
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
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

    private String buildBedsBathsAreaText(House house) {
        if (house == null) return "";

        // Only show fields that have values.
        // Beds/Baths: assume 0 means missing.
        StringBuilder sb = new StringBuilder();

        int beds = house.getBedrooms();
        int baths = house.getBathrooms();
        double area = house.getArea();

        if (beds > 0) {
            sb.append(beds).append(" Bed");
        }
        if (baths > 0) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(baths).append(" Bath");
        }

        // Area: use database value; if missing/unrealistic -> show N/A
        // Minimum realistic size: studio 20m², house 40m².
        // We don't know type reliably here, so we validate generically with a conservative minimum.
        // If propertyType is present, apply better min.
        double minArea = 20; // fallback
        String type = house.getPropertyType();
        if (type != null) {
            String t = type.trim().toLowerCase();
            if (t.contains("house") || t.contains("villa")) minArea = 40;
            else if (t.contains("studio")) minArea = 20;
            else if (t.contains("apartment") || t.contains("room")) minArea = 20;
        }

        if (area > 0) {
            if (area < minArea) {
                // Treat as missing/unrealistic.
                if (sb.length() > 0) sb.append(" • ");
                sb.append("N/A");
            } else {
                if (sb.length() > 0) sb.append(" • ");
                // Avoid decimals for area (database might be fractional though)
                if (Math.abs(area - Math.rint(area)) < 0.000001) {
                    sb.append((long) area).append(" m²");
                } else {
                    sb.append(String.format(java.util.Locale.US, "%.1f", area)).append(" m²");
                }
            }
        } else {
            if (sb.length() > 0) sb.append(" • ");
            sb.append("N/A");
        }

        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvLocation, tvPrice, tvMeta, tvMetrics, tvStatus;
        View btnView, btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_property_image);
            tvTitle = itemView.findViewById(R.id.tv_property_title);
            tvLocation = itemView.findViewById(R.id.tv_property_location);
            tvPrice = itemView.findViewById(R.id.tv_property_price);
            tvMeta = itemView.findViewById(R.id.tv_property_meta);
            tvMetrics = itemView.findViewById(R.id.tv_property_metrics);
            tvStatus = itemView.findViewById(R.id.tv_property_status);
            btnView = itemView.findViewById(R.id.btn_view_property);
            btnEdit = itemView.findViewById(R.id.btn_edit_property);
            btnDelete = itemView.findViewById(R.id.btn_delete_property);
        }
    }
}
