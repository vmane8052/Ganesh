package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu, ivDrawerLogo;
    private CardView cardTransactions, cardMembers, cardEvents, cardDonations;
    private TextView tvDrawerUserName, tvRoleTitle, tvAdminSectionTitle;
    private View menuAddMember, menuAddEvent, menuAddDonationRate, menuUploadPhoto, menuChangePassword, menuLogout;
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

        ivDrawerLogo = findViewById(R.id.ivDrawerLogo);
        tvDrawerUserName = findViewById(R.id.tvDrawerUserName);
        tvRoleTitle = findViewById(R.id.tvRoleTitle);
        tvAdminSectionTitle = findViewById(R.id.tvAdminSectionTitle);

        menuAddMember = findViewById(R.id.menuAddMember);
        menuAddEvent = findViewById(R.id.menuAddEvent);
        menuAddDonationRate = findViewById(R.id.menuAddDonationRate);
        menuUploadPhoto = findViewById(R.id.menuUploadPhoto);
        menuChangePassword = findViewById(R.id.menuChangePassword);
        menuLogout = findViewById(R.id.menuLogout);

        navGallery = findViewById(R.id.navGallery);
        navAarti = findViewById(R.id.navAarti);
        navProfile = findViewById(R.id.navProfile);

        // Check user role from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "USER");
        String name = prefs.getString("USER_NAME", "सदस्य");
        String photoUrl = prefs.getString("USER_PHOTO_URL", "");

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (tvDrawerUserName != null) {
            tvDrawerUserName.setText(name);
        }

        if (ivDrawerLogo != null) {
            if (photoUrl != null && !photoUrl.trim().isEmpty() && (photoUrl.startsWith("http://") || photoUrl.startsWith("https://"))) {
                Glide.with(this).load(photoUrl).circleCrop().placeholder(R.drawable.app_logo).into(ivDrawerLogo);
            } else {
                Glide.with(this).load(R.drawable.app_logo).circleCrop().into(ivDrawerLogo);
            }
        }

        if (isAdmin) {
            if (tvRoleTitle != null) tvRoleTitle.setText("👑 मुख्य व्यवस्थापक (Admin)");
            if (tvAdminSectionTitle != null) tvAdminSectionTitle.setVisibility(View.VISIBLE);
            if (menuAddMember != null) menuAddMember.setVisibility(View.VISIBLE);
            if (menuAddEvent != null) menuAddEvent.setVisibility(View.VISIBLE);
            if (menuAddDonationRate != null) menuAddDonationRate.setVisibility(View.VISIBLE);
            if (menuUploadPhoto != null) menuUploadPhoto.setVisibility(View.VISIBLE);
        } else {
            if (tvRoleTitle != null) tvRoleTitle.setText("👤 मंडळ सदस्य (User)");
            if (tvAdminSectionTitle != null) tvAdminSectionTitle.setVisibility(View.GONE);
            if (menuAddMember != null) menuAddMember.setVisibility(View.GONE);
            if (menuAddEvent != null) menuAddEvent.setVisibility(View.GONE);
            if (menuAddDonationRate != null) menuAddDonationRate.setVisibility(View.GONE);
            if (menuUploadPhoto != null) menuUploadPhoto.setVisibility(View.GONE);
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
        if (menuAddMember != null) {
            menuAddMember.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(MainActivity.this, AddMemberActivity.class);
                startActivity(intent);
            });
        }

        if (menuAddEvent != null) {
            menuAddEvent.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                startActivity(intent);
            });
        }

        if (menuAddDonationRate != null) {
            menuAddDonationRate.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(MainActivity.this, AddDonationActivity.class);
                startActivity(intent);
            });
        }

        if (menuUploadPhoto != null) {
            menuUploadPhoto.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
            });
        }

        if (menuChangePassword != null) {
            menuChangePassword.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                prefs.edit().clear().apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
