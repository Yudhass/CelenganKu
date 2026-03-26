package com.yudhas.celenganku.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.yudhas.celenganku.database.entity.NotifikasiTabungan;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.database.entity.Transaksi;

@Database(entities = {Tabungan.class, Transaksi.class, NotifikasiTabungan.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "celenganku_db";
    private static volatile AppDatabase instance;

    public abstract TabunganDao tabunganDao();
    public abstract TransaksiDao transaksiDao();
    public abstract NotifikasiTabunganDao notifikasiTabunganDao();

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

    // Migration v3 → v4: buat tabel notifikasi_tabungan (multi-notif per tabungan)
    //   + migrasi data notif lama dari kolom tabungan ke tabel baru
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `notifikasi_tabungan` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`tabungan_id` INTEGER NOT NULL," +
                "`notif_type` TEXT," +
                "`notif_time` TEXT," +
                "`notif_interval_jam` INTEGER NOT NULL DEFAULT 0," +
                "`notif_interval_menit` INTEGER NOT NULL DEFAULT 0," +
                "`notif_hari_minggu` TEXT," +
                "`notif_tanggal_bulanan` INTEGER NOT NULL DEFAULT 0," +
                "FOREIGN KEY(`tabungan_id`) REFERENCES `tabungan`(`id`) ON DELETE CASCADE)"
            );
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_notifikasi_tabungan_tabungan_id` " +
                "ON `notifikasi_tabungan` (`tabungan_id`)"
            );
            // Migrate existing single-notif data from tabungan columns
            database.execSQL(
                "INSERT INTO `notifikasi_tabungan` " +
                "(`tabungan_id`,`notif_type`,`notif_time`,`notif_interval_jam`," +
                "`notif_interval_menit`,`notif_hari_minggu`,`notif_tanggal_bulanan`) " +
                "SELECT `id`,`notif_type`,`notif_time`,`notif_interval_jam`," +
                "`notif_interval_menit`,`notif_hari_minggu`,`notif_tanggal_bulanan` " +
                "FROM `tabungan` WHERE `notif_type` IS NOT NULL AND `notif_type` != 'none'"
            );
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build();
                }
            }
        }
        return instance;
    }
}
