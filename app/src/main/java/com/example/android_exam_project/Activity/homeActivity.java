package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.R;
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
    }

    private void init() {
        settings = findViewById(R.id.settings);
        accounts = findViewById(R.id.accounts);
        Intent intent = getIntent();
        key = intent.getStringExtra("Key");
        Log.d(TAG, "init: " + key);

        db = FirebaseDatabase.getInstance().getReference("users");

        loadAccounts();
        //checkMonthlyDeposits();

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
        startActivityForResult(intent,1);
    }

    public void logout(View v) {
        finish();
    }

    //Update accounts when a transaction is made
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {

            if(resultCode == RESULT_OK){
                loadAccounts();
            }
            if (resultCode == RESULT_CANCELED) {
                //Do nothing?
            }
        }
    }

    //Check monthly deposits in database and send the money if needed
    public void checkMonthlyDeposits(){
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Calendar today = Calendar.getInstance();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("monthly_deposit")){
                            //Get date from database and convert to Calendar format
                            String date = snapshot.child("budget").child("date").getValue(String.class);
                            Log.d(TAG, "Deposit date: " + date);
                            int day = Integer.valueOf(date.substring(1));
                            int month = Integer.valueOf(date.substring(4));
                            int year = Integer.valueOf(date.substring(5,8));
                            Calendar depositDate = Calendar.getInstance();
                            depositDate.set(year, month, day);
                            if (depositDate.before(today)){
                                Log.d(TAG, "Before today");;
                            }else {
                                Log.d(TAG, "After today");
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
}
