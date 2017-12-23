package com.artifex.mupdfdemo.webViewer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by vyomkeshjha on 29/06/16.
 */
public class Client extends WebViewClient {
    AlertDialog.Builder  dialog;
    public Client( AlertDialog.Builder  dialog) {
        this.dialog=dialog;
    }

    @Override
    public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(android.webkit.WebView view, String url) {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);
        Log.i("WebViewX","Download complete");
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
   if(dialog!=null)
       dialog.setTitle("Connection Failed");
        dialog.setCancelable(true);
        dialog.setMessage("Check your internet connection");
     }
}
