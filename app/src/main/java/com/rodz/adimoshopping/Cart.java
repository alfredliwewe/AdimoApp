package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.rodz.adimoshopping.views.CartQuantity;

public class Cart extends AppCompatActivity {
    SQLiteDatabase db;
    LinearLayout main;
    String webid;
    int screenWidth;
    ContextWrapper cw;
    String path;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);

        main = (LinearLayout)findViewById(R.id.main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        if (Build.VERSION.SDK_INT >= 21) {
            //change color of the activity
            getWindow().setNavigationBarColor(Color.parseColor(getString(R.color.colorAccent)));
            getWindow().setStatusBarColor(Color.parseColor(getString(R.color.colorAccent)));
        }
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        }

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        User user = new User(db);
        if (user.status) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                printCart();
            }
        }
        else{
            Intent intent = new Intent(this, Account.class);
            intent.putExtra("mode", "pay");
            startActivity(intent);
            this.finish();
        }
        
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"Range", "ResourceType"})
    public void printCart(){
        //read the cart
        main.setPadding(dpToPx(10), dpToPx(15), dpToPx(10), dpToPx(15));
        main.removeAllViews();

        Typeface product_sans = ResourcesCompat.getFont(this, R.font.product_sans_bold);

        int total = 0;
        Cursor c = db.rawQuery("SELECT *, products.name AS product_name, cart.qty AS cqty,cart.id AS cart_id FROM cart JOIN products ON cart.product = products.webid JOIN categories ON products.category = categories.webid", null);
        if (c.getCount() > 0) {
            while(c.moveToNext()){ 
                total += c.getInt(c.getColumnIndex("price")) * c.getInt(c.getColumnIndex("cqty"));
                String cart_id = c.getString(c.getColumnIndex("cart_id"));
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dpToPx(10), 0, dpToPx(10));

                LinearLayout left = new LinearLayout(this);
                left.setOrientation(LinearLayout.VERTICAL);
                left.setGravity(Gravity.CENTER);
                left.setPadding(0,dpToPx(10),0,dpToPx(10));
                left.setLayoutParams(new LayoutParams((int)(0.3 * screenWidth), LayoutParams.WRAP_CONTENT));

                ShapeableImageView iv = new ShapeableImageView(new ContextThemeWrapper((Context) Cart.this, R.style.rounded_home), null, 0);
                iv.setLayoutParams(new LayoutParams((int)(0.2 * screenWidth), (int)(0.2 * screenWidth)));
                //iv.setPadding(0,dpToPx(5),0,dpToPx(5));
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
                left.addView(iv);
                row.addView(left);

                LinearLayout right = new LinearLayout(this);
                right.setOrientation(LinearLayout.VERTICAL);
                right.setLayoutParams(new LayoutParams((int)(0.7 * screenWidth), LayoutParams.WRAP_CONTENT));

                TextView name = new TextView(this);
                name.setText(c.getString(c.getColumnIndex("product_name")));
                name.setTextSize(18.0f);
                name.setTextColor(Color.parseColor(getString(R.color.dark)));
                name.setTypeface(product_sans);
                right.addView(name);

                TextView category = new TextView(this);
                category.setText(c.getString(c.getColumnIndex("name")));
                //right.addView(category);

                TextView price = new TextView(this);
                price.setText("MWK"+c.getString(c.getColumnIndex("price"))+". Each");
                //price.setTextSize(18.0f);
                price.setTextColor(Color.parseColor(getString(R.color.colorAccent)));
                price.setTypeface(price.getTypeface(), Typeface.BOLD);
                right.addView(price);

                TextView qty = new TextView(this);
                qty.setText(Html.fromHtml("Quantity: <b>"+c.getString(c.getColumnIndex("cqty"))+"</b>"));
                qty.setTextColor(Color.parseColor(getString(R.color.dark)));
                right.addView(qty);
                int quantity = c.getInt(c.getColumnIndex("cqty"));
                CartQuantity cartQuantity = new CartQuantity(this,quantity);
                right.addView(cartQuantity.view);
                cartQuantity.setOnQuantityChange(new CartQuantity.QuantityListener(){
                    @Override
                    public void onChanged(int i) {
                        ContentValues cv = new ContentValues();
                        cv.put("qty", i);
                        db.update("cart", cv, "id = ?", new String[]{cart_id});
                        printCart();
                    }
                });

                LinearLayout horizontal = new LinearLayout(this);
                horizontal.setOrientation(LinearLayout.HORIZONTAL);
                right.addView(horizontal);

                TextView remove = new TextView(this);
                remove.setText("Remove");
                remove.setTextColor(Color.parseColor(getString(R.color.red)));
                ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                lp.leftMargin = dpToPx(12);
                remove.setLayoutParams(lp);
                horizontal.addView(remove);

                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db.delete("cart", "id = ?", new String[]{cart_id});
                        printCart();
                    }
                });

                row.addView(right);
                main.addView(row);

                View line = new View(this);
                line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
                line.setBackgroundColor(Color.parseColor("#cccccc"));
                main.addView(line);
            }

            TextView totalPrint = new TextView(this);
            totalPrint.setText("Total: MWK"+total);
            totalPrint.setTextColor(Color.parseColor(getString(R.color.dark)));
            totalPrint.setTypeface(totalPrint.getTypeface(), Typeface.BOLD);
            totalPrint.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10));
            main.addView(totalPrint);

            //print the buttons
            LinearLayout bottom = new LinearLayout(this);
            bottom.setOrientation(LinearLayout.HORIZONTAL);
            bottom.setGravity(Gravity.CENTER);
            bottom.setPadding(0, dpToPx(30), 0, dpToPx(30));

            LinearLayout lleft = new LinearLayout(this), rright = new LinearLayout(this);
            lleft.setLayoutParams(new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT, .5f));
            rright.setLayoutParams(new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT, .5f));
            bottom.addView(lleft);
            bottom.addView(rright);
            lleft.setPadding(dpToPx(7),dpToPx(7),dpToPx(7),dpToPx(7));
            rright.setPadding(dpToPx(7),dpToPx(7),dpToPx(7),dpToPx(7));

            MaterialButton sendQuotation = new MaterialButton(this);
            sendQuotation.setText("Pay Now");
            sendQuotation.setAllCaps(false);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.rightMargin = dpToPx(15);
            sendQuotation.setLayoutParams(params);
            sendQuotation.setOnClickListener(new View.OnClickListener(){
                public void onClick(View p2){
                    AlertDialog.Builder alert = new AlertDialog.Builder(Cart.this);
                    alert.setTitle("Choose Method");
                    alert.setMessage("Select your prefered mode");

                    final RadioGroup rg = new RadioGroup(Cart.this);
                    rg.setOrientation(LinearLayout.VERTICAL);
                    rg.setPadding(30, 10, 30, 10);

                    final RadioButton airtelMpamba = new RadioButton(Cart.this);
                    airtelMpamba.setText("AirtelMoney or TNM Mpamba or FDH");
                    rg.addView(airtelMpamba);

                    final RadioButton visa = new RadioButton(Cart.this);
                    visa.setText("Card (Visa, MasterCard, etc)");
                    rg.addView(visa);

                    final RadioButton paypal = new RadioButton(Cart.this);
                    paypal.setText("PayPal");
                    rg.addView(paypal);

                    alert.setView(rg);

                    alert.setPositiveButton((CharSequence)"Pay", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            try{
                                String mode = "";
                                if(airtelMpamba.isChecked()){
                                    mode = "local";
                                }
                                else if(visa.isChecked()){
                                    mode = "visa";
                                }
                                else if(paypal.isChecked()){
                                    mode = "paypal";
                                }
                                if (!mode.equals("")) {
                                    Intent intent = new Intent(Cart.this, WebPayment.class);
                                    intent.putExtra("mode", mode);
                                    Cart.this.startActivityForResult(intent, 12);
                                }
                                else{
                                    Toast.makeText(Cart.this, "Choose payment mode", Toast.LENGTH_LONG).show();
                                }
                            }
                            catch(Exception ff1){
                                Toast.makeText(Cart.this, ff1.toString(), Toast.LENGTH_LONG).show();
                            }
                            dialog.cancel();
                        }
                    });
                    alert.setNegativeButton((CharSequence)"Cancel", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }
            });
            lleft.addView(sendQuotation);


            MaterialButton clearCart = new MaterialButton(this);
            clearCart.setText("Clear Cart");
            clearCart.setTextColor(Color.WHITE);
            clearCart.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            //clearCart.setBackgroundResource(R.drawable.btn_lg_danger);
            clearCart.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));

            clearCart.setOnClickListener(new View.OnClickListener(){
                public void onClick(View p1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Cart.this);
                    builder.setTitle("Clear Cart");
                    builder.setMessage("Are you sure you want to clear the shopping list?");
                    builder.setPositiveButton((CharSequence)"Okay", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                            db.delete("cart", "id != ?", new String[]{"0"});
                            printCart();
                        }
                    });
                    builder.setNegativeButton((CharSequence)"Cancel", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });

            clearCart.setLayoutParams(params);
            rright.addView(clearCart);

            main.addView(bottom);
        }
        else{
            TextView price = new TextView(this);
            price.setText("Shopping cart is empty");
            price.setTextSize(18.0f);
            price.setTypeface(price.getTypeface(), Typeface.BOLD);
            price.setBackgroundResource(R.drawable.alert_danger);
            price.setTextColor(Color.parseColor("#f44336"));
            main.addView(price);
        }
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        this.finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String trans_id = data.getStringExtra("trans_id");
            String amount = data.getStringExtra("amount");
            String mode = data.getStringExtra("mode");
            Toast.makeText(this, "Trans id: " + trans_id + " and amount is " + amount, Toast.LENGTH_LONG).show();
            //your success code here
            db.delete("cart", "id != ?", new String[]{"0"});
            Cart.this.finish();
            Intent account = new Intent(Cart.this, Purchases.class);
            startActivity(account);
        }
    }
}