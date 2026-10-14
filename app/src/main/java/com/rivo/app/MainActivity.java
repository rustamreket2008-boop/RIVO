
package com.rivo.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    int bg = Color.rgb(12, 18, 30);
    int green = Color.rgb(0, 210, 130);
    LinearLayout root;
    EditText phone;

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 35, 28, 35);
        root.setBackgroundColor(bg);

        TextView logo = text("R", 42, Color.WHITE);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setBackground(shape(green, 35));

        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(90, 90);
        lp.gravity = Gravity.CENTER;
        root.addView(logo, lp);

        TextView title = text("RIVO", 34, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 20, 0, 5);
        root.addView(title);

        TextView subtitle = text(
            "Пайваст бош. Суҳбат кун.", 16,
            Color.rgb(170, 185, 200)
        );
        root.addView(subtitle);

        TextView label = text(
            "Воридшавӣ бо рақами телефон", 17, Color.WHITE
        );
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setPadding(0, 45, 0, 12);
        root.addView(label);

        phone = new EditText(this);
        phone.setSingleLine(true);
        phone.setHint("+992 900 00 00 00");
        phone.setHintTextColor(Color.GRAY);
        phone.setTextColor(Color.WHITE);
        phone.setTextSize(18);
        phone.setInputType(3);
        phone.setPadding(18, 14, 18, 14);
        phone.setBackground(shape(Color.rgb(30, 40, 55), 18));

        root.addView(phone, new LinearLayout.LayoutParams(
            -1, 58
        ));

        Button next = new Button(this);
        next.setText("ИДОМА →");
        next.setTextColor(bg);
        next.setTextSize(16);
        next.setAllCaps(false);
        next.setTypeface(null, Typeface.BOLD);
        next.setBackground(shape(green, 18));

        LinearLayout.LayoutParams bp =
            new LinearLayout.LayoutParams(-1, 58);
        bp.setMargins(0, 20, 0, 0);
        root.addView(next, bp);

        next.setOnClickListener(v -> {
            String number = phone.getText().toString().trim();

            if (number.isEmpty() || number.length() < 9) {
                Toast.makeText(this,
                    "Рақами телефонро дуруст навис!",
                    Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                    "Қадами навбатӣ: пайваст кардани Firebase SMS",
                    Toast.LENGTH_LONG).show();
            }
        });

        TextView note = text(
            "Паёмнависии замонавӣ • RIVO",
            13, Color.rgb(130, 145, 160)
        );
        note.setPadding(0, 35, 0, 0);
        root.addView(note);

        setContentView(root);
    }
}
