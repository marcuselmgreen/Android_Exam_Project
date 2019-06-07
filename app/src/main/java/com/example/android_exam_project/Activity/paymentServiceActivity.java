package com.example.android_exam_project.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.Model.PaymentService;
import com.example.android_exam_project.R;
import com.example.android_exam_project.Service.transferService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class paymentServiceActivity extends AppCompatActivity {

    Button back, addNewService, payBills;
    ListView paymentServices;
    String key, TAG = "TAG";
    DatabaseReference db;
    ArrayAdapter arrayAdapter;

    private ArrayList<Account> accountList = new ArrayList<>();
    private ArrayList<PaymentService> paymentServiceList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_service);

        //MAKE THIS ACTIVITY AN PAYMENTSERVICE OVERVIEW WITH ABILITY TO PAY BILLS MANUALLY

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPaymentServiceList();
    }

    private void init() {
        back = findViewById(R.id.back);
        payBills = findViewById(R.id.pay_bills);
        addNewService = findViewById(R.id.add_new_service);
        paymentServices = findViewById(R.id.payment_service_list);

        db = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        accountList = intent.getParcelableArrayListExtra("Accounts");
        key = intent.getStringExtra("Key");

        loadPaymentServiceList();

    }

    private void loadPaymentServiceList() {
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Get accounts with payment_service child and show in listView
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.hasChild("payment_service")) {
                        Iterable<DataSnapshot> monthlyDepositChildren = snapshot.child("payment_service").getChildren();
                        int count = 0;
                        for (DataSnapshot children : monthlyDepositChildren) {
                            String account = children.getKey();
                            Double amount = children.child("amount").getValue(Double.class);
                            String date = children.child("date").getValue(String.class);
                            String description = children.child("description").getValue(String.class);
                            String autopay = children.child("autopay").getValue(String.class);
                            String idSender = snapshot.getKey();
                            PaymentService paymentService = new PaymentService(date, amount, account, description, autopay, idSender);

                            if (!paymentServiceList.contains(paymentService) && paymentServiceList.size() == count) {
                                paymentServiceList.add(paymentService);
                                //Might not work when adding more accounts
                                arrayAdapter = new ArrayAdapter(paymentServiceActivity.this, android.R.layout.simple_list_item_1, paymentServiceList);
                                paymentServices.setAdapter(arrayAdapter);
                                //Update accountList and listView if an account has changed
                            }else {
                                paymentServiceList.set(count, paymentService);
                                arrayAdapter.notifyDataSetChanged();
                            }
                            count += 1;
                        }
                    }else {
                        Log.d(TAG, "No payment services created");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void payBills(View v) {
        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.MONTH, today.get(Calendar.MONTH) + 1);

                //Test if method works
                //today.set(Calendar.MONTH, today.get(Calendar.MONTH) + 4);
                Log.d(TAG, "date today: " + today);
                for (int i = 0; i < paymentServiceList.size(); i++) {
                    Log.d(TAG, "onDataChange: Checking accounts");
                    String date = paymentServiceList.get(i).getDate();
                    String[] dateSplit = date.split(":");
                    int day = Integer.valueOf(dateSplit[0]);
                    int month = Integer.valueOf(dateSplit[1]) + 1;
                    int year = Integer.valueOf(dateSplit[2]);
                    Calendar dueDate = Calendar.getInstance();
                    dueDate.set(year, month, day);
                    Log.d(TAG, "onDataChange: Date today: " + today.get(Calendar.DAY_OF_MONTH) + "/" + today.get(Calendar.MONTH) + ", Date due: " + date);
                    if (dueDate.before(today) && paymentServiceList.get(i).getAutopay().equals("no")) {
                        Log.d(TAG, "onDataChange: Checking date");
                        String idSender = paymentServiceList.get(i).getIdSender();
                        Double balance = dataSnapshot.child(idSender).child("balance").getValue(Double.class);
                        if (paymentServiceList.get(i).getAmount() < balance) {
                            Log.d(TAG, "onDataChange: Checking balance");
                            //Update with new date
                            String idReceiver = paymentServiceList.get(i).getAccount();
                            Context context = paymentServiceActivity.this;
                            String nextMonth = day + ":" + (today.get(Calendar.MONTH) + 1) + ":" + today.get(Calendar.YEAR);
                            Double amountToSend = dataSnapshot.child(idSender).child("payment_service").child(idReceiver).child("amount").getValue(Double.class);
                            String autopay = paymentServiceList.get(i).getAutopay();
                            String description = paymentServiceList.get(i).getDescription();

                            //Calculate amount of months worth of deposits
                            Double amountToSendCalc = amountToSend * (today.get(Calendar.MONTH) - month);

                            //Make transfer
                            transferService.transaction(idSender, idReceiver, amountToSendCalc, context, key, "payment_service");

                            //Remove children
                            db.child(key).child(idSender).child("payment_service").child(idReceiver).removeValue();

                            //Create new child with the date
                            transferService.paymentService(idReceiver, idSender, nextMonth, amountToSend, key, autopay, description);
                        }
                    }
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError databaseError){
            }
        });
        finish();
    }

    public void addNewService(View v) {
        Intent intent = new Intent(this, newPaymentServiceActivity.class);
        intent.putExtra("Accounts", accountList);
        intent.putExtra("Key", key);
        startActivity(intent);
    }

    public void back(View v) {
        finish();
    }
}
