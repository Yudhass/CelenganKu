package com.yudhas.celenganku.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.database.entity.Transaksi;

@Database(entities = {Tabungan.class, Transaksi.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "celenganku_db";
    private static volatile AppDatabase instance;

    public abstract TabunganDao tabunganDao();
    public abstract TransaksiDao transaksiDao();

    // Migration v1 → v2: tambah 3 kolom notifikasi baru
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tabungan ADD COLUMN notif_interval_jam INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tabungan ADD COLUMN notif_hari_minggu TEXT");
            database.execSQL("ALTER TABLE tabungan ADD COLUMN notif_tanggal_bulanan INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Migration v2 → v3: tambah kolom frekuensi notif per menit
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tabungan ADD COLUMN notif_interval_menit INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
                }
            }
        }
        return instance;
    }
}
