package com.example.smarthome.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.models.Complaint;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    private Context context;
    private List<Complaint> complaintList;
    private OnComplaintClickListener listener;

    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    public ComplaintAdapter(Context context, List<Complaint> complaintList) {
        this.context = context;
        this.complaintList = complaintList;
    }

    public ComplaintAdapter(Context context, List<Complaint> complaintList, OnComplaintClickListener listener) {
        this.context = context;
        this.complaintList = complaintList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);

        holder.tvSubject.setText(complaint.getSubject());
        holder.tvCategory.setText(complaint.getCategory() != null ? complaint.getCategory() : "General");
        holder.tvDescription.setText(complaint.getDescription());
        holder.tvStatus.setText(complaint.getStatusText());
        holder.tvDate.setText(formatDate(complaint.getCreatedAt()));

        // Set status color
        int statusColor = getStatusColor(complaint.getStatus());
        holder.tvStatus.setTextColor(statusColor);
        holder.tvStatus.setBackgroundColor(Color.parseColor("#33" + Integer.toHexString(statusColor).substring(2)));

        // Show response if available
        if (complaint.getResponse() != null && !complaint.getResponse().isEmpty()) {
            holder.tvResponse.setVisibility(View.VISIBLE);
            holder.tvResponse.setText("Response: " + complaint.getResponse());
        } else {
            holder.tvResponse.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onComplaintClick(complaint);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaintList != null ? complaintList.size() : 0;
    }

    public void updateList(List<Complaint> newList) {
        this.complaintList = newList;
        notifyDataSetChanged();
    }

    private String formatDate(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(time));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.GRAY;
        switch (status.toLowerCase()) {
            case "pending":
                return Color.parseColor("#FFA000"); // Orange
            case "reviewing":
                return Color.parseColor("#1976D2"); // Blue
            case "resolved":
                return Color.parseColor("#388E3C"); // Green
            case "rejected":
                return Color.parseColor("#D32F2F"); // Red
            default:
                return Color.GRAY;
        }
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvCategory, tvDescription, tvStatus, tvDate, tvResponse;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tv_complaint_subject);
            tvCategory = itemView.findViewById(R.id.tv_complaint_category);
            tvDescription = itemView.findViewById(R.id.tv_complaint_description);
            tvStatus = itemView.findViewById(R.id.tv_complaint_status);
            tvDate = itemView.findViewById(R.id.tv_complaint_date);
            tvResponse = itemView.findViewById(R.id.tv_complaint_response);
        }
    }
}
