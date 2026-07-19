package com.example.smarthome.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.activities.BookingDetailActivity;
import com.example.smarthome.models.Booking;
import com.example.smarthome.repository.BookingRepository;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class MyBookingsTenantAdapter extends RecyclerView.Adapter<MyBookingsTenantAdapter.BookingViewHolder> {

    public interface Listener {
        void onViewDetails(Booking booking);
        void onCancelBooking(Booking booking);
    }

    private final Context context;
    private final Listener listener;
    private final BookingRepository bookingRepository;

    private List<Booking> items = new ArrayList<>();

    public MyBookingsTenantAdapter(Context context, List<Booking> initial, Listener listener) {
        this.context = context;
        this.items = initial != null ? initial : new ArrayList<>();
        this.listener = listener;
        this.bookingRepository = BookingRepository.getInstance(context);
    }

    public void submitList(List<Booking> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void requestCancel(Booking booking) {
        if (booking == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking? You will receive a refund as per policy.")
                .setNegativeButton("Keep", (d, w) -> d.dismiss())
                .setPositiveButton("Cancel Booking", (d, w) -> {
                    if (listener != null) listener.onCancelBooking(booking);
                })
                .show();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_booking_tenant, parent, false);
        return new BookingViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = items.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProperty;
        private final TextView tvTitle;
        private final TextView tvLocation;
        private final TextView tvCheckIn;
        private final TextView tvCheckOut;
        private final TextView tvTotal;
        private final TextView tvStatus;
        private final View statusDot;

        private final MaterialButton btnViewDetails;
        private final MaterialButton btnCancelBooking;

        private Booking current;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProperty = itemView.findViewById(R.id.iv_property_image);
            tvTitle = itemView.findViewById(R.id.tv_booking_house_title);
            tvLocation = itemView.findViewById(R.id.tv_booking_location);
            tvCheckIn = itemView.findViewById(R.id.tv_booking_checkin);
            tvCheckOut = itemView.findViewById(R.id.tv_booking_checkout);
            tvTotal = itemView.findViewById(R.id.tv_booking_price);
            tvStatus = itemView.findViewById(R.id.tv_booking_status);
            statusDot = itemView.findViewById(R.id.status_indicator);

            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnCancelBooking = itemView.findViewById(R.id.btn_cancel_booking);

            itemView.setOnClickListener(v -> {
                if (listener != null && current != null) listener.onViewDetails(current);
            });

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null && current != null) listener.onViewDetails(current);
            });

            btnCancelBooking.setOnClickListener(v -> {
                if (current != null) requestCancel(current);
            });
        }

        void bind(Booking booking) {
            current = booking;
            tvTitle.setText(booking.getHouseTitle());
            tvLocation.setText(booking.getHouseLocation());
            tvCheckIn.setText(booking.getMoveInDate());
            tvCheckOut.setText(booking.getMoveOutDate());
            tvTotal.setText(String.format(Locale.US, "Tsh %,.0f", booking.getTotalPrice()));

            String status = booking.getStatus() == null ? "" : booking.getStatus().toLowerCase(Locale.US);
            switch (status) {
                case "confirmed":
                    styleStatus(this, status, R.color.success, R.color.success);
                    break;
                case "pending":
                    styleStatus(this, status, R.color.warning, R.color.warning);
                    break;
                case "completed":
                    styleStatus(this, status, R.color.info, R.color.info);
                    break;
                case "cancelled":
                    styleStatus(this, status, R.color.error, R.color.error);
                    break;
                default:
                    styleStatus(this, status, R.color.gray_medium, R.color.gray_medium);
                    break;
            }


            btnCancelBooking.setVisibility(("pending".equals(status) || "confirmed".equals(status)) ? View.VISIBLE : View.GONE);
        }

        private void styleStatus(BookingViewHolder holder, String statusKey, int dotColorRes, int textColorRes) {
            tvStatus.setText(bookingStatusLabel(statusKey));
            int dotColor = ContextCompat.getColor(context, dotColorRes);
            int textColor = ContextCompat.getColor(context, textColorRes);
            tvStatus.setTextColor(textColor);
            statusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dotColor));
        }

        private String bookingStatusLabel(String statusKey) {
            switch (statusKey) {
                case "confirmed":
                    return "Confirmed";
                case "pending":
                    return "Pending";
                case "completed":
                    return "Completed";
                case "cancelled":
                    return "Cancelled";
                default:
                    return statusKey;
            }
        }
    }
}

