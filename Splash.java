package com.project.raymond.reporttopolice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);

        Thread myThread = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(2000);
                    Intent intent = new Intent((getApplicationContext()), LoginActivity.class );
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };

        myThread.start();
    }
}

