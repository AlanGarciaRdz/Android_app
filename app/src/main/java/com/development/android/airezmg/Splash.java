package com.development.android.airezmg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.development.android.airezmg.model.GPSTracker;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Splash extends AppCompatActivity {

    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

    private final Handler mHideHandler = new Handler();
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        mContentView = (View) findViewById(R.id.splashlayout);
        mHideHandler.removeCallbacks(mSkipRunnable);
        mHideHandler.postDelayed(mSkipRunnable, AUTO_HIDE_DELAY_MILLIS);
        GPSTracker tracker = new GPSTracker();
        boolean needPermissions = tracker.checkPermissions(this);
        if(!needPermissions)
            tracker.initLocationService(this);

    }

    private final Runnable mSkipRunnable = new Runnable() {
        @Override
        public void run() {
            goToMainActivity();
        }
    };

    private void goToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }



}
