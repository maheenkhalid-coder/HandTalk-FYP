package com.project.signlanguagetranslator;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import fi.iki.elonen.NanoHTTPD;

public class tts extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private TextToSpeech textToSpeech;
    private WebView webView;
    private WebServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide the status bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);

        webView = findViewById(R.id.webView);

        // Enable JavaScript in WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");


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


        TextToSpeech textToSpeech = new TextToSpeech(this, null);

        // Add the JavaScript interface to the WebView
        JavaScriptInterface jsInterface = new JavaScriptInterface(this, textToSpeech);
        webView.addJavascriptInterface(jsInterface, "AndroidTextToSpeech");

        // Load a web page from the server into the WebView
        webView.loadUrl("http://localhost:8080/login.html?splash=true");//http://localhost:8080/login.html

    }

    // Define a JavaScript interface for communication
    public class JavaScriptInterface {
        private Context context;
        private TextToSpeech textToSpeech;

        public JavaScriptInterface(Context context, TextToSpeech textToSpeech) {
            this.context = context;
            this.textToSpeech = textToSpeech;
        }

        @JavascriptInterface
        public void speak(String textToSpeak, String locale) {
            Locale speechLocale = new Locale(locale);
            // Display a toast message using the valid context
            Toast.makeText(context, textToSpeak, Toast.LENGTH_SHORT).show();
            textToSpeech.setLanguage(speechLocale);
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }

    }


    @Override
    protected void onDestroy() {
        // Stop the server when the activity is destroyed
        if (server != null) {
            server.stop();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    // Custom NanoHTTPD web server
    private class WebServer extends NanoHTTPD {

        public WebServer() {
            super(8080);
        }

        /*@Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();

            if ("/index.html".equals(uri)) {
                try {
                    InputStream inputStream = getResources().openRawResource(R.raw.tts);
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
        }*/
        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();

            if ("/index.html".equals(uri)) {
                return serveResource(R.raw.index, "text/html");
            } else if ("/jquery.js".equals(uri)) {
                return serveResource(R.raw.jquery, "text/javascript");
            } else if ("/t2sign.html".equals(uri)) {
                return serveResource(R.raw.t2sign, "text/html");
            }else if ("/login.html".equals(uri)) {
                return serveResource(R.raw.login, "text/html");
            }else if ("/account.html".equals(uri)) {
                return serveResource(R.raw.accounts, "text/html");
            } else if ("/demoimg.png".equals(uri)) {
                return serveResource(R.raw.demoimg, "image/png");
            } else if ("/bg2.jpg".equals(uri)) {
                return serveResource(R.raw.bg2, "image/jpg");
            } else if ("/splash.jpg".equals(uri)) {
                return serveResource(R.raw.splash, "image/jpg");
            } else if ("/loginimg.png".equals(uri)) {
                return serveResource(R.raw.loginimg, "image/png");
            } else if ("/hi.mp4".equals(uri)) {
                return serveResource(R.raw.hi, "video/mp4");
            } else if ("/excuseme.mp4".equals(uri)) {
                return serveResource(R.raw.excuseme, "video/mp4");
            } else if ("/howareyou.mp4".equals(uri)) {
                return serveResource(R.raw.howareyou, "video/mp4");
            } else if ("/iamfine.mp4".equals(uri)) {
                return serveResource(R.raw.iamfine, "video/mp4");
            } else if ("/iamhungry.mp4".equals(uri)) {
                return serveResource(R.raw.iamhungry, "video/mp4");
            } else if ("/iamsorry.mp4".equals(uri)) {
                return serveResource(R.raw.iamsorry, "video/mp4");
            } else if ("/iamstudent.mp4".equals(uri)) {
                return serveResource(R.raw.iamstudent, "video/mp4");
            } else if ("/iamthirsty.mp4".equals(uri)) {
                return serveResource(R.raw.iamthirsty, "video/mp4");
            } else if ("/nicetomeetyou.mp4".equals(uri)) {
                return serveResource(R.raw.nicetomeetyou, "video/mp4");
            } else if ("/whatdoyoudo.mp4".equals(uri)) {
                return serveResource(R.raw.whatdoyoudo, "video/mp4");
            } else if ("/whatistimenow.mp4".equals(uri)) {
                return serveResource(R.raw.whatistimenow, "video/mp4");
            } else if ("/whatisyourname.mp4".equals(uri)) {
                return serveResource(R.raw.whatisyourname, "video/mp4");
            } else if ("/thankyou.mp4".equals(uri)) {
                return serveResource(R.raw.thankyou, "video/mp4");
            } else if ("/youlookgreat.mp4".equals(uri)) {
                return serveResource(R.raw.youlookgreat, "video/mp4");
            } else if ("/whereareyoufrom.mp4".equals(uri)) {
                return serveResource(R.raw.whereareyoufrom, "video/mp4");
            } else if ("/no.mp4".equals(uri)) {
                return serveResource(R.raw.no, "video/mp4");
            } else if ("/whereareyoufrom.mp4".equals(uri)) {
                return serveResource(R.raw.whereareyoufrom, "video/mp4");
            } else if ("/yes.mp4".equals(uri)) {
                return serveResource(R.raw.yes, "video/mp4");
            } else if ("/iamnotfeelingwell.mp4".equals(uri)) {
                return serveResource(R.raw.iamnotfeelingwell, "video/mp4");
            } else if ("/welcome.mp4".equals(uri)) {
                return serveResource(R.raw.welcome, "video/mp4");
            } else if ("/itsok.mp4".equals(uri)) {
                return serveResource(R.raw.itsok, "video/mp4");
            } else if ("/black.mp4".equals(uri)) {
                return serveResource(R.raw.black, "video/mp4");
            } else if ("/noise.mp4".equals(uri)) {
                return serveResource(R.raw.noise, "video/mp4");
            }
            // Add more conditions to serve other files as needed
            return newFixedLengthResponse("Not Found");
        }

        private Response serveResource(int resourceId, String contentType) {
            try {
                // Open the raw resource
                InputStream inputStream = getResources().openRawResource(resourceId);

                // Create a temporary file to copy the resource to
                File tempFile = File.createTempFile("temp", null, getCacheDir());
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();

                // Serve the temporary file
                FileInputStream fileInputStream = new FileInputStream(tempFile);
                return newChunkedResponse(Response.Status.OK, contentType, fileInputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return newFixedLengthResponse("Error loading resource");
            }
        }



    }

    // Custom WebChromeClient to handle camera permissions
    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            if (request.getOrigin() != null) {
                String[] resources = request.getResources();
                if (resources.length > 0 && PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resources[0])) {
                    if (ContextCompat.checkSelfPermission(tts.this, android.Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        // Permission already granted
                        request.grant(resources);
                    } else {
                        // Request camera permission from the user
                        ActivityCompat.requestPermissions(tts.this,
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
