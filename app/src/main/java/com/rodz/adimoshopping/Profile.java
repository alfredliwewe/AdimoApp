package com.rodz.adimoshopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {
    SQLiteDatabase db;
    User user;
    TextView name;
    TextView phone;
    TextView email;
    ImageView userPicture;
    int screenWidth;
    int thirty;
    int twenty;
    int twentyfive;
    ContextWrapper cw;
    String path;
    String filePath = null;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);

        name = (TextView) findViewById(R.id.nameDisplay);
        phone = (TextView) findViewById(R.id.phoneDisplay);
        email = (TextView) findViewById(R.id.emailDisplay);
        LinearLayout container = (LinearLayout)findViewById(R.id.container);
        userPicture = (ImageView) findViewById(R.id.userPicture);
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

        name.setText(user.name);
        phone.setText(user.phone);
        email.setText(user.email);

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        thirty = (int)(0.3 * screenWidth);
        twenty = (int)(0.2 * screenWidth);
        twentyfive = (int)(0.25 * screenWidth);

        cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        path = directory.getAbsolutePath();

        Bitmap bitmap = loadImageFromStorage(path, user.picture);
        if (bitmap == null) {
            downloadImage(user.picture, userPicture);
        }
        else{
            try{
                userPicture.setImageBitmap(bitmap);
            }
            catch(Exception ff){
                Toast.makeText(this, ff.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void updateName(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit name");
        alert.setIcon(R.drawable.edit);
        alert.setMessage("Enter new username");

        LinearLayout cont = new LinearLayout(this);
        cont.setOrientation(LinearLayout.VERTICAL);
        cont.setPadding(dpToPx(15), dpToPx(5), dpToPx(15), dpToPx(5));

        final EditText input = new EditText(this);
        input.setText(user.name);
        input.setHint("Enter new username");
        cont.addView(input);
        alert.setView(cont);

        alert.setPositiveButton((CharSequence) "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Map<String, Object> params = new LinkedHashMap<>();
                final String name1 = input.getText().toString();
                params.put("updateUsername", user.id);
                params.put("name", name1);

                new Http(new HttpParams(Profile.this, user.link+"/api.php", params){
                    @Override
                    public void onResponse(String text){
                        try{
                            Response res = new Response(text);
                            if (res.status){
                                if (res.get("status").equals("true")){
                                    user.setName(name1);
                                    name.setText(name1);
                                }
                                else{
                                    Toast.makeText(Profile.this, res.get("message"), Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                Toast.makeText(Profile.this, text, Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception ee){
                            //do nothing
                            Toast.makeText(Profile.this, text, Toast.LENGTH_LONG).show();
                        }
                    }
                });
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


    public void updatePhone(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit phone");
        alert.setIcon(R.drawable.edit);
        alert.setMessage("Enter new phone number");

        LinearLayout cont = new LinearLayout(this);
        cont.setOrientation(LinearLayout.VERTICAL);
        cont.setPadding(dpToPx(15), dpToPx(5), dpToPx(15), dpToPx(5));

        final EditText input = new EditText(this);
        input.setText(user.phone);
        input.setHint("Enter new new phone number");
        cont.addView(input);
        alert.setView(cont);

        alert.setPositiveButton((CharSequence) "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Map<String, Object> params = new LinkedHashMap<>();
                final String name1 = input.getText().toString();
                params.put("updatePhone", user.id);
                params.put("phone", name1);

                new Http(new HttpParams(Profile.this, user.link+"/api.php", params){
                    @Override
                    public void onResponse(String text){
                        try{
                            Response res = new Response(text);
                            if (res.status){
                                if (res.get("status").equals("true")){
                                    user.setPhone(name1);
                                    phone.setText(name1);
                                }
                                else{
                                    Toast.makeText(Profile.this, res.get("message"), Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                Toast.makeText(Profile.this, text, Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception ee){
                            //do nothing
                            Toast.makeText(Profile.this, text, Toast.LENGTH_LONG).show();
                        }
                    }
                });
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

    public void updateEmail(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Edit email");
        alert.setIcon(R.drawable.edit);
        alert.setMessage("Enter new email address");

        LinearLayout cont = new LinearLayout(this);
        cont.setOrientation(LinearLayout.VERTICAL);
        cont.setPadding(dpToPx(15), dpToPx(5), dpToPx(15), dpToPx(5));

        final EditText input = new EditText(this);
        input.setText(user.email);
        input.setHint("Enter new email");
        cont.addView(input);
        alert.setView(cont);

        alert.setPositiveButton((CharSequence) "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Map<String, Object> params = new LinkedHashMap<>();
                final String name1 = input.getText().toString();
                params.put("updateEmail", user.id);
                params.put("email", name1);

                new Http(new HttpParams(Profile.this, user.link+"/api.php", params){
                    @Override
                    public void onResponse(String text){
                        try{
                            Response res = new Response(text);
                            if (res.status){
                                if (res.get("status").equals("true")){
                                    user.setEmail(name1);
                                    email.setText(name1);
                                }
                                else{
                                    Toast.makeText(Profile.this, res.get("message"), Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                Toast.makeText(Profile.this, text, Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception ee){
                            //do nothing
                            Toast.makeText(Profile.this, text, Toast.LENGTH_LONG).show();
                        }
                    }
                });
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

    public void logout(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
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
                Profile.this.finish();
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

    public void downloadImage(String filename, ImageView myImage){
        //proceed to download image
        //Toast.makeText(MainActivity.this, "Image saved successfully", Toast.LENGTH_LONG).show();
        final String fname = filename;
        final ImageView iv = myImage;
        final User user = new User(db);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pro_file", fname);

        new Http(new HttpParams(Profile.this, user.link+"/api.php", params){
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
                    Toast.makeText(Profile.this, alf.toString(), Toast.LENGTH_LONG).show();
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

    public void chooseFile(View v){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("image/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            try{
                final Uri imageUri = data.getData();
                filePath = imageUri.getPath();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);

                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                iv.setImageBitmap(selectedImage);
                alert.setView(iv);

                alert.setPositiveButton((CharSequence) "Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do upload
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
            catch(Exception tg){
                Toast.makeText(this, tg.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    public void changePhoto(View v){
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View inf = getLayoutInflater().inflate(R.layout.change_photo, null);
        dialog.setContentView(inf);
        dialog.show();
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
}
