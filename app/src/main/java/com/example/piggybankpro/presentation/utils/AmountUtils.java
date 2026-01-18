package com.example.piggybankpro.presentation.utils;

import android.annotation.SuppressLint;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Optional;

public class AmountUtils {
    @SuppressLint("ConstantLocale")
    public static final Locale CurrentLocale = Locale.getDefault();

    private static final NumberFormat Format = NumberFormat.getInstance(CurrentLocale);

    public static String formatAmount(double amount) {
        return Format.format(amount);
    }

    public static ParseResult amountFromString(String str) {
        if (str == null || str.isEmpty()) {
            return new ParseResult(true, null);
        }

        ParsePosition pos = new ParsePosition(0);
        Number num = Format.parse(str, pos);

        if (num != null && pos.getIndex() == str.length() && pos.getErrorIndex() == -1) {
            return new ParseResult(true, num.doubleValue());
        }

        return new ParseResult(false, null);
    }

    public record ParseResult(boolean parsed, Double result) {}
}
