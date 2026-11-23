package com.unir.calltrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Telephony;
import android.telephony.TelephonyManager;

import com.unir.calltrack.database.DbContract;
import com.unir.calltrack.database.DbHelper;

public class NumberReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE); // recupera estado do telefone
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (number != null) {
                DbHelper dbHelper = new DbHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                dbHelper.addNumber(number, db);
                dbHelper.close();
            }
        }
        Intent intent1 = new Intent(DbContract.UPDATE_UI_FILTER);
        intent1.setPackage(context.getPackageName());
        context.sendBroadcast(intent1);
    }
}