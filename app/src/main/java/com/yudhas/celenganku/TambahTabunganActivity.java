package com.yudhas.celenganku;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yudhas.celenganku.database.AppDatabase;
import com.yudhas.celenganku.database.entity.NotifikasiTabungan;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.databinding.ActivityTambahTabunganBinding;
import com.yudhas.celenganku.notification.NotificationScheduler;
import com.yudhas.celenganku.util.CurrencyTextWatcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    // Multi-notification draft list
    private final List<NotifikasiTabungan> draftNotifList = new ArrayList<>();
    private int editingNotifIndex = -1; // -1 = adding new, >=0 = editing existing

    private final String[] notifLabels = {"Pilih tipe…", "Per Menit ⏱️", "Per Jam ⏰", "Harian 📅", "Mingguan 📆", "Bulanan 🗓️"};
    private final String[] notifValues = {"none", "permenit", "perjam", "harian", "mingguan", "bulanan"};
    private final String[] hariLabels  = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityTambahTabunganBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBars.bottom);
            return windowInsets;
        });

        db = AppDatabase.getInstance(this);
        tabunganId = getIntent().getLongExtra(EXTRA_TABUNGAN_ID, -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupCurrencyInput();
        setupNotifTypeDropdown();
        setupHariDropdown();
        setupDatePicker();
        setupTimePicker();
        setupNotifFormButtons();

        if (tabunganId != -1) {
            binding.toolbar.setTitle("Edit Tabungan");
            loadExistingData();
        } else {
            binding.toolbar.setTitle("Buat Tabungan Baru");
            resetNotifForm();
        }

        binding.btnTambahNotif.setOnClickListener(v -> showNotifForm(-1));
        binding.btnSimpan.setOnClickListener(v -> saveTabungan());
    }

    // ── Currency ──────────────────────────────────────────────────────────────

    private void setupCurrencyInput() {
        binding.etTarget.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        binding.etTarget.addTextChangedListener(new CurrencyTextWatcher(binding.etTarget));
    }

    // ── Notification type dropdown (inside form card) ─────────────────────────

    private void setupNotifTypeDropdown() {
        // Exclude the placeholder "Pilih tipe…" in the adapter shown label,
        // but keep full array for index lookup. We use labels[1..5] in the dropdown.
        String[] adapterLabels = new String[notifLabels.length - 1];
        System.arraycopy(notifLabels, 1, adapterLabels, 0, adapterLabels.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, adapterLabels);
        binding.actvNotifType.setAdapter(adapter);
        binding.actvNotifType.setOnItemClickListener((parent, view, position, id) -> {
            // position 0..4 → notifValues[1..5]
            updateNotifFieldsVisibility(notifValues[position + 1]);
        });
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
        }
    }

    // ── Date / Time pickers ───────────────────────────────────────────────────

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

    // ── Notification form lifecycle ───────────────────────────────────────────

    private void setupNotifFormButtons() {
        binding.btnBatalJadwal.setOnClickListener(v -> hideNotifForm());
        binding.btnSimpanJadwal.setOnClickListener(v -> onSimpanJadwal());
    }

    /** Open the inline notification form. index=-1 → add new; index>=0 → edit existing. */
    private void showNotifForm(int index) {
        editingNotifIndex = index;
        if (index < 0) {
            binding.tvFormNotifTitle.setText("📝 Jadwal Baru");
            resetNotifForm();
        } else {
            binding.tvFormNotifTitle.setText("✏️ Edit Jadwal " + (index + 1));
            populateFormFromNotif(draftNotifList.get(index));
        }
        binding.cardNotifForm.setVisibility(View.VISIBLE);
        // Scroll to form
        binding.getRoot().post(() -> {
            // Find scroll position of the form card and scroll to it
            int[] loc = new int[2];
            binding.cardNotifForm.getLocationOnScreen(loc);
        });
    }

    private void hideNotifForm() {
        binding.cardNotifForm.setVisibility(View.GONE);
        editingNotifIndex = -1;
        resetNotifForm();
    }

    private void resetNotifForm() {
        binding.actvNotifType.setText("", false);
        updateNotifFieldsVisibility("none");
        binding.etNotifTime.setText("19:00");
        binding.etIntervalJam.setText("");
        binding.etIntervalMenit.setText("");
        binding.actvNotifHari.setText("", false);
        binding.etNotifTanggal.setText("");
        // Clear errors
        binding.tilIntervalJam.setError(null);
        binding.tilIntervalMenit.setError(null);
        binding.tilNotifHari.setError(null);
        binding.tilNotifTanggal.setError(null);
    }

    private void populateFormFromNotif(NotifikasiTabungan notif) {
        String type = notif.getNotifType() != null ? notif.getNotifType() : "none";
        // Find label for this type (skip index 0 = "Pilih tipe…")
        for (int i = 1; i < notifValues.length; i++) {
            if (notifValues[i].equals(type)) {
                binding.actvNotifType.setText(notifLabels[i], false);
                break;
            }
        }
        binding.etNotifTime.setText(notif.getNotifTime() != null ? notif.getNotifTime() : "19:00");
        binding.etIntervalJam.setText(notif.getNotifIntervalJam() > 0 ? String.valueOf(notif.getNotifIntervalJam()) : "");
        binding.etIntervalMenit.setText(notif.getNotifIntervalMenit() > 0 ? String.valueOf(notif.getNotifIntervalMenit()) : "");
        binding.actvNotifHari.setText(notif.getNotifHariMinggu() != null ? notif.getNotifHariMinggu() : "", false);
        binding.etNotifTanggal.setText(notif.getNotifTanggalBulanan() > 0 ? String.valueOf(notif.getNotifTanggalBulanan()) : "");
        updateNotifFieldsVisibility(type);
    }

    /**
     * Reads current form and builds a NotifikasiTabungan.
     * Returns null if validation fails (errors already set on inputs).
     */
    private NotifikasiTabungan extractNotifFromForm() {
        String selectedLabel = binding.actvNotifType.getText().toString().trim();
        String notifType = "none";
        for (int i = 1; i < notifLabels.length; i++) {
            if (notifLabels[i].equals(selectedLabel)) { notifType = notifValues[i]; break; }
        }

        if ("none".equals(notifType)) {
            Toast.makeText(this, "Pilih tipe frekuensi notifikasi terlebih dahulu", Toast.LENGTH_SHORT).show();
            return null;
        }

        NotifikasiTabungan notif = new NotifikasiTabungan();
        notif.setNotifType(notifType);

        switch (notifType) {
            case "permenit": {
                String s = binding.etIntervalMenit.getText() != null ? binding.etIntervalMenit.getText().toString().trim() : "";
                if (s.isEmpty()) { binding.tilIntervalMenit.setError("Masukkan interval menit"); return null; }
                try {
                    int v = Integer.parseInt(s);
                    if (v < 1 || v > 59) { binding.tilIntervalMenit.setError("Interval 1 – 59 menit"); return null; }
                    notif.setNotifIntervalMenit(v);
                } catch (NumberFormatException e) { binding.tilIntervalMenit.setError("Angka tidak valid"); return null; }
                binding.tilIntervalMenit.setError(null);
                break;
            }
            case "perjam": {
                String s = binding.etIntervalJam.getText() != null ? binding.etIntervalJam.getText().toString().trim() : "";
                if (s.isEmpty()) { binding.tilIntervalJam.setError("Masukkan interval jam"); return null; }
                try {
                    int v = Integer.parseInt(s);
                    if (v < 1 || v > 24) { binding.tilIntervalJam.setError("Interval 1 – 24 jam"); return null; }
                    notif.setNotifIntervalJam(v);
                } catch (NumberFormatException e) { binding.tilIntervalJam.setError("Angka tidak valid"); return null; }
                binding.tilIntervalJam.setError(null);
                break;
            }
            case "harian": {
                String t = binding.etNotifTime.getText() != null ? binding.etNotifTime.getText().toString() : "19:00";
                notif.setNotifTime(t.isEmpty() ? "19:00" : t);
                break;
            }
            case "mingguan": {
                String hari = binding.actvNotifHari.getText().toString().trim();
                if (hari.isEmpty()) { binding.tilNotifHari.setError("Pilih hari"); return null; }
                binding.tilNotifHari.setError(null);
                String t = binding.etNotifTime.getText() != null ? binding.etNotifTime.getText().toString() : "19:00";
                notif.setNotifHariMinggu(hari);
                notif.setNotifTime(t.isEmpty() ? "19:00" : t);
                break;
            }
            case "bulanan": {
                String s = binding.etNotifTanggal.getText() != null ? binding.etNotifTanggal.getText().toString().trim() : "";
                if (s.isEmpty()) { binding.tilNotifTanggal.setError("Masukkan tanggal"); return null; }
                try {
                    int v = Integer.parseInt(s);
                    if (v < 1 || v > 31) { binding.tilNotifTanggal.setError("Tanggal 1 – 31"); return null; }
                    notif.setNotifTanggalBulanan(v);
                } catch (NumberFormatException e) { binding.tilNotifTanggal.setError("Angka tidak valid"); return null; }
                binding.tilNotifTanggal.setError(null);
                String t = binding.etNotifTime.getText() != null ? binding.etNotifTime.getText().toString() : "19:00";
                notif.setNotifTime(t.isEmpty() ? "19:00" : t);
                break;
            }
        }
        return notif;
    }

    /** Called when user taps "Simpan Jadwal" in the inline form. */
    private void onSimpanJadwal() {
        NotifikasiTabungan notif = extractNotifFromForm();
        if (notif == null) return; // validation failed

        if (editingNotifIndex >= 0) {
            // Preserve DB id if editing an existing record
            notif.setId(draftNotifList.get(editingNotifIndex).getId());
            draftNotifList.set(editingNotifIndex, notif);
        } else {
            draftNotifList.add(notif);
        }

        hideNotifForm();
        refreshNotifListUI();
    }

    /** Rebuilds the visual notification list from draftNotifList. */
    private void refreshNotifListUI() {
        binding.layoutNotifList.removeAllViews();
        int count = draftNotifList.size();

        binding.tvNoNotifHint.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        binding.tvNotifCount.setText(count + " jadwal");

        if (count == 0) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < count; i++) {
            final int idx = i;
            NotifikasiTabungan notif = draftNotifList.get(i);

            View itemView = inflater.inflate(R.layout.item_notifikasi, binding.layoutNotifList, false);
            TextView tvSummary    = itemView.findViewById(R.id.tvNotifItemSummary);
            ImageButton btnEdit   = itemView.findViewById(R.id.btnEditNotifItem);
            ImageButton btnDelete = itemView.findViewById(R.id.btnDeleteNotifItem);

            tvSummary.setText((idx + 1) + ". " + notif.getSummary());
            btnEdit.setOnClickListener(v -> showNotifForm(idx));
            btnDelete.setOnClickListener(v -> {
                draftNotifList.remove(idx);
                refreshNotifListUI();
            });

            binding.layoutNotifList.addView(itemView);
        }
    }

    // ── Load existing tabungan for edit mode ──────────────────────────────────

    private void loadExistingData() {
        executor.execute(() -> {
            existingTabungan = db.tabunganDao().getById(tabunganId);
            if (existingTabungan == null) { finish(); return; }

            // Load existing notification schedules
            List<NotifikasiTabungan> existing = db.notifikasiTabunganDao().getByTabunganId(tabunganId);

            runOnUiThread(() -> {
                binding.etNama.setText(existingTabungan.getNama());
                CurrencyTextWatcher.setValue(binding.etTarget, existingTabungan.getTargetNominal());
                binding.etDeadline.setText(existingTabungan.getDeadline() != null ? existingTabungan.getDeadline() : "");
                binding.etDeskripsi.setText(existingTabungan.getDeskripsi() != null ? existingTabungan.getDeskripsi() : "");

                draftNotifList.clear();
                draftNotifList.addAll(existing);
                refreshNotifListUI();
            });
        });
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private void saveTabungan() {
        // If the inline form is open, attempt to auto-save it first
        if (binding.cardNotifForm.getVisibility() == View.VISIBLE) {
            String selectedLabel = binding.actvNotifType.getText().toString().trim();
            boolean hasTypeSelected = false;
            for (int i = 1; i < notifLabels.length; i++) {
                if (notifLabels[i].equals(selectedLabel)) { hasTypeSelected = true; break; }
            }
            if (hasTypeSelected) {
                NotifikasiTabungan pending = extractNotifFromForm();
                if (pending == null) {
                    // Has errors — stop and let user fix them
                    Toast.makeText(this, "Perbaiki atau batalkan jadwal notifikasi yang sedang dibuat", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editingNotifIndex >= 0) {
                    pending.setId(draftNotifList.get(editingNotifIndex).getId());
                    draftNotifList.set(editingNotifIndex, pending);
                } else {
                    draftNotifList.add(pending);
                }
            }
            hideNotifForm();
            refreshNotifListUI();
        }

        // Validate basic fields
        String nama = binding.etNama.getText() != null ? binding.etNama.getText().toString().trim() : "";
        if (nama.isEmpty()) { binding.tilNama.setError("Nama target tidak boleh kosong"); return; }
        binding.tilNama.setError(null);

        long targetNominal = CurrencyTextWatcher.getValue(binding.etTarget);
        if (targetNominal <= 0) { binding.tilTarget.setError("Nominal target harus lebih dari 0"); return; }
        binding.tilTarget.setError(null);

        String deadline  = binding.etDeadline.getText() != null ? binding.etDeadline.getText().toString().trim() : "";
        String deskripsi = binding.etDeskripsi.getText() != null ? binding.etDeskripsi.getText().toString().trim() : "";

        final String fNama = nama, fDeadline = deadline, fDeskripsi = deskripsi;
        final long fTarget = targetNominal;
        final List<NotifikasiTabungan> fDraftList = new ArrayList<>(draftNotifList);

        executor.execute(() -> {
            long savedTabunganId;

            if (existingTabungan != null) {
                existingTabungan.setNama(fNama);
                existingTabungan.setTargetNominal(fTarget);
                existingTabungan.setDeadline(fDeadline);
                existingTabungan.setDeskripsi(fDeskripsi);
                db.tabunganDao().update(existingTabungan);
                savedTabunganId = existingTabungan.getId();
            } else {
                Tabungan t = new Tabungan();
                t.setNama(fNama);
                t.setTargetNominal(fTarget);
                t.setCurrentNominal(0);
                t.setDeadline(fDeadline);
                t.setDeskripsi(fDeskripsi);
                t.setCreatedAt(System.currentTimeMillis());
                savedTabunganId = db.tabunganDao().insert(t);
            }

            // Replace all notifications: delete old, insert new, reschedule
            NotificationScheduler.cancelAll(TambahTabunganActivity.this, savedTabunganId);
            db.notifikasiTabunganDao().deleteByTabunganId(savedTabunganId);

            List<NotifikasiTabungan> insertedList = new ArrayList<>();
            for (NotifikasiTabungan draft : fDraftList) {
                draft.setId(0); // reset so Room auto-generates new PK
                draft.setTabunganId(savedTabunganId);
                long newId = db.notifikasiTabunganDao().insert(draft);
                draft.setId(newId);
                insertedList.add(draft);
            }

            NotificationScheduler.scheduleAll(TambahTabunganActivity.this, savedTabunganId, insertedList);

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

