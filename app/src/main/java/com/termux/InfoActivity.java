package com.termux;

import android.os.Bundle;
import android.app.Activity;
import android.webkit.WebView;

public class InfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        WebView webView = (WebView) findViewById(R.id.infoView);
        webView.loadUrl("file:///android_asset/info.html");
    }
}
