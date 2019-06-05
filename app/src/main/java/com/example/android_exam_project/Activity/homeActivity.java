package com.example.android_exam_project.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.R;
import com.example.android_exam_project.Service.transferService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class homeActivity extends AppCompatActivity {

    private ArrayList<Account> accountList = new ArrayList<>();

    Button settings;
    String key, TAG = "TAG";
    ListView accounts;
    DatabaseReference db;
    ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        loadAccounts();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        loadAccounts();
    }

    private void init() {
        settings = findViewById(R.id.settings);
        accounts = findViewById(R.id.accounts);
        Intent intent = getIntent();
        key = intent.getStringExtra("Key");
        Log.d(TAG, "init: " + key);

        db = FirebaseDatabase.getInstance().getReference("users");

        checkMonthlyDeposits();

        //Go to account settings activity when selecting an item in listView
        accounts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(homeActivity.this, accountSettingsActivity.class);
                intent.putExtra("Account", accountList.get(position));
                intent.putExtra("Accounts", accountList);
                intent.putExtra("Key", key);
                startActivity(intent);
            }
        });
    }

    //Load accounts from database and insert them into a listView
    private void loadAccounts() {
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 int count = 0;
                 for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                     //Check if values are available in database and create an account with these values
                     if (snapshot.child("balance").getValue() != null) {
                         String id = snapshot.child("id").getRef().getParent().getKey();
                         Double balance = Double.valueOf(snapshot.child("balance").getValue(Long.class));
                         String type = String.valueOf(snapshot.child("id").getValue());
                         Account account = new Account(id, balance, type);
                         //If account does not exist in accountList, then add and show in listView
                         if (!accountList.contains(account) && accountList.size() == count) {
                             Log.d(TAG, "onDataChange: " + id + ", balance: " + snapshot.child("balance").getValue());
                             accountList.add(account);
                             //Might not work when adding more accounts
                             arrayAdapter = new ArrayAdapter(homeActivity.this, android.R.layout.simple_list_item_1, accountList);
                             accounts.setAdapter(arrayAdapter);
                         //Update accountList and listView if an account has changed
                         }else {
                             accountList.set(count, account);
                             arrayAdapter.notifyDataSetChanged();
                         }
                         count += 1;
                     } else {
                         break;
                     }
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
        });
    }

    //Check monthly deposits in database and send the money if needed
    public void checkMonthlyDeposits(){
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Calendar today = Calendar.getInstance();
                //Test if method works
                //today.set(Calendar.MONTH, today.get(Calendar.MONTH) + 8);
                //Log.d(TAG, "date today: " + today);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("monthly_deposit")){
                            //Get each date registered for monthly deposit from database and convert to Calendar format
                            Iterable<DataSnapshot> monthlyDepositChildren = snapshot.child("monthly_deposit").getChildren();
                            for (DataSnapshot children : monthlyDepositChildren){
                                String date = children.getKey();
                                String[] dateSplit = children.getKey().split(":");
                                Log.d(TAG, "Deposit date: " + date);
                                int day = Integer.valueOf(dateSplit[0]);
                                int month = Integer.valueOf(dateSplit[1]);
                                int year = Integer.valueOf(dateSplit[2]);
                                Calendar depositDate = Calendar.getInstance();
                                depositDate.set(year, month, day);
                                if (depositDate.before(today)){

                                    //Update with new date
                                    String idReceiver = children.child("account").getValue(String.class);
                                    String idSender = snapshot.getKey();
                                    Context context = homeActivity.this;
                                    String nextMonth = "1:" + (today.get(Calendar.MONTH)+1) + ":" + today.get(Calendar.YEAR);
                                    Double amountToSend = children.child("amount").getValue(Double.class);

                                    //Create new child with the date
                                    transferService.monthlyDeposit(idReceiver, idSender, nextMonth, amountToSend, key);

                                    //Calculate amount of months worth of deposits
                                    amountToSend += amountToSend * (today.get(Calendar.MONTH) - month);
                                    //Make deposit
                                    transferService.transaction(idSender, idReceiver, amountToSend, context, key, "monthly_deposit");

                                    //Remove children
                                    db.child(key).child(idSender).child("monthly_deposit").child(date).removeValue();
                                }else {

                                    break;
                                }
                            }
                        }else {
                            Log.d(TAG, "No monthly deposit requests created");
                        }
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Go to user settings activity
    public void showSettings(View v) {
        Intent intent = new Intent(this, settingsActivity.class);
        intent.putExtra("Key", key);
        startActivity(intent);
    }
    //Go to transaction activity and expect result from intent
    public void showTransaction(View v) {
        Intent intent = new Intent(this, transactionActivity.class);
        intent.putExtra("Accounts", accountList);
        intent.putExtra("Key", key);
        startActivity(intent);
    }

    public void logout(View v) {
        finish();
    }
}
