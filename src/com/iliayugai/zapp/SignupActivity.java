package com.iliayugai.zapp;

import java.io.ByteArrayOutputStream;

import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.R;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SignupActivity extends Activity implements View.OnClickListener 
{
	private static final String TAG = SignupActivity.class.getSimpleName();
	
	private EditText mEditUsername;
	private EditText mEditEmail;
	private EditText mEditPassword;
	private EditText mEditCPassword;
	
	private ImageView mButPhoto;
	
	private Uri mFileUri;
	private Bitmap mNewBitmap = null;
	private boolean mbHasPhoto = false;
	
	private boolean mButtonPressed = false;	
	
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		initView();
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		mButtonPressed = false;
	}
	
	@Override
	public void onBackPressed() 
	{
		startActivity(new Intent(this, SigninActivity.class));
		overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
		super.onBackPressed();
	}

	@Override
	public void onClick(View view) 
	{
		if (mButtonPressed)
			return;
		
		mButtonPressed = false;
		int id = view.getId();
		
		switch(id)
		{
		case R.id.btn_signin:
			onBackPressed();
			break;
			
		case R.id.btn_photo:
			mFileUri = Uri.fromFile(CommonUtils.getOutputMediaFile(true));

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //intent.putExtra("outputX", 320);
            //intent.putExtra("outputY", 320);
            intent.putExtra("scale", 1);
            intent.putExtra("return-data", false/*return_data*/);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            //intent.putExtra("noFaceDetection",!faceDetection); // lol, negative boolean noFaceDetection
            /*if (circleCrop) {
                intent.putExtra("circleCrop", true);
            }*/

            startActivityForResult(intent, CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE);
            
            break;
			
		case R.id.btn_facebook:
			CommonUtils.onFacebookLogin(this);
			break;
			
		case R.id.btn_twitter:
			CommonUtils utils = CommonUtils.sharedObject();
			utils.onTwitterLogin(this);
			break;
			
		case R.id.btn_signup:
			onSignup();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				if (data == null)
				{
					onBackPressed();
				}
				else
				{
					Object object = data.getData();
	                if (object != null) 
	                {
	                    mFileUri = (Uri) object;
	                }
	
	                String mimeType = null;
	                String realFilePath;
	                Log.d(TAG, "selected fileName = " + mFileUri.getPath());
	
	                // Crop image
	                /*if (requestCode == GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE) {
	                    Intent viewMediaIntent = new Intent();
	                    viewMediaIntent.setAction(android.content.Intent.ACTION_VIEW);
	                    File file = new File(mFileUri.getPath());
	                    viewMediaIntent.setDataAndType(Uri.fromFile(file), "image*//*");
	                    viewMediaIntent.putExtra("crop", "true");
	                    viewMediaIntent.putExtra("aspectX", 1);
	                    viewMediaIntent.putExtra("aspectY", 1);
	                    viewMediaIntent.putExtra("outputX", 140);
	                    viewMediaIntent.putExtra("outputY", 140);
	                    viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
	                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
	                    startActivityForResult(viewMediaIntent, CROP_REQUEST_CODE);
	                    return;
	                }*/
	
	                if (mFileUri.getPath().contains("/external/")) 
	                {
	                    mimeType = CommonUtils.getMimeType(this, mFileUri);
	                    realFilePath = CommonUtils.convertImageUriToFile(this, mFileUri);
	                }
	                else 
	                {
	                    realFilePath = mFileUri.getPath();
	                    String extension = MimeTypeMap.getFileExtensionFromUrl(realFilePath);
	                    if (extension != null) 
	                    {
	                        MimeTypeMap mime = MimeTypeMap.getSingleton();
	                        mimeType = mime.getMimeTypeFromExtension(extension);
	                    }
	                }
	
	                if (mimeType != null) 
	                {
	                    if (mimeType.contains("image")) 
	                    {
	                        mNewBitmap = CommonUtils.getBitmapFromUri(Uri.parse(realFilePath));
	                        mButPhoto.setImageBitmap(mNewBitmap);
	                        mbHasPhoto = true;
	                        
	                        return;
	                    } 
	                }
	
	                mNewBitmap = null;
				}
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	

	public void initView()
	{
		Config.scaleLayout(this, "signup", findViewById(R.id.layout_root), true);
		
		// set font
		Typeface helveticaFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");
		Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");
		
		// signin button
		Button button = (Button)findViewById(R.id.btn_signin);
		button.setTypeface(helveticaFont);
		button.setOnClickListener(this);
		
		// signup button		
		button = (Button)findViewById(R.id.btn_signup);
		button.setOnClickListener(this);
		
		// title text
		TextView txtTitle = (TextView)findViewById(R.id.text_title);
		txtTitle.setTypeface(helveticaBoldFont);
		
		// username text
		mEditUsername = (EditText)findViewById(R.id.text_username);
		mEditUsername.setTypeface(helveticaFont);
		
		// email text
		mEditEmail = (EditText)findViewById(R.id.text_email);
		mEditEmail.setTypeface(helveticaFont);

		// password text
		mEditPassword = (EditText)findViewById(R.id.text_password);
		mEditPassword.setTypeface(helveticaFont);
		
		// password text
		mEditCPassword = (EditText)findViewById(R.id.text_cpassword);
		mEditCPassword.setTypeface(helveticaFont);

		// or text
		TextView txtOr = (TextView)findViewById(R.id.text_or);
		txtOr.setTypeface(helveticaFont);
		
		//
		// Button
		//
		mButPhoto = (ImageView)findViewById(R.id.btn_photo);
		mButPhoto.setOnClickListener(this);
		
		button = (Button)findViewById(R.id.btn_facebook);
		button.setOnClickListener(this);
		
		button = (Button)findViewById(R.id.btn_twitter);
		button.setOnClickListener(this);
		
	}
	
	private void onSignup()
	{
		if (TextUtils.isEmpty(mEditUsername.getText().toString()) || TextUtils.isEmpty(mEditEmail.getText().toString()))			
		{
			CommonUtils.createErrorAlertDialog(this, "Alert", "Fill username and email").show();
			return;
		}
		
		if (TextUtils.isEmpty(mEditPassword.getText().toString()))
		{
			CommonUtils.createErrorAlertDialog(this, "Alert", "Input your password").show();
			return;
		}
		
		if (!mEditPassword.getText().toString().equals(mEditCPassword.getText().toString()))
		{
			CommonUtils.createErrorAlertDialog(this, "Alert", "Password does not match").show();
			return;
		}
		
		final ParseUser user = new ParseUser();
		user.setEmail(mEditEmail.getText().toString());
		user.setUsername(mEditUsername.getText().toString());
		user.setPassword(mEditPassword.getText().toString());
		user.put("distanceFilter", false);
	    user.put("distance", 50);
		
		user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) 
            {
                if (e == null) 
                {
                    Log.d(TAG, "Sign up success with Parse user");

                    ParseACL roleACL = new ParseACL();
                    roleACL.setPublicReadAccess(true);
                    roleACL.setReadAccess(user, true);
                    roleACL.setWriteAccess(user, true);
                    user.setACL(roleACL);
                    
                    if (mbHasPhoto)
                    {
                    	mProgressDialog.setMessage("Saving image data");
                    	
	                    // saving image
	                    BitmapDrawable bitmapDrawable = (BitmapDrawable)mButPhoto.getDrawable();
	        			Bitmap bitmap = bitmapDrawable.getBitmap();
	        			ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	        			byte[] imageInByte = stream.toByteArray();
	        			
	        			ParseFile imageFile = new ParseFile("photo.jpg", imageInByte);
	        			user.put("photo", imageFile);
	        			
	        			Bitmap bitmapThumb = bitmapDrawable.getBitmap();
	        			ByteArrayOutputStream streamThumb = new ByteArrayOutputStream();
	        			bitmapThumb.compress(Bitmap.CompressFormat.JPEG, 30, streamThumb);
	        			byte[] imageInByteThumb = streamThumb.toByteArray();
	        			
	        			imageFile = new ParseFile("photoThumb.jpg", imageInByteThumb);
	        			user.put("photothumb", imageFile);
                    }

                    user.saveInBackground();
                    
                    mProgressDialog.dismiss();
                    
                    CommonUtils.moveNextActivity(SignupActivity.this, HomeActivity.class, true);
                } 
                else 
                {
                    CommonUtils.createErrorAlertDialog(SignupActivity.this, "Alert", e.getMessage()).show();
                    mProgressDialog.dismiss();
                }
            }
        });
		
		mProgressDialog = ProgressDialog.show(this, "", "Signing up...");
	}
}
