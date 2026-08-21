package com.ganeshmandal.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMemberActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePhoto;
    private TextView btnSelectPhoto;
    private TextInputEditText etName, etPhone, etPin;
    private AutoCompleteTextView actvRoleInMandal;
    private MaterialButton btnSaveMember;
    private String selectedPhotoBase64 = "";

    private static final String[] MANDAL_ROLES = new String[] {
            "अध्यक्ष",
            "उपाध्यक्ष",
            "सचिव",
            "खजिनदार",
            "कार्यकर्ते",
            "सदस्य"
    };

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        ivProfilePhoto.setImageURI(uri);
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        if (bitmap != null) {
                            int maxDimension = 500;
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            if (width > maxDimension || height > maxDimension) {
                                float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
                                width = Math.round(ratio * width);
                                height = Math.round(ratio * height);
                                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                            }
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
        actvRoleInMandal = findViewById(R.id.actvRoleInMandal);
        btnSaveMember = findViewById(R.id.btnSaveMember);

        // Populate Mandal Roles Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, MANDAL_ROLES);
        actvRoleInMandal.setAdapter(adapter);
        actvRoleInMandal.setText(MANDAL_ROLES[5], false); // Default: "सामान्य सदस्य"

        btnBack.setOnClickListener(v -> finish());

        ivProfilePhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        btnSelectPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));

        btnSaveMember.setOnClickListener(v -> saveMember());
    }

    private void saveMember() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String pin = etPin.getText() != null ? etPin.getText().toString().trim() : "";
        String roleInMandal = actvRoleInMandal.getText() != null ? actvRoleInMandal.getText().toString().trim() : "सामान्य सदस्य";

        if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "कृपया नाव, मोबाईल नंबर आणि पासवर्ड भरा (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10) {
            Toast.makeText(this, "कृपया योग्य १० अंकी मोबाईल नंबर टाका", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveMember.setEnabled(false);
        btnSaveMember.setText("डेटाबेसमध्ये साठवत आहे...");

        User newUser = new User(name, phone, pin, "USER", roleInMandal, selectedPhotoBase64);
        String mandalId = getSharedPreferences("MandalPrefs", MODE_PRIVATE).getString("MANDAL_ID", "M001");
        newUser.setMandalId(mandalId);

        // Send directly to MongoDB Atlas Cloud API (100% Strict Cloud Saving)
        ApiClient.getService().addUser(newUser).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnSaveMember.setEnabled(true);
                btnSaveMember.setText("सदस्य साठवा");
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddMemberActivity.this, "सदस्य MongoDB डेटाबेसमध्ये साठवला गेला!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "डेटाबेस एरर";
                    Toast.makeText(AddMemberActivity.this, "एरर: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnSaveMember.setEnabled(true);
                btnSaveMember.setText("सदस्य साठवा");
                Toast.makeText(AddMemberActivity.this, "नेटवर्क / डेटाबेस कनेक्ट होऊ शकले नाही: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
