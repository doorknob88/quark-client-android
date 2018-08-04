package com.arvind.quark;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arvind.quark.util.NanoUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Operations {
    String TAG = this.getClass().getName();

    GlobalValues globalValues;
    RequestQueue requestQueue;
    public void send(String amount, String destination, final Context context) throws Exception {

        globalValues = GlobalValues.getInstance();
        requestQueue = Volley.newRequestQueue(context);

        JSONObject sendObject = new JSONObject();
        JSONObject sendBlock = new JSONObject();


        sendBlock.put("action", "block_create");
        sendBlock.put("type", "state");
        sendBlock.put("previous", globalValues.getFrontier());
        sendBlock.put("account", globalValues.getPublicAddress());
        sendBlock.put("representative", globalValues.getRepresentative());

        BigDecimal power = new BigDecimal("10").pow(24);

        BigInteger balance = new BigInteger(globalValues.getBalance());
        BigInteger amountTemp = new BigDecimal(amount).multiply(power).toBigIntegerExact();

        Log.i(TAG, "Balance: " + balance.toString());
        Log.i(TAG, "amount " + amountTemp.toString());

        if (amountTemp.compareTo(balance) != -1){
            return ;
        }

        BigInteger newBalance = balance.subtract(amountTemp);
        sendBlock.put("balance" , newBalance.toString());

        sendBlock.put("link", destination);
        sendBlock.put("key", NanoUtil.seedToPrivate(globalValues.getSeed()));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (globalValues.getHostURL()+"/nano/create", sendBlock, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("hash")){

                            JSONObject processBlock = new JSONObject();
                            try {
                                processBlock.put("action", "process");
                                processBlock.put("block", response.getString("block"));

                                JsonObjectRequest processObjectRequest = new JsonObjectRequest(
                                        globalValues.getHostURL() + "/nano/process", processBlock, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        if (response.has("error")){
                                            CharSequence text = "Sending Error!";
                                            int duration = Toast.LENGTH_SHORT;
                                            Toast toast = null;
                                            try {
                                                toast = Toast.makeText(context, text+response.getString("error"), duration);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            toast.show();
                                        }

                                        if (response.has("hash")){
                                            CharSequence text = "Done!";
                                            int duration = Toast.LENGTH_SHORT;
                                            Toast toast = null;
                                                toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                });

                                processObjectRequest.setRetryPolicy(new DefaultRetryPolicy(100000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                requestQueue.add(processObjectRequest);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "Error: " + error.getLocalizedMessage());
                    }
                });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);

    }

}
