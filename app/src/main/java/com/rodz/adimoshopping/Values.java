package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Values {
    public static String url = "http://192.168.43.128/";
    public String link = "http://192.168.43.128/";
    public boolean status = false;
    public String unik;
    SQLiteDatabase db;

    @SuppressLint("Range")
    public Values(SQLiteDatabase db){
        this.db = db;

        Cursor c = db.rawQuery("SELECT * FROM user", null);
        if (c.getCount() > 0){
            c.moveToFirst();
            status = true;
            //unik = c.getString(c.getColumnIndex("unik"));
        }
    }

    @SuppressLint("Range")
    public String get(String key){
        String value = "";

        Cursor c = db.rawQuery("SELECT * FROM settings WHERE name = '"+key+"'", null);
        if (c.getCount() > 0){
            c.moveToFirst();
            value = c.getString(c.getColumnIndex("value"));
        }
        return value;
    }

    public void set(String key, String value){
        Cursor c = db.rawQuery("SELECT * FROM settings WHERE name = '"+key+"'", null);
        if (c.getCount() > 0){
            //update
            ContentValues cv = new ContentValues();
            cv.put("value", value);

            db.update("settings", cv, "name = ?", new String[]{key});
        }
        else{
            //insert
            db.execSQL("INSERT INTO settings (name, value) VALUES (?, ?)",
                    new Object[]{key, value});
        }
    }

    public static String reverseString(String str){
        String string = "";
        for(int i = str.length(); i > 0; i--){
            string += str.charAt((int)(i-1));
        }
        return string;
    }

    public static String numberFormat(String num){
        num= reverseString(num);

        String string = "";
        for (int i = 0; i < num.length(); i++){
            string += num.charAt(i);
            if ((i+1) % 3 == 0 && (i+1) != num.length()){
                string += ",";
            }
        }
        return reverseString(string);
    }
}

