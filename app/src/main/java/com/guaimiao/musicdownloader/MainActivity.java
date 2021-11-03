package com.guaimiao.musicdownloader;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.guaimiao.musicdownloader.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    public static MainActivity getInstance(){return instance;}

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private static final String[] nperimissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};

    public static int pxtodip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static int dptopx(Context context, float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }

    private static final String[] platform = {"网易云音乐","QQ音乐","酷我音乐","酷狗音乐","音频拦截"};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class MyArrayAdapter extends ArrayAdapter<String>{
        public MyArrayAdapter(@NonNull Context context, int resource,List<String> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LinearLayout linearLayout = new LinearLayout(MainActivity.this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ImageView imageView = new ImageView(MainActivity.this);

            if(getItem(position).equals("...")) imageView.setImageResource(R.drawable.fileselecter_folder_icon);
            else if(getItem(position).contains(".")) imageView.setImageResource(R.drawable.fileselecter_file_icon);
            else imageView.setImageResource(R.drawable.fileselecter_folder_icon);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(dptopx(MainActivity.this,40),dptopx(MainActivity.this,40)));
            linearLayout.addView(imageView);
            TextView textView = new TextView(MainActivity.this);
            textView.setText(getItem(position));
            textView.setTextSize(15);
            textView.setTextColor(Color.BLACK);
            linearLayout.addView(textView);

            return linearLayout;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            instance = this;
            setContentView(R.layout.activity_main);

            setSupportActionBar(findViewById(R.id.toolbar));

            findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("选择平台");
                    int[] select = {0};
                    builder.setSingleChoiceItems(platform, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            select[0] = which;
                        }
                    });
                    builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which==1){
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                                builder1.setMessage("qq音乐暂未实现\n如需下载请使用音频拦截");
                                builder1.show();
                                return;
                            }
                            Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                            intent.putExtra("url", select[0]);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }
            });

            //检查权限并申请
            for (String i : nperimissions) {
                if (ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},100);
                }
            }

            updateListview();
        }catch (Exception e){e.printStackTrace();}
    }

    //更新歌曲列表
    File viewing = new File(Environment.getExternalStoragePublicDirectory("Music")+"/MusicDownload");
    File defultViewing = new File(Environment.getExternalStoragePublicDirectory("Music")+"/MusicDownload");
    public void updateListview(){
        ListView lv = findViewById(R.id.listview);
        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1,new ArrayList<String>());
        if (!viewing.exists()) {
            viewing.mkdirs();
        }
        if(viewing.listFiles().length!=0){
            for(File i:viewing.listFiles()){
                adapter.add(i.getName());
            }
        }
        if(!viewing.toString().equals(defultViewing.toString())){
            adapter.add("...");
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(adapter.getItem(position).equals("...")){
                    viewing = viewing.getParentFile();
                    updateListview();
                }
                else if(adapter.getItem(position).contains(".")){
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    File musicFile = new File(viewing+"/"+adapter.getItem(position));
                    Uri uri = FileProvider.getUriForFile(MainActivity.this,"com.guaimiao.musicdownloader.provider",musicFile);
                    intent.setDataAndType(uri,"audio/"+adapter.getItem(position).split("\\.")[adapter.getItem(position).split("\\.").length-1]);
                    startActivity(intent);
                }
                else{
                    viewing = new File(viewing+"/"+adapter.getItem(position));
                    updateListview();
                }
            }
        });
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            updateListview();
        }
        else if(id == R.id.action_help){

        }

        return super.onOptionsItemSelected(item);
    }

}