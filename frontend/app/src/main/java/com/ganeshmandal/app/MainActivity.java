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
    private LinearLayout navGallery, navAarti, navProfile;

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

        navGallery = findViewById(R.id.navGallery);
        navAarti = findViewById(R.id.navAarti);
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

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Open Transactions (जमा / खर्च)
        cardTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionsActivity.class);
            startActivity(intent);
        });

        // Open Members List
        cardMembers.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MembersActivity.class);
            startActivity(intent);
        });

        // Open Daily Events & Aarti Schedule
        cardEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        // Open Donations
        cardDonations.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DonationsActivity.class);
            startActivity(intent);
        });

        // Bottom Nav: Gallery (फोटो गॅलरी)
        if (navGallery != null) {
            navGallery.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Nav: Aarti Sangrah (आरती संग्रह)
        if (navAarti != null) {
            navAarti.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AartiActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Nav: Profile (माझी प्रोफाइल)
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

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
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
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
