package com.giftsmile.app.smile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    Thread myThread = new Thread(){
        @Override
        public void run(){
            try {
                sleep(3000);
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

        myThread.start();
    }
}
