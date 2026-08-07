package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.LoginResponse;
import com.ganeshmandal.app.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberDetailActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePhoto, btnCall;
    private TextView tvName, tvRoleInMandal, tvPhone, tvPin;
    private LinearLayout layoutPinRow, layoutAdminActions;
    private MaterialButton btnEditMember, btnDeleteMember;

    private String name, phone, pin, role, roleInMandal, photoUrl;
    private boolean isAdmin = false;

    private static final String[] MANDAL_ROLES = new String[] {
            "अध्यक्ष",
            "उपाध्यक्ष",
            "सचिव",
            "खजिनदार",
            "कार्यकर्ते",
            "सामान्य सदस्य"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail);

        btnBack = findViewById(R.id.btnBack);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnCall = findViewById(R.id.btnCall);
        tvName = findViewById(R.id.tvName);
        tvRoleInMandal = findViewById(R.id.tvRoleInMandal);
        tvPhone = findViewById(R.id.tvPhone);
        tvPin = findViewById(R.id.tvPin);
        layoutPinRow = findViewById(R.id.layoutPinRow);
        layoutAdminActions = findViewById(R.id.layoutAdminActions);
        btnEditMember = findViewById(R.id.btnEditMember);
        btnDeleteMember = findViewById(R.id.btnDeleteMember);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String userRole = prefs.getString("USER_ROLE", "USER");
        isAdmin = "ADMIN".equalsIgnoreCase(userRole);

        // Get member data from Intent
        Intent intent = getIntent();
        name = intent.getStringExtra("MEMBER_NAME");
        phone = intent.getStringExtra("MEMBER_PHONE");
        pin = intent.getStringExtra("MEMBER_PIN");
        role = intent.getStringExtra("MEMBER_ROLE");
        roleInMandal = intent.getStringExtra("MEMBER_ROLE_IN_MANDAL");
        photoUrl = intent.getStringExtra("MEMBER_PHOTO_URL");

        if (name == null) name = "सदस्य";
        if (phone == null) phone = "-";
        if (pin == null) pin = "1234";
        if (role == null) role = "USER";
        if (roleInMandal == null) roleInMandal = "सामान्य सदस्य";
        if (photoUrl == null) photoUrl = "";

        updateUi();

        // Show Admin controls if Admin
        if (isAdmin) {
            layoutPinRow.setVisibility(View.VISIBLE);
            layoutAdminActions.setVisibility(View.VISIBLE);
            btnEditMember.setOnClickListener(v -> showEditDialog());
            btnDeleteMember.setOnClickListener(v -> confirmDeleteMember());
        } else {
            layoutPinRow.setVisibility(View.GONE);
            layoutAdminActions.setVisibility(View.GONE);
        }

        btnCall.setOnClickListener(v -> {
            if (!phone.equals("-")) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                startActivity(dialIntent);
            }
        });
    }

    private void updateUi() {
        tvName.setText(name);
        tvPhone.setText(phone);
        tvRoleInMandal.setText(roleInMandal);
        tvPin.setText(pin);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(photoUrl, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivProfilePhoto.setImageBitmap(bitmap);
                } else {
                    ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } catch (Exception e) {
                ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_member, null);
        TextInputEditText etEditName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etEditPhone = dialogView.findViewById(R.id.etEditPhone);
        TextInputEditText etEditPin = dialogView.findViewById(R.id.etEditPin);
        AutoCompleteTextView actvEditRoleInMandal = dialogView.findViewById(R.id.actvEditRoleInMandal);

        etEditName.setText(name);
        etEditPhone.setText(phone);
        etEditPin.setText(pin);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, MANDAL_ROLES);
        actvEditRoleInMandal.setAdapter(adapter);
        actvEditRoleInMandal.setText(roleInMandal, false);

        new AlertDialog.Builder(this)
                .setTitle("सदस्य माहिती अपडेट करा")
                .setView(dialogView)
                .setPositiveButton("अपडेट करा", (dialog, which) -> {
                    String newName = etEditName.getText() != null ? etEditName.getText().toString().trim() : name;
                    String newPhone = etEditPhone.getText() != null ? etEditPhone.getText().toString().trim() : phone;
                    String newPin = etEditPin.getText() != null ? etEditPin.getText().toString().trim() : pin;
                    String newRoleInMandal = actvEditRoleInMandal.getText() != null ? actvEditRoleInMandal.getText().toString().trim() : roleInMandal;

                    if (newName.isEmpty() || newPhone.isEmpty() || newPin.isEmpty()) {
                        Toast.makeText(this, "कृपया आवश्यक माहिती भरा", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String oldPhone = phone;
                    name = newName;
                    phone = newPhone;
                    pin = newPin;
                    roleInMandal = newRoleInMandal;

                    User updatedUser = new User(name, phone, pin, role, roleInMandal, photoUrl);

                    // Sync update directly with MongoDB Atlas via API
                    ApiClient.getService().updateUser(oldPhone, updatedUser).enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                updateUi();
                                Toast.makeText(MemberDetailActivity.this, "डेटाबेसमध्ये माहिती यशस्वीरित्या अपडेट झाली!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MemberDetailActivity.this, "डेटाबेस अपडेट करण्यात अडचण आली", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(MemberDetailActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("रद्द करा", null)
                .show();
    }

    private void confirmDeleteMember() {
        new AlertDialog.Builder(this)
                .setTitle("सदस्य हटवा")
                .setMessage("तुम्हाला नक्की " + name + " या सदस्याला डेटाबेसमधून हटवायचे आहे का?")
                .setPositiveButton("हटवा", (dialog, which) -> deleteMember())
                .setNegativeButton("रद्द करा", null)
                .show();
    }

    private void deleteMember() {
        String phoneToDelete = phone;

        // Sync deletion directly with MongoDB Atlas cloud database via API
        ApiClient.getService().deleteUser(phoneToDelete).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(MemberDetailActivity.this, "सदस्य डेटाबेसमधून हटवला!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MemberDetailActivity.this, "डेटाबेस डिलीट एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
