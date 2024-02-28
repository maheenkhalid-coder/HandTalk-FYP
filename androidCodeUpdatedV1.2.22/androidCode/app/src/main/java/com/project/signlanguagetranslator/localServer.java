package com.project.signlanguagetranslator;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fi.iki.elonen.NanoHTTPD;

public class localServer extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private WebView webView;
    private WebServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);

        webView = findViewById(R.id.webView);

        // Enable JavaScript in WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);


        // Set up WebViewClient to load URLs inside the WebView
        webView.setWebViewClient(new WebViewClient());

        // Set up WebChromeClient to handle camera permission requests
        webView.setWebChromeClient(new MyWebChromeClient());

        // Start the HTTP server on a background thread
        server = new WebServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load a web page from the server into the WebView
        webView.loadUrl("http://localhost:8080/index.html");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the server when the activity is destroyed
        if (server != null) {
            server.stop();
        }
    }

    // Custom NanoHTTPD web server
    private class WebServer extends NanoHTTPD {

        public WebServer() {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();

            if ("/index.html".equals(uri)) {
                try {
                    InputStream inputStream = getResources().openRawResource(R.raw.index);//main
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    reader.close();
                    inputStream.close();

                    return newFixedLengthResponse(Response.Status.OK, "text/html", stringBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse("Error loading index.html");
                }
            }

            // Add more conditions to serve other files as needed
            return newFixedLengthResponse("Not Found");
        }
    }

    // Custom WebChromeClient to handle camera permissions
    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            if (request.getOrigin() != null) {
                String[] resources = request.getResources();
                if (resources.length > 0 && PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resources[0])) {
                    if (ContextCompat.checkSelfPermission(localServer.this, android.Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        // Permission already granted
                        request.grant(resources);
                    } else {
                        // Request camera permission from the user
                        ActivityCompat.requestPermissions(localServer.this,
                                new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

    }
}
