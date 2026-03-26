package com.yudhas.celenganku.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents ONE notification schedule tied to a Tabungan.
 * A Tabungan can have MULTIPLE NotifikasiTabungan rows.
 */
@Entity(
    tableName = "notifikasi_tabungan",
    foreignKeys = @ForeignKey(
        entity = Tabungan.class,
        parentColumns = "id",
        childColumns = "tabungan_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("tabungan_id")}
)
public class NotifikasiTabungan {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "tabungan_id")
    private long tabunganId;

    @ColumnInfo(name = "notif_type")
    private String notifType; // "permenit","perjam","harian","mingguan","bulanan"

    @ColumnInfo(name = "notif_time")
    private String notifTime; // HH:mm — untuk harian, mingguan, bulanan

    @ColumnInfo(name = "notif_interval_jam")
    private int notifIntervalJam; // untuk perjam

    @ColumnInfo(name = "notif_interval_menit")
    private int notifIntervalMenit; // untuk permenit

    @ColumnInfo(name = "notif_hari_minggu")
    private String notifHariMinggu; // untuk mingguan

    @ColumnInfo(name = "notif_tanggal_bulanan")
    private int notifTanggalBulanan; // untuk bulanan

    public NotifikasiTabungan() {}

    // --- Getters ---
    public long getId()                 { return id; }
    public long getTabunganId()         { return tabunganId; }
    public String getNotifType()        { return notifType; }
    public String getNotifTime()        { return notifTime; }
    public int getNotifIntervalJam()    { return notifIntervalJam; }
    public int getNotifIntervalMenit()  { return notifIntervalMenit; }
    public String getNotifHariMinggu()  { return notifHariMinggu; }
    public int getNotifTanggalBulanan() { return notifTanggalBulanan; }

    // --- Setters ---
    public void setId(long id)                         { this.id = id; }
    public void setTabunganId(long tabunganId)         { this.tabunganId = tabunganId; }
    public void setNotifType(String notifType)         { this.notifType = notifType; }
    public void setNotifTime(String notifTime)         { this.notifTime = notifTime; }
    public void setNotifIntervalJam(int v)             { this.notifIntervalJam = v; }
    public void setNotifIntervalMenit(int v)           { this.notifIntervalMenit = v; }
    public void setNotifHariMinggu(String v)           { this.notifHariMinggu = v; }
    public void setNotifTanggalBulanan(int v)          { this.notifTanggalBulanan = v; }

    /** Human-readable one-line summary. */
    public String getSummary() {
        if (notifType == null) return "—";
        switch (notifType) {
            case "permenit": return "Setiap " + notifIntervalMenit + " menit ⏱️";
            case "perjam":   return "Setiap " + notifIntervalJam + " jam ⏰";
            case "harian":   return "Harian 📅  " + notifTime;
            case "mingguan": return "Mingguan 📆  " + notifHariMinggu + " " + notifTime;
            case "bulanan":  return "Bulanan 🗓️  tgl " + notifTanggalBulanan + " " + notifTime;
            default:         return "—";
        }
    }
}

