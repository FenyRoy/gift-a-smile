package com.giftsmile.app.smile;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class GIFTSMILE extends Application{

    public void onCreate(){
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
