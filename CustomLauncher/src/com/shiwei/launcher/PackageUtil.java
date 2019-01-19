package com.shiwei.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import android.app.AppGlobals;
import android.content.pm.IPackageManager;


public class PackageUtil {
	public final String TAG = "niotongyuan PackageUtil";
	
//a64
	public String mSDPath = "/storage/extsd/";
	public String mExeDir = "/storage/extsd/exefile/";
//a40i
	//public String mSDPath = "/storage/card/";
	//public String mExeDir = "/storage/card/exefile/";
	
	//InstallHandler的一些消息类型
	public final static int INSTALL = 0;
	public final static int INSTALL_SUCCESS = 1;
	public final static int INSTALL_FAILED = 2;
	public final static int ALL_INSTALLED = 3;
	
	//UnInstallHandler的一些消息类型
	public final static int UNINSTALL = 0;
	public final static int UNINSTALL_SUCCESS = 1;
	public final static int UNINSTALL_FAILED = 2;
	public final static int ALL_UNINSTALLED = 3;
	
	List<String> cmdList = new ArrayList<String>();
	List<String> installList = new ArrayList<String>();
	List<String> uninstallList = new ArrayList<String>();
			
	public String mOpenApkPackageName = null;		//指示最终要打开的应用的包名
	public String mInstallApkPackageName = null;	//安装过程中，该值为具体安装的apk的包名，和broadcast配合确定是否成功安装
	public String mUninstallApkPackageName = null;	//卸载过程中，该值为具体卸载的apk的包名，和broadcast配合确定是否成功卸载
	
    private final IPackageManager mIPackageManager;
    private final Context mContext;
    private Handler launcherHandler;	//最终指向Launcher.java中的mPackageHandler,在这个class中用来通知apk安装卸载的情况
    
    Handler mInstallHandler = new InstallHandler();	//用来控制安装的流程
    Handler mUnInstallHandler = new UnInstallHandler();	//用来控制卸载的流程
    
    ApkReceiver mApkReceiver = null;	//接收系统发送的apk安装和卸载的广播

	PackageUtil(Context context,Handler handler) {
		this(context, AppGlobals.getPackageManager(),handler); 
    }

    PackageUtil(Context context, IPackageManager ipackageManager,Handler handler) {
        mContext = context;
        launcherHandler = handler;
        mIPackageManager = ipackageManager;
        
		mApkReceiver = new ApkReceiver();		
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        context.registerReceiver(mApkReceiver, filter);
    }
    
