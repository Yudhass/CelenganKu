package com.yudhas.celenganku;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yudhas.celenganku.database.AppDatabase;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.database.entity.Transaksi;
import com.yudhas.celenganku.databinding.ActivityTambahTransaksiBinding;
import com.yudhas.celenganku.notification.NotificationHelper;
import com.yudhas.celenganku.util.CurrencyHelper;
import com.yudhas.celenganku.util.CurrencyTextWatcher;
import com.yudhas.celenganku.util.DateHelper;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TambahTransaksiActivity extends AppCompatActivity {

    public static final String EXTRA_TABUNGAN_ID   = "extra_tabungan_id";
    public static final String EXTRA_TIPE          = "extra_tipe";       // "masuk" or "keluar"
    public static final String EXTRA_NAMA          = "extra_nama";
    public static final String EXTRA_TRANSAKSI_ID  = "extra_transaksi_id"; // for edit mode

    private ActivityTambahTransaksiBinding binding;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private long tabunganId;
    private String tipe;
    private String namaTabungan;
    private long transaksiId = -1;         // -1 means add mode
    private Transaksi existingTransaksi;   // original data when editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityTambahTransaksiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBars.bottom);
            return windowInsets;
        });

        db = AppDatabase.getInstance(this);
        tabunganId    = getIntent().getLongExtra(EXTRA_TABUNGAN_ID, -1);
        tipe          = getIntent().getStringExtra(EXTRA_TIPE);
        namaTabungan  = getIntent().getStringExtra(EXTRA_NAMA);
        transaksiId   = getIntent().getLongExtra(EXTRA_TRANSAKSI_ID, -1);

        if (tabunganId == -1 || tipe == null) { finish(); return; }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupUI();
        setupDatePicker();
        binding.etNominal.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        binding.etNominal.addTextChangedListener(new CurrencyTextWatcher(binding.etNominal));

        if (transaksiId != -1) {
            // Edit mode: load existing data
            loadExistingTransaksi();
        } else {
            // Add mode: set today as default
            binding.etTanggal.setText(DateHelper.formatDateInput(System.currentTimeMillis()));
        }

        binding.btnSimpan.setOnClickListener(v -> saveTransaksi());
    }

    private void loadExistingTransaksi() {
        executor.execute(() -> {
            existingTransaksi = db.transaksiDao().getById(transaksiId);
            if (existingTransaksi == null) { runOnUiThread(this::finish); return; }
            runOnUiThread(() -> {
                // Pre-fill form
                CurrencyTextWatcher.setValue(binding.etNominal, existingTransaksi.getNominal());
                String desc = existingTransaksi.getDeskripsi();
                binding.etDeskripsi.setText(desc != null ? desc : "");
                binding.etTanggal.setText(DateHelper.formatDateInput(existingTransaksi.getTanggal()));
            });
        });
    }

    private void setupUI() {
        boolean isMasuk = "masuk".equals(tipe);
        boolean isEdit  = transaksiId != -1;

        if (isMasuk) {
            binding.toolbar.setTitle(isEdit ? "Ubah Tambah Saldo" : "Tambah Saldo");
            binding.tvTipeIcon.setText("➕");
            binding.tvTipeLabel.setText(isEdit ? "Edit: Tambah Saldo" : "Tambah Saldo");
            binding.tvTipeLabel.setTextColor(Color.parseColor("#1B5E20"));
            binding.cardInfo.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            binding.btnSimpan.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#00897B")));
        } else {
            binding.toolbar.setTitle(isEdit ? "Ubah Kurangi Saldo" : "Kurangi Saldo");
            binding.tvTipeIcon.setText("➖");
            binding.tvTipeLabel.setText(isEdit ? "Edit: Kurangi Saldo" : "Kurangi Saldo");
            binding.tvTipeLabel.setTextColor(Color.parseColor("#B71C1C"));
            binding.cardInfo.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            binding.btnSimpan.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F57C00")));
        }

        binding.btnSimpan.setText(isEdit ? "✏️ Perbarui Transaksi" : "💾 Simpan Transaksi");

        if (namaTabungan != null) {
            binding.tvNamaTabungan.setText("Target: " + namaTabungan);
        }
    }

    private void setupDatePicker() {
        binding.etTanggal.setOnClickListener(v -> showDatePicker());
        binding.tilTanggal.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
            binding.etTanggal.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaksi() {
        long nominal = CurrencyTextWatcher.getValue(binding.etNominal);
        if (nominal <= 0) {
            binding.tilNominal.setError("Nominal harus lebih dari 0");
            return;
        }
        binding.tilNominal.setError(null);

        String deskripsi  = binding.etDeskripsi.getText() != null
                ? binding.etDeskripsi.getText().toString().trim() : "";
        String tanggalStr = binding.etTanggal.getText() != null
                ? binding.etTanggal.getText().toString().trim() : "";

        if (tanggalStr.isEmpty()) {
            binding.tilTanggal.setError("Pilih tanggal transaksi");
            return;
        }
        binding.tilTanggal.setError(null);

        long tanggal = DateHelper.parseDate(tanggalStr);

        if (transaksiId != -1) {
            updateTransaksi(nominal, deskripsi, tanggal);
        } else {
            insertTransaksi(nominal, deskripsi, tanggal);
        }
    }

    private void insertTransaksi(long nominal, String deskripsi, long tanggal) {
        executor.execute(() -> {
            // Validate keluar: cannot exceed current nominal
            if ("keluar".equals(tipe)) {
                Tabungan tabungan = db.tabunganDao().getById(tabunganId);
                if (tabungan != null && nominal > tabungan.getCurrentNominal()) {
                    runOnUiThread(() -> binding.tilNominal.setError(
                            "Nominal melebihi saldo terkumpul ("
                            + CurrencyHelper.formatRupiah(tabungan.getCurrentNominal()) + ")"));
                    return;
                }
            }

            Transaksi transaksi = new Transaksi();
            transaksi.setTabunganId(tabunganId);
            transaksi.setTipe(tipe);
            transaksi.setNominal(nominal);
            transaksi.setDeskripsi(deskripsi);
            transaksi.setTanggal(tanggal);
            db.transaksiDao().insert(transaksi);

            if ("masuk".equals(tipe)) {
                db.tabunganDao().addNominal(tabunganId, nominal);
            } else {
                db.tabunganDao().subtractNominal(tabunganId, nominal);
            }

            // Check achievement
            if ("masuk".equals(tipe)) {
                Tabungan updated = db.tabunganDao().getById(tabunganId);
                if (updated != null && updated.isCompleted()) {
                    NotificationHelper.sendAchievementNotification(
                            TambahTransaksiActivity.this, tabunganId, updated.getNama());
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this,
                        "masuk".equals(tipe) ? "✅ Saldo bertambah!" : "✅ Saldo dikurangi!",
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void updateTransaksi(long newNominal, String deskripsi, long tanggal) {
        executor.execute(() -> {
            if (existingTransaksi == null) {
                existingTransaksi = db.transaksiDao().getById(transaksiId);
            }
            if (existingTransaksi == null) { runOnUiThread(this::finish); return; }

            long oldNominal = existingTransaksi.getNominal();
            String oldTipe  = existingTransaksi.getTipe();

            // Validate keluar change: new resultant balance must be >= 0
            if ("keluar".equals(oldTipe)) {
                Tabungan tabungan = db.tabunganDao().getById(tabunganId);
                if (tabungan != null) {
                    // reverse old keluar, apply new keluar
                    long projected = tabungan.getCurrentNominal() + oldNominal - newNominal;
                    if (projected < 0) {
                        runOnUiThread(() -> binding.tilNominal.setError(
                                "Nominal melebihi saldo terkumpul setelah penyesuaian ("
                                + CurrencyHelper.formatRupiah(tabungan.getCurrentNominal() + oldNominal) + ")"));
                        return;
                    }
                }
            }

            // Update entity
            existingTransaksi.setNominal(newNominal);
            existingTransaksi.setDeskripsi(deskripsi);
            existingTransaksi.setTanggal(tanggal);
            db.transaksiDao().update(existingTransaksi);

            // Adjust tabungan balance: reverse old effect, apply new effect
            long diff = newNominal - oldNominal;
            if ("masuk".equals(oldTipe)) {
                if (diff > 0) db.tabunganDao().addNominal(tabunganId, diff);
                else if (diff < 0) db.tabunganDao().subtractNominal(tabunganId, -diff);
            } else {
                if (diff > 0) db.tabunganDao().subtractNominal(tabunganId, diff);
                else if (diff < 0) db.tabunganDao().addNominal(tabunganId, -diff);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "✅ Transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
