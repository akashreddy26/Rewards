package com.example.rewards;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    Activity activity;
    public CustomToast(Activity activity){
        this.activity = activity;
    }
    public void showCustomToast(String message, int colorValue) {
        View v = activity.getLayoutInflater().inflate(R.layout.toast, (ViewGroup) activity.findViewById(R.id.custom_toast));
        TextView textView = v.findViewById(R.id.custom_toast_text);
        CardView cardView = v.findViewById(R.id.card_view_toast);
        textView.setText(message);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 700);
        toast.setDuration(Toast.LENGTH_SHORT);
        if(colorValue == Color.GREEN){
            cardView.setCardBackgroundColor(Color.rgb(0,133,119));
        }else{
            cardView.setCardBackgroundColor(Color.RED);
        }
        toast.setView(v);
        toast.show();
    }


}
