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

        holder.tvPhotoTitle.setText(photo.getTitle() != null && !photo.getTitle().isEmpty() ? photo.getTitle() : "श्री गणेश उत्सव");
        holder.tvUploadedBy.setText("👤 " + (photo.getUploadedBy() != null && !photo.getUploadedBy().isEmpty() ? photo.getUploadedBy() : "सदस्य"));

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

        // Tap to open full screen image preview
        holder.itemView.setOnClickListener(v -> showFullPhotoDialog(photo));

        // Admin or Uploader can delete photo via long press
        boolean canDelete = isAdmin || (photo.getUploadedBy() != null && photo.getUploadedBy().equals(loggedInUserName));
        if (canDelete) {
            holder.itemView.setOnLongClickListener(v -> {
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
                return true;
            });
        }
    }

    private void showFullPhotoDialog(GalleryPhoto photo) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_photo);

        ImageView ivFull = dialog.findViewById(R.id.ivFullPhoto);
        ImageView btnClose = dialog.findViewById(R.id.btnCloseFullPhoto);
        TextView tvTitle = dialog.findViewById(R.id.tvFullTitle);
        TextView tvUploaded = dialog.findViewById(R.id.tvFullUploadedBy);

        tvTitle.setText(photo.getTitle() != null ? photo.getTitle() : "श्री गणेश उत्सव");
        tvUploaded.setText("अपलोड: " + (photo.getUploadedBy() != null ? photo.getUploadedBy() : "मंडळ सदस्य"));

        String img = photo.getImageUrl();
        if (img != null && (img.startsWith("http://") || img.startsWith("https://"))) {
            Glide.with(context)
                    .load(img)
                    .fitCenter()
                    .placeholder(R.drawable.app_logo)
                    .into(ivFull);
        } else if (img != null && !img.isEmpty()) {
            try {
                String clean = img.contains(",") ? img.substring(img.indexOf(",") + 1) : img;
                byte[] b = Base64.decode(clean, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                if (bmp != null) ivFull.setImageBitmap(bmp);
            } catch (Exception ignored) {}
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return photoList != null ? photoList.size() : 0;
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGalleryPhoto, btnDeletePhoto;
        TextView tvPhotoTitle, tvUploadedBy;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGalleryPhoto = itemView.findViewById(R.id.ivGalleryPhoto);
            btnDeletePhoto = itemView.findViewById(R.id.btnDeletePhoto);
            tvPhotoTitle = itemView.findViewById(R.id.tvPhotoTitle);
            tvUploadedBy = itemView.findViewById(R.id.tvUploadedBy);
        }
    }
}
