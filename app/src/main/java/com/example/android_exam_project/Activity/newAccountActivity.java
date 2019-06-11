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

        //Load saved instance
        if (savedInstanceState != null) {
            description.setText(savedInstanceState.getString("description"));
            availableAccountList = savedInstanceState.getStringArrayList("availableAccountList");
        }
    }

    private void init() {
        apply = findViewById(R.id.apply);
        back = findViewById(R.id.back);
        accounts = findViewById(R.id.accounts);
        description = findViewById(R.id.account_description);

        //Set description text as account overview
        description.setText(getString(R.string.account_descriptions));

        Intent intent = getIntent();
        key = intent.getStringExtra("Key");

        db = FirebaseDatabase.getInstance().getReference("users");

        loadAvailableAccounts();
    }

    private void loadAvailableAccounts() {
        availableAccountList.add("business".toUpperCase());
        availableAccountList.add("savings".toUpperCase());
        availableAccountList.add("pension".toUpperCase());
        availableAccountList.add("budget".toUpperCase());
        availableAccountList.add("default".toUpperCase());

        arrayAdapter = new ArrayAdapter<>(newAccountActivity.this, R.layout.spinner_item_row, availableAccountList);
        accounts.setAdapter(arrayAdapter);
    }

    public void apply(View v) {
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String type = accounts.getSelectedItem().toString();
                Double balance = 0.0;
                Account account = new Account(type, balance);

                //Registration number is the same for all the accounts in the bank
                long regNumber = 2055;
                //Account id is the same for every account under a single user, so we need to
                //retrieve that from the database
                long accountId = dataSnapshot.child("accountId").getValue(Long.class);

                //Account number is unique for every account under a single user
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
                //Send new account to the database
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("description", description.getText().toString());
        outState.putStringArrayList("availableAccountList", availableAccountList);
    }
}
