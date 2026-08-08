package com.justplayer.urllauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private EditText urlInput;

    private LinearLayout historyLayout;
    private LinearLayout favouritesLayout;

    private SharedPreferences prefs;

    private static final String PREFS = "player_data";
    private static final String HISTORY = "history_data";
    private static final String FAVOURITES = "favourite_data";

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        prefs = getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );

        createUI();

        refreshHistory();
        refreshFavourites();
    }

    private void createUI() {

        ScrollView mainScroll = new ScrollView(this);

        mainScroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(55),
                dp(30),
                dp(55),
                dp(40)
        );

        root.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        mainScroll.addView(root);

        // ---------------- TITLE ----------------

        TextView title = new TextView(this);

        title.setText("JUST PLAYER");
        title.setTextSize(38);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        root.addView(
                title,
                fullParams()
        );

        TextView subtitle = new TextView(this);

        subtitle.setText(
                "Paste a video URL or select one below"
        );

        subtitle.setTextSize(20);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                fullParams();

        subtitleParams.bottomMargin =
                dp(25);

        root.addView(
                subtitle,
                subtitleParams
        );

        // ---------------- URL INPUT ----------------

        urlInput = new EditText(this);

        urlInput.setHint(
                "Paste video URL here"
        );

        urlInput.setSingleLine(true);
        urlInput.setTextSize(22);

        urlInput.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        urlInput.setFocusable(true);
        urlInput.setFocusableInTouchMode(true);

        root.addView(
                urlInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        // ---------------- PLAY BUTTON ----------------

        Button playButton =
                makeButton(
                        "▶  OPEN IN JUST PLAYER"
                );

        LinearLayout.LayoutParams playParams =
                fullParams();

        playParams.height = dp(68);
        playParams.topMargin = dp(15);

        root.addView(
                playButton,
                playParams
        );

        playButton.setOnClickListener(
                v -> playTypedUrl()
        );

        // ---------------- FAVOURITE CURRENT URL ----------------

        Button favouriteCurrent =
                makeButton(
                        "☆  ADD CURRENT URL TO FAVOURITES"
                );

        LinearLayout.LayoutParams favCurrentParams =
                fullParams();

        favCurrentParams.height =
                dp(62);

        favCurrentParams.topMargin =
                dp(10);

        root.addView(
                favouriteCurrent,
                favCurrentParams
        );

        favouriteCurrent.setOnClickListener(
                v -> favouriteCurrentUrl()
        );

        // ---------------- FAVOURITES TITLE ----------------

        TextView favTitle =
                sectionTitle(
                        "⭐ FAVOURITES"
                );

        LinearLayout.LayoutParams favTitleParams =
                fullParams();

        favTitleParams.topMargin =
                dp(30);

        favTitleParams.bottomMargin =
                dp(10);

        root.addView(
                favTitle,
                favTitleParams
        );

        favouritesLayout =
                new LinearLayout(this);

        favouritesLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        root.addView(
                favouritesLayout,
                fullParams()
        );

        // ---------------- CLEAR FAVOURITES ----------------

        Button clearFavourites =
                makeButton(
                        "🧹 CLEAR ALL FAVOURITES"
                );

        LinearLayout.LayoutParams clearFavParams =
                fullParams();

        clearFavParams.height =
                dp(58);

        clearFavParams.topMargin =
                dp(10);

        root.addView(
                clearFavourites,
                clearFavParams
        );

        clearFavourites.setOnClickListener(
                v -> clearFavourites()
        );

        // ---------------- HISTORY TITLE ----------------

        TextView historyTitle =
                sectionTitle(
                        "🕘 LAST-USED URLs"
                );

        LinearLayout.LayoutParams historyTitleParams =
                fullParams();

        historyTitleParams.topMargin =
                dp(35);

        historyTitleParams.bottomMargin =
                dp(10);

        root.addView(
                historyTitle,
                historyTitleParams
        );

        historyLayout =
                new LinearLayout(this);

        historyLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        root.addView(
                historyLayout,
                fullParams()
        );

        // ---------------- CLEAR HISTORY ----------------

        Button clearHistory =
                makeButton(
                        "🧹 CLEAR ALL HISTORY"
                );

        LinearLayout.LayoutParams clearHistoryParams =
                fullParams();

        clearHistoryParams.height =
                dp(58);

        clearHistoryParams.topMargin =
                dp(12);

        root.addView(
                clearHistory,
                clearHistoryParams
        );

        clearHistory.setOnClickListener(
                v -> clearHistory()
        );

        setContentView(mainScroll);

        urlInput.requestFocus();
    }

    private LinearLayout.LayoutParams fullParams() {

        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private TextView sectionTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(text);
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);

        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        return title;
    }

    private Button makeButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(19);
        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setFocusable(true);
        button.setFocusableInTouchMode(true);

        button.setPadding(
                dp(15),
                dp(8),
                dp(15),
                dp(8)
        );

        return button;
    }

    // =====================================================
    // PLAY
    // =====================================================

    private void playTypedUrl() {

        String url =
                urlInput
                        .getText()
                        .toString()
                        .trim();

        if (url.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter a video URL",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        addToHistory(url);

        openJustPlayer(url);
    }

    private void openJustPlayer(
            String url
    ) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW
                    );

            intent.setDataAndType(
                    Uri.parse(url),
                    "video/*"
            );

            intent.setPackage(
                    "com.brouken.player"
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Just Player could not open this URL",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // HISTORY
    // =====================================================

    private void addToHistory(
            String url
    ) {

        List<String> list =
                getList(HISTORY);

        // Remove old copy
        list.remove(url);

        // Newest first
        list.add(0, url);

        saveList(
                HISTORY,
                list
        );

        refreshHistory();
    }

    private void refreshHistory() {

        if (historyLayout == null)
            return;

        historyLayout.removeAllViews();

        List<String> history =
                getList(HISTORY);

        if (history.isEmpty()) {

            addEmptyText(
                    historyLayout,
                    "No recently used URLs"
            );

            return;
        }

        for (String url : history) {

            addUrlRow(
                    historyLayout,
                    url,
                    false
            );
        }
    }

    // =====================================================
    // FAVOURITES
    // =====================================================

    private void favouriteCurrentUrl() {

        String url =
                urlInput
                        .getText()
                        .toString()
                        .trim();

        if (url.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter a URL first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        addFavourite(url);
    }

    private void addFavourite(
            String url
    ) {

        List<String> favourites =
                getList(FAVOURITES);

        if (!favourites.contains(url)) {

            favourites.add(0, url);

            saveList(
                    FAVOURITES,
                    favourites
            );

            Toast.makeText(
                    this,
                    "Added to favourites",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Already in favourites",
                    Toast.LENGTH_SHORT
            ).show();
        }

        refreshFavourites();
        refreshHistory();
    }

    private void removeFavourite(
            String url
    ) {

        List<String> favourites =
                getList(FAVOURITES);

        favourites.remove(url);

        saveList(
                FAVOURITES,
                favourites
        );

        refreshFavourites();
        refreshHistory();

        Toast.makeText(
                this,
                "Removed from favourites",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void refreshFavourites() {

        if (favouritesLayout == null)
            return;

        favouritesLayout.removeAllViews();

        List<String> favourites =
                getList(FAVOURITES);

        if (favourites.isEmpty()) {

            addEmptyText(
                    favouritesLayout,
                    "No favourite URLs yet"
            );

            return;
        }

        for (String url : favourites) {

            addUrlRow(
                    favouritesLayout,
                    url,
                    true
            );
        }
    }

    // =====================================================
    // URL ROW
    // =====================================================

    private void addUrlRow(
            LinearLayout parent,
            String url,
            boolean favouriteSection
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // PLAY

        Button play =
                makeButton(
                        "▶  " + shorten(url)
                );

        LinearLayout.LayoutParams playParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(65),
                        1
                );

        row.addView(
                play,
                playParams
        );

        play.setOnClickListener(
                v -> {

                    urlInput.setText(url);

                    addToHistory(url);

                    openJustPlayer(url);
                }
        );

        // FAVOURITE

        Button favourite =
                makeButton(
                        favouriteSection
                                ? "★"
                                : "☆"
                );

        LinearLayout.LayoutParams favouriteParams =
                new LinearLayout.LayoutParams(
                        dp(75),
                        dp(65)
                );

        row.addView(
                favourite,
                favouriteParams
        );

        favourite.setOnClickListener(
                v -> {

                    if (favouriteSection) {

                        removeFavourite(url);

                    } else {

                        addFavourite(url);
                    }
                }
        );

        // DELETE

        Button delete =
                makeButton("🗑");

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        dp(75),
                        dp(65)
                );

        row.addView(
                delete,
                deleteParams
        );

        delete.setOnClickListener(
                v -> {

                    if (favouriteSection) {

                        removeFavourite(url);

                    } else {

                        deleteHistory(url);
                    }
                }
        );

        // TV REMOTE:
        // URL -> STAR -> DELETE

        play.setOnKeyListener(
                (v, keyCode, event) -> {

                    if (event.getAction()
                            != KeyEvent.ACTION_DOWN)
                        return false;

                    if (keyCode ==
                            KeyEvent.KEYCODE_DPAD_RIGHT) {

                        favourite.requestFocus();

                        return true;
                    }

                    return false;
                }
        );

        favourite.setOnKeyListener(
                (v, keyCode, event) -> {

                    if (event.getAction()
                            != KeyEvent.ACTION_DOWN)
                        return false;

                    if (keyCode ==
                            KeyEvent.KEYCODE_DPAD_LEFT) {

                        play.requestFocus();

                        return true;
                    }

                    if (keyCode ==
                            KeyEvent.KEYCODE_DPAD_RIGHT) {

                        delete.requestFocus();

                        return true;
                    }

                    return false;
                }
        );

        delete.setOnKeyListener(
                (v, keyCode, event) -> {

                    if (event.getAction()
                            != KeyEvent.ACTION_DOWN)
                        return false;

                    if (keyCode ==
                            KeyEvent.KEYCODE_DPAD_LEFT) {

                        favourite.requestFocus();

                        return true;
                    }

                    return false;
                }
        );

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(67)
                );

        rowParams.bottomMargin =
                dp(8);

        parent.addView(
                row,
                rowParams
        );
    }

    // =====================================================
    // DELETE
    // =====================================================

    private void deleteHistory(
            String url
    ) {

        List<String> history =
                getList(HISTORY);

        history.remove(url);

        saveList(
                HISTORY,
                history
        );

        refreshHistory();

        Toast.makeText(
                this,
                "Removed from history",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void clearHistory() {

        prefs.edit()
                .remove(HISTORY)
                .apply();

        refreshHistory();

        Toast.makeText(
                this,
                "History cleared",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void clearFavourites() {

        prefs.edit()
                .remove(FAVOURITES)
                .apply();

        refreshFavourites();
        refreshHistory();

        Toast.makeText(
                this,
                "Favourites cleared",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =====================================================
    // STORAGE
    // =====================================================

    /*
     * URLs are Base64 encoded before storage.
     * This allows separators and special characters
     * inside URLs without breaking the list.
     */

    private void saveList(
            String key,
            List<String> list
    ) {

        StringBuilder result =
                new StringBuilder();

        for (String value : list) {

            if (result.length() > 0)
                result.append("\n");

            result.append(
                    Base64.encodeToString(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            Base64.NO_WRAP
                    )
            );
        }

        prefs.edit()
                .putString(
                        key,
                        result.toString()
                )
                .apply();
    }

    private List<String> getList(
            String key
    ) {

        String data =
                prefs.getString(
                        key,
                        ""
                );

        List<String> result =
                new ArrayList<>();

        if (data.isEmpty())
            return result;

        String[] lines =
                data.split("\n");

        for (String line : lines) {

            try {

                byte[] decoded =
                        Base64.decode(
                                line,
                                Base64.NO_WRAP
                        );

                String url =
                        new String(
                                decoded,
                                StandardCharsets.UTF_8
                        );

                if (!url.isEmpty()
                        && !result.contains(url)) {

                    result.add(url);
                }

            } catch (Exception ignored) {
            }
        }

        return result;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void addEmptyText(
            LinearLayout parent,
            String text
    ) {

        TextView empty =
                new TextView(this);

        empty.setText(text);
        empty.setTextSize(19);
        empty.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams params =
                fullParams();

        params.height =
                dp(55);

        parent.addView(
                empty,
                params
        );
    }

    private String shorten(
            String url
    ) {

        if (url.length() <= 65)
            return url;

        return url.substring(0, 62)
                + "...";
    }
}
