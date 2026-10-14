
package com.rivo.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(13, 16, 35);
    private final int CARD = Color.rgb(27, 31, 57);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(174, 181, 207);
    private final int PINK = Color.rgb(244, 65, 157);
    private final int PURPLE = Color.rgb(126, 75, 255);

    private EditText phoneInput;
    private EditText passwordInput;
    private EditText nameInput;

    private int dp(int n) {
        return (int) (n *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable gradient() {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{PINK, PURPLE});
        d.setCornerRadius(dp(18));
        return d;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private TextView label(String value, int size,
                           int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return t;
    }

    private void space(LinearLayout parent, int height) {
        parent.addView(new android.view.View(this),
                new LinearLayout.LayoutParams(-1, dp(height)));
    }

    private TextView button(String title) {
        TextView b = label(title, 16, WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(gradient());

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(54));
        p.topMargin = dp(12);
        b.setLayoutParams(p);

        return b;
    }

    private EditText field(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setTextSize(15);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(WHITE);
        e.setPadding(dp(16), 0, dp(16), 0);
        e.setInputType(inputType);
        e.setBackground(shape(CARD, 15));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(54));
        p.topMargin = dp(10);
        e.setLayoutParams(p);

        return e;
    }

    private void prepareScreen() {
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
    }

    private LinearLayout baseLayout() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = column();
        root.setPadding(dp(24), dp(25), dp(24), dp(30));
        scroll.addView(root);

        setContentView(scroll);
        return root;
    }

    private void addBrand(LinearLayout root) {
        TextView logo = label("R", 32, WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(gradient());

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(dp(58), dp(58));
        lp.bottomMargin = dp(18);
        root.addView(logo, lp);

        root.addView(label("RIVO", 29, WHITE, true));
        space(root, 5);
        root.addView(label(
                "CONNECT • SHARE • DISCOVER", 11, MUTED, true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareScreen();
        showHome();
    }

    private void showHome() {
        LinearLayout root = baseLayout();

        addBrand(root);
        space(root, 45);

        root.addView(label(
                "Ҳамаи дӯстони ту,\nдар як ҷо.", 30, WHITE, true));

        space(root, 14);

        TextView description = label(
                "Бо RIVO пайваст шав, суҳбат кун\n"
                + "ва лаҳзаҳои худро мубодила намо.",
                15, MUTED, false);
        description.setLineSpacing(dp(5), 1f);
        root.addView(description);

        space(root, 30);

        LinearLayout card = column();
        card.setPadding(dp(20), dp(22), dp(20), dp(22));
        card.setBackground(shape(CARD, 22));

        card.addView(label("✦  Ҷаҳони нави ту", 21, WHITE, true));
        space(card, 12);
        card.addView(label(
                "Бо дӯстони худ робита барқарор кун.",
                14, MUTED, false));

        TextView login = button("ВОРИД ШУДАН");
        login.setOnClickListener(v -> showLogin());
        card.addView(login);

        TextView register = label(
                "Ҳисоб надорӣ?  Сабти ном кун", 14, WHITE, true);
        register.setGravity(Gravity.CENTER);
        register.setPadding(0, dp(22), 0, dp(8));
        register.setOnClickListener(v -> showRegister());
        card.addView(register);

        root.addView(card,
                new LinearLayout.LayoutParams(-1, -2));

        space(root, 28);

        TextView footer = label(
                "RIVO • Муошират осонтар мешавад",
                12, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
    }

    private void showLogin() {
        LinearLayout root = baseLayout();

        TextView back = label("←  Бозгашт", 15, MUTED, true);
        back.setOnClickListener(v -> showHome());
        root.addView(back);

        space(root, 35);
        addBrand(root);
        space(root, 35);

        root.addView(label("Хуш омадӣ!", 27, WHITE, true));
        space(root, 8);
        root.addView(label(
                "Ба ҳисоби RIVO-и худ ворид шав.",
                14, MUTED, false));

        space(root, 25);

        root.addView(label("Рақами телефон", 14, WHITE, true));
        phoneInput = field("Рақами телефон", 3);
        root.addView(phoneInput);

        space(root, 18);
        root.addView(label("Рамз", 14, WHITE, true));
        passwordInput = field(
                "Рамзи худро ворид кун", 129);
        root.addView(passwordInput);

        space(root, 15);

        TextView enter = button("ВОРИД ШУДАН");
        enter.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            String password =
                    passwordInput.getText().toString();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Ҳамаи майдонҳоро пур кун",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Қадами навбатӣ: пайваст кардани Firebase",
                        Toast.LENGTH_LONG).show();
            }
        });
        root.addView(enter);

        TextView register = label(
                "Ҳисоб надорӣ? Сабти ном кун",
                14, WHITE, true);
        register.setGravity(Gravity.CENTER);
        register.setPadding(0, dp(24), 0, dp(10));
        register.setOnClickListener(v -> showRegister());
        root.addView(register);
    }

    private void showRegister() {
        LinearLayout root = baseLayout();

        TextView back = label("←  Бозгашт", 15, MUTED, true);
        back.setOnClickListener(v -> showHome());
        root.addView(back);

        space(root, 30);
        addBrand(root);
        space(root, 30);

        root.addView(label("Ҳисоб соз", 27, WHITE, true));
        space(root, 8);
        root.addView(label(
                "Ба оилаи RIVO ҳамроҳ шав.",
                14, MUTED, false));

        space(root, 22);

        root.addView(label("Номи ту", 14, WHITE, true));
        nameInput = field("Ном ва насаб", 1);
        root.addView(nameInput);

        space(root, 15);
        root.addView(label("Рақами телефон", 14, WHITE, true));
        phoneInput = field("Рақами телефон", 3);
        root.addView(phoneInput);

        space(root, 15);
        root.addView(label("Сохтани рамз", 14, WHITE, true));
        passwordInput = field("Камаш 6 аломат", 129);
        root.addView(passwordInput);

        space(root, 15);

        TextView create = button("СОХТАНИ ҲИСОБ");
        create.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (name.isEmpty() || phone.isEmpty()
                    || password.isEmpty()) {
                Toast.makeText(this,
                        "Ҳамаи майдонҳоро пур кун",
                        Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(this,
                        "Рамз бояд камаш 6 аломат дошта бошад",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Интерфейс омода аст. Барои сохтани ҳисоб Firebase лозим аст.",
                        Toast.LENGTH_LONG).show();
            }
        });
        root.addView(create);

        TextView login = label(
                "Аллакай ҳисоб дорӣ? Ворид шав",
                14, WHITE, true);
        login.setGravity(Gravity.CENTER);
        login.setPadding(0, dp(22), 0, dp(10));
        login.setOnClickListener(v -> showLogin());
        root.addView(login);
    }
            }
