/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.ococci;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.security.InvalidParameterException;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.ococci.SerialPort;
import com.ococci.aidl.OcocciInterface;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;

public class OcocciService extends Service {
	private static final int USBFUNC_DEFAULT_VAL = 0;
	private static final int USBFUNC_WRONG_PARM = 1;
	private static final int USBFUNC_NOT_RECIVED = 2;
	private static final int USBFUNC_NOT_SEND = 3;
	private static final int USBFUNC_RECIVED_ERR =  4;
	private static final int USBFUNC_FIRST_STM_UART1_ERR= 5;
	private static final int USBFUNC_FIRST_STM_UART2_ERR= 6;
	private static final int USBFUNC_SECOND_STM_UART1_ERR = 7;
	private static final int USBFUNC_SET_SUCCESS = 8;
	private static final int USBFUNC_SET_FAIL = 9;
	private static final int USBFUNC_USB_STATUS_ON = 10;
	private static final int 	USBFUNC_USB_STATUS_OFF = 11;
	private static final int 	USBFUNC_USB_LAST_ERR = 12;

	private static final String PC_SERIAL = "/dev/ttyS3";
	private static final String STM_SERIAL = "/dev/ttyS1";
	
	protected SerialPort mPCSerialPort;
	protected OutputStream mPCOutputStream;
	private InputStream mPCInputStream;
	
	protected SerialPort mSTMSerialPort;
	protected OutputStream mSTMOutputStream;
	private InputStream mSTMInputStream;
	private boolean stmStatus = false;	//if get USBID status ,this value is it
	private int stmVal = 0;
	
	private PCReadThread mPCReadThread;
	private STMReadThread mSTMReadThread;
	private Thread mSTMWriteThread;
	
	private EthernetManager mEthManager;
	private EthernetDevInfo mInterfaceInfo;
	private List<EthernetDevInfo> mListDevices = new ArrayList<EthernetDevInfo>();
	
	SharedPreferences myPreference; 
	
	private byte testSTM[] = {0x12,0x12,0x3,0x0D,0x0A};
	
	private static final String TAG = "niotongyuan_SerialPortService";
	
