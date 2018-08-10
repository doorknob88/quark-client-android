package com.arvind.quark;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quark.models.Contact;
import com.arvind.quark.util.NanoUtil;
import com.arvind.quark.util.NumberUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Iterator;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PollService extends IntentService {
    GlobalValues globalValues;
    String TAG = this.getClass().getName();
    RequestQueue requestQueue;
    Boolean doing = false;
    public PollService() {
        super("PollService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        globalValues = GlobalValues.getInstance();
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        while (true) {

            try {
                if (doing == false) {
                    System.out.println("Polling");
                    getAccountInfo();
                }else {
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
            }
        }

    }

    public void getAccountInfo(){
        Log.i(TAG, "Getting Account Info");
        doing = true;
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", globalValues.getPublicAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (globalValues.getHostURL()+"/nano/account_info", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("error")){

                            try {
                                if (response.get("error").equals("Account not found")){
                                    globalValues.setFrontier("0");
                                    globalValues.setBalance("0");
                                    Log.i(TAG, "Account Does not exist!");
                                    getBalance();

                                    //receive();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if(response.has("pending")){

                            try {
                                globalValues.setFrontier(response.getString("frontier"));
                                globalValues.setRepresentative(response.getString("representative"));
                                String pending = response.getString("pending");
                                String balance = response.getString("balance");
                                globalValues.setBalance(balance);
                                Log.i(TAG, "Account Updated");
                                NumberUtil numberUtil = new NumberUtil();
                                BigInteger pendingBigInt = new BigInteger(pending);

                                if ( pendingBigInt.compareTo(new BigInteger("0")) > 0 ){
                                    Log.i(TAG, "Balance Pending");
                                    CharSequence text = "Funds Pending! Processing (This could take a little while):  "+ NumberUtil.getRawAsUsableString(pending);
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = null;
                                    toast = Toast.makeText(getApplicationContext(), text, duration);
                                    toast.show();
                                    receive();
                                }else {
                                    doing = false;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i(TAG, "Balance: " + response.toString() );
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        doing = false;
                    }
                });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    public void receive(){
    Log.i(TAG, "Receive Called");
    JSONObject pendingObject = new JSONObject();
        try {
            pendingObject.put("address", globalValues.getPublicAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest pendingObjectRequest = new JsonObjectRequest
                (globalValues.getHostURL()+"/nano/pending", pendingObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("blocks")){
                            Log.i(TAG, "Response: " + response.toString());
                            try {
                            String pending;
                                JSONObject blocks = response.getJSONObject("blocks");
                            if (blocks.length() > 0){
                                Iterator<String> keys = blocks.keys();
                                if( keys.hasNext() ){
                                    String key = (String)keys.next(); // First key in your json object
                                    pending = key;
                                    Log.i(TAG, "KEY: " + key);
                                    JSONObject object = blocks.getJSONObject(key);
                                    Log.i(TAG, "OBJECT: " + object.toString());
                                    String amountPending = object.getString("amount");
                                    BigInteger balance = new BigInteger(globalValues.getBalance());
                                    BigInteger pendingBigInt = new BigInteger(amountPending);
                                    String newBalance = balance.add(pendingBigInt).toString();

                                    final JSONObject receiveObject = new JSONObject();
                                    receiveObject.put("action", "block_create");
                                    receiveObject.put("type", "state");
                                    receiveObject.put("previous", globalValues.getFrontier());
                                    receiveObject.put("account", globalValues.getPublicAddress());
                                    receiveObject.put("representative", globalValues.getRepresentative());
                                    receiveObject.put("link", pending);
                                    receiveObject.put("key", NanoUtil.seedToPrivate(globalValues.getSeed()));
                                    receiveObject.put("balance", newBalance);

                                    JsonObjectRequest createObjectRequest = new JsonObjectRequest
                                            (globalValues.getHostURL()+"/nano/create", receiveObject, new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {

                                                    if (response.has("error")){
                                                        doing = false;
                                                    }

                                                    if (response.has("hash")) {

                                                        JSONObject receiveProcessObject = new JSONObject();
                                                        try {
                                                            receiveProcessObject.put("action", "process");
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            receiveProcessObject.put("block", response.getString("block"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }


                                                        JsonObjectRequest processObjectRequest = new JsonObjectRequest
                                                                (globalValues.getHostURL() + "/nano/process", receiveProcessObject, new Response.Listener<JSONObject>() {
                                                                    @Override
                                                                    public void onResponse(JSONObject response) {

                                                                        if (response.has("error")) {
                                                                                doing = false;
                                                                        }

                                                                        if (response.has("hash")) {
                                                                            doing = false;
                                                                            try {
                                                                                globalValues.setFrontier(response.getString("hash"));
                                                                                CharSequence text = "Funds Received! Refresh app to see updated funds!";
                                                                                int duration = Toast.LENGTH_SHORT;
                                                                                Toast toast = null;
                                                                                toast = Toast.makeText(getApplicationContext(), text, duration);
                                                                                toast.show();
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }


                                                                    }
                                                                }, new Response.ErrorListener() {
                                                                    @Override
                                                                    public void onErrorResponse(VolleyError error) {
                                                                        doing = false;
                                                                    }
                                                                });

                                                        int duration = Toast.LENGTH_SHORT;
                                                        Toast toast = null;
                                                        toast = Toast.makeText(getApplicationContext(), "Receive Block created! Pushing to network.....", duration);
                                                        toast.show();

                                                        processObjectRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                                                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                        Log.i(TAG, "Processing a receive block!");
                                                        requestQueue.add(processObjectRequest);
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    doing = false;
                                                }
                                            });

                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = null;
                                    toast = Toast.makeText(getApplicationContext(), "Generating a Receive block on the server...", duration);
                                    toast.show();

                                    createObjectRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    Log.i(TAG, "Creating a receive block!");
                                    requestQueue.add(createObjectRequest);

                                }
                            }else {

                            }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        pendingObjectRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(pendingObjectRequest);
    }

    public void getBalance(){

        JSONObject balanceObject = new JSONObject();
        try {
            balanceObject.put("address", globalValues.getPublicAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (globalValues.getHostURL()+"/nano/balance", balanceObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("error")){

                        }

                        String balance = null;
                        try {
                            balance = response.getString("balance");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String pending = null;
                        try {
                            pending = response.getString("pending");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        globalValues.setBalance(balance);
                        BigInteger pendingBigInt = new BigInteger(pending);

                        if ( pendingBigInt.compareTo(new BigInteger("0")) > 0 ){
                            Log.i(TAG, "Balance Pending");
                            receive();
                        }else {
                            doing = false;
                        }

                        Log.i(TAG, "Getting Balance " + response.toString());

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        doing = false;
                    }
                });
        requestQueue.add(jsonObjectRequest);

    }
}
