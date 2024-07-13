package com.rodz.adimoshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class GeneralPurpose extends AppCompatActivity {
    SQLiteDatabase db;
    User user;
    Values values;
    LinearLayout main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);
        values = new Values(db);

        main= findViewById(R.id.main);

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        getSupportActionBar().setTitle(ucFirst(action));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (action){
            case "help":
                loadHelp();
                break;

            case "notifications":
                getNotifications();
                printNotifications();
                break;

            default:
                //nothing
        }
    }

    public void loadHelp(){
        WebView webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        main.addView(webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                if (url.contains("bookingComplete.php") || url.contains("success.php")){
                    // do nothing
                }
                else{ super.doUpdateVisitedHistory(view, url, isReload);}
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(user.link+"/mobile-help.php");
    }

    String ucFirst(String str){
        if (str.length() > 0){
            return str.substring(0,1).toUpperCase()+str.substring(1);
        }
        else{
            return str;
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

    public void getNotifications(){
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("getNotifications", user.id);
        params.put("type", values.get("user_type"));

        new Http(new HttpParams(this, Values.url, params){
            @Override
            public void onResponse(String text){
                try{
                    JSONArray array = new JSONArray(text);

                    for (int i = 0; i < array.length(); i++){
                        JSONObject row = array.getJSONObject(i);

                        Cursor check = db.rawQuery("SELECT * FROM notifications WHERE id = ? ", new String[]{row.getString("id")});
                        if (check.getCount() < 1) {
                            //save to database
                            db.execSQL("INSERT INTO notifications (id, webid, type, content, date, status, refer) VALUES (NULL, ?,?,?, ?,?,?)",
                                    new Object[]{row.getString("id"), row.getString("type"), row.getString("content"), row.getString("date"), "saved", ""});
                        }
                    }

                    printNotifications();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("Range")
    public void printNotifications(){
        main.removeAllViews();
        main.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        Cursor cursor = db.rawQuery("SELECT * FROM notifications ORDER BY id DESC LIMIT 20", null);

        while (cursor.moveToNext()){
            LinearLayout major = new LinearLayout(this);
            major.setOrientation(LinearLayout.VERTICAL);
            major.setPadding(0,dpToPx(5),0, dpToPx(5));
            main.addView(major);

            LinearLayout linearLayout1 = new LinearLayout(this);
            linearLayout1.setOrientation(LinearLayout.VERTICAL);
            linearLayout1.setBackgroundResource(cursor.getString(cursor.getColumnIndex("status")).equals("saved")?R.drawable.alert_secondary:R.drawable.alert_border);
            //linearLayout1.setPadding(dpToPx(0), dpToPx(12), dpToPx(0), dpToPx(12));
            linearLayout1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            major.addView(linearLayout1);

            TextView content = new TextView(this);
            content.setText(cursor.getString(cursor.getColumnIndex("content")));
            content.setTextColor(Color.BLACK);
            linearLayout1.addView(content);

            TextView date = new TextView(this);
            date.setText(cursor.getString(cursor.getColumnIndex("date")));
            linearLayout1.addView(date);
        }

        ContentValues cv = new ContentValues();
        cv.put("status", "read");
        db.update("notifications", cv, "id != ?", new String[]{"0"});
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}