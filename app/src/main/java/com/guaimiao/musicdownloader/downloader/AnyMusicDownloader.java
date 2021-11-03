package com.guaimiao.musicdownloader.downloader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.widget.EditText;
import android.widget.Toast;

import com.guaimiao.musicdownloader.DownloadActivity;

class AnyMusicDownloader extends MusicDownloader {
    public static final String musicTypes[] = {".mp3",".aac",".m4a",".ogg",".ape",".flac",".wav",".arm"};
    @Override
    public void interceptRequest(WebResourceRequest request) {
        String url = request.getUrl().toString();
            for (String i:musicTypes){
                if(url.contains(i)){
                    DownloadActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.getInstance());
                            builder.setTitle("识别到歌曲");
                            builder.setMessage("识别到"+url);
                            builder.setNeutralButton("这不是我想要的",null);
                            builder.setNegativeButton("预览", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DownloadActivity.getInstance().webView.loadUrl(url);
                                    downloadUrl = url;
                                    DownloadActivity.getInstance().foundMusic(url,"");
                                }
                            });
                            builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadUrl = url;
                                    download();
                                }
                            });
                            builder.show();
                        }
                    });

                    Log.i("test","test");
                }
            }

    }

    @Override
    public void download() {
        if(downloadUrl.equals("")){
            Toast.makeText(DownloadActivity.getInstance(),"未识别歌曲",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.getInstance());
        builder.setTitle("输入文件名");
        EditText et = new EditText(builder.getContext());
        et.setText(downloadUrl.split("/")[downloadUrl.split("/").length-1].split("\\.")[0]);
        builder.setView(et);
        builder.setNeutralButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                download(downloadUrl,et.getText().toString());
                Toast.makeText(DownloadActivity.getInstance(),"开始下载",Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();

    }

    @Override
    public void loadedPage(String url) {

    }

    @Override
    public void downloadList() {

    }
}
