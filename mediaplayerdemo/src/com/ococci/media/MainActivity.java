package com.ococci.media;

import java.io.IOException;

import com.ococci.media.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private ProgressBar progressBar;
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
	        progressBar= (ProgressBar) findViewById(R.id.progressBar);
	      //视频链接可能已失效  
	      String uri="rtsp://192.168.1.112:554/niotong.sdp";

	        player=new MediaPlayer();
	        try {
	            player.setDataSource(this, Uri.parse(uri));
	            holder=surfaceView.getHolder();
	            holder.addCallback(new MyCallBack());
	            player.prepare();
	            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
	                @Override
	                public void onPrepared(MediaPlayer mp) {
	                    progressBar.setVisibility(View.INVISIBLE);
	                    player.start();
	                    player.setLooping(true);
	                }
	            });
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    private class MyCallBack implements SurfaceHolder.Callback {
	        @Override
	        public void surfaceCreated(SurfaceHolder holder) {
	            player.setDisplay(holder);
	        }

	        @Override
	        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	        }

	        @Override
	        public void surfaceDestroyed(SurfaceHolder holder) {

	        }
	    }
}
