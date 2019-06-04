package com.example.android_exam_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Account implements Parcelable {
    private String id;
    private double balance;
    private String type;

    public Account(String id, double balance, String type) {
        this.id = id;
        this.balance = balance;
        this.type = type;
    }

    public Account(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Account{" +
                "balance=" + balance +
                ", type='" + type + '\'' +
                '}';
    }

    protected Account(Parcel in) {
        id = in.readString();
        balance = in.readDouble();
        type = in.readString();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(balance);
        dest.writeString(type);
    }
}
