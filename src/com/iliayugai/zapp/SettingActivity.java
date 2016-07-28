package com.iliayugai.zapp;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SettingActivity extends Activity implements View.OnClickListener, ToggleButton.OnCheckedChangeListener 
{
	private static final String TAG = SettingActivity.class.getSimpleName();

	private SeekBar mSeekbar;
	private TextView mtextCurrentDist;
	private TextView mtextDistTitle;
	private TextView mtextMinDist;
	private TextView mtextMaxDist;
	private EditText meditFullname;
	private EditText meditPassword;
	private EditText meditCPassword;

	private boolean mbPhoto1;
	private boolean mbPhoto2;
	private boolean mbPhoto3;
	private boolean mbPhoto4;

	private boolean mbPhotoUploaded;
	private boolean mbPhotoThumbUploaded;
	private boolean mbPhotoUploaded1;
	private boolean mbPhotoUploaded2;
	private boolean mbPhotoUploaded3;
	private boolean mbPhotoUploaded4;

	private boolean mbPhotoChanged = false;
	private boolean mbPhotoChanged1 = false;
	private boolean mbPhotoChanged2 = false;
	private boolean mbPhotoChanged3 = false;
	private boolean mbPhotoChanged4 = false;

	private static final int GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE = 100;

	private ParseImageView mButPhoto;
	private ParseImageView mButPhoto1;
	private ParseImageView mButPhoto2;
	private ParseImageView mButPhoto3;
	private ParseImageView mButPhoto4;

	private ImageView mBtnDeletePhoto1;
	private ImageView mBtnDeletePhoto2;
	private ImageView mBtnDeletePhoto3;
	private ImageView mBtnDeletePhoto4;

	private Uri mFileUri;
	private Bitmap mNewBitmap = null;

	private ToggleButton mSwitchDistance;

	private ProgressDialog mProgressDialog;

	private int mnHeightOffset;
	
	Timer mSaveTimer;

	// NSTimer* mSavePhotoTimer;
	private String mStrOldName;

	private boolean mbDistanceFilterOld;
	private int mnDistanceOld;
	
	public static ProfileActivity mProfileActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		initView();

		//
		// show user Info
		//
		ParseUser currentUser = ParseUser.getCurrentUser();

		// photo
		ParseFile photoFile = currentUser.getParseFile("photo");
		if (photoFile != null) {
			mButPhoto.setImageDrawable(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto.setParseFile(photoFile);
			mButPhoto.setPlaceholder(getResources().getDrawable(
					R.drawable.profile_photo_default));

			mButPhoto.loadInBackground();
		}

		photoFile = currentUser.getParseFile("photo1");
		if (photoFile != null) {
			mButPhoto1.setImageDrawable(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto1.setParseFile(photoFile);
			mButPhoto1.setPlaceholder(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto1.loadInBackground();

			mbPhoto1 = true;
		}

		photoFile = currentUser.getParseFile("photo2");
		if (photoFile != null) {
			mButPhoto2.setImageDrawable(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto2.setParseFile(photoFile);
			mButPhoto2.setPlaceholder(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto2.loadInBackground();

			mbPhoto2 = true;
		}

		photoFile = currentUser.getParseFile("photo3");
		if (photoFile != null) {
			mButPhoto3.setImageDrawable(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto3.setParseFile(photoFile);
			mButPhoto3.setPlaceholder(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto3.loadInBackground();

			mbPhoto3 = true;
		}

		photoFile = currentUser.getParseFile("photo4");
		if (photoFile != null) {
			mButPhoto4.setImageDrawable(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto4.setParseFile(photoFile);
			mButPhoto4.setPlaceholder(getResources().getDrawable(
					R.drawable.profile_photo_default));
			mButPhoto4.loadInBackground();

			mbPhoto4 = true;
		}

		showDeletePhotoButtons();

		// show full name
		mStrOldName = currentUser.getString("fullname");
		meditFullname.setText(mStrOldName);

		mbPhotoUploaded = mbPhotoThumbUploaded = mbPhotoUploaded1 = mbPhotoUploaded2 = mbPhotoUploaded3 = mbPhotoUploaded4 = true;

		// distance enable state
		mbDistanceFilterOld = currentUser.getBoolean("distanceFilter");
		mnDistanceOld = currentUser.getInt("distance");

		// show distance
		mSeekbar.setProgress(mnDistanceOld - 1);
		mtextCurrentDist.setText(String.format("%d KM", mnDistanceOld));

		mSwitchDistance.setChecked(mbDistanceFilterOld);
		enableDistanceView();
	}

	public void initView() 
	{
		View rootView = getWindow().getDecorView().findViewById(
				android.R.id.content);
		Config.mHeightScaleFactor /= 1.3f;
		Config.scaleLayout(this, "setting", rootView, true);
		Config.mHeightScaleFactor *= 1.3f;

		Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(),
				"fonts/HelveticaNeueLTPro-Bd.otf");
		Typeface helveticaFont = Typeface.createFromAsset(getAssets(),
				"fonts/HelveticaNeueLTPro-Md.otf");

		// title
		TextView textTitle = (TextView) findViewById(R.id.text_title);
		textTitle.setTypeface(helveticaBoldFont);

		// button
		Button button = (Button) rootView.findViewById(R.id.but_save);
		button.setTypeface(helveticaFont);
		button.setOnClickListener(this);

		// button
		button = (Button) rootView.findViewById(R.id.but_cancel);
		button.setTypeface(helveticaFont);
		button.setOnClickListener(this);

		// photo buttons
		mButPhoto = (ParseImageView) findViewById(R.id.img_photomain);
		mButPhoto.setOnClickListener(this);

		mButPhoto1 = (ParseImageView) findViewById(R.id.img_photo1);
		mButPhoto1.setOnClickListener(this);

		mButPhoto2 = (ParseImageView) findViewById(R.id.img_photo2);
		mButPhoto2.setOnClickListener(this);

		mButPhoto3 = (ParseImageView) findViewById(R.id.img_photo3);
		mButPhoto3.setOnClickListener(this);

		mButPhoto4 = (ParseImageView) findViewById(R.id.img_photo4);
		mButPhoto4.setOnClickListener(this);

		mBtnDeletePhoto1 = (ImageView) findViewById(R.id.img_deletephoto1);
		mBtnDeletePhoto1.setOnClickListener(this);

		mBtnDeletePhoto2 = (ImageView) findViewById(R.id.img_deletephoto2);
		mBtnDeletePhoto2.setOnClickListener(this);

		mBtnDeletePhoto3 = (ImageView) findViewById(R.id.img_deletephoto3);
		mBtnDeletePhoto3.setOnClickListener(this);

		mBtnDeletePhoto4 = (ImageView) findViewById(R.id.img_deletephoto4);
		mBtnDeletePhoto4.setOnClickListener(this);

		// text
		TextView text = (TextView) findViewById(R.id.text_nickname);
		text.setTypeface(helveticaFont);

		meditFullname = (EditText) findViewById(R.id.edit_nickname);
		meditFullname.setTypeface(helveticaBoldFont);

		text = (TextView) findViewById(R.id.text_switch);
		text.setTypeface(helveticaFont);

		mSwitchDistance = (ToggleButton) findViewById(R.id.toggle_switch);
		mSwitchDistance.setOnCheckedChangeListener(this);

		mtextDistTitle = (TextView) findViewById(R.id.text_distance);
		mtextDistTitle.setTypeface(helveticaFont);

		mtextMinDist = (TextView) findViewById(R.id.text_dist_min);
		mtextMinDist.setTypeface(helveticaFont);

		mtextMaxDist = (TextView) findViewById(R.id.text_dist_max);
		mtextMaxDist.setTypeface(helveticaFont);

		mtextCurrentDist = (TextView) findViewById(R.id.text_dist_current);
		mtextCurrentDist.setTypeface(helveticaBoldFont);

		text = (TextView) findViewById(R.id.text_passchg);
		text.setTypeface(helveticaFont);

		text = (TextView) findViewById(R.id.text_password);
		text.setTypeface(helveticaFont);

		text = (TextView) findViewById(R.id.text_cpassword);
		text.setTypeface(helveticaFont);

		meditPassword = (EditText) findViewById(R.id.edit_password);
		meditPassword.setTypeface(helveticaFont);

		meditCPassword = (EditText) findViewById(R.id.edit_cpassword);
		meditCPassword.setTypeface(helveticaFont);

		button = (Button) rootView.findViewById(R.id.but_logout);
		button.setTypeface(helveticaFont);
		button.setOnClickListener(this);

		// distance seek bar
		mSeekbar = (SeekBar) findViewById(R.id.seekbar_distance);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSeekbar.getLayoutParams();
		params.width = (int) (CommonUtils.dpToPx(R.dimen.setting_seekbar_distance_width, this) * Config.mWidthScaleFactor);
//		params.topMargin = (int) (CommonUtils.dpToPx(R.dimen.setting_seekbar_distance_topMargin, this) * Config.mWidthScaleFactor);
		mSeekbar.setLayoutParams(params);
		mSeekbar.setProgress(49);
		mSeekbar.setMax(99);

		mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				mtextCurrentDist.setText(String.format("%d KM", i + 1));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

	public void showDeletePhotoButtons() {
		if (mbPhoto1) {
			mBtnDeletePhoto1.setVisibility(View.VISIBLE);
		} else {
			mBtnDeletePhoto1.setVisibility(View.GONE);
		}

		if (mbPhoto2) {
			mBtnDeletePhoto2.setVisibility(View.VISIBLE);
		} else {
			mBtnDeletePhoto2.setVisibility(View.GONE);
		}

		if (mbPhoto3) {
			mBtnDeletePhoto3.setVisibility(View.VISIBLE);
		} else {
			mBtnDeletePhoto3.setVisibility(View.GONE);
		}

		if (mbPhoto4) {
			mBtnDeletePhoto4.setVisibility(View.VISIBLE);
		} else {
			mBtnDeletePhoto4.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		int id = view.getId();

		switch (id) {
		case R.id.but_cancel:
			onBackPressed();
			break;

		case R.id.but_save:

			onSave();
			break;

		case R.id.but_logout:
			ParseUser.logOut();
			
			if (ParseFacebookUtils.getSession() != null && ParseFacebookUtils.getSession().isOpened())
                ParseFacebookUtils.getSession().closeAndClearTokenInformation();

			intent = new Intent(this, SigninActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Removes other Activities from stack

			startActivity(intent);
			// finish();
			overridePendingTransition(R.anim.pop_in_vert, R.anim.pop_out_vert);
			break;

		case R.id.img_photomain:
		case R.id.img_photo1:
		case R.id.img_photo2:
		case R.id.img_photo3:
		case R.id.img_photo4:

			mFileUri = Uri.fromFile(CommonUtils.getOutputMediaFile(true));

			intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			// intent.putExtra("outputX", 320);
			// intent.putExtra("outputY", 320);
			intent.putExtra("scale", 1);
			intent.putExtra("return-data", false/* return_data */);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
			intent.putExtra("outputFormat",
					Bitmap.CompressFormat.JPEG.toString());
			// intent.putExtra("noFaceDetection",!faceDetection); // lol,
			// negative boolean noFaceDetection
			/*
			 * if (circleCrop) { intent.putExtra("circleCrop", true); }
			 */

			startActivityForResult(intent,
					CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + id);

			break;

		case R.id.img_deletephoto1:
			mButPhoto1.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_psetting_no_pic));
			mbPhoto1 = false;
			mbPhotoChanged1 = false;
			showDeletePhotoButtons();
			break;

		case R.id.img_deletephoto2:
			mButPhoto2.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_psetting_no_pic));
			mbPhoto2 = false;
			mbPhotoChanged2 = false;
			showDeletePhotoButtons();
			break;

		case R.id.img_deletephoto3:
			mButPhoto3.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_psetting_no_pic));
			mbPhoto3 = false;
			mbPhotoChanged3 = false;
			showDeletePhotoButtons();
			break;

		case R.id.img_deletephoto4:
			mButPhoto4.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_psetting_no_pic));
			mbPhoto4 = false;
			mbPhotoChanged4 = false;
			showDeletePhotoButtons();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.pop_in_vert, R.anim.pop_out_vert);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + R.id.img_photomain || 
			requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + R.id.img_photo1 || 
			requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + R.id.img_photo2 || 
			requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + R.id.img_photo3 || 
			requestCode == CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE + R.id.img_photo4) 
		{
			if (resultCode == RESULT_OK) {
				if (data == null) {
					onBackPressed();
				} else {
					Object object = data.getData();
					if (object != null) {
						mFileUri = (Uri) object;
					}

					String mimeType = null;
					String realFilePath;
					Log.d(TAG, "selected fileName = " + mFileUri.getPath());

					// Crop image
					/*
					 * if (requestCode == GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE) {
					 * Intent viewMediaIntent = new Intent();
					 * viewMediaIntent.setAction
					 * (android.content.Intent.ACTION_VIEW); File file = new
					 * File(mFileUri.getPath());
					 * viewMediaIntent.setDataAndType(Uri.fromFile(file), "image
					 *//*
						 * "); viewMediaIntent.putExtra("crop", "true");
						 * viewMediaIntent.putExtra("aspectX", 1);
						 * viewMediaIntent.putExtra("aspectY", 1);
						 * viewMediaIntent.putExtra("outputX", 140);
						 * viewMediaIntent.putExtra("outputY", 140);
						 * viewMediaIntent
						 * .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						 * Intent.FLAG_ACTIVITY_SINGLE_TOP);
						 * 
						 * startActivityForResult(viewMediaIntent,
						 * CROP_REQUEST_CODE); return; }
						 */

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

					if (mimeType != null) {
						if (mimeType.contains("image")) {
							mNewBitmap = CommonUtils.getBitmapFromUri(Uri.parse(realFilePath));

							switch (requestCode - CommonUtils.GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE) 
							{
							case R.id.img_photomain:
								mButPhoto.setImageBitmap(mNewBitmap);
								mbPhotoChanged = true;
								break;

							case R.id.img_photo1:
								mButPhoto1.setImageBitmap(mNewBitmap);
								mbPhoto1 = true;
								mbPhotoChanged1 = true;
								break;

							case R.id.img_photo2:
								mButPhoto2.setImageBitmap(mNewBitmap);
								mbPhoto2 = true;
								mbPhotoChanged2 = true;
								break;

							case R.id.img_photo3:
								mButPhoto3.setImageBitmap(mNewBitmap);
								mbPhoto3 = true;
								mbPhotoChanged3 = true;
								break;

							case R.id.img_photo4:
								mButPhoto4.setImageBitmap(mNewBitmap);
								mbPhoto4 = true;
								mbPhotoChanged4 = true;
								break;
							}

							showDeletePhotoButtons();

							return;
						}
					}

					mNewBitmap = null;
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void enableDistanceView() {
		mtextDistTitle.setEnabled(mSwitchDistance.isChecked());
		mtextMinDist.setEnabled(mSwitchDistance.isChecked());
		mtextMaxDist.setEnabled(mSwitchDistance.isChecked());
		mtextCurrentDist.setEnabled(mSwitchDistance.isChecked());
		mSeekbar.setEnabled(mSwitchDistance.isChecked());
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
	{
		enableDistanceView();
	}

	private void onSave() 
	{
		final ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null)
			return;

		// check whether password matches or not
		if (meditPassword.getText().toString().length() > 0
				|| meditCPassword.getText().toString().length() > 0) {
			if (!meditPassword.getText().toString()
					.equals(meditCPassword.getText().toString())) {
				CommonUtils.createErrorAlertDialog(this, "Alert",
						"Password does not match").show();
				return;
			}
		}

		mProgressDialog = ProgressDialog.show(this, "", "Saving...");

		if (mbPhotoChanged) 
		{
			BitmapDrawable bitmapDrawable = (BitmapDrawable)mButPhoto.getDrawable();
			Bitmap bitmap = bitmapDrawable.getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] imageInByte = stream.toByteArray();
			
			ParseFile imageFile = new ParseFile("photo.jpg", imageInByte);
			currentUser.put("photo", imageFile);
			
			Bitmap bitmapThumb = bitmapDrawable.getBitmap();
			ByteArrayOutputStream streamThumb = new ByteArrayOutputStream();
			bitmapThumb.compress(Bitmap.CompressFormat.JPEG, 30, streamThumb);
			byte[] imageInByteThumb = streamThumb.toByteArray();
			
			imageFile = new ParseFile("photoThumb.jpg", imageInByteThumb);
			currentUser.put("photothumb", imageFile);
		}
		
		if (mbPhotoChanged1) 
		{
			BitmapDrawable bitmapDrawable = (BitmapDrawable)mButPhoto1.getDrawable();
			Bitmap bitmap = bitmapDrawable.getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] imageInByte = stream.toByteArray();
			
			ParseFile imageFile = new ParseFile("photo1.jpg", imageInByte);
			currentUser.put("photo1", imageFile);
		}
		
		if (mbPhotoChanged2) 
		{
			BitmapDrawable bitmapDrawable = (BitmapDrawable)mButPhoto2.getDrawable();
			Bitmap bitmap = bitmapDrawable.getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] imageInByte = stream.toByteArray();
			
			ParseFile imageFile = new ParseFile("photo2.jpg", imageInByte);
			currentUser.put("photo2", imageFile);
		}
		
		if (mbPhotoChanged3) 
		{
			BitmapDrawable bitmapDrawable = (BitmapDrawable)mButPhoto3.getDrawable();
			Bitmap bitmap = bitmapDrawable.getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] imageInByte = stream.toByteArray();
			
			ParseFile imageFile = new ParseFile("photo3.jpg", imageInByte);
			currentUser.put("photo3", imageFile);
		}
		
		if (mbPhotoChanged4) 
		{
			BitmapDrawable bitmapDrawable = (BitmapDrawable)mButPhoto4.getDrawable();
			Bitmap bitmap = bitmapDrawable.getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] imageInByte = stream.toByteArray();
			
			ParseFile imageFile = new ParseFile("photo4.jpg", imageInByte);
			currentUser.put("photo4", imageFile);
		}
		
		if (!mbPhoto1 && currentUser.getParseFile("photo1") != null)
	    {
			currentUser.remove("photo1");
	    }
	    if (!mbPhoto2 && currentUser.getParseFile("photo2") != null)
	    {
	    	currentUser.remove("photo2");
	    }
	    if (!mbPhoto3 && currentUser.getParseFile("photo3") != null)
	    {
	    	currentUser.remove("photo3");
	    }
	    if (!mbPhoto4 && currentUser.getParseFile("photo4") != null)
	    {
	    	currentUser.remove("photo4");
	    }
	    
	    // save full name
	    currentUser.put("fullname", meditFullname.getText().toString());
	    
	    // save distance
	    currentUser.put("distanceFilter", mSwitchDistance.isChecked());
	    currentUser.put("distance", mSeekbar.getProgress() + 1);
	    
	    if (mbDistanceFilterOld != mSwitchDistance.isChecked() || mnDistanceOld != mSeekbar.getProgress() + 1)
        {
	    	CommonUtils.mbNeedRefresh = true;
        }
	    
	    // save password
	    currentUser.setPassword(meditPassword.getText().toString());
	    
	    TimerTask task = new TimerTask()
        {
            @Override
            public void run() {
                runOnUiThread(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                    	if (mbPhotoUploaded && mbPhotoThumbUploaded && mbPhotoUploaded1 && mbPhotoUploaded2 && mbPhotoUploaded3 && mbPhotoUploaded4)
                        {
                            // update name in zapp, like, comments if changed
                            if (mStrOldName == null || mStrOldName.equals(currentUser.getString("fullname")))
                            {
                                // zapp
                				ParseQuery<ParseObject> query = ParseQuery.getQuery("Zapps");
                				query.whereEqualTo("user", currentUser);

                				query.findInBackground(new FindCallback<ParseObject>() 
                				{
                		            @Override
                		            public void done(final List<ParseObject> zappobjects, ParseException e) 
                		            {
                		                if (e == null) 
                		                {
                		                	for (ParseObject object : zappobjects)
                                            {
                                                object.put("username", currentUser.getString("fullname"));
                                                object.saveInBackground();
                                            }
                		                }
                		            }
                				});
                                
                                // like
                                query = ParseQuery.getQuery("Likes");
                                query.whereEqualTo("user", currentUser);
                                
                                query.findInBackground(new FindCallback<ParseObject>() 
                				{
                		            @Override
                		            public void done(final List<ParseObject> zappobjects, ParseException e) 
                		            {
                		                if (e == null) 
                		                {
                		                	for (ParseObject object : zappobjects)
                                            {
                                                object.put("username", currentUser.getString("fullname"));
                                                object.saveInBackground();
                                            }
                		                }
                		            }
                				});
                                
                                // comment
                                query = ParseQuery.getQuery("Comments");
                                query.whereEqualTo("user", currentUser);
                                
                                query.findInBackground(new FindCallback<ParseObject>() 
                				{
                		            @Override
                		            public void done(final List<ParseObject> zappobjects, ParseException e) 
                		            {
                		                if (e == null) 
                		                {
                		                	for (ParseObject object : zappobjects)
                                            {
                                                object.put("username", currentUser.getString("fullname"));
                                                object.saveInBackground();
                                            }
                		                }
                		            }
                				});
                            }
                           
                            mProgressDialog.dismiss();
                            currentUser.saveInBackground();

                            mProfileActivity.updateUserInfo();
                            onBackPressed();
                            
                            if (mSaveTimer != null)
                            {
                            	mSaveTimer.cancel();
                            	mSaveTimer.purge();
                            	mSaveTimer = null;
                            }
                        }
                    }
                });
            }
        };

        mSaveTimer = new Timer();
        mSaveTimer.scheduleAtFixedRate(task, 0, 500);
	}

}
