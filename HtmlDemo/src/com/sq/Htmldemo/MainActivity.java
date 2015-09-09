package com.sq.Htmldemo;

import java.util.Vector;

import com.sq.iChart.chart.Column2D;
import com.sq.iChart.chart.Donut2D;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends Activity {
	private WebView web;
	private WebSettings webSettings;
	private Vector<Item> chartDataOne = new Vector<Item>();
	private Vector<Item> chartDataTwo = new Vector<Item>();
	private Vector<Item> chartDataThree = new Vector<Item>();
	private String dataOne;
	private String dataTwo;
	private String dataThree;
	private String dataOne_labels;
	private String dataTwo_labels;
	private String dataThree_labels;
	private Donut2D donut2DOne;
	private Column2D column2DOne;
	private Column2D column2DTwo;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initChartDataOne();
        initChartDataTwo();
        initChartDataThree();
        
        web = (WebView)findViewById(R.id.web);
        webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true); //设定该WebView可以执行JavaScript程序
        webSettings.setBuiltInZoomControls(false); //设定该WebView可以缩放
        web.addJavascriptInterface(this, "mainActivity");
        web.loadUrl("file:///android_asset/12.html");
        
        dataOne = PackageChartData.PackageData(chartDataOne);
        dataTwo = PackageChartData.PackageData(chartDataTwo);
        dataThree = PackageChartData.PackageData(chartDataThree);

        column2DOne = new Column2D(800,200,"标题一", dataOne);
        
        column2DTwo = new Column2D(400,200,"标题二", dataTwo);
        
        donut2DOne = new Donut2D(400, 200, "袁满大爷一天的生活", dataThree);
        donut2DOne.setRadius(1000);

        web.addJavascriptInterface(column2DOne, "column2DOne");
        web.addJavascriptInterface(column2DTwo, "column2DTwo");
        web.addJavascriptInterface(donut2DOne, "donut2DOne");

        Log.i("test", "dataOne:"+dataOne);
        Log.i("test", "dataTwo:"+dataTwo);
        Log.i("test", "dataThree:"+dataThree);
    }

    private void initChartDataOne(){
    	Item item = new Item();
    	item.setName("吃饭");
    	item.setValue(2);
    	item.setColor("#4572a7");
    	chartDataOne.add(item);
    	
    	item = new Item();
    	item.setName("睡觉");
    	item.setValue(8);
    	item.setColor("#aa4643");
    	chartDataOne.add(item);
    	
    	item = new Item();
    	item.setName("工作");
    	item.setValue(10);
    	item.setColor("#89a54e");
    	chartDataOne.add(item);
    	
    	item = new Item();
    	item.setName("发呆");
    	item.setValue(1);
    	item.setColor("#80699b");
    	chartDataOne.add(item);
    	
    }
    
    
    private void initChartDataTwo(){
    	Item item = new Item();
    	item.setName("吃饭");
    	item.setValue(5);
    	item.setColor("#4572a7");
    	chartDataTwo.add(item);
    	
    	item = new Item();
    	item.setName("睡觉");
    	item.setValue(2);
    	item.setColor("#aa4643");
    	chartDataTwo.add(item);
    	
    	item = new Item();
    	item.setName("工作");
    	item.setValue(3);
    	item.setColor("#89a54e");
    	chartDataTwo.add(item);
    	
    	item = new Item();
    	item.setName("发呆");
    	item.setValue(4);
    	item.setColor("#80699b");
    	chartDataTwo.add(item);
    	
    }
    
    private void initChartDataThree(){
    	Item item = new Item();
    	item.setName("吃饭");
    	item.setValue(2);
    	item.setColor("#4572a7");
    	chartDataThree.add(item);
    	
    	item = new Item();
    	item.setName("睡觉");
    	item.setValue(8);
    	item.setColor("#aa4643");
    	chartDataThree.add(item);
    	
    	item = new Item();
    	item.setName("工作");
    	item.setValue(10);
    	item.setColor("#89a54e");
    	chartDataThree.add(item);
    	
    	item = new Item();
    	item.setName("发呆");
    	item.setValue(1);
    	item.setColor("#80699b");
    	chartDataThree.add(item);
    	
    }
    @JavascriptInterface
    public void updateColumn2DOne(){
    	column2DOne.setTitle("");
    }
    @JavascriptInterface
    public void updateColumn2DTwo(){
    	column2DTwo.setTitle("");
    }
    @JavascriptInterface
    public void updateDonut2DOne(){
    	donut2DOne.setTitle("改变之后的标题");
    	donut2DOne.setData(dataTwo);
    }
    
    /**
     * 用于调试的方法，该方法将在js脚本中，通过window.mainActivity.debugOut(“”)进行调用
     * @param out
     */
    @JavascriptInterface
    public void debugOut(String out) {
		// TODO Auto-generated method stub
    	Log.i("test", "debugOut:" + out);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
