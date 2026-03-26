package com.yudhas.celenganku.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.yudhas.celenganku.database.entity.Transaksi;

import java.util.List;

@Dao
public interface TransaksiDao {

    @Insert
    long insert(Transaksi transaksi);

    @Update
    void update(Transaksi transaksi);

    @Delete
    void delete(Transaksi transaksi);

    @Query("SELECT * FROM transaksi WHERE tabungan_id = :tabunganId ORDER BY tanggal DESC")
    List<Transaksi> getByTabunganId(long tabunganId);

    @Query("SELECT * FROM transaksi WHERE tabungan_id = :tabunganId AND tipe = :tipe ORDER BY tanggal DESC")
    List<Transaksi> getByTabunganIdAndTipe(long tabunganId, String tipe);

    @Query("SELECT * FROM transaksi WHERE id = :id LIMIT 1")
    Transaksi getById(long id);

    @Query("DELETE FROM transaksi WHERE tabungan_id = :tabunganId")
    void deleteByTabunganId(long tabunganId);
}

