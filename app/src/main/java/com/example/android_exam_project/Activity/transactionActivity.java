package com.example.android_exam_project.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.R;
import com.example.android_exam_project.Service.transferService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class transactionActivity extends AppCompatActivity {

    TextView amount, toAccountAcc, toAccountReg;
    Spinner fromAccount;
    DatabaseReference db;
    String key, TAG = "TAG";
    ArrayList<Account> accountList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        init();
    }

    private void init() {
        amount = findViewById(R.id.amount);
        fromAccount = findViewById(R.id.fromAccount);
        toAccountAcc = findViewById(R.id.toAccountAcc);
        toAccountReg = findViewById(R.id.toAccountReg);
        db = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        accountList = intent.getParcelableArrayListExtra("Accounts");
        key = intent.getStringExtra("Key");

        arrayAdapter = new ArrayAdapter<>(transactionActivity.this, android.R.layout.simple_list_item_1, accountList);
        fromAccount.setAdapter(arrayAdapter);
    }

    public void send(View v) {
        //Get sender account + receiver account + transaction amount
        Account accountFromSpinner = (Account) fromAccount.getSelectedItem();
        //Receiver id
        String idReceiver = toAccountReg.getText().toString() + " " + toAccountAcc.getText().toString();
        //Amount to send
        Double amountToSend = Double.valueOf(amount.getText().toString());
        //Sender id
        String idSender = accountFromSpinner.getId();
        //Context
        Context context = transactionActivity.this;

        transferService.transaction(idSender, idReceiver, amountToSend, context, key, "normal_transfer");
    }

    public void back(View v) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

}
