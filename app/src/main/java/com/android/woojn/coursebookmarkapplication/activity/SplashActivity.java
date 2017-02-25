package com.android.woojn.coursebookmarkapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by wjn on 2017-02-24.
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            Thread.sleep(1000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(new Intent(this, MainActivity.class));
    }
}
