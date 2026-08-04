package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.LoginResponse;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPin = findViewById(R.id.etPin);
        btnLogin = findViewById(R.id.btnLogin);

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
        btnLogin.setText("लॉगिन करत आहे...");

        Map<String, String> creds = new HashMap<>();
        creds.put("phone", phone);
        creds.put("pin", pin);

        ApiClient.getService().login(creds).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText(getString(R.string.btn_login));

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("USER_ROLE", response.body().getUser().getRole())
                            .putString("USER_NAME", response.body().getUser().getName())
                            .putString("USER_PHONE", response.body().getUser().getPhone())
                            .apply();

                    String welcome = "स्वागत आहे, " + response.body().getUser().getName();
                    Toast.makeText(LoginActivity.this, welcome, Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "चुकीचा मोबाईल नंबर किंवा पिन (9999999999 / 1234 वापरा)", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText(getString(R.string.btn_login));
                Toast.makeText(LoginActivity.this, "सर्व्हरशी संपर्क होऊ शकला नाही: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
