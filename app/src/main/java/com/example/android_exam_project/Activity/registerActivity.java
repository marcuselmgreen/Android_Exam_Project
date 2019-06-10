package com.example.android_exam_project.Activity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_exam_project.Model.Account;
import com.example.android_exam_project.Model.User;
import com.example.android_exam_project.R;

import com.example.android_exam_project.Service.userService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class registerActivity extends AppCompatActivity {

    Button submit, back;
    TextView cpr, name, email, zip, phone, password, dateofbirth;
    DatabaseReference db;

    String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
    }

    public void init(){
        submit = findViewById(R.id.Submit);
        back = findViewById(R.id.Back);

        cpr = findViewById(R.id.Cpr_input);
        name = findViewById(R.id.Name_input);
        email = findViewById(R.id.Email_input);
        zip = findViewById(R.id.Zip_input);
        phone = findViewById(R.id.phone_input);
        password = findViewById(R.id.password_input);
        dateofbirth = findViewById(R.id.dateofbirth_input);
        db = FirebaseDatabase.getInstance().getReference("users");
    }

    public void registerUser(View v){
        if (isValid()) {
            Log.d(TAG, "Submit pressed");
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("users")) {
                        String userCpr = cpr.getText().toString();
                        Log.d(TAG, "onDataChange method, cpr: " + userCpr);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onChildAdded: " + snapshot);
                            if (snapshot.child("cpr").getValue(String.class).equals(userCpr)) {
                                Log.d(TAG, "onDataChange: " + snapshot.child("cpr").getValue(String.class) + ", Input: " + userCpr);
                                Toast toast = Toast.makeText(registerActivity.this, "A user with the same cpr already exists!", Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            } else {
                                Log.d(TAG, "onDataChange: " + snapshot.child("cpr").getValue(String.class) + ", Input: " + userCpr);
                                writeNewUser(cpr.getText().toString(), name.getText().toString(), email.getText().toString(), Integer.parseInt(zip.getText().toString()), Integer.parseInt(phone.getText().toString()), password.getText().toString(), dateofbirth.getText().toString());
                                Toast toast = Toast.makeText(registerActivity.this, "User has been created!", Toast.LENGTH_SHORT);
                                toast.show();
                                finish();
                            }
                        }
                    } else {
                        writeNewUser(cpr.getText().toString(), name.getText().toString(), email.getText().toString(), Integer.parseInt(zip.getText().toString()), Integer.parseInt(phone.getText().toString()), password.getText().toString(), dateofbirth.getText().toString());
                        Toast toast = Toast.makeText(registerActivity.this, "User has been created!", Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("cancel");
                }
            });
        }
    }

    private boolean isValid() {
        String cprInput = cpr.getText().toString();
        String nameInput = name.getText().toString();
        String emailInput = email.getText().toString();
        String zipInput = zip.getText().toString();
        String passwordInput = password.getText().toString();
        String dateOfBirthInput = dateofbirth.getText().toString();

        if (cprInput.length() != 10 && !Pattern.matches("[a-zA-Z]+", cprInput)){
            Toast toast = Toast.makeText(this, "Cpr must of format xxxxxxxxxx!", Toast.LENGTH_SHORT);
            toast.show();
            cpr.requestFocus();
            return false;
        }
        if (nameInput.isEmpty()){
            Toast toast = Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT);
            toast.show();
            name.requestFocus();
            return false;
        }
        if (emailInput.isEmpty()){
            Toast toast = Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT);
            toast.show();
            email.requestFocus();
            return false;
        }
        if (zipInput.length() != 4){
            Toast toast = Toast.makeText(this, "Must be a valid zip code!", Toast.LENGTH_SHORT);
            toast.show();
            zip.requestFocus();
            return false;
        }
        if (passwordInput.isEmpty()){
            Toast toast = Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT);
            toast.show();
            password.requestFocus();
            return false;
        }
        if (dateOfBirthInput.length() != 10 && !dateOfBirthInput.matches("^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$")){
            Toast toast = Toast.makeText(this, "Date of birth must of format dd/mm/yyyy!", Toast.LENGTH_SHORT);
            toast.show();
            dateofbirth.requestFocus();
            return false;
        }
        return true;
    }

    public void writeNewUser(String cpr, String name, String email, int zip, int phone, String password, String dateOfBirth){
        User user = new User(cpr, name, email, zip, phone, password, dateOfBirth);
        Log.d(TAG, "writeNewUser: " + user.toString());
        String key = db.push().getKey();

        db.child(key).setValue(user);

        //Calculate nearest affiliate
        Context context = this.getApplicationContext();
        userService.getAffiliate(zip, context, key);

        //Create unique account id
        //Alt efter hvilken bank der ligger tættest på, bestemmes regnummeret
        long regNumber = 2055;
        long accountId = regNumber + Math.round(Math.random() * (9999 - 1000) + 1000);

        Account defaultAccount = new Account("default", 0.0);
        Account budgetAccount = new Account("budget", 0.0);

        db.child(key).child(regNumber + " " + regNumber + accountId + 1).setValue(defaultAccount);
        db.child(key).child(regNumber + " " + regNumber + accountId + 2).setValue(budgetAccount);
        db.child(key).child("accountId").setValue(accountId);
    }

    public void back(View v){
        Intent intent = new Intent(this, loginActivity.class);
        startActivity(intent);
    }

}
