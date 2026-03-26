package com.yudhas.celenganku.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yudhas.celenganku.R;
import com.yudhas.celenganku.database.entity.Transaksi;
import com.yudhas.celenganku.databinding.ItemTransaksiBinding;
import com.yudhas.celenganku.util.CurrencyHelper;
import com.yudhas.celenganku.util.DateHelper;

import java.util.ArrayList;
import java.util.List;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    public interface OnTransaksiActionListener {
        void onEdit(Transaksi transaksi);
        void onDelete(Transaksi transaksi);
    }

    private List<Transaksi> list = new ArrayList<>();
    private OnTransaksiActionListener listener;

    public void setData(List<Transaksi> data) {
        this.list = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnTransaksiActionListener(OnTransaksiActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransaksiBinding binding = ItemTransaksiBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransaksiBinding b;

        ViewHolder(ItemTransaksiBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Transaksi transaksi, OnTransaksiActionListener listener) {
            boolean isMasuk = "masuk".equals(transaksi.getTipe());

            if (isMasuk) {
                b.tvTipeBadge.setText("➕");
                b.tvTipeBadge.setBackground(b.getRoot().getContext()
                        .getResources().getDrawable(R.drawable.bg_chip_masuk, null));
                b.tvNominal.setText("+" + CurrencyHelper.formatRupiah(transaksi.getNominal()));
                b.tvNominal.setTextColor(Color.parseColor("#1B5E20"));
            } else {
                b.tvTipeBadge.setText("➖");
                b.tvTipeBadge.setBackground(b.getRoot().getContext()
                        .getResources().getDrawable(R.drawable.bg_chip_keluar, null));
                b.tvNominal.setText("-" + CurrencyHelper.formatRupiah(transaksi.getNominal()));
                b.tvNominal.setTextColor(Color.parseColor("#B71C1C"));
            }

            String desc = transaksi.getDeskripsi();
            b.tvDeskripsi.setText((desc != null && !desc.isEmpty()) ? desc
                    : (isMasuk ? "Tambah Tabungan" : "Kurangi Tabungan"));
            b.tvTanggal.setText(DateHelper.formatDate(transaksi.getTanggal()));

            // More button (3-dot menu)
            b.btnMore.setOnClickListener(v -> {
                android.view.ContextThemeWrapper ctx =
                        new android.view.ContextThemeWrapper(v.getContext(), com.yudhas.celenganku.R.style.CompactPopupMenu);
                PopupMenu popup = new PopupMenu(ctx, v);
                popup.inflate(R.menu.menu_transaksi_item);
                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;
                    int id = item.getItemId();
                    if (id == R.id.action_edit_transaksi) {
                        listener.onEdit(transaksi);
                        return true;
                    } else if (id == R.id.action_delete_transaksi) {
                        listener.onDelete(transaksi);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}

