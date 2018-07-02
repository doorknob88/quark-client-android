package com.arvind.quark.models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.HashMap;
import java.util.Map;


public class ContactModel extends AndroidViewModel {

    private class PhoneNumbers extends LiveData<Map<String, String>> {
        private final Context context;

        private final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        private final String[] projections = {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };
        private final String sortBy = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";

        private PhoneNumbers(Context context) {
            this.context = context;
            load();
        }

        private void load() {
            new AsyncTask<Void, Void, Map<String, String>>() {
                @Override
                protected Map<String, String> doInBackground(Void... voids) {
                    Cursor cursor = context.getContentResolver().query(uri,
                                                                       projections,
                                                                       null,
                                                                       null,
                                                                       sortBy);

                    Map<String, String> phoneNumbers = new HashMap<>();
                    while (cursor.moveToNext()) {
                        phoneNumbers.put(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                         cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)));
                    }
                    return phoneNumbers;
                }

                @Override
                protected void onPostExecute(Map<String, String> phoneNumbers) {
                    setValue(phoneNumbers);
                }
            }.execute();
        }
    }

    private final PhoneNumbers phoneNumbers;

    public ContactModel(Application application) {
        super(application);
        phoneNumbers = new PhoneNumbers(application);
    }

    public LiveData<Map<String, String>> getPhoneNumbers() {
        return phoneNumbers;
    }
}