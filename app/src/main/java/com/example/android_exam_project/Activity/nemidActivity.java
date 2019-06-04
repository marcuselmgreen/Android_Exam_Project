package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_exam_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class nemidActivity extends AppCompatActivity {

    TextView cpr, password, key, keycardnumber;
    Button back, submit;
    String accountTo, accountNumber, receiverKey, fromKey, TAG = "TAG";
    Double accountFromBalance, receiverBalance, amountToSend;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nemid);

        init();
    }

    private void init() {
        cpr = findViewById(R.id.cpr_input);
        password = findViewById(R.id.password_input);
        key = findViewById(R.id.keycard_input);
        keycardnumber = findViewById(R.id.keycard_txt);
        back = findViewById(R.id.back);
        submit = findViewById(R.id.submit);
        db = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        accountNumber = intent.getStringExtra("AccountNumber");
        accountTo = intent.getStringExtra("AccountTo");
        amountToSend = intent.getDoubleExtra("AmountToSend", 0);
        accountFromBalance = intent.getDoubleExtra("AccountFromBalance", 0);
        receiverBalance = intent.getDoubleExtra("ReceiverBalance", 0);
        receiverKey = intent.getStringExtra("ReceiverKey");
        fromKey = intent.getStringExtra("FromKey");


        String number = String.valueOf(Math.round(Math.random() * (9999 - 1000) + 1000));
        keycardnumber.setText(number);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.8), (int) (height*.6));
    }

    public void submit(View v){
        db.child(fromKey).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userCpr = cpr.getText().toString();
                String userPassword = password.getText().toString();
                if (dataSnapshot.child("cpr").getValue(String.class).equals(userCpr) && dataSnapshot.child("password").getValue(String.class).equals(userPassword)) {
                    if (Integer.parseInt(key.getText().toString()) == 123456) {
                        //Retract amount from sender
                        db.child(fromKey).child(accountNumber).child("balance").setValue(accountFromBalance - amountToSend);

                        //Add amount to receiver
                        Log.d(TAG, "Receiver balance: " + receiverBalance);
                        db.child(receiverKey).child(accountTo).child("balance").getRef().setValue(receiverBalance + amountToSend);
                        Toast toast = Toast.makeText(nemidActivity.this, amountToSend + " DKK was transferred", Toast.LENGTH_SHORT);
                        toast.show();

                        finish();
                    } else {
                        Toast toast = Toast.makeText(nemidActivity.this, "Wrong key!", Toast.LENGTH_SHORT);
                        toast.show();
                        key.setText("");
                        key.requestFocus();
                    }
                } else {
                    Toast toast = Toast.makeText(nemidActivity.this, "Wrong cpr or password! Try again", Toast.LENGTH_SHORT);
                    toast.show();
                    cpr.setText("");
                    password.setText("");
                    cpr.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void back(View v){
        finish();
        }
}