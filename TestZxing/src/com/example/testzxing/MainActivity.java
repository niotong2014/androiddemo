package com.example.testzxing;


import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class MainActivity extends Activity {
	String macAddress = null;
	int defBright = 0;
	private static  int setBright = 255;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
       defBright = getScreenBrightness();
       saveScreenBrightness(255);
       setScreenBrightness(255);
        
        
        macAddress = getWifiaddr();
        try{
        	macAddress = macAddress.replaceAll(":", "");
        }catch(NullPointerException e){
        	e.printStackTrace();
        	Toast.makeText(getApplicationContext(), "macAddress is error", Toast.LENGTH_LONG).show();
        	return;
        }
		Bitmap bitmap = null;
		ImageView iv = new ImageView(this);
		bitmap = BarcodeCreater.creatBarcode(getApplicationContext(),
				macAddress, 600, 300, true);
		iv.setImageBitmap(bitmap);
		iv.setScaleType(ScaleType.FIT_CENTER);
		setContentView(iv, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
            
    }  
    private String getWifiaddr() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String macAddressRet = wifiInfo == null ? null : wifiInfo.getMacAddress();
        return macAddressRet;
    }
    /** 
     * 获得当前屏幕亮度的模式     
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度 
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0  为手动调节屏幕亮度 
     */  
      private int getScreenMode(){  
        int screenMode=0;  
        try{  
            screenMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);  
        }  
        catch (Exception localException){  
              
        }  
        return screenMode;  
      }  
        
     /** 
     * 获得当前屏幕亮度值  0--255 
     */  
      private int getScreenBrightness(){  
        int screenBrightness=255;  
        try{  
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);  
        }  
        catch (Exception localException){  
            
        }  
        return screenBrightness;  
      }  

      private void setScreenMode(int paramInt){  
        try{  
          Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);  
        }catch (Exception localException){  
          localException.printStackTrace();  
        }  
      }  
      /** 
       * 设置当前屏幕亮度值  0--255 
       */  
      private void saveScreenBrightness(int paramInt){  
        try{  
          Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);  
        }  
        catch (Exception localException){  
          localException.printStackTrace();  
        }  
      }  
      /** 
       * 保存当前的屏幕亮度值，并使之生效 
       */  
      private void setScreenBrightness(int paramInt){  
        Window localWindow = getWindow();  
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();  
        float f = paramInt / 255.0F;  
        localLayoutParams.screenBrightness = f;  
        localWindow.setAttributes(localLayoutParams);  
      }  
}
