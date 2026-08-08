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
    private MaterialButton btnLogin, btnQuickAdmin, btnQuickUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPin = findViewById(R.id.etPin);
        btnLogin = findViewById(R.id.btnLogin);
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin);
        btnQuickUser = findViewById(R.id.btnQuickUser);

        btnQuickAdmin.setOnClickListener(v -> {
            etPhone.setText("9999999999");
            etPin.setText("1234");
            performLogin();
        });

        btnQuickUser.setOnClickListener(v -> {
            etPhone.setText("8888888888");
            etPin.setText("1234");
            performLogin();
        });

        btnLogin.setOnClickListener(v -> performLogin());
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

        // 100% Strict Real-Time MongoDB Atlas Authentication
        ApiClient.getService().login(creds).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText(getString(R.string.btn_login));

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getUser() != null) {
                    User u = response.body().getUser();
                    SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("USER_ROLE", u.getRole())
                            .putString("USER_NAME", u.getName())
                            .putString("USER_PHONE", u.getPhone())
                            .putString("USER_ROLE_IN_MANDAL", u.getRoleInMandal())
                            .putString("USER_PHOTO_URL", u.getPhotoUrl() != null ? u.getPhotoUrl() : "")
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
