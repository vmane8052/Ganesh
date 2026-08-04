package com.ganeshmandal.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.Transaction;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactionList = new ArrayList<>();
    private boolean isAdmin = false;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onDeleteClick(Transaction transaction, int position);
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> list) {
        this.transactionList = list;
        notifyDataSetChanged();
    }

    public void removeTransaction(int position) {
        if (position >= 0 && position < transactionList.size()) {
            transactionList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, transactionList.size());
        }
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

        // Show delete button ONLY IF ADMIN
        if (isAdmin) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (listener != null && adapterPos != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(tx, adapterPos);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvDetails, tvAmount;
        ImageView btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
