package com.niotong.tester;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.niotong.tester.JniTest;

public class MainActivity extends Activity {

	private TextView resultView ;
	private Button addButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (TextView) findViewById(R.id.textView2);
        addButton = (Button) findViewById(R.id.button1);
        
        addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				resultView.setText(String.valueOf(JniTest.Jadd(3.4,5.6)));
			}
		});
    }
    
}
