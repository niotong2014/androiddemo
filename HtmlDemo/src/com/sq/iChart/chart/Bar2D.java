package com.sq.iChart.chart;

import android.webkit.JavascriptInterface;

import com.sq.iChart.parameter.Chart;

/**
 * 2D条形图
 * @author SQ
 *
 */
public class Bar2D extends Chart{
	private int barheight; //条形的高度，默认会根据坐标系的高度进行计算
	
	/**
	 * 2D条形图
	 * @param title 2D条形图标题
	 * @param data 2D条形图所需的单一数据源,是打包好的JSON格式数据
	 */
	public Bar2D(String title, String data) {
		super(title, data);
		// TODO Auto-generated constructor stub
	}
	@JavascriptInterface
	public int getBarheight() {
		return barheight;
	}

	public void setBarheight(int barheight) {
		this.barheight = barheight;
	}
}
