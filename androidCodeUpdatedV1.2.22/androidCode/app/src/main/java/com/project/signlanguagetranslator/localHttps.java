package com.project.signlanguagetranslator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class localHttps extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);

        webView = findViewById(R.id.webView);

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set up WebViewClient to load URLs inside the WebView
        webView.setWebViewClient(new SSLTolerantWebViewClient());

        // Set up WebChromeClient to handle camera permission requests
        webView.setWebChromeClient(new MyWebChromeClient());

        // Load the HTML file with the camera preview
        webView.loadUrl("https://192.168.2.110/fyp/singlanguage/"); // Use the correct URL

        // Add permission check for CAMERA here if needed
    }

    // Custom WebChromeClient to handle camera permissions
    private class MyWebChromeClient extends WebChromeClient {
        @SuppressLint("NewApi")
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            if (request.getOrigin() != null) {
                // Check if the request is for camera permission
                if (request.getResources()[0].equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(localHttps.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        // Permission already granted
                        request.grant(request.getResources());
                    } else {
                        // Request camera permission from the user
                        ActivityCompat.requestPermissions(localHttps.this,
                                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                    }
                }
            } else {
                super.onPermissionRequest(request);
            }
        }

        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            super.onPermissionRequestCanceled(request);
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                webView.reload(); // Reload the WebView to apply permissions
            } else {
                // Permission denied
                // Handle this case as needed
            }
        }
    }

    // SSL Error Tolerant WebView Client
    private class SSLTolerantWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }
    }
}
