package com.unir.calltrack;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unir.calltrack.database.DbContract;
import com.unir.calltrack.database.DbHelper;
import com.unir.calltrack.model.IncomingNumber;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<IncomingNumber> list = new ArrayList<>();
    private RecyclerAdapter recyclerAdapter;
    private BroadcastReceiver broadcastReceiver;
    private TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG}, 1);
        }

        recyclerView = findViewById(R.id.recyclerview);
        txtView = findViewById(R.id.textView);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        recyclerAdapter = new RecyclerAdapter(list);
        recyclerView.setAdapter(recyclerAdapter);

        readFromDb();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                readFromDb();
            }
        };

        recyclerAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {   // editar nome
                IncomingNumber item = list.get(position);
                EditText input = new EditText(MainActivity.this);
                input.setText(item.getName());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Editar nome")
                        .setView(input)
                        .setPositiveButton("Salvar", (d, w) -> {
                            DbHelper h = new DbHelper(MainActivity.this);
                            h.updateName(item.getId(), input.getText().toString(), h.getWritableDatabase());
                            h.close();
                            readFromDb();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onItemLongClick(int position) {   // excluir
                IncomingNumber item = list.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Excluir contato?")
                        .setPositiveButton("Excluir", (d, w) -> {
                            DbHelper h = new DbHelper(MainActivity.this);
                            h.deleteContact(item.getId(), h.getWritableDatabase());
                            h.close();
                            readFromDb();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
    }

    private void readFromDb() {
        list.clear();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readNumber(db);
        if (cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(DbContract.INCOMING_NUMBER));
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DbContract.CONTACT_NAME));
                @SuppressLint("Range") int qtdCalls = cursor.getInt(cursor.getColumnIndex(DbContract.NUMBER_OF_CALLS));

                list.add(new IncomingNumber(id, number, name, qtdCalls));
            }

            cursor.close();
            dbHelper.close();
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            cursor.close();
            dbHelper.close();
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.GONE);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        ContextCompat.registerReceiver(this, broadcastReceiver,
                new IntentFilter(DbContract.UPDATE_UI_FILTER), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}