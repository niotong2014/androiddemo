package com.shiwei.launcher;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class AllApp extends Activity {
	public final String TAG = "niotongyuan AllApp";
	
	AllappApkStatusReceiver mAllappApkStatusReceiver = null;
	
    private List<ResolveInfo> apps;
    private Resources mResources;
    private Context mContent;
    GridView gridView;
    private AppsAdapter  mAppsAdapter = new AppsAdapter();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allapp_main);
        loadApps();
        
        gridView = (GridView) findViewById(R.id.apps_list);
        //设置默认适配器。
        mContent = getApplicationContext();
        mResources = getResources();
        
        gridView.setAdapter(mAppsAdapter);
        gridView.setOnItemClickListener(clickListener);
        gridView.setOnItemLongClickListener(longClickListener);

    }
    @Override
    protected void onPause(){
    	//Log.d(TAG,"onStart() in");
    	this.unregisterReceiver(mAllappApkStatusReceiver);
    	super.onPause();
    }
    @Override
    protected void onResume(){
    	//Log.d(TAG,"onResume() in");
    	refreshGridView();
		mAllappApkStatusReceiver = new AllappApkStatusReceiver();		
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        this.registerReceiver(mAllappApkStatusReceiver, filter);
    	super.onResume();   
    	
    }
    
	private class AllappApkStatusReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub        
	        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {   
	        	Log.d(TAG,"有apk安装");
	        	refreshGridView();
	        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {  
	        	Log.d(TAG,"有apk卸载");
	        	refreshGridView();
	        }
		}  

	}
	private void refreshGridView(){
		loadApps();
    	mAppsAdapter.notifyDataSetChanged();
	}
	
    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        new ImageView(AllApp.this);

        apps = getPackageManager().queryIntentActivities(mainIntent, 0);
        Log.d(TAG,"apps.size() = "+apps.size());
        if(apps != null){
        	for (ResolveInfo resolveInfo : apps) {
        		Log.d(TAG,resolveInfo.toString());
        	}
        }
    }

	public class AppsAdapter extends BaseAdapter {

		public AppsAdapter(){
			
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return apps.size();
		}

		@Override
		public Object getItem(int i) {
			// TODO Auto-generated method stub
			return apps.get(i);
		}

		@Override
		public long getItemId(int i) {
			// TODO Auto-generated method stub
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGrop) {
			// TODO Auto-generated method stub
            ResolveInfo info = apps.get(i);

            View convertView = LayoutInflater.from(mContent).inflate(R.layout.text_img_view, null);
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            TextView text = (TextView) convertView.findViewById(R.id.text);
            //设置文字和图片。
            text.setText(info.loadLabel(getPackageManager()));

            image.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));

            // convertView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            //使用dp进行参数设置。进行分辨率适配。
            convertView.setLayoutParams(new GridView.LayoutParams(
                    (int) mResources.getDimension(R.dimen.app_width),
                    (int) mResources.getDimension(R.dimen.app_height)));
            //返回一个图文混合。
            return convertView;
		}

	}
	
    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ResolveInfo info = apps.get(i);
            //该应用的包名
            String pkg = info.activityInfo.packageName;
            //应用的主activity类
            String cls = info.activityInfo.name;
            ComponentName componet = new ComponentName(pkg, cls);
            Intent intent = new Intent();
            intent.setComponent(componet);
            startActivity(intent);
        }
    };
    
    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
			// TODO Auto-generated method stub
            ResolveInfo info = apps.get(i);
            //该应用的包名
            String pkg = info.activityInfo.packageName;
            Uri uri = Uri.parse("package:" + pkg);      
    		Intent intent = new Intent(Intent.ACTION_DELETE, uri);      
    		mContent.startActivity(intent);
			return true;//如果返回true的话不会触发OnItemClickListener,返回false的话依旧会触发OnItemClickListener
		}
    };
}
