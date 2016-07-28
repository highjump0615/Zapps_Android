package com.iliayugai.zapp;

import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ForgetActivity extends Activity implements View.OnClickListener 
{
	private static final String TAG = ForgetActivity.class.getSimpleName();
	
	private EditText mEditEmail;
	
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		
		initView();
	}
	
	@Override
	public void onClick(View view) 
	{
		int id = view.getId();
		
		switch(id)
		{
		case R.id.btn_signin:
			onBackPressed();
			break;
			
		case R.id.btn_reset:
			onRequest();			
			break;
		}
	}
	
	@Override
	public void onBackPressed() 
	{
		startActivity(new Intent(this, SigninActivity.class));		
		overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
		super.onBackPressed();
	}
	
	public void initView()
	{
		Config.scaleLayout(this, "forget", findViewById(R.id.layout_root), true);
		
		// set font
		Typeface helveticaFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");
		Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");
		
		// signin button
		Button button = (Button)findViewById(R.id.btn_signin);
		button.setTypeface(helveticaFont);
		button.setOnClickListener(this);
		
		// request button
		button = (Button)findViewById(R.id.btn_reset);
		button.setOnClickListener(this);
		
		// title text
		TextView txtTitle = (TextView)findViewById(R.id.text_title);
		txtTitle.setTypeface(helveticaBoldFont);
		
		// content text
		TextView txtDesc = (TextView)findViewById(R.id.text_desc);
		txtDesc.setTypeface(helveticaFont);
		
		// email text
		mEditEmail = (EditText)findViewById(R.id.text_email);
		mEditEmail.setTypeface(helveticaFont);
	}
	
	private void onRequest()
	{
		if (TextUtils.isEmpty(mEditEmail.getText().toString()))
		{
			CommonUtils.createErrorAlertDialog(this, "Alert", "Input your email address").show();
			return;
		}
		
		ParseUser.requestPasswordResetInBackground(mEditEmail.getText().toString(), new RequestPasswordResetCallback() 
		{
            @Override
            public void done(ParseException e) 
            {
                mProgressDialog.dismiss();

                if (e == null) 
                {
                    Log.d(TAG, "Request has been submitted successfully");
                    CommonUtils.createErrorAlertDialog(ForgetActivity.this, "Alert", "Request has been submitted").show();
                }
                else 
                {
                    CommonUtils.createErrorAlertDialog(ForgetActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });


		mProgressDialog = ProgressDialog.show(this, "", "Sending Request...");
	}

}
