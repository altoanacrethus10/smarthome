package com.example.smarthome.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.models.Feedback;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private Context context;
    private List<Feedback> feedbackList;

    public FeedbackAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);

        holder.tvUserName.setText(feedback.getUserName() != null ? feedback.getUserName() : "Anonymous");
        holder.tvFeedbackType.setText(feedback.getFeedbackType() != null ? feedback.getFeedbackType() : "General");
        holder.tvMessage.setText(feedback.getMessage());
        holder.tvDate.setText(feedback.getCreatedAt() != null ? 
            formatDate(feedback.getCreatedAt()) : "N/A");

        // Set rating
        holder.ratingBar.setRating(feedback.getRating());

        // Show house info if available
        if (feedback.getHouseTitle() != null && !feedback.getHouseTitle().isEmpty()) {
            holder.tvHouseTitle.setVisibility(View.VISIBLE);
            holder.tvHouseTitle.setText("Property: " + feedback.getHouseTitle());
        } else {
            holder.tvHouseTitle.setVisibility(View.GONE);
        }

        // Rating text
        String ratingText = getRatingText(feedback.getRating());
        holder.tvRatingText.setText(ratingText);
    }

    @Override
    public int getItemCount() {
        return feedbackList != null ? feedbackList.size() : 0;
    }

    public void updateList(List<Feedback> newList) {
        this.feedbackList = newList;
        notifyDataSetChanged();
    }

    private String formatDate(String timestamp) {
        try {
            // Simple formatting - in real app, use proper date formatting
            long time = Long.parseLong(timestamp);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(time));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private String getRatingText(float rating) {
        if (rating >= 4.5) return "Excellent ⭐";
        if (rating >= 4.0) return "Very Good 👍";
        if (rating >= 3.0) return "Good 🙂";
        if (rating >= 2.0) return "Fair 😐";
        return "Poor 😞";
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvFeedbackType, tvMessage, tvDate, tvHouseTitle, tvRatingText;
        RatingBar ratingBar;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_feedback_user);
            tvFeedbackType = itemView.findViewById(R.id.tv_feedback_type);
            tvMessage = itemView.findViewById(R.id.tv_feedback_message);
            tvDate = itemView.findViewById(R.id.tv_feedback_date);
            tvHouseTitle = itemView.findViewById(R.id.tv_feedback_house);
            tvRatingText = itemView.findViewById(R.id.tv_rating_text);
            ratingBar = itemView.findViewById(R.id.rb_feedback_display);
        }
    }
}
