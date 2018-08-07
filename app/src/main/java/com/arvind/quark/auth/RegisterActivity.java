package com.arvind.quark.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quark.GlobalValues;
import com.arvind.quark.MainActivity;
import com.arvind.quark.R;
import com.arvind.quark.util.Encryption;
import com.arvind.quark.util.NanoUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {


    String TAG = this.getClass().getName();
    RequestQueue requestQueue;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    Button registerButton;
    EditText username;
    String seed;
    String publicAddress;
    String phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        registerButton = findViewById(R.id.register);
        username = findViewById(R.id.create_username);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                assert firebaseUser != null;
                phoneNumber = firebaseUser.getPhoneNumber();
                if (username.getText().toString().length() > 4) {
                    firebaseUser.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onSuccess(GetTokenResult getTokenResult) {
                            try {
                                seed = NanoUtil.generateSeed();
                                publicAddress = NanoUtil.publicToAddress(NanoUtil.privateToPublic(NanoUtil.seedToPrivate(seed)));

                                Log.i(TAG, firebaseUser.getUid());
                                Encryption encryption = new Encryption(firebaseUser.getUid());
                                String encryptedSeed = encryption.encrypt(seed);
                                JSONObject jsonObject = new JSONObject();

                                jsonObject.put("token", getTokenResult.getToken());
                                jsonObject.put("username", username.getText().toString());
                                jsonObject.put("publicAddress", publicAddress);
                                jsonObject.put("phoneNumber", phoneNumber);
                                jsonObject.put("encryptedSeed", encryptedSeed);

                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                        (Request.Method.POST, GlobalValues.getInstance().getHostURL() + "/register", jsonObject, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Log.i("RESPONSE: ", response.toString());
                                                try {
                                                    if (response.has("errors")) {
                                                        JSONArray errors = response.getJSONArray("errors");
                                                        for(int i = 0; i < errors.length(); i++){
                                                            String error = errors.getJSONObject(i).getString("param");
                                                            if (error.equals("username")){
                                                                Context context = getApplicationContext();
                                                                CharSequence text = "Username already exists, Please try again!";
                                                                int duration = Toast.LENGTH_SHORT;
                                                                Toast toast = Toast.makeText(context, text, duration);
                                                                toast.show();
                                                            }else {
                                                                Context context = getApplicationContext();
                                                                CharSequence text = "DEBUG, MISSING: "+error;
                                                                int duration = Toast.LENGTH_SHORT;
                                                                Toast toast = Toast.makeText(context, text, duration);
                                                                toast.show();
                                                            }
                                                        }

                                                    } else if (response.has("username")){
                                                        //No errors, create the user then redirect to Mainactivity
                                                        SharedPreferences.Editor editor = getSharedPreferences("store", MODE_PRIVATE).edit();
                                                        editor.putString("encryptedSeed", seed).commit();
                                                        editor.putString("publicAddress", publicAddress).commit();
                                                        editor.putString("username", response.getString("username")).commit();
                                                        editor.putString("phoneNumber", firebaseUser.getPhoneNumber()).commit();


                                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });


    }

    private void createUser(String username) {

        SharedPreferences prefs = getSharedPreferences("store", MODE_PRIVATE);
        String restoredText = prefs.getString("seed", null);
        if (restoredText == null) {
            Log.e(TAG, "SEED IS MISSING");
        }
    }

}
