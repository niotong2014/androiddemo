package com.niotong.aidldemoclient;

import com.niotong.aidl.CalculateInterface;
import com.niotong.aidldemoclient.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements android.view.View.OnClickListener{

	Button bindBt;
	Button unbindBt;
	Button calBt;
	EditText numEt1;
	EditText numEt2;
	TextView resultTv;
	CalculateInterface mService;
	
	private static final String TAG = "niotongyuan_CalculateClient";
	private void LOG(String msg){
		Log.d(TAG,msg);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bindBt = (Button) findViewById(R.id.bindBt);
        unbindBt = (Button) findViewById(R.id.unbindBt);
        calBt = (Button) findViewById(R.id.calculateBt);
        numEt1 = (EditText) findViewById(R.id.editText1);
        numEt2 = (EditText)  findViewById(R.id.editText2);
        resultTv = (TextView) findViewById(R.id.result);
        bindBt.setOnClickListener(this);
        unbindBt.setOnClickListener(this);
        calBt.setOnClickListener(this);
	
    }
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.bindBt:
			Bundle args = new Bundle();
			Intent intent = new Intent("com.niotong.aidldemoserver.CalculateService");
			intent.putExtras(args);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			LOG("click bindBt");
			break;
		case R.id.unbindBt:
			LOG("click unbindBt");
			unbindService(mConnection);
			break;
		case R.id.calculateBt:
			LOG("click calculateBt");
			double a = Double.valueOf( numEt1.getText().toString());
			double b = Double.valueOf( numEt2.getText().toString());
			resultTv.setText("结果："+getResult(a,b));
			break;

		default:
			break;
		}
	}
	private double getResult(double a, double b) {
		// TODO Auto-generated method stub
		
		double result = 0;
		try {
			result = mService.doCalculate(a, b);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			LOG("onServiceDisconnected");
			mService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			LOG("onServiceConnected");
			mService = CalculateInterface.Stub.asInterface(arg1);
		}
	};
}
