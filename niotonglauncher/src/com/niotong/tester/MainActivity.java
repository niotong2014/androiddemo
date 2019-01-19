package com.niotong.tester;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
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

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadApps();
        
        GridView gridView = (GridView) findViewById(R.id.apps_list);
        //设置默认适配器。
        mContent = getApplicationContext();
        mResources = getResources();
        gridView.setAdapter(new AppsAdapter());

        gridView.setOnItemClickListener(clickListener);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private List<ResolveInfo> apps;
    private Resources mResources;
    private Context mContent;
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

    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        new ImageView(MainActivity.this);

        apps = getPackageManager().queryIntentActivities(mainIntent, 0);
        Log.d("niotongyuan","apps.size() = "+apps.size());
        if(apps != null){
        	for (ResolveInfo resolveInfo : apps) {
        		Log.d("niotongyuan",resolveInfo.toString());
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
}
