package com.ganeshmandal.app;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.MandalAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.LoginResponse;
import com.ganeshmandal.app.models.Mandal;
import com.ganeshmandal.app.models.MandalListResponse;
import com.ganeshmandal.app.models.SingleMandalResponse;
import com.ganeshmandal.app.models.User;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuperAdminActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialButton btnAddMandal;
    private TextView tvTotalMandals;
    private RecyclerView rvMandals;
    private MandalAdapter adapter;
    private List<Mandal> mandalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        btnBack = findViewById(R.id.btnBack);
        btnAddMandal = findViewById(R.id.btnAddMandal);
        tvTotalMandals = findViewById(R.id.tvTotalMandals);
        rvMandals = findViewById(R.id.rvMandals);

        btnBack.setOnClickListener(v -> finish());
        btnAddMandal.setOnClickListener(v -> showAddMandalDialog());

        rvMandals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MandalAdapter();
        rvMandals.setAdapter(adapter);

        adapter.setListener(new MandalAdapter.OnMandalClickListener() {
            @Override
            public void onEditClick(Mandal mandal) {
                showEditMandalDialog(mandal);
            }

            @Override
            public void onAddAdminClick(Mandal mandal) {
                showAddAdminDialog(mandal);
            }
        });

        fetchMandals();
    }

    private void fetchMandals() {
        ApiClient.getService().getMandals().enqueue(new Callback<MandalListResponse>() {
            @Override
            public void onResponse(Call<MandalListResponse> call, Response<MandalListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    mandalList = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                    adapter.setMandals(mandalList);
                    tvTotalMandals.setText(String.valueOf(mandalList.size()));
                } else {
                    Toast.makeText(SuperAdminActivity.this, "मंडळांची यादी लोड करू शकलो नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MandalListResponse> call, Throwable t) {
                Toast.makeText(SuperAdminActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddMandalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ नवीन मंडळ जोडा");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(this);
        etName.setHint("मंडळाचे नाव (उदा. जय महाराष्ट्र तरुण मंडळ)");
        layout.addView(etName);

        final EditText etAddress = new EditText(this);
        etAddress.setHint("पत्ता (उदा. कोथरूड, पुणे)");
        layout.addView(etAddress);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("संपर्क नंबर");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        builder.setView(layout);

        builder.setPositiveButton("मंडळ तयार करा", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(SuperAdminActivity.this, "कृपया मंडळाचे नाव टाका", Toast.LENGTH_SHORT).show();
                return;
            }

            Mandal newMandal = new Mandal(name, address, phone);
            ApiClient.getService().addMandal(newMandal).enqueue(new Callback<SingleMandalResponse>() {
                @Override
                public void onResponse(Call<SingleMandalResponse> call, Response<SingleMandalResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(SuperAdminActivity.this, "नवीन मंडळ यशस्वीरीत्या जोडले!", Toast.LENGTH_LONG).show();
                        fetchMandals();
                    } else {
                        Toast.makeText(SuperAdminActivity.this, "मंडळ जोडताना एरर आला", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SingleMandalResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("रद्द करा", null);
        builder.show();
    }

    private void showEditMandalDialog(Mandal mandal) {
        if (mandal == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ मंडळाचे नाव बदला (" + mandal.getMandalId() + ")");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(this);
        etName.setHint("मंडळाचे नाव");
        etName.setText(mandal.getMandalName());
        layout.addView(etName);

        final EditText etAddress = new EditText(this);
        etAddress.setHint("पत्ता");
        etAddress.setText(mandal.getAddress());
        layout.addView(etAddress);

        builder.setView(layout);

        builder.setPositiveButton("माहिती बदला", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(SuperAdminActivity.this, "कृपया मंडळाचे नाव टाका", Toast.LENGTH_SHORT).show();
                return;
            }

            mandal.setMandalName(name);
            mandal.setAddress(address);

            ApiClient.getService().updateMandal(mandal.getMandalId(), mandal).enqueue(new Callback<SingleMandalResponse>() {
                @Override
                public void onResponse(Call<SingleMandalResponse> call, Response<SingleMandalResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(SuperAdminActivity.this, "मंडळाची माहिती अपडेट झाली!", Toast.LENGTH_SHORT).show();
                        fetchMandals();
                    } else {
                        Toast.makeText(SuperAdminActivity.this, "अपडेट करताना एरर आला", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SingleMandalResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("रद्द करा", null);
        builder.show();
    }

    private void showAddAdminDialog(Mandal mandal) {
        if (mandal == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👤 ॲडमिन जोडा (" + mandal.getMandalName() + ")");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(this);
        etName.setHint("ॲडमिनचे नाव (उदा. राहुल माने)");
        layout.addView(etName);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("मोबाईल नंबर");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        final EditText etPin = new EditText(this);
        etPin.setHint("पासवर्ड / पिन (उदा. 1234)");
        etPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(etPin);

        builder.setView(layout);

        builder.setPositiveButton("ॲडमिन नियुक्त करा", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String pin = etPin.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                Toast.makeText(SuperAdminActivity.this, "सर्व माहिती भरणे आवश्यक आहे", Toast.LENGTH_SHORT).show();
                return;
            }

            User newAdmin = new User(name, phone, pin, "ADMIN", "मुख्य व्यवस्थापक", "");
            newAdmin.setMandalId(mandal.getMandalId());

            ApiClient.getService().addUser(newAdmin).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(SuperAdminActivity.this, "मंडळाचा ॲडमिन यशस्वीरीत्या जोडला गेला!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SuperAdminActivity.this, "ॲडमिन जोडताना एरर आला", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("रद्द करा", null);
        builder.show();
    }
}
