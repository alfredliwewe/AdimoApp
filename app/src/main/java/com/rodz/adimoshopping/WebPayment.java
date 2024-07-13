package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.database.sqlite.*;
import android.database.*;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*; 
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebPayment extends AppCompatActivity {

    private WebView webView;
    SQLiteDatabase db;
    String ids = "";
    Integer total = 0;
    User user;
    @SuppressLint({"Range", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);
        Intent intent = getIntent();
        final String mode = intent.getStringExtra("mode");

        if (Build.VERSION.SDK_INT >= 21) {
            //change color of the activity
            getWindow().setNavigationBarColor(Color.parseColor(getString(R.color.colorAccent)));
            getWindow().setStatusBarColor(Color.parseColor(getString(R.color.colorAccent)));
        }
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        }

        //getting total amount
        Cursor c = db.rawQuery("SELECT *, products.name AS product_name, cart.qty AS cqty FROM cart JOIN products ON cart.product = products.webid JOIN categories ON products.category = categories.webid", null);
        
        
        if (c.getCount() > 0) {
            try {
                JSONArray add = new JSONArray();
                while (c.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    obj.put("qty", c.getInt(c.getColumnIndex("cqty")));
                    obj.put("product",  c.getString(c.getColumnIndex("product")));
                    add.put(obj);
                    total += (c.getInt(c.getColumnIndex("cqty")) * c.getInt(c.getColumnIndex("price")));
                    if (ids.equals("")) {
                        ids = c.getString(c.getColumnIndex("product"));
                    } else {
                        ids += "," + c.getString(c.getColumnIndex("product"));
                    }
                }

                JSONObject p = new JSONObject();
                p.put("user", user.id);
                p.put("cart", add);
                p.put("mode", mode);
                String json = p.toString();

                Http.JSONPost(new Http.JSONPostParams(WebPayment.this, user.link+"/app/startPayment.php", json){
                    @Override
                    public void onResponse(String text){
                        //Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject obj = new JSONObject(text);
                            webView.loadUrl(user.link+obj.getString("link"));
                        }
                        catch (Exception x){
                            System.out.println(text);
                            x.printStackTrace();
                        }
                    }
                });
            }
            catch(Exception exception){
                exception.printStackTrace();
            }
        }


        webView = (WebView)findViewById(R.id.web);


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
                    Intent resultIntent = new Intent();
                    // TODO Add extras or a data URI to this intent as appropriate.
                    //get trans id
                    db.delete("cart", "id != ?", new String[]{"0"});
                    setResult(Activity.RESULT_OK, resultIntent);
                    WebPayment.this.finish();
                }
                else{ super.doUpdateVisitedHistory(view, url, isReload);}
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        //User user = new User(db);
        //webView.loadUrl(user.link+"/pay.php?amount="+total+"&user="+user.id+"&ids="+ids+"&mode="+mode+"&approach=app");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        try{
            getMenuInflater().inflate(R.menu.finish, menu);
            return true;
        }
        catch(Exception f){
            //Toast.makeText(this, f.toString(), Toast.LENGTH_LONG).show();
            f.printStackTrace();
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.finish:
                Toast.makeText(this, "Check last payment", Toast.LENGTH_LONG).show();
                checkLast();
                return true;

            case R.id.search:
                startActivity(new Intent(this, Search.class));
                return true;

            default:
                this.finish();
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
    }

    public void checkLast(){
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("checkLastVisa", user.id);

        new Http(new HttpParams(this, user.link+"/api.php", params){
            @Override
            public void onResponse(String text){
                Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}