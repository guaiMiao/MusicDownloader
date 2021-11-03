package com.guaimiao.musicdownloader.downloader;

import android.util.Log;
import android.webkit.WebResourceRequest;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.guaimiao.musicdownloader.DownloadActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//未实现
//无法获取vkey
class QQMusicDownloader extends MusicDownloader {
    @Override
    public void interceptRequest(WebResourceRequest request) {
        String url = request.getUrl().toString();
        if(url.contains("fcg")){
            OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();
            Request request1 = new Request.Builder().url(url).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
            Call call = client.newCall(request1);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.i("test",response.request().url().toString());
                                Log.i("test",response.body().string());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void loadedPage(String url) {

    }

    @Override
    public void downloadList() {

    }
}
