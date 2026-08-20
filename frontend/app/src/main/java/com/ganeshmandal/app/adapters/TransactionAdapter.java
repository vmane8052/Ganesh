package com.ganeshmandal.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
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
        void onReceiptClick(Transaction transaction);
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

        if (tx.isJama()) {
            // JAMA: Title = Member/Donor Name, Subtitle = Details (वर्गणी/देणगी)
            String name = tx.getMemberName() != null && !tx.getMemberName().trim().isEmpty() ? tx.getMemberName() : "देणगीदार";
            holder.tvTitle.setText(name);
            holder.tvDetails.setText(tx.getDetails() != null ? tx.getDetails() : "जमा");

            holder.tvAmount.setText("+ ₹ " + (long)tx.getAmount());
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else {
            // KHARCH: Title in BOLD = Exact reason for expense (कशासाठी खर्च केला उदा. मंडप, लाईट, प्रसाद)
            String expenseReason = tx.getDetails() != null && !tx.getDetails().trim().isEmpty() ? tx.getDetails() : "मंडळ खर्च";
            holder.tvTitle.setText(expenseReason);
            holder.tvDetails.setText("मंडळ खर्च • " + tx.getDate());

            holder.tvAmount.setText("- ₹ " + (long)tx.getAmount());
            holder.tvAmount.setTextColor(Color.parseColor("#C62828")); // Red
        }

        // Options Button (Three Dots ⋮)
        // Receipts are ONLY generated for JAMA (income). KHARCH (expenses) do not have receipts.
        if (tx.isJama()) {
            holder.btnOptions.setVisibility(View.VISIBLE);
            holder.btnOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add(0, 1, 0, "📄 पावती जनरेट करा / पहा");
                if (isAdmin) {
                    popup.getMenu().add(0, 2, 1, "🗑️ हा व्यवहार हटवा");
                }

                popup.setOnMenuItemClickListener(item -> {
                    int adapterPos = holder.getAdapterPosition();
                    if (listener != null && adapterPos != RecyclerView.NO_POSITION) {
                        if (item.getItemId() == 1) {
                            listener.onReceiptClick(tx);
                        } else if (item.getItemId() == 2) {
                            listener.onDeleteClick(tx, adapterPos);
                        }
                    }
                    return true;
                });
                popup.show();
            });
        } else {
            // KHARCH (Expense): No receipt option!
            if (isAdmin) {
                holder.btnOptions.setVisibility(View.VISIBLE);
                holder.btnOptions.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.getMenu().add(0, 2, 0, "🗑️ हा व्यवहार हटवा");
                    popup.setOnMenuItemClickListener(item -> {
                        int adapterPos = holder.getAdapterPosition();
                        if (listener != null && adapterPos != RecyclerView.NO_POSITION) {
                            if (item.getItemId() == 2) {
                                listener.onDeleteClick(tx, adapterPos);
                            }
                        }
                        return true;
                    });
                    popup.show();
                });
            } else {
                holder.btnOptions.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvDetails, tvAmount;
        ImageView btnOptions;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }
}
