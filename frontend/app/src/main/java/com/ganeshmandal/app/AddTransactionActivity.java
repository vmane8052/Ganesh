package com.ganeshmandal.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.SingleTransactionResponse;
import com.ganeshmandal.app.models.Transaction;
import com.ganeshmandal.app.models.User;
import com.ganeshmandal.app.models.UserListResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvFormTitle, tvDetailsLabel;
    private LinearLayout layoutMemberName;
    private TextInputEditText etAmount, etDate, etMemberPhone;
    private AutoCompleteTextView etDetails, etMemberName;
    private MaterialButton btnSave;
    private String transactionType = "JAMA"; // default
    private final Calendar calendar = Calendar.getInstance();

    private final Map<String, String> memberNameToPhoneMap = new HashMap<>();
    private final List<String> memberNamesList = new ArrayList<>();

    private static final String[] JAMA_OPTIONS = new String[]{
            "वर्गणी",
            "देणगी",
            "आरती देणगी",
            "महाप्रसाद देणगी",
            "इतर देणगी"
    };

    private static final String[] KHARCH_OPTIONS = new String[]{
            "मंडप व डेकोरेशन",
            "लाईट व विद्युत रोषणाई",
            "ध्वनीक्षेपक (साउंड/स्पीकर)",
            "महाप्रसाद व भोजन",
            "पूजा साहित्य व हार-फुले",
            "फटाके व आतषबाजी",
            "वाहतूक खर्च",
            "इतर मंडळ खर्च"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        btnBack = findViewById(R.id.btnBack);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvDetailsLabel = findViewById(R.id.tvDetailsLabel);
        layoutMemberName = findViewById(R.id.layoutMemberName);
        etAmount = findViewById(R.id.etAmount);
        etDetails = findViewById(R.id.etDetails);
        etDate = findViewById(R.id.etDate);
        etMemberName = findViewById(R.id.etMemberName);
        etMemberPhone = findViewById(R.id.etMemberPhone);
        btnSave = findViewById(R.id.btnSave);

        transactionType = getIntent().getStringExtra("TYPE");
        if (transactionType == null) transactionType = "JAMA";

        if ("KHARCH".equalsIgnoreCase(transactionType)) {
            tvFormTitle.setText("खर्च करा");
            if (tvDetailsLabel != null) tvDetailsLabel.setText("कशासाठी खर्च केला? (तपशील निवडा किंवा लिहा) *");
            if (layoutMemberName != null) layoutMemberName.setVisibility(View.GONE);
            btnSave.setBackgroundColor(getResources().getColor(R.color.kharch_red));
            etDetails.setHint("खर्चाचा तपशील निवडा (उदा. लाईट, मंडप)");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, KHARCH_OPTIONS);
            etDetails.setAdapter(adapter);
        } else {
            tvFormTitle.setText("जमा करा");
            if (tvDetailsLabel != null) tvDetailsLabel.setText("तपशील (वर्गणी / देणगी निवडा किंवा लिहा) *");
            if (layoutMemberName != null) layoutMemberName.setVisibility(View.VISIBLE);
            btnSave.setBackgroundColor(getResources().getColor(R.color.jama_green));
            etDetails.setHint("वर्गणी / देणगी निवडा");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, JAMA_OPTIONS);
            etDetails.setAdapter(adapter);

            // Fetch registered Mandal members for dropdown
            fetchMembersForDropdown();
        }

        // Set today's date by default
        updateDateInField();

        // Open DatePickerDialog calendar on date click
        etDate.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void fetchMembersForDropdown() {
        ApiClient.getService().getUsers().enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<User> users = response.body().getData();
                    if (users != null) {
                        memberNamesList.clear();
                        memberNameToPhoneMap.clear();

                        for (User u : users) {
                            if (u.getName() != null && !u.getName().isEmpty()) {
                                memberNamesList.add(u.getName());
                                memberNameToPhoneMap.put(u.getName(), u.getPhone() != null ? u.getPhone() : "");
                            }
                        }

                        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                                AddTransactionActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                memberNamesList
                        );
                        etMemberName.setAdapter(memberAdapter);

                        // Auto fill mobile number on selection
                        etMemberName.setOnItemClickListener((parent, view, position, id) -> {
                            String selectedName = (String) parent.getItemAtPosition(position);
                            String phone = memberNameToPhoneMap.get(selectedName);
                            if (phone != null && etMemberPhone != null) {
                                etMemberPhone.setText(phone);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                // Ignore network error for dropdown
            }
        });
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
                ? etMemberName.getText().toString().trim() : ("KHARCH".equals(transactionType) ? "मंडळ खर्च" : "सदस्य");
        String memberPhone = etMemberPhone != null && etMemberPhone.getText() != null ? etMemberPhone.getText().toString().trim() : "";

        if (amountStr.isEmpty() || details.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "कृपया सर्व आवश्यक माहिती भरा (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("JAMA".equals(transactionType) && (etMemberName == null || etMemberName.getText() == null || etMemberName.getText().toString().trim().isEmpty())) {
            Toast.makeText(this, "कृपया सदस्याचे / देणगीदाराचे नांव टाका (*)", Toast.LENGTH_SHORT).show();
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
        btnSave.setText("डेटाबेसमध्ये साठवत आहे...");

        String receiptNo = "REC-" + Calendar.getInstance().get(Calendar.YEAR) + "-" + (1000 + (int)(Math.random() * 9000));

        Transaction tx = new Transaction(
                transactionType,
                amount,
                details,
                date,
                "JAMA".equals(transactionType) ? details : "मंडळ खर्च",
                memberName,
                memberPhone,
                receiptNo
        );

        // Send directly to MongoDB Atlas Cloud API (100% Strict Cloud Saving)
        ApiClient.getService().addTransaction(tx).enqueue(new Callback<SingleTransactionResponse>() {
            @Override
            public void onResponse(Call<SingleTransactionResponse> call, Response<SingleTransactionResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText(getString(R.string.btn_save));
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddTransactionActivity.this, "व्यवहार साठवला गेला!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, "डेटाबेस एरर: व्यवहार साठवता आला नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SingleTransactionResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(getString(R.string.btn_save));
                Toast.makeText(AddTransactionActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
