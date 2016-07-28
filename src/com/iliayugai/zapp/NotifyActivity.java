package com.iliayugai.zapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.iliayugai.pulltorefresh.PullToRefreshBase;
import com.iliayugai.pulltorefresh.PullToRefreshListView;
import com.iliayugai.segmentedgroup.SegmentedGroup;
import com.iliayugai.zapp.adapter.NotifyAdapter;
import com.iliayugai.zapp.data.NotifyData;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotifyActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener
{
	private static final String TAG = NotifyActivity.class.getSimpleName();
	
    private PullToRefreshListView mPullRefreshListView;
    
    private ArrayList<NotifyData> mLikeNotifyList = new ArrayList<NotifyData>();
    private NotifyAdapter mLikeNotifyAdapter;
    
    private ArrayList<NotifyData> mCommentNotifyList = new ArrayList<NotifyData>();
    private NotifyAdapter mCommentNotifyAdapter;
    
    private int mnLikeCurCount;
    private int mnCommentCurCount;
    
    private int mnNotifyType;
    private int mnCountOnce;
    
    private int mnCurNotifyNum;

    private ParseQuery<ParseObject> mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        
        initView();
        initPullToRefresh();
        
        mnNotifyType = 0;   // like
        mnLikeCurCount = mnCommentCurCount = 0;
        mnCountOnce = 10;
        
        getNotificationData();
    }

    public void initView()
    {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Config.scaleLayout(this, "notify", rootView, true);

        Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");
        TextView textTitle = (TextView)findViewById(R.id.text_title);
        textTitle.setTypeface(helveticaBoldFont);

        // segment
        SegmentedGroup segmented = (SegmentedGroup)rootView.findViewById(R.id.segmented);
        segmented.setTintColor(getResources().getColor(R.color.navigation_bar_back_color));
        segmented.setOnCheckedChangeListener(this);

        // Option Button
        RadioButton radioBut = (RadioButton)rootView.findViewById(R.id.btn_segmentlike);
        radioBut.setTypeface(helveticaBoldFont);

        radioBut = (RadioButton)rootView.findViewById(R.id.btn_segmentcomment);
        radioBut.setTypeface(helveticaBoldFont);

        // Button
        ImageView imageView = (ImageView)rootView.findViewById(R.id.but_back);
        imageView.setOnClickListener(this);
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

                getNotificationData();
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

                getNotificationData();
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
        
        mLikeNotifyAdapter = new NotifyAdapter(this, mLikeNotifyList);
        mCommentNotifyAdapter = new NotifyAdapter(this, mCommentNotifyList);

        // Get actual ListView
        final ListView feedListView = mPullRefreshListView.getRefreshableView();

        // You can also just use setListAdapter(mAdapter) or
        // mPullRefreshListView.setAdapter(mFeedAdapter);
        feedListView.setAdapter(mLikeNotifyAdapter);
        
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

    private void getNotificationData()
    {
    	if (mQuery != null)
    	{
    		try
    		{
    			mQuery.cancel();
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
        
        if (mnNotifyType == 0)  // like
        {
        	mQuery = ParseQuery.getQuery("Likes");
        	mQuery.whereEqualTo("targetuser", ParseUser.getCurrentUser());
        	mQuery.orderByDescending("updatedAt");

        	mQuery.setLimit(mnCountOnce);
        	mQuery.setSkip(mnLikeCurCount);
            
            Log.d(TAG, "notify like before query");
            
            mQuery.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> objects, ParseException e) 
				{
					Log.d(TAG, "notify like after query");
					
					if (e == null) 
	                {
						if (mnLikeCurCount == 0)
	                    {
							mLikeNotifyList.clear();
//							[self removeFooterView];
	                    }
						
						for (final ParseObject obj : objects) 
	                    {
							final NotifyData notifyData = new NotifyData(obj);
							notifyData.notificationType = mnNotifyType;
							mLikeNotifyList.add(notifyData);
	                    }
						
						mnLikeCurCount += objects.size();
						
						mLikeNotifyAdapter.notifyDataSetChanged();
	                    mPullRefreshListView.onRefreshComplete();
	                    
	                    if (mnLikeCurCount > mnCountOnce)
	                    {
//	                    	[self testFinishedLoadData];
	                    }	                    
	                }
					else
					{
						CommonUtils.createErrorAlertDialog(NotifyActivity.this, "Alert", e.getMessage()).show();
					}
					
					mQuery = null;
				}            	
            });
        }
        else    // comment
        {
        	mQuery = ParseQuery.getQuery("Comments");
        	mQuery.whereEqualTo("targetuser", ParseUser.getCurrentUser());
        	mQuery.orderByDescending("updatedAt");

        	mQuery.setLimit(mnCountOnce);
        	mQuery.setSkip(mnCommentCurCount);
            
        	mQuery.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> objects, ParseException e) 
				{
					if (e == null) 
	                {
						if (mnCommentCurCount == 0)
	                    {
							mCommentNotifyList.clear();
//							[self removeFooterView];
	                    }
						
						for (final ParseObject obj : objects) 
	                    {
							final NotifyData notifyData = new NotifyData(obj);
							notifyData.notificationType = mnNotifyType;
							mCommentNotifyList.add(notifyData);
	                    }
						
						mnCommentCurCount += objects.size();
						
						mCommentNotifyAdapter.notifyDataSetChanged();
	                    mPullRefreshListView.onRefreshComplete();
	                    
	                    if (mnCommentCurCount > mnCountOnce)
	                    {
//	                    	[self testFinishedLoadData];
	                    }	                    
	                }
					else
					{
						CommonUtils.createErrorAlertDialog(NotifyActivity.this, "Alert", e.getMessage()).show();
					}
					
					mQuery = null;
				}            	
            });
        }
    }


    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.but_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId)
    {        
    	final ListView feedListView = mPullRefreshListView.getRefreshableView();
    	
    	switch (checkedId)
        {
            case R.id.btn_segmentlike:
            	mnNotifyType = 0;
            	mnLikeCurCount = 0;
            	feedListView.setAdapter(mLikeNotifyAdapter);
                break;

            case R.id.btn_segmentcomment:
            	mnNotifyType = 1;
            	mnCommentCurCount = 0;
            	feedListView.setAdapter(mCommentNotifyAdapter);
                break;
        }
    	    	
        getNotificationData();
    }
}
