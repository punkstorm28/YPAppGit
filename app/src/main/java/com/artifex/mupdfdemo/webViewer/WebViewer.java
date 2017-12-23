package com.artifex.mupdfdemo.webViewer;

import android.app.AlertDialog;
import android.content.Context;

import com.artifex.mupdfdemo.YoungPioneerMain;

public class WebViewer
{
 android.webkit.WebView ypView ;
   public void showInWebView(String url, Context ctx){

        ypView =new android.webkit.WebView(ctx);
        ypView.getSettings().setJavaScriptEnabled(true);
        ypView.getSettings().setUseWideViewPort(true);
       AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
          Client webViewClient= new Client(alert);
        ypView.setWebViewClient(webViewClient);
        ypView.loadUrl(url);
        ypView.getSettings().setBuiltInZoomControls(true);

        //alert.setTitle("Title here");
        alert.setView(ypView);
        alert.setCancelable(false);

        alert.show();


    }
}
