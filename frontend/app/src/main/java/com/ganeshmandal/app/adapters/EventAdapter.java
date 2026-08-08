package com.ganeshmandal.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.MandalEvent;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<MandalEvent> eventList = new ArrayList<>();
    private boolean isAdmin = false;
    private OnEventListener listener;

    public interface OnEventListener {
        void onEdit(MandalEvent event, int position);
        void onDelete(MandalEvent event, int position);
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public void setListener(OnEventListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<MandalEvent> list) {
        this.eventList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MandalEvent ev = eventList.get(position);

        holder.tvDayTitle.setText(ev.getDayTitle() != null ? ev.getDayTitle() : "दैनिक कार्यक्रम");
        holder.tvDate.setText(ev.getDate() != null ? ev.getDate() : "");

        // 1. सकाळची आरती
        if (ev.getMorningAarti() != null && !ev.getMorningAarti().trim().isEmpty()) {
            holder.layoutMorningAarti.setVisibility(View.VISIBLE);
            holder.tvMorningAarti.setText(ev.getMorningAarti());
        } else {
            holder.layoutMorningAarti.setVisibility(View.GONE);
        }

        // 2. संध्याकाळची आरती
        if (ev.getEveningAarti() != null && !ev.getEveningAarti().trim().isEmpty()) {
            holder.layoutEveningAarti.setVisibility(View.VISIBLE);
            holder.tvEveningAarti.setText(ev.getEveningAarti());
        } else {
            holder.layoutEveningAarti.setVisibility(View.GONE);
        }

        // 3. महाप्रसाद / जेवणाचा मान
        if (ev.getLunchHost() != null && !ev.getLunchHost().trim().isEmpty()) {
            holder.layoutLunch.setVisibility(View.VISIBLE);
            holder.tvLunchHost.setText(ev.getLunchHost());
        } else {
            holder.layoutLunch.setVisibility(View.GONE);
        }

        // 4. मोदकाचा मान
        if (ev.getModakHost() != null && !ev.getModakHost().trim().isEmpty()) {
            holder.layoutModak.setVisibility(View.VISIBLE);
            holder.tvModakHost.setText(ev.getModakHost());
        } else {
            holder.layoutModak.setVisibility(View.GONE);
        }

        // 5. सांस्कृतिक / विशेष कार्यक्रम
        if (ev.getCulturalProgram() != null && !ev.getCulturalProgram().trim().isEmpty()) {
            holder.layoutCultural.setVisibility(View.VISIBLE);
            holder.tvCultural.setText(ev.getCulturalProgram());
        } else {
            holder.layoutCultural.setVisibility(View.GONE);
        }

        // 6. विशेष सूचना / टीप
        if (ev.getSpecialNotes() != null && !ev.getSpecialNotes().trim().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText("📢 सूचना: " + ev.getSpecialNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Admin Actions (Edit & Delete)
        if (isAdmin) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.btnEditEvent.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onEdit(ev, pos);
                }
            });
            holder.btnDeleteEvent.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onDelete(ev, pos);
                }
            });
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayTitle, tvDate, tvMorningAarti, tvEveningAarti, tvLunchHost, tvModakHost, tvCultural, tvNotes;
        LinearLayout layoutMorningAarti, layoutEveningAarti, layoutLunch, layoutModak, layoutCultural, layoutAdminActions;
        MaterialButton btnEditEvent, btnDeleteEvent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayTitle = itemView.findViewById(R.id.tvDayTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMorningAarti = itemView.findViewById(R.id.tvMorningAarti);
            tvEveningAarti = itemView.findViewById(R.id.tvEveningAarti);
            tvLunchHost = itemView.findViewById(R.id.tvLunchHost);
            tvModakHost = itemView.findViewById(R.id.tvModakHost);
            tvCultural = itemView.findViewById(R.id.tvCultural);
            tvNotes = itemView.findViewById(R.id.tvNotes);

            layoutMorningAarti = itemView.findViewById(R.id.layoutMorningAarti);
            layoutEveningAarti = itemView.findViewById(R.id.layoutEveningAarti);
            layoutLunch = itemView.findViewById(R.id.layoutLunch);
            layoutModak = itemView.findViewById(R.id.layoutModak);
            layoutCultural = itemView.findViewById(R.id.layoutCultural);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);

            btnEditEvent = itemView.findViewById(R.id.btnEditEvent);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}
