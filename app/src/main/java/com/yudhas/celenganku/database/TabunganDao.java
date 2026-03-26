package com.yudhas.celenganku.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.yudhas.celenganku.database.entity.Tabungan;

import java.util.List;

@Dao
public interface TabunganDao {

    @Insert
    long insert(Tabungan tabungan);

    @Update
    void update(Tabungan tabungan);

    @Delete
    void delete(Tabungan tabungan);

    @Query("SELECT * FROM tabungan ORDER BY created_at DESC")
    List<Tabungan> getAll();

    @Query("SELECT * FROM tabungan WHERE id = :id LIMIT 1")
    Tabungan getById(long id);

    @Query("SELECT COUNT(*) FROM tabungan")
    int getCount();

    @Query("UPDATE tabungan SET current_nominal = current_nominal + :amount WHERE id = :id")
    void addNominal(long id, long amount);

    @Query("UPDATE tabungan SET current_nominal = MAX(0, current_nominal - :amount) WHERE id = :id")
    void subtractNominal(long id, long amount);

    @Query("UPDATE tabungan SET current_nominal = 0 WHERE id = :id")
    void resetNominal(long id);
}

