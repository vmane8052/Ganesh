package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.MemberAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.User;
import com.ganeshmandal.app.models.UserListResponse;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MembersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalMembersCount;
    private TextInputEditText etSearchMember;
    private RecyclerView rvMembers;
    private ExtendedFloatingActionButton btnAddMemberFab;

    private MemberAdapter adapter;
    private List<User> allMembersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        btnBack = findViewById(R.id.btnBack);
        tvTotalMembersCount = findViewById(R.id.tvTotalMembersCount);
        etSearchMember = findViewById(R.id.etSearchMember);
        rvMembers = findViewById(R.id.rvMembers);
        btnAddMemberFab = findViewById(R.id.btnAddMemberFab);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "USER");
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (isAdmin) {
            btnAddMemberFab.setVisibility(View.VISIBLE);
            btnAddMemberFab.setOnClickListener(v -> {
                Intent intent = new Intent(MembersActivity.this, AddMemberActivity.class);
                startActivity(intent);
            });
        } else {
            btnAddMemberFab.setVisibility(View.GONE);
        }

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MemberAdapter(this, allMembersList);
        rvMembers.setAdapter(adapter);

        etSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    tvTotalMembersCount.setText("एकूण: " + adapter.getItemCount());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMembers();
    }

    private void loadMembers() {
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        Set<String> deletedPhones = prefs.getStringSet("DELETED_PHONES", new HashSet<>());

        // 100% Strict Real-Time Fetch directly from MongoDB Atlas Cloud API
        ApiClient.getService().getUsers().enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<User> remoteUsers = response.body().getData();
                    List<User> filteredList = new ArrayList<>();
                    for (User u : remoteUsers) {
                        if (u.getPhone() != null && !u.getPhone().isEmpty() && !deletedPhones.contains(u.getPhone())) {
                            filteredList.add(u);
                        }
                    }
                    allMembersList = filteredList;
                    updateListAndCount();
                } else {
                    Toast.makeText(MembersActivity.this, "MongoDB डेटाबेसमधून माहिती लोड करू शकलो नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                Toast.makeText(MembersActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateListAndCount() {
        adapter.updateData(allMembersList);
        tvTotalMembersCount.setText("एकूण: " + allMembersList.size());
    }
}
