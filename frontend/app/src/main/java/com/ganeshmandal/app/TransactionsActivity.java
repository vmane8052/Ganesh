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

        // CRITICAL REQUIREMENT: Show bottom Jama/Kharch buttons ONLY IF ADMIN
        layoutAdminActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        adapter.setAdmin(isAdmin);

        adapter.setListener((tx, pos) -> {
            new androidx.appcompat.app.AlertDialog.Builder(TransactionsActivity.this)
                    .setTitle("व्यवहार हटवा")
                    .setMessage("तुम्हाला नक्की हा व्यवहार हटवायचा आहे का?")
                    .setPositiveButton("हटवा", (dialog, which) -> {
                        deleteTransactionLocalAndRemote(tx, pos);
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
        List<Transaction> localList = getLocalTransactions();
        allTransactions = new ArrayList<>(localList);
        applyFilter();

        ApiClient.getService().getTransactions(null).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Transaction> serverList = response.body().getData();
                    allTransactions = new ArrayList<>(localList);
                    if (serverList != null) {
                        allTransactions.addAll(serverList);
                    }
                    applyFilter();
                }
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                // Keep local list
            }
        });
    }

    private List<Transaction> getLocalTransactions() {
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String json = prefs.getString("LOCAL_TXS", "[]");
        com.google.gson.Gson gson = new com.google.gson.Gson();
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Transaction>>() {}.getType();
        List<Transaction> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
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

    private void deleteTransactionLocalAndRemote(Transaction tx, int pos) {
        allTransactions.remove(tx);

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String json = prefs.getString("LOCAL_TXS", "[]");
        com.google.gson.Gson gson = new com.google.gson.Gson();
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Transaction>>() {}.getType();
        List<Transaction> list = gson.fromJson(json, type);
        if (list != null) {
            List<Transaction> toKeep = new ArrayList<>();
            for (Transaction item : list) {
                if (!(item.getAmount() == tx.getAmount() && item.getDetails().equals(tx.getDetails()) && item.getDate().equals(tx.getDate()))) {
                    toKeep.add(item);
                }
            }
            prefs.edit().putString("LOCAL_TXS", gson.toJson(toKeep)).apply();
        }

        applyFilter();
        Toast.makeText(this, "व्यवहार हटवला!", Toast.LENGTH_SHORT).show();

        if (tx.getId() != null && !tx.getId().isEmpty()) {
            ApiClient.getService().deleteTransaction(tx.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
    }
}
