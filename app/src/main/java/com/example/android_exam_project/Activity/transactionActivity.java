package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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

public class transactionActivity extends AppCompatActivity {

    TextView amount, toAccountAcc, toAccountReg;
    Spinner fromAccount;
    DatabaseReference db;
    String key, TAG = "TAG";
    Double receiverBalance;
    ArrayList<Account> accountList = new ArrayList<>();

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
        Log.d(TAG, "init: " + accountList.toString());

        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accountList);
        fromAccount.setAdapter(adapter);
    }

    public void send(View v) {
        //Get sender account + receiver account + transaction amount
        final Account accountFrom = (Account) fromAccount.getSelectedItem();
        final String accountTo = toAccountReg.getText().toString() + " " + toAccountAcc.getText().toString();
        final Double amountToSend = Double.valueOf(amount.getText().toString());
        final String accountNumber = accountFrom.getId();
        final Double senderBalance = accountFrom.getBalance();

        //Checking if receiver account is bound to this user
        final String senderAccount = accountNumber.substring(9, 13);
        final String receiverAccount = accountTo.substring(9, 13);
        Log.d(TAG, "sender: " + senderAccount + "receiver: " + receiverAccount);
        if (!accountNumber.equals(accountTo)){
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild(accountTo) && senderAccount.equals(receiverAccount)) {
                            //Retract amount from sender
                            //senderBalance = snapshot.child(accountNumber).child("balance").getValue(Double.class);
                            db.child(key).child(accountNumber).child("balance").setValue(senderBalance - amountToSend);
                            String accountId = snapshot.child(accountTo).child("id").getValue(String.class);

                            //Add amount to receiver
                            receiverBalance = snapshot.child(accountTo).child("balance").getValue(Double.class);
                            snapshot.child(accountTo).child("balance").getRef().setValue(receiverBalance + amountToSend);

                            Toast toast = Toast.makeText(transactionActivity.this, amountToSend + " DKK was sent to your " + accountId + " account", Toast.LENGTH_SHORT);
                            toast.show();
                            finish();
                            break;
                        }
                        if (snapshot.hasChild(accountTo) && !senderAccount.equals(receiverAccount)) {
                            //nemId popup
                            Log.d(TAG, "Sender and receiver are not accounts under the same user");
                            String receiverKey = snapshot.getKey();
                            Intent intent = new Intent(transactionActivity.this, nemidActivity.class);
                            intent.putExtra("AccountTo", accountTo);
                            intent.putExtra("AmountToSend", amountToSend);
                            intent.putExtra("AccountNumber", accountNumber);
                            intent.putExtra("AccountFromBalance", senderBalance);
                            intent.putExtra("ReceiverBalance", receiverBalance);
                            intent.putExtra("ReceiverKey", receiverKey);
                            intent.putExtra("FromKey", key);
                            startActivity(intent);
                            finish();
                            break;
                        }
                        Toast toast = Toast.makeText(transactionActivity.this, "Account does not exist! Try again", Toast.LENGTH_SHORT);
                        toast.show();
                        toAccountAcc.setText("");
                        toAccountReg.setText("");
                        toAccountReg.requestFocus();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        if (amountToSend > senderBalance){
            Toast toast = Toast.makeText(transactionActivity.this, "You don't have enough money on this account to make the transaction", Toast.LENGTH_SHORT);
            toast.show();
            amount.setText("");
            amount.requestFocus();
        }else {
            Toast toast = Toast.makeText(transactionActivity.this, "Please enter a valid account to receive the money..", Toast.LENGTH_SHORT);
            toast.show();
            toAccountAcc.setText("");
            toAccountReg.setText("");
            toAccountReg.requestFocus();
        }
    }

    public void back(View v) {
        finish();
    }

}
