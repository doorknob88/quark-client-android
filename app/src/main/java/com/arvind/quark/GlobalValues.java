package com.arvind.quark;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.arvind.quark.models.Contact;
import com.arvind.quark.util.Encryption;
import com.arvind.quark.util.NanoUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private ArrayList<Contact> matchedContacts;
    private String hostURL = "http://quark.cash";
    private String balance;
    private String representative = "xrb_1nanode8ngaakzbck8smq6ru9bethqwyehomf79sae1k7xd47dkidjqzffeg";
    private String frontier;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPublicAddress() {
        return NanoUtil.publicToAddress(NanoUtil.privateToPublic(NanoUtil.seedToPrivate(seed)));
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
            if (encryptedSeed != null)
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

    public ArrayList<Contact> getMatchedContacts() {
        if (matchedContacts == null){
            matchedContacts = new ArrayList<>();
        }
        return matchedContacts;
    }

    public String getBalance() {
        if (balance == null){
            balance = "0";
        }

        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }


    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public String getFrontier() {
        return frontier;
    }

    public void setFrontier(String frontier) {
        this.frontier = frontier;
    }
}
