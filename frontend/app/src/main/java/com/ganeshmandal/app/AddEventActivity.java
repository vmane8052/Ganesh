package com.ganeshmandal.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.MandalEvent;
import com.ganeshmandal.app.models.SingleEventResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEventActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvFormTitle;
    private AutoCompleteTextView etDayTitle;
    private TextInputEditText etDate, etMorningAarti, etEveningAarti, etLunchHost, etModakHost, etCulturalProgram, etSpecialNotes;
    private MaterialButton btnSaveEvent;
    private final Calendar calendar = Calendar.getInstance();
    private MandalEvent editingEvent = null;

    private static final String[] GANESHOTSAV_DAYS = new String[]{
            "दिवस १ ",
            "दिवस २ ",
            "दिवस ३ ",
            "दिवस ४ ",
            "दिवस ५",
            "दिवस ६",
            "दिवस ७ ",
            "दिवस ८ ",
            "दिवस ९ ",
            "दिवस १० ",
            "दिवस ११ "
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        btnBack = findViewById(R.id.btnBack);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etDayTitle = findViewById(R.id.etDayTitle);
        etDate = findViewById(R.id.etDate);
        etMorningAarti = findViewById(R.id.etMorningAarti);
        etEveningAarti = findViewById(R.id.etEveningAarti);
        etLunchHost = findViewById(R.id.etLunchHost);
        etModakHost = findViewById(R.id.etModakHost);
        etCulturalProgram = findViewById(R.id.etCulturalProgram);
        etSpecialNotes = findViewById(R.id.etSpecialNotes);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        // Populate Day Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, GANESHOTSAV_DAYS);
        etDayTitle.setAdapter(adapter);

        // Check if editing
        if (getIntent().hasExtra("EVENT_DATA")) {
            editingEvent = (MandalEvent) getIntent().getSerializableExtra("EVENT_DATA");
            if (editingEvent != null) {
                tvFormTitle.setText("दैनिक कार्यक्रम संपादित करा");
                etDayTitle.setText(editingEvent.getDayTitle(), false);
                etDate.setText(editingEvent.getDate());
                etMorningAarti.setText(editingEvent.getMorningAarti());
                etEveningAarti.setText(editingEvent.getEveningAarti());
                etLunchHost.setText(editingEvent.getLunchHost());
                etModakHost.setText(editingEvent.getModakHost());
                etCulturalProgram.setText(editingEvent.getCulturalProgram());
                etSpecialNotes.setText(editingEvent.getSpecialNotes());
                btnSaveEvent.setText("💾 बदल साठवा");
            }
        } else {
            updateDateField();
        }

        etDate.setOnClickListener(v -> showDatePicker());
        btnBack.setOnClickListener(v -> finish());
        btnSaveEvent.setOnClickListener(v -> saveEvent());
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

    private void saveEvent() {
        String dayTitle = etDayTitle.getText() != null ? etDayTitle.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String morningAarti = etMorningAarti.getText() != null ? etMorningAarti.getText().toString().trim() : "";
        String eveningAarti = etEveningAarti.getText() != null ? etEveningAarti.getText().toString().trim() : "";
        String lunchHost = etLunchHost.getText() != null ? etLunchHost.getText().toString().trim() : "";
        String modakHost = etModakHost.getText() != null ? etModakHost.getText().toString().trim() : "";
        String cultural = etCulturalProgram.getText() != null ? etCulturalProgram.getText().toString().trim() : "";
        String notes = etSpecialNotes.getText() != null ? etSpecialNotes.getText().toString().trim() : "";

        if (dayTitle.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "कृपया दिवस आणि तारीख निवडा (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveEvent.setEnabled(false);
        btnSaveEvent.setText("डेटाबेसमध्ये साठवत आहे...");

        MandalEvent ev = new MandalEvent(dayTitle, date, morningAarti, eveningAarti, lunchHost, modakHost, cultural, notes);
        String mandalId = getSharedPreferences("MandalPrefs", MODE_PRIVATE).getString("MANDAL_ID", "M001");
        ev.setMandalId(mandalId);

        if (editingEvent != null && editingEvent.getId() != null) {
            // Update Existing Event
            ApiClient.getService().updateEvent(editingEvent.getId(), ev).enqueue(new Callback<SingleEventResponse>() {
                @Override
                public void onResponse(Call<SingleEventResponse> call, Response<SingleEventResponse> response) {
                    btnSaveEvent.setEnabled(true);
                    btnSaveEvent.setText("💾 बदल साठवा");
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEventActivity.this, "कार्यक्रम यशस्वीरीत्या अपडेट केला!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddEventActivity.this, "अपडेट करता आला नाही", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SingleEventResponse> call, Throwable t) {
                    btnSaveEvent.setEnabled(true);
                    btnSaveEvent.setText("💾 बदल साठवा");
                    Toast.makeText(AddEventActivity.this, "डेटाबेस एरर: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Add New Event
            ApiClient.getService().addEvent(ev).enqueue(new Callback<SingleEventResponse>() {
                @Override
                public void onResponse(Call<SingleEventResponse> call, Response<SingleEventResponse> response) {
                    btnSaveEvent.setEnabled(true);
                    btnSaveEvent.setText("💾 कार्यक्रम साठवा");
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEventActivity.this, "दैनिक कार्यक्रम MongoDB डेटाबेसमध्ये साठवला गेला!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddEventActivity.this, "साठवता आला नाही", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SingleEventResponse> call, Throwable t) {
                    btnSaveEvent.setEnabled(true);
                    btnSaveEvent.setText("💾 कार्यक्रम साठवा");
                    Toast.makeText(AddEventActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
