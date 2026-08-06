package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePhoto;
    private TextView tvProfileName, tvProfileRoleInMandal, tvProfilePhone, tvProfileRole;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnBack = findViewById(R.id.btnBack);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRoleInMandal = findViewById(R.id.tvProfileRoleInMandal);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "सदस्य");
        String phone = prefs.getString("USER_PHONE", "-");
        String role = prefs.getString("USER_ROLE", "USER");
        String roleInMandal = prefs.getString("USER_ROLE_IN_MANDAL", "ADMIN".equalsIgnoreCase(role) ? "मुख्य व्यवस्थापक" : "सामान्य सदस्य");
        String photoUrl = prefs.getString("USER_PHOTO_URL", "");

        tvProfileName.setText(name);
        tvProfilePhone.setText(phone);
        tvProfileRoleInMandal.setText(roleInMandal);
        tvProfileRole.setText("ADMIN".equalsIgnoreCase(role) ? "व्यवस्थापक (Admin)" : "सामान्य सदस्य (User)");

        if (photoUrl != null && !photoUrl.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(photoUrl, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivProfilePhoto.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
