package com.yudhas.celenganku.util;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputEditText;

public class CurrencyTextWatcher implements TextWatcher {

    private final TextInputEditText editText;
    private boolean isUpdating = false;

    public CurrencyTextWatcher(TextInputEditText editText) {
        this.editText = editText;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable editable) {
        if (isUpdating) return;
        isUpdating = true;

        // Ambil hanya angka dari input
        String raw = editable.toString().replaceAll("[^0-9]", "");

        String formatted = "";
        if (!raw.isEmpty()) {
            // Batasi 13 digit (max 9.999.999.999.999)
            if (raw.length() > 13) raw = raw.substring(0, 13);
            try {
                long value = Long.parseLong(raw);
                formatted = formatWithDots(value);
            } catch (NumberFormatException e) {
                formatted = raw;
            }
        }

        // Set langsung via setText — lebih reliable daripada editable.replace()
        editText.setText(formatted);
        // Pindah cursor ke akhir
        if (editText.getText() != null && !formatted.isEmpty()) {
            try {
                editText.setSelection(editText.getText().length());
            } catch (IndexOutOfBoundsException ignored) {}
        }

        isUpdating = false;
    }

    /** Format angka dengan titik ribuan: 1000000 → "1.000.000" */
    private String formatWithDots(long value) {
        String str = String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) sb.append('.');
            sb.append(str.charAt(i));
        }
        return sb.toString();
    }

    /** Ambil nilai long dari EditText berformat */
    public static long getValue(TextInputEditText et) {
        if (et.getText() == null) return 0;
        String digits = et.getText().toString().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try { return Long.parseLong(digits); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Set nilai ke EditText dengan format titik ribuan */
    public static void setValue(TextInputEditText et, long value) {
        if (value <= 0) { et.setText(""); return; }
        String str = String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) sb.append('.');
            sb.append(str.charAt(i));
        }
        et.setText(sb.toString());
        if (et.getText() != null)
            try { et.setSelection(et.getText().length()); } catch (IndexOutOfBoundsException ignored) {}
    }
}
