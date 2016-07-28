package com.iliayugai.zapp;

import java.util.Timer;
import java.util.TimerTask;

import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SplashActivity extends Activity
{
	static final String TAG = SplashActivity.class.getSimpleName();
	public Timer mTransitionTimer;

	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        Config.calculateScaleFactor(this);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        
        setContentView(R.layout.activity_splash);
        
        initView();
        
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, SigninActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    }
                });
            }
        };
        mTransitionTimer = new Timer();
        mTransitionTimer.schedule(task, 3000);

        Log.d(TAG, "Splash screen would be disappeared after 3 seconds.");
    }

	@Override
	protected void onDestroy() 
	{
		if (mTransitionTimer != null) 
		{
            mTransitionTimer.cancel();
            mTransitionTimer = null;
        }

		super.onDestroy();
	}
	
	public void initView()
	{
		View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
		Config.scaleLayout(this, "splash", rootView, true);
	}

}
