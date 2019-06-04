package com.example.android_exam_project.Service;

import com.example.android_exam_project.Model.Account;

import java.util.ArrayList;

public class UserService {
    private static ArrayList<Account> accounts = new ArrayList<>();

    public static void loadAllAccounts(){
        ArrayList<Account> accountList = new ArrayList<>();
        //accountList.add()
        accounts = accountList;
        System.out.println("printing accounts");
    }
}


