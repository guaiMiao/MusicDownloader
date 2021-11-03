package com.guaimiao.musicdownloader.downloader;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;
import com.guaimiao.musicdownloader.DownloadActivity;
import com.guaimiao.musicdownloader.MainActivity;
import com.guaimiao.musicdownloader.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class KugouMusicDownloader extends MusicDownloader {
    @Override
    public void interceptRequest(WebResourceRequest request) {
        String url = request.getUrl().toString();
        if(url.contains("/api/v1/song/get_song_info_v2")){
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
                                Gson g = new Gson();
                                LinkedTreeMap<String,Object> g1 = g.fromJson(response.body().string(), LinkedTreeMap.class);
//                                Log.e("test",g1.toString());
                                LinkedTreeMap<String,String> g2 = (LinkedTreeMap<String, String>) g1.get("data");
                                singer = (String) g2.get("choricSinger");
                                name = (String) g2.get("songName");
                                downloadUrl = (String) g2.get("url");
                                DownloadActivity.getInstance().foundMusic(name,singer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }

    String listName;
    ArrayList<String> urlList = new ArrayList<String>();
    ArrayList<String> fileNameList = new ArrayList<String>();

    @Override
    public void loadedPage(String url) {
        if(url.contains("plist/list")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String listid = url.split("plist/list/")[1];
                        String newUrl = "https://www.kugou.com/yy/special/single/" + listid + ".html";
                        Document doc = Jsoup.connect(newUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36").get();
                        listName = doc.select("strong").text();
                        listName = listName.substring(listName.indexOf("<")+1,listName.indexOf(">"));
                        DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DownloadActivity.getInstance().foundMusicList(listName);
                            }
                        });

                        Element div = doc.select("div[id=songs]").first();
                        Element ul = null;
                        for (Element i:div.children()){
                            if(i.tagName().equals("ul")){
                                ul = i;
                                break;
                            }
                        }
                        Elements a = ul.select("a");
                        for(Element i:a){
                            OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();
                            Request request1 = new Request.Builder().url(i.attr("href")).removeHeader("User-Agent").addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36").build();
                            Call call = client.newCall(request1);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) { }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        Document doc = Jsoup.connect(response.request().url().toString()).userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36").get();
                                        Elements elements = doc.select("script");
                                        String hash = "";
                                        String album_id = "";
                                        for(Element i:elements) {
                                            if (i.html().contains("dataFromSmarty")) {
                                                Gson g = new Gson();
                                                String jsonData = i.html().substring(i.html().indexOf("[")+1,i.html().indexOf("]"));
                                                LinkedTreeMap<String,Object> map = g.fromJson(jsonData,LinkedTreeMap.class);
                                                hash = (String) map.get("hash");
                                                album_id = ((int)(double)map.get("album_id"))+"";
                                                break;
                                            }
                                        }

                                        String newUrl = "https://m3ws.kugou.com/api/v1/song/get_song_info_v2?cmd=playInfo&from=mkugou&apiver=2&mid=1e8d9450ee27ae0e73ad10dea18dee1c&userid=0&platid=4&dfid=2B4X0T1jW4EU45wcnn4Y8nOC&hash=" + hash+"&album_id"+album_id;
                                        OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();
                                        Request request1 = new Request.Builder().url(newUrl).removeHeader("User-Agent").addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36").build();
                                        Call call1 = client.newCall(request1);
                                        call1.enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {

                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            Gson g = new Gson();
                                                            LinkedTreeMap<String, Object> g1 = g.fromJson(response.body().string(), LinkedTreeMap.class);
                                                            LinkedTreeMap<String, String> g2 = (LinkedTreeMap<String, String>) g1.get("data");
                                                            String ssinger = (String) g2.get("choricSinger");
                                                            String sname = (String) g2.get("songName");
                                                            fileNameList.add(sname + " - " + ssinger);
                                                            urlList.add((String) g2.get("url"));
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }catch (Exception e){e.printStackTrace();Log.i("test",response.request().url().toString());}
                                }
                            });
                        }
                    }catch (Exception e){e.printStackTrace();Log.i("test",listName);}
                }
            }).start();

        }
    }

    @Override
    public void downloadList() {
        downloadList(urlList,fileNameList,listName);
        Toast.makeText(DownloadActivity.getInstance(),"开始下载"+fileNameList.size()+"首歌",Toast.LENGTH_SHORT).show();
    }
}
