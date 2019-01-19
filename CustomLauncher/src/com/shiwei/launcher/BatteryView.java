package com.shiwei.launcher;

import android.view.View;
import android.widget.ImageView;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class BatteryView extends View {
	
    private int mPower = 0;   
    private Bitmap mbitmap;
    private Paint paint;
    private boolean isCharge = false;
    
	public BatteryView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBatter(canvas);
    }
    
    private void drawBatter(Canvas canvas) {
    	String powerStr;
    	paint = new Paint();
    	if(isCharge == true){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_charge);
    		canvas.drawBitmap(mbitmap,  (getMeasuredWidth() - mbitmap.getWidth())/2,(getMeasuredHeight() - mbitmap.getHeight())/2, paint);
    		
    		paint.setTextSize(12);
        	Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        	paint.setTypeface(font);
        	if(mPower>=0 && mPower <= 10){
        		powerStr = "10 %";
        	}else if(mPower>10 && mPower <= 20){
        		powerStr = "20 %";
        	}else if(mPower>20 && mPower <= 30){
        		powerStr = "30 %";
        	}else if(mPower>30 && mPower <= 40){
        		powerStr = "40 %";
        	}else if(mPower>40 && mPower <= 50){
        		powerStr = "50 %";
        	}else if(mPower>50 && mPower <= 60){
        		powerStr = "60 %";
        	}else if(mPower>60 && mPower <= 70){
        		powerStr = "70 %";
        	}else if(mPower>70 && mPower <= 80){
        		powerStr = "80 %";
        	}else if(mPower>80 && mPower <= 90){
        		powerStr = "90 %";
        	}else if(mPower>90 && mPower <= 100){
        		powerStr = "100%";
        	}else{
        		powerStr = "10 %";
        	}
    		canvas.drawText(powerStr,(getMeasuredWidth() - mbitmap.getWidth())/2,(getMeasuredHeight() + mbitmap.getHeight())/2-10, paint);
    		return;
    	}
    	if(mPower>=0 && mPower <= 10){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_10);
    	}else if(mPower>10 && mPower <= 20){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_20);
    	}else if(mPower>20 && mPower <= 30){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_30);
    	}else if(mPower>30 && mPower <= 40){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_40);
    	}else if(mPower>40 && mPower <= 50){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_50);
    	}else if(mPower>50 && mPower <= 60){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_60);
    	}else if(mPower>60 && mPower <= 70){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_70);
    	}else if(mPower>70 && mPower <= 80){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_80);
    	}else if(mPower>80 && mPower <= 90){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_90);
    	}else if(mPower>90 && mPower <= 100){
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_100);
    	}else{
    		mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batt_10);
    	}
    	canvas.drawBitmap(mbitmap,  (getMeasuredWidth() - mbitmap.getWidth())/2,(getMeasuredHeight() - mbitmap.getHeight())/2, paint);
    	
    }

    /**
     * 设置电池电量
     *
     * @param power
     */
    public void setPower(int power) {
        this.mPower = power;
        if (mPower < 0) {
            mPower = 0;
        }
        invalidate();//刷新VIEW
    }
    public void setCharge(boolean isCharge) {
        this.isCharge = isCharge;
        //invalidate();//刷新VIEW
    }

    /**
     * 获取电池电量
     *
     * @return
     */
    public int getPower() {
        return mPower;
    }

}
