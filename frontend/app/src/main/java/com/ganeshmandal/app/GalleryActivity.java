package com.ganeshmandal.app;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
    private LinearLayout layoutEmpty;
    private ExtendedFloatingActionButton fabAddPhoto;

    private GalleryAdapter adapter;
    private final List<GalleryPhoto> photoList = new ArrayList<>();
    private boolean isAdmin = false;
    private String loggedInUserName = "सदस्य";

    // Multi-Image Picker: Allows selecting 1, 2, 5, 10 or more photos at once without asking for titles
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
        fabAddPhoto = findViewById(R.id.fabAddPhoto);

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "USER");
        loggedInUserName = prefs.getString("USER_NAME", "सदस्य");
        isAdmin = "ADMIN".equalsIgnoreCase(role);

        btnBack.setOnClickListener(v -> finish());

        rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GalleryAdapter(this, photoList, isAdmin, loggedInUserName, this::deletePhoto);
        rvGallery.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchGalleryPhotos);

        // Show "फोटो जोडा" option ONLY for Admin
        if (isAdmin) {
            fabAddPhoto.setVisibility(View.VISIBLE);
            btnAddPhotoTop.setVisibility(View.VISIBLE);
            View.OnClickListener addPhotoClickListener = v -> multiPhotoPickerLauncher.launch("image/*");
            fabAddPhoto.setOnClickListener(addPhotoClickListener);
            btnAddPhotoTop.setOnClickListener(addPhotoClickListener);
        } else {
            fabAddPhoto.setVisibility(View.GONE);
            btnAddPhotoTop.setVisibility(View.GONE);
        }

        fetchGalleryPhotos();
    }

    private void fetchGalleryPhotos() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService().getGallery().enqueue(new Callback<GalleryListResponse>() {
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
                    Toast.makeText(GalleryActivity.this, "गॅलरी फोटो लोड करण्यात अडचण आली", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, count + " फोटो Cloudinary वर अपलोड होत आहेत, कृपया थोडा वेळ थांबा...", Toast.LENGTH_LONG).show();
        swipeRefresh.setRefreshing(true);

        new Thread(() -> {
            List<GalleryPhoto> batchPhotos = new ArrayList<>();

            for (Uri uri : uriList) {
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap originalBitmap = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();

                    if (originalBitmap != null) {
                        // Compress to max 900px for optimal cloud upload speed & quality
                        int maxDimension = 900;
                        int width = originalBitmap.getWidth();
                        int height = originalBitmap.getHeight();
                        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
                        Bitmap scaled = Bitmap.createScaledBitmap(originalBitmap, Math.round(width * ratio), Math.round(height * ratio), true);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] bytes = baos.toByteArray();
                        String base64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);

                        batchPhotos.add(new GalleryPhoto("", base64, loggedInUserName));
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

                Map<String, List<GalleryPhoto>> payload = new HashMap<>();
                payload.put("photos", batchPhotos);

                ApiClient.getService().addGalleryBatch(payload).enqueue(new Callback<GalleryListResponse>() {
                    @Override
                    public void onResponse(Call<GalleryListResponse> call, Response<GalleryListResponse> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(GalleryActivity.this, batchPhotos.size() + " फोटो गॅलरीमध्ये यशस्वीरीत्या अपलोड झाले!", Toast.LENGTH_LONG).show();
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
