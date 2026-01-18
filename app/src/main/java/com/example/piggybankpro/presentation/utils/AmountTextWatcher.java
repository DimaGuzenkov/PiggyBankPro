package com.example.piggybankpro.presentation.utils;

import android.os.Build;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.text.ParseException;

public class AmountTextWatcher implements TextWatcher {
    private final EditText editText;
    private String currentText = "";

    public AmountTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.toString().equals(currentText)) {
            return;
        }

        var amount = AmountUtils.amountFromString(editable.toString()).result();
        if (amount == null) {
            return;
        }

        var oldLength = currentText.length();
        var oldPosition = editText.getSelectionEnd() + 1;
        currentText = AmountUtils.formatAmount(amount);
        editText.setText(currentText);
        var newPosition = oldPosition + currentText.length() - oldLength;
        editText.setSelection(Math.max(Math.min(newPosition, currentText.length()), 0));
    }
}
