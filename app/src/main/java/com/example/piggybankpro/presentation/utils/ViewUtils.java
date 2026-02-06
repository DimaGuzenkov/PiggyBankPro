package com.example.piggybankpro.presentation.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.piggybankpro.R;
import com.example.piggybankpro.presentation.views.costomViews.RingProgressBar;

import java.util.Locale;

public class ViewUtils {
    public static int progressLow = Color.parseColor("#F44336");
    public static int progressMedium = Color.parseColor("#FF9800");
    public static int progressHigh = Color.parseColor("#4CAF50");

    public static void updateGoalProgress(ProgressBar progressBar, TextView textViewProgressPercentage, double progress) {
        textViewProgressPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", progress));
        progressBar.setProgress((int) Math.min(progress, 100));

        var color = progressHigh;
        if (progress < 30) {
            color = progressLow;
        } else if (progress < 70) {
            color = progressMedium;
        }

        progressBar.setProgressTintList(ColorStateList.valueOf(color));
        progressBar.setVisibility(View.VISIBLE);
        textViewProgressPercentage.setVisibility(View.VISIBLE);
    }
}
