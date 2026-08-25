package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
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

        btnLogin.setOnClickListener(v -> performLogin());
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
}
