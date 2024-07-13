package com.rodz.adimoshopping;

import android.app.Activity;
import android.os.Bundle;
import java.util.*;
import android.database.sqlite.*;
import android.database.*;
import android.content.Intent;
import android.view.View;
import com.rodz.adimoshopping.Response.*;
import android.widget.*;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase db;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first);
        getSupportActionBar().hide();

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);
        downloadData();
        startPage();
    }

    public void startPage(){
        Thread thread = new Thread(new Runnable(){
            public void run(){
                try{
                    Thread.sleep(2000);

                    runOnUiThread(new Runnable(){
                        public void run(){
                            Cursor c = db.rawQuery("SELECT * FROM categories", null);
                            if (c.getCount() > 0) {
                                Intent intent = new Intent(MainActivity.this, Home.class);
                                MainActivity.this.startActivity(intent);
                            }
                            else{
                                /*TextView report = (TextView)findViewById(R.id.report);
                                report.setText("Unable to start :)");
                                report.setTextColor(Color.RED);*/
                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                alert.setTitle("Can't start");
                                alert.setMessage("Check your connection and restart this app");
                                
                                alert.setNegativeButton((CharSequence) "Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //MainActivity.this.finish();
                                        dialogInterface.cancel();
                                    }
                                });
                                alert.show();
                            }
                        }
                    });
                }
                catch(Exception x){

                }
            }
        });

        thread.start();
    }

    public void downloadData(){
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("downloadData", "true");

        new Http(new HttpParams(this, user.link+"/api.php", params){
            @Override
            public void onResponse(String text){
                //save to database
                try{
                    JSONObject res = new JSONObject(text);

                    JSONArray categories = res.optJSONArray("categories");

                    for(int i = 0; i < categories.length(); i++){
                        JSONObject row = categories.getJSONObject(i);
                        if (row != null) {
                            Cursor c = db.rawQuery("SELECT * FROM categories WHERE webid = '"+row.getString("id")+"'", null);
                            if (c.getCount() < 1) {
                                db.execSQL("INSERT INTO categories (id, webid, name, views) VALUES(NULL, ?, ?, ?)",
                                        new String[]{row.getString("id"), row.getString("name"), "0"});
                            }
                        }
                    }


                    JSONArray subcategories = res.optJSONArray("subcategories");

                    for(int i = 0; i < subcategories.length(); i++){
                        JSONObject row = subcategories.getJSONObject(i);
                        if (row != null) {
                            Cursor c = db.rawQuery("SELECT * FROM subcategories WHERE webid = '"+row.getString("id")+"'", null);
                            if (c.getCount() < 1) {
                                db.execSQL("INSERT INTO subcategories (id, webid, name, category, parent, views) VALUES(NULL, ?, ?, ?, ?, ?)",
                                        new String[]{row.getString("id"), row.getString("name"), row.getString("category"), row.getString("parent"), "0"});
                            }
                        }
                    }


                    JSONArray products = res.optJSONArray("products");

                    for(int i = 0; i < products.length(); i++){
                        JSONObject row = products.getJSONObject(i);
                        if (row != null) {
                            Cursor c = db.rawQuery("SELECT * FROM products WHERE webid = '"+row.getString("id")+"'", null);
                            if (c.getCount() < 1) {
                                db.execSQL("INSERT INTO products (id, webid, category, subcategory, name, price, views, features, description, picture) VALUES(NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                        new String[]{row.getString("id"), row.getString("category"), row.getString("subcategory"), row.getString("name"), row.getString("price"), row.getString("views"), row.getString("features"), row.getString("description"), row.getString("resampled")});
                            }
                        }
                    }
                }
                catch(Exception ex){
                    System.out.println(text);
                    ex.printStackTrace();
                }
            }
        });
    }

    public void next(View v){
        startActivity(new Intent(MainActivity.this, Home.class));
    }

    public void settings(View v){
        //update web address
        final User user = new User(db);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Web Address");
        builder.setMessage("Update the URL address to redirect HTTP requests to");

        final EditText input = new EditText(this);
        input.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        input.setPadding(10,20,10,20);
        input.setText(user.link);

        final LinearLayout display = new LinearLayout(this);
        display.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        display.setOrientation(LinearLayout.VERTICAL);
        display.setPadding(50,2,50,2);
        display.addView(input);
        builder.setView(display);

        builder.setPositiveButton((CharSequence)"Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.setLink(input.getText().toString());
                Toast.makeText(MainActivity.this, "Link is now updated", Toast.LENGTH_LONG).show();
                dialog.cancel();
                downloadData();
            }
        });
        builder.setNegativeButton((CharSequence)"Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}