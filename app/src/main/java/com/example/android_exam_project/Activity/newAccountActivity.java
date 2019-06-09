package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class newAccountActivity extends AppCompatActivity {

    Button apply, back;
    Spinner accounts;
    TextView description;
    String key, TAG = "TAG";
    DatabaseReference db;
    ArrayAdapter arrayAdapter;

    private ArrayList<String> availableAccountList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        init();
    }

    private void init() {
        apply = findViewById(R.id.apply);
        back = findViewById(R.id.back);
        accounts = findViewById(R.id.accounts);
        description = findViewById(R.id.account_description);

        Intent intent = getIntent();
        key = intent.getStringExtra("Key");

        db = FirebaseDatabase.getInstance().getReference("users");

        loadAvailableAccounts();
    }

    private void loadAvailableAccounts() {
        availableAccountList.add("business");
        availableAccountList.add("savings");
        availableAccountList.add("pension");
        availableAccountList.add("budget");
        availableAccountList.add("default");

        arrayAdapter = new ArrayAdapter<>(newAccountActivity.this, android.R.layout.simple_list_item_1, availableAccountList);
        accounts.setAdapter(arrayAdapter);
    }

    public void apply(View v) {
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String type = accounts.getSelectedItem().toString();
                Double balance = 0.0;
                Account account = new Account(type, balance);

                //Create unique account id
                long regNumber = 2055;
                long accountId = dataSnapshot.child("accountId").getValue(Long.class);

                long count = 1;
                //Increment the last account number
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String lastAccount = snapshot.getKey();
                    if (lastAccount.startsWith("2055")) {
                        if (Long.valueOf(lastAccount.substring(13, 14)) > count){
                            count++;
                        }else {
                            break;
                        }
                    }
                }
                long accountNumber = count;
                //Increment last account instead
                db.child(key).child(regNumber + " " + regNumber + accountId + accountNumber).setValue(account);
                Toast toast = Toast.makeText(newAccountActivity.this, "You have received a new account", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void back(View v) {
        finish();
    }
}
