package com.arvind.quark;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.arvind.quark.auth.LoginActivity;
import com.arvind.quark.models.Contact;
import com.arvind.quark.models.ContactModel;
import com.arvind.quark.settings.SettingsActivity;
import com.arvind.quark.util.NanoUtil;
import com.firebase.ui.auth.util.data.PhoneNumberUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    GlobalValues globalValues;
    String TAG = this.getClass().getName();
    RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        globalValues = GlobalValues.getInstance().getValues(getApplicationContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        FloatingActionButton fab = findViewById(R.id.fab);
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
            Contact contact = new Contact(id,name,phoneNumber);
            globalValues.getContactMap().put(id, contact);
        }
        phones.close();

        matchContacts();

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), SendActivity.class);
                startActivity(intent);

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TextView sharedPref_view = findViewById(R.id.SharedPref_view);
        TextView balance_view = findViewById(R.id.balance);

        String text = "Global Values: "
                +"\n Seed: "+ globalValues.getSeed()
                +"\n Public Address: "+globalValues.getPublicAddress()
                +"\n Phone Number: "+globalValues.getPhoneNumber()
                +"\n UserName :"+globalValues.getUserName();



        Log.i(TAG, "SEED: "+globalValues.getSeed());
        Log.i(TAG, "PUBLIC ADDRESS: "+globalValues.getPublicAddress());
        Log.i(TAG, "Private Key: "+ NanoUtil.seedToPrivate(globalValues.getSeed()));


        sharedPref_view.setText(text);
        //balance_view.setText(globalValues.getBalance());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = new Intent(this, PollService.class);
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.sign_out_button){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            globalValues.logout(getApplicationContext());
            auth.signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_search_people) {
            //launch activity to search people
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void matchContacts(){

        final JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Contact> entry : globalValues.getContactMap().entrySet()) {
            String key = entry.getKey();
            Contact value = entry.getValue();
            try {
                if (value.getPhoneNumber() != null)
                jsonObject.put(value.getPhoneNumber(), key);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * The contact JSON should look like this:
         *  {
         *      "contact_ID" : "phoneNumber"
         *  }
         *  Because contact_ID is the same for multiple contact groups.
         */

        Log.i(TAG, jsonObject.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (globalValues.getHostURL()+"/contacts", jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0){
                            Iterator<String> keys = response.keys();

                            while (keys.hasNext()){
                                String key = (String) keys.next();
                                try {
                                    if ( response.get(key) instanceof JSONObject ) {

                                        Contact temp = globalValues.getContactMap().get(key);
                                        JSONObject matchedObject = response.getJSONObject(key);

                                        temp.setUserName((String) matchedObject.get("username"));
                                        temp.setPublicAddress((String) matchedObject.get("publicAddress"));
                                        globalValues.getMatchedContacts().add(temp);

                                        for (int i = 0; i < globalValues.getMatchedContacts().size(); i++){
                                            Log.i(TAG, "Matched Contact:");
                                            Log.i(TAG, globalValues.getMatchedContacts().get(i).getUserName()+ " "
                                                            + globalValues.getMatchedContacts().get(i).getPublicAddress() + " "
                                                            + globalValues.getMatchedContacts().get(i).getDisplayName());
                                        }


                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
