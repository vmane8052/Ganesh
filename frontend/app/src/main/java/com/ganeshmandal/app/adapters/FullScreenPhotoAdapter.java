package com.ganeshmandal.app.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.GalleryPhoto;
import java.util.List;

public class FullScreenPhotoAdapter extends RecyclerView.Adapter<FullScreenPhotoAdapter.FullScreenViewHolder> {

    private final Context context;
    private final List<GalleryPhoto> photoList;

    public FullScreenPhotoAdapter(Context context, List<GalleryPhoto> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @NonNull
    @Override
    public FullScreenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_full_photo, parent, false);
        return new FullScreenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FullScreenViewHolder holder, int position) {
        GalleryPhoto photo = photoList.get(position);
        String img = photo.getImageUrl();

        if (img != null && !img.isEmpty()) {
            if (img.startsWith("http://") || img.startsWith("https://")) {
                Glide.with(context)
                        .load(img)
                        .fitCenter()
                        .placeholder(R.drawable.app_logo)
                        .error(R.drawable.app_logo)
                        .into(holder.ivSwipePhoto);
            } else {
                try {
                    String clean = img.contains(",") ? img.substring(img.indexOf(",") + 1) : img;
                    byte[] b = Base64.decode(clean, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                    if (bmp != null) {
                        holder.ivSwipePhoto.setImageBitmap(bmp);
                    } else {
                        holder.ivSwipePhoto.setImageResource(R.drawable.app_logo);
                    }
                } catch (Exception e) {
                    holder.ivSwipePhoto.setImageResource(R.drawable.app_logo);
                }
            }
        } else {
            holder.ivSwipePhoto.setImageResource(R.drawable.app_logo);
        }
    }

    @Override
    public int getItemCount() {
        return photoList != null ? photoList.size() : 0;
    }

    static class FullScreenViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSwipePhoto;

        public FullScreenViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSwipePhoto = itemView.findViewById(R.id.ivSwipePhoto);
        }
    }
}
