package com.justplayer.urllauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private EditText urlInput;
    private LinearLayout historyLayout;
    private LinearLayout favouritesLayout;

    private android.content.SharedPreferences prefs;

    private static final String PREFS = "player_data_v3";
    private static final String HISTORY = "history";
    private static final String FAVOURITES = "favourites";

    private static final String JUST_PLAYER_PACKAGE =
            "com.brouken.player";

    private static final int STATUS_UNKNOWN = 0;
    private static final int STATUS_WORKING = 1;
    private static final int STATUS_EXPIRED = 2;
    private static final int STATUS_FAILED = 3;
    private static final int STATUS_CHECKING = 4;

    private final ExecutorService networkExecutor =
            Executors.newCachedThreadPool();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private int dp(int value) {
        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
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

        refreshFavourites();
        refreshHistory();
    }

    // =====================================================
    // UI
    // =====================================================

    private void createUI() {

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

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
                dp(40)
        );

        scrollView.addView(root);

        TextView title =
                new TextView(this);

        title.setText("VIDEO URL LAUNCHER");
        title.setTextSize(36);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        root.addView(
                title,
                fullParams()
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Paste a video URL and choose how to play it"
        );

        subtitle.setTextSize(19);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subParams =
                fullParams();

        subParams.bottomMargin =
                dp(25);

        root.addView(
                subtitle,
                subParams
        );

        urlInput =
                new EditText(this);

        urlInput.setHint(
                "Paste video URL here"
        );

        urlInput.setSingleLine(true);
        urlInput.setTextSize(21);

        urlInput.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        root.addView(
                urlInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(72)
                )
        );

        Button justPlayer =
                makeButton(
                        "▶  PLAY IN JUST PLAYER"
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

        Button choosePlayer =
                makeButton(
                        "🎬  CHOOSE PLAYER"
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

        Button addFavourite =
                makeButton(
                        "☆  ADD CURRENT URL TO FAVOURITES"
                );

        LinearLayout.LayoutParams addFavParams =
                fullParams();

        addFavParams.height =
                dp(62);

        addFavParams.topMargin =
                dp(10);

        root.addView(
                addFavourite,
                addFavParams
        );

        addFavourite.setOnClickListener(
                v -> addCurrentToFavourites()
        );

        TextView favTitle =
                sectionTitle(
                        "⭐ FAVOURITES"
                );

        LinearLayout.LayoutParams favTitleParams =
                fullParams();

        favTitleParams.topMargin =
                dp(32);

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

        Button clearFav =
                makeButton(
                        "🧹  CLEAR ALL FAVOURITES"
                );

        LinearLayout.LayoutParams clearFavParams =
                fullParams();

        clearFavParams.height =
                dp(58);

        clearFavParams.topMargin =
                dp(10);

        root.addView(
                clearFav,
                clearFavParams
        );

        clearFav.setOnClickListener(
                v -> clearFavourites()
        );

        TextView historyTitle =
                sectionTitle(
                        "🕘 LAST-USED HISTORY"
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

        Button clearHistory =
                makeButton(
                        "🧹  CLEAR ALL HISTORY"
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

        setContentView(scrollView);

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
        title.setTextSize(25);
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
        button.setTextSize(18);
        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setFocusable(true);
        button.setFocusableInTouchMode(true);

        button.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        return button;
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

        String url = getCurrentUrl();

        if (url.isEmpty()) {
            showMessage(
                    "Please enter a video URL"
            );
            return;
        }

        addHistory(url);

        /*
         * Resolve a fresh URL first.
         * If resolving fails because of network,
         * we still try the original URL.
         */
        resolveAndPlay(
                url,
                true
        );
    }

    private void choosePlayerForCurrentUrl() {

        String url = getCurrentUrl();

        if (url.isEmpty()) {
            showMessage(
                    "Please enter a video URL"
            );
            return;
        }

        addHistory(url);

        resolveAndChoose(
                url
        );
    }

    private void addCurrentToFavourites() {

        String url = getCurrentUrl();

        if (url.isEmpty()) {
            showMessage(
                    "Enter a URL first"
            );
            return;
        }

        addFavourite(url);
    }

    // =====================================================
    // FRESH RESOLUTION
    // =====================================================

    private void resolveAndPlay(
            String sourceUrl,
            boolean fallbackToSource
    ) {

        setStatusForUrl(
                sourceUrl,
                STATUS_CHECKING
        );

        refreshHistory();
        refreshFavourites();

        networkExecutor.execute(() -> {

            ResolveResult result =
                    resolveUrl(sourceUrl);

            mainHandler.post(() -> {

                if (result.status ==
                        STATUS_WORKING) {

                    updateStatus(
                            sourceUrl,
                            STATUS_WORKING
                    );

                    refreshHistory();
                    refreshFavourites();

                    openInJustPlayer(
                            result.finalUrl
                    );

                } else if (
                        result.status ==
                                STATUS_EXPIRED
                ) {

                    updateStatus(
                            sourceUrl,
                            STATUS_EXPIRED
                    );

                    refreshHistory();
                    refreshFavourites();

                    showMessage(
                            "🔴 Link expired / unavailable"
                    );

                } else {

                    updateStatus(
                            sourceUrl,
                            STATUS_FAILED
                    );

                    refreshHistory();
                    refreshFavourites();

                    if (fallbackToSource) {

                        /*
                         * Network/server check failed.
                         * We don't label it expired.
                         * Try the original source.
                         */
                        openInJustPlayer(
                                sourceUrl
                        );
                    }
                }
            });
        });
    }

    private void resolveAndChoose(
            String sourceUrl
    ) {

        setStatusForUrl(
                sourceUrl,
                STATUS_CHECKING
        );

        refreshHistory();
        refreshFavourites();

        networkExecutor.execute(() -> {

            ResolveResult result =
                    resolveUrl(sourceUrl);

            mainHandler.post(() -> {

                if (result.status ==
                        STATUS_WORKING) {

                    updateStatus(
                            sourceUrl,
                            STATUS_WORKING
                    );

                    refreshHistory();
                    refreshFavourites();

                    openPlayerChooser(
                            result.finalUrl
                    );

                } else if (
                        result.status ==
                                STATUS_EXPIRED
                ) {

                    updateStatus(
                            sourceUrl,
                            STATUS_EXPIRED
                    );

                    refreshHistory();
                    refreshFavourites();

                    showMessage(
                            "🔴 Link expired / unavailable"
                    );

                } else {

                    updateStatus(
                            sourceUrl,
                            STATUS_FAILED
                    );

                    refreshHistory();
                    refreshFavourites();

                    openPlayerChooser(
                            sourceUrl
                    );
                }
            });
        });
    }

    /*
     * Attempts to resolve redirects and checks whether
     * the source is reachable.
     *
     * IMPORTANT:
     * We only save sourceUrl in history.
     * Temporary redirected URLs are NEVER saved.
     */
    private ResolveResult resolveUrl(
            String sourceUrl
    ) {

        HttpURLConnection connection = null;

        try {

            URL url =
                    new URL(sourceUrl);

            connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setInstanceFollowRedirects(
                    true
            );

            connection.setConnectTimeout(
                    12000
            );

            connection.setReadTimeout(
                    12000
            );

            connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
            );

            /*
             * Request only a tiny part of the file.
             * This helps video hosts that don't like HEAD.
             */
            connection.setRequestProperty(
                    "Range",
                    "bytes=0-0"
            );

            connection.setRequestMethod(
                    "GET"
            );

            int code =
                    connection.getResponseCode();

            String finalUrl =
                    connection
                            .getURL()
                            .toString();

            /*
             * 2xx = reachable.
             *
             * 3xx normally gets followed automatically.
             */
            if (code >= 200
                    && code < 300) {

                closeConnection(
                        connection
                );

                return new ResolveResult(
                        STATUS_WORKING,
                        finalUrl
                );
            }

            /*
             * 404 / 410 are strong indicators that
             * the resource is gone/expired.
             */
            if (code == 404
                    || code == 410) {

                closeConnection(
                        connection
                );

                return new ResolveResult(
                        STATUS_EXPIRED,
                        finalUrl
                );
            }

            /*
             * 401/403 can also happen because of
             * anti-hotlink/authentication.
             * We deliberately call these CHECK FAILED
             * rather than falsely calling them expired.
             */
            closeConnection(
                    connection
            );

            return new ResolveResult(
                    STATUS_FAILED,
                    finalUrl
            );

        } catch (Exception e) {

            if (connection != null) {

                closeConnection(
                        connection
                );
            }

            return new ResolveResult(
                    STATUS_FAILED,
                    sourceUrl
            );
        }
    }

    private void closeConnection(
            HttpURLConnection connection
    ) {

        try {

            InputStream stream =
                    connection.getInputStream();

            if (stream != null) {
                stream.close();
            }

        } catch (Exception ignored) {
        }

        connection.disconnect();
    }

    // =====================================================
    // JUST PLAYER
    // =====================================================

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
                    JUST_PLAYER_PACKAGE
            );

            /*
             * Fresh external activity request.
             */
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);

        } catch (Exception e) {

            showMessage(
                    "Just Player is not installed"
            );
        }
    }

    // =====================================================
    // PLAYER CHOOSER
    // =====================================================

    private void openPlayerChooser(
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

        if (history.isEmpty()) {

            addEmptyText(
                    historyLayout,
                    "No recently used videos"
            );

            return;
        }

        for (VideoItem item : history) {

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

        VideoItem item =
                createItem(url);

        favourites.add(
                0,
                item
        );

        saveItems(
                FAVOURITES,
                favourites
        );

        refreshFavourites();
        refreshHistory();

        showMessage(
                "Added to favourites"
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
        refreshHistory();

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

        if (favourites.isEmpty()) {

            addEmptyText(
                    favouritesLayout,
                    "No favourite videos yet"
            );

            return;
        }

        for (VideoItem item : favourites) {

            addVideoRow(
                    favouritesLayout,
                    item,
                    true
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

        // FILENAME

        TextView filename =
                new TextView(this);

        filename.setText(
                "🎬 " + item.fileName
        );

        filename.setTextSize(20);

        filename.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        filename.setMaxLines(2);

        container.addView(
                filename,
                fullParams()
        );

        // DATE / TIME + STATUS

        TextView dateStatus =
                new TextView(this);

        dateStatus.setText(
                getStatusText(item)
                        + "   •   "
                        + formatDate(
                                item.timestamp
                        )
        );

        dateStatus.setTextSize(15);

        container.addView(
                dateStatus,
                fullParams()
        );

        // URL

        TextView urlText =
                new TextView(this);

        urlText.setText(
                item.url
        );

        urlText.setTextSize(13);

        urlText.setMaxLines(1);

        container.addView(
                urlText,
                fullParams()
        );

        // BUTTON ROW

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttons.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // JUST PLAYER

        Button play =
                makeButton(
                        "▶ JUST PLAYER"
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

                    resolveAndPlay(
                            item.url,
                            true
                    );
                }
        );

        // CHOOSE PLAYER

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

                    resolveAndChoose(
                            item.url
                    );
                }
        );

        // CHECK

        Button check =
                makeButton(
                        "↻ CHECK"
                );

        buttons.addView(
                check,
                buttonWeightParams()
        );

        check.setOnClickListener(
                v -> checkOnly(
                        item.url
                )
        );

        // FAVOURITE

        Button favourite =
                makeButton(
                        favouriteSection
                                ? "★"
                                : "☆"
                );

        LinearLayout.LayoutParams smallParams =
                new LinearLayout.LayoutParams(
                        dp(65),
                        dp(58)
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

        // DELETE

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

                        removeFavourite(
                                item.url
                        );

                    } else {

                        deleteHistory(
                                item.url
                        );
                    }
                }
        );

        container.addView(
                buttons,
                fullParams()
        );

        LinearLayout.LayoutParams containerParams =
                fullParams();

        containerParams.bottomMargin =
                dp(18);

        parent.addView(
                container,
                containerParams
        );

        /*
         * TV remote left/right navigation.
         */
        play.setNextFocusRightId(
                choose.getId()
        );

        choose.setNextFocusLeftId(
                play.getId()
        );

        choose.setNextFocusRightId(
                check.getId()
        );

        check.setNextFocusLeftId(
                choose.getId()
        );

        check.setNextFocusRightId(
                favourite.getId()
        );

        favourite.setNextFocusLeftId(
                check.getId()
        );

        favourite.setNextFocusRightId(
                delete.getId()
        );

        delete.setNextFocusLeftId(
                favourite.getId()
        );
    }

    private String getStatusText(
            VideoItem item
    ) {

        switch (item.status) {

            case STATUS_WORKING:
                return "🟢 WORKING";

            case STATUS_EXPIRED:
                return "🔴 EXPIRED";

            case STATUS_FAILED:
                return "🟡 CHECK FAILED";

            case STATUS_CHECKING:
                return "🔄 CHECKING...";

            default:
                return "⚪ NOT CHECKED";
        }
    }

    // =====================================================
    // CHECK ONLY
    // =====================================================

    private void checkOnly(
            String url
    ) {

        setStatusForUrl(
                url,
                STATUS_CHECKING
        );

        refreshHistory();
        refreshFavourites();

        networkExecutor.execute(() -> {

            ResolveResult result =
                    resolveUrl(url);

            mainHandler.post(() -> {

                updateStatus(
                        url,
                        result.status
                );

                refreshHistory();
                refreshFavourites();

                if (result.status ==
                        STATUS_WORKING) {

                    showMessage(
                            "🟢 Link is working"
                    );

                } else if (
                        result.status ==
                                STATUS_EXPIRED
                ) {

                    showMessage(
                            "🔴 Link is expired/unavailable"
                    );

                } else {

                    showMessage(
                            "🟡 Could not verify link"
                    );
                }
            });
        });
    }

    // =====================================================
    // STATUS STORAGE
    // =====================================================

    private void setStatusForUrl(
            String url,
            int status
    ) {

        List<VideoItem> history =
                getItems(HISTORY);

        for (VideoItem item : history) {

            if (item.url.equals(url)) {

                item.status =
                        status;
            }
        }

        saveItems(
                HISTORY,
                history
        );

        List<VideoItem> favourites =
                getItems(FAVOURITES);

        for (VideoItem item : favourites) {

            if (item.url.equals(url)) {

                item.status =
                        status;
            }
        }

        saveItems(
                FAVOURITES,
                favourites
        );
    }

    private void updateStatus(
            String url,
            int status
    ) {

        setStatusForUrl(
                url,
                status
        );
    }

    // =====================================================
    // DELETE / CLEAR
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
        refreshHistory();

        showMessage(
                "Favourites cleared"
        );
    }

    // =====================================================
    // ITEM CREATION
    // =====================================================

    private VideoItem createItem(
            String url
    ) {

        VideoItem item =
                new VideoItem();

        item.url = url;

        item.fileName =
                getFileName(url);

        item.timestamp =
                System.currentTimeMillis();

        item.status =
                STATUS_UNKNOWN;

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

                return decode(last);
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
                        "dd MMM yyyy • hh:mm a",
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

        for (VideoItem item : items) {

            if (data.length() > 0) {

                data.append("\n");
            }

            String record =
                    item.timestamp
                            + "\t"
                            + item.status
                            + "\t"
                            + item.fileName
                            + "\t"
                            + item.url;

            String encoded =
                    android.util.Base64.encodeToString(
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

        for (String encoded : records) {

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
                                4
                        );

                if (parts.length >= 4) {

                    VideoItem item =
                            new VideoItem();

                    item.timestamp =
                            Long.parseLong(
                                    parts[0]
                            );

                    item.status =
                            Integer.parseInt(
                                    parts[1]
                            );

                    item.fileName =
                            parts[2];

                    item.url =
                            parts[3];

                    result.add(item);
                }

            } catch (Exception ignored) {
            }
        }

        return result;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private boolean containsUrl(
            List<VideoItem> items,
            String url
    ) {

        for (VideoItem item : items) {

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

        for (int i = items.size() - 1;
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
                new TextView(this);

        empty.setText(text);
        empty.setTextSize(18);
        empty.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams params =
                fullParams();

        params.height =
                dp(60);

        parent.addView(
                empty,
                params
        );
    }

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
    // DATA
    // =====================================================

    private static class VideoItem {

        String url;

        String fileName;

        long timestamp;

        int status =
                STATUS_UNKNOWN;
    }

    private static class ResolveResult {

        int status;

        String finalUrl;

        ResolveResult(
                int status,
                String finalUrl
        ) {

            this.status =
                    status;

            this.finalUrl =
                    finalUrl;
        }
    }
}
