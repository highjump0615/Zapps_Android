package com.iliayugai.zapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.iliayugai.pulltorefresh.PullToRefreshBase;
import com.iliayugai.pulltorefresh.PullToRefreshListView;
import com.iliayugai.zapp.adapter.ZappAdapter;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.viewpager_indicator.PhotoFragmentAdapter;
import com.iliayugai.zapp.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends FragmentActivity implements View.OnClickListener
{
	private final String TAG = ProfileActivity.class.getSimpleName();
	
    private PullToRefreshListView mPullRefreshListView;

    private PhotoFragmentAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private int mCurPage;

    private ImageView mbutMine;
    private ImageView mbutSetting;
    private ImageView mbutLiked;

    private int mnMode = 0; // 0: Mine, 1: Liked
    private TextView mTextTitle;
    private TextView mTextMineNum;
    private TextView mTextLikedNum;
    
    public static ParseUser mUser;
    
    private int mnCountOnce = 0;
    private int mnCurrentMineCount;
    private int mnCurrentLikedCount;
    private int mnLikedCount;
    private int mnMineCount;
    
    private ArrayList<ZappData> mMineZapps = new ArrayList<ZappData>();
    private ZappAdapter mMineZappAdapter;
    
    private ArrayList<ZappData> mLikedZapps = new ArrayList<ZappData>();
    private ZappAdapter mLikedZappAdapter;

    private ProgressDialog mProgressDialog;
    private boolean mbReady;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        initView();
        initPullToRefresh();
        
        mnCountOnce = 5;
        mnCurrentMineCount = 0;
        mnCurrentLikedCount = 0;
        mnLikedCount = mnMineCount = 0;
        
        updateUserInfo();
        
        if (mUser != ParseUser.getCurrentUser())
        {
        	mbutSetting.setVisibility(View.GONE);
        }
        
        getMineBlog();
        getLikedBlog();
        
        mRunnable = new Runnable()
        {
            @Override
            public void run()
            {
            	if (mAdapter != null)
            	{
	                final int pageCount = mAdapter.getCount();
	                
	                if (mCurPage < pageCount)
	                {
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
								mPager.setCurrentItem(mCurPage++, true);
	                        }
	                    });
	                }
	                else
	                {
	                    mCurPage = 0;
	                }
            	}
            	
                mHandler.postDelayed(mRunnable, 5000);
            }
        };
        mRunnable.run();

    }
    
    private void initCountData()
    {
    	if (mbReady)
        {
            // get mine and liked count
            mnMineCount = mUser.getInt("zappcount");
            updateLikeMineCount(false);

            ParseQuery<ParseObject> queryCount = ParseQuery.getQuery("Likes");
	        queryCount.whereEqualTo("user", mUser);
	        queryCount.findInBackground(new FindCallback<ParseObject>() {
	            @Override
	            public void done(final List<ParseObject> objects, ParseException e) 
	            {
	            	mnLikedCount = objects.size();
		            updateLikeMineCount(false);
	            }
	        });

	        onChangeOption();
        }
    }
    
    @Override
	protected void onStart() 
    {
    	initCountData();
		super.onStart();
	}
    
    @Override
	protected void onPause() 
    {		
		super.onPause();
		
		Log.d(TAG, "onPause");
		cancelQuery();
	}

	private void updateTable()
    {
        if (mbReady)
        {
        	mMineZappAdapter.notifyDataSetChanged();
        	mLikedZappAdapter.notifyDataSetChanged();
        }
    }

    private void updateProfilePage()
    {
    	mTextTitle.setText(CommonUtils.getUserNameToShow(mUser));
    	
    	// set up photo view
    	mAdapter = new PhotoFragmentAdapter(getSupportFragmentManager());
    	
    	for (int i = 0; i < 5; i++)
    	{
    		String photoFileName = "photo";
    		
    		if (i > 0)
    		{
    			photoFileName += i;
    		}
    		
    		ParseFile photoFile = mUser.getParseFile(photoFileName);
    		if (photoFile != null)
	        {
	    		mAdapter.mUrlList.add(photoFile);
	        }
    	}
    	
    	mAdapter.mCount = mAdapter.mUrlList.size();

        mPager = (ViewPager)findViewById(R.id.pager_photo);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator_photo);
        mIndicator.setViewPager(mPager);

        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });

        mCurPage = 0;
    }

	public void updateUserInfo()
    {
    	if (mUser.getCreatedAt() != null)	// fetched
    	{
    		mbReady = true;
    		updateProfilePage();
    	}
    	else
    	{
    		mbReady = false;
    		
    		mProgressDialog = ProgressDialog.show(this, "", "Loading User info...");
    		
    		mUser.fetchIfNeededInBackground(new GetCallback<ParseObject>() 
    		{
				@Override
				public void done(ParseObject parseObject, ParseException e) 
				{
					if (e == null)
					{
						updateProfilePage();
						mProgressDialog.dismiss();
						
						mbReady = true;
						
						initCountData();
					}
				}
    		});
    		
    	}
    }
    
    private void updateLikeMineCount(boolean bSave)
    {
        mTextMineNum.setText(String.format("%d", mnMineCount));
        mTextLikedNum.setText(String.format("%d", mnLikedCount));
        
        if (bSave && mUser == ParseUser.getCurrentUser())
        {
        	mUser.put("zappcount", mnMineCount);
            mUser.saveInBackground();
        }
    }


    public void initView()
    {
//        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Config.scaleLayout(this, "profile", findViewById(R.id.layout_root), false);

        Typeface helveticaFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");
        Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");
        
        mTextTitle = (TextView)findViewById(R.id.text_title);
        mTextTitle.setTypeface(helveticaBoldFont);
        
        mTextMineNum = (TextView)findViewById(R.id.text_minenum);
        mTextMineNum.setTypeface(helveticaFont);
        
        mTextLikedNum = (TextView)findViewById(R.id.text_likenum);
        mTextLikedNum.setTypeface(helveticaFont);

        // Button
        ImageView imageView = (ImageView)findViewById(R.id.but_zapp);
        imageView.setOnClickListener(this);

        mbutSetting = (ImageView)findViewById(R.id.but_setting);
        mbutSetting.setOnClickListener(this);

        mbutMine = (ImageView)findViewById(R.id.but_optionmine);
        mbutMine.setOnClickListener(this);

        mbutLiked = (ImageView)findViewById(R.id.but_optionliked);
        mbutLiked.setOnClickListener(this);
    }

    private void onChangeOption()
    {
        // stop current play
//        CommonUtils *utils = [CommonUtils sharedObject];
//        [utils stopCurrentPlaying];
        
    	final ListView feedListView = mPullRefreshListView.getRefreshableView();
    	
        if (mnMode == 0)
        {
            mbutMine.setImageResource(R.drawable.profile_mine_on_but);
            mbutLiked.setImageResource(R.drawable.btn_profile_liked_bg);
            
            feedListView.setAdapter(mMineZappAdapter);
            
            for (ZappData zdata : mMineZapps)
        	{
        		CommonUtils.getLikeCommentInfo(zdata, mMineZappAdapter);
        	}
        }
        else
        {
            mbutLiked.setImageResource(R.drawable.profile_liked_on_but);
            mbutMine.setImageResource(R.drawable.btn_profile_mine_bg);
            
            feedListView.setAdapter(mLikedZappAdapter);
            
            for (ZappData zdata : mLikedZapps)
        	{
        		CommonUtils.getLikeCommentInfo(zdata, mLikedZappAdapter);
        	}
        }
    }

    private void initPullToRefresh()
    {

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>()
        {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
            {
//                m_nCurrentCount = 0;
//                //new GetDataTask().execute();
//                getBlog(false);

            	if (mnMode == 0)    // mine
                {
                    mnCurrentMineCount = 0;
                    getMineBlog();
                }
                else
                {
                    mnCurrentLikedCount = 0;
                    getLikedBlog();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
            {
            	if (mnMode == 0)    // mine
                {
                    getMineBlog();
                }
                else
                {
                    getLikedBlog();
                }
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
        
        mMineZappAdapter = new ZappAdapter(this, mMineZapps);
        mLikedZappAdapter = new ZappAdapter(this, mLikedZapps);

        // Get actual ListView
        final ListView feedListView = mPullRefreshListView.getRefreshableView();

        // You can also just use setListAdapter(mAdapter) or
        // mPullRefreshListView.setAdapter(mFeedAdapter);
        feedListView.setAdapter(mMineZappAdapter);
        feedListView.setVerticalFadingEdgeEnabled(false);

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

    private void getMineBlog()
    {
    	ParseQuery<ParseObject> query = ParseQuery.getQuery("Zapps");
    	query.orderByDescending("createdAt");
    	query.whereEqualTo("user", mUser);
    	
    	query.setLimit(mnCountOnce);
        query.setSkip(mnCurrentMineCount);
    	
    	query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> zappObjectList, ParseException e) 
            {
                if (e == null) 
                {
                    if (mnCurrentMineCount == 0) 
                    {
                    	mMineZapps.clear();
                        //if (blogObjectList.size() > 0) mFeedListView.setVisibility(View.VISIBLE);
                    }

                    // set parent objects
                    for (final ParseObject zappObject : zappObjectList) 
                    {
                        final ZappData zapp = new ZappData(zappObject);
                        
                        CommonUtils.getLikeCommentInfo(zapp, mMineZappAdapter);
                        mMineZapps.add(zapp);
                    }
                    
                    mMineZappAdapter.notifyDataSetChanged();                    
                    mnCurrentMineCount += zappObjectList.size();
                    
//                    [self setFooter];
                    mPullRefreshListView.onRefreshComplete();
                } 
                else 
                {
                    CommonUtils.createErrorAlertDialog(ProfileActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });

    }
    
    private void getLikedBlog()
    {
    	ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
    	query.orderByDescending("createdAt");
    	query.whereEqualTo("user", mUser);
    	query.include("zapp");
    	
    	query.setLimit(mnCountOnce);
        query.setSkip(mnCurrentLikedCount);
    	
    	query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> zappObjectList, ParseException e) 
            {
                if (e == null) 
                {
                    if (mnCurrentLikedCount == 0) 
                    {
                    	mLikedZapps.clear();
                        //if (blogObjectList.size() > 0) mFeedListView.setVisibility(View.VISIBLE);
                    }

                    // set parent objects
                    for (final ParseObject zappObject : zappObjectList) 
                    {
                        final ZappData zapp = new ZappData(zappObject.getParseObject("zapp"));
                        
                        CommonUtils.getLikeCommentInfo(zapp, mLikedZappAdapter);
                        mLikedZapps.add(zapp);
                    }
                    
                    mLikedZappAdapter.notifyDataSetChanged();                    
                    mnCurrentLikedCount += zappObjectList.size();
                    
//                    [self setFooter];
                    mPullRefreshListView.onRefreshComplete();
                } 
                else 
                {
                    CommonUtils.createErrorAlertDialog(ProfileActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });
    }


    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.but_setting:
            	SettingActivity.mProfileActivity = this;
                this.startActivity(new Intent(this, SettingActivity.class));
                this.overridePendingTransition(R.anim.anim_in_vert, R.anim.anim_out_vert);
                break;

            case R.id.but_zapp:
                onBackPressed();
                break;

            case R.id.but_optionmine:
                mnMode = 0;
                onChangeOption();
                break;

            case R.id.but_optionliked:
                mnMode = 1;
                onChangeOption();
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    public void cancelQuery()
    {
    	for (ZappData zdata : mMineZapps)
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
    	
    	for (ZappData zdata : mLikedZapps)
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
