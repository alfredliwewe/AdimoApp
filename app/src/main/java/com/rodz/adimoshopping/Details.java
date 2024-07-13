package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

public class Details extends AppCompatActivity {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);

        main = (LinearLayout)findViewById(R.id.main);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);
        //db.execSQL("CREATE TABLE IF NOT EXISTS displayImages (id INTEGER primary key autoincrement, webid VARCHAR, product VARCHAR, file VARCHAR)");
        if (Build.VERSION.SDK_INT >= 21) {
            //change color of the activity
            getWindow().setNavigationBarColor(Color.parseColor(getString(R.color.colorAccent)));
            getWindow().setStatusBarColor(Color.parseColor(getString(R.color.colorAccent)));
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        }

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        thirty = (int)(0.3 * screenWidth);
        twenty = (int)(0.2 * screenWidth);
        twentyfive = (int)(0.25 * screenWidth);

        Intent intent = getIntent();
        webid = intent.getStringExtra("id");

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        Cursor c = db.rawQuery("SELECT *,products.name AS product_name FROM products JOIN subcategories ON products.subcategory = subcategories.webid WHERE products.webid = '"+webid+"'", null);
        c.moveToFirst();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(c.getString(c.getColumnIndex("product_name")));
        category_id = c.getString(c.getColumnIndex("category"));

        db.execSQL("UPDATE products SET views = views + 1 WHERE webid = ?", new Object[]{webid});
        db.execSQL("UPDATE categories SET views = views + 1 WHERE webid = ?", new Object[]{category_id});


        Cursor d = db.rawQuery("SELECT * FROM products WHERE webid = '"+webid+"'", null);
        while (d.moveToNext()) {
            LinearLayout major = new LinearLayout(this);
            //registerForContextMenu(major);
            major.setPadding(6, dpToPx(12), 6, 2);
            major.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout imgContainer = new LinearLayout(this);
            ShapeableImageView houseImage = new ShapeableImageView(new ContextThemeWrapper((Context) Details.this, R.style.rounded_home), null, 0);
            houseImage.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            LayoutParams params = new LayoutParams(twentyfive, twentyfive);
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
            imgContainer.setLayoutParams(new LayoutParams(thirty, thirty));
            major.addView(imgContainer);

            LinearLayout rightContainer = new LinearLayout(this);
            rightContainer.setOrientation(LinearLayout.VERTICAL);
            rightContainer.setPadding(10, 14, 10, 14);

            TextView head = new TextView(this);
            head.setText(d.getString(d.getColumnIndex("name")));
            head.setTextSize(19.0f);
            head.setPadding(0, dpToPx(10),0, dpToPx(10));
            head.setTextColor(Color.parseColor((String)"#262626"));

            TextView landlord = new TextView(this);
            landlord.setText(d.getString(d.getColumnIndex("features")));
            rightContainer.addView(head);
            //rightContainer.addView(landlord);
            major.addView(rightContainer);

            TextView price = new TextView(this);
            price.setText("Price: MWK"+Values.numberFormat(d.getString(d.getColumnIndex("price"))));
            price.setTextSize(15.0f);
            price.setTextColor(Color.BLACK);
            rightContainer.addView(price);

            TextView beds = new TextView(this);
            beds.setText(c.getString(c.getColumnIndex("name")));
            //beds.setTextSize(15.0f);
            beds.setTypeface(beds.getTypeface(), Typeface.ITALIC);
            rightContainer.addView(beds);

            LinearLayout major2 = major;

            main.addView(major);

            //add the extra details row

            LinearLayout optionsContainer = new LinearLayout(this);
            optionsContainer.setOrientation(LinearLayout.HORIZONTAL);
            optionsContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            main.addView(optionsContainer);
            //main.addView(sv);

            optionsContainer.addView(w3Col(R.drawable.star, "4.5M Reviews"));
            optionsContainer.addView(w3Col(R.drawable.location, "Mzuzu"));
            //optionsContainer.addView(w3Col(R.drawable.category, c.getString(c.getColumnIndex("name"))));
            optionsContainer.addView(w3Col(R.drawable.ic_favorite_border, "Add Fav+"));

            LinearLayout buttonContainer = new LinearLayout(this);
            buttonContainer.setOrientation(LinearLayout.VERTICAL);
            buttonContainer.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(30));

            TextView purchase = new TextView(this);
            purchase.setText("Add to Cart");
            purchase.setTextColor(Color.WHITE);
            purchase.setTypeface(purchase.getTypeface(), Typeface.BOLD);
            purchase.setBackgroundResource(R.drawable.btn_dark);
            purchase.setGravity(Gravity.CENTER);
            purchase.setOnClickListener(new View.OnClickListener(){
                public void onClick(View p1){
                    //save product to cart -- check first
                    db.execSQL("CREATE TABLE IF NOT EXISTS cart (id INTEGER primary key autoincrement, product VARCHAR, qty VARCHAR)");
                    Cursor g = db.rawQuery("SELECT * FROM cart WHERE product = '"+webid+"' ", null);
                    if (g.getCount() > 0) {
                        //Toast.makeText(Details.this, "Product is already in cart", Toast.LENGTH_LONG).show();
                        Snackbar.make(p1, "Product is already in cart", Snackbar.LENGTH_LONG).show();
                    }
                    else{
                        //insert it
                        db.execSQL("INSERT INTO cart (id, product, qty) VALUES (NULL, ?, ?)", new String[]{webid, "1"});
                        //Toast.makeText(Details.this, "Added", Toast.LENGTH_LONG).show();
                        //Snackbar.make(p1, "Added", Snackbar.LENGTH_LONG).show();
                        
                        LinearLayout sheetView = new LinearLayout(Details.this);
                        sheetView.setOrientation(LinearLayout.VERTICAL);
                        sheetView.setBackgroundColor(Color.WHITE);
                        sheetView.setPadding(dpToPx(10), dpToPx(15), dpToPx(10), dpToPx(15));
                        //sheetView.addView(major2);

                        ///THESE ARE REPEATED CODES
                        //////////////
                        ////////////////////////////
                        Cursor d = db.rawQuery("SELECT *,products.name AS product_name FROM products JOIN subcategories ON products.subcategory = subcategories.webid WHERE products.webid = '"+webid+"'", null);
                        d.moveToFirst();
                        LinearLayout major2 = new LinearLayout(Details.this);
                        //registerForContextMenu(major);
                        major2.setPadding(6, 2, 6, 2);
                        major2.setOrientation(LinearLayout.HORIZONTAL);

                        LinearLayout imgContainer2 = new LinearLayout(Details.this);
                        ShapeableImageView houseImage2 = new ShapeableImageView(new ContextThemeWrapper((Context) Details.this, R.style.rounded), null, 0);
                        LayoutParams params = new LayoutParams(twentyfive, twentyfive);
                        houseImage2.setImageResource(R.drawable.thumbnail);
                        Bitmap bitmap2 = loadImageFromStorage(path, d.getString(d.getColumnIndex("picture")));
                        if (bitmap2 == null) {
                            downloadImage(d.getString(d.getColumnIndex("picture")), houseImage2);
                        }
                        else{
                            try{
                                houseImage2.setImageBitmap(bitmap2);
                            }
                            catch(Exception ff){
                                ff.printStackTrace();
                                //Toast.makeText(Details.this, ff.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                        houseImage2.setLayoutParams(params);
                        imgContainer2.addView(houseImage2);
                        imgContainer2.setLayoutParams(new LayoutParams(thirty, thirty));
                        major2.addView(imgContainer2);

                        LinearLayout rightContainer2 = new LinearLayout(Details.this);
                        rightContainer2.setOrientation(LinearLayout.VERTICAL);
                        rightContainer2.setPadding(10, 14, 10, 14);

                        TextView head2 = new TextView(Details.this);
                        head2.setText(d.getString(d.getColumnIndex("product_name")));
                        head2.setTextSize(19.0f);
                        head2.setPadding(0, dpToPx(10),0, dpToPx(10));
                        //head.setTextColor(Color.parseColor((String)"#006600"));

                        TextView landlord2 = new TextView(Details.this);
                        landlord2.setText(d.getString(d.getColumnIndex("features")));
                        rightContainer2.addView(head2);
                        //rightContainer.addView(landlord);
                        major2.addView(rightContainer2);

                        TextView price2 = new TextView(Details.this);
                        price2.setText("Price: MWK"+Values.numberFormat(d.getString(d.getColumnIndex("price"))));
                        price2.setTextSize(15.0f);
                        price2.setTextColor(Color.parseColor((String)"#006600"));
                        rightContainer2.addView(price2);

                        TextView beds = new TextView(Details.this);
                        beds.setText(c.getString(c.getColumnIndex("name")));
                        //beds.setTextSize(15.0f);
                        beds.setTypeface(beds.getTypeface(), Typeface.ITALIC);
                        rightContainer2.addView(beds);
                        sheetView.addView(major2);

                        BottomSheetDialog dialog = new BottomSheetDialog(Details.this);
                        //////////////////////////////////////////////////////////////////
                        //////////////////////////////////////////////////////////////////
                        //// PLEASE REMOVE THEM                                       ////
                        //////////////////////////////////////////////////////////////////

                        TextView success = new TextView(Details.this);
                        success.setText(R.string.success_add);
                        success.setTextColor(Color.parseColor("#007bff"));
                        success.setBackgroundResource(R.drawable.alert_primary);
                        sheetView.addView(success);

                        LinearLayout hori = new LinearLayout(Details.this);
                        hori.setPadding(0,dpToPx(10),0,0);
                        hori.setOrientation(LinearLayout.HORIZONTAL);

                        TextView goCart = new TextView(Details.this);
                        goCart.setText("Go to Cart");
                        goCart.setBackgroundResource(R.drawable.btn_dark);
                        goCart.setClickable(true);
                        goCart.setTextColor(Color.WHITE);
                        goCart.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                        goCart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(Details.this, Cart.class));
                                dialog.hide();
                                finish();
                            }
                        });
                        hori.addView(goCart);
                        TextView emp = new TextView(Details.this);
                        emp.setText("   ");
                        hori.addView(emp);

                        TextView conti = new TextView(Details.this);
                        conti.setText("Continue shopping");
                        conti.setClickable(true);
                        conti.setBackgroundResource(R.drawable.btn_outline);
                        conti.setTextColor(Color.parseColor("#212529"));
                        conti.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.hide();
                            }
                        });
                        hori.addView(conti);

                        sheetView.addView(hori);

                        TextView similar = new TextView(Details.this);
                        similar.setText("Similar products");
                        similar.setTextSize(19.0f);
                        Typeface product_sans = ResourcesCompat.getFont(Details.this, R.font.product_sans_bold);
                        similar.setTypeface(product_sans);
                        similar.setTextColor(Color.BLACK);
                        similar.setPadding(dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15));
                        sheetView.addView(similar);

                        //get products of the category
                        HorizontalScrollView sv = new HorizontalScrollView(Details.this);
                        sv.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
                        sheetView.addView(sv);
                        //lg.addView(mm);

                        LinearLayout responsive = new LinearLayout(Details.this);
                        responsive.setOrientation(LinearLayout.HORIZONTAL);
                        responsive.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
                        sv.addView(responsive);

                        Typeface roboto = ResourcesCompat.getFont(Details.this, R.font.word);

                        Cursor p = db.rawQuery("SELECT * FROM products WHERE category = '"+category_id+"'", null);
                        while(p.moveToNext()){
                            LinearLayout col = new LinearLayout(Details.this);
                            col.setOrientation(LinearLayout.VERTICAL);
                            //col.setGravity(Gravity.CENTER);
                            col.setPadding(0, dpToPx(10), 0, dpToPx(10));
                            col.setLayoutParams(new Gallery.LayoutParams(dpToPx(130), Gallery.LayoutParams.WRAP_CONTENT));
                            //col.setGravity(Gravity.CENTER);

                            ShapeableImageView iv = new ShapeableImageView(new ContextThemeWrapper((Context) Details.this, R.style.rounded_home), null, 0);
                            iv.setImageResource(R.drawable.thumbnail);
                            iv.setLayoutParams(new Gallery.LayoutParams(dpToPx(110), dpToPx(110)));
                            Bitmap bitmap = loadImageFromStorage(path, p.getString(p.getColumnIndex("picture")));
                            if (bitmap == null) {
                                downloadImage(p.getString(p.getColumnIndex("picture")), iv);
                            }
                            else{
                                try{
                                    iv.setImageBitmap(bitmap);
                                    String height = bitmap.getHeight()+"";
                                    String width = bitmap.getWidth()+"";
                                    Double ratio = Double.parseDouble(height) / Double.parseDouble(width);
                                    ViewGroup.LayoutParams params2 = (ViewGroup.LayoutParams)iv.getLayoutParams();
                                    params2.height = dpToPx(110);
                                    params2.width = dpToPx(110);
                                    iv.setLayoutParams(params2);
                                }
                                catch(Exception ff){
                                    ff.printStackTrace();
                                    //Toast.makeText(Details.this, ff.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                            col.addView(iv);

                            TextView product = new TextView(Details.this);
                            product.setText(p.getString(p.getColumnIndex("name")));
                            product.setMaxLines(1);
                            product.setTextColor(Color.parseColor("#1a1a1a"));
                            product.setPadding(0, dpToPx(4),0,0);
                            product.setTypeface(roboto);
                            product.setShadowLayer(1,1,1,Color.parseColor("#999999"));

                            //product.setTextSize(17.0f);
                            product.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
                            col.addView(product);

                            TextView price = new TextView(Details.this);
                            price.setText("K"+Values.numberFormat(p.getString(p.getColumnIndex("price"))));
                            //price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            price.setTextColor(Color.parseColor("#1a1a1a"));
                            price.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                            price.setTextSize(17.0f);
                            col.addView(price);
                            final String product_id = p.getString(p.getColumnIndex("webid"));
                            col.setOnClickListener(new View.OnClickListener(){
                                public void onClick(View p1){
                                    dialog.hide();
                                    Intent intent = new Intent(Details.this, Details.class);
                                    intent.putExtra("id", product_id);
                                    Details.this.startActivity(intent);
                                }
                            });

                            responsive.addView(col);
                        }


                        dialog.setContentView(sheetView);

                        //genarate view
                        dialog.show();
                    }
                }
            });
            buttonContainer.addView(purchase);
            main.addView(buttonContainer);

            //get display pictures
            HorizontalScrollView hv = new HorizontalScrollView(this);
            hv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            final LinearLayout imagesContainer = new LinearLayout(this);
            imagesContainer.setOrientation(LinearLayout.HORIZONTAL);
            hv.addView(imagesContainer);
            main.addView(hv);

            //print available images
            final ArrayList<String> availableImages = new ArrayList<>();
            Cursor imagesList = db.rawQuery("SELECT * FROM displayImages WHERE product = '"+webid+"'", null);
            while(imagesList.moveToNext()){
                availableImages.add(imagesList.getString(imagesList.getColumnIndex("webid")));

                LinearLayout paddy = new LinearLayout(Details.this);
                paddy.setPadding(dpToPx(10), 0, dpToPx(10), 0);
                paddy.setOrientation(LinearLayout.VERTICAL);

                ImageView imgy = new ImageView(Details.this);
                imgy.setImageResource(R.drawable.thumbnail);
                Bitmap bitmapy = loadImageFromStorage(path, imagesList.getString(imagesList.getColumnIndex("file")));
                if (bitmapy == null) {
                    downloadImage(imagesList.getString(imagesList.getColumnIndex("file")), imgy);
                }
                else{
                    try{
                        imgy.setImageBitmap(bitmapy);
                        String heighty = bitmapy.getHeight()+"";
                        String widthy = bitmapy.getWidth()+"";
                        //Double ratio = Double.parseDouble(height) / Double.parseDouble(width);
                        int width1y = (int)(dpToPx(200) / Double.parseDouble(heighty) * Double.parseDouble(widthy));
                        imgy.setLayoutParams(new LayoutParams(width1y, dpToPx(200)));
                    }
                    catch(Exception ff){
                        ff.printStackTrace();
                        //Toast.makeText(Details.this, ff.toString(), Toast.LENGTH_LONG).show();
                    }
                } 
                paddy.addView(imgy);
                //img.setPadding(dpToPx(10), 0, dpToPx(10));
                imagesContainer.addView(paddy);
            }

            Map<String, Object> params1 = new LinkedHashMap<>();
            params1.put("getDisplayImages", webid);

            new Http(new HttpParams(this, user.link+"/api.php", params1){
                @Override
                public void onResponse(String text){
                    try{
                        Response res = new Response(text);
                        if (res.status) {
                            for (int i = 0; i < res.rows; i++) {
                                Response.Row row = res.getRow(i);

                                //check already saved
                                db.execSQL("CREATE TABLE IF NOT EXISTS displayImages (id INTEGER primary key autoincrement, webid VARCHAR, product VARCHAR, file VARCHAR)");
                                Cursor x1 = db.rawQuery("SELECT * FROM displayImages WHERE webid = '"+row.getData("id")+"' ", null);
                                if (x1.getCount() < 1) {
                                    //insert
                                    db.execSQL("INSERT INTO displayImages (id, webid, product, file) VALUES (NULL, ?, ?, ?)", new String[]{row.getData("id"), row.getData("product"), row.getData("file")});
                                }

                                if (!availableImages.contains(row.getData("id"))) {
                                    LinearLayout padd = new LinearLayout(Details.this);
                                    padd.setPadding(dpToPx(10), 0, dpToPx(10), 0);
                                    padd.setOrientation(LinearLayout.VERTICAL);

                                    ImageView img = new ImageView(Details.this);
                                    img.setImageResource(R.drawable.thumbnail);
                                    Bitmap bitmap = loadImageFromStorage(path, row.getData("file"));
                                    if (bitmap == null) {
                                        downloadImage(row.getData("file"), img);
                                    }
                                    else{
                                        try{
                                            img.setImageBitmap(bitmap);
                                            String height = bitmap.getHeight()+"";
                                            String width = bitmap.getWidth()+"";
                                            //Double ratio = Double.parseDouble(height) / Double.parseDouble(width);
                                            int width1 = (int)(dpToPx(200) / Double.parseDouble(height) * Double.parseDouble(width));
                                            img.setLayoutParams(new LayoutParams(width1, dpToPx(200)));
                                        }
                                        catch(Exception ff){
                                            ff.printStackTrace();
                                            //Toast.makeText(Details.this, ff.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    } 
                                    padd.addView(img);
                                    //img.setPadding(dpToPx(10), 0, dpToPx(10));
                                    imagesContainer.addView(padd);
                                }
                            }
                        }
                        else{
                            Toast.makeText(Details.this, text, Toast.LENGTH_LONG).show();
                        }
                    }
                    catch(Exception ex1){
                        Toast.makeText(Details.this, text, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        d.close();


        TextView write = new TextView(this);
        write.setText("Write a review/comment");
        write.setTextColor(Color.BLACK);
        write.setPadding(dpToPx(15), dpToPx(25),dpToPx(15), dpToPx(25));
        if (user.status) {
            main.addView(write);
        }
        write.setTypeface(write.getTypeface(), Typeface.BOLD);
        write.setTextColor(Color.parseColor(getString(R.color.colorAccent)));
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent comm = new Intent(Details.this, Comment.class);
                comm.putExtra("product", webid);
                startActivityForResult(comm, 12);
            }
        });

        View ratings = getLayoutInflater().inflate(R.layout.ratings, null);
        main.addView(ratings);

        //get ratings stats
        Map<String, Object> par1 = new LinkedHashMap<>();
        par1.put("getProductDetails", webid);
        new Http(new HttpParams(this, user.link+"/api.php", par1) {
            @Override
            public void onResponse(String text) {
                try {
                    JSONObject obj = new JSONObject(text);

                    TextView rating = findViewById(R.id.rating);
                    rating.setText(obj.getString("stars"));
                    double width = (.7 * screenWidth) - dpToPx(44);

                    View rail1 = findViewById(R.id.rail1), rail2 = findViewById(R.id.rail2), rail3 = findViewById(R.id.rail3), rail4 = findViewById(R.id.rail4), rail5 = findViewById(R.id.rail5);
                    rail1.setLayoutParams(new LinearLayout.LayoutParams((int) (obj.getDouble("r1") * width), dpToPx(8)));
                    rail2.setLayoutParams(new LinearLayout.LayoutParams((int) (obj.getDouble("r2") * width), dpToPx(8)));
                    rail3.setLayoutParams(new LinearLayout.LayoutParams((int) (obj.getDouble("r3") * width), dpToPx(8)));
                    rail4.setLayoutParams(new LinearLayout.LayoutParams((int) (obj.getDouble("r4") * width), dpToPx(8)));
                    rail5.setLayoutParams(new LinearLayout.LayoutParams((int) (obj.getDouble("r5") * width), dpToPx(8)));

                    String number = obj.getString("r1")+","+obj.getString("r2")+","+obj.getString("r3")+","+obj.getString("r4")+","+obj.getString("r5");
                    System.out.println("Number rail was: "+number);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(Details.this, ex.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        //getting comments
        Map<String, Object> par = new LinkedHashMap<>();
        par.put("getReviews", webid);
        new Http(new HttpParams(this, user.link+"/api.php", par){
            @Override
            public void onResponse(String text){
                try{
                    JSONArray rows = new JSONArray(text);

                    for (int i = 0; i < rows.length(); i++){
                        JSONObject row = rows.getJSONObject(i);
                        LinearLayout commentBox = new LinearLayout(Details.this);
                        commentBox.setOrientation(LinearLayout.VERTICAL);
                        commentBox.setPadding(dpToPx(3), dpToPx(20), dpToPx(3), dpToPx(20));

                        LinearLayout commentHead = new LinearLayout(Details.this);
                        commentHead.setOrientation(LinearLayout.HORIZONTAL);
                        commentBox.addView(commentHead);

                        LinearLayout imgC = new LinearLayout(Details.this);
                        imgC.setOrientation(LinearLayout.VERTICAL);
                        imgC.setLayoutParams(new LayoutParams(twenty, LayoutParams.WRAP_CONTENT));
                        imgC.setGravity(Gravity.CENTER);
                        commentHead.addView(imgC);

                        ShapeableImageView profile = new ShapeableImageView(new ContextThemeWrapper((Context) Details.this, R.style.rounded_50), null, 0);
                        profile.setLayoutParams(new LayoutParams(dpToPx(40), dpToPx(40)));
                        profile.setImageResource(R.drawable.user);
                        Bitmap bitmap = loadImageFromStorage(path, row.getString("resampled"));
                        if (bitmap == null) {
                            downloadProfile(row.getString("resampled"), profile);
                        }
                        else {
                            try {
                                profile.setImageBitmap(bitmap);
                            } catch (Exception ee) {
                            }
                        }
                        imgC.addView(profile);

                        LinearLayout nameC = new LinearLayout(Details.this);
                        nameC.setPadding(dpToPx(0), dpToPx(7), 0, 0);
                        nameC.setLayoutParams(new LayoutParams((int)(0.59*screenWidth), LayoutParams.WRAP_CONTENT));
                        commentHead.addView(nameC);

                        TextView senderName = new TextView(Details.this);
                        senderName.setText(row.getString("name"));
                        senderName.setTextColor(Color.BLACK);
                        senderName.setTextSize(17.0f);
                        nameC.addView(senderName);

                        ImageView options = new ImageView(Details.this);
                        options.setImageResource(R.drawable.more_vertical);
                        commentHead.addView(options);
                        options.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PopupMenu popupMenu = new PopupMenu(Details.this, view);
                                popupMenu.getMenuInflater().inflate(R.menu.comment_privacy, popupMenu.getMenu());
                                popupMenu.show();
                            }
                        });

                        TextView commet = new TextView(Details.this);
                        commet.setText(row.getString("comment"));
                        commet.setTextColor(Color.BLACK);
                        commet.setPadding(dpToPx(15), dpToPx(10), dpToPx(15), dpToPx(10));
                        commentBox.addView(commet);

                        main.addView(commentBox);
                    }
                }
                catch (Exception ff){
                    System.out.println(text);
                    ff.printStackTrace();
                }
            }
        });
        //c.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Success");
            alert.setMessage("Successfull sent a review");
            alert.setNegativeButton((CharSequence) "Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alert.show();
        }
    }


    public LinearLayout w3Col(int img, String text){
        LinearLayout col1 = new LinearLayout(this);
        col1.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, .3f));
        col1.setOrientation(LinearLayout.VERTICAL);
        col1.setGravity(Gravity.CENTER);

        ImageView star = new ImageView(this);
        star.setImageResource(img);
        star.setLayoutParams(new LayoutParams(dpToPx(20), dpToPx(20)));
        col1.addView(star);

        TextView rev = new TextView(this);
        rev.setText(text);
        rev.setTextColor(Color.parseColor("#262626"));
        rev.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        col1.addView(rev);
        //optionsContainer.addView(col1);

        return col1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        try{
            getMenuInflater().inflate(R.menu.menu, menu);
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
            case R.id.cart:
                startActivity(new Intent(this, Cart.class));
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

        new Http(new HttpParams(Details.this, user.link+"/api.php", params){
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
                    alf.printStackTrace();
                    //Toast.makeText(Details.this, alf.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void downloadProfile(String filename, ImageView myImage){
        //proceed to download image
        //Toast.makeText(MainActivity.this, "Image saved successfully", Toast.LENGTH_LONG).show();
        final String fname = filename;
        final ImageView iv = myImage;
        final User user = new User(db);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pro_file", fname);

        new Http(new HttpParams(Details.this, user.link+"/api.php", params){
            @Override
            public void onResponse(String response){
                try{
                    byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    iv.setImageBitmap(decodedByte);
                    saveToInternalStorage(decodedByte, fname);
                }
                catch(Exception alf){
                    alf.printStackTrace();
                    //Toast.makeText(Details.this, alf.toString(), Toast.LENGTH_LONG).show();
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
            FileInputStream inputStream = new FileInputStream(f);
            Bitmap b = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return b;
        }
        catch(Exception ee){
            return null;
        }
    }
}