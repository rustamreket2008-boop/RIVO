
package com.rivo.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.rivo_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

        root.addView(logo, new LinearLayout.LayoutParams(
                dp(120), dp(120)));

        TextView title = new TextView(this);
        title.setText("RIVO");
        title.setTextSize(38);
        title.setTextColor(Color.rgb(193, 53, 132));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Connect • Share • Discover");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setGravity(Gravity.CENTER);

        root.addView(subtitle);

        TextView welcome = new TextView(this);
        welcome.setText("Хуш омадӣ ба RIVO!");
        welcome.setTextSize(21);
        welcome.setTextColor(Color.BLACK);
        welcome.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, dp(30), 0, dp(18));

        root.addView(welcome);

        TextView button = new TextView(this);
        button.setText("Оғоз кардан");
        button.setTextSize(17);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.rgb(240, 45, 145),
                        Color.rgb(113, 56, 232)
                });
        gradient.setCornerRadius(dp(28));
        button.setBackground(gradient);

        root.addView(button, new LinearLayout.LayoutParams(
                -1, dp(54)));

        setContentView(root);
    }
          }
      
