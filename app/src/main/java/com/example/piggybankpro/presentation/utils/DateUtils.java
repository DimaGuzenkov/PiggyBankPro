package com.example.piggybankpro.presentation.utils;

import androidx.annotation.NonNull;

import com.example.piggybankpro.data.local.entities.AutoDepositEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static final int PERIOD_DAILY = 1;
    public static final int PERIOD_WEEKLY = 2;
    public static final int PERIOD_BIWEEKLY = 3;
    public static final int PERIOD_MONTHLY = 4;
    public static final int PERIOD_QUARTERLY = 5;
    public static final int PERIOD_YEARLY = 6;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public static String formatDate(@NonNull Date date) {
        return "Дата: " + sdf.format(date.getTime());
    }

    public static String formatDays(long days) {
        if (days == 0) {
            return "Сегодня";
        } else if (days == 1) {
            return "1 день";
        } else if (days < 5) {
            return days + " дня";
        } else if (days < 21) {
            return days + " дней";
        } else {
            long weeks = days / 7;
            if (weeks < 5) return weeks + " недели";
            return weeks + " недель";
        }
    }

    public static Date calculateNextDate(Date baseDate, int periodType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);

        switch (periodType) {
            case PERIOD_DAILY:
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;

            case PERIOD_WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;

            case PERIOD_BIWEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 2);
                break;

            case PERIOD_MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;

            case PERIOD_QUARTERLY:
                calendar.add(Calendar.MONTH, 3);
                break;

            case PERIOD_YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
        }

        return calendar.getTime();
    }

    public static int getPeriodPosition(int periodType) {
        switch (periodType) {
            case PERIOD_DAILY: return 0;
            case PERIOD_WEEKLY: return 1;
            case PERIOD_BIWEEKLY: return 2;
            case PERIOD_MONTHLY: return 3;
            case PERIOD_QUARTERLY: return 4;
            case PERIOD_YEARLY: return 5;
            default: return 3;
        }
    }

    public static int getPeriodTypeFromPosition(int position) {
        switch (position) {
            case 0: return PERIOD_DAILY;
            case 1: return PERIOD_WEEKLY;
            case 2: return PERIOD_BIWEEKLY;
            case 4: return PERIOD_QUARTERLY;
            case 5: return PERIOD_YEARLY;
            default: return PERIOD_MONTHLY;
        }
    }

    public static String getPeriodString(int periodType) {
        switch (periodType) {
            case PERIOD_DAILY: return "Ежедневно";
            case PERIOD_WEEKLY: return "Еженедельно";
            case PERIOD_BIWEEKLY: return "Раз в две недели";
            case PERIOD_MONTHLY: return "Ежемесячно";
            case PERIOD_QUARTERLY: return "Ежеквартально";
            case PERIOD_YEARLY: return "Ежегодно";
            default: return "Неизвестно";
        }
    }
}
