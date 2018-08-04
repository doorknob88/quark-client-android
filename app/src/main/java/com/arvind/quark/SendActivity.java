package com.arvind.quark;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.arvind.quark.models.Contact;
import com.arvind.quark.models.ContactModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SendActivity extends AppCompatActivity {
    private LiveData<List<Contact>> contactsLiveData;
    GlobalValues globalValues;
    String TAG = this.getClass().getName();
    EditText editText;
    ListView listView;
    TextView noContactFoundTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        globalValues = GlobalValues.getInstance();

        editText = findViewById(R.id.search);
        listView = findViewById(R.id.search_result_list);
        noContactFoundTextView = findViewById(R.id.no_contacts_found);

        String[] values = new String[globalValues.getMatchedContacts().size()];

        for (int i = 0; i < globalValues.getMatchedContacts().size(); i++){
            values[i] = globalValues.getMatchedContacts().get(i).getDisplayName();
        }

        ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(this,
                R.layout.contact_list_item, R.id.contact_display_name, globalValues.getMatchedContacts());

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long tempLong = id;

                Contact selectedContact = globalValues.getMatchedContacts().get(tempLong.intValue());

                AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
                LayoutInflater inflater = SendActivity.this.getLayoutInflater();

                builder.setView(inflater.inflate(R.layout.dialog_send, null))
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // sign in the user ...
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).setTitle("Send to: "+globalValues.getMatchedContacts().get(tempLong.intValue()).getDisplayName());

                AlertDialog dialog = builder.create();

                dialog.show();


            }
        });

        if (globalValues.getMatchedContacts().size() > 0){
            //noContactFoundTextView.setVisibility(View.INVISIBLE);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = editText.getText().toString();
                for (int i = 0; i < globalValues.getMatchedContacts().size(); i++){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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

        // TODO: Add handler for input events to search bar that calls
        // contactsLiveData.filter(partialContactName)
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
