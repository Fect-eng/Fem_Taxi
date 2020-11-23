package com.example.femtaxi.providers;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverProvider {

    DatabaseReference mDatabase;

    public DriverProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients");

    }
}

  /*  public Task<Void> create(Driver driver){
       // return mDatabase.child(driver.getId()).setValue(driver);

}*/
