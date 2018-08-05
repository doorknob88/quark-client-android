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
import android.renderscript.ScriptGroup;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.Toast;

import com.arvind.quark.models.Contact;
import com.arvind.quark.models.ContactModel;
import com.arvind.quark.util.NanoUtil;
import com.arvind.quark.util.NumberUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
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
    EditText sendAmount;
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

                final Contact selectedContact = globalValues.getMatchedContacts().get(tempLong.intValue());


                AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
                LayoutInflater inflater = SendActivity.this.getLayoutInflater();

                final EditText input = new EditText(getApplicationContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                input.setLayoutParams(lp);

                builder.setView(input); // uncomment this line
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });

                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = input.getText().toString();

                        BigDecimal power = new BigDecimal("10").pow(24);
                        BigInteger balance = new BigInteger(globalValues.getBalance());
                        BigInteger amountTemp = new BigDecimal(text).multiply(power).toBigIntegerExact();

                        Log.i(TAG, "Balance: " + balance.toString());
                        Log.i(TAG, "amount " + amountTemp.toString());

                        if (amountTemp.compareTo(balance) == -1){

                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = null;
                            toast = Toast.makeText(getApplicationContext(), "Insufficient Funds or out of sync. Try again.", duration);
                            toast.show();
                            return ;
                        }else {
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = null;
                            toast = Toast.makeText(getApplicationContext(), "Sending...", duration);
                            toast.show();

                            Operations operations = new Operations();

                            try {
                                operations.send(text, selectedContact.getPublicAddress(), getApplicationContext());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });

                builder.setTitle("Send to " + selectedContact.getDisplayName());
                builder.setMessage("Balance: " + NumberUtil.getRawAsUsableString(globalValues.getBalance()));
                builder.show();
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
