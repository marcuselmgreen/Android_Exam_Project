package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

        arrayAdapter = new ArrayAdapter(homeActivity.this, android.R.layout.simple_list_item_1, accountList);
        accounts.setAdapter(arrayAdapter);

        db = FirebaseDatabase.getInstance().getReference("users");

        loadAccounts();
    }

    private void loadAccounts() {
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 int count = 0;
                 for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                     if (snapshot.child("balance").getValue() != null) {
                         String id = snapshot.child("id").getRef().getParent().getKey();
                         Double balance = Double.valueOf(snapshot.child("balance").getValue(Long.class));
                         Account account = new Account(id, balance);
                         if (!accountList.contains(account) && accountList.size() == count) {
                             Log.d(TAG, "onDataChange: " + id + ", balance: " + snapshot.child("balance").getValue());
                             accountList.add(account);
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

    public void showSettings(View v) {
        Intent intent = new Intent(this, settingsActivity.class);
        intent.putExtra("Key", key);
        startActivity(intent);
    }

    public void showTransaction(View v) {
        Intent intent = new Intent(this, transactionActivity.class);
        intent.putExtra("Accounts", accountList);
        intent.putExtra("Key", key);
        startActivityForResult(intent,1);
    }

    public void logout(View v) {
        finish();
    }

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
}
