package com.example.android_exam_project.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_exam_project.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    Button login, register;
    TextView cpr, password;
    String TAG = "TAG";
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        cpr = findViewById(R.id.cpr_input);
        password = findViewById(R.id.password_input);
        db = FirebaseDatabase.getInstance().getReference("users");

        cpr.requestFocus();
    }

    public void validateLogin(View v) {
        // Read from the database
        db.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userCpr = cpr.getText().toString();
                String userPassword = password.getText().toString();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onChildAdded: " + snapshot);
                    if (snapshot.child("cpr").getValue(String.class).equals(userCpr) && snapshot.child("password").getValue(String.class).equals(userPassword)) {
                        String name = snapshot.child("name").getValue(String.class);
                        String key = snapshot.getKey();
                        Toast toast = Toast.makeText(loginActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT);
                        toast.show();
                        Intent intent = new Intent(loginActivity.this, homeActivity.class);
                        intent.putExtra("Key", key);
                        startActivity(intent);
                        break;
                        //Check for number
                    } else {
                        Log.d(TAG, "User input: " + cpr.getText().toString() + ", " + password.getText().toString());
                        Toast toast = Toast.makeText(loginActivity.this, "Wrong cpr or password! Try again", Toast.LENGTH_SHORT);
                        toast.show();
                        cpr.setText("");
                        password.setText("");
                        cpr.requestFocus();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void registerUser(View v) {
        Intent intent = new Intent(this, registerActivity.class);
        startActivity(intent);
    }


}
