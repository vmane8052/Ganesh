package com.ganeshmandal.app;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.ganeshmandal.app.adapters.GalleryAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.GalleryListResponse;
import com.ganeshmandal.app.models.GalleryPhoto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryActivity extends AppCompatActivity {

    private ImageView btnBack, btnAddPhotoTop;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvGallery;
    private LinearLayout layoutEmpty, layoutYearChips;
    private TextView tvEmptyMessage;
    private ExtendedFloatingActionButton fabAddPhoto;

    private GalleryAdapter adapter;
    private final List<GalleryPhoto> photoList = new ArrayList<>();
    private boolean isAdmin = false;
    private String loggedInUserName = "सदस्य";

    // Year selection & filtering ("सर्व" first, then latest years descending)
    private final List<String> availableYears = Arrays.asList(
            "सर्व", "2026", "2025", "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016", "2015"
    );
    private String selectedFilterYear = "2026"; // Default filter to current year
    private String selectedUploadYear = "2026"; // Default upload year

    // Multi-Image Picker
    private final ActivityResultLauncher<String> multiPhotoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            this::handleSelectedMultipleImages
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        btnBack = findViewById(R.id.btnBack);
        btnAddPhotoTop = findViewById(R.id.btnAddPhotoTop);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvGallery = findViewById(R.id.rvGallery);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        layoutYearChips = findViewById(R.id.layoutYearChips);
        fabAddPhoto = findViewById(R.id.fabAddPhoto);

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "USER");
        loggedInUserName = prefs.getString("USER_NAME", "सदस्य");
        isAdmin = "ADMIN".equalsIgnoreCase(role);

        btnBack.setOnClickListener(v -> finish());

        rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GalleryAdapter(this, photoList, isAdmin, loggedInUserName, this::deletePhoto);
        rvGallery.setAdapter(adapter);

        setupYearFilterChips();

        swipeRefresh.setOnRefreshListener(this::fetchGalleryPhotos);

        // Upload photo action (Admin only)
        if (isAdmin) {
            fabAddPhoto.setVisibility(View.VISIBLE);
            btnAddPhotoTop.setVisibility(View.VISIBLE);
            View.OnClickListener addPhotoClickListener = v -> showSelectUploadYearDialog();
            fabAddPhoto.setOnClickListener(addPhotoClickListener);
            btnAddPhotoTop.setOnClickListener(addPhotoClickListener);
        } else {
            fabAddPhoto.setVisibility(View.GONE);
            btnAddPhotoTop.setVisibility(View.GONE);
        }

        fetchGalleryPhotos();
    }

    private void setupYearFilterChips() {
        layoutYearChips.removeAllViews();

        for (String year : availableYears) {
            TextView chip = new TextView(this);
            chip.setText(year.equals("2026") ? "2026 (चालू)" : (year.equals("सर्व") ? "सर्व (All)" : year));
            chip.setTextSize(13f);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(36, 16, 36, 16);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            chip.setLayoutParams(params);

            boolean isSelected = year.equals(selectedFilterYear);
            applyChipStyle(chip, isSelected);

            chip.setOnClickListener(v -> {
                selectedFilterYear = year;
                setupYearFilterChips();
                fetchGalleryPhotos();
            });

            layoutYearChips.addView(chip);
        }
    }

    private void applyChipStyle(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.chip_year_selected);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.chip_year_unselected);
            chip.setTextColor(getResources().getColor(R.color.text_primary));
            chip.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void showSelectUploadYearDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_upload_year);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Spinner spinner = dialog.findViewById(R.id.spinnerUploadYear);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelYear);
        MaterialButton btnProceed = dialog.findViewById(R.id.btnProceedPickPhotos);

        List<String> uploadYearsList = new ArrayList<>();
        uploadYearsList.add("2026 (चालू वर्ष)");
        for (int y = 2025; y >= 2015; y--) {
            uploadYearsList.add(String.valueOf(y));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, uploadYearsList);
        spinner.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnProceed.setOnClickListener(v -> {
            String selectedItem = spinner.getSelectedItem().toString();
            selectedUploadYear = selectedItem.startsWith("2026") ? "2026" : selectedItem;
            dialog.dismiss();
            multiPhotoPickerLauncher.launch("image/*");
        });

        dialog.show();
    }

    private void fetchGalleryPhotos() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService().getGallery(selectedFilterYear).enqueue(new Callback<GalleryListResponse>() {
            @Override
            public void onResponse(Call<GalleryListResponse> call, Response<GalleryListResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    photoList.clear();
                    if (response.body().getData() != null) {
                        photoList.addAll(response.body().getData());
                    }
                    adapter.updateData(photoList);
                    updateEmptyState();
                } else {
                    Toast.makeText(GalleryActivity.this, "फोटो लोड करण्यात अडचण आली", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<GalleryListResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(GalleryActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (photoList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvGallery.setVisibility(View.GONE);
            if (tvEmptyMessage != null) {
                if ("सर्व वर्षे".equals(selectedFilterYear)) {
                    tvEmptyMessage.setText("अजून कोणतेही फोटो अपलोड केलेले नाहीत");
                } else {
                    tvEmptyMessage.setText(selectedFilterYear + " या वर्षासाठी अजून कोणतेही फोटो उपलब्ध नाहीत");
                }
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvGallery.setVisibility(View.VISIBLE);
        }
    }

    private void handleSelectedMultipleImages(List<Uri> uriList) {
        if (uriList == null || uriList.isEmpty()) {
            return;
        }

        int count = uriList.size();
        Toast.makeText(this, count + " फोटो (" + selectedUploadYear + ") Cloudinary वर अपलोड होत आहेत...", Toast.LENGTH_LONG).show();
        swipeRefresh.setRefreshing(true);

        new Thread(() -> {
            List<GalleryPhoto> batchPhotos = new ArrayList<>();

            for (Uri uri : uriList) {
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap originalBitmap = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();

                    if (originalBitmap != null) {
                        int maxDimension = 900;
                        int width = originalBitmap.getWidth();
                        int height = originalBitmap.getHeight();
                        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
                        Bitmap scaled = Bitmap.createScaledBitmap(originalBitmap, Math.round(width * ratio), Math.round(height * ratio), true);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] bytes = baos.toByteArray();
                        String base64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);

                        batchPhotos.add(new GalleryPhoto("", base64, loggedInUserName, selectedUploadYear));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(() -> {
                if (batchPhotos.isEmpty()) {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(GalleryActivity.this, "कोणतेही फोटो लोड करता आले नाहीत", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("year", selectedUploadYear);
                payload.put("photos", batchPhotos);

                ApiClient.getService().addGalleryBatch(payload).enqueue(new Callback<GalleryListResponse>() {
                    @Override
                    public void onResponse(Call<GalleryListResponse> call, Response<GalleryListResponse> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(GalleryActivity.this, batchPhotos.size() + " फोटो (" + selectedUploadYear + ") यशस्वीरीत्या जोडले गेले!", Toast.LENGTH_LONG).show();

                            // Automatically switch filter to the uploaded year
                            selectedFilterYear = selectedUploadYear;
                            setupYearFilterChips();
                            fetchGalleryPhotos();
                        } else {
                            Toast.makeText(GalleryActivity.this, "फोटो अपलोड करताना त्रुटी आली", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GalleryListResponse> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(GalleryActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }).start();
    }

    private void deletePhoto(GalleryPhoto photo) {
        if (photo == null || photo.getId() == null) return;

        swipeRefresh.setRefreshing(true);
        ApiClient.getService().deleteGalleryPhoto(photo.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(GalleryActivity.this, "फोटो गॅलरीमधून हटवला!", Toast.LENGTH_SHORT).show();
                fetchGalleryPhotos();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(GalleryActivity.this, "फोटो हटवताना एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
