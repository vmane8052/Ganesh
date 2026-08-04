package com.ganeshmandal.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.Transaction;
import com.ganeshmandal.app.models.TransactionResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvFormTitle;
    private TextInputEditText etAmount, etDetails, etDate, etMemberName;
    private MaterialButton btnSave;
    private String transactionType = "JAMA"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        btnBack = findViewById(R.id.btnBack);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etAmount = findViewById(R.id.etAmount);
        etDetails = findViewById(R.id.etDetails);
        etDate = findViewById(R.id.etDate);
        etMemberName = findViewById(R.id.etMemberName);
        btnSave = findViewById(R.id.btnSave);

        transactionType = getIntent().getStringExtra("TYPE");
        if (transactionType == null) transactionType = "JAMA";

        if ("KHARCH".equalsIgnoreCase(transactionType)) {
            tvFormTitle.setText("खर्च करा");
            btnSave.setBackgroundColor(getResources().getColor(R.color.kharch_red));
        } else {
            tvFormTitle.setText("जमा करा");
            btnSave.setBackgroundColor(getResources().getColor(R.color.jama_green));
        }

        // Set today's date by default
        String today = new SimpleDateFormat("dd MMM yyyy", new Locale("mr", "IN")).format(new Date());
        etDate.setText(today);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String details = etDetails.getText() != null ? etDetails.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String memberName = etMemberName.getText() != null ? etMemberName.getText().toString().trim() : "सदस्य";

        if (amountStr.isEmpty() || details.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "कृपया सर्व आवश्यक माहिती भरा (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "योग्य रक्कम टाका", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("साठवत आहे...");

        Transaction tx = new Transaction(
                transactionType,
                amount,
                details,
                date,
                "JAMA".equals(transactionType) ? "देणगी" : "मंडप/कार्यक्रम खर्च",
                memberName
        );

        ApiClient.getService().addTransaction(tx).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText(getString(R.string.btn_save));

                if (response.isSuccessful()) {
                    Toast.makeText(AddTransactionActivity.this, "व्यवहार यशस्वीरित्या साठवला!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, "साठवण्यात अडचण आली", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(getString(R.string.btn_save));
                Toast.makeText(AddTransactionActivity.this, "सर्व्हरशी संपर्क होऊ शकला नाही: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
