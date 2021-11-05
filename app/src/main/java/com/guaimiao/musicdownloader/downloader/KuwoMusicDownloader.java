package com.guaimiao.musicdownloader.downloader;

import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.guaimiao.musicdownloader.DownloadActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class KuwoMusicDownloader extends MusicDownloader {

    @Override
    public void interceptRequest(WebResourceRequest request) {
        String url = request.getUrl().toString();
        if(url.contains("api/mobile/v2/music/src/")){
            downloadUrl = url.split("/")[url.split("/").length-1];
            OkHttpClient client = new OkHttpClient();
            Request request1 = new Request.Builder().url("http://m.kuwo.cn/newh5app/api/mobile/v2/music/src/"+downloadUrl).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
            Call call = client.newCall(request1);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        Gson g = new Gson();
                        LinkedTreeMap<String,LinkedTreeMap<String,String>> l = g.fromJson(response.body().string(),LinkedTreeMap.class);
                        downloadUrl = l.get("data").get("url");
                        DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DownloadActivity.getInstance().foundMusic(name, singer);
                            }
                        });
                    }catch (Exception e){e.printStackTrace();}
                }
            });
            Request request2 = new Request.Builder().url("http://m.kuwo.cn/newh5app/api/mobile/v1/music/info/"+downloadUrl).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
            Call call1 = client.newCall(request2);
            call1.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        Gson g = new Gson();
                        LinkedTreeMap<String,LinkedTreeMap<String,LinkedTreeMap<String,Object>>> l = g.fromJson(response.body().string(),LinkedTreeMap.class);
                        name = (String) l.get("data").get("info").get("name");
                        singer = (String) l.get("data").get("info").get("artist_name");
                        DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DownloadActivity.getInstance().foundMusic(name, singer);
                            }
                        });
                    }catch (Exception e){e.printStackTrace();}
                }
            });
        }

        else if(url.contains("api/mobile/v1/music/playlist")){
            OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();
            Request request1 = new Request.Builder().url(url).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
            Call call = client.newCall(request1);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Gson g = new Gson();
                                LinkedTreeMap<String,Object> t1 = g.fromJson(response.body().string(),LinkedTreeMap.class);
                                LinkedTreeMap<String,Object> t2 = (LinkedTreeMap<String, Object>) t1.get("data");
                                int total = (int)(double) t2.get("total");
                                OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();
                                Request request1 = new Request.Builder().url(url.split("\\?")[0]+"?rn="+total+"&ua=&ip=").removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
                                Call call = client.newCall(request1);
                                call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {

                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try{
                                                    Gson g = new Gson();
                                                    LinkedTreeMap<String,Object> t1 = g.fromJson(response.body().string(),LinkedTreeMap.class);
                                                    LinkedTreeMap<String,Object> t2 = (LinkedTreeMap<String, Object>) t1.get("data");
                                                    listName = (String) t2.get("name");
                                                    DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            DownloadActivity.getInstance().foundMusicList(listName);
                                                        }
                                                    });
                                                    ArrayList<LinkedTreeMap<String,Object>> l = (ArrayList<LinkedTreeMap<String, Object>>) t2.get("musicList");
                                                    urlList.clear();
                                                    fileNameList.clear();
                                                    for(int i = 0;i<l.size();i++){
                                                        fileNameList.add(l.get(i).get("name")+" - "+l.get(i).get("artist_name"));
                                                        OkHttpClient client = new OkHttpClient();
                                                        Request request1 = new Request.Builder().url("http://m.kuwo.cn/newh5app/api/mobile/v1/music/info/"+((int)(double)l.get(i).get("id"))).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
                                                        Call call = client.newCall(request1);
                                                        call.enqueue(new Callback() {
                                                            @Override
                                                            public void onFailure(Call call, IOException e) {

                                                            }

                                                            @Override
                                                            public void onResponse(Call call, Response response1) {
                                                                try {
                                                                    Gson g = new Gson();
                                                                    LinkedTreeMap<String,LinkedTreeMap<String,String>> l = g.fromJson(response.body().string(),LinkedTreeMap.class);
                                                                    urlList.add(l.get("data").get("url"));
                                                                }catch (Exception e){
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        }
    }

    @Override
    public void loadedPage(String url) {

    }

    ArrayList<String> urlList = new ArrayList<String>();
    ArrayList<String> fileNameList = new ArrayList<String>();
    String listName;

    @Override
    public void downloadList() {
        downloadList(urlList,fileNameList,listName);
        Toast.makeText(DownloadActivity.getInstance(),"开始下载"+fileNameList.size()+"首歌",Toast.LENGTH_SHORT).show();
    }
}
