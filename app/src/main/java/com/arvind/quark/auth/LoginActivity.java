package com.arvind.quark.auth;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quark.GlobalValues;
import com.arvind.quark.MainActivity;
import com.arvind.quark.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    String TAG = this.getClass().getName();
    GlobalValues globalValues;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        final Button signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        try {
            globalValues = GlobalValues.getInstance().getValues(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (currentUser == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build());
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        } else {
            if (globalValues.getSeed() == null) {
                connectToServer();
            }else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void connectToServer(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        user.getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
                try {
                    JSONObject jsonObject = new JSONObject().put("token", getTokenResult.getToken());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, GlobalValues.getInstance().getHostURL()+"/login",jsonObject, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("RESPONSE: ", response.toString());
                                    try {
                                        if (response.has("response")){
                                            if(response.getString("response").equals("register")){
                                                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                                                startActivity(intent);
                                            }
                                        }else {
                                            if (response.has("username")){
                                                Log.i(TAG, "User received!"+ response.toString());
                                                SharedPreferences.Editor editor = getSharedPreferences("store", MODE_PRIVATE).edit();
                                                editor.putString("encryptedSeed", response.getString("encryptedSeed")).commit();
                                                editor.putString("publicAddress", response.getString("publicAddress")).commit();
                                                editor.putString("username", response.getString("username")).commit();
                                                editor.putString("phoneNumber", user.getPhoneNumber()).commit();

                                                Log.i(TAG, "Logged in, launching main activity!");
                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                Log.e(TAG, "Strange Message from Server");
                                                Log.e(TAG, response.toString());
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.i("ERROR: ", error.toString());
                                    Context context = getApplicationContext();
                                    CharSequence text = "Error Signing In, Please try again!";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            });
                    requestQueue.add(jsonObjectRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Log.i(TAG, "Firebase Signed in");
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                // ...
            } else {
                Log.i(TAG, "Firebase Sign in Failed");
                // Sign in failed, check response for error code
                // ...

            }
        }
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {

                        SharedPreferences.Editor editor = getSharedPreferences("store", MODE_PRIVATE).edit();
                        editor.putString("encryptedSeed", null);
                        editor.putString("publicAddress", null);
                        editor.putString("username", null);
                        editor.putString("phoneNumber", null).commit();

                        Toast toast =  Toast.makeText(getApplicationContext(), GlobalValues.getInstance().getUserName(), Toast.LENGTH_LONG);
                        toast.show();

                        finish();
                        startActivity(getIntent());
                    }
                });
    }
}
