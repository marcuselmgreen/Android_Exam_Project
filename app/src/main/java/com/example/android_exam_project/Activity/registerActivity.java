package com.example.android_exam_project.Activity;

import android.content.Context;
import android.content.Intent;
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

        //Load saved instance
        if (savedInstanceState != null) {
            cpr.setText(savedInstanceState.getString("cpr"));
            name.setText(savedInstanceState.getString("name"));
            email.setText(savedInstanceState.getString("email"));
            zip.setText(savedInstanceState.getString("zip"));
            phone.setText(savedInstanceState.getString("phone"));
            password.setText(savedInstanceState.getString("password"));
            dateofbirth.setText(savedInstanceState.getString("dateofbirth"));
        }
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

    //Register new user and send information to the database
    public void registerUser(View v){
        //Check input values
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
                            //Check if a user with the same cpr exists
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
                        //Write new user to database
                        writeNewUser(cpr.getText().toString(), name.getText().toString(), email.getText().toString(), Integer.parseInt(zip.getText().toString()), Integer.parseInt(phone.getText().toString()), password.getText().toString(), dateofbirth.getText().toString());
                        Toast toast = Toast.makeText(registerActivity.this, "User has been created!", Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
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

        //Check length of cpr and if cpr consists of numbers
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
        //Zip code must be 4 digits
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
        //Check length of date of birth and if it matches the pattern dd/mm/yyyy
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

        //Send information to the database. It will automatically make children according to the
        //number of attributes of the User class and make key + values from that
        db.child(key).setValue(user);

        //Calculate nearest affiliate
        Context context = this.getApplicationContext();
        userService.getAffiliate(zip, context, key);

        //Create unique account id and registration number is the same for all the users of the bank
        long regNumber = 2055;
        long accountId = regNumber + Math.round(Math.random() * (9999 - 1000) + 1000);

        //Provide a default and budget account to the user with a balance of 100
        Account defaultAccount = new Account("default", 100);
        Account budgetAccount = new Account("budget", 100);

        //Set account number as 1 and 2, because these are the first two accounts
        db.child(key).child(regNumber + " " + regNumber + accountId + 1).setValue(defaultAccount);
        db.child(key).child(regNumber + " " + regNumber + accountId + 2).setValue(budgetAccount);
        db.child(key).child("accountId").setValue(accountId);
    }

    public void back(View v){
        Intent intent = new Intent(this, loginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("cpr", cpr.getText().toString());
        outState.putString("name", name.getText().toString());
        outState.putString("email", email.getText().toString());
        outState.putString("zip", zip.getText().toString());
        outState.putString("phone", phone.getText().toString());
        outState.putString("dateofbirth", dateofbirth.getText().toString());
        outState.putString("password", password.getText().toString());
    }
}
