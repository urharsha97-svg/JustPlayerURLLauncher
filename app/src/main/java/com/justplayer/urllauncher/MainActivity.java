package com.justplayer.urllauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private EditText urlInput;
    private LinearLayout historyLayout;
    private LinearLayout favouritesLayout;

    private SharedPreferences preferences;

    private static final String PREFS = "url_data";
    private static final String HISTORY = "history";
    private static final String FAVOURITES = "favourites";

    private static final int MAX_HISTORY = 10;

    private int dp(float value) {
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

        preferences = getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );

        buildTVUI();
        loadHistory();
        loadFavourites();
    }

    private void buildTVUI() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        root.setPadding(
                dp(70),
                dp(30),
                dp(70),
                dp(30)
        );

        TextView title = new TextView(this);

        title.setText("JUST PLAYER");
        title.setTextSize(38);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        TextView subtitle = new TextView(this);

        subtitle.setText(
                "Paste a video URL or select one below"
        );

        subtitle.setTextSize(20);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.bottomMargin = dp(25);

        root.addView(
                subtitle,
                subtitleParams
        );

        urlInput = new EditText(this);

        urlInput.setHint("Paste video URL here");
        urlInput.setSingleLine(true);
        urlInput.setTextSize(22);

        urlInput.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(70)
                );

        root.addView(
                urlInput,
                inputParams
        );

        Button playButton =
                createButton("▶  OPEN IN JUST PLAYER");

        LinearLayout.LayoutParams playParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(70)
                );

        playParams.topMargin = dp(15);

        root.addView(
                playButton,
                playParams
        );

        playButton.setOnClickListener(
                v -> openCurrentUrl()
        );

        /*
         * FAVOURITES
         */

        TextView favouriteTitle =
                createSectionTitle("⭐ FAVOURITES");

        root.addView(
                favouriteTitle,
                sectionParams()
        );

        ScrollView favouriteScroll =
                new ScrollView(this);

        favouritesLayout =
                new LinearLayout(this);

        favouritesLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        favouriteScroll.addView(
                favouritesLayout
        );

        LinearLayout.LayoutParams favouriteScrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(170)
                );

        root.addView(
                favouriteScroll,
                favouriteScrollParams
        );

        /*
         * LAST USED
         */

        TextView historyTitle =
                createSectionTitle("🕘 LAST-USED URLs");

        root.addView(
                historyTitle,
                sectionParams()
        );

        ScrollView historyScroll =
                new ScrollView(this);

        historyLayout =
                new LinearLayout(this);

        historyLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        historyScroll.addView(
                historyLayout
        );

        LinearLayout.LayoutParams historyScrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        root.addView(
                historyScroll,
                historyScrollParams
        );

        Button clearButton =
                createButton("CLEAR HISTORY");

        LinearLayout.LayoutParams clearParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(60)
                );

        clearParams.topMargin = dp(12);

        root.addView(
                clearButton,
                clearParams
        );

        clearButton.setOnClickListener(
                v -> clearHistory()
        );

        setContentView(root);

        /*
         * Remote starts here
         */
        urlInput.requestFocus();
    }

    private LinearLayout.LayoutParams sectionParams() {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(18);
        params.bottomMargin = dp(8);

        return params;
    }

    private TextView createSectionTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(text);
        title.setTextSize(24);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(Gravity.CENTER);

        return title;
    }

    private Button createButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(20);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setGravity(Gravity.CENTER);

        /*
         * Android TV remote focus
         */
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);

        button.setPadding(
                dp(20),
                dp(8),
                dp(20),
                dp(8)
        );

        return button;
    }

    /*
     * OPEN CURRENT URL
     */

    private void openCurrentUrl() {

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

        saveHistory(url);

        openInJustPlayer(url);
    }

    /*
     * OPEN IN JUST PLAYER
     */

    private void openInJustPlayer(
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

    /*
     * HISTORY
     */

    private void saveHistory(
            String url
    ) {

        Set<String> saved =
                preferences.getStringSet(
                        HISTORY,
                        new HashSet<>()
                );

        List<String> urls =
                new ArrayList<>(saved);

        urls.remove(url);

        urls.add(0, url);

        while (urls.size() > MAX_HISTORY) {

            urls.remove(
                    urls.size() - 1
            );
        }

        preferences.edit()
                .putStringSet(
                        HISTORY,
                        new HashSet<>(urls)
                )
                .apply();

        loadHistory();
    }

    private void loadHistory() {

        if (historyLayout == null)
            return;

        historyLayout.removeAllViews();

        Set<String> saved =
                preferences.getStringSet(
                        HISTORY,
                        new HashSet<>()
                );

        List<String> urls =
                new ArrayList<>(saved);

        if (urls.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "No recently used URLs"
            );

            empty.setTextSize(19);
            empty.setGravity(
                    Gravity.CENTER
            );

            historyLayout.addView(
                    empty
            );

            return;
        }

        for (String url : urls) {

            addURLRow(
                    historyLayout,
                    url,
                    false
            );
        }
    }

    /*
     * FAVOURITES
     */

    private boolean isFavourite(
            String url
    ) {

        Set<String> favourites =
                preferences.getStringSet(
                        FAVOURITES,
                        new HashSet<>()
                );

        return favourites.contains(url);
    }

    private void toggleFavourite(
            String url
    ) {

        Set<String> saved =
                preferences.getStringSet(
                        FAVOURITES,
                        new HashSet<>()
                );

        Set<String> favourites =
                new HashSet<>(saved);

        if (favourites.contains(url)) {

            favourites.remove(url);

            Toast.makeText(
                    this,
                    "Removed from favourites",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            favourites.add(url);

            Toast.makeText(
                    this,
                    "Added to favourites",
                    Toast.LENGTH_SHORT
            ).show();
        }

        preferences.edit()
                .putStringSet(
                        FAVOURITES,
                        favourites
                )
                .apply();

        loadFavourites();
        loadHistory();
    }

    private void loadFavourites() {

        if (favouritesLayout == null)
            return;

        favouritesLayout.removeAllViews();

        Set<String> saved =
                preferences.getStringSet(
                        FAVOURITES,
                        new HashSet<>()
                );

        List<String> favourites =
                new ArrayList<>(saved);

        if (favourites.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "No favourite URLs yet"
            );

            empty.setTextSize(19);
            empty.setGravity(
                    Gravity.CENTER
            );

            favouritesLayout.addView(
                    empty
            );

            return;
        }

        for (String url : favourites) {

            addURLRow(
                    favouritesLayout,
                    url,
                    true
            );
        }
    }

    /*
     * URL ROW
     *
     * OK = play
     * RIGHT = favourite
     */

    private void addURLRow(
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

        /*
         * URL PLAY BUTTON
         */

        Button play =
                createButton(
                        "▶  " + url
                );

        LinearLayout.LayoutParams playParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(62),
                        1
                );

        row.addView(
                play,
                playParams
        );

        play.setOnClickListener(v -> {

            urlInput.setText(url);

            saveHistory(url);

            openInJustPlayer(url);
        });

        /*
         * STAR BUTTON
         */

        Button star =
                createButton(
                        isFavourite(url)
                                ? "★"
                                : "☆"
                );

        LinearLayout.LayoutParams starParams =
                new LinearLayout.LayoutParams(
                        dp(75),
                        dp(62)
                );

        row.addView(
                star,
                starParams
        );

        star.setOnClickListener(
                v -> toggleFavourite(url)
        );

        /*
         * TV REMOTE:
         *
         * LEFT/RIGHT moves between
         * URL and star.
         */

        play.setOnKeyListener(
                (v, keyCode, event) -> {

                    if (event.getAction()
                            != KeyEvent.ACTION_DOWN)
                        return false;

                    if (keyCode
                            == KeyEvent.KEYCODE_DPAD_RIGHT) {

                        star.requestFocus();

                        return true;
                    }

                    return false;
                }
        );

        star.setOnKeyListener(
                (v, keyCode, event) -> {

                    if (event.getAction()
                            != KeyEvent.ACTION_DOWN)
                        return false;

                    if (keyCode
                            == KeyEvent.KEYCODE_DPAD_LEFT) {

                        play.requestFocus();

                        return true;
                    }

                    return false;
                }
        );

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(65)
                );

        rowParams.bottomMargin = dp(8);

        parent.addView(
                row,
                rowParams
        );
    }

    /*
     * CLEAR HISTORY
     */

    private void clearHistory() {

        preferences.edit()
                .remove(HISTORY)
                .apply();

        loadHistory();

        Toast.makeText(
                this,
                "History cleared",
                Toast.LENGTH_SHORT
        ).show();
    }
}
