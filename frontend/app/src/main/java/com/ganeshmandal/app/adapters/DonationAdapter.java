package com.ganeshmandal.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.Donation;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {
    private List<Donation> donationList = new ArrayList<>();
    private boolean isAdmin = false;
    private OnDonationListener listener;

    public interface OnDonationListener {
        void onEdit(Donation donation, int position);
        void onDelete(Donation donation, int position);
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public void setListener(OnDonationListener listener) {
        this.listener = listener;
    }

    public void setDonations(List<Donation> list) {
        this.donationList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation d = donationList.get(position);

        holder.tvDonorName.setText(d.getDonorName() != null ? d.getDonorName() : "देणगीदार");
        holder.tvDate.setText(d.getDate() != null && !d.getDate().isEmpty() ? "🗓️ " + d.getDate() : "");

        if (d.getDonorPhone() != null && !d.getDonorPhone().trim().isEmpty()) {
            holder.tvPhone.setVisibility(View.VISIBLE);
            holder.tvPhone.setText("📞 " + d.getDonorPhone());
        } else {
            holder.tvPhone.setVisibility(View.GONE);
        }

        if (d.getAddress() != null && !d.getAddress().trim().isEmpty()) {
            holder.tvAddress.setVisibility(View.VISIBLE);
            holder.tvAddress.setText("📍 " + d.getAddress());
        } else {
            holder.tvAddress.setVisibility(View.GONE);
        }

        if (d.isItem()) {
            // ITEM (वस्तू देणगी)
            holder.tvDonationTypeBadge.setText("🎁 वस्तू देणगी");
            holder.tvDonationTypeBadge.setTextColor(Color.parseColor("#E65100")); // Deep Orange
            holder.tvDonationValue.setText("🎁 " + (d.getItemDetails() != null && !d.getItemDetails().isEmpty() ? d.getItemDetails() : "वस्तू"));
            holder.tvDonationValue.setTextColor(Color.parseColor("#EF6C00"));
        } else if ("ONLINE".equalsIgnoreCase(d.getDonationType())) {
            // ONLINE / UPI
            holder.tvDonationTypeBadge.setText("💳 ऑनलाइन देणगी");
            holder.tvDonationTypeBadge.setTextColor(Color.parseColor("#1565C0")); // Blue
            holder.tvDonationValue.setText(String.format(Locale.getDefault(), "+ ₹ %.0f", d.getAmount()));
            holder.tvDonationValue.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            // CASH (रोख देणगी)
            holder.tvDonationTypeBadge.setText("💵 रोख देणगी");
            holder.tvDonationTypeBadge.setTextColor(Color.parseColor("#2E7D32")); // Green
            holder.tvDonationValue.setText(String.format(Locale.getDefault(), "+ ₹ %.0f", d.getAmount()));
            holder.tvDonationValue.setTextColor(Color.parseColor("#2E7D32"));
        }

        // Admin Actions
        if (isAdmin) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.btnEditDonation.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onEdit(d, pos);
                }
            });
            holder.btnDeleteDonation.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onDelete(d, pos);
                }
            });
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return donationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDonorName, tvDonationTypeBadge, tvDonationValue, tvAddress, tvDate, tvPhone;
        LinearLayout layoutAdminActions;
        MaterialButton btnEditDonation, btnDeleteDonation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDonorName = itemView.findViewById(R.id.tvDonorName);
            tvDonationTypeBadge = itemView.findViewById(R.id.tvDonationTypeBadge);
            tvDonationValue = itemView.findViewById(R.id.tvDonationValue);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
            btnEditDonation = itemView.findViewById(R.id.btnEditDonation);
            btnDeleteDonation = itemView.findViewById(R.id.btnDeleteDonation);
        }
    }
}
