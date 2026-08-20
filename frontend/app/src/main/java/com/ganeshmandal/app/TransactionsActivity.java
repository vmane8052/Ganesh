package com.ganeshmandal.app;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ganeshmandal.app.adapters.TransactionAdapter;
import com.ganeshmandal.app.api.ApiClient;
import com.ganeshmandal.app.models.Transaction;
import com.ganeshmandal.app.models.TransactionResponse;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    private ImageView btnBack, btnFilter;
    private TextView tvTotalJama, tvTotalKharch, tvBalance;
    private MaterialButton tabJama, tabKharch, btnJamaKara, btnKharchKara;
    private RecyclerView rvTransactions;
    private LinearLayout layoutAdminActions;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private String currentFilter = null; // null = ALL, "JAMA" or "KHARCH"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter);
        tvTotalJama = findViewById(R.id.tvTotalJama);
        tvTotalKharch = findViewById(R.id.tvTotalKharch);
        tvBalance = findViewById(R.id.tvBalance);
        tabJama = findViewById(R.id.tabJama);
        tabKharch = findViewById(R.id.tabKharch);
        rvTransactions = findViewById(R.id.rvTransactions);
        layoutAdminActions = findViewById(R.id.layoutAdminActions);
        btnJamaKara = findViewById(R.id.btnJamaKara);
        btnKharchKara = findViewById(R.id.btnKharchKara);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter();
        rvTransactions.setAdapter(adapter);

        // Check if user is Admin
        SharedPreferences prefs = getSharedPreferences("MandalPrefs", MODE_PRIVATE);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(prefs.getString("USER_ROLE", "USER"));

        layoutAdminActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        adapter.setAdmin(isAdmin);

        adapter.setListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onDeleteClick(Transaction tx, int position) {
                new AlertDialog.Builder(TransactionsActivity.this)
                        .setTitle("व्यवहार हटवा")
                        .setMessage("तुम्हाला नक्की हा व्यवहार डेटाबेसमधून हटवायचा आहे का?")
                        .setPositiveButton("हटवा", (dialog, which) -> {
                            deleteTransactionRemote(tx, position);
                        })
                        .setNegativeButton("रद्द करा", null)
                        .show();
            }

            @Override
            public void onReceiptClick(Transaction tx) {
                showReceiptDialog(tx);
            }
        });

        btnBack.setOnClickListener(v -> finish());

        tabJama.setOnClickListener(v -> filterTransactions("JAMA"));
        tabKharch.setOnClickListener(v -> filterTransactions("KHARCH"));

        btnJamaKara.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("TYPE", "JAMA");
            startActivity(intent);
        });

        btnKharchKara.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("TYPE", "KHARCH");
            startActivity(intent);
        });

        fetchTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
    }

    private void fetchTransactions() {
        // 100% Strict Real-Time Fetch directly from MongoDB Atlas Cloud API
        ApiClient.getService().getTransactions(null).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Transaction> serverList = response.body().getData();
                    allTransactions = serverList != null ? serverList : new ArrayList<>();
                    applyFilter();
                } else {
                    Toast.makeText(TransactionsActivity.this, "डेटाबेसमधून व्यवहार लोड करू शकलो नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                Toast.makeText(TransactionsActivity.this, "डेटाबेस नेटवर्क एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTransactions(String type) {
        if (currentFilter != null && currentFilter.equals(type)) {
            currentFilter = null; // reset filter
        } else {
            currentFilter = type;
        }
        applyFilter();
    }

    private void applyFilter() {
        updateSummary();
        if (currentFilter == null) {
            adapter.setTransactions(allTransactions);
        } else {
            List<Transaction> filtered = new ArrayList<>();
            for (Transaction tx : allTransactions) {
                if (tx.getType().equalsIgnoreCase(currentFilter)) {
                    filtered.add(tx);
                }
            }
            adapter.setTransactions(filtered);
        }
    }

    private void updateSummary() {
        double totalJama = 0;
        double totalKharch = 0;
        for (Transaction tx : allTransactions) {
            if ("JAMA".equalsIgnoreCase(tx.getType())) {
                totalJama += tx.getAmount();
            } else if ("KHARCH".equalsIgnoreCase(tx.getType())) {
                totalKharch += tx.getAmount();
            }
        }
        double balance = totalJama - totalKharch;
        tvTotalJama.setText(String.format(Locale.getDefault(), "₹ %.0f", totalJama));
        tvTotalKharch.setText(String.format(Locale.getDefault(), "₹ %.0f", totalKharch));
        tvBalance.setText(String.format(Locale.getDefault(), "₹ %.0f", balance));
    }

    private void deleteTransactionRemote(Transaction tx, int pos) {
        if (tx.getId() != null && !tx.getId().isEmpty()) {
            ApiClient.getService().deleteTransaction(tx.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    allTransactions.remove(tx);
                    applyFilter();
                    Toast.makeText(TransactionsActivity.this, "व्यवहार डेटाबेसमधून हटवला!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(TransactionsActivity.this, "डेटाबेस डिलीट एरर: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showReceiptDialog(Transaction tx) {
        if (tx == null) return;

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_receipt);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout layoutReceiptCard = dialog.findViewById(R.id.layoutReceiptCard);
        TextView tvReceiptNo = dialog.findViewById(R.id.tvReceiptNo);
        TextView tvReceiptDate = dialog.findViewById(R.id.tvReceiptDate);
        TextView tvReceiptName = dialog.findViewById(R.id.tvReceiptName);
        TextView tvReceiptPhone = dialog.findViewById(R.id.tvReceiptPhone);
        TextView tvReceiptDetails = dialog.findViewById(R.id.tvReceiptDetails);
        TextView tvReceiptAmount = dialog.findViewById(R.id.tvReceiptAmount);
        MaterialButton btnDownload = dialog.findViewById(R.id.btnDownloadReceipt);
        MaterialButton btnShare = dialog.findViewById(R.id.btnShareReceipt);

        String receiptNo = tx.getReceiptNo() != null && !tx.getReceiptNo().isEmpty()
                ? tx.getReceiptNo() : ("REC-2026-" + Math.abs(tx.hashCode() % 9000 + 1000));
        String name = tx.getMemberName() != null && !tx.getMemberName().trim().isEmpty() ? tx.getMemberName() : "सदस्य / देणगीदार";
        String phone = tx.getMemberPhone() != null && !tx.getMemberPhone().trim().isEmpty() ? tx.getMemberPhone() : "-";
        String details = tx.getDetails() != null ? tx.getDetails() : (tx.isJama() ? "वर्गणी / देणगी" : "खर्च");
        String date = tx.getDate() != null ? tx.getDate() : "-";
        String amountStr = String.format(Locale.getDefault(), "₹ %.0f/-", tx.getAmount());

        tvReceiptNo.setText("पावती क्र: " + receiptNo);
        tvReceiptDate.setText("तारीख: " + date);
        tvReceiptName.setText(name);
        tvReceiptPhone.setText(phone);
        tvReceiptDetails.setText(details);
        tvReceiptAmount.setText(amountStr);

        btnDownload.setOnClickListener(v -> downloadReceiptBitmap(layoutReceiptCard, receiptNo));
        btnShare.setOnClickListener(v -> shareReceiptBitmap(layoutReceiptCard, tx));

        dialog.show();
    }

    private Bitmap captureViewBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth() > 0 ? view.getWidth() : 800,
                view.getHeight() > 0 ? view.getHeight() : 1000, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void downloadReceiptBitmap(View view, String receiptNo) {
        Toast.makeText(this, "पावती डाऊनलोड होत आहे...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Bitmap bitmap = captureViewBitmap(view);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "Ganesh_Receipt_" + receiptNo + "_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GaneshMandal");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                }

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
                    if (os != null) os.close();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        getContentResolver().update(uri, values, null, null);
                    }

                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(TransactionsActivity.this, "पावती गॅलरीमध्ये सेव्ह झाली! 📥", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(TransactionsActivity.this, "पावती डाऊनलोड त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void shareReceiptBitmap(View view, Transaction tx) {
        Toast.makeText(this, "पावती तयार होत आहे...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Bitmap bitmap = captureViewBitmap(view);
                File cachePath = new File(getCacheDir(), "images");
                if (!cachePath.exists()) cachePath.mkdirs();
                File imageFile = new File(cachePath, "receipt_share.jpg");
                FileOutputStream stream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                stream.close();

                Uri contentUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        imageFile
                );

                String shareText = "🚩 *श्री गणेश मित्र मंडळ - गणेशोत्सव जमा पावती* 🌺\n\n" +
                        "👤 नाव: " + (tx.getMemberName() != null ? tx.getMemberName() : "सदस्य") + "\n" +
                        "📱 मोबाईल: " + (tx.getMemberPhone() != null ? tx.getMemberPhone() : "-") + "\n" +
                        "💰 जमा रक्कम: ₹ " + String.format(Locale.getDefault(), "%.0f", tx.getAmount()) + "/-\n" +
                        "📝 तपशील: " + (tx.getDetails() != null ? tx.getDetails() : "वर्गणी / देणगी") + "\n" +
                        "📅 तारीख: " + (tx.getDate() != null ? tx.getDate() : "") + "\n\n" +
                        "गणपती बाप्पा मोरया! मंगलमूर्ती मोरया! 🙏";

                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpeg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // If phone number is available, user can select WhatsApp directly
                    startActivity(Intent.createChooser(shareIntent, "व्हाट्सअ‍ॅपवर पावती शेअर करा"));
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(TransactionsActivity.this, "पावती शेअर त्रुटी: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
