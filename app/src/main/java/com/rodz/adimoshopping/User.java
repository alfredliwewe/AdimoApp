package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.database.sqlite.*;
import android.database.*;
import android.content.ContentValues;

public class User
{
	SQLiteDatabase db;
	public String id = "0";
	public String name  = "";
	public String phone = "";
	public String email = "";
	public String type = "";
	public String picture = "";
	public boolean status = false;
	public String link = "http://192.168.43.128/adimo/";
	
	@SuppressLint("Range")
	public User(SQLiteDatabase conn){
		db = conn;
		Cursor c = db.rawQuery("SELECT * FROM user", null);
		while(c.moveToNext()){
			id = c.getString(c.getColumnIndex("webid"));
			name = c.getString(c.getColumnIndex("name"));
			phone = c.getString(c.getColumnIndex("phone"));
			email = c.getString(c.getColumnIndex("email"));
			type = c.getString(c.getColumnIndex("type"));
			picture = c.getString(c.getColumnIndex("file"));
			status = true;
		}
		c.close();

		Cursor d = db.rawQuery("SELECT * FROM settings", null);
		while(d.moveToNext()){
			switch(d.getString(d.getColumnIndex("name"))){
				case "link":
					link = d.getString(d.getColumnIndex("value"));
					break;
			}
		}
		d.close();
	}
	
	public void setName(String name){
		ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        db.update("user", contentValues, "id != ?", new String[]{"0"});
	}

	public void setPicture(String picture){
		ContentValues contentValues = new ContentValues();
		contentValues.put("picture", picture);
		db.update("user", contentValues, "id != ?", new String[]{"0"});
	}

	public void setPhone(String phone){
		ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        db.update("user", contentValues, "id != ?", new String[]{"0"});
	}

	public void setEmail(String email){
		ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        db.update("user", contentValues, "id != ?", new String[]{"0"});
	}

	public void setLink(String link){
		db.delete("settings", "name = ?", new String[]{"link"});
		db.execSQL("INSERT INTO settings (name, value) VALUES (?, ?)", new String[]{"link", link});
	}
}