
package com.novaai.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.content.Intent;
import android.view.View;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String APP_URL =
            "https://novaai-mndxi9nthpjva6diemhbvd.streamlit.app/";

    private FrameLayout root;
    private WebView webView;
    private LinearLayout errorPanel;
    private TextView errorText;
    private ProgressBar progressBar;

    private boolean pageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(7, 10, 16));
        getWindow().setNavigationBarColor(Color.rgb(7, 10, 16));

        createInterface();
        configureWebView();

        if (savedInstanceState == null) {
            webView.loadUrl(APP_URL);
        } else {
            webView.restoreState(savedInstanceState);
            if (webView.getUrl() == null) {
                webView.loadUrl(APP_URL);
            }
        }
    }

    private void createInterface() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(7, 10, 16));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(7, 10, 16));

        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams progressParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                );

        root.addView(progressBar, progressParams);

        errorPanel = new LinearLayout(this);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setPadding(28, 28, 28, 28);
        errorPanel.setBackgroundColor(Color.rgb(7, 10, 16));
        errorPanel.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("NOVA AI");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        errorText = new TextView(this);
        errorText.setTextColor(Color.LTGRAY);
        errorText.setTextSize(15);
        errorText.setGravity(Gravity.CENTER);
        errorText.setPadding(0, 20, 0, 24);
        errorText.setText(
                "Не удалось открыть NOVA AI.\nПроверь интернет."
        );

        Button retryButton = new Button(this);
        retryButton.setText("Повторить");
        retryButton.setTextColor(Color.WHITE);
        retryButton.setAllCaps(false);

        GradientDrawable buttonBackground = new GradientDrawable();
        buttonBackground.setColor(Color.rgb(25, 75, 150));
        buttonBackground.setCornerRadius(24);
        retryButton.setBackground(buttonBackground);

        retryButton.setOnClickListener(v -> {
            errorPanel.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            pageLoaded = false;
            webView.loadUrl(APP_URL);
        });

        errorPanel.addView(title);
        errorPanel.addView(errorText);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        errorPanel.addView(retryButton, buttonParams);

        root.addView(errorPanel, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        setContentView(root);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && !pageLoaded) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                String host = uri.getHost();

                if (scheme == null || host == null) {
                    return true;
                }

                // Сайт Streamlit и его поддомены открываются в приложении.
                if ("https".equalsIgnoreCase(scheme)
                        && (host.equalsIgnoreCase("streamlit.app")
                        || host.endsWith(".streamlit.app")
                        || host.equalsIgnoreCase("streamlit.io")
                        || host.endsWith(".streamlit.io"))) {
                    return false;
                }

                // Остальные HTTP(S)-ссылки открываем во внешнем браузере.
                if ("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme)) {
                    try {
                        startActivity(new Intent(
                                Intent.ACTION_VIEW, uri
                        ));
                    } catch (Exception e) {
                        showError("Не удалось открыть внешнюю ссылку.");
                    }
                }

                return true;
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {
                pageLoaded = false;
                errorPanel.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pageLoaded = true;
                progressBar.setVisibility(View.GONE);
                errorPanel.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                super.onReceivedError(view, request, error);

                if (request.isForMainFrame()) {
                    int code = error.getErrorCode();
                    String description =
                            error.getDescription().toString();

                    showError(
                            "Не удалось загрузить сайт.\n\n"
                            + "Код: " + code + "\n"
                            + "Причина: " + description
                            + "\n\nПроверь подключение к интернету."
                    );
                }
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceResponse response
            ) {
                super.onReceivedHttpError(view, request, response);

                if (request.isForMainFrame()) {
                    showError(
                            "Ошибка сервера HTTP "
                            + response.getStatusCode()
                            + ".\nПопробуй повторить попытку позже."
                    );
                }
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            errorPanel.setVisibility(View.VISIBLE);
            errorText.setText(message);
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }
}