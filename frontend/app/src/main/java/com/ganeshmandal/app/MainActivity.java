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
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private CardView cardTransactions, cardMembers, cardEvents, cardDonations;
    private TextView tvRoleTitle, menuAddMember, menuAddEvent, menuAddDonationRate, menuUploadPhoto, menuLogout;
    private LinearLayout navVyavahar, navAarti, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        cardTransactions = findViewById(R.id.cardTransactions);
        cardMembers = findViewById(R.id.cardMembers);
        cardEvents = findViewById(R.id.cardEvents);
        cardDonations = findViewById(R.id.cardDonations);

        tvRoleTitle = findViewById(R.id.tvRoleTitle);
        menuAddMember = findViewById(R.id.menuAddMember);
        menuAddEvent = findViewById(R.id.menuAddEvent);
        menuAddDonationRate = findViewById(R.id.menuAddDonationRate);
        menuUploadPhoto = findViewById(R.id.menuUploadPhoto);
        menuLogout = findViewById(R.id.menuLogout);

        navVyavahar = findViewById(R.id.navVyavahar);
        LinearLayout navAarti = findViewById(R.id.navAarti);
        navProfile = findViewById(R.id.navProfile);

        // Check user role from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "USER");
        String name = prefs.getString("USER_NAME", "सदस्य");

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (isAdmin) {
            tvRoleTitle.setText("व्यवस्थापक (Admin)\n" + name);
            menuAddMember.setVisibility(View.VISIBLE);
            menuAddEvent.setVisibility(View.VISIBLE);
            menuAddDonationRate.setVisibility(View.VISIBLE);
            menuUploadPhoto.setVisibility(View.VISIBLE);
        } else {
            tvRoleTitle.setText("सामान्य सदस्य (User)\n" + name);
            menuAddMember.setVisibility(View.GONE);
            menuAddEvent.setVisibility(View.GONE);
            menuAddDonationRate.setVisibility(View.GONE);
            menuUploadPhoto.setVisibility(View.GONE);
        }

        // Open sidebar on menu click
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Navigation to Transactions list
        cardTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionsActivity.class);
            startActivity(intent);
        });

        if (navVyavahar != null) {
            navVyavahar.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, TransactionsActivity.class);
                startActivity(intent);
            });
        }

        // Members List - Opens for BOTH Admin and Normal User
        cardMembers.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MembersActivity.class);
            startActivity(intent);
        });

        if (navAarti != null) {
            navAarti.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AartiActivity.class);
                startActivity(intent);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        cardEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        cardDonations.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DonationsActivity.class);
            startActivity(intent);
        });

        // Sidebar Actions
        menuAddMember.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(MainActivity.this, AddMemberActivity.class);
            startActivity(intent);
        });

        menuAddEvent.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        menuAddDonationRate.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(MainActivity.this, AddDonationActivity.class);
            startActivity(intent);
        });

        menuUploadPhoto.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(this, "फोटो अपलोड करा लवकरच येत आहे...", Toast.LENGTH_SHORT).show();
        });

        menuLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
