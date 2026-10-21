
package com.example.novaai;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String APP_URL =
            "https://novaai-mndxi9nthpjva6diemhbvd.streamlit.app/";

    private WebView webView;
    private LinearLayout errorPanel;
    private TextView errorText;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        errorPanel = findViewById(R.id.errorPanel);
        errorText = findViewById(R.id.errorText);
        Button retryButton = findViewById(R.id.retryButton);

        // Настройки WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(
                WebSettings.MIXED_CONTENT_NEVER_ALLOW
        );
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Cookies для авторизации и состояния сессии
        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient());

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

                // Оставляем сайт и его поддомены внутри WebView
                if ("https".equalsIgnoreCase(scheme)
                        && (host.equalsIgnoreCase("streamlit.app")
                        || host.endsWith(".streamlit.app")
                        || host.equalsIgnoreCase("streamlit.io")
                        || host.endsWith(".streamlit.io"))) {
                    return false;
                }

                // Внешние ссылки открываем в браузере
                if ("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme)) {
                    try {
                        startActivity(
                                new Intent(Intent.ACTION_VIEW, uri)
                        );
                    } catch (Exception e) {
                        showError(
                                "Не удалось открыть ссылку:\n" + uri
                        );
                    }
                    return true;
                }

                return true;
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {
                errorPanel.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                errorPanel.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                cookies.flush();
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                super.onReceivedError(view, request, error);

                // Игнорируем ошибки отдельных картинок и скриптов.
                if (request.isForMainFrame()) {
                    int code = error.getErrorCode();
                    String description =
                            error.getDescription().toString();

                    showError(
                            "Не удалось загрузить NOVA AI.\n\n"
                            + "Код ошибки: " + code + "\n"
                            + "Причина: " + description + "\n\n"
                            + "Проверь интернет и повтори попытку."
                    );
                }
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    android.webkit.WebResourceResponse response
            ) {
                super.onReceivedHttpError(view, request, response);

                if (request.isForMainFrame()) {
                    showError(
                            "Сервер вернул HTTP "
                            + response.getStatusCode()
                            + ".\nПопробуй открыть сайт позже."
                    );
                }
            }
        });

        // Повторная загрузка
        retryButton.setOnClickListener(v -> {
            errorPanel.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(APP_URL);
        });

        // Кнопка «Назад»
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            finish();
                        }
                    }
                }
        );

        if (savedInstanceState == null) {
            webView.loadUrl(APP_URL);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            webView.setVisibility(View.GONE);
            errorPanel.setVisibility(View.VISIBLE);
            errorText.setText(message);
        });
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
            webView.destroy();
        }

        super.onDestroy();
    }
}
