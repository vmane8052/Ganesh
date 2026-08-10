package com.ganeshmandal.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.R;
import com.ganeshmandal.app.models.User;
import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final Context context;
    private List<User> memberList;
    private List<User> fullList;

    public MemberAdapter(Context context, List<User> memberList) {
        this.context = context;
        this.memberList = memberList != null ? memberList : new ArrayList<>();
        this.fullList = new ArrayList<>(this.memberList);
    }

    public void updateData(List<User> newList) {
        this.memberList = newList != null ? newList : new ArrayList<>();
        this.fullList = new ArrayList<>(this.memberList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            memberList = new ArrayList<>(fullList);
        } else {
            String lower = query.trim().toLowerCase();
            List<User> filtered = new ArrayList<>();
            for (User u : fullList) {
                if ((u.getName() != null && u.getName().toLowerCase().contains(lower)) ||
                    (u.getPhone() != null && u.getPhone().contains(lower)) ||
                    (u.getRoleInMandal() != null && u.getRoleInMandal().toLowerCase().contains(lower))) {
                    filtered.add(u);
                }
            }
            memberList = filtered;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User user = memberList.get(position);

        holder.tvMemberName.setText(user.getName() != null ? user.getName() : "सदस्य");
        holder.tvMemberRoleInMandal.setText(user.getRoleInMandal());
        holder.tvMemberPhone.setText("📱 " + (user.getPhone() != null ? user.getPhone() : "-"));

        // Highlight Admin role vs Normal User
        if (user.isAdmin()) {
            holder.tvMemberRoleInMandal.setTextColor(context.getResources().getColor(R.color.primary_blue));
            holder.tvMemberRoleInMandal.setBackgroundColor(0xFFE3F2FD);
        } else {
            holder.tvMemberRoleInMandal.setTextColor(context.getResources().getColor(R.color.jama_green));
            holder.tvMemberRoleInMandal.setBackgroundColor(0xFFE8F5E9);
        }

        // Profile Photo loading (Cloudinary URL or Base64)
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            if (user.getPhotoUrl().startsWith("http://") || user.getPhotoUrl().startsWith("https://")) {
                com.bumptech.glide.Glide.with(context)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.app_logo)
                        .error(R.drawable.app_logo)
                        .into(holder.ivMemberPhoto);
            } else {
                try {
                    byte[] decodedBytes = Base64.decode(user.getPhotoUrl(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        holder.ivMemberPhoto.setImageBitmap(bitmap);
                    } else {
                        holder.ivMemberPhoto.setImageResource(R.drawable.app_logo);
                    }
                } catch (Exception e) {
                    holder.ivMemberPhoto.setImageResource(R.drawable.app_logo);
                }
            }
        } else {
            holder.ivMemberPhoto.setImageResource(R.drawable.app_logo);
        }

        // Call member dialer
        holder.btnCall.setOnClickListener(v -> {
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + user.getPhone()));
                context.startActivity(intent);
            }
        });

        // Open MemberDetailActivity on clicking item card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.ganeshmandal.app.MemberDetailActivity.class);
            intent.putExtra("MEMBER_NAME", user.getName());
            intent.putExtra("MEMBER_PHONE", user.getPhone());
            intent.putExtra("MEMBER_PIN", user.getPin());
            intent.putExtra("MEMBER_ROLE", user.getRole());
            intent.putExtra("MEMBER_ROLE_IN_MANDAL", user.getRoleInMandal());
            intent.putExtra("MEMBER_PHOTO_URL", user.getPhotoUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return memberList != null ? memberList.size() : 0;
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMemberPhoto, btnCall;
        TextView tvMemberName, tvMemberRoleInMandal, tvMemberPhone;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMemberPhoto = itemView.findViewById(R.id.ivMemberPhoto);
            btnCall = itemView.findViewById(R.id.btnCall);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberRoleInMandal = itemView.findViewById(R.id.tvMemberRoleInMandal);
            tvMemberPhone = itemView.findViewById(R.id.tvMemberPhone);
        }
    }
}
