package com.niotong.tester;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.niotong.tester.JniTest;

public class MainActivity extends Activity implements OnClickListener{

	private EditText etValue1 ;
	private EditText etValue2 ;
	private TextView tvValue1 ;
	private TextView tvValue2 ;
	private Button equalsButton;
	private Button dojniButton;
	private double a = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etValue1 = (EditText) findViewById(R.id.editText1); // the first value
        etValue2 = (EditText) findViewById(R.id.editText2); // the second value
        tvValue1 = (TextView) findViewById(R.id.textView1);
        tvValue2 = (TextView) findViewById(R.id.textView2); // the result value
        equalsButton = (Button) findViewById(R.id.button1); // the = button
        dojniButton = (Button) findViewById(R.id.button2);
        
        equalsButton.setOnClickListener(this);
        dojniButton.setOnClickListener(this);
    }
    /**
     * 
     * @param b
     * @return
     */
    public double getValue(double b){
    	double a = 0;
    	a = b + 1.0;
    	return a;
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		case R.id.button1:
			useJNItoAdd();
			break;
		case R.id.button2:
			doJNI();
			break;

		default:
			break;
		}
	}
	private void doJNI() {
		// TODO Auto-generated method stub
		JniTest.doJNI();
	}
	private void useJNItoAdd(){
		double a = 0, b = 0, c = 0;
		try {
			a = Double.valueOf(etValue1.getText().toString()).doubleValue();
			b = Double.valueOf(etValue2.getText().toString()).doubleValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c= JniTest.Jadd(a,b);
		tvValue2.setText(String.valueOf(c));
	}
}
