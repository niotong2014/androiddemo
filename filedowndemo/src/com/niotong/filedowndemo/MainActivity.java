package com.niotong.filedowndemo;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ActionBar;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity implements OnClickListener{

	Button mbtCreatFile;
	Button mbtDelFile;
	Button mbtIsFileExist;
	Button mbtCreatDir;
	Button mbtDelDir;
	Button mbtIsDirExist;
	Button mbtDownFile;
	
	Toast tst;
	
	public String FILE_NAME = "niotongyuan";
	public String DIR_NAME = "niotongdir";
	public String FILE_URL = "http://upload.cbg.cn/2015/1126/1448517087148.jpg";
	
	File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+"/yuan");
	File thedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+"/thedir");
	File dir = new File(thedir,"");
	File file = new File(path, FILE_NAME);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mbtCreatFile = (Button) findViewById(R.id.btCreatFile);
		mbtDelFile = (Button) findViewById(R.id.btDelFile);
		mbtIsFileExist = (Button) findViewById(R.id.btIsFileExist);
		mbtCreatDir = (Button) findViewById(R.id.btCreatDir);
		mbtDelDir = (Button) findViewById(R.id.btDelDir);
		mbtIsDirExist =(Button) findViewById(R.id.btIsDirExist);
		mbtDownFile = (Button) findViewById(R.id.btDownFile);
		
		mbtCreatFile.setOnClickListener(this) ;
		mbtDelFile.setOnClickListener(this) ;
		mbtIsFileExist.setOnClickListener(this);
		mbtCreatDir.setOnClickListener(this) ;
		mbtDelDir.setOnClickListener(this) ;
		mbtIsDirExist.setOnClickListener(this);
		mbtDownFile.setOnClickListener(this) ;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		
		case R.id.btCreatFile:
			tst  = Toast.makeText(this, "btCreatFile", Toast.LENGTH_SHORT);
			tst.show();
			
			if(!path.exists()){
				Log.d("niotong","path is not exist");
				try{
					Log.d("niotong","to creat DIR");
					path.mkdirs();
				}catch(Exception e){
					e.printStackTrace();
					Log.d("niotong","create dir fail");
				}
			}else{
				Log.d("niotong",path.getAbsolutePath());
			}
			
			if(!file.exists()){
				Log.d("niotong","file is not exist");
				try{
					Log.d("niotong","to creat file");
					file.createNewFile();
				}catch(Exception e){
					e.printStackTrace();
					Log.d("niotong","create file fail");
				}
			}else{
				Log.d("niotong",file.getAbsolutePath());
			}
			break;
			
			
		case R.id.btDelFile:
			tst  = Toast.makeText(this, "btDelFile", Toast.LENGTH_SHORT);
			tst.show();
			if(file.exists()){
				Log.d("niotong","file is exist,to del");
				try{
					file.delete();
				}catch(Exception e){
					e.printStackTrace();
					Log.d("niotong","del file fail");
				}
			}else{
				Log.d("niotong","file is not exist,uneed to del");
			}
			break;
			
			
		case R.id.btIsFileExist:
			tst  = Toast.makeText(this, "btIsFileExist", Toast.LENGTH_SHORT);
			tst.show();
			if(file.exists()){
				Log.d("niotong","file is exist");
//				try{
//					file.delete();
//				}catch(Exception e){
//					e.printStackTrace();
//					Log.d("niotong","del file fail");
//				}
			}else{
				Log.d("niotong","file is not exist");
			}
			break;
		case R.id.btCreatDir:
			tst  = Toast.makeText(this, "btCreatDir", Toast.LENGTH_SHORT);
			tst.show();
			if(!dir.exists()){
				Log.d("niotong","dir is not exist");
				try{
					Log.d("niotong","to creat DIR");
					dir.mkdirs();
				}catch(Exception e){
					e.printStackTrace();
					Log.d("niotong","create dir fail");
				}
			}else{
				Log.d("niotong",dir.getAbsolutePath());
			}
			break;
		case R.id.btDelDir:
			tst  = Toast.makeText(this, "btDelDir", Toast.LENGTH_SHORT);
			tst.show();
			if(dir.exists()){
				Log.d("niotong","dir is exist,to del");
				try{
					dir.delete();
				}catch(Exception e){
					e.printStackTrace();
					Log.d("niotong","del dir fail");
				}
			}else{
				Log.d("niotong","dir is not exist,uneed to del");
			}
			break;
		case R.id.btIsDirExist:
			tst  = Toast.makeText(this, "btIsDirExist", Toast.LENGTH_SHORT);
			tst.show();
			if(dir.exists()){
				Log.d("niotong","dir is exist");
			}else{
				Log.d("niotong","dir is not exist");
			}
			break;
		case R.id.btDownFile:
			tst  = Toast.makeText(this, "btDownFile", Toast.LENGTH_SHORT);
			tst.show();
			String serviceString = Context.DOWNLOAD_SERVICE;
			DownloadManager downloadManager;
			downloadManager = (DownloadManager) getSystemService(serviceString);
			 Uri uri = Uri.parse(FILE_URL);
			 DownloadManager.Request request = new Request(uri);
			 request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "liuyifei.jpg");
			 long reference = downloadManager.enqueue(request);
			 break;
		default:
			tst  = Toast.makeText(this, "default", Toast.LENGTH_SHORT);
			tst.show();
			break;
		}
	}
}
