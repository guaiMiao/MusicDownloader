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
    public void download() {
        if(downloadUrl.equals("")){
            Toast.makeText(DownloadActivity.getInstance(),"未识别歌曲",Toast.LENGTH_SHORT).show();
            return;
        }
        final int[] downloadType = {0};
        AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.getInstance());
        builder.setSingleChoiceItems(new String[]{"mp3", "aac"}, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadType[0] = which;
            }
        });
        builder.setTitle("选择格式");
        builder.setNeutralButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(downloadType[0] == 0)
                    download(downloadUrl,singer,name);
                else
                    download(aacDownloadUrl,singer,name);
                Toast.makeText(DownloadActivity.getInstance(),"开始下载",Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
    public String aacDownloadUrl = "";

    @Override
    public void interceptRequest(WebResourceRequest request) {
        String url = request.getUrl().toString();
        if(url.contains("api/mobile/v2/music/src/")){
            aacDownloadUrl = url.split("/")[url.split("/").length-1];
            OkHttpClient client = new OkHttpClient();
            Request request1 = new Request.Builder().url("http://www.kuwo.cn/webmusic/st/getMuiseByRid?rid=MUSIC_"+aacDownloadUrl).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
            Call call = client.newCall(request1);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Map<String,String> xml = xmlToMap(response.body().string().replace("&","&amp;"));
                    name = xml.get("name");
                    singer = xml.get("singer");
                    downloadUrl = "http://"+xml.get("mp3dl")+"/"+xml.get("mp3path").replace("//","/");
                    aacDownloadUrl = "http://"+xml.get("aacdl")+"/"+xml.get("aacpath").replace("//","/");
                    DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DownloadActivity.getInstance().foundMusic(name,singer);
                        }
                    });

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
                                                    aacUrlList.clear();
                                                    fileNameList.clear();
                                                    for(int i = 0;i<l.size();i++){

                                                        OkHttpClient client = new OkHttpClient();
                                                        Request request1 = new Request.Builder().url("http://www.kuwo.cn/webmusic/st/getMuiseByRid?rid=MUSIC_"+((int)(double)l.get(i).get("id"))).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
                                                        Call call = client.newCall(request1);
                                                        call.enqueue(new Callback() {
                                                            @Override
                                                            public void onFailure(Call call, IOException e) {

                                                            }

                                                            @Override
                                                            public void onResponse(Call call, Response response1) {
                                                                try {
                                                                    Map<String, String> xml = xmlToMap(response1.body().string().replace("&", "&amp;"));
                                                                    fileNameList.add(xml.get("name") + " - " + xml.get("singer"));
                                                                    urlList.add("http://" + xml.get("mp3dl") + "/" + xml.get("mp3path").replace("//", "/"));
                                                                    aacUrlList.add("http://" + xml.get("aacdl") + "/" + xml.get("aacpath").replace("//", "/"));
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
    ArrayList<String> aacUrlList = new ArrayList<String>();
    String listName;

    @Override
    public void downloadList() {
        final int[] downloadType = {0};
        AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.getInstance());
        builder.setSingleChoiceItems(new String[]{"mp3", "aac"}, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadType[0] = which;
            }
        });
        builder.setTitle("选择格式");
        builder.setNeutralButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(downloadType[0] == 0)
                    downloadList(urlList,fileNameList,listName);
                else
                    downloadList(aacUrlList,fileNameList,listName);
                Toast.makeText(DownloadActivity.getInstance(),"开始下载"+fileNameList.size()+"首歌",Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}
