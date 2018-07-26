package com.arvind.quark;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.arvind.quark.models.Contact;
import com.arvind.quark.models.ContactModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SendActivity extends AppCompatActivity {
    private LiveData<List<Contact>> contactsLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ContactModel contactModel = ViewModelProviders.of(this).get(ContactModel.class);
        contactsLiveData = contactModel.getContacts();

        contactsLiveData.observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> contacts) {
                final List<Contact> registeredContacts = contacts == null
                                                         ? new ArrayList<Contact>()
                                                         : filterNotRegistered(contacts);
            }
        });
    }

    private List<Contact> filterNotRegistered(List<Contact> contacts) {
        final List<Contact> filtered = new ArrayList<>();

        final HashSet<String> registedContactIds = new HashSet<>();  // TODO: Retrieve from backend

        for (Contact contact : contacts) {
            if (registedContactIds.contains(contact.getContactId())) {
                filtered.add(contact);
            }
        }

        return filtered;
    }
}
