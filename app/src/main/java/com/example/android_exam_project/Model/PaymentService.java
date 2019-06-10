package com.example.android_exam_project.Model;

import android.os.Parcelable;

public class PaymentService {
    private String date;
    private double amount;
    private String account;
    private String description;
    private String autopay;
    private String idSender;

    public PaymentService(String date, double amount, String account, String description, String autopay, String idSender) {
        this.date = date;
        this.amount = amount;
        this.account = account;
        this.description = description;
        this.autopay = autopay;
        this.idSender = idSender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAutopay() {
        return autopay;
    }

    public void setAutopay(String autopay) {
        this.autopay = autopay;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    @Override
    public String toString() {
        return description.toUpperCase() + " DUE: " + date + " " + amount + " DKK " + " AUTOPAYMENT: " + autopay.toUpperCase();
    }
}
