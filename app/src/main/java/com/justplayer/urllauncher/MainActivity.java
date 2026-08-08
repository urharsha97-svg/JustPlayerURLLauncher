package com.justplayer.urllauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private EditText urlInput;
    private EditText historySearch;
    private EditText favouritesSearch;

    private LinearLayout historyLayout;
    private LinearLayout favouritesLayout;

    private SharedPreferences prefs;

    private static final String PREFS = "player_data_v6";
    private static final String HISTORY = "history";
    private static final String FAVOURITES = "favourites";

    private static final String JUST_PLAYER_PACKAGE =
            "com.brouken.player";

    // =====================================================
    // PREMIUM BLACK THEME
    // =====================================================

    private static final int BLACK =
            Color.rgb(0, 0, 0);

    private static final int CARD =
            Color.rgb(17, 17, 17);

    private static final int CARD_2 =
            Color.rgb(25, 25, 25);

    private static final int FOCUS =
            Color.rgb(35, 75, 125);

    private static final int FOCUS_BORDER =
            Color.rgb(80, 160, 255);

    private static final int WHITE =
            Color.rgb(245, 245, 245);

    private static final int GREY =
            Color.rgb(165, 165, 165);

    private static final int BLUE =
            Color.rgb(80, 160, 255);

    private static final int GOLD =
            Color.rgb(255, 193, 7);

    // =====================================================
    // SORT MODES
    // =====================================================

    private static final int SORT_NEWEST = 0;
    private static final int SORT_OLDEST = 1;
    private static final int SORT_AZ = 2;
    private static final int SORT_ZA = 3;

    private int historySort =
            SORT_NEWEST;

    private int favouritesSort =
            SORT_NEWEST;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        getWindow().setNavigationBarColor(
                BLACK
        );

        getWindow().setStatusBarColor(
                BLACK
        );

        prefs = getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );

        createUI();

        refreshHistory();
        refreshFavourites();
    }

    // =====================================================
    // BASIC HELPERS
    // =====================================================

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    private LinearLayout.LayoutParams fullParams() {

        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    // =====================================================
    // PREMIUM BUTTON BACKGROUND
    // =====================================================

    private StateListDrawable createButtonBackground() {

        GradientDrawable normal =
                new GradientDrawable();

        normal.setColor(CARD_2);
        normal.setCornerRadius(dp(10));
        normal.setStroke(
                dp(1),
                Color.rgb(45, 45, 45)
        );

        GradientDrawable focused =
                new GradientDrawable();

        focused.setColor(FOCUS);
        focused.setCornerRadius(dp(10));
        focused.setStroke(
                dp(3),
                FOCUS_BORDER
        );

        GradientDrawable pressed =
                new GradientDrawable();

        pressed.setColor(
                Color.rgb(50, 100, 160)
        );

        pressed.setCornerRadius(dp(10));
        pressed.setStroke(
                dp(3),
                Color.WHITE
        );

        StateListDrawable states =
                new StateListDrawable();

        states.addState(
                new int[]{
                        android.R.attr.state_pressed
                },
                pressed
        );

        states.addState(
                new int[]{
                        android.R.attr.state_focused
                },
                focused
        );

        states.addState(
                new int[]{},
                normal
        );

        return states;
    }

    private StateListDrawable createInputBackground() {

        GradientDrawable normal =
                new GradientDrawable();

        normal.setColor(CARD);
        normal.setCornerRadius(dp(10));
        normal.setStroke(
                dp(1),
                Color.rgb(50, 50, 50)
        );

        GradientDrawable focused =
                new GradientDrawable();

        focused.setColor(
                Color.rgb(20, 25, 32)
        );

        focused.setCornerRadius(dp(10));

        focused.setStroke(
                dp(3),
                FOCUS_BORDER
        );

        StateListDrawable states =
                new StateListDrawable();

        states.addState(
                new int[]{
                        android.R.attr.state_focused
                },
                focused
        );

        states.addState(
                new int[]{},
                normal
        );

        return states;
    }

    private Button makeButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(17);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);

        button.setTextColor(WHITE);

        button.setFocusable(true);
        button.setFocusableInTouchMode(true);

        button.setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
        );

        button.setBackground(
                createButtonBackground()
        );

        return button;
    }

    private LinearLayout.LayoutParams buttonWeightParams() {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(60),
                        1f
                );

        params.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        return params;
    }

    private TextView makeText(
            String text,
            float size
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(WHITE);

        return view;
    }

    private TextView sectionTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(text);
        title.setTextSize(25);
        title.setTextColor(WHITE);

        title.setGravity(
                Gravity.CENTER
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        return title;
    }

    // =====================================================
    // MAIN UI
    // =====================================================

    private void createUI() {

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BLACK);

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        root.setPadding(
                dp(55),
                dp(30),
                dp(55),
                dp(50)
        );

        root.setBackgroundColor(BLACK);

        scrollView.addView(root);

        // =================================================
        // TITLE
        // =================================================

        TextView title =
                new TextView(this);

        title.setText(
                "VIDEO URL LAUNCHER"
        );

        title.setTextSize(36);
        title.setTextColor(WHITE);

        title.setGravity(
                Gravity.CENTER
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        root.addView(
                title,
                fullParams()
        );

        TextView subtitle =
                makeText(
                        "Paste a video URL • Play instantly on your TV",
                        18
                );

        subtitle.setTextColor(GREY);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                fullParams();

        subtitleParams.topMargin =
                dp(5);

        subtitleParams.bottomMargin =
                dp(25);

        root.addView(
                subtitle,
                subtitleParams
        );

        // =================================================
        // URL INPUT
        // =================================================

        urlInput =
                new EditText(this);

        urlInput.setHint(
                "Paste video URL here"
        );

        urlInput.setHintTextColor(
                Color.rgb(115, 115, 115)
        );

        urlInput.setTextColor(WHITE);
        urlInput.setSingleLine(true);
        urlInput.setTextSize(21);

        urlInput.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        urlInput.setBackground(
                createInputBackground()
        );

        root.addView(
                urlInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        // =================================================
        // JUST PLAYER
        // =================================================

        Button justPlayer =
                makeButton(
                        "▶   PLAY IN JUST PLAYER"
                );

        LinearLayout.LayoutParams justParams =
                fullParams();

        justParams.height =
                dp(68);

        justParams.topMargin =
                dp(15);

        root.addView(
                justPlayer,
                justParams
        );

        justPlayer.setOnClickListener(
                v -> playCurrentInJustPlayer()
        );

        // =================================================
        // CHOOSE PLAYER
        // =================================================

        Button choosePlayer =
                makeButton(
                        "🎬   CHOOSE VIDEO PLAYER"
                );

        LinearLayout.LayoutParams chooseParams =
                fullParams();

        chooseParams.height =
                dp(68);

        chooseParams.topMargin =
                dp(10);

        root.addView(
                choosePlayer,
                chooseParams
        );

        choosePlayer.setOnClickListener(
                v -> choosePlayerForCurrentUrl()
        );

        // =================================================
        // ADD FAVOURITE
        // =================================================

        Button addFavourite =
                makeButton(
                        "☆   ADD CURRENT URL TO FAVOURITES"
                );

        LinearLayout.LayoutParams favouriteParams =
                fullParams();

        favouriteParams.height =
                dp(62);

        favouriteParams.topMargin =
                dp(10);

        root.addView(
                addFavourite,
                favouriteParams
        );

        addFavourite.setOnClickListener(
                v -> addCurrentToFavourites()
        );

        // =================================================
        // FAVOURITES
        // =================================================

        TextView favTitle =
                sectionTitle(
                        "⭐  FAVOURITES"
                );

        LinearLayout.LayoutParams favTitleParams =
                fullParams();

        favTitleParams.topMargin =
                dp(38);

        favTitleParams.bottomMargin =
                dp(12);

        root.addView(
                favTitle,
                favTitleParams
        );

        // SEARCH FAVOURITES

        favouritesSearch =
                createSearchBox(
                        "🔍  Search favourites by name or URL..."
                );

        root.addView(
                favouritesSearch,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(62)
                )
        );

        favouritesSearch.setOnFocusChangeListener(
                (v, hasFocus) -> {
                    if (!hasFocus) {
                        refreshFavourites();
                    }
                }
        );

        favouritesSearch.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        refreshFavourites();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                    }
                }
        );

        // FAVOURITE SORT

        LinearLayout favouriteSort =
                createSortBar(true);

        root.addView(
                favouriteSort,
                fullParams()
        );

        favouritesLayout =
                new LinearLayout(this);

        favouritesLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        favouritesLayout.setBackgroundColor(
                BLACK
        );

        LinearLayout.LayoutParams favListParams =
                fullParams();

        favListParams.topMargin =
                dp(10);

        root.addView(
                favouritesLayout,
                favListParams
        );

        Button clearFav =
                makeButton(
                        "🧹   CLEAR ALL FAVOURITES"
                );

        LinearLayout.LayoutParams clearFavParams =
                fullParams();

        clearFavParams.height =
                dp(58);

        clearFavParams.topMargin =
                dp(12);

        root.addView(
                clearFav,
                clearFavParams
        );

        clearFav.setOnClickListener(
                v -> confirmClearFavourites()
        );

        // =================================================
        // HISTORY
        // =================================================

        TextView historyTitle =
                sectionTitle(
                        "🕘  LAST-USED HISTORY"
                );

        LinearLayout.LayoutParams historyTitleParams =
                fullParams();

        historyTitleParams.topMargin =
                dp(42);

        historyTitleParams.bottomMargin =
                dp(12);

        root.addView(
                historyTitle,
                historyTitleParams
        );

        // SEARCH HISTORY

        historySearch =
                createSearchBox(
                        "🔍  Search history by name or URL..."
                );

        root.addView(
                historySearch,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(62)
                )
        );

        historySearch.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        refreshHistory();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                    }
                }
        );

        // HISTORY SORT

        LinearLayout historySortBar =
                createSortBar(false);

        root.addView(
                historySortBar,
                fullParams()
        );

        historyLayout =
                new LinearLayout(this);

        historyLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        historyLayout.setBackgroundColor(
                BLACK
        );

        LinearLayout.LayoutParams historyListParams =
                fullParams();

        historyListParams.topMargin =
                dp(10);

        root.addView(
                historyLayout,
                historyListParams
        );

        Button clearHistory =
                makeButton(
                        "🧹   CLEAR ALL HISTORY"
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
                v -> confirmClearHistory()
        );

        setContentView(scrollView);

        urlInput.requestFocus();
    }

    // =====================================================
    // SEARCH BOX
    // =====================================================

    private EditText createSearchBox(
            String hint
    ) {

        EditText search =
                new EditText(this);

        search.setHint(hint);

        search.setHintTextColor(
                Color.rgb(115, 115, 115)
        );

        search.setTextColor(WHITE);

        search.setSingleLine(true);

        search.setTextSize(18);

        search.setPadding(
                dp(18),
                dp(8),
                dp(18),
                dp(8)
        );

        search.setBackground(
                createInputBackground()
        );

        return search;
    }

    // =====================================================
    // SORT BAR
    // =====================================================

    private LinearLayout createSortBar(
            boolean favourites
    ) {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        Button newest =
                makeButton(
                        "NEWEST"
                );

        Button oldest =
                makeButton(
                        "OLDEST"
                );

        Button az =
                makeButton(
                        "A-Z"
                );

        Button za =
                makeButton(
                        "Z-A"
                );

        bar.addView(
                newest,
                buttonWeightParams()
        );

        bar.addView(
                oldest,
                buttonWeightParams()
        );

        bar.addView(
                az,
                buttonWeightParams()
        );

        bar.addView(
                za,
                buttonWeightParams()
        );

        newest.setOnClickListener(
                v -> {

                    if (favourites) {

                        favouritesSort =
                                SORT_NEWEST;

                        refreshFavourites();

                    } else {

                        historySort =
                                SORT_NEWEST;

                        refreshHistory();
                    }
                }
        );

        oldest.setOnClickListener(
                v -> {

                    if (favourites) {

                        favouritesSort =
                                SORT_OLDEST;

                        refreshFavourites();

                    } else {

                        historySort =
                                SORT_OLDEST;

                        refreshHistory();
                    }
                }
        );

        az.setOnClickListener(
                v -> {

                    if (favourites) {

                        favouritesSort =
                                SORT_AZ;

                        refreshFavourites();

                    } else {

                        historySort =
                                SORT_AZ;

                        refreshHistory();
                    }
                }
        );

        za.setOnClickListener(
                v -> {

                    if (favourites) {

                        favouritesSort =
                                SORT_ZA;

                        refreshFavourites();

                    } else {

                        historySort =
                                SORT_ZA;

                        refreshHistory();
                    }
                }
        );

        // Remote navigation

        newest.setNextFocusRightId(
                oldest.getId()
        );

        oldest.setNextFocusLeftId(
                newest.getId()
        );

        oldest.setNextFocusRightId(
                az.getId()
        );

        az.setNextFocusLeftId(
                oldest.getId()
        );

        az.setNextFocusRightId(
                za.getId()
        );

        za.setNextFocusLeftId(
                az.getId()
        );

        return bar;
    }

    // =====================================================
    // CURRENT URL
    // =====================================================

    private String getCurrentUrl() {

        return urlInput
                .getText()
                .toString()
                .trim();
    }

    private void playCurrentInJustPlayer() {

        String url =
                getCurrentUrl();

        if (url.isEmpty()) {

            showMessage(
                    "Please enter a video URL"
            );

            return;
        }

        addHistory(url);

        openInJustPlayerFresh(url);
    }

    private void choosePlayerForCurrentUrl() {

        String url =
                getCurrentUrl();

        if (url.isEmpty()) {

            showMessage(
                    "Please enter a video URL"
            );

            return;
        }

        addHistory(url);

        openPlayerChooserFresh(url);
    }

    private void addCurrentToFavourites() {

        String url =
                getCurrentUrl();

        if (url.isEmpty()) {

            showMessage(
                    "Enter a URL first"
            );

            return;
        }

        addFavourite(url);
    }

    // =====================================================
    // PLAY FRESH
    // =====================================================

    private void openInJustPlayerFresh(
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
                    JUST_PLAYER_PACKAGE
            );

            /*
             * Fresh launch:
             * Don't reuse the currently opened
             * player activity when Android allows it.
             */

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

        } catch (Exception e) {

            // Fallback if the player doesn't support
            // the aggressive fresh-task flags.

            try {

                Intent fallback =
                        new Intent(
                                Intent.ACTION_VIEW
                        );

                fallback.setDataAndType(
                        Uri.parse(url),
                        "video/*"
                );

                fallback.setPackage(
                        JUST_PLAYER_PACKAGE
                );

                fallback.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(fallback);

            } catch (Exception ignored) {

                showMessage(
                        "Just Player is not installed"
                );
            }
        }
    }

    // =====================================================
    // CHOOSE ANY PLAYER
    // =====================================================

    private void openPlayerChooserFresh(
            String url
    ) {

        try {

            Intent videoIntent =
                    new Intent(
                            Intent.ACTION_VIEW
                    );

            videoIntent.setDataAndType(
                    Uri.parse(url),
                    "video/*"
            );

            videoIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            videoIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            Intent chooser =
                    Intent.createChooser(
                            videoIntent,
                            "Choose Video Player"
                    );

            startActivity(chooser);

        } catch (Exception e) {

            showMessage(
                    "No compatible video player found"
            );
        }
    }

    // =====================================================
    // COPY URL
    // =====================================================

    private void copyUrl(
            String url
    ) {

        ClipboardManager clipboard =
                (ClipboardManager)
                        getSystemService(
                                CLIPBOARD_SERVICE
                        );

        ClipData clip =
                ClipData.newPlainText(
                        "Video URL",
                        url
                );

        clipboard.setPrimaryClip(clip);

        showMessage(
                "📋 URL copied"
        );
    }

    // =====================================================
    // HISTORY
    // =====================================================

    private void addHistory(
            String url
    ) {

        List<VideoItem> history =
                getItems(HISTORY);

        removeUrl(
                history,
                url
        );

        VideoItem item =
                createItem(url);

        /*
         * History timestamp means
         * LAST PLAYED time.
         */

        item.timestamp =
                System.currentTimeMillis();

        history.add(
                0,
                item
        );

        saveItems(
                HISTORY,
                history
        );

        refreshHistory();
    }

    private void refreshHistory() {

        if (historyLayout == null)
            return;

        historyLayout.removeAllViews();

        List<VideoItem> history =
                getItems(HISTORY);

        String query = "";

        if (historySearch != null) {

            query =
                    historySearch
                            .getText()
                            .toString()
                            .trim()
                            .toLowerCase(
                                    Locale.getDefault()
                            );
        }

        List<VideoItem> filtered =
                filterItems(
                        history,
                        query
                );

        sortItems(
                filtered,
                historySort
        );

        if (filtered.isEmpty()) {

            addEmptyText(
                    historyLayout,
                    query.isEmpty()
                            ? "No recently used videos"
                            : "No matching history"
            );

            return;
        }

        for (VideoItem item :
                filtered) {

            addVideoRow(
                    historyLayout,
                    item,
                    false
            );
        }
    }

    // =====================================================
    // FAVOURITES
    // =====================================================

    private void addFavourite(
            String url
    ) {

        List<VideoItem> favourites =
                getItems(FAVOURITES);

        if (containsUrl(
                favourites,
                url
        )) {

            showMessage(
                    "Already in favourites"
            );

            return;
        }

        /*
         * Favourite timestamp is
         * FAVOURITE ADDED time.
         */

        VideoItem item =
                createItem(url);

        item.timestamp =
                System.currentTimeMillis();

        favourites.add(
                0,
                item
        );

        saveItems(
                FAVOURITES,
                favourites
        );

        refreshFavourites();

        showMessage(
                "⭐ Added to favourites"
        );
    }

    private void removeFavourite(
            String url
    ) {

        List<VideoItem> favourites =
                getItems(FAVOURITES);

        removeUrl(
                favourites,
                url
        );

        saveItems(
                FAVOURITES,
                favourites
        );

        refreshFavourites();

        showMessage(
                "Removed from favourites"
        );
    }

    private void refreshFavourites() {

        if (favouritesLayout == null)
            return;

        favouritesLayout.removeAllViews();

        List<VideoItem> favourites =
                getItems(FAVOURITES);

        String query = "";

        if (favouritesSearch != null) {

            query =
                    favouritesSearch
                            .getText()
                            .toString()
                            .trim()
                            .toLowerCase(
                                    Locale.getDefault()
                            );
        }

        List<VideoItem> filtered =
                filterItems(
                        favourites,
                        query
                );

        sortItems(
                filtered,
                favouritesSort
        );

        if (filtered.isEmpty()) {

            addEmptyText(
                    favouritesLayout,
                    query.isEmpty()
                            ? "No favourite videos yet"
                            : "No matching favourites"
            );

            return;
        }

        for (VideoItem item :
                filtered) {

            addVideoRow(
                    favouritesLayout,
                    item,
                    true
            );
        }
    }

    // =====================================================
    // FILTER
    // =====================================================

    private List<VideoItem> filterItems(
            List<VideoItem> source,
            String query
    ) {

        List<VideoItem> result =
                new ArrayList<>();

        if (query.isEmpty()) {

            result.addAll(source);

            return result;
        }

        for (VideoItem item :
                source) {

            String name =
                    item.fileName
                            .toLowerCase(
                                    Locale.getDefault()
                            );

            String url =
                    item.url
                            .toLowerCase(
                                    Locale.getDefault()
                            );

            if (name.contains(query)
                    || url.contains(query)) {

                result.add(item);
            }
        }

        return result;
    }

    // =====================================================
    // SORT
    // =====================================================

    private void sortItems(
            List<VideoItem> items,
            int sortMode
    ) {

        if (sortMode == SORT_NEWEST) {

            Collections.sort(
                    items,
                    (a, b) ->
                            Long.compare(
                                    b.timestamp,
                                    a.timestamp
                            )
            );

        } else if (
                sortMode ==
                        SORT_OLDEST
        ) {

            Collections.sort(
                    items,
                    (a, b) ->
                            Long.compare(
                                    a.timestamp,
                                    b.timestamp
                            )
            );

        } else if (
                sortMode ==
                        SORT_AZ
        ) {

            Collections.sort(
                    items,
                    new Comparator<VideoItem>() {

                        @Override
                        public int compare(
                                VideoItem a,
                                VideoItem b
                        ) {

                            return a.fileName
                                    .toLowerCase(
                                            Locale.getDefault()
                                    )
                                    .compareTo(
                                            b.fileName
                                                    .toLowerCase(
                                                            Locale.getDefault()
                                                    )
                                    );
                        }
                    }
            );

        } else {

            Collections.sort(
                    items,
                    new Comparator<VideoItem>() {

                        @Override
                        public int compare(
                                VideoItem a,
                                VideoItem b
                        ) {

                            return b.fileName
                                    .toLowerCase(
                                            Locale.getDefault()
                                    )
                                    .compareTo(
                                            a.fileName
                                                    .toLowerCase(
                                                            Locale.getDefault()
                                                    )
                                    );
                        }
                    }
            );
        }
    }

    // =====================================================
    // VIDEO ROW
    // =====================================================

    private void addVideoRow(
            LinearLayout parent,
            VideoItem item,
            boolean favouriteSection
    ) {

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        GradientDrawable card =
                new GradientDrawable();

        card.setColor(CARD);
        card.setCornerRadius(dp(12));

        card.setStroke(
                dp(1),
                Color.rgb(40, 40, 40)
        );

        container.setBackground(card);

        // =================================================
        // FILE NAME
        // =================================================

        TextView filename =
                makeText(
                        "🎬  " +
                                item.fileName,
                        20
                );

        filename.setTypeface(
                null,
                Typeface.BOLD
        );

        filename.setMaxLines(2);

        container.addView(
                filename,
                fullParams()
        );

        // =================================================
        // DATE / TIME
        // =================================================

        TextView date =
                makeText(
                        "🕘  " +
                                formatDate(
                                        item.timestamp
                                ),
                        15
                );

        date.setTextColor(GREY);

        LinearLayout.LayoutParams dateParams =
                fullParams();

        dateParams.topMargin =
                dp(5);

        container.addView(
                date,
                dateParams
        );

        // =================================================
        // URL
        // =================================================

        TextView urlText =
                makeText(
                        item.url,
                        13
                );

        urlText.setTextColor(
                Color.rgb(
                        125,
                        125,
                        125
                )
        );

        urlText.setMaxLines(1);

        LinearLayout.LayoutParams urlParams =
                fullParams();

        urlParams.topMargin =
                dp(5);

        urlParams.bottomMargin =
                dp(8);

        container.addView(
                urlText,
                urlParams
        );

        // =================================================
        // BUTTON ROW
        // =================================================

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttons.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // =================================================
        // PLAY FRESH
        // =================================================

        Button play =
                makeButton(
                        "▶ PLAY FRESH"
                );

        buttons.addView(
                play,
                buttonWeightParams()
        );

        play.setOnClickListener(
                v -> {

                    urlInput.setText(
                            item.url
                    );

                    addHistory(
                            item.url
                    );

                    openInJustPlayerFresh(
                            item.url
                    );
                }
        );

        // =================================================
        // CHOOSE PLAYER
        // =================================================

        Button choose =
                makeButton(
                        "🎬 PLAYER"
                );

        buttons.addView(
                choose,
                buttonWeightParams()
        );

        choose.setOnClickListener(
                v -> {

                    urlInput.setText(
                            item.url
                    );

                    addHistory(
                            item.url
                    );

                    openPlayerChooserFresh(
                            item.url
                    );
                }
        );

        // =================================================
        // COPY
        // =================================================

        Button copy =
                makeButton(
                        "📋 COPY"
                );

        buttons.addView(
                copy,
                buttonWeightParams()
        );

        copy.setOnClickListener(
                v -> copyUrl(item.url)
        );

        // =================================================
        // FAVOURITE
        // =================================================

        Button favourite =
                makeButton(
                        favouriteSection
                                ? "★"
                                : "☆"
                );

        LinearLayout.LayoutParams smallParams =
                new LinearLayout.LayoutParams(
                        dp(65),
                        dp(60)
                );

        smallParams.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        buttons.addView(
                favourite,
                smallParams
        );

        favourite.setOnClickListener(
                v -> {

                    if (favouriteSection) {

                        removeFavourite(
                                item.url
                        );

                    } else {

                        addFavourite(
                                item.url
                        );
                    }
                }
        );

        // =================================================
        // DELETE
        // =================================================

        Button delete =
                makeButton(
                        "🗑"
                );

        buttons.addView(
                delete,
                smallParams
        );

        delete.setOnClickListener(
                v -> {

                    if (favouriteSection) {

                        confirmDeleteFavourite(
                                item.url
                        );

                    } else {

                        confirmDeleteHistory(
                                item.url
                        );
                    }
                }
        );

        container.addView(
                buttons,
                fullParams()
        );

        // =================================================
        // TV REMOTE NAVIGATION
        // =================================================

        play.setNextFocusRightId(
                choose.getId()
        );

        choose.setNextFocusLeftId(
                play.getId()
        );

        choose.setNextFocusRightId(
                copy.getId()
        );

        copy.setNextFocusLeftId(
                choose.getId()
        );

        copy.setNextFocusRightId(
                favourite.getId()
        );

        favourite.setNextFocusLeftId(
                copy.getId()
        );

        favourite.setNextFocusRightId(
                delete.getId()
        );

        delete.setNextFocusLeftId(
                favourite.getId()
        );

        LinearLayout.LayoutParams containerParams =
                fullParams();

        containerParams.bottomMargin =
                dp(14);

        parent.addView(
                container,
                containerParams
        );
    }

    // =====================================================
    // DELETE CONFIRMATIONS
    // =====================================================

    private void confirmDeleteHistory(
            String url
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Remove from History?"
                )
                .setMessage(
                        "This video will be removed from your history."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "REMOVE",
                        (dialog, which) ->
                                deleteHistory(url)
                )
                .show();
    }

    private void confirmDeleteFavourite(
            String url
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Remove Favourite?"
                )
                .setMessage(
                        "This video will be removed from favourites."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "REMOVE",
                        (dialog, which) ->
                                removeFavourite(url)
                )
                .show();
    }

    private void confirmClearHistory() {

        List<VideoItem> items =
                getItems(HISTORY);

        if (items.isEmpty()) {

            showMessage(
                    "History is already empty"
            );

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Clear All History?"
                )
                .setMessage(
                        "All saved history entries will be permanently removed."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "CLEAR ALL",
                        (dialog, which) ->
                                clearHistory()
                )
                .show();
    }

    private void confirmClearFavourites() {

        List<VideoItem> items =
                getItems(FAVOURITES);

        if (items.isEmpty()) {

            showMessage(
                    "Favourites are already empty"
            );

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Clear All Favourites?"
                )
                .setMessage(
                        "All favourite videos will be permanently removed."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "CLEAR ALL",
                        (dialog, which) ->
                                clearFavourites()
                )
                .show();
    }

    // =====================================================
    // DELETE
    // =====================================================

    private void deleteHistory(
            String url
    ) {

        List<VideoItem> history =
                getItems(HISTORY);

        removeUrl(
                history,
                url
        );

        saveItems(
                HISTORY,
                history
        );

        refreshHistory();

        showMessage(
                "Removed from history"
        );
    }

    private void clearHistory() {

        prefs.edit()
                .remove(HISTORY)
                .apply();

        refreshHistory();

        showMessage(
                "History cleared"
        );
    }

    private void clearFavourites() {

        prefs.edit()
                .remove(FAVOURITES)
                .apply();

        refreshFavourites();

        showMessage(
                "Favourites cleared"
        );
    }

    // =====================================================
    // ITEM
    // =====================================================

    private VideoItem createItem(
            String url
    ) {

        VideoItem item =
                new VideoItem();

        item.url =
                url;

        item.fileName =
                getFileName(url);

        item.timestamp =
                System.currentTimeMillis();

        return item;
    }

    private String getFileName(
            String url
    ) {

        try {

            Uri uri =
                    Uri.parse(url);

            String last =
                    uri.getLastPathSegment();

            if (last != null
                    && !last.isEmpty()) {

                String decoded =
                        decode(last);

                if (!decoded.isEmpty()) {

                    return decoded;
                }
            }

        } catch (Exception ignored) {
        }

        try {

            String clean =
                    url.split("\\?")[0];

            int slash =
                    clean.lastIndexOf('/');

            if (slash >= 0
                    && slash <
                    clean.length() - 1) {

                return decode(
                        clean.substring(
                                slash + 1
                        )
                );
            }

        } catch (Exception ignored) {
        }

        return "Video";
    }

    private String decode(
            String value
    ) {

        try {

            return URLDecoder.decode(
                    value,
                    "UTF-8"
            );

        } catch (Exception e) {

            return value;
        }
    }

    private String formatDate(
            long timestamp
    ) {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "dd MMM yyyy  •  hh:mm a",
                        Locale.getDefault()
                );

        return format.format(
                new Date(timestamp)
        );
    }

    // =====================================================
    // STORAGE
    // =====================================================

    private void saveItems(
            String key,
            List<VideoItem> items
    ) {

        StringBuilder data =
                new StringBuilder();

        for (VideoItem item :
                items) {

            if (data.length() > 0) {

                data.append("\n");
            }

            String record =
                    item.timestamp
                            + "\t"
                            + item.fileName
                            + "\t"
                            + item.url;

            String encoded =
                    android.util.Base64
                            .encodeToString(
                                    record.getBytes(
                                            StandardCharsets.UTF_8
                                    ),
                                    android.util.Base64.NO_WRAP
                            );

            data.append(encoded);
        }

        prefs.edit()
                .putString(
                        key,
                        data.toString()
                )
                .apply();
    }

    private List<VideoItem> getItems(
            String key
    ) {

        String data =
                prefs.getString(
                        key,
                        ""
                );

        List<VideoItem> result =
                new ArrayList<>();

        if (data.isEmpty()) {

            return result;
        }

        String[] records =
                data.split("\n");

        for (String encoded :
                records) {

            try {

                byte[] decoded =
                        android.util.Base64.decode(
                                encoded,
                                android.util.Base64.NO_WRAP
                        );

                String record =
                        new String(
                                decoded,
                                StandardCharsets.UTF_8
                        );

                String[] parts =
                        record.split(
                                "\t",
                                3
                        );

                if (parts.length >= 3) {

                    VideoItem item =
                            new VideoItem();

                    item.timestamp =
                            Long.parseLong(
                                    parts[0]
                            );

                    item.fileName =
                            parts[1];

                    item.url =
                            parts[2];

                    result.add(item);
                }

            } catch (Exception ignored) {
            }
        }

        return result;
    }

    // =====================================================
    // LIST HELPERS
    // =====================================================

    private boolean containsUrl(
            List<VideoItem> items,
            String url
    ) {

        for (VideoItem item :
                items) {

            if (item.url.equals(url)) {

                return true;
            }
        }

        return false;
    }

    private void removeUrl(
            List<VideoItem> items,
            String url
    ) {

        for (int i =
                items.size() - 1;
             i >= 0;
             i--) {

            if (items.get(i)
                    .url
                    .equals(url)) {

                items.remove(i);
            }
        }
    }

    private void addEmptyText(
            LinearLayout parent,
            String text
    ) {

        TextView empty =
                makeText(
                        text,
                        18
                );

        empty.setTextColor(GREY);

        empty.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams params =
                fullParams();

        params.height =
                dp(70);

        parent.addView(
                empty,
                params
        );
    }

    // =====================================================
    // MESSAGE
    // =====================================================

    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // =====================================================
    // DATA CLASS
    // =====================================================

    private static class VideoItem {

        String url;
        String fileName;
        long timestamp;
    }
}
