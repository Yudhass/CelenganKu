package com.yudhas.celenganku.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "transaksi",
    foreignKeys = @ForeignKey(
        entity = Tabungan.class,
        parentColumns = "id",
        childColumns = "tabungan_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("tabungan_id")}
)
public class Transaksi {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "tabungan_id")
    private long tabunganId;

    @ColumnInfo(name = "tipe")
    private String tipe; // "masuk" atau "keluar"

    @ColumnInfo(name = "nominal")
    private long nominal;

    @ColumnInfo(name = "deskripsi")
    private String deskripsi;

    @ColumnInfo(name = "tanggal")
    private long tanggal; // timestamp in milliseconds

    // Constructor
    public Transaksi() {}

    // --- Getters ---
    public long getId() { return id; }
    public long getTabunganId() { return tabunganId; }
    public String getTipe() { return tipe; }
    public long getNominal() { return nominal; }
    public String getDeskripsi() { return deskripsi; }
    public long getTanggal() { return tanggal; }

    // --- Setters ---
    public void setId(long id) { this.id = id; }
    public void setTabunganId(long tabunganId) { this.tabunganId = tabunganId; }
    public void setTipe(String tipe) { this.tipe = tipe; }
    public void setNominal(long nominal) { this.nominal = nominal; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setTanggal(long tanggal) { this.tanggal = tanggal; }
}

