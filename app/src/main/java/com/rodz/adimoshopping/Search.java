package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*; 
import android.database.sqlite.*;
import android.database.*;
import java.util.*;
import android.graphics.*;
import android.view.*;
import android.view.View;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import java.io.*;
import android.content.ContextWrapper;
import android.content.Context;
import android.util.Base64;
import android.util.Base64.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.appcompat.app.AppCompatActivity;

public class Search extends AppCompatActivity {
    SQLiteDatabase db;
    LinearLayout main;
    String webid;
    int screenWidth;
    EditText input;
    ContextWrapper cw;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        getSupportActionBar().hide();

        main = (LinearLayout)findViewById(R.id.main);
        input = (EditText)findViewById(R.id.input);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search(input);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        input.requestFocus();
    }

    @SuppressLint({"Range", "ResourceType"})
    public void search(View v){
        String text = input.getText().toString();
        main.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        main.setBackgroundColor(Color.parseColor("#eeeeee"));
        main.removeAllViews();

        TextView report = new TextView(this);
        report.setText("Search results for \""+text+"\"");
        report.setTextColor(Color.BLACK);
        report.setPadding(0, dpToPx(10), 0, dpToPx(10));
        report.setTypeface(report.getTypeface(), Typeface.BOLD);
        main.addView(report);

        Cursor c = db.rawQuery("SELECT *, products.name AS product_name, products.webid AS product_id FROM products JOIN categories ON products.category = categories.webid WHERE products.name LIKE '%"+text+"%' LIMIT 12", null);
        while(c.moveToNext()){
            LinearLayout major = new LinearLayout(this);
            major.setPadding(0, dpToPx(10), 0, dpToPx(10));
            major.setOrientation(LinearLayout.VERTICAL);

            LinearLayout row = new LinearLayout(this);
            row.setBackgroundResource(R.drawable.row);
            row.setOrientation(LinearLayout.HORIZONTAL);
            major.addView(row);

            LinearLayout left = new LinearLayout(this);
            left.setGravity(Gravity.CENTER);
            left.setOrientation(LinearLayout.VERTICAL);
            left.setLayoutParams(new LayoutParams((int)(0.3 * screenWidth), LayoutParams.WRAP_CONTENT));

            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.thumbnail);
            @SuppressLint("Range") Bitmap bitmap = loadImageFromStorage(path, c.getString(c.getColumnIndex("picture")));
            if (bitmap == null) {
                //downloadImage(p.getString(p.getColumnIndex("picture")), iv);
            }
            else{
                try{
                    iv.setImageBitmap(bitmap);
                    iv.setLayoutParams(new LayoutParams((int)(0.2 * screenWidth), (int)(0.2 * screenWidth)));
                }
                catch(Exception ff){
                    Toast.makeText(this, ff.toString(), Toast.LENGTH_LONG).show();
                }
            }
            iv.setLayoutParams(new LayoutParams((int)(0.2 * screenWidth), (int)(0.2 * screenWidth)));
            left.addView(iv);
            row.addView(left);

            LinearLayout right = new LinearLayout(this);
            right.setGravity(Gravity.CENTER);
            right.setOrientation(LinearLayout.VERTICAL);
            right.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            TextView product = new TextView(this);
            product.setText(c.getString(c.getColumnIndex("product_name")));
            product.setTextColor(Color.BLACK);
            product.setTextSize(17.0f);
            right.addView(product);

            TextView price = new TextView(this);
            price.setText("MWK"+c.getString(c.getColumnIndex("price")));
            price.setTextColor(Color.BLACK);
            right.addView(price);

            TextView shop = new TextView(this);
            shop.setText(c.getString(c.getColumnIndex("name")));
            shop.setTypeface(product.getTypeface(), Typeface.BOLD);
            shop.setTextColor(Color.parseColor(getString(R.color.blue)));
            right.addView(shop);
            row.addView(right);
            final String product_id = c.getString(c.getColumnIndex("product_id"));
            row.setOnClickListener(new View.OnClickListener(){
                public void onClick(View p1){
                    Intent intent = new Intent(Search.this, Details.class);
                    intent.putExtra("id", product_id);
                    Search.this.startActivity(intent);
                }
            });

            main.addView(major);
        }
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void downloadImage(String filename, ImageView myImage){
        //proceed to download image
        //Toast.makeText(MainActivity.this, "Image saved successfully", Toast.LENGTH_LONG).show();
        final String fname = filename;
        final ImageView iv = myImage;
        final User user = new User(db);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("file", fname);

        new Http(new HttpParams(Search.this, user.link+"/api.php", params){
            @Override
            public void onResponse(String response){
                try{
                    byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    iv.setImageBitmap(decodedByte);
                    saveToInternalStorage(decodedByte, fname);

                    String height = decodedByte.getHeight()+"";
                    String width = decodedByte.getWidth()+"";
                    Double ratio = Double.parseDouble(height) / Double.parseDouble(width);
                    LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)iv.getLayoutParams();
                    params2.height = (int)(ratio * 0.25 * screenWidth);
                    params2.width = (int)(0.25 * screenWidth);
                    iv.setLayoutParams(params2);
                }
                catch(Exception alf){
                    Toast.makeText(Search.this, alf.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public String saveToInternalStorage(Bitmap bitmapImage, String filename){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(directory, filename);

        FileOutputStream fos = null;

        try{
            fos = new FileOutputStream(myPath);

            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                fos.close();
            }
            catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public Bitmap loadImageFromStorage(String path, String filename){
        try{
            File f = new File(path, filename);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch(Exception ee){
            return null;
        }
    }

    public void openScanner(View view){
        startActivity(new Intent(this, ScanActivity.class));
    }

    public void clearMe(View view) {
        if (input.getText().toString().length() > 0){
            input.getText().clear();
            input.requestFocus();
        }
        else{
            finish();
        }
    }
}