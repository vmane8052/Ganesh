package com.ganeshmandal.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.Transaction;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactionList = new ArrayList<>();

    public void setTransactions(List<Transaction> list) {
        this.transactionList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactionList.get(position);
        holder.tvDate.setText(tx.getDate());
        holder.tvTitle.setText(tx.getMemberName() != null && !tx.getMemberName().isEmpty() ? tx.getMemberName() : "सदस्य");
        holder.tvDetails.setText(tx.getDetails() + " (" + tx.getCategory() + ")");

        if (tx.isJama()) {
            holder.tvAmount.setText("+ ₹ " + tx.getAmount());
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else {
            holder.tvAmount.setText("- ₹ " + tx.getAmount());
            holder.tvAmount.setTextColor(Color.parseColor("#C62828")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvDetails, tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
