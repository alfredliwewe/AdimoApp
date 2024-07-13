package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Comment extends AppCompatActivity {
    LinearLayout main;
    SQLiteDatabase db;
    User user;
    String webid;
    String category_id;
    int screenWidth;
    int thirty;
    int twenty;
    int twentyfive;
    ContextWrapper cw;
    String path;
    @SuppressLint({"Range", "ResourceType"})
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());
        setContentView(R.layout.blank);

        main = (LinearLayout) findViewById(R.id.main);

        if (Build.VERSION.SDK_INT >= 21) {
            //change color of the activity
            getWindow().setNavigationBarColor(Color.parseColor(getString(R.color.colorAccent)));
            getWindow().setStatusBarColor(Color.parseColor(getString(R.color.colorAccent)));
        }
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        }

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        thirty = (int) (0.3 * screenWidth);
        twenty = (int) (0.2 * screenWidth);
        twentyfive = (int) (0.25 * screenWidth);

        Intent intent = getIntent();
        webid = intent.getStringExtra("product");

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        Cursor c = db.rawQuery("SELECT *,products.name AS product_name FROM products JOIN subcategories ON products.subcategory = subcategories.webid WHERE products.webid = '" + webid + "'", null);
        c.moveToFirst();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Review: " + c.getString(c.getColumnIndex("product_name")));
        category_id = c.getString(c.getColumnIndex("category"));

        Cursor d = db.rawQuery("SELECT * FROM products WHERE webid = '" + webid + "'", null);
        while (d.moveToNext()) {
            LinearLayout major = new LinearLayout(this);
            //registerForContextMenu(major);
            major.setPadding(6, 2, 6, 2);
            major.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout imgContainer = new LinearLayout(this);
            ImageView houseImage = new ImageView(this);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(twentyfive, twentyfive);
            houseImage.setImageResource(R.drawable.thumbnail);
            Bitmap bitmap = loadImageFromStorage(path, d.getString(d.getColumnIndex("picture")));
            if (bitmap == null) {
                downloadImage(d.getString(d.getColumnIndex("picture")), houseImage);
            } else {
                try {
                    houseImage.setImageBitmap(bitmap);
                } catch (Exception ff) {
                    Toast.makeText(this, ff.toString(), Toast.LENGTH_LONG).show();
                }
            }
            houseImage.setLayoutParams(params);
            imgContainer.addView(houseImage);
            imgContainer.setLayoutParams(new ViewGroup.LayoutParams(thirty, thirty));
            major.addView(imgContainer);

            LinearLayout rightContainer = new LinearLayout(this);
            rightContainer.setOrientation(LinearLayout.VERTICAL);
            rightContainer.setPadding(10, 14, 10, 14);

            TextView head = new TextView(this);
            head.setText(d.getString(d.getColumnIndex("name")));
            head.setTextSize(19.0f);
            head.setPadding(0, dpToPx(10), 0, dpToPx(10));
            head.setTextColor(Color.parseColor((String)"#212529"));

            TextView landlord = new TextView(this);
            landlord.setText(d.getString(d.getColumnIndex("features")));
            rightContainer.addView(head);
            landlord.setTextColor(Color.parseColor("#343a40"));
            //rightContainer.addView(landlord);
            major.addView(rightContainer);

            TextView price = new TextView(this);
            price.setText("Price: MWK" + d.getString(d.getColumnIndex("price")));
            price.setTextSize(15.0f);
            landlord.setTextColor(Color.parseColor(getString(R.color.colorAccent)));
            rightContainer.addView(price);

            TextView beds = new TextView(this);
            beds.setText(c.getString(c.getColumnIndex("name")));
            //beds.setTextSize(15.0f);
            beds.setTextColor(Color.BLACK);
            beds.setTypeface(beds.getTypeface(), Typeface.ITALIC);
            rightContainer.addView(beds);

            LinearLayout major2 = major;

            main.addView(major);
        }
        View line = new View(this);
        line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        line.setBackgroundColor(Color.GRAY);
        main.addView(line);

        //load the profile
        View view = getLayoutInflater().inflate(R.layout.comm_profile, null);
        main.addView(view);
        TextView username = (TextView) findViewById(R.id.username);
        username.setText(user.name);

        View line2 = new View(this);
        line2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        line2.setBackgroundColor(Color.GRAY);
        main.addView(line2);
    }

    public void sendComment(View v){
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        EditText comment = (EditText) findViewById(R.id.comment);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("user", user.id);
        params.put("product", webid);
        params.put("comment", comment.getText().toString());
        params.put("rating", ratingBar.getRating());

        new Http(new HttpParams(this, user.link+"/api.php", params){
            @Override
            public void onResponse(String text){
                try{
                    Response res = new Response(text);
                    if (res.status){
                        //success
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        Comment.this.finish();
                    }
                    else{
                        Toast.makeText(Comment.this, text, Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception ee){
                    Toast.makeText(Comment.this, text, Toast.LENGTH_LONG).show();
                }
            }
        });

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

        new Http(new HttpParams(Comment.this, user.link+"/api.php", params){
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
                    Toast.makeText(Comment.this, alf.toString(), Toast.LENGTH_LONG).show();
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
