package com.rodz.adimoshopping.views;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.material.chip.Chip;
import com.rodz.adimoshopping.R;

public class CartQuantity {
    Context _this;
    public LinearLayout view;
    int quantity = 0;
    EditText editText1;
    QuantityListener listener = null;

    public CartQuantity(Context ctx, int quantit){
        this.quantity = quantit;
        _this = ctx;
        view = new LinearLayout(ctx);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Chip chip1 = new Chip(ctx);
        chip1.setText("Dec");
        chip1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chip1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 1){
                    quantity = quantity -1;
                    editText1.setText(String.valueOf(quantity));
                    if (listener != null){
                        listener.onChanged(quantity);
                    }
                }
            }
        });
        view.addView(chip1);

        editText1 = new EditText(ctx);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dpToPx(7);
        editText1.setBackgroundResource(R.drawable.btn_outline_sm);
        editText1.setText(String.valueOf(quantity));
        editText1.setTextColor(Color.BLACK);
        editText1.setLayoutParams(lp);
        view.addView(editText1);

        Chip chip2 = new Chip(ctx);
        chip2.setLayoutParams(lp);
        chip2.setText("Increase");
        chip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity = quantity + 1;
                editText1.setText(String.valueOf(quantity));
                if (listener != null){
                    listener.onChanged(quantity);
                }
            }
        });
        view.addView(chip2);
    }

    public void setOnQuantityChange(QuantityListener listener){
        this.listener = listener;
    }

    public int dpToPx(int dp){
        DisplayMetrics displayMetrics = _this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static class QuantityListener{
        public QuantityListener(){

        }
        public void onChanged(int i){

        }
    }
}
