
package com.rivo.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(13, 16, 35);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(174, 181, 207);
    private final int PINK = Color.rgb(244, 65, 157);
    private final int PURPLE = Color.rgb(126, 75, 255);

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(
            int color, int radius) {

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private GradientDrawable gradientButton() {
        GradientDrawable shape = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{PINK, PURPLE}
        );
        shape.setCornerRadius(dp(20));
        return shape;
    }

    private TextView text(
            String value, int size, int color, boolean bold) {

        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return t;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout.LayoutParams params(
            int width, int height) {
        return new LinearLayout.LayoutParams(
                width < 0 ? width : dp(width),
                height < 0 ? height : dp(height)
        );
    }

    private void addSpace(LinearLayout parent, int height) {
        View space = new View(this);
        parent.addView(space, params(1, height));
    }

    private LinearLayout makeCard() {
        LinearLayout card = column();
        card.setPadding(dp(18), dp(17), dp(18), dp(17));
        card.setBackground(rounded(Color.rgb(27, 31, 57), 22));
        return card;
    }

    private void addFeature(
            LinearLayout parent,
            String symbol,
            String heading,
            String description) {

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackground(rounded(Color.rgb(27, 31, 57), 18));

        TextView icon = text(symbol, 25, WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(Color.rgb(55, 43, 91), 15));

        row.addView(icon, params(52, 52));

        LinearLayout details = column();
        details.setPadding(dp(14), 0, 0, 0);

        details.addView(text(heading, 16, WHITE, true));
        addSpace(details, 4);
        details.addView(text(description, 12, MUTED, false));

        row.addView(details,
                new LinearLayout.LayoutParams(0, -2, 1));

        parent.addView(row, params(-1, -2));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = column();
        root.setPadding(dp(22), dp(20), dp(22), dp(28));

        scroll.addView(root);

        // TOP BAR
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.rivo_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

        top.addView(logo, params(48, 48));

        LinearLayout brand = column();
        brand.setPadding(dp(12), 0, 0, 0);
        brand.addView(text("RIVO", 23, WHITE, true));
        brand.addView(text("CONNECT • SHARE", 9, MUTED, true));

        top.addView(brand,
                new LinearLayout.LayoutParams(0, -2, 1));

        TextView status = text("● ONLINE", 11,
                Color.rgb(107, 235, 168), true);
        status.setPadding(dp(10), dp(8), dp(10), dp(8));
        status.setBackground(rounded(Color.rgb(28, 54, 57), 20));
        top.addView(status);

        root.addView(top);
        addSpace(root, 30);

        // HERO
        TextView smallTitle = text(
                "САЛОМ, ХУШ ОМАДӢ!", 12, PINK, true);
        root.addView(smallTitle);

        addSpace(root, 9);

        TextView title = text(
                "Ҳамаи дӯстони ту,\nдар як ҷо.", 31, WHITE, true);
        title.setLineSpacing(dp(4), 1f);
        root.addView(title);

        addSpace(root, 10);

        TextView subtitle = text(
                "Бо RIVO пайваст шав, суҳбат кун\nва лаҳзаҳои хубро мубодила намо.",
                14, MUTED, false);
        subtitle.setLineSpacing(dp(5), 1f);
        root.addView(subtitle);

        addSpace(root, 24);

        // MAIN CARD
        LinearLayout hero = column();
        hero.setPadding(dp(20), dp(22), dp(20), dp(20));
        hero.setBackground(gradientButton());

        TextView heroIcon = text("✦", 36, WHITE, true);
        heroIcon.setGravity(Gravity.CENTER);
        hero.addView(heroIcon, params(-1, 55));

        addSpace(hero, 8);

        TextView heroTitle = text(
                "Ҷаҳони нави ту", 22, WHITE, true);
        heroTitle.setGravity(Gravity.CENTER);
        hero.addView(heroTitle);

        addSpace(hero, 7);

        TextView heroDesc = text(
                "Бо одамон пайваст шав ва\nсуҳбатҳои худро оғоз кун.",
                14, WHITE, false);
        heroDesc.setGravity(Gravity.CENTER);
        heroDesc.setLineSpacing(dp(4), 1f);
        hero.addView(heroDesc);

        addSpace(hero, 18);

        TextView start = text(
                "ОҒОЗ КАРДАН     →", 15, PURPLE, true);
        start.setGravity(Gravity.CENTER);
        start.setBackground(rounded(WHITE, 17));
        hero.addView(start, params(-1, 53));

        start.setOnClickListener(v ->
                Toast.makeText(this,
                        "Қадами навбатӣ: сохтани саҳифаи чат",
                        Toast.LENGTH_SHORT).show());

        root.addView(hero, params(-1, -2));

        addSpace(root, 28);

        // FEATURES
        root.addView(text("ИМКОНИЯТҲОИ RIVO", 13, MUTED, true));
        addSpace(root, 14);

        addFeature(root, "✉", "Паёмҳо",
                "Бо дӯстони худ суҳбат кун");

        addSpace(root, 11);

        addFeature(root, "♧", "Дӯстон",
                "Муошират ва шиносоӣ");

        addSpace(root, 11);

        addFeature(root, "◉", "Профил",
                "Маълумоти шахсии худро бин");

        addSpace(root, 25);

        // FOOTER
        TextView footer = text(
                "RIVO  •  CONNECT. SHARE. DISCOVER.",
                10, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, params(-1, 35));

        setContentView(scroll);
    }
    }
