package com.ganeshmandal.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.Donation;
import com.ganeshmandal.app.models.SingleDonationResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDonationActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvFormTitle;
    private TextInputEditText etDonorName, etAmount, etItemDetails, etDonorPhone, etDate, etAddress, etReceiptNo;
    private RadioGroup rgDonationType;
    private RadioButton rbCash, rbItem, rbOnline;
    private LinearLayout layoutAmount, layoutItemDetails;
    private MaterialButton btnSaveDonation;
    private final Calendar calendar = Calendar.getInstance();
    private Donation editingDonation = null;
    private String selectedType = "CASH"; // CASH, ITEM, ONLINE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation);

        btnBack = findViewById(R.id.btnBack);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etDonorName = findViewById(R.id.etDonorName);
        etAmount = findViewById(R.id.etAmount);
        etItemDetails = findViewById(R.id.etItemDetails);
        etDonorPhone = findViewById(R.id.etDonorPhone);
        etDate = findViewById(R.id.etDate);
        etAddress = findViewById(R.id.etAddress);
        etReceiptNo = findViewById(R.id.etReceiptNo);
        rgDonationType = findViewById(R.id.rgDonationType);
        rbCash = findViewById(R.id.rbCash);
        rbItem = findViewById(R.id.rbItem);
        rbOnline = findViewById(R.id.rbOnline);
        layoutAmount = findViewById(R.id.layoutAmount);
        layoutItemDetails = findViewById(R.id.layoutItemDetails);
        btnSaveDonation = findViewById(R.id.btnSaveDonation);

        rgDonationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbItem) {
                selectedType = "ITEM";
                layoutAmount.setVisibility(View.GONE);
                layoutItemDetails.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbOnline) {
                selectedType = "ONLINE";
                layoutAmount.setVisibility(View.VISIBLE);
                layoutItemDetails.setVisibility(View.GONE);
            } else {
                selectedType = "CASH";
                layoutAmount.setVisibility(View.VISIBLE);
                layoutItemDetails.setVisibility(View.GONE);
            }
        });

        // Check if editing
        if (getIntent().hasExtra("DONATION_DATA")) {
            editingDonation = (Donation) getIntent().getSerializableExtra("DONATION_DATA");
            if (editingDonation != null) {
                tvFormTitle.setText("देणगी नोंद संपादित करा");
                etDonorName.setText(editingDonation.getDonorName());
                etDonorPhone.setText(editingDonation.getDonorPhone());
                etDate.setText(editingDonation.getDate());
                etAddress.setText(editingDonation.getAddress());
                etReceiptNo.setText(editingDonation.getReceiptNo());

                if (editingDonation.isItem()) {
                    rbItem.setChecked(true);
                    etItemDetails.setText(editingDonation.getItemDetails());
                } else if ("ONLINE".equalsIgnoreCase(editingDonation.getDonationType())) {
                    rbOnline.setChecked(true);
                    etAmount.setText(String.valueOf((int) editingDonation.getAmount()));
                } else {
                    rbCash.setChecked(true);
                    etAmount.setText(String.valueOf((int) editingDonation.getAmount()));
                }

                btnSaveDonation.setText("💾 बदल साठवा");
            }
        } else {
            updateDateField();
        }

        etDate.setOnClickListener(v -> showDatePicker());
        btnBack.setOnClickListener(v -> finish());
        btnSaveDonation.setOnClickListener(v -> saveDonation());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("mr", "IN"));
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveDonation() {
        String donorName = etDonorName.getText() != null ? etDonorName.getText().toString().trim() : "";
        String phone = etDonorPhone.getText() != null ? etDonorPhone.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        String receiptNo = etReceiptNo.getText() != null ? etReceiptNo.getText().toString().trim() : "";

        if (donorName.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "कृपया देणगीदाराचे नाव आणि तारीख भरा (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = 0;
        String itemDetails = "";

        if ("ITEM".equals(selectedType)) {
            itemDetails = etItemDetails.getText() != null ? etItemDetails.getText().toString().trim() : "";
            if (itemDetails.isEmpty()) {
                Toast.makeText(this, "कृपया वस्तू देणगीचे नाव व तपशील लिहा (*)", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "कृपया देणगी रक्कम भरा (*)", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "योग्य रक्कम टाका", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnSaveDonation.setEnabled(false);
        btnSaveDonation.setText("डेटाबेसमध्ये साठवत आहे...");

        Donation donation = new Donation(donorName, phone, selectedType, amount, itemDetails, date, address, receiptNo);

        if (editingDonation != null && editingDonation.getId() != null) {
            // Update Existing Donation
            ApiClient.getService().updateDonation(editingDonation.getId(), donation).enqueue(new Callback<SingleDonationResponse>() {
                @Override
                public void onResponse(Call<SingleDonationResponse> call, Response<SingleDonationResponse> response) {
                    btnSaveDonation.setEnabled(true);
                    btnSaveDonation.setText("💾 बदल साठवा");
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddDonationActivity.this, "देणगी नोंद यशस्वीरीत्या अपडेट केली!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddDonationActivity.this, "अपडेट करता आले नाही", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SingleDonationResponse> call, Throwable t) {
                    btnSaveDonation.setEnabled(true);
                    btnSaveDonation.setText("💾 बदल साठवा");
                    Toast.makeText(AddDonationActivity.this, "डेटाबेस एरर: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Add New Donation
            ApiClient.getService().addDonation(donation).enqueue(new Callback<SingleDonationResponse>() {
                @Override
                public void onResponse(Call<SingleDonationResponse> call, Response<SingleDonationResponse> response) {
                    btnSaveDonation.setEnabled(true);
                    btnSaveDonation.setText("💾 देणगी साठवा");
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddDonationActivity.this, "देणगी MongoDB डेटाबेसमध्ये साठवली गेली!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddDonationActivity.this, "साठवता आली नाही", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SingleDonationResponse> call, Throwable t) {
                    btnSaveDonation.setEnabled(true);
                    btnSaveDonation.setText("💾 देणगी साठवा");
                    Toast.makeText(AddDonationActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
