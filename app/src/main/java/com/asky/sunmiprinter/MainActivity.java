package com.asky.sunmiprinter;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
    private PrinterBridge printer;
    private TextView status;
    private WebView web;
    private static final String DEFAULT_URL = "https://skyapp.askyservices.net.pk/";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        printer = new PrinterBridge(this);
        buildUi();
        printer.connect();
        updateStatus();
    }
    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.BLACK);
        LinearLayout bar = new LinearLayout(this); bar.setPadding(12,8,12,8); bar.setBackgroundColor(Color.BLACK);
        status = new TextView(this); status.setTextColor(Color.rgb(0,255,102)); status.setTextSize(12); status.setText("SUNMI printer: connecting...");
        Button test = new Button(this); test.setText("TEST PRINT"); test.setOnClickListener(v -> printer.printText("A. SKY SERVICES\nSUNMI V2 PRO\nPrinter Test OK\n\n", (ok,msg)->runOnUiThread(()->toast(msg))));
        Button reconnect = new Button(this); reconnect.setText("CONNECT"); reconnect.setOnClickListener(v->{printer.connect(); updateStatus();});
        bar.addView(status,new LinearLayout.LayoutParams(0,55,1)); bar.addView(reconnect,new LinearLayout.LayoutParams(110,55)); bar.addView(test,new LinearLayout.LayoutParams(120,55));
        root.addView(bar);
        web = new WebView(this); WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setAllowFileAccess(true); s.setMediaPlaybackRequiresUserGesture(false); web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient()); web.addJavascriptInterface(new JsBridge(),"SunmiPrinter"); web.loadUrl(DEFAULT_URL);
        root.addView(web,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1)); setContentView(root);
    }
    private void updateStatus(){ new Thread(()->{try{Thread.sleep(800);}catch(Exception ignored){} runOnUiThread(()->status.setText("SUNMI: "+printer.status()));}).start(); }
    private void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_SHORT).show(); }
    public class JsBridge {
        @JavascriptInterface public void printText(String text){ runOnUiThread(()->printer.printText(text,(ok,msg)->runOnUiThread(()->toast(msg)))); }
        @JavascriptInterface public String getStatus(){ return printer.status(); }
        @JavascriptInterface public void connect(){ printer.connect(); }
    }
    @Override public void onDestroy(){ printer.disconnect(); super.onDestroy(); }
    @Override public void onBackPressed(){ if(web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
