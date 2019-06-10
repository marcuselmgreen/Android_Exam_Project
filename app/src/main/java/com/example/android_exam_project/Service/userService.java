package com.example.android_exam_project.Service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.example.android_exam_project.Model.Account;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.constraint.Constraints.TAG;

public class userService {
    private static DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
    private static String TAG = "TAG";

    public static void getAffiliate(int zip, Context context, String key){
        String locationName = zip + ", Denmark";
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> address = geoCoder.getFromLocationName(locationName, 1);
            double latitude = address.get(0).getLatitude();
            double longitude = address.get(0).getLongitude();
            float[] resultKBH = new float[1];
            float[] resultOdense = new float[1];
            Location.distanceBetween(latitude, longitude, 55.676098, 12.568337, resultKBH);
            Location.distanceBetween(latitude, longitude, 55.403756, 10.402370, resultOdense);
            double distanceKbh = resultKBH[0];
            double distanceOdense = resultOdense[0];
            Log.d(TAG, "writeNewUser: KBH: " + distanceKbh + ", Odense: " + distanceOdense);
            if (distanceKbh > distanceOdense){
                db.child(key).child("affiliate").setValue("Odense");
            }else {
                db.child(key).child("affiliate").setValue("Copenhagen");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


