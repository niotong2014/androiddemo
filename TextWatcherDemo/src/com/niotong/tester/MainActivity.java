package com.niotong.tester;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class MainActivity extends Activity {
	private EditText edittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.niotongtest);
        edittext = (EditText) findViewById(R.id.nioedittext);
        edittext.setText("$0");
        edittext.setSelection(2);
        edittext.addTextChangedListener(new editChangedListener(edittext,true));
    }
}
