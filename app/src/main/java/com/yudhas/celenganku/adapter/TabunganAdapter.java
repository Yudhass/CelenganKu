package com.yudhas.celenganku.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yudhas.celenganku.R;
import com.yudhas.celenganku.database.entity.Tabungan;
import com.yudhas.celenganku.databinding.ItemTabunganBinding;
import com.yudhas.celenganku.util.CurrencyHelper;
import com.yudhas.celenganku.util.DateHelper;

import java.util.ArrayList;
import java.util.List;

public class TabunganAdapter extends RecyclerView.Adapter<TabunganAdapter.ViewHolder> {

    public interface OnTabunganListener {
        void onTambah(Tabungan tabungan);
        void onKurangi(Tabungan tabungan);
        void onDetail(Tabungan tabungan);
    }

    private List<Tabungan> list = new ArrayList<>();
    private final OnTabunganListener listener;

    public TabunganAdapter(OnTabunganListener listener) {
        this.listener = listener;
    }

    public void setData(List<Tabungan> data) {
        this.list = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTabunganBinding binding = ItemTabunganBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTabunganBinding b;

        ViewHolder(ItemTabunganBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Tabungan tabungan) {
            Context ctx = b.getRoot().getContext();
            int progress = tabungan.getProgressPercent();

            b.tvNama.setText(tabungan.getNama());
            b.tvTerkumpul.setText(CurrencyHelper.formatRupiah(tabungan.getCurrentNominal()));
            b.tvTarget.setText(CurrencyHelper.formatRupiah(tabungan.getTargetNominal()));
            b.progressBar.setProgress(progress);

            // Status badge
            if (tabungan.isCompleted()) {
                b.tvStatus.setText("✅ Selesai");
                b.tvStatus.setBackgroundColor(Color.parseColor("#2E7D32"));
                b.tvSisa.setText("Rp 0");
                b.tvSisa.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                b.tvStatus.setText(progress + "%");
                // Color based on progress
                if (progress >= 75) {
                    b.tvStatus.setBackgroundColor(Color.parseColor("#2E7D32"));
                } else if (progress >= 40) {
                    b.tvStatus.setBackgroundColor(Color.parseColor("#F57C00"));
                } else {
                    b.tvStatus.setBackgroundColor(Color.parseColor("#C62828"));
                }
                b.tvSisa.setText(CurrencyHelper.formatRupiah(tabungan.getSisaNominal()));

                // Sisa color based on progress
                if (progress >= 75) {
                    b.tvSisa.setTextColor(Color.parseColor("#2E7D32"));
                } else if (progress >= 40) {
                    b.tvSisa.setTextColor(Color.parseColor("#F57C00"));
                } else {
                    b.tvSisa.setTextColor(Color.parseColor("#C62828"));
                }
            }

            // Progress bar color
            if (progress >= 75) {
                b.progressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
            } else if (progress >= 40) {
                b.progressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#F57C00")));
            } else {
                b.progressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#C62828")));
            }

            // Deadline
            if (tabungan.getDeadline() != null && !tabungan.getDeadline().isEmpty()) {
                b.tvDeadline.setVisibility(View.VISIBLE);
                long daysLeft = DateHelper.getDaysRemaining(tabungan.getDeadline());
                if (daysLeft < 0) {
                    b.tvDeadline.setText("📅 Deadline: " + tabungan.getDeadline() + " (Terlewat)");
                    b.tvDeadline.setTextColor(Color.parseColor("#C62828"));
                } else if (daysLeft == 0) {
                    b.tvDeadline.setText("⚠️ Deadline: " + tabungan.getDeadline() + " (Hari ini!)");
                    b.tvDeadline.setTextColor(Color.parseColor("#C62828"));
                } else if (daysLeft <= 7) {
                    String sisaWaktu = DateHelper.formatSisaWaktu(daysLeft);
                    b.tvDeadline.setText("⚠️ Deadline: " + tabungan.getDeadline() + " (" + sisaWaktu + ")");
                    b.tvDeadline.setTextColor(Color.parseColor("#C62828"));
                } else {
                    String sisaWaktu = DateHelper.formatSisaWaktu(daysLeft);
                    b.tvDeadline.setText("📅 Deadline: " + tabungan.getDeadline() + " (" + sisaWaktu + ")");
                    b.tvDeadline.setTextColor(Color.parseColor("#757575"));
                }
            } else {
                b.tvDeadline.setVisibility(View.GONE);
            }

            // Disable buttons if completed
            b.btnTambah.setEnabled(!tabungan.isCompleted());
            b.btnKurangi.setEnabled(tabungan.getCurrentNominal() > 0);

            b.btnTambah.setOnClickListener(v -> listener.onTambah(tabungan));
            b.btnKurangi.setOnClickListener(v -> listener.onKurangi(tabungan));
            b.btnDetail.setOnClickListener(v -> listener.onDetail(tabungan));
            b.cardTabungan.setOnClickListener(v -> listener.onDetail(tabungan));
        }
    }
}

