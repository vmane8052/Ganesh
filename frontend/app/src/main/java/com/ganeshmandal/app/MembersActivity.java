package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.MemberAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.User;
import com.ganeshmandal.app.models.UserListResponse;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Map<String, User> userMap = new HashMap<>();

        // 1. Add Default Admin
        User admin = new User("मुख्य व्यवस्थापक (Admin)", "9999999999", "1234", "ADMIN", "मुख्य व्यवस्थापक", "");
        userMap.put("9999999999", admin);

        // 2. Add Default User
        User defaultUser = new User("गणेश विठ्ठल माने", "8888888888", "1234", "USER", "उपाध्यक्ष", "");
        userMap.put("8888888888", defaultUser);

        // 3. Load locally saved members added by Admin from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        String localJson = prefs.getString("REGISTERED_USERS", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<User>>() {}.getType();
        List<User> localUsers = gson.fromJson(localJson, type);
        if (localUsers != null) {
            for (User u : localUsers) {
                if (u.getPhone() != null && !u.getPhone().isEmpty()) {
                    userMap.put(u.getPhone(), u);
                }
            }
        }

        allMembersList = new ArrayList<>(userMap.values());
        updateListAndCount();

        // 4. Fetch dynamic cloud database members from MongoDB Atlas via API
        ApiClient.getService().getUsers().enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<User> remoteUsers = response.body().getData();
                    for (User u : remoteUsers) {
                        if (u.getPhone() != null && !u.getPhone().isEmpty()) {
                            userMap.put(u.getPhone(), u);
                        }
                    }
                    allMembersList = new ArrayList<>(userMap.values());
                    updateListAndCount();
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                // Silently retain current list
            }
        });
    }

    private void updateListAndCount() {
        adapter.updateData(allMembersList);
        tvTotalMembersCount.setText("एकूण: " + allMembersList.size());
    }
}
