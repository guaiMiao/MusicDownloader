package com.guaimiao.musicdownloader;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.guaimiao.musicdownloader.databinding.ActivityDownloadBinding;
import com.guaimiao.musicdownloader.downloader.MusicDownloader;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
//    private static final String[] platformSearchUrl = {"http://y.music.163.com/m/","https://y.qq.com/n/ryqq/search","http://kuwo.cn/newh5app","https://m.kugou.com"};
    private static final String[] platformSearchUrl = {"http://y.music.163.com/m/","https://i.y.qq.com/n2/m/index.html","https://www.kuwo.cn/","https://m.kugou.com","https://www.baidu.com/"};

    private int type;

    public Button ok;
    private static DownloadActivity instance;
    public static DownloadActivity getInstance(){return instance;}
    public WebView webView;
    TextView urlText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        instance = this;

        setSupportActionBar(findViewById(R.id.toolbar));

        Intent intent = getIntent();
        type = intent.getIntExtra("url",0);
        String url = platformSearchUrl[type];

        urlText = findViewById(R.id.urltext);
        Button back = findViewById(R.id.back);

        Button cancel = findViewById(R.id.cancel);
        ok = findViewById(R.id.ok);

        webView = findViewById(R.id.webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setDomStorageEnabled(true);
        final String[] downloadUrl = {"","","",""};
        final boolean[] isDownloading = {false};
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient(){

        });
        webView.setWebViewClient(new WebViewClient(){
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.i("intercept request",url);
                MusicDownloader.getMusicDownloader(type).interceptRequest(request);
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = Uri.parse(url);
                if (!"http".equals(uri.getScheme()) || !"https".equals(uri.getScheme())) {
                    return false;
                } else {
                    view.loadUrl(url);
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(!foundMusic) urlText.setText(url);
                MusicDownloader.getMusicDownloader(type).loadedPage(url);
            }
        });

        Button ua = findViewById(R.id.uachange);
        ua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ua.getText().toString().equals("电脑UA")){
                    ua.setText("手机UA");
                    webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
                    webView.loadUrl(webView.getUrl());
                }
                else{
                    ua.setText("电脑UA");
                    webSettings.setUserAgentString(WebSettings.getDefaultUserAgent(DownloadActivity.this));
                    webView.loadUrl(webView.getUrl());
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(DownloadActivity.this,MainActivity.class);
                startActivity(intent1);
                finish();
                MainActivity.getInstance().updateListview();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ok.getText().toString().equals("下载"))
                    MusicDownloader.getMusicDownloader(type).download();

                else if(ok.getText().toString().equals("下载全部"))
                    MusicDownloader.getMusicDownloader(type).downloadList();
            }
        });
        MusicDownloader.init();
    }
    private boolean foundMusic = false;
    public void foundMusic(String name,String singer){
        urlText.setText("识别到歌曲:"+name+"     作者:"+singer);
        ok.setText("下载");
        foundMusic =true;
    }
    public void foundMusicList(String name){
        urlText.setText("识别到歌单/专辑:"+name);
        ok.setText("下载全部");
        foundMusic =true;
    }
}