package com.justplayer.urllauncher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText urlInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 40, 60, 40);

        urlInput = new EditText(this);
        urlInput.setHint("Paste video URL here");
        urlInput.setSingleLine(true);
        urlInput.setTextSize(20);

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        layout.addView(urlInput, inputParams);

        Button openButton = new Button(this);
        openButton.setText("OPEN IN JUST PLAYER");
        openButton.setTextSize(18);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = 30;
        layout.addView(openButton, buttonParams);

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInJustPlayer();
            }
        });

        setContentView(layout);

        urlInput.requestFocus();
    }

    private void openInJustPlayer() {

        String url = urlInput.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please enter a video URL",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

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
}
