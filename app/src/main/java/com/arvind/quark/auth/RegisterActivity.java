package com.arvind.quark.auth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.arvind.quark.R;
import com.arvind.quark.util.NanoUtil;

public class RegisterActivity extends AppCompatActivity {


    String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.i(TAG, "Secure Seed: " + NanoUtil.generateSeed());
    }

    private void createUser(){

        NanoUtil.generateSeed();

    }

}
