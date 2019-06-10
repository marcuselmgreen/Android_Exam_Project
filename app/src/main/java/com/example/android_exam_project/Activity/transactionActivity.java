package com.example.android_exam_project.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class transactionActivity extends AppCompatActivity {

    TextView amount, toAccountAcc, toAccountReg;
    Spinner fromAccount;
    DatabaseReference db;
    String key, TAG = "TAG";
    ArrayList<Account> accountList = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    long age;

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

        checkPension();

        arrayAdapter = new ArrayAdapter<>(transactionActivity.this, R.layout.spinner_item_row, accountList);
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

    //Check age of user, if old enough, they get access to withdraw from pension account
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void checkPension() {
        //Check age and remove pension account from spinner
        for (final Account account : accountList) {
            if (account.getType().equals("pension")) {
                db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String dob = dataSnapshot.child("dateOfBirth").getValue(String.class);
                        String[] dobSplit = dob.split("/");
                        LocalDate from = LocalDate.of(Integer.valueOf(dobSplit[2]), Integer.valueOf(dobSplit[1]), Integer.valueOf(dobSplit[0]));
                        LocalDate today = LocalDate.now();
                        age = ChronoUnit.YEARS.between(from, today);
                        if (age < 77){
                            accountList.remove(account);
                            Log.d(TAG, "Pension account removed!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
    }
}
