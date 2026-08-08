package com.justplayer.urllauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private EditText urlInput;
    private LinearLayout historyLayout;
    private SharedPreferences preferences;

    private static final String PREFS = "url_history";
    private static final String URLS = "urls";
    private static final int MAX_HISTORY = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        buildUI();
        loadHistory();
    }

    private void buildUI() {

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(60, 40, 60, 40);

        TextView title = new TextView(this);
        title.setText("Just Player URL Launcher");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        mainLayout.addView(title);

        urlInput = new EditText(this);
        urlInput.setHint("Paste video URL here");
        urlInput.setSingleLine(true);
        urlInput.setTextSize(20);

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        inputParams.topMargin = 30;
        mainLayout.addView(urlInput, inputParams);

        Button openButton = new Button(this);
        openButton.setText("OPEN IN JUST PLAYER");
        openButton.setTextSize(18);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = 20;
        mainLayout.addView(openButton, buttonParams);

        openButton.setOnClickListener(v -> openCurrentUrl());

        TextView historyTitle = new TextView(this);
        historyTitle.setText("Last-used URLs");
        historyTitle.setTextSize(22);
        historyTitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams historyTitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        historyTitleParams.topMargin = 40;
        mainLayout.addView(historyTitle, historyTitleParams);

        historyLayout = new LinearLayout(this);
        historyLayout.setOrientation(LinearLayout.VERTICAL);

        mainLayout.addView(historyLayout);

        Button clearButton = new Button(this);
        clearButton.setText("CLEAR HISTORY");

        LinearLayout.LayoutParams clearParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        clearParams.topMargin = 20;
        mainLayout.addView(clearButton, clearParams);

        clearButton.setOnClickListener(v -> clearHistory());

        setContentView(mainLayout);

        urlInput.requestFocus();
    }

    private void openCurrentUrl() {

        String url = urlInput.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please enter a video URL",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        saveUrl(url);
        openInJustPlayer(url);
    }

    private void openInJustPlayer(String url) {

        try {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            intent.setPackage("com.brouken.player");

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Just Player could not open this URL",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void saveUrl(String url) {

        Set<String> saved =
                preferences.getStringSet(URLS, new HashSet<>());

        List<String> urls = new ArrayList<>(saved);

        // Remove duplicate
        urls.remove(url);

        // Newest URL goes first
        urls.add(0, url);

        // Keep only last 10
        while (urls.size() > MAX_HISTORY) {
            urls.remove(urls.size() - 1);
        }

        preferences.edit()
                .putStringSet(URLS, new HashSet<>(urls))
                .apply();

        loadHistory();
    }

    private void loadHistory() {

        if (historyLayout == null) {
            return;
        }

        historyLayout.removeAllViews();

        Set<String> saved =
                preferences.getStringSet(URLS, new HashSet<>());

        List<String> urls = new ArrayList<>(saved);

        if (urls.isEmpty()) {

            TextView empty = new TextView(this);
            empty.setText("No URLs used yet");
            empty.setTextSize(18);
            empty.setGravity(Gravity.CENTER);

            historyLayout.addView(empty);

            return;
        }

        for (String url : urls) {

            Button historyButton = new Button(this);

            historyButton.setText(url);
            historyButton.setTextSize(16);

            historyButton.setOnClickListener(v -> {
                urlInput.setText(url);
                openInJustPlayer(url);
            });

            historyLayout.addView(historyButton);
        }
    }

    private void clearHistory() {

        preferences.edit()
                .remove(URLS)
                .apply();

        loadHistory();

        Toast.makeText(
                this,
                "History cleared",
                Toast.LENGTH_SHORT
        ).show();
    }
}
