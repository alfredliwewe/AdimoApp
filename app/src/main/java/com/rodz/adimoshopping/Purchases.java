package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedHashMap;
import java.util.Map;

public class Purchases extends AppCompatActivity {
    SQLiteDatabase db;
    User user;
    LinearLayout main;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);
        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS purchase (id INTEGER PRIMARY KEY AUTOINCREMENT, webid VARCHAR, product INTEGER, `key` TEXT, user TEXT, amount NUMERIC, time TEXT, status TEXT, approach TEXT)");
        user = new User(db);

        getSupportActionBar().setTitle("Purchases");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= 21) {
            //change color of the activity
            getWindow().setNavigationBarColor(Color.parseColor(getString(R.color.colorAccent)));
            getWindow().setStatusBarColor(Color.parseColor(getString(R.color.colorAccent)));
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        }

        main = (LinearLayout)findViewById(R.id.main);

        downloadPurchases();
        printPurchases();
    }

    public void downloadPurchases(){
        //do download
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("downloadPurchases", user.id);

        new Http(new HttpParams(this, user.link+"/api.php", params){
            @Override
            public void onResponse(String text){
                try{
                    Response res = new Response(text);
                    if (res.status){
                        //save the bookings
                        for(int i = 0; i < res.rows; i++){
                            Response.Row row = res.getRow(i);

                            Cursor c = db.rawQuery("SELECT * FROM purchase WHERE webid = '"+row.getData("id")+"'", null);
                            if(c.getCount() < 1){
                                //insert
                                db.execSQL("INSERT INTO purchase (id, webid, product, `key`, user, amount, time, status, approach) VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{row.getData("id"), row.getData("product"), row.getData("key"), row.getData("user"), row.getData("amount"), row.getData("date"), row.getData("status"), row.getData("approach")});
                            }
                        }

                        printPurchases();
                    }
                    else{
                        Toast.makeText(Purchases.this, text, Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception ex1){
                    Toast.makeText(Purchases.this, ex1.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @SuppressLint("Range")
    public void printPurchases(){
        //do print
        main.removeAllViews();

        main.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        Cursor c = db.rawQuery("SELECT * FROM purchase JOIN products ON purchase.product = products.webid", null);
        //Toast.makeText(this, "Found "+c.getCount(), Toast.LENGTH_LONG).show();
        while(c.moveToNext()){
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackgroundResource(R.drawable.purchase_row);
            row.setPadding(dpToPx(15), dpToPx(10), dpToPx(15), dpToPx(10));

            TextView head = new TextView(this);
            head.setText(c.getString(c.getColumnIndex("name")));
            head.setTextColor(Color.parseColor("#212529"));
            head.setTypeface(head.getTypeface(), Typeface.BOLD);
            row.addView(head);

            TextView amount = new TextView(this);
            amount.setText("Amount: MWK"+c.getString(c.getColumnIndex("amount")));
            amount.setTextColor(Color.BLACK);
            row.addView(amount);

            TextView date = new TextView(this);
            date.setText("Date: "+c.getString(c.getColumnIndex("time"))+"\n");
            date.setTextColor(Color.parseColor("#212529"));
            date.setTypeface(head.getTypeface(), Typeface.ITALIC);
            row.addView(date);

            View line = new View(this);
            line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(7)));
            //line.setBackgroundColor(Color.GRAY);
            main.addView(line);

            main.addView(row);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            default:
                this.finish();
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
