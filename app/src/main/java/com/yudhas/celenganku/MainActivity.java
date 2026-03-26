package com.yudhas.celenganku;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yudhas.celenganku.adapter.TabunganAdapter;
import com.yudhas.celenganku.database.AppDatabase;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.databinding.ActivityMainBinding;
import com.yudhas.celenganku.notification.NotificationHelper;
import com.yudhas.celenganku.util.CurrencyHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TabunganAdapter.OnTabunganListener {

    private ActivityMainBinding binding;
    private TabunganAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Permission result handled silently
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply navigation bar bottom inset so content is never covered
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBars.bottom);
            return windowInsets;
        });

        db = AppDatabase.getInstance(this);
        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();

        setupRecyclerView();
        setupFab();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new TabunganAdapter(this);
        binding.recyclerTabungan.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTabungan.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabTambah.setOnClickListener(v -> {
            Intent intent = new Intent(this, TambahTabunganActivity.class);
            startActivity(intent);
        });
    }

    private void loadData() {
        executor.execute(() -> {
            List<Tabungan> list = db.tabunganDao().getAll();
            long totalTerkumpul = 0;
            for (Tabungan t : list) {
                totalTerkumpul += t.getCurrentNominal();
            }
            final long finalTotal = totalTerkumpul;
            final int count = list.size();

            runOnUiThread(() -> {
                adapter.setData(list);
                binding.tvTotalTabungan.setText(String.valueOf(count));
                binding.tvTotalTerkumpul.setText(CurrencyHelper.formatRupiahShort(finalTotal));

                if (list.isEmpty()) {
                    binding.recyclerTabungan.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerTabungan.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    public void onTambah(Tabungan tabungan) {
        Intent intent = new Intent(this, TambahTransaksiActivity.class);
        intent.putExtra(TambahTransaksiActivity.EXTRA_TABUNGAN_ID, tabungan.getId());
        intent.putExtra(TambahTransaksiActivity.EXTRA_TIPE, "masuk");
        intent.putExtra(TambahTransaksiActivity.EXTRA_NAMA, tabungan.getNama());
        startActivity(intent);
    }

    @Override
    public void onKurangi(Tabungan tabungan) {
        Intent intent = new Intent(this, TambahTransaksiActivity.class);
        intent.putExtra(TambahTransaksiActivity.EXTRA_TABUNGAN_ID, tabungan.getId());
        intent.putExtra(TambahTransaksiActivity.EXTRA_TIPE, "keluar");
        intent.putExtra(TambahTransaksiActivity.EXTRA_NAMA, tabungan.getNama());
        startActivity(intent);
    }

    @Override
    public void onDetail(Tabungan tabungan) {
        Intent intent = new Intent(this, DetailTabunganActivity.class);
        intent.putExtra(DetailTabunganActivity.EXTRA_TABUNGAN_ID, tabungan.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
