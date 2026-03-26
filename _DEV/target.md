
# 📱 Konsep Aplikasi: **CelenganKu (Updated)**

## 🎯 1. Core Idea

Aplikasi tabungan berbasis target yang:

* Menampilkan **total terkumpul**
* Menampilkan **sisa yang harus ditabung**
* Memberikan **notifikasi fleksibel (harian, mingguan, bulanan)**

---

## 💰 2. Fitur Utama (Updated)

### 🧩 A. Tabungan dengan Informasi Lengkap

Setiap tabungan memiliki:

* Nama target
* Target nominal
* **Nominal terkumpul (auto update)**
* **Sisa nominal (auto hitung)**
* Deadline (opsional)
* Deskripsi

📊 Tampilan:

* Progress bar (%)
* 💰 Terkumpul: Rp 3.000.000
* 🎯 Target: Rp 10.000.000
* ❗ Sisa: Rp 7.000.000

👉 **Rumus:**

* Sisa = Target - Terkumpul

---

### ➕ B. Tambah Tabungan

Input:

* Nominal
* Deskripsi
* Tanggal

Efek:

* Menambah “nominal terkumpul”
* Update sisa otomatis

---

### ➖ C. Kurangi Tabungan

Input:

* Nominal
* Deskripsi
* Tanggal

Efek:

* Mengurangi “nominal terkumpul”
* Sisa bertambah

---

### 🧾 D. Riwayat Transaksi

* List semua transaksi
* Label:

    * ➕ Masuk
    * ➖ Keluar
* Bisa filter (tanggal / jenis)

---

## 🔔 3. Notifikasi (Improved & Flexible)

### 🔄 Jenis Notifikasi

User bisa pilih saat membuat tabungan:

* 📅 **Harian**
* 📆 **Mingguan**
* 🗓️ **Bulanan**

---

### ⚙️ Pengaturan Notifikasi

Saat buat tabungan:

* Frekuensi notifikasi:

    * Harian / Mingguan / Bulanan
* Waktu notifikasi (misal jam 19:00)

---

### 🔔 Contoh Notifikasi

**Harian:**

> “Yuk nabung untuk ‘Beli Laptop’. Sisa Rp 7.000.000 lagi 💪”

**Mingguan:**

> “Sudah minggu ini nabung belum? Target ‘Liburan Bali’ masih kurang Rp 2.500.000”

**Bulanan:**

> “Update tabungan bulan ini! Kamu sudah kumpulkan Rp 3.000.000 dari Rp 10.000.000”

**Deadline Mendekat:**

> “⚠️ Target ‘Motor Baru’ tinggal 5 hari lagi. Sisa Rp 4.000.000”

**Target Tercapai:**

> “🎉 Selamat! Target ‘iPhone’ berhasil tercapai!”

---

## 📊 4. UI/UX (Updated Detail)

### 🏠 Home Card (Lebih Informatif)

Setiap card tabungan:

```
[Beli Laptop]
Progress: █████░░░░ 30%

Terkumpul: Rp 3.000.000
Sisa: Rp 7.000.000

[+ Tambah]   [- Kurangi]
```

---

### 📄 Detail Tabungan

Lebih lengkap:

* Progress besar
* Target vs terkumpul
* **Highlight sisa (warna merah/orange biar kelihatan penting)**
* Riwayat transaksi

---

### 🎨 UX Improvement

* Warna:

    * Hijau = progres bagus
    * Orange = masih jauh
    * Merah = deadline dekat
* Animasi saat:

    * Tambah saldo
    * Target tercapai

---

## 🏗️ 5. Struktur Data (Updated)

### Tabel: `tabungan`

Tambahan field:

* id
* nama
* target_nominal
* current_nominal
* **sisa_nominal (optional, bisa dihitung langsung)**
* deadline
* deskripsi
* **notif_type (daily / weekly / monthly)**
* **notif_time (jam notifikasi)**
* created_at

---

### Tabel: `transaksi`

Tetap:

* id
* tabungan_id
* tipe (masuk / keluar)
* nominal
* deskripsi
* tanggal

---

## ⚡ 6. Logic Penting (Core System)

### 🧮 Perhitungan Otomatis

Setiap transaksi:

* Jika masuk → tambah current_nominal
* Jika keluar → kurangi current_nominal

Lalu:

* sisa = target - current

---

### 🔔 Scheduler Notifikasi

* Gunakan:

    * Cron job (backend) atau
    * Firebase scheduler (mobile)

Trigger:

* Berdasarkan notif_type user

---

## 🚀 7. Value yang Makin Kuat

Dengan update ini:

* User selalu tahu:

    * Sudah kumpul berapa
    * Kurang berapa
* Notifikasi jadi:

    * Lebih personal
    * Lebih konsisten
* UX jadi lebih “nagih” (habit building)

---

Kalau kamu mau lanjut, aku bisa bantu:

* bikin **flow diagram (alur user dari awal sampai transaksi)**
* atau **API endpoint Laravel (best practice sesuai gaya kamu)**
* atau langsung **desain database + migration** 👍
