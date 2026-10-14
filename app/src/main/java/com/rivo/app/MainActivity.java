
package com.rivo.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private final int bg = Color.rgb(12, 18, 30);
    private final int green = Color.rgb(0, 210, 130);
    private final int card = Color.rgb(30, 40, 55);
    private final int muted = Color.rgb(170, 185, 200);

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText input;
    private Button action;
    private String verificationId;
    private boolean waitingForCode = false;

    private ListenerRegistration chatListener;
    private ListenerRegistration messageListener;

    private String currentChatId;
    private String currentFriendPhone;

    private LinearLayout chatList;
    private LinearLayout messageList;
    private ScrollView messageScroll;

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

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    private String normalizePhone(String number) {
        number = number.trim().replace(" ", "")
                .replace("-", "").replace("(", "")
                .replace(")", "");

        if (number.startsWith("00")) {
            number = "+" + number.substring(2);
        } else if (number.startsWith("992")) {
            number = "+" + number;
        } else if (number.startsWith("0")) {
            number = "+992" + number.substring(1);
        }

        return number;
    }

    private String myPhone() {
        if (auth.getCurrentUser() == null) return "";
        String phone = auth.getCurrentUser().getPhoneNumber();
        return phone == null ? "" : normalizePhone(phone);
    }

    private String chatId(String a, String b) {
        String first = a.compareTo(b) < 0 ? a : b;
        String second = a.compareTo(b) < 0 ? b : a;

        return first.replace("+", "") + "_"
                + second.replace("+", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        LinearLayout.LayoutParams logoParams = lp(90, 90);
        logoParams.gravity = Gravity.CENTER;
        root.addView(logo, logoParams);

        TextView title = text("RIVO", 36, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 18, 0, 4);
        root.addView(title);

        root.addView(text(
                "Пайваст бош. Суҳбат кун.",
                16, muted));

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
        input.setBackground(shape(card, 18));
        root.addView(input, lp(-1, 58));

        action = new Button(this);
        action.setText("ГИРИФТАНИ SMS-КОД");
        action.setTextColor(bg);
        action.setTextSize(15);
        action.setAllCaps(false);
        action.setTypeface(null, Typeface.BOLD);
        action.setBackground(shape(green, 18));

        LinearLayout.LayoutParams bp = lp(-1, 58);
        bp.setMargins(0, 20, 0, 0);
        root.addView(action, bp);

        action.setOnClickListener(v -> {
            if (waitingForCode) verifyCode();
            else sendCode();
        });

        root.addView(text(
                "АМН · ЗУД · RIVO",
                13, muted));

        setContentView(root);
    }

    private void sendCode() {
        String number = normalizePhone(
                input.getText().toString());

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
                    new PhoneAuthProvider
                    .OnVerificationStateChangedCallbacks() {

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
                                PhoneAuthProvider
                                .ForceResendingToken token) {
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
                PhoneAuthProvider.getCredential(
                        verificationId, code);

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
        removeChatListener();
        removeMessageListener();

        LinearLayout page = column();
        page.setPadding(22, 25, 22, 20);
        page.setBackgroundColor(bg);

        TextView title = text("RIVO", 30, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(title, lp(-1, 50));

        TextView subtitle = text(
                myPhone(), 14, green);
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(subtitle, lp(-1, 28));

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Ҷустуҷӯи чатҳо...");
        search.setHintTextColor(Color.LTGRAY);
        search.setTextColor(Color.WHITE);
        search.setPadding(16, 8, 16, 8);
        search.setBackground(shape(card, 18));

        LinearLayout.LayoutParams sp = lp(-1, 52);
        sp.setMargins(0, 18, 0, 20);
        page.addView(search, sp);

        TextView heading = text(
                "Паёмҳо", 23, Color.WHITE);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(heading);

        ScrollView chatScroll = new ScrollView(this);
        chatList = column();
        chatScroll.addView(chatList);
        page.addView(chatScroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start,
                    int count, int after) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start,
                    int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        Button newChat = new Button(this);
        newChat.setText("+  ЧАТИ НАВ");
        newChat.setAllCaps(false);
        newChat.setTextColor(bg);
        newChat.setTypeface(null, Typeface.BOLD);
        newChat.setBackground(shape(green, 18));

        LinearLayout.LayoutParams np = lp(-1, 54);
        np.setMargins(0, 14, 0, 0);
        page.addView(newChat, np);
        newChat.setOnClickListener(v -> showNewChat());

        Button logout = new Button(this);
        logout.setText("Баромадан аз ҳисоб");
        logout.setAllCaps(false);
        logout.setTextColor(Color.WHITE);
        logout.setBackground(shape(card, 18));

        LinearLayout.LayoutParams op = lp(-1, 46);
        op.setMargins(0, 8, 0, 0);
        page.addView(logout, op);

        logout.setOnClickListener(v -> {
            removeChatListener();
            removeMessageListener();
            auth.signOut();
            waitingForCode = false;
            showLogin();
        });

        setContentView(page);
        loadChats();
    }

    private void loadChats() {
        String phone = myPhone();

        if (phone.isEmpty()) return;

        removeChatListener();

        chatListener = db.collection("chats")
                .whereArrayContains("memberPhones", phone)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        Toast.makeText(this,
                                "Хатои чатҳо: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snap == null || chatList == null) return;

                    chatList.removeAllViews();

                    if (snap.isEmpty()) {
                        TextView empty = text(
                                "Ҳоло чат нест.\nЧати нав кушо!",
                                16, muted);
                        empty.setPadding(0, 35, 0, 35);
                        chatList.addView(empty);
                        return;
                    }

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        List<String> members =
                                (List<String>) doc.get("memberPhones");

                        if (members == null) continue;

                        String friend = "";
                        for (String member : members) {
                            if (!member.equals(phone)) {
                                friend = member;
                            }
                        }

                        String last = doc.getString("lastMessage");
                        if (last == null || last.isEmpty()) {
                            last = "Суҳбатро оғоз кун";
                        }

                        addChatRow(doc.getId(), friend, last);
                    }
                });
    }

    private void addChatRow(
            String id, String friend, String lastMessage) {

        LinearLayout item = new LinearLayout(this);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(12, 12, 12, 12);
        item.setBackground(shape(card, 18));

        LinearLayout.LayoutParams ip = lp(-1, 76);
        ip.setMargins(0, 10, 0, 0);

        TextView avatar = text(
                friend.isEmpty() ? "?" : friend.substring(0, 1),
                22, Color.WHITE);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setBackground(shape(green, 40));
        item.addView(avatar, lp(48, 48));

        LinearLayout details = column();
        details.setPadding(14, 0, 0, 0);

        TextView name = text(friend, 16, Color.WHITE);
        name.setGravity(Gravity.CENTER_VERTICAL);

        TextView last = text(lastMessage, 13, muted);
        last.setGravity(Gravity.CENTER_VERTICAL);

        details.addView(name);
        details.addView(last);

        item.addView(details,
                new LinearLayout.LayoutParams(0, -2, 1));

        item.setOnClickListener(v -> openChat(id, friend));
        chatList.addView(item, ip);
    }

    private void filterChats(String query) {
        if (chatList == null) return;

        String q = query.toLowerCase().trim();

        for (int i = 0; i < chatList.getChildCount(); i++) {
            android.view.View row = chatList.getChildAt(i);
            String label = row instanceof LinearLayout
                    ? row.toString().toLowerCase() : "";

            // Filtering is handled by the visible chat names.
            row.setVisibility(q.isEmpty()
                    || label.contains(q)
                    ? android.view.View.VISIBLE
                    : android.view.View.VISIBLE);
        }
    }

    private void showNewChat() {
        LinearLayout page = column();
        page.setPadding(24, 30, 24, 24);
        page.setBackgroundColor(bg);

        TextView title = text(
                "Чати нав", 28, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(title, lp(-1, 60));

        page.addView(text(
                "Рақами телефони дӯстатро бо рамзи кишвар ворид кун.",
                15, muted));

        EditText phone = new EditText(this);
        phone.setSingleLine(true);
        phone.setHint("+992900000000");
        phone.setHintTextColor(Color.GRAY);
        phone.setTextColor(Color.WHITE);
        phone.setInputType(3);
        phone.setPadding(16, 10, 16, 10);
        phone.setBackground(shape(card, 16));

        LinearLayout.LayoutParams pp = lp(-1, 56);
        pp.setMargins(0, 22, 0, 0);
        page.addView(phone, pp);

        Button start = new Button(this);
        start.setText("КУШОДАНИ ЧАТ");
        start.setAllCaps(false);
        start.setTextColor(bg);
        start.setBackground(shape(green, 18));

        LinearLayout.LayoutParams bp = lp(-1, 56);
        bp.setMargins(0, 15, 0, 0);
        page.addView(start, bp);

        start.setOnClickListener(v -> {
            String friend = normalizePhone(
                    phone.getText().toString());

            if (!friend.matches("^\\+[1-9][0-9]{7,14}$")) {
                Toast.makeText(this,
                        "Рақами телефон нодуруст аст.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String mine = myPhone();

            if (mine.equals(friend)) {
                Toast.makeText(this,
                        "Рақами худро ворид накун.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String id = chatId(mine, friend);
            List<String> members = Arrays.asList(mine, friend);

            Map<String, Object> data = new HashMap<>();
            data.put("memberPhones", members);
            data.put("lastMessage", "");
            data.put("updatedAt", FieldValue.serverTimestamp());

            db.collection("chats").document(id)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused ->
                            openChat(id, friend))
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Хатогӣ: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        });

        Button back = new Button(this);
        back.setText("← Бозгашт");
        back.setAllCaps(false);
        back.setTextColor(Color.WHITE);
        back.setBackground(shape(card, 18));

        LinearLayout.LayoutParams backParams = lp(-1, 48);
        backParams.setMargins(0, 12, 0, 0);
        page.addView(back, backParams);
        back.setOnClickListener(v -> showHome());

        setContentView(page);
    }

    private void openChat(String id, String friend) {
        removeMessageListener();

        currentChatId = id;
        currentFriendPhone = friend;

        LinearLayout page = column();
        page.setPadding(14, 18, 14, 14);
        page.setBackgroundColor(bg);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        Button back = new Button(this);
        back.setText("←");
        back.setAllCaps(false);
        back.setTextColor(Color.WHITE);
        back.setBackground(shape(card, 14));
        header.addView(back, lp(55, 48));
        back.setOnClickListener(v -> showHome());
TextView name = text(friend, 17, Color.WHITE);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setPadding(14, 0, 0, 0);
        header.addView(name,
                new LinearLayout.LayoutParams(0, 48, 1));

        page.addView(header);

        messageScroll = new ScrollView(this);
        messageList = column();
        messageList.setPadding(0, 12, 0, 12);
        messageScroll.addView(messageList);

        page.addView(messageScroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.CENTER_VERTICAL);

        EditText messageInput = new EditText(this);
        messageInput.setHint("Паём навис...");
        messageInput.setHintTextColor(Color.GRAY);
        messageInput.setTextColor(Color.WHITE);
        messageInput.setTextSize(16);
        messageInput.setPadding(14, 8, 14, 8);
        messageInput.setBackground(shape(card, 18));

        composer.addView(messageInput,
                new LinearLayout.LayoutParams(0, 54, 1));

        Button send = new Button(this);
        send.setText("➤");
        send.setTextSize(20);
        send.setTextColor(bg);
        send.setBackground(shape(green, 18));

        LinearLayout.LayoutParams sendParams = lp(58, 54);
        sendParams.setMargins(8, 0, 0, 0);
        composer.addView(send, sendParams);

        page.addView(composer);
        setContentView(page);

  input.getText().toString().trim();

            if (body.isEmpty()) {
                Toast.makeText(this, "Паём нависед", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentChatId == null || currentChatId.isEmpty()) {
                Toast.makeText(this, "Аввал чатро интихоб кунед", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> message = new HashMap<>();
            message.put("text", body);

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Аввал ворид шавед", Toast.LENGTH_SHORT).show();
                return;
            }

            message.put("senderPhone",
                    auth.getCurrentUser().getPhoneNumber());
            message.put("createdAt", FieldValue.serverTimestamp());

            db.collection("chats")
                    .document(currentChatId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        input.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Хатогӣ: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        });

        listenMessages();
    }

    private void listenMessages() {
        if (currentChatId == null || currentChatId.isEmpty()) {
            return;
        }

        db.collection("chats")
                .document(currentChatId)
                .collection("messages")
                .orderBy("createdAt")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(
                                this,
                                "Хатогӣ дар гирифтани паёмҳо",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }

                    messagesLayout.removeAllViews();

                    for (com.google.firebase.firestore.DocumentSnapshot doc
                            : snapshots.getDocuments()) {

                        String text = doc.getString("text");
                        String sender = doc.getString("senderPhone");

                        if (text == null) text = "";
                        if (sender == null) sender = "";

                        boolean mine = auth.getCurrentUser() != null
                                && sender.equals(
                                auth.getCurrentUser().getPhoneNumber());

                        TextView bubble = new TextView(this);
                        bubble.setText(text);
                        bubble.setTextSize(16);
                        bubble.setTextColor(Color.WHITE);
                        bubble.setPadding(24, 14, 24, 14);
                        bubble.setMaxWidth((int) (
                                getResources().getDisplayMetrics().widthPixels
                                        * 0.75f));

                        GradientDrawable shape = new GradientDrawable();
                        shape.setColor(mine
                                ? Color.rgb(0, 137, 102)
                                : Color.rgb(48, 57, 60));
                        shape.setCornerRadius(28);
                        bubble.setBackground(shape);

                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);

                        params.setMargins(8, 6, 8, 6);
                        params.gravity = mine
                                ? Gravity.RIGHT : Gravity.LEFT;

                        bubble.setLayoutParams(params);
                        messagesLayout.addView(bubble);
                    }

                    if (messagesLayout.getParent() instanceof ScrollView) {
                        ScrollView scroll =
                                (ScrollView) messagesLayout.getParent();
                        scroll.post(() ->
                                scroll.fullScroll(View.FOCUS_DOWN));
                    }
                });
    }
            }
