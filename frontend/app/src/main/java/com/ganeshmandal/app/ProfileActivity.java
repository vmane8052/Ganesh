package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.UploadResponse;
import com.google.android.material.button.MaterialButton;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePhoto, btnChangePhoto;
    private TextView tvProfileName, tvProfileRoleInMandal, tvProfilePhone, tvProfileRole, tvChangePhotoHint;
    private MaterialButton btnLogout;
    private SharedPreferences prefs;
    private String userPhone = "";

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedImage
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnBack = findViewById(R.id.btnBack);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        tvChangePhotoHint = findViewById(R.id.tvChangePhotoHint);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRoleInMandal = findViewById(R.id.tvProfileRoleInMandal);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        btnLogout = findViewById(R.id.btnLogout);

        prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "सदस्य");
        userPhone = prefs.getString("USER_PHONE", "-");
        String role = prefs.getString("USER_ROLE", "USER");
        String roleInMandal = prefs.getString("USER_ROLE_IN_MANDAL", "ADMIN".equalsIgnoreCase(role) ? "मुख्य व्यवस्थापक" : "सामान्य सदस्य");
        String photoUrl = prefs.getString("USER_PHOTO_URL", "");

        tvProfileName.setText(name);
        tvProfilePhone.setText(userPhone);
        tvProfileRoleInMandal.setText(roleInMandal);
        tvProfileRole.setText("ADMIN".equalsIgnoreCase(role) ? "व्यवस्थापक (Admin)" : "सामान्य सदस्य (User)");

        loadProfilePhoto(photoUrl);

        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener pickPhotoListener = v -> imagePickerLauncher.launch("image/*");
        ivProfilePhoto.setOnClickListener(pickPhotoListener);
        btnChangePhoto.setOnClickListener(pickPhotoListener);
        tvChangePhotoHint.setOnClickListener(pickPhotoListener);

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfilePhoto(String photoUrl) {
        ivProfilePhoto.setImageTintList(null);
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.app_logo)
                        .error(R.drawable.app_logo)
                        .into(ivProfilePhoto);
            } else {
                try {
                    String cleanBase64 = photoUrl;
                    if (photoUrl.contains(",")) {
                        cleanBase64 = photoUrl.substring(photoUrl.indexOf(",") + 1);
                    }
                    byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        Glide.with(this).load(bitmap).circleCrop().into(ivProfilePhoto);
                    } else {
                        Glide.with(this).load(R.drawable.app_logo).circleCrop().into(ivProfilePhoto);
                    }
                } catch (Exception e) {
                    Glide.with(this).load(R.drawable.app_logo).circleCrop().into(ivProfilePhoto);
                }
            }
        } else {
            Glide.with(this).load(R.drawable.app_logo).circleCrop().into(ivProfilePhoto);
        }
    }

    private void handleSelectedImage(Uri uri) {
        if (uri == null) return;

        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (originalBitmap == null) {
                Toast.makeText(this, "फोटो लोड करता आला नाही", Toast.LENGTH_SHORT).show();
                return;
            }

            // Scale down to max 500x500 for fast upload
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float scale = Math.min(500f / width, 500f / height);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, Math.round(width * scale), Math.round(height * scale), true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            ivProfilePhoto.setImageBitmap(scaledBitmap);
            Toast.makeText(this, "फोटो क्लाउडवर अपलोड होत आहे...", Toast.LENGTH_SHORT).show();

            Map<String, String> payload = new HashMap<>();
            payload.put("image", base64Image);
            payload.put("phone", userPhone);

            // Upload to Cloudinary / Backend API
            ApiClient.getService().uploadPhoto(payload).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String uploadedUrl = response.body().getPhotoUrl();
                        prefs.edit().putString("USER_PHOTO_URL", uploadedUrl).apply();
                        loadProfilePhoto(uploadedUrl);
                        Toast.makeText(ProfileActivity.this, "प्रोफाइल फोटो यशस्वीरीत्या सेव्ह झाला!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "फोटो सेव्ह करताना त्रुटी आली", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "एरर: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
