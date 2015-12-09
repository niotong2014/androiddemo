package com.niotong.jnitest;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.niotong.jnitest.getJniString;

public class JniActivity extends Activity implements OnClickListener {
	private Button btGet ;
	private Button btSet;
	private TextView textGet;
	private TextView textSet;
	
	String stringGet;
	String stringSet;
	static{
		System.loadLibrary("GetStingJni");
		System.loadLibrary("SetStingJni");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jni);
		
		btGet = (Button) findViewById(R.id.btGet);
		btSet = (Button) findViewById(R.id.btSet);
		textGet = (TextView) findViewById(R.id.textGet);
		textSet = (TextView) findViewById(R.id.textSet);
		
		btGet.setOnClickListener(this);
		btSet.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
			case R.id.btGet:
				stringGet = getJniString.getScpp();
				Log.d("niotong",stringGet);
				break;
			case R.id.btSet:
				stringGet = getJniString.getSj();
				Log.d("niotong",stringGet);
				break;
			default:
				break;
		}
	}

}
