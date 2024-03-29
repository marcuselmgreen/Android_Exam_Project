package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.R;
import com.example.android_exam_project.Service.transferService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class accountSettingsActivity extends AppCompatActivity {

    Button confirm, back, payment_service;
    TextView id, balance, type, amount, budget_txt;
    String key, TAG = "TAG";
    ArrayList accountList = new ArrayList<>();
    Account account;
    Spinner deposit_account;
    DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        init();

        //Load saved instance
        if (savedInstanceState != null) {
            id.setText(savedInstanceState.getString("id"));
            balance.setText(savedInstanceState.getString("balance"));
            type.setText(savedInstanceState.getString("type"));
            accountList = savedInstanceState.getParcelableArrayList("accountList");
            Log.d(TAG, "onCreate: " + accountList);
        }

        //Checking if account is either type default or business, other accounts should not be able
        //to make monthly deposits. Other accounts are only able to view information.
        if (account.getType().equals("default") || account.getType().equals("business")){
            confirm.setVisibility(View.VISIBLE);
            budget_txt.setVisibility(View.VISIBLE);
            amount.setVisibility(View.VISIBLE);
            deposit_account.setVisibility(View.VISIBLE);

            //Set spinner with budget or savings account from accountList
            for (int i = 0; i < accountList.size(); i++) {
                if (!((Account) accountList.get(i)).getType().equals("budget") || !((Account) accountList.get(i)).getType().equals("savings")){
                    accountList.remove(i);
                }
            }
            ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.spinner_item_row, accountList);
            deposit_account.setAdapter(adapter);
        }
    }

    private void init() {
        confirm = findViewById(R.id.confirm);
        back = findViewById(R.id.back);
        id = findViewById(R.id.account_id);
        balance = findViewById(R.id.account_balance);
        type = findViewById(R.id.account_type);
        amount = findViewById(R.id.budget_amount);
        budget_txt = findViewById(R.id.budget_deposits);
        payment_service = findViewById(R.id.payment_service);
        deposit_account = findViewById(R.id.deposit_account);
        db = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        accountList = intent.getParcelableArrayListExtra("Accounts");
        key = intent.getStringExtra("Key");
        account = intent.getParcelableExtra("Account");

        String accountNumber = "ACCOUNT NUMBER: " + account.getId().toUpperCase();
        id.setText(accountNumber);
        String balanceTxt = account.getBalance() + " DKK";
        balance.setText(balanceTxt);
        String accountType = account.getType().toUpperCase() + " ACCOUNT";
        type.setText(accountType);
        Log.d(TAG, "init: " + account.getId());
    }
    public void back(View v){
        finish();
    }

    public void confirm(View v){
        //Checking valid amount
        if (amount.length() > 0){
            //Get date of next deposit
            Account accountToDeposit = (Account) deposit_account.getSelectedItem();
            Double amountToDeposit = Double.valueOf(amount.getText().toString());
            Calendar today = Calendar.getInstance();
            //We need the next month from today and since Calendar.MONTH returns the current month minus 1 we add 2
            today.set(Calendar.MONTH, today.get(Calendar.MONTH) + 2);
            String nextMonth = "1:" + today.get(Calendar.MONTH) + ":" + today.get(Calendar.YEAR);
            Log.d(TAG, "Date: " + nextMonth + ", Amount: " + amountToDeposit);
            //Save to database using the transferService class
            transferService.monthlyDeposit(accountToDeposit.getId(), account.getId(), nextMonth, amountToDeposit, key);
            Toast toast = Toast.makeText(accountSettingsActivity.this, "Monthly deposit of " + amountToDeposit + " DKK has been registered to your " + accountToDeposit.getType() + " account!", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        } else {
            Toast toast = Toast.makeText(accountSettingsActivity.this, "Please insert a monthly deposit amount", Toast.LENGTH_SHORT);
            toast.show();
            amount.setText("");
            amount.requestFocus();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("id", id.getText().toString());
        outState.putString("balance", balance.getText().toString());
        outState.putString("type", type.getText().toString());
        outState.putParcelableArrayList("accountList", accountList);
    }
}
