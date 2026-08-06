package com.ganeshmandal.app;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.LoginResponse;
import com.ganeshmandal.app.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMemberActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePhoto;
    private TextView btnSelectPhoto;
    private TextInputEditText etName, etPhone, etPin, etRoleInMandal;
    private MaterialButton btnSaveMember;
    private String selectedPhotoBase64 = "";

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        ivProfilePhoto.setImageURI(uri);
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        if (bitmap != null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                            byte[] bytes = baos.toByteArray();
                            selectedPhotoBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "फोटो लोड करण्यात अडचण आली", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        btnBack = findViewById(R.id.btnBack);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etPin = findViewById(R.id.etPin);
        etRoleInMandal = findViewById(R.id.etRoleInMandal);
        btnSaveMember = findViewById(R.id.btnSaveMember);

        btnBack.setOnClickListener(v -> finish());

        ivProfilePhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        btnSelectPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));

        btnSaveMember.setOnClickListener(v -> saveMember());
    }

    private void saveMember() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String pin = etPin.getText() != null ? etPin.getText().toString().trim() : "";
        String roleInMandal = etRoleInMandal.getText() != null ? etRoleInMandal.getText().toString().trim() : "सामान्य सदस्य";

        if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "कृपया नाव, मोबाईल नंबर आणि पासवर्ड भरा (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10) {
            Toast.makeText(this, "कृपया योग्य १० अंकी मोबाईल नंबर टाका", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveMember.setEnabled(false);
        btnSaveMember.setText("साठवत आहे...");

        User newUser = new User(name, phone, pin, "USER", roleInMandal, selectedPhotoBase64);

        // 1. Save user locally in SharedPreferences REGISTERED_USERS list for guaranteed instant login
        saveUserLocally(newUser);

        // 2. Call backend API to sync with cloud MongoDB
        ApiClient.getService().addUser(newUser).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnSaveMember.setEnabled(true);
                btnSaveMember.setText("सदस्य साठवा");
                Toast.makeText(AddMemberActivity.this, "सदस्य यशस्वीरित्या ॲड झाला!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnSaveMember.setEnabled(true);
                btnSaveMember.setText("सदस्य साठवा");
                Toast.makeText(AddMemberActivity.this, "सदस्य ॲड झाला (Local Mode)", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void saveUserLocally(User user) {
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String existingJson = prefs.getString("REGISTERED_USERS", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<User>>() {}.getType();
        List<User> list = gson.fromJson(existingJson, type);
        if (list == null) list = new ArrayList<>();

        // Remove duplicates if phone already exists
        List<User> filtered = new ArrayList<>();
        for (User u : list) {
            if (!u.getPhone().equals(user.getPhone())) {
                filtered.add(u);
            }
        }
        filtered.add(0, user);
        prefs.edit().putString("REGISTERED_USERS", gson.toJson(filtered)).apply();
    }
}
