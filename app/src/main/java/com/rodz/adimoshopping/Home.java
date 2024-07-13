package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import java.net.URL;
import java.util.*;
import android.database.sqlite.*;
import android.database.*;
import android.content.Intent;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.Gallery.*;
import android.content.res.Resources;
import android.content.ContextWrapper;
import java.io.*;
import android.util.Base64;
import android.util.Base64.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

public class Home extends Activity {
    SQLiteDatabase db;
    User user;
    LinearLayout hr;
    LinearLayout lg;
    int screenWidth;
    int thirty;
    int twenty;
    int twentyfive;
    ContextWrapper cw;
    String path;
    ScrollView scrollView;

    View.OnClickListener homeClick = new View.OnClickListener(){

        @SuppressLint("ResourceType")
        @Override
        public void onClick(View p1)
        {
            // TODO: Implement this method
            int childCount= hr.getChildCount();
            for(int i = 0; i < childCount; i++){
                TextView elem = (TextView)hr.getChildAt(i);
                //elem.setBackgroundResource(0);
                elem.setTextColor(Color.parseColor("#1a1a1a"));
            }
            
            TextView elem = (TextView)p1;
            //elem.setBackgroundResource(R.drawable.borderbottom);
            elem.setTextColor(Color.parseColor(getString(R.color.colorAccent)));
            if (elem.getText().toString().equals("Home")) {
                printProducts();
            }
            else{
                @SuppressLint("ResourceType") int category_id = (int)(elem.getId() / 20);
                printCategory(category_id);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"Range", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());
        setContentView(R.layout.blank);
        //getActionBar().hide();

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        thirty = (int)(0.3 * screenWidth);
        twenty = (int)(0.2 * screenWidth);
        twentyfive = (int)(0.25 * screenWidth);

        scrollView = (ScrollView) findViewById(R.id.scrollView);

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int x, int y, int oldX, int oldY) {
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

                // if diff is zero, then the bottom has been reached
                if (diff == 0) {
                    //Toast.makeText(Home.this, "Top", Toast.LENGTH_SHORT).show();
                }
                else{
                    //
                }
            }
        });

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        try{
            LinearLayout main = (LinearLayout)findViewById(R.id.main);

            View inf = getLayoutInflater().inflate(R.layout.home_search, null);
            main.addView(inf);

            if (user.status){
                //show profile image
                ShapeableImageView pro = (ShapeableImageView) findViewById(R.id.profile_home);

                Bitmap bitmap = loadImageFromStorage(path, user.picture);
                if (bitmap == null) {
                    downloadProfile(user.picture, pro);
                }
                else {
                    try {
                        pro.setImageBitmap(bitmap);
                    } catch (Exception ee) {
                    }
                }
            }
            
            HorizontalScrollView hv = new HorizontalScrollView(this);
            hr = new LinearLayout(this);
            hr.setOrientation(LinearLayout.HORIZONTAL);

            TextView prod1 = new TextView(this);
            prod1.setText("Home");
            prod1.setOnClickListener(homeClick);
            prod1.setPadding(dpToPx(25), dpToPx(18), dpToPx(25), dpToPx(18));
            prod1.setTextColor(Color.parseColor(getString(R.color.blue)));
            Typeface sans = ResourcesCompat.getFont(this, R.font.source_sans_semi_bold);
            prod1.setTypeface(sans);
            hr.addView(prod1);
            
            Cursor c = db.rawQuery("SELECT * FROM categories", null);
            //String[] names = new String[]{"Computers", "Kitchen & Home", "Luggage & Bags", "Dresses & Clothing", "Phones and Accessories", "Watches and Accessories", "Electronics", "Vehicles and Motor"};
            while(c.moveToNext()){
                TextView prod = new TextView(this);
                prod.setText(c.getString(c.getColumnIndex("name")));
                prod.setOnClickListener(homeClick);
                prod.setPadding(dpToPx(25), dpToPx(18), dpToPx(25), dpToPx(18));
                prod.setTypeface(sans);
                prod.setTextColor(Color.parseColor("#1a1a1a"));
                prod.setId(20 * c.getInt(c.getColumnIndex("webid")));
                hr.addView(prod);
            }
            c.close();
            hv.addView(hr);
            main.addView(hv);
            View line = new View(this);
            line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
            line.setBackgroundColor(Color.GRAY);
            main.addView(line);
            
            TextView first = (TextView)hr.getChildAt(0);
            //first.setBackgroundResource(R.drawable.borderbottom);
            first.setTextColor(Color.parseColor(getString(R.color.blue)));
            lg = new LinearLayout(this);
            lg.setPadding(dpToPx(5), dpToPx(15), dpToPx(5), dpToPx(15));
            lg.setOrientation(LinearLayout.VERTICAL);
            main.addView(lg);

            //lets add a tab layout
            TabLayout tabLayout= new TabLayout(this);
            tabLayout.setBackgroundColor(Color.WHITE);
            tabLayout.setTabTextColors(Color.BLACK, Color.parseColor("#1565c0"));
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            main.addView(tabLayout);
            Cursor c1 = db.rawQuery("SELECT * FROM categories", null);
            //String[] names = new String[]{"Computers", "Kitchen & Home", "Luggage & Bags", "Dresses & Clothing", "Phones and Accessories", "Watches and Accessories", "Electronics", "Vehicles and Motor"};
            while(c1.moveToNext()){
                TabLayout.Tab prod = tabLayout.newTab();
                prod.setText(c1.getString(c1.getColumnIndex("name")));
                //prod.setCustomView(tabItem);
                //prod.setId(20 * c.getInt(c.getColumnIndex("webid")));
                tabLayout.addTab(prod);
            }
            c1.close();


            printProducts();
        }
        catch(Exception ex){
            ex.printStackTrace();
            //Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void loadCart(View v){
        startActivity(new Intent(this, Cart.class));
    }

    public void loadProfile(View v){
        startActivity(new Intent(this, Account.class));
    }

    public void loadSearch(View v){
        startActivity(new Intent(this, Search.class));
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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
        subs.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        //subs.setBackgroundResource(R.drawable.round_xlarge);
        ArrayList<String> subcategories = new ArrayList<>();
        Cursor read = db.rawQuery("SELECT * FROM subcategories WHERE category = '"+id+"'", null);
        while(read.moveToNext()){
            subcategories.add(read.getString(read.getColumnIndex("name")));
        }
        ArrayAdapter aa1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, subcategories);
        aa1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subs.setAdapter(aa1);
        //subs.setTextColor(Color.parseColor("##155724"));
        dd.addView(subs);


        Spinner sort = new Spinner(this);
        sort.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        //sort.setBackgroundResource(R.drawable.round_xlarge);
        String[] mnth = new String[]{"Cheaper", "Popular", "Most purchased"};
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mnth);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort.setAdapter(aa);
        //sort.setTextColor(Color.parseColor("##155724"));
        dd.addView(sort);
        lg.addView(dd);

        Cursor d = db.rawQuery("SELECT * FROM products WHERE category = '"+id+"'", null);
        while (d.moveToNext()) {
            LinearLayout major = new LinearLayout(this);
            //registerForContextMenu(major);
            major.setPadding(6, 2, 6, 2);
            major.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout imgContainer = new LinearLayout(this);
            ShapeableImageView houseImage = new ShapeableImageView(new ContextThemeWrapper((Context) Home.this, R.style.rounded_home), null, 0);
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
            head.setTextSize(17.0f);
            head.setTextColor(Color.parseColor((String)"#006600"));

            TextView landlord = new TextView(this);
            landlord.setText(d.getString(d.getColumnIndex("features")));
            rightContainer.addView(head);
            rightContainer.addView(landlord);
            major.addView(rightContainer);

            TextView price = new TextView(this);
            price.setText("Price: MWK"+Values.numberFormat(d.getString(d.getColumnIndex("price"))));
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
                    Intent myIntent = new Intent(Home.this, Details.class);
                    myIntent.putExtra("id", house_id+"");
                    Home.this.startActivity(myIntent);
                }
            });

            lg.addView(major);
        }
        }
        catch(Exception ex1){
            Toast.makeText(this, ex1.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("Range")
    public void printProducts(){
        lg.removeAllViews();
        Typeface product_sans = ResourcesCompat.getFont(this, R.font.product_sans_bold);
        Typeface roboto = ResourcesCompat.getFont(this, R.font.word);

        Cursor c = db.rawQuery("SELECT * FROM categories ORDER BY views DESC", null);
        try{
            while(c.moveToNext()){
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(dpToPx(15), dpToPx(10), dpToPx(25), dpToPx(10));

                LinearLayout ft = new LinearLayout(this);
                ft.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

                TextView category = new TextView(this);
                category.setText(c.getString(c.getColumnIndex("name")));
                category.setTextSize(17.0f);
                category.setTypeface(product_sans);
                category.setTextColor(Color.parseColor("#1a1a1a"));
                ft.addView(category);
                row.addView(ft);

                LinearLayout sc = new LinearLayout(this);
                sc.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                sc.setGravity(Gravity.RIGHT);

                ImageView viewall = new ImageView(this);
                //viewall.setText("View all");
                //viewall.setTextColor(Color.WHITE);
                viewall.setImageResource(R.drawable.ic_arrow_forward);
                final String category_id = c.getString(c.getColumnIndex("webid"));
                viewall.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View p1){
                        Intent intent = new Intent(Home.this, Category.class);
                        intent.putExtra("category", category_id);
                        Home.this.startActivity(intent);
                    }
                });
                sc.addView(viewall);
                row.addView(sc);
                lg.addView(row);
                //pint the available products
                LinearLayout mm = new LinearLayout(this);
                mm.setOrientation(LinearLayout.VERTICAL);
                mm.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                HorizontalScrollView sv = new HorizontalScrollView(this);
                sv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                mm.addView(sv);
                lg.addView(mm);

                LinearLayout responsive = new LinearLayout(this);
                responsive.setOrientation(LinearLayout.HORIZONTAL);
                responsive.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                responsive.setPadding(dpToPx(20),0,0,dpToPx(25));
                sv.addView(responsive);

                Cursor p = db.rawQuery("SELECT * FROM products WHERE category = '"+c.getString(c.getColumnIndex("webid"))+"' ORDER BY views DESC", null);
                while(p.moveToNext()){
                    LinearLayout col = new LinearLayout(this);
                    col.setOrientation(LinearLayout.VERTICAL);
                    //col.setGravity(Gravity.CENTER);
                    col.setPadding(0, dpToPx(10), 0, dpToPx(10));
                    col.setLayoutParams(new LayoutParams(dpToPx(130), LayoutParams.WRAP_CONTENT));
                    col.setPadding(dpToPx(4),0,dpToPx(4), 0);
                    TypedValue outValue = new TypedValue();
                    this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    col.setClickable(true);
                    col.setBackgroundResource(outValue.resourceId);
                    //col.setGravity(Gravity.CENTER);

                    ShapeableImageView iv = new ShapeableImageView(new ContextThemeWrapper((Context) Home.this, R.style.rounded_home), null, 0);
                    iv.setImageResource(R.drawable.thumbnail);
                    iv.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
                    iv.setLayoutParams(new LayoutParams(dpToPx(110), dpToPx(110)));
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
                            LayoutParams params2 = (LayoutParams)iv.getLayoutParams();
                            params2.height = dpToPx(110);
                            params2.width = dpToPx(110);
                            iv.setLayoutParams(params2);
                        }
                        catch(Exception ff){
                            Toast.makeText(this, ff.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                    col.addView(iv);

                    TextView product = new TextView(this);
                    product.setText(p.getString(p.getColumnIndex("name")));
                    //product.setTextSize(17.0f);
                    product.setTextColor(Color.parseColor("#1a1a1a"));
                    product.setMaxLines(1);
                    product.setPadding(0, dpToPx(4),0,0);
                    product.setTypeface(roboto);
                    product.setShadowLayer(1,1,1,Color.parseColor("#999999"));
                    product.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    col.addView(product);

                    TextView price = new TextView(this);
                    price.setText("K"+Values.numberFormat(p.getString(p.getColumnIndex("price"))));
                    //price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    price.setTextColor(Color.parseColor("#1a1a1a"));
                    price.setTypeface(roboto);
                    price.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    col.addView(price);
                    final String product_id = p.getString(p.getColumnIndex("webid"));
                    col.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View p1){
                            Intent intent = new Intent(Home.this, Details.class);
                            intent.putExtra("id", product_id);
                            Home.this.startActivity(intent);
                        }
                    });

                    responsive.addView(col);
                }

                /*View line = new View(this);
                line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
                line.setBackgroundColor(Color.parseColor("#cccccc"));
                main.addView(line); */
            }
        }
        catch(Exception ex1){
            Toast.makeText(this, ex1.toString(), Toast.LENGTH_LONG).show();
        }
        c.close();
    }

    public void downloadImage(String filename, ImageView myImage){
        //proceed to download image
        //Toast.makeText(MainActivity.this, "Image saved successfully", Toast.LENGTH_LONG).show();
        final String fname = filename;
        final ImageView iv = myImage;
        final User user = new User(db);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("file", fname);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL newurl = new URL(user.link+"/products/"+filename);
                    Bitmap decodedByte = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (decodedByte != null) {
                                iv.setImageBitmap(decodedByte);
                                saveToInternalStorage(decodedByte, fname);


                                LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) iv.getLayoutParams();
                                params2.height = dpToPx(110);
                                params2.width = dpToPx(110);
                                iv.setLayoutParams(params2);
                            }
                        }
                    });
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
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

    public void downloadProfile(String filename, ImageView myImage){
        //proceed to download image
        //Toast.makeText(MainActivity.this, "Image saved successfully", Toast.LENGTH_LONG).show();
        final String fname = filename;
        final ImageView iv = myImage;
        final User user = new User(db);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pro_file", fname);

        new Http(new HttpParams(Home.this, user.link+"/api.php", params){
            @Override
            public void onResponse(String response){
                try{
                    byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    iv.setImageBitmap(decodedByte);
                    saveToInternalStorage(decodedByte, fname);
                }
                catch(Exception alf){
                    Toast.makeText(Home.this, alf.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}