package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.DonationAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.Donation;
import com.ganeshmandal.app.models.DonationListResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonationsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalCash, tvTotalItems, tvEmpty;
    private MaterialButton tabAll, tabCash, tabItem;
    private RecyclerView rvDonations;
    private ExtendedFloatingActionButton fabAddDonation;
    private DonationAdapter adapter;
    private boolean isAdmin = false;
    private List<Donation> allDonations = new ArrayList<>();
    private String currentFilter = "ALL"; // ALL, CASH, ITEM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donations);

        btnBack = findViewById(R.id.btnBack);
        tvTotalCash = findViewById(R.id.tvTotalCash);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvEmpty = findViewById(R.id.tvEmpty);
        tabAll = findViewById(R.id.tabAll);
        tabCash = findViewById(R.id.tabCash);
        tabItem = findViewById(R.id.tabItem);
        rvDonations = findViewById(R.id.rvDonations);
        fabAddDonation = findViewById(R.id.fabAddDonation);

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        isAdmin = "ADMIN".equalsIgnoreCase(prefs.getString("USER_ROLE", "USER"));

        fabAddDonation.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        rvDonations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonationAdapter();
        adapter.setAdmin(isAdmin);
        rvDonations.setAdapter(adapter);

        adapter.setListener(new DonationAdapter.OnDonationListener() {
            @Override
            public void onEdit(Donation donation, int position) {
                Intent intent = new Intent(DonationsActivity.this, AddDonationActivity.class);
                intent.putExtra("DONATION_DATA", donation);
                startActivity(intent);
            }

            @Override
            public void onDelete(Donation donation, int position) {
                new AlertDialog.Builder(DonationsActivity.this)
                        .setTitle("देणगी नोंद हटवा")
                        .setMessage("तुम्हाला नक्की " + donation.getDonorName() + " यांची देणगी नोंद हटवायची आहे का?")
                        .setPositiveButton("हटवा", (dialog, which) -> deleteDonationRemote(donation, position))
                        .setNegativeButton("रद्द करा", null)
                        .show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
        fabAddDonation.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddDonationActivity.class);
            startActivity(intent);
        });

        tabAll.setOnClickListener(v -> setFilter("ALL"));
        tabCash.setOnClickListener(v -> setFilter("CASH"));
        tabItem.setOnClickListener(v -> setFilter("ITEM"));

        fetchDonations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDonations();
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        tabAll.setBackgroundColor(getResources().getColor("ALL".equals(filter) ? R.color.primary_blue : android.R.color.transparent));
        tabAll.setTextColor(getResources().getColor("ALL".equals(filter) ? R.color.white : R.color.text_primary));

        tabCash.setBackgroundColor(getResources().getColor("CASH".equals(filter) ? R.color.jama_green : android.R.color.transparent));
        tabCash.setTextColor(getResources().getColor("CASH".equals(filter) ? R.color.white : R.color.text_primary));

        tabItem.setBackgroundColor(getResources().getColor("ITEM".equals(filter) ? R.color.accent_orange : android.R.color.transparent));
        tabItem.setTextColor(getResources().getColor("ITEM".equals(filter) ? R.color.white : R.color.text_primary));

        applyFilter();
    }

    private void fetchDonations() {
        // 100% Strict Real-Time Cloud MongoDB Atlas Fetch
        ApiClient.getService().getDonations().enqueue(new Callback<DonationListResponse>() {
            @Override
            public void onResponse(Call<DonationListResponse> call, Response<DonationListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Donation> list = response.body().getData();
                    allDonations = list != null ? list : new ArrayList<>();
                    applyFilter();
                } else {
                    Toast.makeText(DonationsActivity.this, "डेटाबेसमधून देणग्या लोड करू शकलो नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationListResponse> call, Throwable t) {
                Toast.makeText(DonationsActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        double totalCash = 0;
        int totalItems = 0;
        List<Donation> filtered = new ArrayList<>();

        for (Donation d : allDonations) {
            if (d.isItem()) {
                totalItems++;
            } else {
                totalCash += d.getAmount();
            }

            if ("ALL".equals(currentFilter)) {
                filtered.add(d);
            } else if ("CASH".equals(currentFilter) && !d.isItem()) {
                filtered.add(d);
            } else if ("ITEM".equals(currentFilter) && d.isItem()) {
                filtered.add(d);
            }
        }

        tvTotalCash.setText(String.format(Locale.getDefault(), "₹ %.0f", totalCash));
        tvTotalItems.setText(totalItems + " वस्तू");

        adapter.setDonations(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void deleteDonationRemote(Donation d, int pos) {
        if (d.getId() != null && !d.getId().isEmpty()) {
            ApiClient.getService().deleteDonation(d.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    allDonations.remove(d);
                    applyFilter();
                    Toast.makeText(DonationsActivity.this, "देणगी नोंद हटवली गेली!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(DonationsActivity.this, "डेटाबेस डिलीट एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
