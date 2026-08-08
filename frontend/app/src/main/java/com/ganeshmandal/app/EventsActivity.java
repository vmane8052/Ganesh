package com.ganeshmandal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.EventAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.EventListResponse;
import com.ganeshmandal.app.models.MandalEvent;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvEvents;
    private TextView tvEmpty;
    private ExtendedFloatingActionButton fabAddEvent;
    private EventAdapter adapter;
    private boolean isAdmin = false;
    private List<MandalEvent> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        btnBack = findViewById(R.id.btnBack);
        rvEvents = findViewById(R.id.rvEvents);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddEvent = findViewById(R.id.fabAddEvent);

        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        isAdmin = "ADMIN".equalsIgnoreCase(prefs.getString("USER_ROLE", "USER"));

        fabAddEvent.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter();
        adapter.setAdmin(isAdmin);
        rvEvents.setAdapter(adapter);

        adapter.setListener(new EventAdapter.OnEventListener() {
            @Override
            public void onEdit(MandalEvent event, int position) {
                Intent intent = new Intent(EventsActivity.this, AddEventActivity.class);
                intent.putExtra("EVENT_DATA", event);
                startActivity(intent);
            }

            @Override
            public void onDelete(MandalEvent event, int position) {
                new AlertDialog.Builder(EventsActivity.this)
                        .setTitle("कार्यक्रम हटवा")
                        .setMessage("तुम्हाला नक्की " + event.getDayTitle() + " चा कार्यक्रम डेटाबेसमधून हटवायचा आहे का?")
                        .setPositiveButton("हटवा", (dialog, which) -> deleteEventRemote(event, position))
                        .setNegativeButton("रद्द करा", null)
                        .show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEventActivity.class);
            startActivity(intent);
        });

        fetchEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEvents();
    }

    private void fetchEvents() {
        // 100% Strict Real-Time Cloud MongoDB Atlas Fetch
        ApiClient.getService().getEvents().enqueue(new Callback<EventListResponse>() {
            @Override
            public void onResponse(Call<EventListResponse> call, Response<EventListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MandalEvent> list = response.body().getData();
                    allEvents = list != null ? list : new ArrayList<>();
                    adapter.setEvents(allEvents);
                    tvEmpty.setVisibility(allEvents.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(EventsActivity.this, "डेटाबेसमधून कार्यक्रम लोड करू शकलो नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventListResponse> call, Throwable t) {
                Toast.makeText(EventsActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteEventRemote(MandalEvent event, int pos) {
        if (event.getId() != null && !event.getId().isEmpty()) {
            ApiClient.getService().deleteEvent(event.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    allEvents.remove(event);
                    adapter.setEvents(allEvents);
                    tvEmpty.setVisibility(allEvents.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(EventsActivity.this, "कार्यक्रम डेटाबेसमधून हटवला गेला!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(EventsActivity.this, "डेटाबेस डिलीट एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
