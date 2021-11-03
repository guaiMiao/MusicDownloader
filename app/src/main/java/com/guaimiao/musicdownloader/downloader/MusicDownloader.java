package com.guaimiao.musicdownloader.downloader;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.guaimiao.musicdownloader.DownloadActivity;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public abstract class MusicDownloader {
    private static MusicDownloader[] downloaders = {new NeteaseMusicDownloader(),null,new KuwoMusicDownloader(),new KugouMusicDownloader(),new AnyMusicDownloader()};
    public static void init(){
        downloaders[0] = new NeteaseMusicDownloader();
        downloaders[1] = new QQMusicDownloader();
        downloaders[2] = new KuwoMusicDownloader();
        downloaders[3] = new KugouMusicDownloader();
        downloaders[4] = new AnyMusicDownloader();
    }
    public static MusicDownloader getMusicDownloader(int index){ return downloaders[index];}
    public void download(){
            if (downloadUrl.equals("")) {
                Toast.makeText(DownloadActivity.getInstance(), "未识别歌曲", Toast.LENGTH_SHORT).show();
                return;
            }
            download(downloadUrl, singer, name);
            Toast.makeText(DownloadActivity.getInstance(), "开始下载", Toast.LENGTH_SHORT).show();
    }
    public abstract void interceptRequest(WebResourceRequest request);
    public abstract void loadedPage(String url);
    protected String name = "";
    protected String singer = "";
    protected String downloadUrl = "";
    public static void download(String url,String singer,String name){
        singer = singer.replace("/","、");
        name = name.replace("/","、");
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir("Music","/MusicDownload/"+name+" - "+singer+"."+url.split("/")[url.split("/").length-1].split("\\.")[1]);
        request.setTitle("音乐下载:   "+name+" - "+singer);
        request.setDescription("曲名："+name+"    作者:"+singer);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager downloadManager= (DownloadManager) DownloadActivity.getInstance().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }
    public static void download(String url,String filename){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir("Music","/MusicDownload/"+filename.replace("/","、")+"."+url.split("/")[url.split("/").length-1].split("\\.")[1]);
        request.setTitle("音乐下载:   "+filename);
        request.setDescription(filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager downloadManager= (DownloadManager) DownloadActivity.getInstance().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }
    public static void downloadList(ArrayList<String> urllist, ArrayList<String> filenames,String listname){
        for(int i = 0;i<urllist.size();i++){
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urllist.get(i)));
            request.setDestinationInExternalPublicDir("Music","/MusicDownload/"+listname.replace("/","、")+"/"+filenames.get(i).replace("/","、")+"."+urllist.get(i).split("/")[urllist.get(i).split("/").length-1].split("\\.")[1]);
            request.setTitle("音乐下载:   "+filenames.get(i));
            request.setDescription(filenames.get(i));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager downloadManager= (DownloadManager) DownloadActivity.getInstance().getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        }

    }
    public static Map<String, String> xmlToMap(String xml) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            stream.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getUserAgent() {
        String userAgent = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(DownloadActivity.getInstance());
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public abstract void downloadList();
}
