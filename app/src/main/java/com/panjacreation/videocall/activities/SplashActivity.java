package com.panjacreation.videocall.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.panjacreation.videocall.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }


        Thread t= new Thread() {
            public void run() {
                try {
                    sleep(4500);

                    Intent i=new Intent(getBaseContext(),LoginActivity.class);
                    startActivity(i);

                    finish();
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        };
        // start thread
        t.start();

    }
}