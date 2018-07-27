package com.arvind.quark.models;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Contacts extends LiveData<List<Contact>> {
    private final Context context;

    public Contacts(Context context) {
        this.context = context;
    }

    public void filter(final String name) {
        new AsyncTask<Void, Void, List<Contact>>() {
            @Override
            protected List<Contact> doInBackground(Void... voids) {

                // Get contact id
                final Uri contactLookupUrl = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                                                                  Uri.encode(name));

                final String[] contactLookupProjections = {
                    ContactsContract.Contacts._ID
                };

                final String contactLookupSelection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";

                final Cursor contactLookupCursor = context.getContentResolver().query(contactLookupUrl,
                                                                                      contactLookupProjections,
                                                                                      contactLookupSelection,
                                                                                      null,
                                                                                      null);

                List<String> contactIds = new ArrayList<>();
                while (contactLookupCursor.moveToNext()) {
                    contactIds.add(contactLookupCursor.getString(contactLookupCursor.getColumnIndex(ContactsContract.Contacts._ID)));
                }

                // Get contact data
                final Uri contactEntityUrl = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                                                                  ContactsContract.Contacts.Entity.CONTENT_DIRECTORY);

                final String[] contactEntityProjections = {
                    ContactsContract.Contacts.Entity.CONTACT_ID,
                    ContactsContract.Contacts.Entity.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                };
                final String contactEntitySelection = ContactsContract.Contacts.Entity.CONTACT_ID
                                                          + " IN ("
                                                          + android.text.TextUtils.join(", ", Collections.nCopies(contactIds.size(), "?"))
                                                          + ") AND "
                                                          + ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " IS NOT NULL";
                final String[] contactEntitySelectionArgs = contactIds.toArray(new String[contactIds.size()]);
                final String sortBy = ContactsContract.Contacts.Entity.DISPLAY_NAME + " ASC";

                Cursor contactEntityCursor = context.getContentResolver().query(contactEntityUrl,
                                                                                contactEntityProjections,
                                                                                contactEntitySelection,
                                                                                contactEntitySelectionArgs,
                                                                                sortBy);

                List<Contact> contacts = new ArrayList<>();
                while (contactEntityCursor.moveToNext()) {
                    contacts.add(new Contact(
                        contactEntityCursor.getString(contactEntityCursor.getColumnIndex(ContactsContract.Contacts.Entity.CONTACT_ID)),
                        contactEntityCursor.getString(contactEntityCursor.getColumnIndex(ContactsContract.Contacts.Entity.DISPLAY_NAME)),
                        contactEntityCursor.getString(contactEntityCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
                    ));
                }
                return contacts;
            }

            @Override
            protected void onPostExecute(List<Contact> contacts) {
                setValue(contacts);
            }
        }.execute();
    }
}
