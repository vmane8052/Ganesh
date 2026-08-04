package com.ganeshmandal.app;

import android.app.DatePickerDialog;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvFormTitle, tvDetailsLabel;
    private TextInputEditText etAmount, etDetails, etDate, etMemberName;
    private MaterialButton btnSave;
    private String transactionType = "JAMA"; // default
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        btnBack = findViewById(R.id.btnBack);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvDetailsLabel = findViewById(R.id.tvDetailsLabel);
        etAmount = findViewById(R.id.etAmount);
        etDetails = findViewById(R.id.etDetails);
        etDate = findViewById(R.id.etDate);
        etMemberName = findViewById(R.id.etMemberName);
        btnSave = findViewById(R.id.btnSave);

        transactionType = getIntent().getStringExtra("TYPE");
        if (transactionType == null) transactionType = "JAMA";

        if ("KHARCH".equalsIgnoreCase(transactionType)) {
            tvFormTitle.setText("खर्च करा");
            if (tvDetailsLabel != null) tvDetailsLabel.setText("कशासाठी खर्च केले? *");
            btnSave.setBackgroundColor(getResources().getColor(R.color.kharch_red));
        } else {
            tvFormTitle.setText("जमा करा");
            if (tvDetailsLabel != null) tvDetailsLabel.setText("कशासाठी जमा झाले / देणगीदार नांव *");
            btnSave.setBackgroundColor(getResources().getColor(R.color.jama_green));
        }

        // Set today's date by default
        updateDateInField();

        // Open DatePickerDialog calendar on date click
        etDate.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateInField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateInField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("mr", "IN"));
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String details = etDetails.getText() != null ? etDetails.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String memberName = etMemberName != null && etMemberName.getText() != null && !etMemberName.getText().toString().trim().isEmpty() 
                ? etMemberName.getText().toString().trim() : details;

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
                "JAMA".equals(transactionType) ? "देणगी/जमा" : "मंडप/कार्यक्रम खर्च",
                memberName
        );

        // Save locally first for 100% instant reliability
        saveLocally(tx);

        ApiClient.getService().addTransaction(tx).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText(getString(R.string.btn_save));
                Toast.makeText(AddTransactionActivity.this, "व्यवहार यशस्वीरित्या साठवला!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(getString(R.string.btn_save));
                Toast.makeText(AddTransactionActivity.this, "व्यवहार साठवला!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveLocally(Transaction tx) {
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String existingJson = prefs.getString("LOCAL_TXS", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Transaction>>() {}.getType();
        List<Transaction> list = gson.fromJson(existingJson, type);
        if (list == null) list = new ArrayList<>();
        list.add(0, tx);
        prefs.edit().putString("LOCAL_TXS", gson.toJson(list)).apply();
    }
}
