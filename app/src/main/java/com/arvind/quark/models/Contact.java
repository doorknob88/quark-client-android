package com.arvind.quark.models;

public class Contact {
    final private String contactId;
    final private String displayName;
    final private String phoneNumber;
    private String publicAddress;
    private String userName;

    public Contact(String contactId, String displayName, String phoneNumber) {
        this.contactId = contactId;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getContactId() {
        return contactId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
