package com.example.android_exam_project.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_exam_project.Activity.homeActivity;
import com.example.android_exam_project.Activity.loginActivity;
import com.example.android_exam_project.R;
import com.example.android_exam_project.Service.userService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class settingsActivity extends AppCompatActivity {

    TextView name, affiliate;
    EditText zip, email, phone, old_password, new_password, confirm_password;
    Button save;
    String key, old_password_db, TAG = "TAG";
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
    }

    private void init() {
        name = findViewById(R.id.Name_txt);
        affiliate = findViewById(R.id.affiliate_txt);
        zip = findViewById(R.id.Zip_input);
        email = findViewById(R.id.Email_input);
        phone = findViewById(R.id.Phone_input);
        old_password = findViewById(R.id.old_password_input);
        new_password = findViewById(R.id.new_password_input);
        confirm_password = findViewById(R.id.confirm_password_input);
        save = findViewById(R.id.Save);
        db = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        key = intent.getStringExtra("Key");
        Log.d(TAG, "init: " + key);

        loadInfo();
    }

    //Load information from Firebase and show to the user
    private void loadInfo() {

        db.child(key).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildAdded: " + dataSnapshot);
                String userName = dataSnapshot.child("name").getValue(String.class).toUpperCase();
                name.setText(userName);
                String currentAffiliate = "AFFILIATE IN " + dataSnapshot.child("affiliate").getValue(String.class).toUpperCase();
                affiliate.setText(currentAffiliate);
                zip.setText(String.valueOf(dataSnapshot.child("zip").getValue(Long.class)));
                email.setText(dataSnapshot.child("email").getValue(String.class));
                phone.setText(String.valueOf(dataSnapshot.child("phone").getValue(Long.class)));

                old_password_db = dataSnapshot.child("password").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void save(View v) {
        //When updating user:
        db.child(key).child("name").setValue(name.getText().toString());
        db.child(key).child("zip").setValue(Integer.valueOf(zip.getText().toString()));
        db.child(key).child("email").setValue(email.getText().toString());
        db.child(key).child("phone").setValue(Integer.valueOf(phone.getText().toString()));
        //If Zip changes then check if current affiliate still is the nearest
        Context context = this.getApplicationContext();
        userService.getAffiliate(Integer.valueOf(zip.getText().toString()), context, key);

        confirmPassword();
    }

    private void confirmPassword() {
        if (!new_password.getText().toString().isEmpty()){
            if (!old_password.getText().toString().equals(old_password_db)){
                Toast toast = Toast.makeText(this, "Old password is not correct!", Toast.LENGTH_SHORT);
                toast.show();
                old_password.setText("");
            } else {
                if (!new_password.getText().toString().equals(confirm_password.getText().toString())){
                    Toast toast = Toast.makeText(this, "New password is not confirmed!", Toast.LENGTH_SHORT);
                    toast.show();
                    new_password.setText("");
                    confirm_password.setText("");
                } else {
                    db.child(key).child("password").setValue(new_password.getText().toString());
                }
            }
        }
    }

    public void back(View v){
        finish();
    }

}
