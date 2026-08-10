package com.ganeshmandal.app;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.ganeshmandal.app.adapters.GalleryAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.GalleryListResponse;
import com.ganeshmandal.app.models.GalleryPhoto;
import com.ganeshmandal.app.models.SingleGalleryResponse;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    private ImageView currentDialogPreview = null;
    private String pendingPhotoBase64 = "";

    private final ActivityResultLauncher<String> galleryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedImage
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

        View.OnClickListener addPhotoClickListener = v -> showAddPhotoDialog();
        fabAddPhoto.setOnClickListener(addPhotoClickListener);
        btnAddPhotoTop.setOnClickListener(addPhotoClickListener);

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

    private void showAddPhotoDialog() {
        pendingPhotoBase64 = "";
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_photo, null);
        ImageView ivAddPhotoPreview = dialogView.findViewById(R.id.ivAddPhotoPreview);
        LinearLayout btnPickGalleryPhoto = dialogView.findViewById(R.id.btnPickGalleryPhoto);
        TextInputEditText etPhotoTitle = dialogView.findViewById(R.id.etPhotoTitle);

        currentDialogPreview = ivAddPhotoPreview;

        View.OnClickListener pickListener = v -> galleryPickerLauncher.launch("image/*");
        ivAddPhotoPreview.setOnClickListener(pickListener);
        btnPickGalleryPhoto.setOnClickListener(pickListener);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("☁️ गॅलरीत अपलोड करा", (dialog, which) -> {
                    String title = etPhotoTitle.getText() != null ? etPhotoTitle.getText().toString().trim() : "";
                    if (title.isEmpty()) {
                        title = "श्री गणेश उत्सव";
                    }

                    if (pendingPhotoBase64.isEmpty()) {
                        Toast.makeText(this, "कृपया आधी फोटो निवडा!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    uploadPhotoToCloud(title, pendingPhotoBase64);
                })
                .setNegativeButton("रद्द करा", null)
                .show();
    }

    private void handleSelectedImage(Uri uri) {
        if (uri == null) return;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (originalBitmap == null) return;

            // Scale down for optimized fast cloud upload
            int maxDimension = 900;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
            Bitmap scaled = Bitmap.createScaledBitmap(originalBitmap, Math.round(width * ratio), Math.round(height * ratio), true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();
            pendingPhotoBase64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);

            if (currentDialogPreview != null) {
                Glide.with(this).load(scaled).into(currentDialogPreview);
            }
            Toast.makeText(this, "फोटो निवडला गेला! आता 'अपलोड करा' बटण दाबा.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "फोटो निवडताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhotoToCloud(String title, String base64Image) {
        Toast.makeText(this, "फोटो Cloudinary वर अपलोड होत आहे...", Toast.LENGTH_SHORT).show();
        swipeRefresh.setRefreshing(true);

        GalleryPhoto newPhoto = new GalleryPhoto(title, base64Image, loggedInUserName);
        ApiClient.getService().addGalleryPhoto(newPhoto).enqueue(new Callback<SingleGalleryResponse>() {
            @Override
            public void onResponse(Call<SingleGalleryResponse> call, Response<SingleGalleryResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(GalleryActivity.this, "फोटो यशस्वीरीत्या गॅलरीमध्ये जोडला गेला!", Toast.LENGTH_LONG).show();
                    fetchGalleryPhotos();
                } else {
                    Toast.makeText(GalleryActivity.this, "फोटो अपलोड करण्यात अडचण आली", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SingleGalleryResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(GalleryActivity.this, "नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
