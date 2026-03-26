package com.yudhas.celenganku.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tabungan")
public class Tabungan {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "nama")
    private String nama;

    @ColumnInfo(name = "target_nominal")
    private long targetNominal;

    @ColumnInfo(name = "current_nominal")
    private long currentNominal;

    @ColumnInfo(name = "deadline")
    private String deadline; // nullable, format: dd/MM/yyyy

    @ColumnInfo(name = "deskripsi")
    private String deskripsi;

    @ColumnInfo(name = "notif_type")
    private String notifType; // "none","perjam","harian","mingguan","bulanan"

    @ColumnInfo(name = "notif_time")
    private String notifTime; // HH:mm — untuk harian, mingguan, bulanan

    @ColumnInfo(name = "notif_interval_jam")
    private int notifIntervalJam; // untuk perjam: setiap X jam

    @ColumnInfo(name = "notif_interval_menit")
    private int notifIntervalMenit; // untuk permenit: setiap X menit

    @ColumnInfo(name = "notif_hari_minggu")
    private String notifHariMinggu; // untuk mingguan: "Senin"..."Minggu"

    @ColumnInfo(name = "notif_tanggal_bulanan")
    private int notifTanggalBulanan; // untuk bulanan: 1-31

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Constructor
    public Tabungan() {}

    // --- Getters ---
    public long getId() { return id; }
    public String getNama() { return nama; }
    public long getTargetNominal() { return targetNominal; }
    public long getCurrentNominal() { return currentNominal; }
    public String getDeadline() { return deadline; }
    public String getDeskripsi() { return deskripsi; }
    public String getNotifType() { return notifType; }
    public String getNotifTime() { return notifTime; }
    public int getNotifIntervalJam() { return notifIntervalJam; }
    public int getNotifIntervalMenit() { return notifIntervalMenit; }
    public String getNotifHariMinggu() { return notifHariMinggu; }
    public int getNotifTanggalBulanan() { return notifTanggalBulanan; }
    public long getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setId(long id) { this.id = id; }
    public void setNama(String nama) { this.nama = nama; }
    public void setTargetNominal(long targetNominal) { this.targetNominal = targetNominal; }
    public void setCurrentNominal(long currentNominal) { this.currentNominal = currentNominal; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setNotifType(String notifType) { this.notifType = notifType; }
    public void setNotifTime(String notifTime) { this.notifTime = notifTime; }
    public void setNotifIntervalJam(int notifIntervalJam) { this.notifIntervalJam = notifIntervalJam; }
    public void setNotifIntervalMenit(int notifIntervalMenit) { this.notifIntervalMenit = notifIntervalMenit; }
    public void setNotifHariMinggu(String notifHariMinggu) { this.notifHariMinggu = notifHariMinggu; }
    public void setNotifTanggalBulanan(int notifTanggalBulanan) { this.notifTanggalBulanan = notifTanggalBulanan; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // --- Helper Methods ---
    public long getSisaNominal() {
        return Math.max(0, targetNominal - currentNominal);
    }

    public int getProgressPercent() {
        if (targetNominal <= 0) return 0;
        int progress = (int) ((currentNominal * 100) / targetNominal);
        return Math.min(100, Math.max(0, progress));
    }

    public boolean isCompleted() {
        return currentNominal >= targetNominal;
    }
}
