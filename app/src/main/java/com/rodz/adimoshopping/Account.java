package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*; 
import android.database.sqlite.*;
import android.database.*;

import java.io.File;
import java.io.FileInputStream;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

public class Account extends AppCompatActivity {
    SQLiteDatabase db;
    LinearLayout main;
    String mode;
    int screenWidth;
    User user;
    ContextWrapper cw;
    String path;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);

        main = (LinearLayout)findViewById(R.id.main);
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

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        Intent this_intent = getIntent();
        mode = this_intent.getStringExtra("mode");

        user = new User(db);
        if (user.status) {
            loadProfile();
        }
        else{
            loadLogin(main);
        }
    }

    public void loadLogin(View v){
        getSupportActionBar().show();
        getSupportActionBar().setTitle("Login");
        main.removeAllViews();

        View cont = this.getLayoutInflater().inflate(R.layout.login, null);
        main.addView(cont);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        this.finish();
        return super.onOptionsItemSelected(item);
    }

    public void loadProfile(){
        getSupportActionBar().setTitle("Manage Account");
        //getActionBar().hide();
        main.removeAllViews();

        View cont = this.getLayoutInflater().inflate(R.layout.profile, null);
        main.addView(cont);

        //set email and username
        TextView username = (TextView) findViewById(R.id.username);
        TextView emailAddress = (TextView) findViewById(R.id.emailAddress);

        username.setText(user.name);
        emailAddress.setText(user.email);

        //show profile image
        ImageView pro = (ImageView) findViewById(R.id.profile_home);

        Bitmap bitmap = loadImageFromStorage(path, user.picture);
        if (bitmap == null) {
            //downloadProfile(user.picture, pro);
        }
        else {
            try {
                pro.setImageBitmap(bitmap);
            } catch (Exception ee) {
            }
        }
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

    public void loadRegister(View v){
        getSupportActionBar().show();
        getSupportActionBar().setTitle("SignUp");
        main.removeAllViews();

        View cont = this.getLayoutInflater().inflate(R.layout.register, null);
        main.addView(cont);
    }

    public void signup(View v){
        EditText username = (EditText)findViewById(R.id.username);
        EditText email = (EditText)findViewById(R.id.email);
        EditText contact = (EditText)findViewById(R.id.contact);
        EditText password = (EditText)findViewById(R.id.password);
        EditText confirmPassword = (EditText)findViewById(R.id.confirmPassword);

        if (password.getText().toString().equals(confirmPassword.getText().toString())) {
            if (password.getText().toString().length() > 5) {
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("username", username.getText().toString());
                params.put("email", email.getText().toString());
                params.put("contact", contact.getText().toString());
                params.put("password", password.getText().toString());
                new Http(new HttpParams(Account.this, user.link+"/api.php", params){
                    @Override
                    public void onResponse(String text){
                        try{
                            JSONObject res = new JSONObject(text);
                            if (res.getBoolean("status")) {
                                //has registered
                                db.execSQL("INSERT INTO user (id, webid, name, phone, email, type, file) VALUES (NULL, ?, ?, ?, ?, ?, ?)",
                                        new String[]{res.getString("id"), res.getString("name"), res.getString("phone"), res.getString("email"), "customer", res.getString("resampled")});
                                user = new User(db);
                                if(mode != null){
                                    if(mode.equals("pay")){
                                        loadProfile();
                                        startActivity(new Intent(Account.this, Cart.class));
                                        //Account.this.finish();
                                    }
                                    else{
                                        loadProfile();
                                    }
                                }
                                else{
                                    loadProfile();
                                }
                            }
                            else{
                                Toast.makeText(Account.this, res.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch(Exception dd){
                            System.out.println(text);
                            dd.printStackTrace();
                            Toast.makeText(Account.this, text, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else{
                Toast.makeText(Account.this, "Password must be 6 characters or longer ", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void next(View v){
        Intent intent = new Intent(this, Profile.class);
        startActivityForResult(intent, 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        this.finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void login(View v){
        EditText email = (EditText)findViewById(R.id.email);
        EditText password = (EditText)findViewById(R.id.password);
        
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("email", email.getText().toString());
        params.put("password", password.getText().toString());
        new Http(new HttpParams(Account.this, user.link+"/api.php", params){
            @Override
            public void onResponse(String text){
                try{
                    JSONObject res = new JSONObject(text);
                    if (res.getBoolean("status")) {
                        //has registered
                        db.execSQL("INSERT INTO user (id, webid, name, phone, email, type, file) VALUES (NULL, ?, ?, ?, ?, ?, ?)",
                                new String[]{res.getString("id"), res.getString("name"), res.getString("phone"), res.getString("email"), "customer", res.getString("resampled")});
                        user = new User(db);
                        if(mode != null){
                            if(mode.equals("pay")){
                                loadProfile();
                                startActivity(new Intent(Account.this, Cart.class));
                                //Account.this.finish();
                            }
                            else{
                                loadProfile();
                            }
                        }
                        else{
                            loadProfile();
                        }
                    }
                    else{
                        Toast.makeText(Account.this, res.getString("message"), Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception dd){
                    Toast.makeText(Account.this, "("+text+")"+dd.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }); 
    }

    public void payments(View v){
        startActivity(new Intent(this, Purchases.class));
    }

    public void notifications(View view) {
        Intent intent = new Intent(this, GeneralPurpose.class);
        intent.putExtra("action", "notifications");
        startActivity(intent);
    }

    public void help(View view) {
        Intent intent = new Intent(this, GeneralPurpose.class);
        intent.putExtra("action", "help");
        startActivity(intent);
    }

    public void goSettings(View view) {
        Intent intent = new Intent(this, Settings.class);
        intent.putExtra("action", "help");
        startActivity(intent);
    }

    public void logout(View v){
        androidx.appcompat.app.AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Logout");
        //alert.setIcon(R.drawable.edit);
        alert.setMessage("Are you sure you want to logout");

        alert.setPositiveButton((CharSequence) "Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.delete("user", "id != ?", new String[]{"0"});
                db.delete("purchase", "id != ?", new String[]{"0"});
                Intent resultIntent = new Intent();

                setResult(Activity.RESULT_OK, resultIntent);
                Account.this.finish();
            }
        });
        alert.setNegativeButton((CharSequence) "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert.show();
    }
}