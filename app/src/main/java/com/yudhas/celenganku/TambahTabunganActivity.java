package com.yudhas.celenganku;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudhas.celenganku.database.AppDatabase;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.databinding.ActivityTambahTabunganBinding;
import com.yudhas.celenganku.notification.NotificationScheduler;
import com.yudhas.celenganku.util.CurrencyTextWatcher;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TambahTabunganActivity extends AppCompatActivity {

    public static final String EXTRA_TABUNGAN_ID = "extra_tabungan_id";

    private ActivityTambahTabunganBinding binding;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Tabungan existingTabungan = null;
    private long tabunganId = -1;

    private final String[] notifLabels = {"Tidak Ada", "Per Menit ⏱️", "Per Jam ⏰", "Harian 📅", "Mingguan 📆", "Bulanan 🗓️"};
    private final String[] notifValues = {"none", "permenit", "perjam", "harian", "mingguan", "bulanan"};
    private final String[] hariLabels = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTambahTabunganBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        tabunganId = getIntent().getLongExtra(EXTRA_TABUNGAN_ID, -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupCurrencyInput();
        setupNotifTypeDropdown();
        setupHariDropdown();
        setupDatePicker();
        setupTimePicker();

        if (tabunganId != -1) {
            binding.toolbar.setTitle("Edit Tabungan");
            loadExistingData();
        } else {
            binding.toolbar.setTitle("Buat Tabungan Baru");
            binding.actvNotifType.setText(notifLabels[0], false);
            updateNotifFieldsVisibility("none");
        }

        binding.btnSimpan.setOnClickListener(v -> saveTabungan());
    }

    /** XML inputType="text" → setRawInputType untuk tampilkan keyboard angka tanpa DigitsKeyListener */
    private void setupCurrencyInput() {
        binding.etTarget.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        binding.etTarget.addTextChangedListener(new CurrencyTextWatcher(binding.etTarget));
    }

    private void setupNotifTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, notifLabels);
        binding.actvNotifType.setAdapter(adapter);
        binding.actvNotifType.setOnItemClickListener((parent, view, position, id) ->
                updateNotifFieldsVisibility(notifValues[position]));
    }

    private void setupHariDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, hariLabels);
        binding.actvNotifHari.setAdapter(adapter);
    }

    private void updateNotifFieldsVisibility(String notifType) {
        binding.layoutNotifPerJam.setVisibility(View.GONE);
        binding.layoutNotifPerMenit.setVisibility(View.GONE);
        binding.layoutNotifHari.setVisibility(View.GONE);
        binding.layoutNotifTanggal.setVisibility(View.GONE);
        binding.tilNotifTime.setVisibility(View.GONE);

        switch (notifType) {
            case "permenit":
                binding.layoutNotifPerMenit.setVisibility(View.VISIBLE);
                break;
            case "perjam":
                binding.layoutNotifPerJam.setVisibility(View.VISIBLE);
                break;
            case "harian":
                binding.tilNotifTime.setVisibility(View.VISIBLE);
                break;
            case "mingguan":
                binding.layoutNotifHari.setVisibility(View.VISIBLE);
                binding.tilNotifTime.setVisibility(View.VISIBLE);
                break;
            case "bulanan":
                binding.layoutNotifTanggal.setVisibility(View.VISIBLE);
                binding.tilNotifTime.setVisibility(View.VISIBLE);
                break;
            // "none" → semua hidden
        }
    }

    private void setupDatePicker() {
        binding.etDeadline.setOnClickListener(v -> showDatePicker());
        binding.tilDeadline.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
            binding.etDeadline.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupTimePicker() {
        binding.etNotifTime.setOnClickListener(v -> showTimePicker());
        binding.tilNotifTime.setEndIconOnClickListener(v -> showTimePicker());
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            binding.etNotifTime.setText(time);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void loadExistingData() {
        executor.execute(() -> {
            existingTabungan = db.tabunganDao().getById(tabunganId);
            if (existingTabungan == null) { finish(); return; }

            runOnUiThread(() -> {
                binding.etNama.setText(existingTabungan.getNama());
                CurrencyTextWatcher.setValue(binding.etTarget, existingTabungan.getTargetNominal());
                binding.etDeadline.setText(existingTabungan.getDeadline() != null ? existingTabungan.getDeadline() : "");
                binding.etDeskripsi.setText(existingTabungan.getDeskripsi() != null ? existingTabungan.getDeskripsi() : "");
                binding.etNotifTime.setText(existingTabungan.getNotifTime() != null ? existingTabungan.getNotifTime() : "19:00");

                String notifType = existingTabungan.getNotifType() != null ? existingTabungan.getNotifType() : "none";
                for (int i = 0; i < notifValues.length; i++) {
                    if (notifValues[i].equals(notifType)) {
                        binding.actvNotifType.setText(notifLabels[i], false);
                        break;
                    }
                }

                // Isi field tambahan
                if (existingTabungan.getNotifIntervalJam() > 0)
                    binding.etIntervalJam.setText(String.valueOf(existingTabungan.getNotifIntervalJam()));
                if (existingTabungan.getNotifIntervalMenit() > 0)
                    binding.etIntervalMenit.setText(String.valueOf(existingTabungan.getNotifIntervalMenit()));
                if (existingTabungan.getNotifHariMinggu() != null)
                    binding.actvNotifHari.setText(existingTabungan.getNotifHariMinggu(), false);
                if (existingTabungan.getNotifTanggalBulanan() > 0)
                    binding.etNotifTanggal.setText(String.valueOf(existingTabungan.getNotifTanggalBulanan()));

                updateNotifFieldsVisibility(notifType);
            });
        });
    }

    private void saveTabungan() {
        String nama = binding.etNama.getText() != null ? binding.etNama.getText().toString().trim() : "";
        if (nama.isEmpty()) { binding.tilNama.setError("Nama target tidak boleh kosong"); return; }
        binding.tilNama.setError(null);

        long targetNominal = CurrencyTextWatcher.getValue(binding.etTarget);
        if (targetNominal <= 0) { binding.tilTarget.setError("Nominal target harus lebih dari 0"); return; }
        binding.tilTarget.setError(null);

        String deadline = binding.etDeadline.getText() != null ? binding.etDeadline.getText().toString().trim() : "";
        String deskripsi = binding.etDeskripsi.getText() != null ? binding.etDeskripsi.getText().toString().trim() : "";

        // Notif type
        String selectedLabel = binding.actvNotifType.getText().toString();
        String notifType = "none";
        for (int i = 0; i < notifLabels.length; i++) {
            if (notifLabels[i].equals(selectedLabel)) { notifType = notifValues[i]; break; }
        }

        // Type-specific data
        String notifTime = "19:00";
        int intervalJam = 0;
        int intervalMenit = 0;
        String hariMinggu = null;
        int tanggalBulanan = 0;

        switch (notifType) {
            case "permenit":
                String menitStr = binding.etIntervalMenit.getText() != null ? binding.etIntervalMenit.getText().toString().trim() : "";
                if (menitStr.isEmpty()) { binding.tilIntervalMenit.setError("Masukkan interval menit"); return; }
                try {
                    intervalMenit = Integer.parseInt(menitStr);
                    if (intervalMenit < 1 || intervalMenit > 59) {
                        binding.tilIntervalMenit.setError("Interval 1 - 59 menit");
                        return;
                    }
                } catch (NumberFormatException e) { binding.tilIntervalMenit.setError("Angka tidak valid"); return; }
                binding.tilIntervalMenit.setError(null);
                break;
            case "perjam":
                String jamStr = binding.etIntervalJam.getText() != null ? binding.etIntervalJam.getText().toString().trim() : "";
                if (jamStr.isEmpty()) { binding.tilIntervalJam.setError("Masukkan interval jam"); return; }
                try {
                    intervalJam = Integer.parseInt(jamStr);
                    if (intervalJam < 1 || intervalJam > 24) {
                        binding.tilIntervalJam.setError("Interval 1 - 24 jam");
                        return;
                    }
                } catch (NumberFormatException e) { binding.tilIntervalJam.setError("Angka tidak valid"); return; }
                binding.tilIntervalJam.setError(null);
                break;
            case "harian":
                notifTime = binding.etNotifTime.getText() != null ? binding.etNotifTime.getText().toString() : "19:00";
                if (notifTime.isEmpty()) notifTime = "19:00";
                break;
            case "mingguan":
                hariMinggu = binding.actvNotifHari.getText().toString().trim();
                if (hariMinggu.isEmpty()) { binding.tilNotifHari.setError("Pilih hari"); return; }
                binding.tilNotifHari.setError(null);
                notifTime = binding.etNotifTime.getText() != null ? binding.etNotifTime.getText().toString() : "19:00";
                if (notifTime.isEmpty()) notifTime = "19:00";
                break;
            case "bulanan":
                String tglStr = binding.etNotifTanggal.getText() != null ? binding.etNotifTanggal.getText().toString().trim() : "";
                if (tglStr.isEmpty()) { binding.tilNotifTanggal.setError("Masukkan tanggal"); return; }
                try {
                    tanggalBulanan = Integer.parseInt(tglStr);
                    if (tanggalBulanan < 1 || tanggalBulanan > 31) {
                        binding.tilNotifTanggal.setError("Tanggal 1 - 31");
                        return;
                    }
                } catch (NumberFormatException e) { binding.tilNotifTanggal.setError("Angka tidak valid"); return; }
                binding.tilNotifTanggal.setError(null);
                notifTime = binding.etNotifTime.getText() != null ? binding.etNotifTime.getText().toString() : "19:00";
                if (notifTime.isEmpty()) notifTime = "19:00";
                break;
        }

        final String fNama = nama, fDeadline = deadline, fDeskripsi = deskripsi;
        final long fTarget = targetNominal;
        final String fNotifType = notifType, fNotifTime = notifTime, fHariMinggu = hariMinggu;
        final int fIntervalJam = intervalJam, fIntervalMenit = intervalMenit, fTanggalBulanan = tanggalBulanan;

        executor.execute(() -> {
            if (existingTabungan != null) {
                existingTabungan.setNama(fNama);
                existingTabungan.setTargetNominal(fTarget);
                existingTabungan.setDeadline(fDeadline);
                existingTabungan.setDeskripsi(fDeskripsi);
                existingTabungan.setNotifType(fNotifType);
                existingTabungan.setNotifTime(fNotifTime);
                existingTabungan.setNotifIntervalJam(fIntervalJam);
                existingTabungan.setNotifIntervalMenit(fIntervalMenit);
                existingTabungan.setNotifHariMinggu(fHariMinggu);
                existingTabungan.setNotifTanggalBulanan(fTanggalBulanan);
                db.tabunganDao().update(existingTabungan);
                NotificationScheduler.schedule(TambahTabunganActivity.this, existingTabungan);
            } else {
                Tabungan t = new Tabungan();
                t.setNama(fNama);
                t.setTargetNominal(fTarget);
                t.setCurrentNominal(0);
                t.setDeadline(fDeadline);
                t.setDeskripsi(fDeskripsi);
                t.setNotifType(fNotifType);
                t.setNotifTime(fNotifTime);
                t.setNotifIntervalJam(fIntervalJam);
                t.setNotifIntervalMenit(fIntervalMenit);
                t.setNotifHariMinggu(fHariMinggu);
                t.setNotifTanggalBulanan(fTanggalBulanan);
                t.setCreatedAt(System.currentTimeMillis());
                long newId = db.tabunganDao().insert(t);
                t.setId(newId);
                NotificationScheduler.schedule(TambahTabunganActivity.this, t);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "✅ Berhasil disimpan!", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}


