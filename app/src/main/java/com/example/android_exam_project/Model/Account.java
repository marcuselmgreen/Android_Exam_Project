package com.example.android_exam_project.Model;

import android.os.Parcel;
import android.os.Parcelable;

//To transfer this objects properties we need to serialize it with the Parcelable interface
//To use parcelable we need to implement the necessary methods and override them
//The methods added are the ones needed to do the work of constructing and deconstruction an object
//in activities
public class Account implements Parcelable {
    private String id;
    private double balance;
    private String type;

    public Account(String id, double balance, String type) {
        this.id = id;
        this.balance = balance;
        this.type = type;
    }

    public Account(String type, double balance) {
        this.type = type;
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
        return type.toUpperCase() +
                ": " + balance + " DKK";
    }

    //We need to write and restore instances of this class to and from a Parcel
    //This is the constructor called on the receiving activity
    protected Account(Parcel in) {
        id = in.readString();
        balance = in.readDouble();
        type = in.readString();
    }

    //The creator method binds everything together
    public static final Creator<Account> CREATOR = new Creator<Account>() {
        //This method takes the parcel and return the new object
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

    //With this method we can write all the properties needed to transfer
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(balance);
        dest.writeString(type);
    }
}
