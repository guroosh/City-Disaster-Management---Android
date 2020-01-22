package com.example.androidase_.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BaseDataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyDBName.db";

    public BaseDataHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int numberOfRows(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, tableName);
    }

    public boolean insertRow(String tableName, String id, HashMap<String, String> rowData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        for (Map.Entry<String, String> entry : rowData.entrySet()) {
            contentValues.put(entry.getKey(), entry.getValue());
        }
        db.insert(tableName, null, contentValues);
        return true;
    }

    public boolean updateRow(String tableName, String id, HashMap<String, String> rowData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, String> entry : rowData.entrySet()) {
            contentValues.put(entry.getKey(), entry.getValue());
        }
        db.update(tableName, contentValues, "id = ? ", new String[]{id});
        return true;
    }

    public int deleteRow(String tableName, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName, "id = ? ", new String[]{id});
    }

    public ArrayList<HashMap<String, String>> getAllRows(String tableName) {
        ArrayList<HashMap<String, String>> allRows = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + tableName, null);
        String[] columnNames = res.getColumnNames();
        res.moveToFirst();
        while (!res.isAfterLast()) {
            HashMap<String, String> row = new HashMap<>();
            for (String columnName : columnNames) {
                row.put(columnName, res.getString(res.getColumnIndex(columnName)));
            }
            res.moveToNext();
            allRows.add(row);
        }
        res.close();
        return allRows;
    }

    public HashMap<String, String> getRow(String tableName, String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + tableName + " where id=\"" + id + "\"", null);
        String[] columnNames = res.getColumnNames();

        res.moveToFirst();
        HashMap<String, String> row = new HashMap<>();
        if (res.getCount() == 1) {
            for (String columnName : columnNames) {
                row.put(columnName, res.getString(res.getColumnIndex(columnName)));
            }
        }
        res.close();
        return row;
    }

    public boolean createTable(String tableName, ArrayList<String> columnNames) {
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder query = new StringBuilder("create table if not exists " + tableName + "(id text primary key, ");
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            if (i != columnNames.size() - 1) {
                query.append(columnName).append(" text, ");
            } else {
                query.append(columnName).append(" text)");
            }
        }
        db.execSQL(query.toString());
        return true;
    }
}
