package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
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
import android.view.ViewGroup.LayoutParams;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.content.ContextWrapper;
import java.io.*;
import android.util.Base64;
import android.util.Base64.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.*;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

public class Category extends AppCompatActivity {
    LinearLayout main;
    SQLiteDatabase db;
    String webid;
    int screenWidth;
    int thirty;
    int twenty;
    int twentyfive;
    ContextWrapper cw;
    String path;
    LinearLayout lg;
    LinearLayout productsContainer;
    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);
        LinearLayout main = (LinearLayout)findViewById(R.id.main);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        thirty = (int)(0.3 * screenWidth);
        twenty = (int)(0.2 * screenWidth);
        twentyfive = (int)(0.25 * screenWidth);

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        Intent intent = getIntent();
        webid = intent.getStringExtra("category");

        Cursor c = db.rawQuery("SELECT * FROM categories WHERE id = '"+webid+"'", null);
        if (c.getCount() > 0) {
            c.moveToFirst();

            getSupportActionBar().setTitle(c.getString(c.getColumnIndex("name")));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            lg = new LinearLayout(this);
            lg.setPadding(dpToPx(5), dpToPx(15), dpToPx(5), dpToPx(15));
            lg.setOrientation(LinearLayout.VERTICAL);
            main.addView(lg);

            printCategory(Integer.parseInt(webid));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        try{
            getMenuInflater().inflate(R.menu.search, menu);
            return true;
        }
        catch(Exception f){
            Toast.makeText(this, f.toString(), Toast.LENGTH_LONG).show();
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                startActivity(new Intent(this, Search.class));
                return true;

            default:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("Range")
    public void printCategory(int id){
        try{
            lg.removeAllViews();

            LinearLayout dd = new LinearLayout(this);
            dd.setOrientation(LinearLayout.HORIZONTAL);
            dd.setPadding(dpToPx(5), dpToPx(15), dpToPx(5), dpToPx(15));

            //add the filter widgets
            Spinner subs = new Spinner(this);
            subs.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
            subs.setBackgroundResource(R.drawable.border);
            ArrayList<String> subcategories = new ArrayList<>();
            Cursor read = db.rawQuery("SELECT * FROM subcategories WHERE category = '"+id+"'", null);
            while(read.moveToNext()){
                subcategories.add(read.getString(read.getColumnIndex("name")));
            }
            ArrayAdapter aa1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, subcategories);
            aa1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subs.setAdapter(aa1);
            subs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try{
                        TextView item = (TextView) view;
                        Toast.makeText(Category.this, item.getText().toString(), Toast.LENGTH_LONG).show();
                    }
                    catch(Exception ee){
                        Toast.makeText(Category.this, ee.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            //subs.setTextColor(Color.parseColor("##155724"));
            dd.addView(subs);


            Spinner sort = new Spinner(this);
            sort.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
            sort.setBackgroundResource(R.drawable.border);
            String[] mnth = new String[]{"Cheaper", "Popular", "Most purchased"};
            ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mnth);
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sort.setAdapter(aa);
            sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView item = (TextView) view;
                    Cursor d = null;
                    switch (item.getText().toString()){
                        case "Cheaper":
                            d = db.rawQuery("SELECT * FROM products WHERE category = '"+id+"' ORDER BY price ASC", null);
                            break;

                        case "Popular":
                            d = db.rawQuery("SELECT * FROM products WHERE category = '"+id+"' ORDER BY views DESC", null);
                            break;

                        case "Most purchased":
                            d = db.rawQuery("SELECT * FROM products WHERE category = '"+id+"' ORDER BY id DESC", null);
                            break;
                    }

                    if (d.getCount() > 0){
                        productsContainer.removeAllViews();
                        while (d.moveToNext()) {
                            LinearLayout major = new LinearLayout(Category.this);
                            //registerForContextMenu(major);
                            major.setPadding(6, 2, 6, 2);
                            major.setOrientation(LinearLayout.HORIZONTAL);

                            LinearLayout imgContainer = new LinearLayout(Category.this);
                            ShapeableImageView houseImage = new ShapeableImageView(new ContextThemeWrapper((Context) Category.this, R.style.rounded_home), null, 0);
                            houseImage.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
                            Gallery.LayoutParams params = new Gallery.LayoutParams(twentyfive, twentyfive);
                            houseImage.setImageResource(R.drawable.thumbnail);
                            Bitmap bitmap = loadImageFromStorage(path, d.getString(d.getColumnIndex("picture")));
                            if (bitmap == null) {
                                downloadImage(d.getString(d.getColumnIndex("picture")), houseImage);
                            }
                            else{
                                try{
                                    houseImage.setImageBitmap(bitmap);
                                }
                                catch(Exception ff){
                                    Toast.makeText(Category.this, ff.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                            houseImage.setLayoutParams(params);
                            imgContainer.addView(houseImage);
                            imgContainer.setLayoutParams(new Gallery.LayoutParams(thirty, thirty));
                            major.addView(imgContainer);

                            LinearLayout rightContainer = new LinearLayout(Category.this);
                            rightContainer.setOrientation(LinearLayout.VERTICAL);
                            rightContainer.setPadding(10, 14, 10, 14);

                            TextView head = new TextView(Category.this);
                            head.setText(d.getString(d.getColumnIndex("name")));
                            head.setTextSize(17.0f);
                            head.setTextColor(Color.parseColor(getString(R.color.colorAccent)));

                            TextView landlord = new TextView(Category.this);
                            landlord.setText(d.getString(d.getColumnIndex("features")));
                            rightContainer.addView(head);
                            rightContainer.addView(landlord);
                            major.addView(rightContainer);

                            TextView price = new TextView(Category.this);
                            price.setText("Price: MWK"+d.getString(d.getColumnIndex("price")));
                            price.setTextSize(15.0f);
                            price.setTypeface(price.getTypeface(), Typeface.BOLD);
                            rightContainer.addView(price);


                            final int house_id = d.getInt(d.getColumnIndex("webid"));
                            major.setOnClickListener(new View.OnClickListener(){
                                public void onClick(View vw){
                                    Intent myIntent = new Intent(Category.this, Details.class);
                                    myIntent.putExtra("id", house_id+"");
                                    Category.this.startActivity(myIntent);
                                }
                            });

                            productsContainer.addView(major);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            //sort.setTextColor(Color.parseColor("##155724"));
            dd.addView(sort);
            lg.addView(dd);

            productsContainer = new LinearLayout(this);
            productsContainer.setOrientation(LinearLayout.VERTICAL);
            lg.addView(productsContainer);

            Cursor d = db.rawQuery("SELECT * FROM products WHERE category = '"+id+"'", null);
            while (d.moveToNext()) {
                LinearLayout major = new LinearLayout(this);
                //registerForContextMenu(major);
                major.setPadding(6, 2, 6, 2);
                major.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout imgContainer = new LinearLayout(this);
                ShapeableImageView houseImage = new ShapeableImageView(new ContextThemeWrapper((Context) Category.this, R.style.rounded), null, 0);
                Gallery.LayoutParams params = new Gallery.LayoutParams(twentyfive, twentyfive);
                houseImage.setImageResource(R.drawable.thumbnail);
                Bitmap bitmap = loadImageFromStorage(path, d.getString(d.getColumnIndex("picture")));
                if (bitmap == null) {
                    downloadImage(d.getString(d.getColumnIndex("picture")), houseImage);
                }
                else{
                    try{
                        houseImage.setImageBitmap(bitmap);
                    }
                    catch(Exception ff){
                        Toast.makeText(this, ff.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                houseImage.setLayoutParams(params);
                imgContainer.addView(houseImage);
                imgContainer.setLayoutParams(new Gallery.LayoutParams(thirty, thirty));
                major.addView(imgContainer);

                LinearLayout rightContainer = new LinearLayout(this);
                rightContainer.setOrientation(LinearLayout.VERTICAL);
                rightContainer.setPadding(10, 14, 10, 14);

                TextView head = new TextView(this);
                head.setText(d.getString(d.getColumnIndex("name")));
                head.setTextSize(17.0f);
                head.setTextColor(Color.parseColor((String)"#006600"));

                TextView landlord = new TextView(this);
                landlord.setText(d.getString(d.getColumnIndex("features")));
                rightContainer.addView(head);
                rightContainer.addView(landlord);
                major.addView(rightContainer);

                TextView price = new TextView(this);
                price.setText("Price: MWK"+d.getString(d.getColumnIndex("price")));
                price.setTextSize(15.0f);
                price.setTypeface(price.getTypeface(), Typeface.BOLD);
                rightContainer.addView(price);

                TextView beds = new TextView(this);
                beds.setText(d.getString(d.getColumnIndex("views"))+" View(s)");
                beds.setTextSize(15.0f);
                beds.setTypeface(beds.getTypeface(), Typeface.ITALIC);
                rightContainer.addView(beds);


                final int house_id = d.getInt(d.getColumnIndex("webid"));
                major.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View vw){
                        Intent myIntent = new Intent(Category.this, Details.class);
                        myIntent.putExtra("id", house_id+"");
                        Category.this.startActivity(myIntent);
                    }
                });

                productsContainer.addView(major);
            }
        }
        catch(Exception ex1){
            Toast.makeText(this, ex1.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
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

        new Http(new HttpParams(Category.this, user.link+"/api.php", params){
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
                    Toast.makeText(Category.this, alf.toString(), Toast.LENGTH_LONG).show();
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
}