
package com.rivo.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private final int GREEN = Color.rgb(0, 137, 102);
    private final int DARK = Color.rgb(18, 28, 32);
    private final int PANEL = Color.rgb(35, 47, 51);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(180, 195, 198);

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String verificationId = "";
    private String currentChatId = "";
    private String currentOtherPhone = "";

    private EditText phoneInput;
    private EditText codeInput;
    private EditText input;

    private LinearLayout page;
    private LinearLayout chatsLayout;
    private LinearLayout messagesLayout;
    private ScrollView messagesScroll;

    private ListenerRegistration chatsListener;
    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);

        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.getPhoneNumber() != null) {
            showHome();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(28, 30, 28, 30);
        page.setBackgroundColor(DARK);

        TextView logo = new TextView(this);
        logo.setText("RIVO");
        logo.setTextSize(38);
        logo.setTextColor(GREEN);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Паёмнависии замонавӣ");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);

        TextView heading = new TextView(this);
        heading.setText("Ворид шудан");
        heading.setTextColor(WHITE);
        heading.setTextSize(24);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, 35, 0, 20);

        phoneInput = new EditText(this);
        phoneInput.setSingleLine(true);
        phoneInput.setHint("+992900000000");
        phoneInput.setHintTextColor(MUTED);
        phoneInput.setTextColor(WHITE);
        phoneInput.setTextSize(17);
        phoneInput.setInputType(3);
        styleInput(phoneInput);

        codeInput = new EditText(this);
        codeInput.setSingleLine(true);
        codeInput.setHint("Рамзи SMS");
        codeInput.setHintTextColor(MUTED);
        codeInput.setTextColor(WHITE);
        codeInput.setTextSize(17);
        codeInput.setInputType(2);
        styleInput(codeInput);
        codeInput.setVisibility(View.GONE);

        Button sendCode = makeButton("Гирифтани рамз");
        Button verifyCode = makeButton("Тасдиқ ва воридшавӣ");
        verifyCode.setVisibility(View.GONE);

        sendCode.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();

            if (!phone.startsWith("+") || phone.length() < 8) {
                toast("Рақамро бо рамзи кишвар навис. Масалан: +992900000000");
                return;
            }

            sendCode.setEnabled(false);

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                        @Override
                                        public void onVerificationCompleted(
                                                PhoneAuthCredential credential) {
                                            signInWithCredential(credential);
                                        }

                                        @Override
                                        public void onVerificationFailed(
                                                FirebaseException e) {
                                            sendCode.setEnabled(true);
                                            toast("Хатои тасдиқ: " + e.getMessage());
                                        }

                                        @Override
                                        public void onCodeSent(
                                                String id,
                                                PhoneAuthProvider.ForceResendingToken token) {
                                            verificationId = id;
                                            sendCode.setEnabled(true);
                                            codeInput.setVisibility(View.VISIBLE);
                                            verifyCode.setVisibility(View.VISIBLE);
                                            toast("Рамз фиристода шуд");
                                        }
                                    })
                            .build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        verifyCode.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();

            if (verificationId.isEmpty() || code.isEmpty()) {
                toast("Рамзи SMS-ро ворид кун");
                return;
            }

            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(verificationId, code);

            signInWithCredential(credential);
        });

        page.addView(logo, matchWrap());
        page.addView(subtitle, matchWrap());
        page.addView(heading, matchWrap());
        page.addView(phoneInput, matchWrap());
        page.addView(codeInput, matchWrap());
        page.addView(sendCode, matchWrap());
        page.addView(verifyCode, matchWrap());

        setContentView(page);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    toast("Бомуваффақият ворид шудӣ!");
                    showHome();
                })
                .addOnFailureListener(e ->
                        toast("Воридшавӣ нашуд: " + e.getMessage()));
    }

    private void showHome() {
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(DARK);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(18, 18, 18, 18);
        header.setBackgroundColor(PANEL);

        TextView title = new TextView(this);
        title.setText("RIVO");
        title.setTextColor(GREEN);
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);

        TextView myPhone = new TextView(this);
        FirebaseUser user = auth.getCurrentUser();
        myPhone.setText(user != null && user.getPhoneNumber() != null
                ? user.getPhoneNumber() : "");
        myPhone.setTextColor(MUTED);
        myPhone.setTextSize(12);
        myPhone.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(myPhone, new LinearLayout.LayoutParams(0, -2, 1));

        Button newChat = makeButton("+ Чати нав");
        newChat.setOnClickListener(v -> showNewChat());

        chatsLayout = new LinearLayout(this);
        chatsLayout.setOrientation(LinearLayout.VERTICAL);
        chatsLayout.setPadding(12, 8, 12, 8);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(chatsLayout);

        page.addView(header, matchWrap());
        page.addView(newChat, matchWrap());
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(page);
        loadChats();
    }

    private void loadChats() {
        if (auth.getCurrentUser() == null) return;

        String myPhone = auth.getCurrentUser().getPhoneNumber();
        if (myPhone == null) return;

        if (chatsListener != null) {
            chatsListener.remove();
        }

        chatsListener = db.collection("chats")
                .whereArrayContains("memberPhones", myPhone)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        toast("Хатои гирифтани чатҳо: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || chatsLayout == null) return;

                    chatsLayout.removeAllViews();

                    if (snapshots.isEmpty()) {
                        TextView empty = new TextView(this);
                        empty.setText("Ҳоло чат надорӣ.\nТугмаи «+ Чати нав»-ро пахш кун.");
                        empty.setTextColor(MUTED);
                        empty.setTextSize(16);
                        empty.setGravity(Gravity.CENTER);
                        empty.setPadding(12, 40, 12, 40);
                        chatsLayout.addView(empty);
                        return;
                    }

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        List<String> members =
                                (List<String>) doc.get("memberPhones");

                        if (members == null) continue;

                        String otherPhone = "";

                        for (String member : members) {
                            if (!member.equals(myPhone)) {
                                otherPhone = member;
                                break;
                            }
                        }

                        if (!otherPhone.isEmpty()) {
                            addChatRow(
                                    doc.getId(),
                                    otherPhone,
                                    doc.getString("lastMessage"));
                        }
                    }
                });
    }

    private void addChatRow(
            String chatId,
            String otherPhone,
            String lastMessage) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(14, 14, 14, 14);
        row.setBackground(makeBackground(PANEL, 18));

        TextView avatar = new TextView(this);
        avatar.setText("●");
        avatar.setTextColor(GREEN);
        avatar.setTextSize(30);
        avatar.setGravity(Gravity.CENTER);
        row.addView(avatar, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(12, 0, 0, 0);

        TextView phone = new TextView(this);
        phone.setText(otherPhone);
        phone.setTextColor(WHITE);
        phone.setTextSize(17);
        phone.setTypeface(null, Typeface.BOLD);

        TextView preview = new TextView(this);
        preview.setText(lastMessage == null || lastMessage.isEmpty()
                ? "Оғози суҳбат" : lastMessage);
        preview.setTextColor(MUTED);
        preview.setTextSize(13);
        preview.setMaxLines(1);

        details.addView(phone);
        details.addView(preview);
        row.addView(details, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 5, 0, 5);

        chatsLayout.addView(row, params);
        row.setOnClickListener(v -> openChat(otherPhone));
    }

    private void showNewChat() {
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(20, 20, 20, 20);
        page.setBackgroundColor(DARK);

        Button back = makeButton("← Бозгашт");
        back.setOnClickListener(v -> showHome());

        TextView heading = new TextView(this);
        heading.setText("Чати нав");
        heading.setTextColor(WHITE);
        heading.setTextSize(25);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, 25, 0, 20);

        TextView description = new TextView(this);
        description.setText("Рақами телефони шахси дигарро бо рамзи кишвар навис.");
        description.setTextColor(MUTED);
        description.setTextSize(15);

        EditText otherPhoneInput = new EditText(this);
        otherPhoneInput.setSingleLine(true);
        otherPhoneInput.setHint("+992900000000");
        otherPhoneInput.setHintTextColor(MUTED);
        otherPhoneInput.setTextColor(WHITE);
        otherPhoneInput.setInputType(3);
        styleInput(otherPhoneInput);

        Button start = makeButton("Кушодани чат");
        start.setOnClickListener(v -> {
            String otherPhone = otherPhoneInput.getText().toString().trim();

            if (!otherPhone.startsWith("+") || otherPhone.length() < 8) {
                toast("Рақами дурустро бо + навис");
                return;
            }

            FirebaseUser user = auth.getCurrentUser();

            if (user == null || user.getPhoneNumber() == null) {
                toast("Аввал ба барнома ворид шав");
                showLogin();
                return;
            }

            if (otherPhone.equals(user.getPhoneNumber())) {
                toast("Рақами худатро ворид накун");
                return;
            }

            openChat(otherPhone);
        });

        page.addView(back, matchWrap());
        page.addView(heading, matchWrap());
        page.addView(description, matchWrap());
        page.addView(otherPhoneInput, matchWrap());
        page.addView(start, matchWrap());

        setContentView(page);
    }
    private String makeChatId(String phoneA, String phoneB) {
        if (phoneA == null) phoneA = "";
        if (phoneB == null) phoneB = "";

        if (phoneA.compareTo(phoneB) < 0) {
            return phoneA.replace("+", "") + "_" +
                    phoneB.replace("+", "");
        }

        return phoneB.replace("+", "") + "_" +
                phoneA.replace("+", "");
    }

    private void openChat(String otherPhone) {
        if (auth.getCurrentUser() == null) {
            toast("Аввал ворид шав!");
            showLogin();
            return;
        }

        String myPhone = auth.getCurrentUser().getPhoneNumber();

        if (myPhone == null || myPhone.isEmpty()) {
            toast("Рақами телефони худро тасдиқ кун.");
            return;
        }

        otherPhone = otherPhone.trim();

        if (!otherPhone.startsWith("+") || otherPhone.length() < 8) {
            toast("Рақамро бо рамзи кишвар навис. Масалан: +992900000000");
            return;
        }

        if (myPhone.equals(otherPhone)) {
            toast("Рақами худатро ворид карда наметавонӣ.");
            return;
        }

        currentOtherPhone = otherPhone;
        currentChatId = makeChatId(myPhone, otherPhone);

        final String finalOtherPhone = otherPhone;
        final String finalMyPhone = myPhone;
        final String finalChatId = currentChatId;

        db.collection("chats")
                .document(finalChatId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Map<String, Object> chat = new HashMap<>();
                        chat.put("memberPhones",
                                Arrays.asList(finalMyPhone, finalOtherPhone));
                        chat.put("createdAt",
                                com.google.firebase.firestore.FieldValue.serverTimestamp());
                        chat.put("updatedAt",
                                com.google.firebase.firestore.FieldValue.serverTimestamp());
                        chat.put("lastMessage", "");
                        chat.put("lastSender", "");

                        db.collection("chats")
                                .document(finalChatId)
                                .set(chat)
                                .addOnSuccessListener(unused -> {
                                    currentChatId = finalChatId;
                                    currentOtherPhone = finalOtherPhone;
                                    showChat();
                                })
                                .addOnFailureListener(e ->
                                        toast("Чат сохта нашуд: " + e.getMessage()));
                    } else {
                        currentChatId = finalChatId;
                        currentOtherPhone = finalOtherPhone;
                        showChat();
                    }
                })
                .addOnFailureListener(e ->
                        toast("Хатогӣ: " + e.getMessage()));
    }

    private void showChat() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(12));
        root.setBackground(makeBackground(Color.rgb(12, 25, 23)));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, dp(8), 0, dp(14));

        Button back = makeButton("‹", Color.rgb(35, 55, 50));
        header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView name = new TextView(this);
        name.setText(currentOtherPhone);
        name.setTextColor(Color.WHITE);
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(dp(12), 0, 0, 0);
        header.addView(name, new LinearLayout.LayoutParams(
                0, -2, 1
        ));

        root.addView(header);

        messagesLayout = new LinearLayout(this);
        messagesLayout.setOrientation(LinearLayout.VERTICAL);
        messagesLayout.setPadding(0, dp(8), 0, dp(8));

        messagesScroll = new ScrollView(this);
        messagesScroll.setFillViewport(true);
        messagesScroll.addView(messagesLayout);

        root.addView(messagesScroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                ));

        LinearLayout sendRow = new LinearLayout(this);
        sendRow.setGravity(Gravity.CENTER_VERTICAL);
        sendRow.setPadding(0, dp(12), 0, 0);

        messageInput = new EditText(this);
        styleInput(messageInput, "Паём навис...");
        messageInput.setSingleLine(false);
        messageInput.setMaxLines(4);
        messageInput.setTextSize(15);

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(0, -2, 1);
        inputParams.setMargins(0, 0, dp(8), 0);
        sendRow.addView(messageInput, inputParams);

        Button send = makeButton("➤", Color.rgb(0, 190, 125));
        send.setTextColor(Color.WHITE);
        sendRow.addView(send,
                new LinearLayout.LayoutParams(dp(52), dp(52)));

        root.addView(sendRow);
        setContentView(root);

        back.setOnClickListener(v -> {
            if (messageListener != null) {
                messageListener.remove();
                messageListener = null;
            }
            showHome();
        });

        send.setOnClickListener(v -> sendMessage());
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        listenMessages();
    }

    private void sendMessage() {
        if (auth.getCurrentUser() == null ||
                currentChatId == null ||
                messageInput == null) {
            toast("Чат дастрас нест.");
            return;
        }

        String text = messageInput.getText().toString().trim();

        if (text.isEmpty()) return;

        String senderPhone = auth.getCurrentUser().getPhoneNumber();

        if (senderPhone == null) {
            toast("Рақами тасдиқшуда ёфт нашуд.");
            return;
        }

        String chatId = currentChatId;

        Map<String, Object> message = new HashMap<>();
        message.put("text", text);
        message.put("senderPhone", senderPhone);
        message.put("createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        messageInput.setText("");

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference ->
                        db.collection("chats")
                                .document(chatId)
                                .update(
                                        "lastMessage", text,
                                        "lastSender", senderPhone,
                                        "updatedAt",
                                        com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                                .addOnFailureListener(e ->
                                        toast("Паём фиристода шуд, вале рӯйхати чат нав нашуд.")))
                .addOnFailureListener(e -> {
                    if (messageInput != null) {
                        messageInput.setText(text);
                    }
                    toast("Паём фиристода нашуд: " + e.getMessage());
                });
    }

    private void listenMessages() {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }

        if (currentChatId == null) return;

        messageListener = db.collection("chats")
                .document(currentChatId)
                .collection("messages")
                .orderBy("createdAt",
                        com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        toast("Хатогии паёмҳо: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || messagesLayout == null) return;

                    messagesLayout.removeAllViews();

                    String myPhone = auth.getCurrentUser() == null
                            ? ""
                            : auth.getCurrentUser().getPhoneNumber();

                    for (com.google.firebase.firestore.DocumentSnapshot doc
                            : snapshots.getDocuments()) {

                        String text = doc.getString("text");
                        String sender = doc.getString("senderPhone");

                        if (text == null) text = "";

                        boolean mine = myPhone != null &&
                                myPhone.equals(sender);

                        LinearLayout row = new LinearLayout(this);
                        row.setGravity(mine
                                ? Gravity.END
                                : Gravity.START);

                        TextView bubble = new TextView(this);
                        bubble.setText(text);
                        bubble.setTextColor(Color.WHITE);
                        bubble.setTextSize(15);
                        bubble.setPadding(
                                dp(14), dp(10), dp(14), dp(10));
                        bubble.setMaxWidth(
                                (int) (getResources()
                                        .getDisplayMetrics().widthPixels * 0.75));

                        bubble.setBackground(makeBackground(
                                mine
                                        ? Color.rgb(0, 135, 95)
                                        : Color.rgb(42, 58, 54)
                        ));

                        LinearLayout.LayoutParams bubbleParams =
                                new LinearLayout.LayoutParams(-2, -2);
                        bubbleParams.setMargins(
                                dp(2), dp(4), dp(2), dp(4));

                        row.addView(bubble, bubbleParams);

                        messagesLayout.addView(row,
                                new LinearLayout.LayoutParams(-1, -2));
                    }

                    messagesScroll.post(() ->
                            messagesScroll.fullScroll(View.FOCUS_DOWN));
                });
    }

    private Button makeButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackground(makeBackground(color));
        return button;
    }

    private void styleInput(EditText input, String hint) {
        input.setHint(hint);
        input.setHintTextColor(Color.rgb(155, 175, 168));
        input.setTextColor(Color.WHITE);
        input.setSingleLine(true);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(makeBackground(Color.rgb(35, 49, 45)));
    }

    private android.graphics.drawable.GradientDrawable makeBackground(int color) {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(16));
        return drawable;
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }

        super.onDestroy();
    }
            }

