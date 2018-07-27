package com.arvind.quark;

import android.util.Log;

import java.util.HashMap;

public class GlobalValues {
    private static final GlobalValues ourInstance = new GlobalValues();

    //Put Everything that you'll use throughout the app in here.

    private String phoneNumber;
    private String publicKey;
    private String privateKey;
    private String userName;
    private HashMap<String, String> contactMap;
    private String hostURL = "http://192.168.0.116:3000";

    public static GlobalValues getOurInstance() {
        return ourInstance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public HashMap<String, String> getContactMap() {
        return contactMap;
    }

    public void setContactMap(HashMap<String, String> contactMap) {
        this.contactMap = contactMap;
    }

    public String getHostURL() {
        return hostURL;
    }

    public void setHostURL(String hostURL) {
        this.hostURL = hostURL;
    }

    public GlobalValues(String phoneNumber, String publicKey, String userName) {
        this.phoneNumber = phoneNumber;
        this.publicKey = publicKey;
        this.userName = userName;
        String TAG = this.getClass().getName();
        Log.i(TAG, "GlobalValues Initilized");
        Log.i(TAG, "Phone Number: "+ phoneNumber);
        Log.i(TAG, "Public Key: "+ publicKey);
        Log.i(TAG, "Username: "+ userName);
    }

    public static GlobalValues getInstance() {
        return ourInstance;
    }

    private GlobalValues() {
    }
}
