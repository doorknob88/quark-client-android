package com.arvind.quark;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.arvind.quark.models.Contact;
import com.arvind.quark.util.Encryption;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class GlobalValues {
    private static final GlobalValues ourInstance = new GlobalValues();

    //Put Everything that you'll use throughout the app in here.

    private String phoneNumber;
    private String publicAddress;
    private String seed;
    private String userName;
    private HashMap<String, Contact> contactMap;
    private HashMap<String, Contact> matchedContacts;
    private String hostURL = "http://192.168.0.116:3000";


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public String getSeed() {
        return seed;
    }

    public String getUserName() {
        return userName;
    }

    public HashMap<String, Contact> getContactMap() {
        if (contactMap == null){
            contactMap = new HashMap<>();
        }
        return contactMap;
    }

    public String getHostURL() {
        return hostURL;
    }

    public static GlobalValues getInstance() {
        return ourInstance;
    }

    private GlobalValues(){

    }

    public GlobalValues getValues(Context context) {

        SharedPreferences prefs = context.getSharedPreferences("store", MODE_PRIVATE);
        String encryptedSeed = prefs.getString("encryptedSeed", null);
        this.phoneNumber = prefs.getString("phoneNumber", null);
        this.publicAddress = prefs.getString("publicAddress", null);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        Encryption encryption = new Encryption(firebaseUser.getUid());
        try {
            this.seed = encryption.decrypt(encryptedSeed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.userName = prefs.getString("username", null);
        return ourInstance;
    }

    public void logout(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences("store", MODE_PRIVATE).edit();
        editor.putString("encryptedSeed", null);
        editor.putString("publicAddress", null);
        editor.putString("username", null);
        editor.putString("phoneNumber", null).commit();
        seed = null;
        publicAddress = null;
        userName = null;
        phoneNumber = null;
    }

    public HashMap<String, Contact> getMatchedContacts() {
        if (matchedContacts == null){
            matchedContacts = new HashMap<>();
        }
        return matchedContacts;
    }
}
