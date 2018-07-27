package com.arvind.quark.models;

public class Contact {
    final private String contactId;
    final private String displayName;
    final private String phoneNumber;

    public Contact(String contactId, String displayName, String phoneNumber) {
        this.contactId = contactId;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Contact(id=" + contactId + ", displayName=" + displayName + ", phoneNumber=" + phoneNumber + ")";
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
}
