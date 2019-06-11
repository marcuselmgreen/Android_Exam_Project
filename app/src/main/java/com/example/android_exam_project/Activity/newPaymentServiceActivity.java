package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.R;
import com.example.android_exam_project.Service.transferService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class newPaymentServiceActivity extends AppCompatActivity {

    Button confirm, back;
    TextView cardType, cardInfo1, cardInfo2, amount, date, accountStatement;
    Spinner accounts;
    CheckBox autopay_checkbox;
    DatabaseReference db;
    ArrayAdapter arrayAdapter;
    String key, autopay, TAG = "TAG";

    private ArrayList<Account> accountList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_payment_service);

        init();

        //Load saved instance
        if (savedInstanceState != null) {
            accountStatement.setText(savedInstanceState.getString("accountStatement"));
            amount.setText(savedInstanceState.getString("amount"));
            date.setText(savedInstanceState.getString("date"));
            cardType.setText(savedInstanceState.getString("cardType"));
            cardInfo1.setText(savedInstanceState.getString("cardInfo1"));
            cardInfo2.setText(savedInstanceState.getString("cardInfo2"));
            accountList = savedInstanceState.getParcelableArrayList("accountList");
        }
    }

    private void init() {
        confirm = findViewById(R.id.confirm);
        back = findViewById(R.id.back);
        cardType = findViewById(R.id.card_type_input);
        cardInfo1 = findViewById(R.id.card_info_1);
        cardInfo2 = findViewById(R.id.card_info_2);
        amount = findViewById(R.id.amount_input);
        date = findViewById(R.id.date_input);
        accountStatement = findViewById(R.id.account_statement_input);
        accounts = findViewById(R.id.accounts);
        autopay_checkbox = findViewById(R.id.autopay_checkbox);

        db = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        accountList = intent.getParcelableArrayListExtra("Accounts");
        key = intent.getStringExtra("Key");

        //Remove pension account
        for (Account account :accountList) {
            if (account.getType().equals("pension")) {
                accountList.remove(account);
                Log.d(TAG, "Pension account removed!");
            }
        }

        arrayAdapter = new ArrayAdapter<>(newPaymentServiceActivity.this, R.layout.spinner_item_row, accountList);
        accounts.setAdapter(arrayAdapter);
    }

    public void confirm(View v){
        //Check user inputs
        //Date needs to match a specific regex: dd/mm/yyyy
        if (date.getText().toString().length() != 10 && !date.getText().toString().matches("^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$")) {
            Toast toast = Toast.makeText(this, "Date must of format dd/mm/yyyy!", Toast.LENGTH_SHORT);
            toast.show();
            date.requestFocus();
        }else {
            //Get values
            Account accountFromSpinner = (Account) accounts.getSelectedItem();
            if (autopay_checkbox.isChecked()) {
                autopay = "yes";
            } else {
                autopay = "no";
            }

            //Concatenate all the payment id inputs
            String idReceiver = "+" + cardType.getText().toString() + "<" + cardInfo1.getText().toString() + "+" + cardInfo2.getText().toString() + "<";
            //Get sender id from spinner
            String idSender = accountFromSpinner.getId();
            //Split date and send to database in new format (The reason it needs to be split, is because
            //a "/" in a string will make a new child in the database, so it is replaced with :)
            String[] dateOfServiceSplit = date.getText().toString().split("/");
            String dateOfService = Integer.valueOf(dateOfServiceSplit[0]).toString() + ":" +  Integer.valueOf(dateOfServiceSplit[1]).toString() + ":" + dateOfServiceSplit[2];
            Double amountToSend = Double.valueOf(amount.getText().toString());
            String description = accountStatement.getText().toString();

            //Send info to database
            transferService.paymentService(idReceiver, idSender, dateOfService, amountToSend, key, autopay, description);
            Toast toast = Toast.makeText(this, "Payment service has been created!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void back(View v){
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("accountStatement", accountStatement.getText().toString());
        outState.putString("amount", amount.getText().toString());
        outState.putString("date", date.getText().toString());
        outState.putString("cardType", cardType.getText().toString());
        outState.putString("cardInfo1", cardInfo1.getText().toString());
        outState.putString("cardInfo2", cardInfo2.getText().toString());
        outState.putParcelableArrayList("accountList", accountList);
    }

}
