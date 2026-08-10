package com.ganeshmandal.app.adapters;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.GalleryPhoto;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    public interface OnPhotoDeleteListener {
        void onDelete(GalleryPhoto photo);
    }

    private final Context context;
    private List<GalleryPhoto> photoList;
    private final boolean isAdmin;
    private final String loggedInUserName;
    private final OnPhotoDeleteListener deleteListener;

    public GalleryAdapter(Context context, List<GalleryPhoto> photoList, boolean isAdmin, String loggedInUserName, OnPhotoDeleteListener deleteListener) {
        this.context = context;
        this.photoList = photoList != null ? photoList : new ArrayList<>();
        this.isAdmin = isAdmin;
        this.loggedInUserName = loggedInUserName;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<GalleryPhoto> newList) {
        this.photoList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryPhoto photo = photoList.get(position);

        String img = photo.getImageUrl();
        if (img != null && !img.isEmpty()) {
            if (img.startsWith("http://") || img.startsWith("https://")) {
                Glide.with(context)
                        .load(img)
                        .centerCrop()
                        .placeholder(R.drawable.app_logo)
                        .error(R.drawable.app_logo)
                        .into(holder.ivGalleryPhoto);
            } else {
                try {
                    String cleanBase64 = img;
                    if (cleanBase64.contains(",")) {
                        cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                    }
                    byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        holder.ivGalleryPhoto.setImageBitmap(bitmap);
                    } else {
                        holder.ivGalleryPhoto.setImageResource(R.drawable.app_logo);
                    }
                } catch (Exception e) {
                    holder.ivGalleryPhoto.setImageResource(R.drawable.app_logo);
                }
            }
        } else {
            holder.ivGalleryPhoto.setImageResource(R.drawable.app_logo);
        }

        // Tap to open full screen swipeable photo viewer (with Pinch-to-Zoom & Double-Tap Zoom)
        int clickedIndex = position;
        holder.itemView.setOnClickListener(v -> showFullPhotoDialog(clickedIndex));

        // Delete option ONLY appears when holding / long pressing the photo (Admin only)
        if (isAdmin) {
            holder.itemView.setOnLongClickListener(v -> {
                confirmDelete(photo);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    private void confirmDelete(GalleryPhoto photo) {
        new AlertDialog.Builder(context)
                .setTitle("फोटो हटवा")
                .setMessage("तुम्हाला नक्की हा फोटो गॅलरीमधून हटवायचा आहे का?")
                .setPositiveButton("हटवा", (dialog, which) -> {
                    if (deleteListener != null) {
                        deleteListener.onDelete(photo);
                    }
                })
                .setNegativeButton("रद्द करा", null)
                .show();
    }

    private void showFullPhotoDialog(int initialPosition) {
        if (photoList == null || photoList.isEmpty()) return;

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_photo);

        ViewPager2 viewPager = dialog.findViewById(R.id.viewPagerPhotos);
        TextView tvCounter = dialog.findViewById(R.id.tvPhotoCounter);
        ImageView btnClose = dialog.findViewById(R.id.btnCloseFullPhoto);
        ImageView btnFullDelete = dialog.findViewById(R.id.btnFullDeletePhoto);
        ImageView btnDownload = dialog.findViewById(R.id.btnDownloadPhoto);
        ImageView btnShare = dialog.findViewById(R.id.btnSharePhoto);

        FullScreenPhotoAdapter pagerAdapter = new FullScreenPhotoAdapter(context, photoList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(initialPosition, false);

        int total = photoList.size();
        tvCounter.setText((initialPosition + 1) + " / " + total);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvCounter.setText((position + 1) + " / " + photoList.size());
            }
        });

        // Download Photo Action
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> {
                int currentPos = viewPager.getCurrentItem();
                if (currentPos >= 0 && currentPos < photoList.size()) {
                    downloadPhoto(photoList.get(currentPos));
                }
            });
        }

        // WhatsApp / Social Share Action
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                int currentPos = viewPager.getCurrentItem();
                if (currentPos >= 0 && currentPos < photoList.size()) {
                    sharePhotoToWhatsApp(photoList.get(currentPos));
                }
            });
        }

        if (isAdmin) {
            btnFullDelete.setVisibility(View.VISIBLE);
            btnFullDelete.setOnClickListener(v -> {
                int currentPos = viewPager.getCurrentItem();
                if (currentPos >= 0 && currentPos < photoList.size()) {
                    GalleryPhoto currentPhoto = photoList.get(currentPos);
                    dialog.dismiss();
                    confirmDelete(currentPhoto);
                }
            });
        } else {
            btnFullDelete.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void downloadPhoto(GalleryPhoto photo) {
        if (photo == null || photo.getImageUrl() == null) return;
        Toast.makeText(context, "फोटो डाऊनलोड होत आहे...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Bitmap bitmap = null;
                String img = photo.getImageUrl();
                if (img.startsWith("http://") || img.startsWith("https://")) {
                    bitmap = Glide.with(context).asBitmap().load(img).submit().get();
                } else {
                    String clean = img.contains(",") ? img.substring(img.indexOf(",") + 1) : img;
                    byte[] b = Base64.decode(clean, Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                }

                if (bitmap != null) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, "Ganesh_Mandal_" + System.currentTimeMillis() + ".jpg");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GaneshMandal");
                        values.put(MediaStore.Images.Media.IS_PENDING, 1);
                    }

                    Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        OutputStream os = context.getContentResolver().openOutputStream(uri);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
                        if (os != null) os.close();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.clear();
                            values.put(MediaStore.Images.Media.IS_PENDING, 0);
                            context.getContentResolver().update(uri, values, null, null);
                        }

                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "फोटो मोबाईल गॅलरीमध्ये सेव्ह झाला! 📥", Toast.LENGTH_LONG).show());
                    }
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "फोटो सेव्ह करताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sharePhotoToWhatsApp(GalleryPhoto photo) {
        if (photo == null || photo.getImageUrl() == null) return;
        Toast.makeText(context, "शेअरिंगसाठी फोटो तयार करत आहे...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Bitmap bitmap = null;
                String img = photo.getImageUrl();
                if (img.startsWith("http://") || img.startsWith("https://")) {
                    bitmap = Glide.with(context).asBitmap().load(img).submit().get();
                } else {
                    String clean = img.contains(",") ? img.substring(img.indexOf(",") + 1) : img;
                    byte[] b = Base64.decode(clean, Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                }

                if (bitmap != null) {
                    File cachePath = new File(context.getCacheDir(), "images");
                    if (!cachePath.exists()) cachePath.mkdirs();
                    File imageFile = new File(cachePath, "ganesh_photo_share.jpg");
                    FileOutputStream stream = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                    stream.close();

                    Uri contentUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".fileprovider",
                            imageFile
                    );

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/jpeg");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "🚩 श्री गणेश मित्र मंडळ - गणेशोत्सव फोटो 🌺\n\nगणपती बाप्पा मोरया! मंगलमूर्ती मोरया! 🙏");
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(Intent.createChooser(shareIntent, "व्हाट्सअ‍ॅप किंवा इतर ॲपवर फोटो शेअर करा"));
                    });
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "फोटो शेअर करताना त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return photoList != null ? photoList.size() : 0;
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGalleryPhoto;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGalleryPhoto = itemView.findViewById(R.id.ivGalleryPhoto);
        }
    }
}
