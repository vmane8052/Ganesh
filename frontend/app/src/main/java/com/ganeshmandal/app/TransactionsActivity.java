package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.TransactionAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.Transaction;
import com.ganeshmandal.app.models.TransactionResponse;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    private ImageView btnBack, btnFilter;
    private TextView tvTotalJama, tvTotalKharch, tvBalance;
    private MaterialButton tabJama, tabKharch, btnJamaKara, btnKharchKara;
    private RecyclerView rvTransactions;
    private LinearLayout layoutAdminActions;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private String currentFilter = null; // null = ALL, "JAMA" or "KHARCH"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter);
        tvTotalJama = findViewById(R.id.tvTotalJama);
        tvTotalKharch = findViewById(R.id.tvTotalKharch);
        tvBalance = findViewById(R.id.tvBalance);
        tabJama = findViewById(R.id.tabJama);
        tabKharch = findViewById(R.id.tabKharch);
        rvTransactions = findViewById(R.id.rvTransactions);
        layoutAdminActions = findViewById(R.id.layoutAdminActions);
        btnJamaKara = findViewById(R.id.btnJamaKara);
        btnKharchKara = findViewById(R.id.btnKharchKara);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter();
        rvTransactions.setAdapter(adapter);

        // Check if user is Admin
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(prefs.getString("USER_ROLE", "USER"));

        layoutAdminActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        adapter.setAdmin(isAdmin);

        adapter.setListener((tx, pos) -> {
            new androidx.appcompat.app.AlertDialog.Builder(TransactionsActivity.this)
                    .setTitle("व्यवहार हटवा")
                    .setMessage("तुम्हाला नक्की हा व्यवहार डेटाबेसमधून हटवायचा आहे का?")
                    .setPositiveButton("हटवा", (dialog, which) -> {
                        deleteTransactionRemote(tx, pos);
                    })
                    .setNegativeButton("रद्द करा", null)
                    .show();
        });

        btnBack.setOnClickListener(v -> finish());

        tabJama.setOnClickListener(v -> filterTransactions("JAMA"));
        tabKharch.setOnClickListener(v -> filterTransactions("KHARCH"));

        btnJamaKara.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("TYPE", "JAMA");
            startActivity(intent);
        });

        btnKharchKara.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("TYPE", "KHARCH");
            startActivity(intent);
        });

        fetchTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
    }

    private void fetchTransactions() {
        // 100% Strict Real-Time Fetch directly from MongoDB Atlas Cloud API
        ApiClient.getService().getTransactions(null).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Transaction> serverList = response.body().getData();
                    allTransactions = serverList != null ? serverList : new ArrayList<>();
                    applyFilter();
                } else {
                    Toast.makeText(TransactionsActivity.this, "डेटाबेसमधून व्यवहार लोड करू शकलो नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                Toast.makeText(TransactionsActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTransactions(String type) {
        if (currentFilter != null && currentFilter.equals(type)) {
            currentFilter = null; // reset filter
        } else {
            currentFilter = type;
        }
        applyFilter();
    }

    private void applyFilter() {
        updateSummary();
        if (currentFilter == null) {
            adapter.setTransactions(allTransactions);
        } else {
            List<Transaction> filtered = new ArrayList<>();
            for (Transaction tx : allTransactions) {
                if (tx.getType().equalsIgnoreCase(currentFilter)) {
                    filtered.add(tx);
                }
            }
            adapter.setTransactions(filtered);
        }
    }

    private void updateSummary() {
        double totalJama = 0;
        double totalKharch = 0;
        for (Transaction tx : allTransactions) {
            if ("JAMA".equalsIgnoreCase(tx.getType())) {
                totalJama += tx.getAmount();
            } else if ("KHARCH".equalsIgnoreCase(tx.getType())) {
                totalKharch += tx.getAmount();
            }
        }
        double balance = totalJama - totalKharch;
        tvTotalJama.setText(String.format(Locale.getDefault(), "₹ %.0f", totalJama));
        tvTotalKharch.setText(String.format(Locale.getDefault(), "₹ %.0f", totalKharch));
        tvBalance.setText(String.format(Locale.getDefault(), "₹ %.0f", balance));
    }

    private void deleteTransactionRemote(Transaction tx, int pos) {
        if (tx.getId() != null && !tx.getId().isEmpty()) {
            ApiClient.getService().deleteTransaction(tx.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    allTransactions.remove(tx);
                    applyFilter();
                    Toast.makeText(TransactionsActivity.this, "व्यवहार डेटाबेसमधून हटवला!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(TransactionsActivity.this, "डेटाबेस डिलीट एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
