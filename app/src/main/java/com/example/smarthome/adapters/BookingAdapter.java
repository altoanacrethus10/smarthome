package com.example.smarthome.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.models.Booking;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private OnBookingInteractionListener listener;
    private boolean isOwner;

    public interface OnBookingInteractionListener {
        void onAccept(Booking booking);
        void onReject(Booking booking);
        void onPay(Booking booking);
        void onChat(Booking booking);
        void onCall(Booking booking);
        void onViewReceipt(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookingList, boolean isOwner, OnBookingInteractionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.isOwner = isOwner;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvHouseTitle.setText(booking.getHouseTitle());
        holder.tvLocation.setText(booking.getHouseLocation());
        holder.tvDate.setText(booking.getMoveInDate());
        holder.tvPrice.setText(String.format("Tsh %,.0f", booking.getTotalPrice()));
        holder.tvStatus.setText(booking.getStatusText());
        
        int statusColor = ContextCompat.getColor(context, booking.getStatusColor());
        holder.tvStatus.setTextColor(statusColor);
        holder.statusIndicator.setBackgroundTintList(ColorStateList.valueOf(statusColor));

        if (isOwner) {
            holder.tvPersonName.setText(booking.getTenantName());
            holder.ownerActions.setVisibility(booking.isPending() ? View.VISIBLE : View.GONE);
            holder.btnPay.setVisibility(View.GONE);
            holder.btnReceipt.setVisibility("paid".equalsIgnoreCase(booking.getStatus()) ? View.VISIBLE : View.GONE);
        } else {
            holder.tvPersonName.setText(booking.getOwnerName());
            holder.ownerActions.setVisibility(View.GONE);
            holder.btnPay.setVisibility("accepted".equalsIgnoreCase(booking.getStatus()) || "confirmed".equalsIgnoreCase(booking.getStatus()) ? View.VISIBLE : View.GONE);
            holder.btnReceipt.setVisibility("paid".equalsIgnoreCase(booking.getStatus()) ? View.VISIBLE : View.GONE);
        }

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(booking);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(booking);
        });

        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onPay(booking);
        });

        holder.btnChat.setOnClickListener(v -> {
            if (listener != null) listener.onChat(booking);
        });

        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) listener.onCall(booking);
        });
        
        holder.btnReceipt.setOnClickListener(v -> {
            if (listener != null) listener.onViewReceipt(booking);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvHouseTitle, tvLocation, tvDate, tvPrice, tvStatus, tvPersonName;
        View statusIndicator, ownerActions;
        MaterialButton btnAccept, btnReject, btnChat, btnCall, btnPay, btnReceipt;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHouseTitle = itemView.findViewById(R.id.tv_booking_house_title);
            tvLocation = itemView.findViewById(R.id.tv_booking_location);
            tvDate = itemView.findViewById(R.id.tv_booking_date);
            tvPrice = itemView.findViewById(R.id.tv_booking_price);
            tvStatus = itemView.findViewById(R.id.tv_booking_status);
            tvPersonName = itemView.findViewById(R.id.tv_booking_person);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            ownerActions = itemView.findViewById(R.id.owner_actions);
            btnAccept = itemView.findViewById(R.id.btn_accept_booking);
            btnReject = itemView.findViewById(R.id.btn_reject_booking);
            btnChat = itemView.findViewById(R.id.btn_chat_booking);
            btnCall = itemView.findViewById(R.id.btn_call_booking);
            btnPay = itemView.findViewById(R.id.btn_pay_booking);
            btnReceipt = itemView.findViewById(R.id.btn_view_receipt);
        }
    }
}
