package com.example.android_exam_project.Service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_exam_project.Activity.accountSettingsActivity;
import com.example.android_exam_project.Activity.homeActivity;
import com.example.android_exam_project.Activity.nemidActivity;
import com.example.android_exam_project.Activity.transactionActivity;
import com.example.android_exam_project.Model.Account;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class transferService {
    private static DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
    private static String TAG = "TAG";

    //To transfer money from one account to another
    //1. idsender
    //2. idreceiver
    //3. amount to send
    //4. key

    public static void transaction(final String idSender, final String idReceiver, final Double amountToSend, final Context context, final String key, final String transferType) {
        //Checking if receiver account is bound to this user
        final String senderAccount = idSender.substring(9, 13);
        final String receiverAccount = idReceiver.substring(9, 13);

        if (!idSender.equals(idReceiver)) {
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "idSender: " + idSender + "senderBalance: " + dataSnapshot.child(idSender).child("balance").getValue(Double.class));
                    Double senderBalance = dataSnapshot.child(key).child(idSender).child("balance").getValue(Double.class);
                    if (amountToSend < senderBalance) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.hasChild(idReceiver) && senderAccount.equals(receiverAccount) && transferType.equals("normal_transfer")) {
                                //Transfer money
                                db.child(key).child(idSender).child("balance").setValue(senderBalance - amountToSend);

                                //Add amount to receiver
                                Double receiverBalance = snapshot.child(idReceiver).child("balance").getValue(Double.class);
                                snapshot.child(idReceiver).child("balance").getRef().setValue(receiverBalance + amountToSend);

                                Toast toast = Toast.makeText(context, amountToSend + " DKK was sent to " + idReceiver + "!", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            if (snapshot.hasChild(idReceiver) && !senderAccount.equals(receiverAccount) && transferType.equals("normal_transfer")) {
                                //nemId popup
                                String receiverKey = snapshot.getKey();
                                Double receiverBalance = snapshot.child(idReceiver).child("balance").getValue(Double.class);
                                Intent intent = new Intent(context, nemidActivity.class);
                                intent.putExtra("IdReceiver", idReceiver);
                                intent.putExtra("AmountToSend", amountToSend);
                                intent.putExtra("IdSender", idSender);
                                intent.putExtra("SenderBalance", senderBalance);
                                intent.putExtra("ReceiverBalance", receiverBalance);
                                intent.putExtra("ReceiverKey", receiverKey);
                                intent.putExtra("SenderKey", key);
                                context.startActivity(intent);
                            }
                            if (snapshot.hasChild(idReceiver) && senderAccount.equals(receiverAccount) && transferType.equals("monthly_deposit")) {
                                //Transfer money
                                db.child(key).child(idSender).child("balance").setValue(senderBalance - amountToSend);

                                //Add amount to receiver
                                Double receiverBalance = snapshot.child(idReceiver).child("balance").getValue(Double.class);
                                snapshot.child(idReceiver).child("balance").getRef().setValue(receiverBalance + amountToSend);
                            }
                            if (transferType.equals("payment_service")) {
                                //We don't have access to the receiver account so we only retract amount from the sender
                                db.child(key).child(idSender).child("balance").setValue(senderBalance - amountToSend);
                            }
                        }
                    } else {
                        Toast toast = Toast.makeText(context, "You don't have enough money on this account to make the transaction", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        /*if (amountToSend > senderBalance){
            Toast toast = Toast.makeText(context, "You don't have enough money on this account to make the transaction", Toast.LENGTH_SHORT);
            toast.show();
            amount.setText("");
            amount.requestFocus();
        }
        /*if (!snapshot.hasChild(accountTo) && !senderAccount.equals(receiverAccount)){
            Toast toast = Toast.makeText(transactionActivity.this, "Please enter a valid account to receive the money..", Toast.LENGTH_SHORT);
            toast.show();
            toAccountAcc.setText("");
            toAccountReg.setText("");
            toAccountReg.requestFocus();
        }*/

        /*db.child(key).child(idFrom).child("balance").setValue(balanceFrom - amountToSend);
        //String accountId = snapshot.child(accountTo).child("id").getValue(String.class);

        //Add amount to receiver
        balanceTo = snapshot.child(idTo).child("balance").getValue(Double.class);
        snapshot.child(idTo).child("balance").getRef().setValue(balanceTo + amountToSend);*/
    }

    public static void monthlyDeposit(String idReceiver, String idSender, String nextMonth, Double amountToDeposit, String key){
        db.child(key).child(idSender).child("monthly_deposit").child(idReceiver).child("date").setValue(nextMonth);
        db.child(key).child(idSender).child("monthly_deposit").child(idReceiver).child("amount").setValue(amountToDeposit);
    }

    public static void paymentService(String idReceiver, String idSender, String nextMonth, Double amountToDeposit, String key, String autoPay, String description){
        db.child(key).child(idSender).child("payment_service").child(idReceiver).child("date").setValue(nextMonth);
        db.child(key).child(idSender).child("payment_service").child(idReceiver).child("amount").setValue(amountToDeposit);
        db.child(key).child(idSender).child("payment_service").child(idReceiver).child("autopay").setValue(autoPay);
        db.child(key).child(idSender).child("payment_service").child(idReceiver).child("description").setValue(description);
    }

    public static void autoTransfer(DataSnapshot snapshot, String transferType, Calendar today, String key, Context context){
        //Get each date registered for monthly deposit from database and convert to Calendar format
        Iterable<DataSnapshot> monthlyDepositChildren = snapshot.child(transferType).getChildren();
        for (DataSnapshot children : monthlyDepositChildren){
            String date = children.child("date").getValue(String.class);
            String[] dateSplit = date.split(":");
            Log.d(TAG, "Deposit date: " + date);
            int day = Integer.valueOf(dateSplit[0]);
            int month = Integer.valueOf(dateSplit[1] + 1);
            int year = Integer.valueOf(dateSplit[2]);
            Calendar depositDate = Calendar.getInstance();
            depositDate.set(year, month, day);
            if (depositDate.before(today)){

                //Update with new date
                String idReceiver = children.getKey();
                String idSender = snapshot.getKey();
                Double amountToSend = children.child("amount").getValue(Double.class);

                //Calculate amount of months worth of deposits
                Double amountToSendCalc = amountToSend * (today.get(Calendar.MONTH) - month);

                //Make deposit
                transferService.transaction(idSender, idReceiver, amountToSendCalc, context, key, transferType);

                //Remove children
                db.child(key).child(idSender).child(transferType).child(idReceiver).removeValue();

                //Create new child with the date
                if (transferType.equals("monthly_deposit")){
                    String nextMonth = "1:" + (today.get(Calendar.MONTH)+1) + ":" + today.get(Calendar.YEAR);
                    transferService.monthlyDeposit(idReceiver, idSender, nextMonth, amountToSend, key);
                }else {
                    String nextMonth = day + (today.get(Calendar.MONTH)+1) + ":" + today.get(Calendar.YEAR);
                    String description = children.child("description").getValue(String.class);
                    transferService.paymentService(idReceiver, idSender, nextMonth, amountToSend, key, "yes", description);
                }
            }else {

                break;
            }
        }
    }
}
