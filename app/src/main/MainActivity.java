package com.novaai.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
    // Replace this with the exact public Streamlit URL before building the APK.
    private static final String APP_URL = "PASTE_YOUR_STREAMLIT_URL_HERE";

    private WebView webView;
    private ProgressBar progress;
    private LinearLayout errorPanel;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(5, 9, 20));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(5, 9, 20));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (host != null && (host.endsWith("streamlit.app") || host.endsWith("streamlit.io"))) return false;
                if ("https".equalsIgnoreCase(uri.getScheme())) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }
                return false;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                progress.setVisibility(View.VISIBLE);
                errorPanel.setVisibility(View.GONE);
            }
            @Override public void onPageFinished(WebView view, String url) { progress.setVisibility(View.GONE); }
            @Override public void onReceivedError(WebView view, WebResourceRequest request,
                                                   android.webkit.WebResourceError error) {
                if (request.isForMainFrame()) {
                    progress.setVisibility(View.GONE);
                    errorPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        progress = new ProgressBar(this);
        errorPanel = new LinearLayout(this);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setPadding(32, 32, 32, 32);
        errorPanel.setBackgroundColor(Color.rgb(5, 9, 20));
        errorPanel.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("NOVA AI");
        title.setTextColor(Color.rgb(83, 151, 255));
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        errorPanel.addView(title);

        TextView note = new TextView(this);
        note.setText("Не удалось открыть NOVA AI. Проверь интернет и адрес сайта.");
        note.setTextColor(Color.WHITE);
        note.setTextSize(16);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 18, 0, 18);
        errorPanel.addView(note);

        Button retry = new Button(this);
        retry.setText("Повторить");
        retry.setOnClickListener(v -> loadApp());
        errorPanel.addView(retry);

        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER);
        root.addView(progress, pp);
        root.addView(errorPanel, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        if (state == null) loadApp(); else webView.restoreState(state);
    }

    private void loadApp() {
        if (APP_URL.startsWith("https://") && !APP_URL.contains("PASTE_YOUR_STREAMLIT_URL_HERE")) {
            webView.loadUrl(APP_URL);
        } else {
            progress.setVisibility(View.GONE);
            errorPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (webView != null) webView.saveState(out);
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.stopLoading(); webView.destroy(); }
        super.onDestroy();
    }
}
