package com.ococci;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ococci.R;
import com.ococci.SerialPort;

public class MainActivity extends Activity implements OnClickListener{

	private final String TAG = "niotong RFID DEMO";
	private EditText sendtext ;
	private TextView recvtext ;
	
	private Button setLowPowerBT;
	private Button ctlAntFoundCardBT;
	private Button setAutoScanIDBT;
	private Button foundCardBT;
	private Button readBlockBT;
	private Button writeBlockBT;
	private Button initWalletBT;
	private Button readWalletBT;
	private Button rechargeBT;
	private Button payBT;
	private Button backupWalletBT;
	
	private final static int SET_LOW_POWER = 0;
	private final static int CTL_ANT_FOUND_CARD = 1;
	private final static int SET_AUTO_SCAN_ID = 2;
	private final static int FOUND_CARD = 3;
	private final static int READ_BLOCK = 4;
	private final static int WRITE_BLOCK = 5;
	private final static int INIT_WALLET = 6;
	private final static int READ_WALLET = 7;
	private final static int RECHARGE = 8;
	private final static int PAY = 9;
	private final static int BACKUP_WALLET = 10;
	
	private static final String RF_SERIAL = "/dev/ttyS2";
	
	protected SerialPort mRFSerialPort;
	protected OutputStream mRFOutputStream;
	protected InputStream mRFInputStream;
	
	private RFReadThread mRFReadThread;
	private Thread mRFWriteThread;
	private String result;
	
	private static  boolean DEBUG = true;
	
