package com.ganeshmandal.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.Mandal;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class MandalAdapter extends RecyclerView.Adapter<MandalAdapter.ViewHolder> {
    private List<Mandal> mandalList = new ArrayList<>();
    private OnMandalClickListener listener;

    public interface OnMandalClickListener {
        void onEditClick(Mandal mandal);
        void onAddAdminClick(Mandal mandal);
    }

    public void setListener(OnMandalClickListener listener) {
        this.listener = listener;
    }

    public void setMandals(List<Mandal> list) {
        this.mandalList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mandal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mandal m = mandalList.get(position);

        holder.tvMandalId.setText(m.getMandalId() != null ? m.getMandalId() : "M001");
        holder.tvMandalName.setText(m.getMandalName() != null ? m.getMandalName() : "श्री गणेश मित्र मंडळ");
        holder.tvMandalAddress.setText(m.getAddress() != null && !m.getAddress().isEmpty() ? ("📍 " + m.getAddress()) : "📍 पत्ता उपलब्ध नाही");
        holder.tvStatus.setText("ACTIVE");

        if (m.getLogoUrl() != null && !m.getLogoUrl().trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(m.getLogoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .into(holder.ivMandalLogo);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.app_logo)
                    .circleCrop()
                    .into(holder.ivMandalLogo);
        }

        holder.btnEditMandal.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(m);
        });

        holder.btnAddAdmin.setOnClickListener(v -> {
            if (listener != null) listener.onAddAdminClick(m);
        });
    }

    @Override
    public int getItemCount() {
        return mandalList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMandalId, tvMandalName, tvMandalAddress, tvStatus;
        ImageView ivMandalLogo;
        MaterialButton btnEditMandal, btnAddAdmin;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMandalId = itemView.findViewById(R.id.tvMandalId);
            tvMandalName = itemView.findViewById(R.id.tvMandalName);
            tvMandalAddress = itemView.findViewById(R.id.tvMandalAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivMandalLogo = itemView.findViewById(R.id.ivMandalLogo);
            btnEditMandal = itemView.findViewById(R.id.btnEditMandal);
            btnAddAdmin = itemView.findViewById(R.id.btnAddAdmin);
        }
    }
}
