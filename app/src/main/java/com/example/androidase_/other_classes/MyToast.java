package com.example.androidase_.other_classes;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast {
    public static void makeToast(Context context, String displayMessage)
    {
        Toast toast = Toast.makeText(context, displayMessage, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.setBackgroundColor(Color.DKGRAY);
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);
        toast.show();
    }
}
