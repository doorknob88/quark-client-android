package com.arvind.quark.models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;


public class ContactModel extends AndroidViewModel {
    /***
     * Warning: The contacts are shared, filtering them will trigger all observers
     */
    private final Contacts contacts;

    public ContactModel(Application application) {
        super(application);
        contacts = new Contacts(application);
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;
    }

    public void filter(String name) {
        contacts.filter(name);
    }
}
