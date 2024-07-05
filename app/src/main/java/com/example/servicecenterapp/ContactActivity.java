package com.example.servicecenterapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ContactActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript for interaction (optional)

        // Load a map image from OpenStreetMap with a marker at specific coordinates
        double latitude = 6.9006030;
        double longitude = 80.2291515;
        String mapUrl = "https://www.openstreetmap.org/export/embed.html?bbox=" +
                (longitude - 0.001) + "," + (latitude - 0.001) + "," +
                (longitude + 0.001) + "," + (latitude + 0.001) +
                "&layer=mapnik&marker=" + latitude + "," + longitude;

        webView.loadUrl(mapUrl);
    }
}
