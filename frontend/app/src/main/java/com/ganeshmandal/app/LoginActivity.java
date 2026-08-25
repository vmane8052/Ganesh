package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.GenericResponse;
import com.ganeshmandal.app.models.LoginResponse;
import com.ganeshmandal.app.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etPhone, etPin;
    private MaterialButton btnLogin;
    private TextView tvForgotPin;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient.init(getApplicationContext());

        prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        String savedPhone = prefs.getString("USER_PHONE", null);
        String savedPin = prefs.getString("USER_PIN", null);

        // Verify Auto-Login credentials live with MongoDB Atlas
        if (isLoggedIn && savedPhone != null && !savedPhone.trim().isEmpty() && savedPin != null && !savedPin.trim().isEmpty()) {
            verifyAutoLogin(savedPhone, savedPin);
            return;
        }

        initLoginUi();
    }

    private void initLoginUi() {
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPin = findViewById(R.id.etPin);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPin = findViewById(R.id.tvForgotPin);

        btnLogin.setOnClickListener(v -> performLogin());
        if (tvForgotPin != null) {
            tvForgotPin.setOnClickListener(v -> showForgotPinDialog());
        }
    }

    private void verifyAutoLogin(String phone, String pin) {
        Map<String, String> creds = new HashMap<>();
        creds.put("phone", phone);
        creds.put("pin", pin);

        ApiClient.getService().login(creds).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getUser() != null) {
                    User u = response.body().getUser();
                    String token = response.body().getToken();
                    prefs.edit()
                            .putString("USER_ROLE", u.getRole())
                            .putString("USER_NAME", u.getName())
                            .putString("USER_PHONE", u.getPhone())
                            .putString("USER_PIN", pin)
                            .putString("USER_ROLE_IN_MANDAL", u.getRoleInMandal())
                            .putString("USER_PHOTO_URL", u.getPhotoUrl() != null ? u.getPhotoUrl() : "")
                            .putString("MANDAL_ID", u.getMandalId())
                            .putString("MANDAL_NAME", u.getMandalName())
                            .putString("MANDAL_ADDRESS", u.getMandalAddress())
                            .putString("MANDAL_LOGO_URL", u.getMandalLogoUrl())
                            .putString("JWT_TOKEN", token != null ? token : "")
                            .apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    prefs.edit().clear().apply();
                    initLoginUi();
                    Toast.makeText(LoginActivity.this, "सत्र संपले आहे. कृपया पुन्हा लॉगिन करा.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // If offline, allow opening main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void performLogin() {
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String pin = etPin.getText() != null ? etPin.getText().toString().trim() : "";

        if (phone.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "कृपया मोबाईल नंबर आणि पिन टाका", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("डेटाबेसमध्ये तपासत आहे...");

        Map<String, String> creds = new HashMap<>();
        creds.put("phone", phone);
        creds.put("pin", pin);

        ApiClient.getService().login(creds).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText(getString(R.string.btn_login));

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getUser() != null) {
                    User u = response.body().getUser();
                    String token = response.body().getToken();
                    prefs.edit()
                            .putBoolean("IS_LOGGED_IN", true)
                            .putString("USER_ROLE", u.getRole())
                            .putString("USER_NAME", u.getName())
                            .putString("USER_PHONE", u.getPhone())
                            .putString("USER_PIN", pin) // Store PIN to detect password changes
                            .putString("USER_ROLE_IN_MANDAL", u.getRoleInMandal())
                            .putString("USER_PHOTO_URL", u.getPhotoUrl() != null ? u.getPhotoUrl() : "")
                            .putString("MANDAL_ID", u.getMandalId())
                            .putString("MANDAL_NAME", u.getMandalName())
                            .putString("MANDAL_ADDRESS", u.getMandalAddress())
                            .putString("MANDAL_LOGO_URL", u.getMandalLogoUrl())
                            .putString("JWT_TOKEN", token != null ? token : "")
                            .apply();

                    String welcome = "स्वागत आहे, " + u.getName();
                    Toast.makeText(LoginActivity.this, welcome, Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = (response.body() != null && response.body().getMessage() != null) ? response.body().getMessage() : "चुकीचा मोबाईल नंबर किंवा पिन";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText(getString(R.string.btn_login));
                Toast.makeText(LoginActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showForgotPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❓ PIN विसरलात? (Reset PIN)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        final TextInputEditText inputPhone = new TextInputEditText(this);
        inputPhone.setHint("📱 मोबाईल नंबर");
        inputPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        if (etPhone != null && etPhone.getText() != null) {
            inputPhone.setText(etPhone.getText().toString().trim());
        }
        layout.addView(inputPhone);

        final TextInputEditText inputOtp = new TextInputEditText(this);
        inputOtp.setHint("🔢 6-अंकी OTP");
        inputOtp.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputOtp.setVisibility(View.GONE);
        layout.addView(inputOtp);

        final TextInputEditText inputNewPin = new TextInputEditText(this);
        inputNewPin.setHint("🔒 नवा 4-अंकी PIN");
        inputNewPin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputNewPin.setVisibility(View.GONE);
        layout.addView(inputNewPin);

        builder.setView(layout);
        builder.setPositiveButton("OTP मागवा", null);
        builder.setNegativeButton("रद्द करा", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String phone = inputPhone.getText() != null ? inputPhone.getText().toString().trim() : "";
            String otp = inputOtp.getText() != null ? inputOtp.getText().toString().trim() : "";
            String newPin = inputNewPin.getText() != null ? inputNewPin.getText().toString().trim() : "";

            if (inputOtp.getVisibility() == View.GONE) {
                // Step 1: Request OTP
                if (phone.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "कृपया मोबाईल नंबर टाका", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> body = new HashMap<>();
                body.put("phone", phone);

                ApiClient.getService().forgotPin(body).enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            inputPhone.setEnabled(false);
                            inputOtp.setText("");
                            inputOtp.setVisibility(View.VISIBLE);
                            inputNewPin.setVisibility(View.VISIBLE);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("PIN रीसेट करा");
                        } else {
                            String err = response.body() != null ? response.body().getMessage() : "OTP पाठवताना अडचण आली";
                            Toast.makeText(LoginActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Step 2: Reset PIN
                if (otp.isEmpty() || newPin.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "कृपया OTP आणि नवा PIN प्रविष्ट करा", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> body = new HashMap<>();
                body.put("phone", phone);
                body.put("otp", otp);
                body.put("newPin", newPin);

                ApiClient.getService().resetPin(body).enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            if (etPhone != null) etPhone.setText(phone);
                            if (etPin != null) etPin.setText(newPin);
                            dialog.dismiss();
                        } else {
                            String err = response.body() != null ? response.body().getMessage() : "PIN अपडेट करण्यात त्रुटी आली";
                            Toast.makeText(LoginActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