	private void LOG(String msg){
		if(DEBUG)
			Log.d(TAG,msg);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {			
			mRFSerialPort = new SerialPort(new File(RF_SERIAL), 9600, 0);
			mRFOutputStream = mRFSerialPort.getOutputStream();
			mRFInputStream = mRFSerialPort.getInputStream();
			/* Create a receiving thread */
			mRFReadThread = new RFReadThread();
			mRFReadThread.start();		
			
		} catch (SecurityException e) {
			LOG(getString(R.string.error_security));
		} catch (IOException e) {
			LOG(getString(R.string.error_unknown));
		} catch (InvalidParameterException e) {
			LOG(getString(R.string.error_configuration));
		}
        sendtext = (EditText) findViewById(R.id.sendText);
        recvtext = (TextView) findViewById(R.id.recvText);
        
        setLowPowerBT = (Button) findViewById(R.id.setLowPower);
        ctlAntFoundCardBT = (Button) findViewById(R.id.ctlAntFoundCard);
        setAutoScanIDBT = (Button) findViewById(R.id.setAutoScanID);
        foundCardBT = (Button) findViewById(R.id.foundCard);
        readBlockBT = (Button) findViewById(R.id.readBlock);
        writeBlockBT = (Button) findViewById(R.id.writeBlock);
        initWalletBT = (Button) findViewById(R.id.initWallet);
        readWalletBT = (Button) findViewById(R.id.readWallet);
        rechargeBT = (Button) findViewById(R.id.recharge);
        payBT = (Button) findViewById(R.id.pay);
        backupWalletBT = (Button) findViewById(R.id.backupWallet);
        
        setLowPowerBT.setOnClickListener(this);
        ctlAntFoundCardBT.setOnClickListener(this);
        setAutoScanIDBT.setOnClickListener(this);
        foundCardBT.setOnClickListener(this);
        readBlockBT.setOnClickListener(this);
        writeBlockBT.setOnClickListener(this);
        initWalletBT.setOnClickListener(this);
        readWalletBT.setOnClickListener(this);
        rechargeBT.setOnClickListener(this);
        payBT.setOnClickListener(this);
        backupWalletBT.setOnClickListener(this);
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		case R.id.setLowPower:
			rfidFunc(SET_LOW_POWER);
			break;
		case R.id.ctlAntFoundCard:
			rfidFunc(CTL_ANT_FOUND_CARD);
			break;
		case R.id.setAutoScanID:
			rfidFunc(SET_AUTO_SCAN_ID);
			break;
		case R.id.foundCard:
			rfidFunc(FOUND_CARD);
			break;
		case R.id.readBlock:
			rfidFunc(READ_BLOCK);
			break;
		case R.id.writeBlock:
			rfidFunc(WRITE_BLOCK);
			break;
		case R.id.initWallet:
			rfidFunc(INIT_WALLET);
			break;
		case R.id.readWallet:
			rfidFunc(READ_WALLET);
			break;
		case R.id.recharge:
			rfidFunc(RECHARGE);
			break;
		case R.id.pay:
			rfidFunc(PAY);
			break;
		case R.id.backupWallet:
			rfidFunc(BACKUP_WALLET);
			break;

		default:
			break;
		}
	}
	private void checkTextValue() {
		// TODO Auto-generated method stub
		String sendString;
		String[] tempString;
		byte[] finalbyte = new byte[29];
		int i=0;
		int j=0;
		int size=0;
		byte xorResult = 0x00;
		//get byte[] from EditText
		sendString = sendtext.getText().toString();
		//Log.d(TAG,"sendtext "+ sendString);
		tempString = sendString.split(" ");
		//Log.d(TAG,"tempString.length = "+tempString.length);
		for (i= 0;i<tempString.length;i++){
			if(!tempString[i].equalsIgnoreCase("")){
				//Log.d(TAG,"tempString["+i+"]="+tempString[i]);
				finalbyte[j++] = (byte)(Integer.parseInt(tempString[i],16));
				Log.d(TAG,"finalbyte["+i+"]="+finalbyte[i]);
			}
		}
		if(!( (finalbyte[0]==0) && (finalbyte[1]==0) ))
			return;

		size = (int)finalbyte[2];
		for(i=0;i < size;i++){
			xorResult ^= finalbyte[i+2];
		}
		//Log.d(TAG,"xorResult = "+xorResult+"   finalbyte["+(size+2)+"]="+finalbyte[size+2]);
		if(xorResult != finalbyte[size+2])
			return;
	}

	private void rfidFunc(int option) {
		// TODO Auto-generated method stub
		Log.d(TAG,"rfidFunc()   option = "+option);
		int i = 0;
		byte xorResult = 0x00;
		byte[] finalbyte = new byte[29];
		finalbyte[0] = 0x00;
		finalbyte[1] = 0x00;
		mRFWriteThread =Thread.currentThread();
		switch(option){
		case SET_LOW_POWER:
			finalbyte[2] = 0x02;
			finalbyte[3] = 0x01;
			break;
		case CTL_ANT_FOUND_CARD:
			finalbyte[2] = 0x03;
			finalbyte[3] = 0x02;
			finalbyte[4] = 0x03;//mask
			break;
		case SET_AUTO_SCAN_ID:
			finalbyte[2] = 0x02;
			finalbyte[3] = 0x0C;
			break;
		case FOUND_CARD:
			finalbyte[2] = 0x03;
			finalbyte[3] = 0x03;
			finalbyte[4] = 0x00;//mask
			break;
		case READ_BLOCK:
			finalbyte[2] = 0x0A;
			finalbyte[3] = 0x04;
			finalbyte[4] = 0x00;//mask
			finalbyte[5] = 0x01;//mask
			finalbyte[6] = (byte)0xff;//mask
			finalbyte[7] = (byte)0xff;//mask
			finalbyte[8] = (byte)0xff;//mask
			finalbyte[9] = (byte)0xff;//mask
			finalbyte[10] = (byte)0xff;//mask
			finalbyte[11] = (byte)0xff;//mask
			break;
		case WRITE_BLOCK:
			finalbyte[2] = 0x1A;
			finalbyte[3] = 0x05;
			finalbyte[4] = 0x00;//mask
			finalbyte[5] = 0x01;//mask
			finalbyte[6] = (byte)0xff;//mask
			finalbyte[7] = (byte)0xff;//mask
			finalbyte[8] = (byte)0xff;//mask
			finalbyte[9] = (byte)0xff;//mask
			finalbyte[10] = (byte)0xff;//mask
			finalbyte[11] = (byte)0xff;//mask
			
			finalbyte[12] = (byte)0x00;//mask
			finalbyte[13] = (byte)0x11;//mask
			finalbyte[14] = (byte)0x22;//mask
			finalbyte[15] = (byte)0x33;//mask
			finalbyte[16] = (byte)0x44;//mask
			finalbyte[17] = (byte)0x55;//mask
			finalbyte[18] = (byte)0x66;//mask
			finalbyte[19] = (byte)0x77;//mask
			finalbyte[20] = (byte)0x88;//mask
			finalbyte[21] = (byte)0x99;//mask
			finalbyte[22] = (byte)0xaa;//mask
			finalbyte[23] = (byte)0xbb;//mask
			finalbyte[24] = (byte)0xcc;//mask
			finalbyte[25] = (byte)0xdd;//mask
			finalbyte[26] = (byte)0xee;//mask
			finalbyte[27] = (byte)0xff;//mask
			break;
		case INIT_WALLET:
			finalbyte[2] = 0x0E;
			finalbyte[3] = 0x06;
			finalbyte[4] = 0x00;//mask
			finalbyte[5] = 0x05;//mask
			finalbyte[6] = (byte)0xff;//mask
			finalbyte[7] = (byte)0xff;//mask
			finalbyte[8] = (byte)0xff;//mask
			finalbyte[9] = (byte)0xff;//mask
			finalbyte[10] = (byte)0xff;//mask
			finalbyte[11] = (byte)0xff;//mask
			
			finalbyte[12] = (byte)0x00;//mask
			finalbyte[13] = (byte)0x00;//mask
			finalbyte[14] = (byte)0x00;//mask
			finalbyte[15] = (byte)0x00;//mask
			break;
		case READ_WALLET:
			finalbyte[2] = 0x0A;
			finalbyte[3] = 0x07;
			finalbyte[4] = 0x00;//mask
			finalbyte[5] = 0x05;//mask
			finalbyte[6] = (byte)0xff;//mask
			finalbyte[7] = (byte)0xff;//mask
			finalbyte[8] = (byte)0xff;//mask
			finalbyte[9] = (byte)0xff;//mask
			finalbyte[10] = (byte)0xff;//mask
			finalbyte[11] = (byte)0xff;//mask
			break;
		case RECHARGE:
			finalbyte[2] = 0x0E;
			finalbyte[3] = 0x08;
			finalbyte[4] = 0x00;//mask
			finalbyte[5] = 0x05;//mask
			finalbyte[6] = (byte)0xff;//mask
			finalbyte[7] = (byte)0xff;//mask
			finalbyte[8] = (byte)0xff;//mask
			finalbyte[9] = (byte)0xff;//mask
			finalbyte[10] = (byte)0xff;//mask
			finalbyte[11] = (byte)0xff;//mask
			
			finalbyte[12] = (byte)0x02;//mask
			finalbyte[13] = (byte)0x00;//mask
			finalbyte[14] = (byte)0x00;//mask
			finalbyte[15] = (byte)0x00;//mask
			break;
		case PAY:
			finalbyte[2] = 0x0E;
			finalbyte[3] = 0x09;
			finalbyte[4] = 0x00;//mask
			finalbyte[5] = 0x05;//mask
			finalbyte[6] = (byte)0xff;//mask
			finalbyte[7] = (byte)0xff;//mask
			finalbyte[8] = (byte)0xff;//mask
			finalbyte[9] = (byte)0xff;//mask
			finalbyte[10] = (byte)0xff;//mask
			finalbyte[11] = (byte)0xff;//mask
			
			finalbyte[12] = (byte)0x01;//mask
			finalbyte[13] = (byte)0x00;//mask
			finalbyte[14] = (byte)0x00;//mask
			finalbyte[15] = (byte)0x00;//mask
			break;
		case BACKUP_WALLET:
			finalbyte[2] = 0x0B;
			finalbyte[3] = 0x0A;
			finalbyte[4] = 0x00;//mask
			finalbyte[5] = 0x05;//mask
			finalbyte[6] = 0x06;//mask
			finalbyte[7] = (byte)0xff;//mask
			finalbyte[8] = (byte)0xff;//mask
			finalbyte[9] = (byte)0xff;//mask
			finalbyte[10] = (byte)0xff;//mask
			finalbyte[11] = (byte)0xff;//mask
			finalbyte[12] = (byte)0xff;//mask
			break;
		default:
			break;
		}
		getDataFromText(finalbyte);
		for(i=0;i<finalbyte[2];i++){
			xorResult ^= finalbyte[2+i];
		}
		finalbyte[finalbyte[2]+2] = xorResult;
		byte rfBuffer[]  = new byte[finalbyte[2]+3];
		for(i=0;i<finalbyte[2]+3;i++){
			Log.d(TAG,"finalbyte["+i+"] = "+finalbyte[i]);
			rfBuffer[i] = finalbyte[i];
		}
		//Log.d(TAG,"xorResult ="+xorResult);
		try {
			mRFOutputStream.write(rfBuffer);
			Thread.sleep(1000);
		}catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e){
			//e.printStackTrace();
			//LOG("get response from rf!");
			recvtext.setText(result);
		}		
	}
	
	private void getDataFromText(byte[] finalbyte) {
		// TODO Auto-generated method stub
		String sendString;
		String[] tempString;
		int i=0;
		int j=0;
		int size=0;
		byte xorResult = 0x00;
		//get byte[] from EditText
		sendString = sendtext.getText().toString();
		//Log.d(TAG,"sendtext "+ sendString);
		tempString = sendString.split(" ");
		//Log.d(TAG,"tempString.length = "+tempString.length);
		if(finalbyte[2]<=2){
			LOG("don't need any data");
			return;
		}
		if(tempString.length < finalbyte[2]-2){
			LOG("text data is error");
			return;
		}
		for (i= 0,j=0;i<tempString.length;i++){
			if(!tempString[i].equalsIgnoreCase("")){
				//Log.d(TAG,"tempString["+i+"]="+tempString[i]);
				finalbyte[4+j++] = (byte)(Integer.parseInt(tempString[i],16));
				Log.d(TAG,"finalbyte["+i+"]="+finalbyte[i]);
			}
		}
		return;
	}

	private class RFReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mRFInputStream == null) return;
					size = mRFInputStream.read(buffer);
					if (size > 0) {
						onRFDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	protected  void onRFDataReceived(final byte[] buffer, final int size){
		
		if(parseRFCMD(buffer, size) == true){
			LOG("parseRFCMD is true");
		}else{
			LOG("parseRFCMD is false");
		}
		
	}
	@SuppressLint("CommitPrefEdits")
	private boolean parseRFCMD( final byte[] buffer, final int size){
		if(size < 4){
			return false;
		}
		if(buffer[0]!=(byte)0xFE){
			return false;
		}
		if((buffer[1]+2)!=(byte)size){
			return false;
		}
		byte xorResult = 0x00;
		for(int i =0;i<buffer[1];i++){
			xorResult ^= buffer[1+i];
		}
		if(xorResult != buffer[buffer[1]+1]){
			return false;
		}
		result = new String();
		for(int i=0;i<size;i++){
			result += Integer.toHexString(buffer[i]&0xff);
			result += " ";
		}
		mRFWriteThread.interrupt();
		return true;
	}

}
