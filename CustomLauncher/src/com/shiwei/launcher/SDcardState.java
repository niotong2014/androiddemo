package com.shiwei.launcher;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SDcardState extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        String action=intent.getAction();
      //判断收到的是哪条广播
      if(action.equals("android.intent.action.MEDIA_MOUNTED")){
    	  //Launcher.setSdcardStatus(true);
    	  File file = new File("/storage/extsd/apps.bat");
    	  Intent mintent = new Intent(context,Launcher.class);
    	  //mintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	  //mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	  mintent.putExtra("sdBroadCast",true);
    	  if(file.exists()){
    		  mintent.putExtra("sdcardState",true);
    		  context.startActivity(mintent);
    		  //Toast.makeText(context,"SD卡可用",Toast.LENGTH_SHORT).show();
    	  }
      }else if(action.equals("android.intent.action.MEDIA_EJECT")){
    	  //Launcher.setSdcardStatus(false);
    	  File file = new File("/storage/extsd/apps.bat");
    	  Intent mintent = new Intent(context,Launcher.class);
    	  //mintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	  //mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	  mintent.putExtra("sdBroadCast",true);
    	  mintent.putExtra("sdcardState",false);
    	  context.startActivity(mintent);
          //Toast.makeText(context,"SD卡不可用",Toast.LENGTH_SHORT).show();
      }
	}
}
