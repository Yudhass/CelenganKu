package com.yudhas.celenganku;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yudhas.celenganku.adapter.TransaksiAdapter;
import com.yudhas.celenganku.database.AppDatabase;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.database.entity.Transaksi;
import com.yudhas.celenganku.databinding.ActivityDetailTabunganBinding;
import com.yudhas.celenganku.notification.NotificationScheduler;
import com.yudhas.celenganku.util.CurrencyHelper;
import com.yudhas.celenganku.util.DateHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailTabunganActivity extends AppCompatActivity {

    public static final String EXTRA_TABUNGAN_ID = "extra_tabungan_id";

    private ActivityDetailTabunganBinding binding;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long tabunganId;
    private Tabungan currentTabungan;
    private TransaksiAdapter transaksiAdapter;
    private String currentFilter = "semua";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityDetailTabunganBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBars.bottom);
            return windowInsets;
        });

        db = AppDatabase.getInstance(this);
        tabunganId = getIntent().getLongExtra(EXTRA_TABUNGAN_ID, -1);
        if (tabunganId == -1) { finish(); return; }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupRecyclerView();
        setupButtons();
        setupFilterChips();
        setupHapusSemuaButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_tabungan, menu);
        // Tint icons white
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                item.getIcon().setTint(Color.WHITE);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, TambahTabunganActivity.class);
            intent.putExtra(TambahTabunganActivity.EXTRA_TABUNGAN_ID, tabunganId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void setupRecyclerView() {
        transaksiAdapter = new TransaksiAdapter();
        binding.recyclerTransaksi.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTransaksi.setAdapter(transaksiAdapter);

        transaksiAdapter.setOnTransaksiActionListener(new TransaksiAdapter.OnTransaksiActionListener() {
            @Override
            public void onEdit(Transaksi transaksi) {
                openEditTransaksi(transaksi);
            }
            @Override
            public void onDelete(Transaksi transaksi) {
                showDeleteTransaksiDialog(transaksi);
            }
        });
    }

    private void setupButtons() {
        binding.btnTambah.setOnClickListener(v -> {
            Intent intent = new Intent(this, TambahTransaksiActivity.class);
            intent.putExtra(TambahTransaksiActivity.EXTRA_TABUNGAN_ID, tabunganId);
            intent.putExtra(TambahTransaksiActivity.EXTRA_TIPE, "masuk");
            if (currentTabungan != null)
                intent.putExtra(TambahTransaksiActivity.EXTRA_NAMA, currentTabungan.getNama());
            startActivity(intent);
        });
        binding.btnKurangi.setOnClickListener(v -> {
            Intent intent = new Intent(this, TambahTransaksiActivity.class);
            intent.putExtra(TambahTransaksiActivity.EXTRA_TABUNGAN_ID, tabunganId);
            intent.putExtra(TambahTransaksiActivity.EXTRA_TIPE, "keluar");
            if (currentTabungan != null)
                intent.putExtra(TambahTransaksiActivity.EXTRA_NAMA, currentTabungan.getNama());
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        // chipFilterSemua is already android:checked="true" in XML
        binding.chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipFilterSemua) currentFilter = "semua";
            else if (checkedId == R.id.chipFilterMasuk) currentFilter = "masuk";
            else if (checkedId == R.id.chipFilterKeluar) currentFilter = "keluar";
            else return;
            loadTransaksi();
        });
    }

    /** Enable / disable a button-like view with proper alpha feedback. */
    private void setButtonEnabled(android.view.View btn, boolean enabled) {
        btn.setEnabled(enabled);
        btn.setClickable(enabled);
        btn.setAlpha(enabled ? 1.0f : 0.45f);
    }

    private void setupHapusSemuaButton() {
        binding.btnHapusSemua.setOnClickListener(v -> showHapusSemuaDialog());
    }

    // ── Open edit for existing transaction ──────────────────────────────────
    private void openEditTransaksi(Transaksi transaksi) {
        Intent intent = new Intent(this, TambahTransaksiActivity.class);
        intent.putExtra(TambahTransaksiActivity.EXTRA_TABUNGAN_ID, tabunganId);
        intent.putExtra(TambahTransaksiActivity.EXTRA_TIPE, transaksi.getTipe());
        intent.putExtra(TambahTransaksiActivity.EXTRA_TRANSAKSI_ID, transaksi.getId());
        if (currentTabungan != null)
            intent.putExtra(TambahTransaksiActivity.EXTRA_NAMA, currentTabungan.getNama());
        startActivity(intent);
    }

    // ── Confirm delete single transaction ───────────────────────────────────
    private void showDeleteTransaksiDialog(Transaksi transaksi) {
        boolean isMasuk = "masuk".equals(transaksi.getTipe());
        String tipeLabel = isMasuk ? "tambah saldo" : "kurangi saldo";
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Hapus transaksi " + tipeLabel + " sebesar "
                        + CurrencyHelper.formatRupiah(transaksi.getNominal()) + "?")
                .setPositiveButton("Ya, Hapus", (d, w) -> deleteTransaksi(transaksi))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteTransaksi(Transaksi transaksi) {
        executor.execute(() -> {
            // Reverse the effect on tabungan balance
            if ("masuk".equals(transaksi.getTipe())) {
                db.tabunganDao().subtractNominal(tabunganId, transaksi.getNominal());
            } else {
                db.tabunganDao().addNominal(tabunganId, transaksi.getNominal());
            }
            db.transaksiDao().delete(transaksi);
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, "🗑️ Transaksi dihapus", android.widget.Toast.LENGTH_SHORT).show();
                loadData();
            });
        });
    }

    // ── Confirm delete ALL transactions ─────────────────────────────────────
    private void showHapusSemuaDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Semua Riwayat")
                .setMessage("Semua riwayat transaksi akan dihapus dan saldo tabungan akan direset ke 0.\n\nLanjutkan?")
                .setPositiveButton("Ya, Hapus Semua", (d, w) -> hapusSemuaTransaksi())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void hapusSemuaTransaksi() {
        executor.execute(() -> {
            db.transaksiDao().deleteByTabunganId(tabunganId);
            // Reset current nominal to 0
            db.tabunganDao().resetNominal(tabunganId);
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, "🗑️ Semua riwayat dihapus", android.widget.Toast.LENGTH_SHORT).show();
                loadData();
            });
        });
    }

    // ── Load ─────────────────────────────────────────────────────────────────
    private void loadData() {
        executor.execute(() -> {
            currentTabungan = db.tabunganDao().getById(tabunganId);
            if (currentTabungan == null) { runOnUiThread(this::finish); return; }
            runOnUiThread(() -> { bindTabunganData(currentTabungan); loadTransaksi(); });
        });
    }

    private void bindTabunganData(Tabungan t) {
        int progress = t.getProgressPercent();
        binding.toolbar.setTitle(t.getNama());
        binding.tvNama.setText(t.getNama());
        binding.tvProgressPersen.setText(progress + "%");
        binding.progressBarLarge.setProgress(progress);
        binding.tvTerkumpul.setText(CurrencyHelper.formatRupiah(t.getCurrentNominal()));
        binding.tvTarget.setText(CurrencyHelper.formatRupiah(t.getTargetNominal()));

        // Sisa
        if (t.isCompleted()) {
            binding.tvSisa.setText("✅ Lunas!");
            setButtonEnabled(binding.btnTambah, false);
            setButtonEnabled(binding.btnKurangi, false);
        } else {
            binding.tvSisa.setText(CurrencyHelper.formatRupiah(t.getSisaNominal()));
            setButtonEnabled(binding.btnTambah, true);
            setButtonEnabled(binding.btnKurangi, t.getCurrentNominal() > 0);
        }

        // Dibuat pada
        binding.tvDibuat.setText(DateHelper.formatDate(t.getCreatedAt()));

        // Target Selesai (Deadline)
        if (t.getDeadline() != null && !t.getDeadline().isEmpty()) {
            binding.layoutDeadline.setVisibility(View.VISIBLE);
            long daysLeft = DateHelper.getDaysRemaining(t.getDeadline());
            if (daysLeft < 0) {
                binding.tvDeadline.setText(t.getDeadline() + "  ⚠️ Terlewat");
                binding.tvDeadline.setTextColor(Color.parseColor("#C62828"));
            } else if (daysLeft == 0) {
                binding.tvDeadline.setText(t.getDeadline() + "  ⚠️ Hari ini!");
                binding.tvDeadline.setTextColor(Color.parseColor("#C62828"));
            } else {
                String sisaWaktu = DateHelper.formatSisaWaktu(daysLeft);
                binding.tvDeadline.setText(t.getDeadline() + "  (" + sisaWaktu + ")");
                binding.tvDeadline.setTextColor(daysLeft <= 7
                        ? Color.parseColor("#F57C00") : Color.parseColor("#212121"));
            }
        } else {
            binding.layoutDeadline.setVisibility(View.GONE);
        }

        // Deskripsi
        if (t.getDeskripsi() != null && !t.getDeskripsi().isEmpty()) {
            binding.layoutDeskripsi.setVisibility(View.VISIBLE);
            binding.tvDeskripsi.setText(t.getDeskripsi());
        } else {
            binding.layoutDeskripsi.setVisibility(View.GONE);
        }

        // Notifikasi info
        String notifText;
        String type = t.getNotifType() != null ? t.getNotifType() : "none";
        switch (type) {
            case "permenit":
                notifText = "Setiap " + t.getNotifIntervalMenit() + " menit ⏱️";
                break;
            case "perjam":
                notifText = "Setiap " + t.getNotifIntervalJam() + " jam ⏰";
                break;
            case "harian":
                notifText = "Harian 📅 jam " + t.getNotifTime();
                break;
            case "mingguan":
                notifText = "Setiap " + t.getNotifHariMinggu() + " 📆 jam " + t.getNotifTime();
                break;
            case "bulanan":
                notifText = "Setiap tgl " + t.getNotifTanggalBulanan() + " 🗓️ jam " + t.getNotifTime();
                break;
            default:
                notifText = "Tidak ada";
                break;
        }
        binding.tvNotifInfo.setText(notifText);
    }

    private void loadTransaksi() {
        executor.execute(() -> {
            List<Transaksi> list;
            switch (currentFilter) {
                case "masuk": list = db.transaksiDao().getByTabunganIdAndTipe(tabunganId, "masuk"); break;
                case "keluar": list = db.transaksiDao().getByTabunganIdAndTipe(tabunganId, "keluar"); break;
                default: list = db.transaksiDao().getByTabunganId(tabunganId); break;
            }
            final List<Transaksi> finalList = list;
            runOnUiThread(() -> {
                transaksiAdapter.setData(finalList);
                boolean empty = finalList.isEmpty();
                binding.tvEmptyTransaksi.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.recyclerTransaksi.setVisibility(empty ? View.GONE : View.VISIBLE);
                // Show/hide hapus semua button
                binding.btnHapusSemua.setVisibility(finalList.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Tabungan")
                .setMessage("Hapus tabungan ini? Semua riwayat transaksi akan ikut terhapus.")
                .setPositiveButton("Ya, Hapus", (d, w) -> deleteTabungan())
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void deleteTabungan() {
        executor.execute(() -> {
            if (currentTabungan != null) {
                NotificationScheduler.cancelSchedule(this, tabunganId);
                db.tabunganDao().delete(currentTabungan);
            }
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, "Berhasil dihapus!", android.widget.Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
