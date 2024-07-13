package com.rodz.adimoshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    SQLiteDatabase db;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DbHelper helper = new DbHelper(this);
        db = helper.getWritableDatabase();
        user = new User(db);

        if (Build.VERSION.SDK_INT >= 21) {
            //change color of the activity
            getWindow().setNavigationBarColor(Color.parseColor(getString(R.color.colorAccent)));
            getWindow().setStatusBarColor(Color.parseColor(getString(R.color.colorAccent)));
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
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

    public void changeUrl(View v){
        //update web address
        final User user = new User(db);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Web Address");
        builder.setMessage("Update the URL address to redirect HTTP requests to");

        final EditText input = new EditText(this);
        input.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        input.setPadding(10,20,10,20);
        input.setText(user.link);

        final LinearLayout display = new LinearLayout(this);
        display.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        display.setOrientation(LinearLayout.VERTICAL);
        display.setPadding(50,2,50,2);
        display.addView(input);
        builder.setView(display);

        builder.setPositiveButton((CharSequence)"Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.setLink(input.getText().toString());
                Toast.makeText(Settings.this, "Link is now updated", Toast.LENGTH_LONG).show();
                dialog.cancel();
                //downloadData();
            }
        });
        builder.setNegativeButton((CharSequence)"Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}