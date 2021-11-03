package com.guaimiao.musicdownloader.downloader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.guaimiao.musicdownloader.DownloadActivity;
import com.guaimiao.musicdownloader.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class NeteaseMusicDownloader extends MusicDownloader {

    @Override
    public void interceptRequest(WebResourceRequest request) {

    }

    ArrayList<String> downloadList = new ArrayList<String>();
    ArrayList<String> fileNameList = new ArrayList<String>();
    String listName;

    @Override
    public void loadedPage(String url) {
        if(url.contains("/song?id=")) {
            String[] title = DownloadActivity.getInstance().webView.getTitle().split(" - ");
            if(title.length>=2) {
                name = title[1];
                singer = title[0];
                downloadUrl = "https://music.163.com/song/media/outer/url?id=" + url.split("id=")[1] + ".mp3";
                OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();
                Request request1 = new Request.Builder().url(downloadUrl).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
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
                                    downloadUrl = response.request().url().toString();
                                    DownloadActivity.getInstance().foundMusic(name,singer);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        }else if(url.contains("/playlist?id=")){
            if(!url.contains("my/m/music")){//未登录（不完整的歌单）
                if(url.contains("y.music")) {//手机端
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Document doc = Jsoup.connect(url).get();
                                downloadList.clear();
                                fileNameList.clear();
//                            Log.e("test",doc.body().toString());
                                for (Element i : doc.body().select("div[class=f-thide sginfo]")) {
                                    fileNameList.add(i.text());
                                }
                                for (Element i : doc.select("a[class=m-sgitem]")) {
                                    String gurl = "https://music.163.com/song/media/outer/url?id=" + i.attr("href").split("id=")[1] + ".mp3";
                                    OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();
                                    Request request1 = new Request.Builder().url(gurl).removeHeader("User-Agent").addHeader("User-Agent", getUserAgent()).build();
                                    Call call = client.newCall(request1);
                                    call.enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) { }
                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        downloadList.add(response.request().url().toString());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                                DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        listName = doc.select("h2[class=f-thide2 f-brk lsthd_title]").text() + "    共" + fileNameList.size() + "首" + "           提示：这是一个不完整的歌单，点击[电脑UA]并登录，在“我的音乐”下载完整歌单";
                                        DownloadActivity.getInstance().foundMusicList(listName);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                else{

                }
            }


        }
    }

    @Override
    public void downloadList() {
        if (listName.contains("           提示：这是一个不完整的歌单，点击[电脑UA]并登录，在“我的音乐”下载完整歌单")){
            AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.getInstance());
            builder.setTitle("提示");
            builder.setMessage("这是一个不完整的歌单\n你可以点击[电脑UA]并登录，在“我的音乐”下载完整歌单\n是否继续下载?");
            builder.setPositiveButton("我就要这些歌，下载！", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    downloadList(downloadList,fileNameList,listName);
                }
            });
            builder.setPositiveButton("我就不完整的，下载！", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listName = listName.replace("           提示：这是一个不完整的歌单，点击[电脑UA]并登录，在“我的音乐”下载完整歌单","");
                    listName = listName.split("    共")[0];
                    downloadList(downloadList,fileNameList,listName);
                    Toast.makeText(DownloadActivity.getInstance(),"开始下载"+downloadList.size()+"首歌",Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("算了",null);
            builder.show();
        }
    }
}
