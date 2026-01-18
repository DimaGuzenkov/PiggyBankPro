package com.example.piggybankpro.presentation.utils;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ToastUtils {
    public static void withdrawAmount(AppCompatActivity context) {
        display(context, "Средства списаны");
    }

    public static void depositAmount(AppCompatActivity context) {
        display(context, "Цель пополнена");
    }

    public static void goalDeleted(AppCompatActivity context) {
        display(context, "Цель удалена");
    }

    public static void goalCreated(AppCompatActivity context) {
        display(context, "Цель создана");
    }

    public static void goalUpdated(AppCompatActivity context) {
        display(context, "Цель обновлена");
    }

    public static void goalNotFound(AppCompatActivity context) {
        display(context, "Ошибка: цель не найдена");
    }

    public static void autoDepositCreated(AppCompatActivity context) {
        display(context, "Автопополнение создано");
    }

    public static void autoDepositUpdated(AppCompatActivity context) {
        display(context, "Автопополнение обновлено");
    }

    public static void autoDepositExecuted(AppCompatActivity context) {
        display(context, "Автопополнение выполнено");
    }

    public static void autoDepositDeleted(AppCompatActivity context) {

    }

    public static void display(AppCompatActivity context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
