package com.yudhas.celenganku.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.yudhas.celenganku.database.entity.NotifikasiTabungan;

import java.util.List;

@Dao
public interface NotifikasiTabunganDao {

    @Insert
    long insert(NotifikasiTabungan notifikasi);

    @Update
    void update(NotifikasiTabungan notifikasi);

    @Delete
    void delete(NotifikasiTabungan notifikasi);

    @Query("SELECT * FROM notifikasi_tabungan WHERE tabungan_id = :tabunganId ORDER BY id ASC")
    List<NotifikasiTabungan> getByTabunganId(long tabunganId);

    @Query("SELECT * FROM notifikasi_tabungan WHERE id = :id LIMIT 1")
    NotifikasiTabungan getById(long id);

    @Query("DELETE FROM notifikasi_tabungan WHERE tabungan_id = :tabunganId")
    void deleteByTabunganId(long tabunganId);
}

