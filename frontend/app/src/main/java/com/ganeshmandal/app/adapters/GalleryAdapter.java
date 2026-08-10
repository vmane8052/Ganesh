package com.ganeshmandal.app.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.GalleryPhoto;
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
