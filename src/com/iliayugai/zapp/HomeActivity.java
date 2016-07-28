package com.iliayugai.zapp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.iliayugai.pulltorefresh.PullToRefreshBase;
import com.iliayugai.pulltorefresh.PullToRefreshListView;
import com.iliayugai.segmentedgroup.SegmentedGroup;
import com.iliayugai.zapp.adapter.ZappAdapter;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.view.CircularSeekBar;
import com.iliayugai.zapp.R;

import com.parse.FindCallback;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class HomeActivity extends Activity implements 	View.OnClickListener, 
														RadioGroup.OnCheckedChangeListener
{
    private final String TAG = HomeActivity.class.getSimpleName();

    private ZappAdapter mZappAdapter;
    private PullToRefreshListView mPullRefreshListView;
    private RelativeLayout mLayoutTitlebar;
    private RelativeLayout mLayoutShare;
    public int mnCurZappNum;

    private View mViewPopupmask;

    private int mnZappType = ZappData.HOME_ALL;
    private int m_nShareViewHeight;

    private RelativeLayout mRecordLayout;
    private TextView mTextRecordSecond;

    private float mfElapsed = 0.0f;

    private Timer mRecordTimer;
    private CircularSeekBar mSeekbar;
    private AudioRecord mRecorder = null;

    // zapp data
    private int mnCountOnce;
    private int mnCurrentCount;
    
    private ProgressDialog mProgressDialog;
    
    // sharing Facebook
//    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();
        
        initView();
        initPullToRefresh();
        initShareLayout();
        
        initLocation();
        
        CommonUtils utils = CommonUtils.sharedObject();
    	utils.initMp3Encoder();
    	
    	CommonUtils.mbNeedRefresh = false;
    	
    	// zapp data
    	mnCountOnce = 5;
        mnCurrentCount = 0;
        
        getZapps(true);

        
        
        // share facebook
//        uiHelper = new UiLifecycleHelper(this, null);
//        uiHelper.onCreate(savedInstanceState);
    }
    
    private void initLocation()
    {
    	LocationManager locationManager;   
    	String serviceName = Context.LOCATION_SERVICE;
    	
    	locationManager = (LocationManager)getSystemService(serviceName);
    	
    	boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    	Location location = null;
    	String provider = "";
    	
    	if (isGpsEnabled)
    	{
	    	// gps
	    	provider = LocationManager.GPS_PROVIDER;
	    	
	//    	Criteria criteria = new Criteria();
	//    	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	//    	criteria.setAltitudeRequired(false);
	//    	criteria.setBearingRequired(false);
	//    	criteria.setCostAllowed(false);
	//    	criteria.setPowerRequirement(Criteria.POWER_LOW);
	    	
	//    	String provider = locationManager.getBestProvider(criteria, true);
	    	location = locationManager.getLastKnownLocation(provider);	    	
    	}
    	
    	if (location == null && isNetworkEnabled)
    	{
    		provider = LocationManager.NETWORK_PROVIDER;
    		location = locationManager.getLastKnownLocation(provider);
    	}
    	
    	updateWithNewLocation(location);
    	
        if (provider.length() > 0)
        {
        	locationManager.requestLocationUpdates (provider, 200000, 100, mLocationListener);
        }
    }
    
    private final LocationListener mLocationListener = new LocationListener() 
    {   
    	public void onLocationChanged(Location location) 
    	{
    		updateWithNewLocation(location);   
    	}   

    	public void onProviderDisabled(String provider)
    	{   
    		updateWithNewLocation(null);   
    	}   

    	public void onProviderEnabled(String provider){ }   

    	public void onStatusChanged(String provider, int status, Bundle extras){ }   
    };   
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if (requestCode == com.facebook.Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE)
    	{
    		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    	}
    	
