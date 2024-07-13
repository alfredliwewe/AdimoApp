package com.rodz.adimoshopping;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydb.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, webid VARCHAR, name VARCHAR, views VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS subcategories (id INTEGER PRIMARY KEY AUTOINCREMENT, webid VARCHAR, name VARCHAR, category VARCHAR, parent VARCHAR, views VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, webid VARCHAR, category VARCHAR, subcategory VARCHAR, name VARCHAR, price VARCHAR, views VARCHAR, features VARCHAR, description VARCHAR, picture VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS user (id INTEGER PRIMARY KEY AUTOINCREMENT, webid VARCHAR, name VARCHAR, phone VARCHAR, email VARCHAR, type VARCHAR, file VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS settings (name VARCHAR, value VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS cart (id INTEGER primary key autoincrement, product VARCHAR, qty VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS displayImages (id INTEGER primary key autoincrement, webid VARCHAR, product VARCHAR, file VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS purchase (id INTEGER PRIMARY KEY AUTOINCREMENT, webid VARCHAR, product INTEGER, `key` TEXT, user TEXT, amount NUMERIC, time TEXT, status TEXT, approach TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, webid VARCHAR, type VARCHAR, content VARCHAR, date VARCHAR, status TEXT, refer VARCHAR)");
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}