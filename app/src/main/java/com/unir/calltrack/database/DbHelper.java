package com.unir.calltrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chamadas.db";
    private static final int DATABASE_VERSION = 2;

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DbContract.TABLE_NAME +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbContract.INCOMING_NUMBER + " text," +
                DbContract.CONTACT_NAME + " text," +
                DbContract.NUMBER_OF_CALLS + " integer);";

        try {
            db.execSQL(sql);
            Log.i("DB", "Tabela criada com sucesso");
        } catch (Exception e) {
            Log.i("DB", "Erro ao criar tabela");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + DbContract.TABLE_NAME + ";";

        try {
            db.execSQL(sql);
            onCreate(db);
            Log.i("DB", "Tabela atualizada com sucesso");
        } catch (Exception e) {
            Log.i("DB", "Erro ao atualizar tabela");
        }
    }

    public void addNumber (String number, SQLiteDatabase db){
        if (exists(number, db)) {
            db.execSQL("UPDATE " + DbContract.TABLE_NAME + " SET " + DbContract.NUMBER_OF_CALLS +
                            " = " + DbContract.NUMBER_OF_CALLS + " + 1 WHERE " + DbContract.INCOMING_NUMBER + " = ?",
                    new String[]{number});
        } else {
            ContentValues values = new ContentValues();
            values.put(DbContract.INCOMING_NUMBER, number);
            values.put(DbContract.CONTACT_NAME, "Desconhecido");
            values.put(DbContract.NUMBER_OF_CALLS, 1);
            db.insert(DbContract.TABLE_NAME, null, values);
        }
    }

    public Cursor readNumber (SQLiteDatabase db) {
        String [] projection = {
                "id",
                DbContract.INCOMING_NUMBER,
                DbContract.CONTACT_NAME,
                DbContract.NUMBER_OF_CALLS
        };
        return db.query(
                DbContract.TABLE_NAME,
                projection,
                null, null, null, null, null
        );
    }

    public boolean exists(String number, SQLiteDatabase db) {
        Cursor c = db.query(DbContract.TABLE_NAME, new String[]{"id"},
                DbContract.INCOMING_NUMBER + "=?", new String[]{number}, null, null, null);
        boolean existe = c.moveToFirst();
        c.close();
        return existe;
    }

    public int updateName (int id, String name, SQLiteDatabase db) {
        ContentValues v = new ContentValues();
        v.put(DbContract.CONTACT_NAME, name);
        return db.update(DbContract.TABLE_NAME, v, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteContact(int id, SQLiteDatabase db) {
        return db.delete(DbContract.TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
    }
}