//    	uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() 
//    	{
//            @Override
//            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) 
//            {
//                Log.e("Activity", String.format("Error: %s", error.toString()));
//            }
//
//            @Override
//            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) 
//            {
//                Log.i("Activity", "Success!");
//            }
//        });
    	
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void updateWithNewLocation(Location location) 
    {   
//    	String latLongString;   

    	if (location != null) 
    	{   
    		CommonUtils.currentLocation = location;
//    		double lat = location.getLatitude();   
//    		double lng = location.getLongitude();   
//
//    		latLongString = "纬度:" + lat + "\n经度:" + lng;
    		
    		ParseGeoPoint currentPoint = CommonUtils.geoPointFromLocation(CommonUtils.currentLocation);
    		ParseUser currentUser = ParseUser.getCurrentUser();
    		
    		currentUser.put("location", currentPoint);
    		currentUser.saveInBackground();
    	} 
    	else 
    	{   
//    		latLongString = "Can't get location info";
    		Toast.makeText(HomeActivity.this, "Can't get location info", Toast.LENGTH_LONG).show();
    	}   

//    	Toast.makeText(HomeActivity.this, "您当前的位置是: " + latLongString, Toast.LENGTH_SHORT).show();
    }   
    
    @Override
	protected void onStart() 
    {
    	if (CommonUtils.mbNeedRefresh)
        {
            mnCurrentCount = 0;
            getZapps(true);
        }
        else
        {
            if (CommonUtils.mZappList.size() > 0)
            {
            	// query like and comment
            	for (ZappData zdata : CommonUtils.mZappList)
            	{
            		CommonUtils.getLikeCommentInfo(zdata, mZappAdapter);
            	}
                mnCurrentCount = (int)CommonUtils.mZappList.size();
            }
        }

		super.onStart();
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		
//		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() 
	{
		super.onPause();

		cancelQuery();
//		uiHelper.onPause();
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
//		uiHelper.onResume();
	}

	@Override
	protected void onDestroy() 
    {
    	if (mRecorder != null)
    	{
    		mRecorder.release();
    	}
    	
    	CommonUtils utils = CommonUtils.sharedObject();
    	CommonUtils.mZappList.clear();
    	utils.destroyEncoder();
    	
		super.onDestroy();
		
//		uiHelper.onDestroy();
	}

	private void stopRecording()
    {
        if (mRecorder != null)
        {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        CommonUtils.mbRecording = false;
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.image_profile:
            	ProfileActivity.mUser = ParseUser.getCurrentUser();
                CommonUtils.moveNextActivity(this, ProfileActivity.class, false);
                break;

            case R.id.image_notify:            	
                CommonUtils.moveNextActivity(this, NotifyActivity.class, false);
                break;
                
            case R.id.but_share_facebook:
            	
            	CommonUtils.shareToFacebook(this, null);            	            	
            	hideShareView();        
            	break;
            	
            case R.id.but_share_twitter:
            	
            	CommonUtils.shareToTwitter(this);            	            	
            	hideShareView();        
            	break;

            case R.id.but_share_email:
            	break;
            	
            case R.id.but_share_cancel:
                hideShareView();
                break;

            case R.id.image_record:
                mRecordLayout.setVisibility(View.VISIBLE);

                mfElapsed = 0.0f;

                if (mRecordTimer == null)
                {
                    TimerTask task = new TimerTask()
                    {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() 
                            {
                                @Override
                                public void run() 
                                {
                                    mfElapsed += 0.01f;
                                    
                                    mTextRecordSecond.setText(String.format("%.02fs", Math.min(mfElapsed, 10)));

                                    int nAngle = (int)(240 / 10.0f * mfElapsed);
                                    mSeekbar.setAngle(nAngle);

                                    if (mfElapsed >= 10 || !CommonUtils.mbRecording)
                                    {
                                        Log.d(TAG, "Time is up");
                                        stopRecording();

                                        if (mRecordTimer != null)
                                        {
	                                        mRecordTimer.cancel();
	                                        mRecordTimer.purge();
	                                        mRecordTimer = null;
                                        }
                                    }
                                }
                            });
                        }
                    };

                    mRecordTimer = new Timer();
                    mRecordTimer.scheduleAtFixedRate(task, 0, 10);
                }

                stopRecording();
                CommonUtils.mbRecording = true;

//                mRecorder = new MediaRecorder();
//                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//                mRecorder.setOutputFile(CommonUtils.getRecordFileName());
//                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

//                try
//                {
//                    mRecorder.prepare();
//                }
//                catch (IOException e)
//                {
//                    Log.e(TAG, "prepare() failed");
//                }
//
//                mRecorder.start();

                int nMinBufferSize = AudioRecord.getMinBufferSize(CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, nMinBufferSize);
                CommonUtils.startBufferedWrite(mRecorder, this);

                break;

            case R.id.image_record_ok:

                stopRecording();

                mRecordLayout.setVisibility(View.GONE);
                showPostView();
                break;

            case R.id.image_record_cancel:

                stopRecording();

                mRecordLayout.setVisibility(View.GONE);
                break;
        }
    }

    
    private void showPostView()
    {
        final PostDialog dialog = new PostDialog(this, R.style.postDialog, R.layout.layout_post_dialog);
        dialog.show();
        dialog.setOnDismissListener(new OnDismissListener()
        {
			@Override
			public void onDismiss(DialogInterface arg0) 
			{
				if (dialog.mbResult)
				{
					mZappAdapter.notifyDataSetChanged();
				}
			}
        });
    }

    public void initView()
    {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Config.mHeightScaleFactor /= 1.3f;
        Config.scaleLayout(this, "home", rootView, true);
        Config.mHeightScaleFactor *= 1.3f;

        // title bar
        mLayoutTitlebar = (RelativeLayout)rootView.findViewById(R.id.layout_title_bar);
        mLayoutTitlebar.setBackgroundColor(getResources().getColor(R.color.navigation_bar_back_color));

        // segment
        SegmentedGroup segmented = (SegmentedGroup)rootView.findViewById(R.id.segmented);
        segmented.setTintColor(Color.WHITE, getResources().getColor(R.color.navigation_bar_back_color));
        segmented.setOnCheckedChangeListener(this);

        // set font
        Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");
        Typeface helveticaFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");

        // Option Button
        RadioButton radioBut = (RadioButton)rootView.findViewById(R.id.btn_alert);
        radioBut.setTypeface(helveticaBoldFont);

        radioBut = (RadioButton)rootView.findViewById(R.id.btn_all);
        radioBut.setTypeface(helveticaBoldFont);

        radioBut = (RadioButton)rootView.findViewById(R.id.btn_fun);
        radioBut.setTypeface(helveticaBoldFont);

        // Button
        ImageView imageView = (ImageView)rootView.findViewById(R.id.image_profile);
        imageView.setOnClickListener(this);

        imageView = (ImageView)rootView.findViewById(R.id.image_notify);
        imageView.setOnClickListener(this);

        // view mask
        mViewPopupmask = (View)rootView.findViewById(R.id.view_popupmask);

        imageView = (ImageView)rootView.findViewById(R.id.image_record);
        imageView.setOnClickListener(this);

        mRecordLayout = (RelativeLayout)rootView.findViewById(R.id.layout_record);
        mTextRecordSecond = (TextView)rootView.findViewById(R.id.text_second);
        mTextRecordSecond.setTypeface(helveticaFont);

        imageView = (ImageView)rootView.findViewById(R.id.image_record_ok);
        imageView.setOnClickListener(this);

        imageView = (ImageView)rootView.findViewById(R.id.image_record_cancel);
        imageView.setOnClickListener(this);

//        mPostLayout = (RelativeLayout)rootView.findViewById(R.id.layout_post);

        mSeekbar = (CircularSeekBar)findViewById(R.id.rec_seeker);
    }

    private void initPullToRefresh()
    {
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>()
        {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
            {                
            	mnCurrentCount = 0;
                getZapps(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
            {
//                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
//                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
//
//                // Update the LastUpdatedLabel
//                fillView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // Do work to refresh the list here.
                //new GetDataTask().execute();
//                getBlog(false);

                getZapps(false);
            }
        });


        // Add an end-of-list listener
//        mPullRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
//
//            @Override
//            public void onLastItemVisible() {
//                Toast.makeText(HomeActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
//            }
//        });

        // Get actual ListView
        final ListView feedListView = mPullRefreshListView.getRefreshableView();
        feedListView.setVerticalFadingEdgeEnabled(false);

        mZappAdapter = new ZappAdapter(this, CommonUtils.mZappList);

        // You can also just use setListAdapter(mAdapter) or
        // mPullRefreshListView.setAdapter(mFeedAdapter);
        feedListView.setAdapter(mZappAdapter);

        feedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                hideMoreView();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void initShareLayout()
    {
        m_nShareViewHeight = (int)(getResources().getDimension(R.dimen.share_layout_root_height) * Config.mWidthScaleFactor);

        mLayoutShare = (RelativeLayout)findViewById(R.id.layout_share_root);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mLayoutShare.getLayoutParams();
        params.height = m_nShareViewHeight;
        mLayoutShare.setLayoutParams(params);

        int nHeight = (int)(getResources().getDimension(R.dimen.share_but_facebook_height) * Config.mWidthScaleFactor);
        int nPadding = (int)(getResources().getDimension(R.dimen.share_but_facebook_paddingBottom) * Config.mWidthScaleFactor);

        // facebook
        int nMargin = (int)(getResources().getDimension(R.dimen.share_but_facebook_topMargin) * Config.mWidthScaleFactor);
        ImageView imageView = (ImageView)findViewById(R.id.but_share_facebook);
        imageView.setPadding(imageView.getPaddingLeft(), nPadding, imageView.getPaddingRight(), nPadding);
        params = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
        params.topMargin = nMargin;
        params.height = nHeight;
        imageView.setLayoutParams(params);
        imageView.setOnClickListener(this);

        // line
        nMargin = (int)(getResources().getDimension(R.dimen.share_view_hline1_topMargin) * Config.mWidthScaleFactor);
        int nWidth = (int)(getResources().getDimension(R.dimen.share_view_hline1_width) * Config.mWidthScaleFactor);
        View viewLine = (View)findViewById(R.id.view_share_hline1);
        params = (RelativeLayout.LayoutParams)viewLine.getLayoutParams();
        params.topMargin = nMargin;
        params.width = nWidth;
        viewLine.setLayoutParams(params);

        // twitter
        nMargin = (int)(getResources().getDimension(R.dimen.share_but_twitter_topMargin) * Config.mWidthScaleFactor);
        imageView = (ImageView)findViewById(R.id.but_share_twitter);
        imageView.setPadding(imageView.getPaddingLeft(), nPadding, imageView.getPaddingRight(), nPadding);
        params = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
        params.topMargin = nMargin;
        params.height = nHeight;
        imageView.setLayoutParams(params);
        imageView.setOnClickListener(this);

        // line
        nMargin = (int)(getResources().getDimension(R.dimen.share_view_hline2_topMargin) * Config.mWidthScaleFactor);
        viewLine = (View)findViewById(R.id.view_share_hline2);
        params = (RelativeLayout.LayoutParams)viewLine.getLayoutParams();
        params.topMargin = nMargin;
        params.width = nWidth;
        viewLine.setLayoutParams(params);

        // email
        nMargin = (int)(getResources().getDimension(R.dimen.share_but_email_topMargin) * Config.mWidthScaleFactor);
        imageView = (ImageView)findViewById(R.id.but_share_email);
        imageView.setPadding(imageView.getPaddingLeft(), nPadding, imageView.getPaddingRight(), nPadding);
        params = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
        params.topMargin = nMargin;
        params.height = nHeight;
        imageView.setLayoutParams(params);
        imageView.setOnClickListener(this);

        // line
        nMargin = (int)(getResources().getDimension(R.dimen.share_view_hline3_topMargin) * Config.mWidthScaleFactor);
        viewLine = (View)findViewById(R.id.view_share_hline3);
        params = (RelativeLayout.LayoutParams)viewLine.getLayoutParams();
        params.topMargin = nMargin;
        params.width = nWidth;
        viewLine.setLayoutParams(params);

        // link
        nMargin = (int)(getResources().getDimension(R.dimen.share_but_link_topMargin) * Config.mWidthScaleFactor);
        imageView = (ImageView)findViewById(R.id.but_share_link);
        imageView.setPadding(imageView.getPaddingLeft(), nPadding, imageView.getPaddingRight(), nPadding);
        params = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
        params.topMargin = nMargin;
        params.height = nHeight;
        imageView.setLayoutParams(params);
        imageView.setOnClickListener(this);

        // line
        nMargin = (int)(getResources().getDimension(R.dimen.share_view_hline4_topMargin) * Config.mWidthScaleFactor);
        viewLine = (View)findViewById(R.id.view_share_hline4);
        params = (RelativeLayout.LayoutParams)viewLine.getLayoutParams();
        params.topMargin = nMargin;
        params.width = nWidth;
        viewLine.setLayoutParams(params);

        // cancel
        nMargin = (int)(getResources().getDimension(R.dimen.share_but_cancel_topMargin) * Config.mWidthScaleFactor);
        Button button = (Button)findViewById(R.id.but_share_cancel);
        params = (RelativeLayout.LayoutParams)button.getLayoutParams();
        params.topMargin = nMargin;
        params.height = nHeight;
        button.setLayoutParams(params);
        button.setOnClickListener(this);

        // set font
        float fFontSize = getResources().getDimension(R.dimen.share_but_cancel_font_size) * Config.mFontScaleFactor;
        Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");
        button.setTypeface(helveticaBoldFont);
        button.setTextSize(fFontSize);

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId)
    {
        switch (checkedId)
        {
            case R.id.btn_alert:
                mnZappType = ZappData.HOME_ALERT;
                mLayoutTitlebar.setBackgroundColor(getResources().getColor(R.color.navigation_bar_alert_back_color));
                break;

            case R.id.btn_all:
                mnZappType = ZappData.HOME_ALL;
                mLayoutTitlebar.setBackgroundColor(getResources().getColor(R.color.navigation_bar_back_color));
                break;

            case R.id.btn_fun:
                mnZappType = ZappData.HOME_FUN;
                mLayoutTitlebar.setBackgroundColor(getResources().getColor(R.color.navigation_bar_fun_back_color));
                break;
        }
        
        mnCurrentCount = 0;
        getZapps(true);
    }

    public void showShareView()
    {
        AlphaAnimation animAlpha = new AlphaAnimation(0f, 0.5f);
        animAlpha.setDuration(300);
        animAlpha.setFillAfter(true);

        mViewPopupmask.startAnimation(animAlpha);
        mViewPopupmask.setVisibility(View.VISIBLE);
        mViewPopupmask.setOnClickListener(this);

        // show share view
        ObjectAnimator upmove = ObjectAnimator.ofFloat(mLayoutShare, "translationY", m_nShareViewHeight, 0f);
        upmove.setDuration(300);
        upmove.setInterpolator(new LinearInterpolator());
        upmove.start();
        mLayoutShare.setVisibility(View.VISIBLE);
    }

    public void hideShareView()
    {
        AlphaAnimation animAlpha = new AlphaAnimation(0.5f, 0f);
        animAlpha.setDuration(300);

        mViewPopupmask.startAnimation(animAlpha);
        mViewPopupmask.setVisibility(View.GONE);
        mViewPopupmask.setOnClickListener(null);

        // show share view
        ObjectAnimator downmove = ObjectAnimator.ofFloat(mLayoutShare, "translationY", 0f, m_nShareViewHeight);
        downmove.setDuration(200);
        downmove.setInterpolator(new LinearInterpolator());
        downmove.addListener(moreViewCollapseListener);
        downmove.start();
    }

    AnimatorListener moreViewCollapseListener = new AnimatorListener()
    {
        @Override
        public void onAnimationStart(Animator animator)
        {
        }

        @Override
        public void onAnimationEnd(android.animation.Animator animator)
        {
            mLayoutShare.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel(Animator animator)
        {
        }

        @Override
        public void onAnimationRepeat(Animator animator)
        {
        }
    };
    
    /**
     * Get Zapps from parse.com
     */
    private void getZapps(final boolean isShowLoading) 
    {
        if (isShowLoading) 
        {
        	mProgressDialog = ProgressDialog.show(this, "", "Loading Zapps...");
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Zapps");
        
        if (mnZappType == ZappData.HOME_ALERT)
        {        	
        	query.whereEqualTo("type", 0);
        }
        else if (mnZappType == ZappData.HOME_FUN)
        {
        	query.whereEqualTo("type", 1);
        }
        
        ParseUser currentUser = ParseUser.getCurrentUser();
        
        if (currentUser.getBoolean("distanceFilter"))
        {
        	// Query for posts sort of kind of near our current location.
        	ParseGeoPoint point = CommonUtils.geoPointFromLocation(CommonUtils.currentLocation);
            query.whereWithinKilometers("location", point, currentUser.getDouble("distance"));
        }
        else
        {
        	query.setLimit(mnCountOnce);
        	query.setSkip(mnCurrentCount);
        }
        
        query.orderByDescending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> zappObjectList, ParseException e) 
            {
                if (e == null) 
                {
                    if (mnCurrentCount == 0) 
                    {
                    	cancelQuery();
                    	CommonUtils.mZappList.clear();
                        //if (blogObjectList.size() > 0) mFeedListView.setVisibility(View.VISIBLE);
                    }

                    // set parent objects
                    for (final ParseObject zappObject : zappObjectList) 
                    {
                        boolean bDuplicated = false;

                        // check whether duplicates
                        for (ZappData zData : CommonUtils.mZappList) 
                        {
                            if (zData.strId.equals(zappObject.getObjectId())) 
                            {
                                bDuplicated = true;
                                break;
                            }
                        }

                        if (bDuplicated) 
                        {
                            continue;
                        }

                        final ZappData zapp = new ZappData(zappObject);
                        
                        CommonUtils.getLikeCommentInfo(zapp, mZappAdapter);

                        CommonUtils.mZappList.add(zapp);
                    }
                    
                    // remove header if necessary
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (!currentUser.getBoolean("distanceFilter"))
                    {
                    	mnCurrentCount += zappObjectList.size();
                    }

                    mZappAdapter.notifyDataSetChanged();                    
                    mPullRefreshListView.onRefreshComplete();
                } 
                else 
                {
                    CommonUtils.createErrorAlertDialog(HomeActivity.this, "Alert", e.getMessage()).show();
                }
                
                if (isShowLoading)
                {
                	mProgressDialog.dismiss();
                }
            }
        });
        
        CommonUtils.mbNeedRefresh = false;
    }
    
    public void cancelQuery()
    {
    	for (ZappData zdata : CommonUtils.mZappList)
    	{
    		if (zdata.mQuery != null)
    		{
    			try
    			{
    				zdata.mQuery.cancel();
    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
}
