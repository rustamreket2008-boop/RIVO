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
import android.text.TextWatcher;
import android.text.Editable;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private final int bg = Color.rgb(12, 18, 30);
    private final int green = Color.rgb(0, 210, 130);
    private final int card = Color.rgb(30, 40, 55);
    private final int muted = Color.rgb(170, 185, 200);

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

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout.LayoutParams params(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            showHome();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        LinearLayout root = column();
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 30, 28, 30);
        root.setBackgroundColor(bg);

        TextView logo = text("R", 42, Color.WHITE);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setBackground(shape(green, 35));

        LinearLayout.LayoutParams logoParams = params(90, 90);
        logoParams.gravity = Gravity.CENTER;
        root.addView(logo, logoParams);

        TextView title = text("RIVO", 36, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 18, 0, 4);
        root.addView(title);

        root.addView(text("Пайваст бош. Суҳбат кун.", 16, muted));

        TextView label = text(
                "Воридшавӣ бо рақами телефон", 17, Color.WHITE);
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
        input.setBackground(shape(card, 18));
        root.addView(input, params(-1, 58));

        action = new Button(this);
        action.setText("ГИРИФТАНИ SMS-КОД");
        action.setTextColor(bg);
        action.setTextSize(15);
        action.setAllCaps(false);
        action.setTypeface(null, Typeface.BOLD);
        action.setBackground(shape(green, 18));

        LinearLayout.LayoutParams bp = params(-1, 58);
        bp.setMargins(0, 20, 0, 0);
        root.addView(action, bp);

        action.setOnClickListener(v -> {
            if (waitingForCode) {
                verifyCode();
            } else {
                sendCode();
            }
        });

        TextView note = text("АМН · ЗУД · RIVO", 13, muted);
        note.setPadding(0, 28, 0, 0);
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
                    "Рақамро дуруст навис: +992...",
                    Toast.LENGTH_LONG).show();
            return;
        }

        action.setEnabled(false);
        action.setText("ИНТИЗОР ШАВЕД...");

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

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
        action.setText("ТАСДИҚ ШУДА ИСТОДААСТ...");

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);

        signIn(credential);
    }

    private void signIn(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showHome();
                    } else {
                        action.setEnabled(true);
                        action.setText("АЗ НАВ КӮШИШ КУН");

                        Toast.makeText(this,
                                "Тасдиқ нашуд. Кодро санҷ.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showHome() {
        LinearLayout page = column();
        page.setPadding(22, 25, 22, 20);
        page.setBackgroundColor(bg);

        TextView logo = text("RIVO", 30, Color.WHITE);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(logo, params(-1, 55));

        TextView subtitle = text(
                "Пайваст бош. Суҳбат кун.", 15, muted);
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(subtitle);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("⌕  Ҷустуҷӯи чатҳо...");
        search.setHintTextColor(Color.LTGRAY);
        search.setTextColor(Color.WHITE);
        search.setTextSize(16);
        search.setPadding(18, 8, 18, 8);
        search.setBackground(shape(card, 18));

        LinearLayout.LayoutParams sp = params(-1, 55);
        sp.setMargins(0, 24, 0, 22);
        page.addView(search, sp);

        TextView heading = text("Паёмҳо", 23, Color.WHITE);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(heading);

        LinearLayout list = column();

        String[] names = {
                "Дӯстони RIVO",
                "Оилаи ман",
                "Чати нав"
        };

        String[] messages = {
                "Суҳбатро оғоз кун",
                "Ба оила паём навис",
                "Барои ҷустуҷӯи одамон пахш кун"
        };

        for (int i = 0; i < names.length; i++) {
            final String name = names[i];

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(12, 12, 12, 12);
            item.setBackground(shape(card, 18));

            LinearLayout.LayoutParams ip = params(-1, 78);
            ip.setMargins(0, 12, 0, 0);

            TextView avatar = text(
                    name.substring(0, 1), 23, Color.WHITE);
            avatar.setTypeface(null, Typeface.BOLD);
            avatar.setBackground(shape(green, 50));
            item.addView(avatar, params(48, 48));

            LinearLayout details = column();
            details.setPadding(14, 0, 0, 0);

            TextView title = text(name, 17, Color.WHITE);
            title.setGravity(Gravity.CENTER_VERTICAL);

            TextView message = text(messages[i], 13, muted);
            message.setGravity(Gravity.CENTER_VERTICAL);

            details.addView(title);
            details.addView(message);

            item.addView(details, params(0, -2));
            ((LinearLayout.LayoutParams)
                    details.getLayoutParams()).weight = 1;

            item.setOnClickListener(v -> {
                if (name.equals("Чати нав")) {
                    showNewChat();
                } else {
                    Toast.makeText(this,
                            "Ин чат ҳоло ба сервер пайваст нест.",
                            Toast.LENGTH_LONG).show();
                }
            });

            list.addView(item, ip);
        }

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                list.removeAllViews();

                for (int i = 0; i < names.length; i++) {
                    if (names[i].toLowerCase().contains(
                            s.toString().toLowerCase())) {
                        final String name = names[i];

                        LinearLayout item = new LinearLayout(
                                MainActivity.this);
                        item.setGravity(Gravity.CENTER_VERTICAL);
                        item.setPadding(12, 12, 12, 12);
                        item.setBackground(shape(card, 18));

                        LinearLayout.LayoutParams ip =
                                params(-1, 72);
                        ip.setMargins(0, 10, 0, 0);

                        TextView av = text(
                                name.substring(0, 1), 22, Color.WHITE);
                        av.setBackground(shape(green, 40));
                        item.addView(av, params(46, 46));

                        TextView nm = text(name, 16, Color.WHITE);
                        nm.setPadding(14, 0, 0, 0);
                        nm.setGravity(Gravity.CENTER_VERTICAL);
                        item.addView(nm);

                        item.setOnClickListener(v -> {
                            if (name.equals("Чати нав")) {
                                showNewChat();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Чат ҳоло пайваст нест.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        list.addView(item, ip);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        page.addView(list);

        Button newChat = new Button(this);
        newChat.setText("+  ЧАТИ НАВ");
        newChat.setTextColor(bg);
        newChat.setAllCaps(false);
        newChat.setTextSize(16);
        newChat.setTypeface(null, Typeface.BOLD);
        newChat.setBackground(shape(green, 18));

        LinearLayout.LayoutParams np = params(-1, 56);
        np.setMargins(0, 22, 0, 0);
        page.addView(newChat, np);
        newChat.setOnClickListener(v -> showNewChat());

        Button logout = new Button(this);
        logout.setText("Баромадан аз ҳисоб");
        logout.setTextColor(Color.WHITE);
        logout.setAllCaps(false);
        logout.setBackground(shape(card, 18));

        LinearLayout.LayoutParams op = params(-1, 48);
        op.setMargins(0, 10, 0, 0);
        page.addView(logout, op);

        logout.setOnClickListener(v -> {
            auth.signOut();
            waitingForCode = false;
            showLogin();
        });

        setContentView(page);
    }

    private void showNewChat() {
        LinearLayout page = column();
        page.setPadding(24, 30, 24, 24);
        page.setBackgroundColor(bg);

        TextView title = text("Чати нав", 28, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(title, params(-1, 60));

        page.addView(text(
                "Барои оғоз рақами телефони дӯстатро ворид кун.",
                15, muted));

        EditText phone = new EditText(this);
        phone.setSingleLine(true);
        phone.setHint("+992900000000");
        phone.setHintTextColor(Color.GRAY);
        phone.setTextColor(Color.WHITE);
        phone.setInputType(3);
        phone.setPadding(16, 10, 16, 10);
        phone.setBackground(shape(card, 16));

        LinearLayout.LayoutParams pp = params(-1, 56);
        pp.setMargins(0, 22, 0, 0);
        page.addView(phone, pp);

        Button find = new Button(this);
        find.setText("ҶУСТУҶӮИ ИСТИФОДАБАРАНДА");
        find.setAllCaps(false);
        find.setTextColor(bg);
        find.setBackground(shape(green, 18));

        LinearLayout.LayoutParams fp = params(-1, 56);
        fp.setMargins(0, 15, 0, 0);
        page.addView(find, fp);

        find.setOnClickListener(v ->
                Toast.makeText(this,
                        "Ҷустуҷӯи воқеӣ баъди пайваст кардани Firestore фаъол мешавад.",
                        Toast.LENGTH_LONG).show());

        Button back = new Button(this);
        back.setText("← Бозгашт");
        back.setAllCaps(false);
        back.setTextColor(Color.WHITE);
        back.setBackground(shape(card, 18));

        LinearLayout.LayoutParams bp = params(-1, 50);
        bp.setMargins(0, 12, 0, 0);
        page.addView(back, bp);
        back.setOnClickListener(v -> showHome());

        setContentView(page);
    }
                        }
                      