	private void LOG(String msg){
		Log.d(TAG,msg);
	}
	
	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				OcocciService.this.stopSelf();
			}
		});
		b.show();
	}
	
	public void onCreate(){
		LOG("Service onCreate");
		super.onCreate();
		myPreference = getSharedPreferences("ipset",Context.MODE_PRIVATE);
		IPInit();
	
		try {
			mPCSerialPort = new SerialPort(new File(PC_SERIAL), 115200, 0);
			mPCOutputStream = mPCSerialPort.getOutputStream();
			mPCInputStream = mPCSerialPort.getInputStream();
			/* Create a receiving thread */
			mPCReadThread = new PCReadThread();
			mPCReadThread.start();
			
			mSTMSerialPort = new SerialPort(new File(STM_SERIAL), 115200, 0);
			mSTMOutputStream = mSTMSerialPort.getOutputStream();
			mSTMInputStream = mSTMSerialPort.getInputStream();
			/* Create a receiving thread */
			mSTMReadThread = new STMReadThread();
			mSTMReadThread.start();
			
			
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError(R.string.error_configuration);
		}
	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
	
		LOG("Service onBind");
		return mBinder;
	}
	
	public void onStart(Intent intent,int startID){
		LOG("Service onStart" + startID);
	}
	
	public void onDestroy(){
		LOG("Service onDestroy");
		super.onDestroy();
		if (mPCReadThread != null)
			mPCReadThread.interrupt();
		if(mPCSerialPort != null)
			mPCSerialPort.serial_close();
		mPCSerialPort = null;
		
		if (mSTMReadThread != null)
			mSTMReadThread.interrupt();
		if(mSTMSerialPort != null)
			mSTMSerialPort.serial_close();
		mSTMSerialPort = null;
	}
	
	public boolean onUnbind (Intent intent){
		LOG("Service onUnbind");
		return super.onUnbind(intent);
	}
	
	public void onRebind(Intent intent){
		LOG("Service onRebind");
		super.onRebind(intent);
	}

	private final OcocciInterface.Stub mBinder = new OcocciInterface.Stub() {
		
		@Override
		public void setIPDHCP() throws RemoteException {
			// TODO Auto-generated method stub
			LOG("remote call from client! current thread id =  "+ Thread.currentThread());
		}

		@Override
		public double doCalculate(double a, double c) throws RemoteException {
			// TODO Auto-generated method stub
			LOG("remote call from client! current thread id =  "+ Thread.currentThread());
			return 0;
		}

		@Override
		public int usbFunc(int usbid, int type, boolean on) throws RemoteException {
			// TODO Auto-generated method stub
			mSTMWriteThread = Thread.currentThread();
			byte stmBuffer[] = new byte[10];
			stmStatus = false;
			stmVal = 0;
			//0x13 0x14 type usbid val retval   reserve2 reserve3 0x05 0x20
			if(usbid<1 || usbid>100)
				return USBFUNC_WRONG_PARM;
			if(type != 1 && type != 2)
				return USBFUNC_WRONG_PARM;
			
			stmBuffer[0] = 0x13;
			stmBuffer[1] = 0x14;
			stmBuffer[2] = (byte) type;	//type 0x1 is set ,0x2 is get
			stmBuffer[3] = (byte) usbid;
			stmBuffer[4] = (byte) (on?0x1:0x2); //0x1 is on 0x2 is off
			stmBuffer[5] = 0x0;
			stmBuffer[6] = 0x0;
			stmBuffer[7] = 0x0;			
			stmBuffer[8] = 0x05;
			stmBuffer[9] = 0x20;
			try {
				mSTMOutputStream.write(stmBuffer);
				Thread.sleep(5000);
			}catch (IOException e) {
				e.printStackTrace();
				return USBFUNC_NOT_SEND;
			}catch (InterruptedException e){
				e.printStackTrace();
				LOG("get response from stm!");
				return stmVal;
			}		
			return USBFUNC_NOT_RECIVED;
		}
	};

	private class PCReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mPCInputStream == null) return;
					size = mPCInputStream.read(buffer);
					if (size > 0) {
						onPCDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	protected void onPCDataReceived( final byte[] buffer, final int size) {
		try {
			if (parsePCCMD(buffer,size)) {
				if(myPreference.getString("Type", "getIP").equals("getIP")){
					//send Info of  IP  to PC
					setIPToSharedPerence();
					mPCOutputStream.write(new String(
					myPreference.getString("Mode", "dhcp")+";"+
					myPreference.getString("IP", "0.0.0.0")+";"+
					myPreference.getString("NetMask", "255.255.255.0")+";"+
					myPreference.getString("GateWay", "8.8.8.8")+";"+
					myPreference.getString("DnsAddr", "0.0.0.0")
							).getBytes());
					return;
				}
				if (setSharedPerenceToIP() == true) {
					mSTMOutputStream.write(testSTM);
					mPCOutputStream.write(new String("SUCCESS;SETIP")
							.getBytes());
				} else {
					mPCOutputStream.write(new String("FAIL;SETIP")
							.getBytes());
				}
			} else {
				// the cmd is not cmd
				mPCOutputStream.write(new String("FAIL;PARSEPCCMD")
						.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
	private class STMReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mSTMInputStream == null) return;
					size = mSTMInputStream.read(buffer);
					if (size > 0) {
						onSTMDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	protected  void onSTMDataReceived(final byte[] buffer, final int size){
	
		if(parseSTMCMD(buffer, size) == true){
			LOG("parseSTMCMD is true");
		}else{
			LOG("parseSTMCMD is false");
		}
		
	}
	
	
	
	@SuppressLint("CommitPrefEdits")
	private boolean parsePCCMD( final byte[] buffer, final int size){
		for (int i = 0;i < size;i++){
			LOG("----PC----"+buffer[i]+"------");
		}
		if(buffer[0] == 84 && buffer[1] == 89 && buffer[2]== 0){
			
		}else{
			return false;
		}
		Editor editor = myPreference.edit();
		if(buffer[3] ==24 && buffer[4]==80){
			//setip
			editor.putString("Type", "setIP");
			editor.commit();
			if(buffer[5]==0){
				//set dhcp
				editor.putString("Mode","dhcp");
				editor.commit();
			}else if(buffer[5]==1){
				//set manual ip
				editor.putString("Mode","manual");
				LOG("IP:"+bytesToIP(buffer[6],buffer[7],buffer[8],buffer[9]));
				editor.putString("IP",  bytesToIP(buffer[6],buffer[7],buffer[8],buffer[9]));
				editor.putString("NetMask", bytesToIP(buffer[10],buffer[11],buffer[12],buffer[13]));
				editor.putString("GateWay", bytesToIP(buffer[14],buffer[15],buffer[16],buffer[17]));
				editor.putString("DnsAddr", bytesToIP(buffer[18],buffer[19],buffer[20],buffer[21]));
				editor.commit();
			}else{
				return false;
			}
		}else if(buffer[3]==7 && buffer[4]==84){
			//get ip
			editor.putString("Type", "getIP");
			editor.commit();
		}else{
			return false;
		}
		return true;
	}
	
	@SuppressLint("CommitPrefEdits")
	private boolean parseSTMCMD( final byte[] buffer, final int size){
		for (int i = 0;i < size;i++){
			LOG("----STM----"+buffer[i]+"------");
		}
		if(buffer[0] == 0x13 && buffer[1] == 0x14 && buffer[8] == 0x05 && buffer[9]==0x20){
			
		}else{
			stmVal = USBFUNC_RECIVED_ERR;
			mSTMWriteThread.interrupt();
			return false;
		}
		stmVal = buffer[5];
		mSTMWriteThread.interrupt();//interrupt mSTMWriteThread
		return true;
	}
	
	
	private String bytesToIP(final byte a,final byte b,final byte c,final byte d){
		return String.format("%d.%d.%d.%d",a<0?a+256:a,b<0?b+256:b,c<0?c+256:c,d<0?d+256:d);
	}
	
	
	private void IPInit(){
		mEthManager = EthernetManager.getInstance();
		if(mEthManager.getState() == EthernetManager.ETHERNET_STATE_ENABLED){
			LOG("ethernet is enabled");
		}else{
			LOG("ethernet is disabled");
		}
		mListDevices = mEthManager.getDeviceNameList();
		if (mListDevices != null) {
			for (EthernetDevInfo deviceinfo : mListDevices) {
				if (!deviceinfo.getIfName().equals("eth0")) {
					LOG("one IfName = " + deviceinfo.getIfName());
				} else {
					mInterfaceInfo = deviceinfo;
					LOG("sec IfName = " + deviceinfo.getIfName());
				}
				try {
					//mInterfaceInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
					mEthManager.updateDevInfo(mInterfaceInfo);
					mEthManager.setEnabled(true);
					Thread.sleep(500);
				} catch (Exception e) {
					LOG("set ethernet enable fail");
				}
			}
		} else {
			LOG("there is no ethernet devices");
		}
		setSharedPerenceToIP();
	}
	
	@SuppressLint("CommitPrefEdits")
	private boolean setIPToSharedPerence(){
		LOG("----setIPToSharedPerence----");
		if(mInterfaceInfo == null){
			LOG("setIPToSharedPerence() mInterfaceInfo is null");
			return false;
		}
		Editor editor = myPreference.edit();
		editor.putString("Mode", mInterfaceInfo.getConnectMode()==EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL?"manual":"dhcp");
		editor.putString("IfName", mInterfaceInfo.getIfName());
		editor.putString("IP", mInterfaceInfo.getIpAddress());
		editor.putString("NetMask",mInterfaceInfo.getNetMask());
		editor.putString("GateWay", mInterfaceInfo.getGateWay());
		editor.putString("DnsAddr", mInterfaceInfo.getDnsAddr());
		editor.putString("Hwaddr", mInterfaceInfo.getHwaddr());
		editor.commit();
		return true;
	}
	
	@SuppressLint("CommitPrefEdits")
	private boolean setSharedPerenceToIP(){
		LOG("----setSharedPerenceToIP------");
		if(mInterfaceInfo == null){
			LOG("setSharedPerenceToIP() mInterfaceInfo is null");
			return false;
		}
		if(myPreference.contains("Mode") == false){
			Editor editor = myPreference.edit();
			editor.putString("Mode", "dhcp");
			editor.commit();
		}
		mInterfaceInfo.setConnectMode(myPreference.getString("Mode","dhcp").equals("manual")?EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL:EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
		
		if (myPreference.getString("Mode","dhcp").equals("manual")) {
			mInterfaceInfo.setIpAddress(myPreference.getString("IP","0.0.0.0"));
			mInterfaceInfo.setNetMask(myPreference.getString("NetMask","255.255.255.0"));
			mInterfaceInfo.setDnsAddr(myPreference.getString("DnsAddr","8.8.8.8"));
			mInterfaceInfo.setGateWay(myPreference.getString("GateWay","0.0.0.0"));
		}
		try{
			mEthManager.updateDevInfo(mInterfaceInfo);
			mEthManager.setEnabled(true);
			Thread.sleep(500);
		}catch(Exception e){
			LOG("setSharedPerenceToIP() set the saved ethernet enable fail");
			return false;
		}
		return true;
	}
	
}
