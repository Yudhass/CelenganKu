# 🐘 CelenganKu

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-21%2B-blue.svg)](https://android-arsenal.com/api?level=21)
[![Open Source](https://img.shields.io/badge/Open%20Source-%E2%9D%A4-red.svg)](https://github.com)

**CelenganKu** adalah aplikasi manajemen tabungan berbasis target untuk Android. Aplikasi ini membantu pengguna merencanakan dan memantau progres tabungan secara terstruktur — lengkap dengan riwayat transaksi, pengingat notifikasi fleksibel, serta visualisasi kemajuan menuju target.

---

## 📋 Daftar Isi

- [Tujuan Aplikasi](#-tujuan-aplikasi)
- [Fitur Utama](#-fitur-utama)
- [Alur Penggunaan](#-alur-penggunaan)
- [Sistem Notifikasi](#-sistem-notifikasi)
- [Arsitektur & Struktur Kode](#-arsitektur--struktur-kode)
- [Database](#-database)
- [Teknologi yang Digunakan](#-teknologi-yang-digunakan)
- [Persyaratan Sistem](#-persyaratan-sistem)
- [Struktur Proyek](#-struktur-proyek)
- [Lisensi](#-lisensi)
- [Berkontribusi](#-berkontribusi)

---

## 🎯 Tujuan Aplikasi

CelenganKu dirancang untuk menjawab kebutuhan pengguna yang ingin:

- Menabung secara **terarah dengan target yang jelas** (nama tujuan, nominal, dan deadline)
- Memantau **seberapa jauh kemajuan** tabungan secara real-time
- Mencatat **setiap penambahan dan pengurangan saldo** secara transparan
- Mendapatkan **pengingat otomatis** agar tidak lupa menabung secara rutin
- Mengelola **beberapa tabungan sekaligus** untuk berbagai tujuan yang berbeda

---

## ✨ Fitur Utama

### 1. 🏠 Halaman Utama (Dashboard)
- Menampilkan **jumlah total tabungan** yang dimiliki pengguna
- Menampilkan **total saldo terkumpul** dari semua tabungan (format singkat, contoh: Rp 3,5jt)
- Daftar semua tabungan dalam bentuk kartu (RecyclerView)
- Tampilan **empty state** yang informatif jika belum ada tabungan
- **Floating Action Button (FAB)** untuk membuat tabungan baru dengan cepat
- Setiap kartu tabungan menampilkan:
  - Nama target tabungan
  - Progress bar visual (persentase)
  - Nominal terkumpul vs target
  - Sisa yang harus ditabung
  - Status badge: `Aktif`, `⚠️ Hampir Selesai` (≥80%), atau `✅ Selesai`
  - Tombol **+ Tambah**, **- Kurangi**, dan **Detail**

### 2. ➕ Buat / Edit Tabungan
- **Nama target** tabungan (wajib diisi)
- **Nominal target** dalam format Rupiah dengan pemformatan otomatis saat mengetik
- **Deadline** opsional dengan Date Picker kalender
- **Deskripsi** opsional sebagai catatan
- **Konfigurasi notifikasi** fleksibel (lihat bagian Notifikasi)
- Form yang sama digunakan untuk mode **buat baru** dan **edit** tabungan yang sudah ada

### 3. 📊 Detail Tabungan
- Nama dan progress lengkap tabungan
- **Progress bar besar** dengan persentase pencapaian
- Nominal **terkumpul** dan **target** dalam format Rupiah lengkap
- **Sisa nominal** yang harus dilengkapi (atau tampilan `✅ Lunas!` jika sudah tercapai)
- Tanggal dibuat
- **Deadline** dengan perhitungan sisa hari:
  - Lebih dari 7 hari: warna normal
  - 1–7 hari: warna oranye sebagai peringatan
  - Hari ini / terlewat: warna merah dengan label `⚠️ Hari ini!` / `⚠️ Terlewat`
- Informasi **jadwal notifikasi** yang aktif
- Tombol **+ Tambah Saldo** dan **- Kurangi Saldo**
- Riwayat transaksi dengan **filter chip** (Semua / Masuk / Keluar)
- Tombol **Hapus Semua Riwayat** (dengan konfirmasi dialog, mereset saldo ke 0)
- Menu aksi di toolbar: **Edit** dan **Hapus** tabungan

### 4. 💸 Tambah Transaksi (Masuk / Keluar)
- Mendukung dua mode:
  - **Tambah Saldo** (masuk) — menambah nominal terkumpul
  - **Kurangi Saldo** (keluar) — mengurangi nominal terkumpul
- Input: Nominal (format Rupiah otomatis), Deskripsi opsional, Tanggal (dengan Date Picker)
- Warna UI berbeda antara transaksi masuk (hijau) dan keluar (merah/oranye)
- Mendukung mode **edit** transaksi yang sudah ada:
  - Ketika transaksi diubah, efek lama dibalik terlebih dahulu, lalu efek baru diterapkan
- Validasi input sebelum disimpan

### 5. 🧾 Riwayat Transaksi
- Menampilkan semua transaksi per tabungan, diurutkan dari yang terbaru
- Setiap item menampilkan:
  - Badge tipe: `➕` (masuk) atau `➖` (keluar) dengan warna berbeda
  - Nominal dengan tanda `+` atau `-`
  - Tanggal transaksi
  - Deskripsi (jika ada)
- Context menu (popup) pada setiap item untuk **Edit** atau **Hapus** transaksi
- Hapus transaksi otomatis membalik efeknya pada saldo tabungan
- Filter berdasarkan jenis transaksi

---

## 🔄 Alur Penggunaan

```
[Buka Aplikasi]
      │
      ▼
[Halaman Utama / Dashboard]
      │
      ├── [FAB] ─────────────────────→ [Form Buat Tabungan Baru]
      │                                        │
      │                                        ▼
      │                               [Pilih Nama, Target, Deadline, Notifikasi]
      │                                        │
      │                                        ▼
      │                               [Simpan → Kembali ke Dashboard]
      │
      ├── [Kartu: + Tambah] ─────────→ [Form Tambah Saldo]
      │                                        │
      │                                        ▼
      │                               [Input Nominal & Tanggal → Simpan]
      │
      ├── [Kartu: - Kurangi] ────────→ [Form Kurangi Saldo]
      │
      └── [Kartu: Detail] ──────────→ [Halaman Detail Tabungan]
                                               │
                                               ├── [Edit] → [Form Edit Tabungan]
                                               ├── [Hapus] → [Konfirmasi Dialog]
                                               ├── [+ Tambah Saldo]
                                               ├── [- Kurangi Saldo]
                                               ├── [Filter Riwayat: Semua/Masuk/Keluar]
                                               └── [Tiap transaksi: Edit / Hapus]
```

---

## � Screenshots

<table style="border-collapse: collapse; border: none;">
  <tr>
    <td align="center"><img src="_DEV/ss/1.jpeg" width="280" alt="Dashboard"></td>
    <td align="center"><img src="_DEV/ss/2.jpeg" width="280" alt="Dashboard 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/3.jpeg" width="280" alt="Buat Tabungan"></td>
    <td align="center"><img src="_DEV/ss/4.jpeg" width="280" alt="Buat Tabungan 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/5.jpeg" width="280" alt="Notifikasi Settings"></td>
    <td align="center"><img src="_DEV/ss/6.jpeg" width="280" alt="Notifikasi Settings 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/7.jpeg" width="280" alt="Detail Tabungan"></td>
    <td align="center"><img src="_DEV/ss/8.jpeg" width="280" alt="Detail Tabungan 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/9.jpeg" width="280" alt="Riwayat Transaksi"></td>
    <td align="center"><img src="_DEV/ss/10.jpeg" width="280" alt="Riwayat Transaksi 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/11.jpeg" width="280" alt="Tambah Saldo"></td>
    <td align="center"><img src="_DEV/ss/12.jpeg" width="280" alt="Tambah Saldo 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/13.jpeg" width="280" alt="Kurangi Saldo"></td>
    <td align="center"><img src="_DEV/ss/14.jpeg" width="280" alt="Kurangi Saldo 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/15.jpeg" width="280" alt="Edit Transaksi"></td>
    <td align="center"><img src="_DEV/ss/16.jpeg" width="280" alt="Edit Transaksi 2"></td>
  </tr>
  <tr>
    <td align="center"><img src="_DEV/ss/17.jpeg" width="280" alt="Filter Riwayat"></td>
    <td align="center"><img src="_DEV/ss/18.jpeg" width="280" alt="Filter Riwayat 2"></td>
  </tr>
</table>

---

## �🔔 Sistem Notifikasi

CelenganKu memiliki sistem notifikasi yang fleksibel dan lengkap menggunakan **WorkManager**.

### Jenis Frekuensi Notifikasi

| Frekuensi | Keterangan | Pengaturan Tambahan |
|-----------|-----------|---------------------|
| **Tidak Ada** | Notifikasi dinonaktifkan | — |
| **Per Menit ⏱️** | Setiap X menit (1–59) | Interval menit |
| **Per Jam ⏰** | Setiap X jam (1–24) | Interval jam |
| **Harian 📅** | Setiap hari pada jam tertentu | Jam notifikasi (HH:mm) |
| **Mingguan 📆** | Setiap hari tertentu dalam seminggu | Hari (Sen–Ming) + Jam |
| **Bulanan 🗓️** | Setiap tanggal tertentu setiap bulan | Tanggal (1–31) + Jam |

### Jenis Konten Notifikasi

1. **Pengingat Biasa** — Mengingatkan pengguna untuk menabung, menampilkan sisa nominal yang belum terkumpul
   - Harian: *"Yuk nabung hari ini! 💪 — Sisa Rp X untuk '[Nama Target]'"*
   - Mingguan: *"Sudah nabung minggu ini? 💰"*
   - Bulanan: *"Update tabungan bulan ini! 📊"*

2. **Peringatan Deadline ⚠️** — Otomatis muncul ketika sisa waktu ≤ 5 hari sebelum deadline
   - Menampilkan sisa hari dan sisa nominal yang belum terkumpul

3. **Notifikasi Pencapaian 🎉** — Muncul ketika tabungan sudah mencapai 100% target
   - *"Selamat! Tabungan '[Nama Target]' berhasil tercapai!"*

### Mekanisme Teknis

- **Per Menit**: Menggunakan `OneTimeWorkRequest` yang di-reschedule sendiri setelah setiap eksekusi — untuk menghindari batasan minimum 15 menit dari `PeriodicWorkRequest`
- **Per Jam / Harian / Mingguan / Bulanan**: Menggunakan `PeriodicWorkRequest` dengan `initialDelay` yang dihitung secara akurat agar notifikasi pertama muncul tepat pada waktu yang ditentukan
- Setiap tabungan memiliki **WorkManager tag unik** (`notif_tabungan_{id}`) agar notifikasi bisa dibatalkan secara individual
- Notifikasi dibatalkan otomatis ketika tabungan dihapus

---

## 🏗️ Arsitektur & Struktur Kode

Aplikasi menggunakan arsitektur **MVC sederhana** berbasis Activity dengan pola repository melalui Room DAO.

### Komponen Utama

```
com.yudhas.celenganku
├── MainActivity.java               # Dashboard utama, daftar semua tabungan
├── TambahTabunganActivity.java     # Form buat/edit tabungan + konfigurasi notifikasi
├── DetailTabunganActivity.java     # Detail tabungan + riwayat transaksi
├── TambahTransaksiActivity.java    # Form tambah/edit/hapus transaksi
│
├── adapter/
│   ├── TabunganAdapter.java        # RecyclerView adapter untuk daftar tabungan
│   └── TransaksiAdapter.java       # RecyclerView adapter untuk riwayat transaksi
│
├── database/
│   ├── AppDatabase.java            # Singleton Room database (versi 3)
│   ├── TabunganDao.java            # CRUD + query khusus untuk tabel tabungan
│   ├── TransaksiDao.java           # CRUD + query khusus untuk tabel transaksi
│   └── entity/
│       ├── Tabungan.java           # Entity tabel tabungan
│       └── Transaksi.java          # Entity tabel transaksi
│
├── notification/
│   ├── NotificationHelper.java     # Membuat channel & mengirim notifikasi
│   ├── NotificationScheduler.java  # Menjadwalkan WorkManager per tabungan
│   └── NotificationWorker.java     # Worker yang dijalankan WorkManager
│
└── util/
    ├── CurrencyHelper.java         # Format Rupiah (Rp 1.000.000 / Rp 3,5jt)
    ├── CurrencyTextWatcher.java    # TextWatcher untuk format Rupiah real-time
    └── DateHelper.java             # Format tanggal, hitung sisa hari, initial delay
```

### Pola Pemrograman
- **View Binding**: Semua layout diakses melalui binding yang di-generate otomatis (tidak ada `findViewById`)
- **ExecutorService**: Semua operasi database dijalankan di thread terpisah (`Executors.newSingleThreadExecutor()`) untuk menghindari blocking UI thread
- **Singleton Database**: `AppDatabase` menggunakan pola singleton thread-safe dengan double-checked locking

---

## 🗄️ Database

Aplikasi menggunakan **Room (SQLite)** dengan nama database `celenganku_db` versi 3.

### Tabel `tabungan`

| Kolom | Tipe | Keterangan |
|-------|------|------------|
| `id` | INTEGER (PK, auto) | ID unik tabungan |
| `nama` | TEXT | Nama target tabungan |
| `target_nominal` | INTEGER | Target nominal dalam Rupiah |
| `current_nominal` | INTEGER | Nominal yang sudah terkumpul |
| `deadline` | TEXT | Deadline opsional (format: dd/MM/yyyy) |
| `deskripsi` | TEXT | Catatan/deskripsi opsional |
| `notif_type` | TEXT | Tipe notifikasi: `none`, `permenit`, `perjam`, `harian`, `mingguan`, `bulanan` |
| `notif_time` | TEXT | Jam notifikasi (format: HH:mm) untuk harian/mingguan/bulanan |
| `notif_interval_jam` | INTEGER | Interval jam untuk tipe `perjam` (1–24) |
| `notif_interval_menit` | INTEGER | Interval menit untuk tipe `permenit` (1–59) |
| `notif_hari_minggu` | TEXT | Nama hari untuk tipe `mingguan` (Senin–Minggu) |
| `notif_tanggal_bulanan` | INTEGER | Tanggal untuk tipe `bulanan` (1–31) |
| `created_at` | INTEGER | Timestamp pembuatan (milliseconds) |

### Tabel `transaksi`

| Kolom | Tipe | Keterangan |
|-------|------|------------|
| `id` | INTEGER (PK, auto) | ID unik transaksi |
| `tabungan_id` | INTEGER (FK) | Referensi ke tabel `tabungan` (CASCADE DELETE) |
| `tipe` | TEXT | `masuk` atau `keluar` |
| `nominal` | INTEGER | Jumlah transaksi dalam Rupiah |
| `deskripsi` | TEXT | Catatan opsional |
| `tanggal` | INTEGER | Timestamp transaksi (milliseconds) |

### Migrasi Database

| Versi | Perubahan |
|-------|-----------|
| v1 → v2 | Tambah kolom `notif_interval_jam`, `notif_hari_minggu`, `notif_tanggal_bulanan` |
| v2 → v3 | Tambah kolom `notif_interval_menit` (untuk tipe notifikasi per menit) |

---

## 🛠️ Teknologi yang Digunakan

| Teknologi | Versi | Kegunaan |
|-----------|-------|---------|
| **Android SDK** | minSdk 21 / targetSdk 36 | Platform Android |
| **Java** | 11 | Bahasa pemrograman utama |
| **AndroidX AppCompat** | 1.6.1 | Kompatibilitas komponen UI |
| **Material Design** | 1.12.0 | Komponen UI modern (TextInputLayout, CardView, dll) |
| **Room** | 2.6.1 | ORM / database lokal SQLite |
| **WorkManager** | 2.9.0 | Penjadwalan notifikasi background |
| **RecyclerView** | 1.3.2 | Daftar tabungan dan transaksi |
| **CardView** | 1.0.0 | Tampilan kartu tabungan |
| **View Binding** | — | Binding layout XML ke kode Java |
| **AGP (Gradle Plugin)** | 9.1.0 | Build system Android |

---

## 📱 Persyaratan Sistem

- **OS**: Android 5.0 (Lollipop) ke atas — API Level 21+
- **Izin yang Diperlukan**:
  - `POST_NOTIFICATIONS` — Untuk mengirim notifikasi (Android 13+, diminta saat pertama buka)
  - `RECEIVE_BOOT_COMPLETED` — Untuk memulihkan jadwal notifikasi setelah perangkat restart
  - `VIBRATE` — Untuk getaran pada notifikasi
- **Penyimpanan**: Data disimpan secara **lokal** di perangkat (tidak memerlukan koneksi internet)

---

## 📂 Struktur Proyek

```
CelenganKu/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/yudhas/celenganku/   # Source code Java
│   │   └── res/
│   │       ├── layout/                   # 6 file layout XML
│   │       │   ├── activity_main.xml
│   │       │   ├── activity_tambah_tabungan.xml
│   │       │   ├── activity_detail_tabungan.xml
│   │       │   ├── activity_tambah_transaksi.xml
│   │       │   ├── item_tabungan.xml
│   │       │   └── item_transaksi.xml
│   │       ├── drawable/                 # Ikon dan background drawable
│   │       ├── values/                   # strings.xml, colors.xml, themes.xml
│   │       └── xml/                      # Konfigurasi backup
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml               # Versi dependency terpusat
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## � Lisensi

Proyek ini bersifat **open source** dan dilisensikan di bawah [MIT License](LICENSE).

```
MIT License

Copyright (c) 2026 Yudhas

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🤝 Berkontribusi

Kontribusi sangat disambut! Baik itu laporan bug, usulan fitur, perbaikan kode, maupun peningkatan dokumentasi.

### Langkah Kontribusi via Pull Request

1. **Fork** repository ini
   ```
   Klik tombol "Fork" di pojok kanan atas halaman GitHub
   ```

2. **Clone** fork Anda ke lokal
   ```bash
   git clone https://github.com/<username-anda>/CelenganKu.git
   cd CelenganKu
   ```

3. **Buat branch baru** untuk fitur atau perbaikan Anda
   ```bash
   git checkout -b fitur/nama-fitur-anda
   # atau untuk bugfix:
   git checkout -b fix/nama-bug-anda
   ```

4. **Lakukan perubahan** pada kode, lalu commit
   ```bash
   git add .
   git commit -m "feat: tambah fitur xxx"
   ```
   > Gunakan format pesan commit yang jelas:
   > - `feat:` untuk fitur baru
   > - `fix:` untuk perbaikan bug
   > - `docs:` untuk perubahan dokumentasi
   > - `refactor:` untuk refactoring kode

5. **Push** branch ke fork Anda
   ```bash
   git push origin fitur/nama-fitur-anda
   ```

6. **Buat Pull Request** ke repository utama
   - Buka halaman repository di GitHub
   - Klik **"Compare & pull request"**
   - Isi judul dan deskripsi yang menjelaskan perubahan Anda
   - Klik **"Create pull request"**

### Panduan Kontribusi

- Pastikan kode dapat di-build tanpa error sebelum membuat pull request
- Ikuti gaya kode yang sudah ada (Java, indentasi 4 spasi)
- Satu pull request sebaiknya fokus pada satu perubahan/fitur
- Tambahkan deskripsi yang jelas tentang apa yang diubah dan mengapa
- Jika menemukan bug, silakan buat **Issue** terlebih dahulu sebelum pull request

### Ide Kontribusi

Berikut beberapa ide yang bisa dikerjakan:

- [ ] Migrasi ke arsitektur MVVM dengan ViewModel + LiveData
- [ ] Ekspor / impor data tabungan ke file JSON atau CSV
- [ ] Statistik tabungan dengan grafik (chart)
- [ ] Dukungan tema gelap (Dark Mode)
- [ ] Widget layar utama Android
- [ ] Backup & restore data ke cloud
- [ ] Kategori / tag pada tabungan
- [ ] Pengurutan dan pencarian tabungan

---

## 👨‍💻 Developer

Dikembangkan oleh **Yudhas** — paket: `com.yudhas.celenganku`

> ⭐ Jika proyek ini bermanfaat, jangan lupa berikan **star** di GitHub!
