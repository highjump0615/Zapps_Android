package com.iliayugai.zapp;

import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.R;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.PushService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SigninActivity extends Activity implements View.OnClickListener 
{
	private static final String TAG = SigninActivity.class.getSimpleName();
	
	private EditText mEditUsername;
	private EditText mEditPassword;
	
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		
		initView();
		
		openGPSSettings();
	}
	
	private void checkSignState()
	{
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null)
		{
			CommonUtils.moveNextActivity(SigninActivity.this, HomeActivity.class, true);
		}
	}
	
	private void openGPSSettings()
	{
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
        	alm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) 
        {
        	checkSignState();
            return;
        }

        AlertDialog alert = new AlertDialog.Builder(this)
        		.setTitle("Alert")
        		.setMessage("You need to enable your location service to use this app.")
        		.setCancelable(false)
        		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        			
            @Override
            public void onClick(DialogInterface dialogInterface, int i) 
            {
            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 0); //此为设置完成后返回到获取界面
            }
            
        }).create();
        
        alert.show();
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		CommonUtils.mButtonPressed = false;
	}

	@Override
	public void onClick(View view) 
	{
		if (CommonUtils.mButtonPressed)
			return;
		
		CommonUtils.mButtonPressed = false;
		int id = view.getId();
		
		switch(id)
		{
		case R.id.btn_facebook:
			CommonUtils.onFacebookLogin(this);
			break;
			
		case R.id.btn_twitter:
			CommonUtils utils = CommonUtils.sharedObject();
			utils.onTwitterLogin(this);
			break;
			
		case R.id.btn_signup:
			CommonUtils.moveNextActivity(this, SignupActivity.class, true);
			break;
			
		case R.id.btn_signin:
			onSignin();
			break;
			
		case R.id.btn_forget:
			CommonUtils.moveNextActivity(this, ForgetActivity.class, true);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (requestCode == 0)
		{
			checkSignState();
		}
		else
		{
			ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void initView()
	{
		View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
		Config.scaleLayout(this, "signin", rootView, true);
		
		// set font
		Typeface helveticaFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Md.otf"); 
		
		// username text
		mEditUsername = (EditText)findViewById(R.id.text_username);
		mEditUsername.setTypeface(helveticaFont);
		
		// password text
		mEditPassword = (EditText)findViewById(R.id.text_password);
		mEditPassword.setTypeface(helveticaFont);
		
		// forget button
		Button btnForget = (Button)findViewById(R.id.btn_forget);
		btnForget.setTypeface(helveticaFont);
		btnForget.setOnClickListener(this);
		
		// or text
		TextView txtOr = (TextView)findViewById(R.id.text_or);
		txtOr.setTypeface(helveticaFont);
		
		// first time text
		TextView txtFirst = (TextView)findViewById(R.id.text_first);
		txtFirst.setTypeface(helveticaFont);
		
		//
		// Button
		//
		Button button = (Button)findViewById(R.id.btn_signin);
		button.setOnClickListener(this);
		
		button = (Button)findViewById(R.id.btn_signup);
		button.setOnClickListener(this);
		
		button = (Button)findViewById(R.id.btn_facebook);
		button.setOnClickListener(this);
		
		button = (Button)findViewById(R.id.btn_twitter);
		button.setOnClickListener(this);
	}
	
	private void onSignin()
	{
		if (TextUtils.isEmpty(mEditUsername.getText().toString()) || TextUtils.isEmpty(mEditPassword.getText().toString()))
		{
			CommonUtils.createErrorAlertDialog(this, "Alert", "Fill username and password").show();
			return;
		}
		
		ParseUser.logInInBackground(mEditUsername.getText().toString(),
                mEditPassword.getText().toString(),
                new LogInCallback() 
		{
                    @Override
                    public void done(ParseUser parseUser, ParseException e) 
                    {
                        if (e == null) 
                        {
                            Log.d(TAG, "Sign in success with Parse user");
                            CommonUtils.moveNextActivity(SigninActivity.this, HomeActivity.class, true);
                        }
                        else 
                        {
                            CommonUtils.createErrorAlertDialog(SigninActivity.this, "Alert", e.getMessage()).show();
                        }
                        
                        mProgressDialog.dismiss();
                    }
                }
        );

		mProgressDialog = ProgressDialog.show(this, "", "Signing in...");		
	}
	
}