	public class InstallHandler extends Handler{
		boolean retValue = false;
		public InstallHandler(){
			super();
		}
	    public InstallHandler(Looper looper) {
	            super(looper);
	    }
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			Message nextMsg = new Message();  
			switch(msg.what){
				case INSTALL:
					Log.d(TAG,"InstallHandler INSTALL");
					if(installList.isEmpty()){
						nextMsg.what = ALL_INSTALLED;
						mInstallHandler.sendMessage(nextMsg);
					}else{
						mInstallApkPackageName = installList.get(0).trim();
						Log.d(TAG,"mInstallApkPackageName = "+mInstallApkPackageName);
						if(!installAction(mInstallApkPackageName)){
							removeApkFile(mInstallApkPackageName);//安装失败，删除apk文件（安装成功会在广播处理流程中删掉apk文件）
							mInstallApkPackageName = null;
							nextMsg.what = INSTALL_FAILED;
							mInstallHandler.sendMessage(nextMsg);
						}
						installList.remove(0);
					}
					break;
				case INSTALL_SUCCESS:
					Log.d(TAG,"InstallHandler INSTALL_SUCCESS");
					//安装成功继续安装
					nextMsg.what = INSTALL;
					mInstallHandler.sendMessage(nextMsg);
					break;
				case INSTALL_FAILED:
					Log.d(TAG,"InstallHandler INSTALL_FAILED");
					//安装失败继续安装
					nextMsg.what = INSTALL;
					mInstallHandler.sendMessage(nextMsg);
					break;
				case ALL_INSTALLED:
					Log.d(TAG,"InstallHandler ALL_INSTALLED");
					nextMsg.what = Launcher.INSTALL_APK_DONE;
					launcherHandler.sendMessage(nextMsg);
					break;
			}
		}
	}
	
	public class UnInstallHandler extends Handler{
		boolean retValue = false;
		public UnInstallHandler(){
			super();
		}
	    public UnInstallHandler(Looper looper) {
	            super(looper);
	    }
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			Message nextMsg = new Message();  
			switch(msg.what){
				case UNINSTALL:
					Log.d(TAG,"UnInstallHandler UNINSTALL");
					if(uninstallList.isEmpty()){
						nextMsg.what = ALL_UNINSTALLED;
						mUnInstallHandler.sendMessage(nextMsg);
					}else{

						mUninstallApkPackageName = uninstallList.get(0).trim();
						Log.d(TAG,"mUninstallApkPackageName = "+mUninstallApkPackageName);
						if(!uninstallAction(mUninstallApkPackageName)){
							mUninstallApkPackageName = null;
							nextMsg.what = UNINSTALL_FAILED;
							mUnInstallHandler.sendMessage(nextMsg);
						}
						uninstallList.remove(0);
					}
					break;
				case UNINSTALL_SUCCESS:
					Log.d(TAG,"UnInstallHandler UNINSTALL_SUCCESS");
					nextMsg.what = UNINSTALL;
					mUnInstallHandler.sendMessage(nextMsg);
					break;
				case UNINSTALL_FAILED:
					Log.d(TAG,"UnInstallHandler UNINSTALL_FAILED");
					nextMsg.what = UNINSTALL;
					mUnInstallHandler.sendMessage(nextMsg);
					break;
				case ALL_UNINSTALLED:
					Log.d(TAG,"UnInstallHandler ALL_UNINSTALLED");
					nextMsg.what = Launcher.UNINSTALL_APK_DONE;   //标志消息的标志
					launcherHandler.sendMessage(nextMsg);
					break;
			}
		}
	}
	
	public class ApkReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
	        PackageManager pm = context.getPackageManager();  
	          
	        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {  
	            String packageName = intent.getData().getSchemeSpecificPart();  
	            Log.d(TAG, "安装成功" + packageName);
	            if(mInstallApkPackageName!=null && mInstallApkPackageName.substring(0, mInstallApkPackageName.length()-4).equalsIgnoreCase(packageName)){
	            	Message msg = new Message(); 
	            	msg.what = INSTALL_SUCCESS;
					mInstallHandler.sendMessage(msg);
	            }
	            //安装成功卸载对应的apk
	            if(mInstallApkPackageName != null){
	            	removeApkFile(mInstallApkPackageName);
	            }
				
	            mInstallApkPackageName = null;
	        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)) {  
	            String packageName = intent.getData().getSchemeSpecificPart();  
	            Log.d(TAG, "替换成功" + packageName);
	        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {  
	            String packageName = intent.getData().getSchemeSpecificPart();  
	            Log.d(TAG, "卸载成功" + packageName);
	            if(mUninstallApkPackageName!=null && mUninstallApkPackageName.substring(0, mUninstallApkPackageName.length()-4).equalsIgnoreCase(packageName)){
	            	Message msg = new Message(); 
	            	msg.what = UNINSTALL_SUCCESS;
					mUnInstallHandler.sendMessage(msg);
	            }
	            mUninstallApkPackageName = null;
	        }
		}  

	}
	
    /**
     * 采用系统的接口静默安装
     * @param exeName 客户apps.bat中的****.exe
     * @return
     */
    private boolean installAction(String exeName){
    	final String apkPath = mExeDir+exeName.substring(0, exeName.length()-4)+".apk";
    	final String apkPackageName = exeName.substring(0, exeName.length()-4);
    	String insVersion = null;	//系统中已经安装的apk的版本号；
    	String apkVersion = null;	//sd卡中安装包的版本号；
    	
    	boolean bInstall = false;
    	//判断是否安装，如果安装那么先卸载，再安装，这样是为了解决有些已经安装的apk无法安装的问题
    	//2018.12.01日修改，修改为如果已经安装，则对比已经安装的apk的版本和sd卡中apk的版本是否一致，不一致则以sd卡中的apk为准
    	List<PackageInfo> pinfo = mContext.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
    	if(pinfo != null){
    		for(int i = 0;i<pinfo.size();i++){
    			if(pinfo.get(i).packageName.equalsIgnoreCase(apkPackageName)){
    				bInstall = true;
    				insVersion = pinfo.get(i).versionName;
    				break;
    			}
    		}
    		
    	}
    	final boolean isInstalled = bInstall;

    	
    	
    	if(exeToApk(mExeDir,exeName)){
        	boolean isSameVersion = false;
        	PackageManager pm = mContext.getPackageManager();
        	PackageInfo apkPackinfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        	apkVersion = apkPackinfo.versionName;
        	Log.d(TAG,"insVerSion = "+insVersion+"  apkVersion="+apkVersion);
        	if(apkVersion != null && insVersion != null && apkVersion.equalsIgnoreCase(insVersion)){
        		isSameVersion = true;
        		Log.d(TAG,exeName +" has been insmod and the vesion is same!");
        		return false;	//不进行安装，所以返回false
        	}
    		//installApkInSilence(apkPath,apkPackageName);//使用反射，静默安装
    		//mIPackageManager.installPackageAsUser(apkPath, null, 0x00000040, apkPackageName, getUserId(Binder.getCallingUid()));
    		try {
					new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								if(isInstalled){
									mIPackageManager.deletePackageAsUser(apkPackageName, null, getUserId(Binder.getCallingUid()),0x00000002);
								}
								Thread.sleep(2000);
								mIPackageManager.installPackageAsUser(apkPath, null, 0x00000040, apkPackageName, getUserId(Binder.getCallingUid()));
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch(InterruptedException e){
								e.printStackTrace();
							}
						}
						
					}.run();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"installAction failed !!!");
				e.printStackTrace();
				return false;
			}
    	}else{
    		Log.d(TAG,"exeToApk failed !!!");
    		return false;
    	}
    	return true;
    }
    
    /**
     * 采用系统的接口静默卸载
     * @param exeName exeName 客户apps.bat中的****.exe
     * @return
     */
    private boolean uninstallAction(String exeName){
    	//静默卸载
    	final String apkPackageName = exeName.substring(0, exeName.length()-4);
    	//uninstallApkInSilence(apkPackageName);
    	//mIPackageManager.deletePackageAsUser(apkPackageName, null, getUserId(Binder.getCallingUid()),0x00000002);
		try {
				new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							mIPackageManager.deletePackageAsUser(apkPackageName, null, getUserId(Binder.getCallingUid()),0x00000002);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"uninstallAction failed !!!");
			e.printStackTrace();
			return false;
		}
    	return true;
    } 

	/**
	 * 启动apk
	 */
	public boolean openApk(){
		if(mContext == null){
			Log.d(TAG,"mContext is null,startAPP failed !!!");
			return false;
		}
		if(mOpenApkPackageName == null){
			return false;
		}
	    try{
	    	String packageName = mOpenApkPackageName.substring(0,mOpenApkPackageName.length()-4);
	        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
	        mContext.startActivity(intent);        
	    }catch(Exception e){
	        Toast.makeText(mContext, "没有安装", Toast.LENGTH_LONG).show();
	        return false;
	    }
	    return true;
	}
	
	
    /**
     * 解析客户的apps.bat文件
     * @return 正确解析返回true,当apps.bat不存在的时候返回false
     */
    public boolean parseAppsBAT(){
    	//清除list
    	cmdList.clear();
    	installList.clear();
    	uninstallList.clear();
    	mOpenApkPackageName=null;
    	
    	String pathname = mSDPath + "apps.bat";
    	File filename = new File(pathname);
    	if(!filename.exists()){
    		Log.d(TAG,"apps.bat is not exit !!!");
    		return false;
    	}
    	
    	//将apps.bat完全解析存放到cmdList中
    	InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(filename));
		    BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言  
		    String line = "";  
		    int i = 0;
		    while (line != null) {  
		        line = br.readLine(); // 一次读入一行数据  
		        Log.d(TAG, "line = "+line);
		        cmdList.add(line);
		    }  
		    br.close();
		    reader.close();
		    Log.d(TAG, "cmdList = "+cmdList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "cmdList.size() = "+cmdList.size());
		
		//分类cmdList
		int flag = 0;
		for (int i = 0; i < cmdList.size(); i++) {
			if (cmdList.get(i) == null)
				continue;
			if (cmdList.get(i).trim().endsWith(".exe")) {
				if(flag == 1){
					installList.add(cmdList.get(i).trim());
				}
				if(flag == 2){
					uninstallList.add(cmdList.get(i).trim());
				}
				if(flag == 3){
					mOpenApkPackageName = cmdList.get(i).trim();	
				}
			} else if (cmdList.get(i).contains("apps install")) {
				flag = 1;
			} else if (cmdList.get(i).contains("apps uninstall")) {
				flag = 2;
			} else if (cmdList.get(i).contains("open apps")) {
				flag = 3;
			}
		}
		Log.d(TAG, "installList = "+installList);
		Log.d(TAG, "uninstallList = "+uninstallList);
		Log.d(TAG, "mOpenApkPackageName = "+mOpenApkPackageName);
		return true;
    }
    
    /**
     * 将客户的exe文件处理之后生成apk
     * @return
     */
    private boolean exeToApk(String exeDir,String exeName){
    	if(exeName == null  || !exeName.contains(".exe")){
    		Log.d(TAG,exeName +" is not incorrect !!!");
    		return false;
    	}
    	
        String exeFilePath=exeDir + exeName;        //exe文件路径
        Log.d(TAG,"exefile path = "+exeFilePath);
        File exef = new File(exeFilePath);
        if(!exef.exists()){
			Log.d(TAG,exeFilePath +" is not exit!!!");
			return false;
        }
        /*判断对应的apk是否存在，如果存在，则删除*/
        String apkName = exeName.substring(0,exeName.length() -4)+".apk";
        String apkFilePath = exeDir + apkName;        //apk文件路径
        Log.d(TAG,"apkFilePath = "+apkFilePath);
        File apkf = new File(apkFilePath);
        if(apkf.exists()){
			apkf.delete();
        }
        
        FileInputStream fis;
		FileOutputStream fos;
		BufferedInputStream bis;
		ByteArrayOutputStream bos;
		try {
			fis = new FileInputStream(exef);
			fos = new FileOutputStream(apkf);
			bis = new BufferedInputStream(fis);
			bos = new ByteArrayOutputStream((int)(exef.length()));
			byte[] buffer = new byte[1024];
			int len = -1;
			bis.read(buffer, 0, 500);//抛弃前500个字节
			while ((len = bis.read(buffer)) != -1) {
			    bos.write(buffer, 0, len);
			}
			bos.writeTo(fos);
			bos.flush();
			bos.close();
			fos.close();
			bis.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    /**
     * 此函数是执行安装操作之后，无论安装是否成功，将之前生成的apk文件删除掉
     * @param exeName
     */
    private void removeApkFile(String exeName){
    	String apkPath = mExeDir+exeName.substring(0, exeName.length()-4)+".apk";
    	File apkf = new File(apkPath);
        if(apkf.exists()){
			apkf.delete();
        }
    }
    
	/**
	 * 此接口没有用，暂时保留
	 * @param packagename
	 * @return
	 */
	private boolean isApkInstalled(String packagename){

		PackageManager localPackageManager = mContext.getPackageManager();
		try{
			PackageInfo localPackageInfo = localPackageManager.getPackageInfo(packagename, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		}catch (PackageManager.NameNotFoundException localNameNotFoundException){
			return false;
		}
	}

	/**
	 * 这是从网上找到的静默安装的方法，经过测试可行
	 * @param installPath apk文件所在路径
	 * @param packageName apk的包名
	 */
     static void installApkInSilence(String installPath,String packageName) {
        Class<?> pmService;
        Class<?> activityTherad;
        Method method;
        try {
            activityTherad = Class.forName("android.app.ActivityThread");
            Class<?> paramTypes[] = getParamTypes(activityTherad, "getPackageManager");
            method = activityTherad.getMethod("getPackageManager", paramTypes);
            Object PackageManagerService = method.invoke(activityTherad);
            pmService = PackageManagerService.getClass();
            Class<?> paramTypes1[] = getParamTypes(pmService, "installPackageAsUser");
            method = pmService.getMethod("installPackageAsUser", paramTypes1);
            method.invoke(PackageManagerService, installPath, null, 0x00000040, packageName, getUserId(Binder.getCallingUid()));//getUserId
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
 	/**
 	 * 这是参照静默安装来写静默卸载的方法，经过测试可行
 	 * @param uninstallpackageName 卸载apk的包名
 	 */
     static void uninstallApkInSilence(String uninstallpackageName) {
         Class<?> pmService;
         Class<?> activityTherad;
         Method method;
         try {
             activityTherad = Class.forName("android.app.ActivityThread");
             Class<?> paramTypes[] = getParamTypes(activityTherad, "getPackageManager");
             method = activityTherad.getMethod("getPackageManager", paramTypes);
             Object PackageManagerService = method.invoke(activityTherad);
             pmService = PackageManagerService.getClass();
             Class<?> paramTypes1[] = getParamTypes(pmService, "deletePackageAsUser");
             method = pmService.getMethod("deletePackageAsUser", paramTypes1);
             method.invoke(PackageManagerService, uninstallpackageName, null, getUserId(Binder.getCallingUid()), 0x00000002);//getUserId
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
     }

    private static Class<?>[] getParamTypes(Class<?> cls, String mName) {
        Class<?> cs[] = null;
        Method[] mtd = cls.getMethods();
        for (int i = 0; i < mtd.length; i++) {
            if (!mtd[i].getName().equals(mName)) {
                continue;
            }

            cs = mtd[i].getParameterTypes();
        }
        return cs;
    }
    public static final int PER_USER_RANGE = 100000;
    public static int getUserId(int uid) {
        return uid / PER_USER_RANGE;
    }
}
