package com.ganeshmandal.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.AartiItem;
import java.util.ArrayList;
import java.util.List;

public class AartiAdapter extends RecyclerView.Adapter<AartiAdapter.ViewHolder> {
    private List<AartiItem> aartiList = new ArrayList<>();
    private float textSizeSp = 17f;

    public void setAartiList(List<AartiItem> list) {
        this.aartiList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setTextSize(float size) {
        this.textSizeSp = size;
        notifyDataSetChanged();
    }

    public float getTextSize() {
        return textSizeSp;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aarti, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AartiItem item = aartiList.get(position);
        holder.tvAartiIcon.setText(item.getIcon() != null ? item.getIcon() : "🪔");
        holder.tvAartiTitle.setText(item.getTitle() != null ? item.getTitle() : "");
        holder.tvAartiLyrics.setText(item.getLyrics() != null ? item.getLyrics() : "");
        holder.tvAartiLyrics.setTextSize(textSizeSp);
    }

    @Override
    public int getItemCount() {
        return aartiList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAartiIcon, tvAartiTitle, tvAartiLyrics;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAartiIcon = itemView.findViewById(R.id.tvAartiIcon);
            tvAartiTitle = itemView.findViewById(R.id.tvAartiTitle);
            tvAartiLyrics = itemView.findViewById(R.id.tvAartiLyrics);
        }
    }
}
