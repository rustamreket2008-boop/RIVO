
package com.rivo.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private final int bg = Color.rgb(12, 18, 30);
    private final int green = Color.rgb(0, 210, 130);

    private LinearLayout root;
    private EditText input;
    private Button action;
    private FirebaseAuth auth;
    private String verificationId;
    private boolean waitingForCode = false;

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private TextView text(String value, int size, int color) {
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

        auth = FirebaseAuth.getInstance();
        showLogin();
    }

    private void showLogin() {
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

        root.addView(text(
                "Пайваст бош. Суҳбат кун.",
                16, Color.rgb(170, 185, 200)));

        TextView label = text(
                "Воридшавӣ бо рақами телефон",
                17, Color.WHITE);
        label.setPadding(0, 40, 0, 12);
        root.addView(label);

        input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("+992900000000");
        input.setHintTextColor(Color.GRAY);
        input.setTextColor(Color.WHITE);
        input.setTextSize(18);
        input.setInputType(3);
        input.setPadding(18, 12, 18, 12);
        input.setBackground(shape(
                Color.rgb(30, 40, 55), 18));

        root.addView(input, new LinearLayout.LayoutParams(
                -1, 58));

        action = new Button(this);
        action.setText("ГИРИФТАНИ SMS-КОД");
        action.setTextColor(bg);
        action.setAllCaps(false);
        action.setTextSize(15);
        action.setTypeface(null, Typeface.BOLD);
        action.setBackground(shape(green, 18));

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(-1, 58);
        bp.setMargins(0, 20, 0, 0);
        root.addView(action, bp);

        action.setOnClickListener(v -> {
            if (!waitingForCode) {
                sendCode();
            } else {
                verifyCode();
            }
        });

        TextView note = text(
                "Паёмнависии замонавӣ • RIVO",
                13, Color.rgb(130, 145, 160));
        note.setPadding(0, 30, 0, 0);
        root.addView(note);

        setContentView(root);
    }

    private void sendCode() {
        String number = input.getText().toString().trim();

        if (number.startsWith("992")) {
            number = "+" + number;
        } else if (number.startsWith("0")) {
            number = "+992" + number.substring(1);
        }

        if (!number.matches("^\\+[1-9][0-9]{7,14}$")) {
            Toast.makeText(this,
                    "Рақамро бо рамзи кишвар навис: +992...",
                    Toast.LENGTH_LONG).show();
            return;
        }

        final String phoneNumber = number;
        action.setEnabled(false);
        action.setText("ИНТИЗОР ШАВЕД...");

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {
                        signIn(credential);
                    }

                    @Override
                    public void onVerificationFailed(
                            FirebaseException e) {
                        action.setEnabled(true);
                        action.setText("АЗ НАВ КӮШИШ КУН");

                        Toast.makeText(MainActivity.this,
                                "Хатогӣ: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(
                            String id,
                            PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = id;
                        waitingForCode = true;

                        input.setText("");
                        input.setHint("SMS-коди 6-рақама");
                        input.setInputType(2);

                        action.setEnabled(true);
                        action.setText("ТАСДИҚ КАРДАН");

                        Toast.makeText(MainActivity.this,
                                "SMS-код фиристода шуд.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        String code = input.getText().toString().trim();

        if (code.length() != 6 || verificationId == null) {
            Toast.makeText(this,
                    "Коди 6-рақамаро ворид кун.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        action.setEnabled(false);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(
                        verificationId, code);

        signIn(credential);
    }

    private void signIn(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    action.setEnabled(true);

                    if (task.isSuccessful()) {
                        showWelcome();
                    } else {
                        action.setText("АЗ НАВ КӮШИШ КУН");

                        Toast.makeText(this,
                                "Тасдиқ нашуд. Кодро санҷ.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showWelcome() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(25, 25, 25, 25);
        page.setBackgroundColor(bg);

        TextView logo = text("R", 42, Color.WHITE);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setBackground(shape(green, 35));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(90, 90);
        lp.gravity = Gravity.CENTER;
        page.addView(logo, lp);

        TextView title = text("ХУШ ОМАДӢ БА RIVO!", 25, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 25, 0, 15);
        page.addView(title);

        page.addView(text(
                "Рақами телефони ту тасдиқ шуд.",
                16, Color.LTGRAY));

        setContentView(page);
    }
            }
                    
