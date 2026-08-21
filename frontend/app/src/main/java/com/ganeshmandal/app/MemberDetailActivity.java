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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.LoginResponse;
import com.ganeshmandal.app.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private boolean isSelf = false;

    private ImageView currentDialogPhotoView = null;
    private String selectedEditPhotoBase64 = "";
    private SharedPreferences prefs;

    private static final String[] MANDAL_ROLES = new String[] {
            "अध्यक्ष",
            "उपाध्यक्ष",
            "सचिव",
            "खजिनदार",
            "कार्यकर्ते",
            "सामान्य सदस्य"
    };

    private final ActivityResultLauncher<String> editPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedEditImage
    );

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

        prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String userRole = prefs.getString("USER_ROLE", "USER");
        String loggedInPhone = prefs.getString("USER_PHONE", "");
        isAdmin = "ADMIN".equalsIgnoreCase(userRole) || "SUPER_ADMIN".equalsIgnoreCase(userRole);

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
        selectedEditPhotoBase64 = photoUrl;

        isSelf = loggedInPhone != null && loggedInPhone.equals(phone);
        boolean canEdit = isAdmin || isSelf;

        updateUi();

        // Admin or Self can edit details & photo
        if (canEdit) {
            layoutAdminActions.setVisibility(View.VISIBLE);
            btnEditMember.setVisibility(View.VISIBLE);
            btnEditMember.setText(isSelf ? "✏️ माझी माहिती व फोटो बदला" : "✏️ माहिती व फोटो बदला");
            btnEditMember.setOnClickListener(v -> showEditDialog());
            ivProfilePhoto.setOnClickListener(v -> showEditDialog());

            if (isAdmin) {
                layoutPinRow.setVisibility(View.VISIBLE);
                btnDeleteMember.setVisibility(View.VISIBLE);
                btnDeleteMember.setOnClickListener(v -> confirmDeleteMember());
            } else {
                layoutPinRow.setVisibility(View.VISIBLE);
                btnDeleteMember.setVisibility(View.GONE);
            }
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

        ivProfilePhoto.setImageTintList(null);
        loadPhotoIntoView(photoUrl, ivProfilePhoto);
    }

    private void loadPhotoIntoView(String urlOrBase64, ImageView targetView) {
        if (targetView == null) return;
        targetView.setImageTintList(null);

        if (urlOrBase64 != null && !urlOrBase64.trim().isEmpty()) {
            if (urlOrBase64.startsWith("http://") || urlOrBase64.startsWith("https://")) {
                Glide.with(this)
                        .load(urlOrBase64)
                        .circleCrop()
                        .placeholder(R.drawable.app_logo)
                        .error(R.drawable.app_logo)
                        .into(targetView);
            } else {
                try {
                    String cleanBase64 = urlOrBase64;
                    if (cleanBase64.contains(",")) {
                        cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                    }
                    byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        Glide.with(this).load(bitmap).circleCrop().into(targetView);
                    } else {
                        Glide.with(this).load(R.drawable.app_logo).circleCrop().into(targetView);
                    }
                } catch (Exception e) {
                    Glide.with(this).load(R.drawable.app_logo).circleCrop().into(targetView);
                }
            }
        } else {
            Glide.with(this).load(R.drawable.app_logo).circleCrop().into(targetView);
        }
    }

    private void handleSelectedEditImage(Uri uri) {
        if (uri == null) return;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (originalBitmap == null) return;

            int maxDimension = 500;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
            Bitmap scaled = Bitmap.createScaledBitmap(originalBitmap, Math.round(width * ratio), Math.round(height * ratio), true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] bytes = baos.toByteArray();
            selectedEditPhotoBase64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);

            if (currentDialogPhotoView != null) {
                loadPhotoIntoView(selectedEditPhotoBase64, currentDialogPhotoView);
            }
            Toast.makeText(this, "नवीन फोटो निवडला गेला! अपडेट बटण दाबा.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "फोटो लोड करताना एरर: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_member, null);
        ImageView ivEditMemberPhoto = dialogView.findViewById(R.id.ivEditMemberPhoto);
        TextView btnSelectEditPhoto = dialogView.findViewById(R.id.btnSelectEditPhoto);
        TextInputEditText etEditName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etEditPhone = dialogView.findViewById(R.id.etEditPhone);
        TextInputEditText etEditPin = dialogView.findViewById(R.id.etEditPin);
        AutoCompleteTextView actvEditRoleInMandal = dialogView.findViewById(R.id.actvEditRoleInMandal);

        currentDialogPhotoView = ivEditMemberPhoto;
        selectedEditPhotoBase64 = photoUrl;
        loadPhotoIntoView(photoUrl, ivEditMemberPhoto);

        View.OnClickListener photoPickListener = v -> editPhotoLauncher.launch("image/*");
        ivEditMemberPhoto.setOnClickListener(photoPickListener);
        btnSelectEditPhoto.setOnClickListener(photoPickListener);

        etEditName.setText(name);
        etEditPhone.setText(phone);
        etEditPin.setText(pin);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, MANDAL_ROLES);
        actvEditRoleInMandal.setAdapter(adapter);
        actvEditRoleInMandal.setText(roleInMandal, false);

        if (!isAdmin) {
            actvEditRoleInMandal.setEnabled(false); // Non-admin cannot change their own mandal role
        }

        new AlertDialog.Builder(this)
                .setTitle(isSelf ? "माझी माहिती व फोटो अपडेट" : "सदस्य माहिती व फोटो अपडेट")
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
                    String newPhoto = selectedEditPhotoBase64;

                    Toast.makeText(MemberDetailActivity.this, "डेटाबेस व Cloudinary वर सेव्ह होत आहे...", Toast.LENGTH_SHORT).show();

                    User updatedUser = new User(name, phone, pin, role, roleInMandal, newPhoto);

                    // Sync update directly with MongoDB Atlas & Cloudinary via API
                    ApiClient.getService().updateUser(oldPhone, updatedUser).enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                if (response.body().getUser() != null && response.body().getUser().getPhotoUrl() != null) {
                                    photoUrl = response.body().getUser().getPhotoUrl();
                                } else {
                                    photoUrl = newPhoto;
                                }

                                if (isSelf) {
                                    prefs.edit()
                                            .putString("USER_NAME", name)
                                            .putString("USER_PHONE", phone)
                                            .putString("USER_PHOTO_URL", photoUrl)
                                            .apply();
                                }

                                updateUi();
                                Toast.makeText(MemberDetailActivity.this, "माहिती व फोटो यशस्वीरित्या अपडेट झाला!", Toast.LENGTH_LONG).show();
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
