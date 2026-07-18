package com.example.smarthome.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.activities.ReceiptActivity;
import com.example.smarthome.models.Booking;

import java.util.List;

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.ViewHolder> {

    public interface Listener {
        void onAccept(Booking booking);
        void onReject(Booking booking);
        void onContact(Booking booking);
        void onDetails(Booking booking);
    }

    private final Context context;
    private List<Booking> bookings;
    private final Listener listener;

    public BookingRequestAdapter(Context context, List<Booking> bookings, Listener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_booking_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.tvTenant.setText(safe(booking.getTenantName(), "Tenant"));
        holder.tvHouse.setText(safe(booking.getHouseTitle(), "Property") + " • " + safe(booking.getHouseLocation(), "Location not set"));
        holder.tvMeta.setText("Move-in: " + safe(booking.getMoveInDate(), "-") + " • Tenants: " + booking.getNumberOfTenants());
        holder.tvStatus.setText(booking.getStatusText());
        holder.tvStatus.setBackgroundResource(statusBackground(booking.getStatus()));
        holder.tvNotes.setVisibility(booking.getNotes() != null && !booking.getNotes().trim().isEmpty() ? View.VISIBLE : View.GONE);
        if (holder.tvNotes.getVisibility() == View.VISIBLE) {
            holder.tvNotes.setText("Notes: " + booking.getNotes());
        }

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(booking);
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(booking);
        });
        holder.btnContact.setOnClickListener(v -> {
            if (listener != null) listener.onContact(booking);
        });
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onDetails(booking);
        });
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public void updateList(List<Booking> newList) {
        this.bookings = newList;
        notifyDataSetChanged();
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    private int statusBackground(String status) {
        if (status == null) return R.drawable.bg_status_warning;
        switch (status.toLowerCase()) {
            case "accepted":
            case "confirmed":
                return R.drawable.bg_status_success;
            case "rejected":
            case "cancelled":
                return R.drawable.bg_status_error;
            default:
                return R.drawable.bg_status_warning;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenant, tvHouse, tvMeta, tvStatus, tvNotes;
        View btnAccept, btnReject, btnContact, btnDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenant = itemView.findViewById(R.id.tv_booking_tenant);
            tvHouse = itemView.findViewById(R.id.tv_booking_house);
            tvMeta = itemView.findViewById(R.id.tv_booking_meta);
            tvStatus = itemView.findViewById(R.id.tv_booking_status);
            tvNotes = itemView.findViewById(R.id.tv_booking_notes);
            btnAccept = itemView.findViewById(R.id.btn_accept_booking);
            btnReject = itemView.findViewById(R.id.btn_reject_booking);
            btnContact = itemView.findViewById(R.id.btn_contact_tenant);
            btnDetails = itemView.findViewById(R.id.btn_booking_details);
        }
    }
}
