package com.iliayugai.zapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.pulltorefresh.PullToRefreshBase;
import com.iliayugai.pulltorefresh.PullToRefreshListView;
import com.iliayugai.segmentedgroup.SegmentedGroup;
import com.iliayugai.zapp.adapter.CommentAdapter;
import com.iliayugai.zapp.adapter.NotifyAdapter;
import com.iliayugai.zapp.data.CommentData;
import com.iliayugai.zapp.data.NotifyData;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.view.ZappListViewHolder;
import com.iliayugai.zapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

public class CommentActivity extends Activity implements View.OnClickListener, EditText.OnEditorActionListener
{
	private final String TAG = HomeActivity.class.getSimpleName();
	
    private ListView mListView;
    private ArrayList<CommentData> mCommentList = new ArrayList<CommentData>();

    public static ZappData mZappData;
    private CommentAdapter mCommentAdapter;
    
    private Timer mRecordTimer = null;
    
    private AudioRecord mRecorder = null;
    private RelativeLayout mLayoutRecording;
    private int mnBufferSizeInBytes;
    private short[] mAudioData;
    private int mnElapsed = 0;

    private ImageView mbutRecPlay;
    private ImageView mbutRecord;
    private AudioTrack mPlayer = null;
    
    private TextView mtextRecording;
    
    private EditText meditComment;
    
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        initView();
        
        if (mZappData == null)
        {
        	mZappData = new ZappData(CommonUtils.mNotifyZappObject);
        }
        
        // get comment data
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
        query.whereEqualTo("zapp", mZappData.object);
        query.orderByDescending("updatedAt");

        query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) 
			{
				if (e == null) 
                {
					for (final ParseObject obj : objects) 
                    {
						final CommentData commentData = new CommentData(obj);
						mCommentList.add(commentData);
                    }
					
					mCommentAdapter.notifyDataSetChanged();
                }
				else
				{
					CommonUtils.createErrorAlertDialog(CommentActivity.this, "Alert", e.getMessage()).show();
				}
			}            	
        });
        
        CommonUtils.mbRecording = false;
    }

    public void initView()
    {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Config.scaleLayout(this, "comment", rootView, true);

        Typeface helveticaBoldFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");
        Typeface helveticaFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");

        // title
        TextView textTitle = (TextView)findViewById(R.id.text_title);
        textTitle.setTypeface(helveticaBoldFont);

        // done button
        Button button = (Button)findViewById(R.id.but_done);
        button.setTypeface(helveticaFont);
        button.setOnClickListener(this);
        
        mbutRecord = (ImageView)findViewById(R.id.but_record);
        mbutRecord.setOnClickListener(this);
        
        mbutRecPlay = (ImageView)findViewById(R.id.but_rec_play);
        mbutRecPlay.setOnClickListener(this);

        meditComment = (EditText)findViewById(R.id.edit_comment);
        meditComment.setTypeface(helveticaFont);
        meditComment.setOnEditorActionListener(this);
        
        mtextRecording = (TextView)findViewById(R.id.text_recording);
        mtextRecording.setTypeface(helveticaFont);

        // Get actual ListView
        final ListView feedListView = (ListView)findViewById(R.id.list_feed);
        feedListView.setVerticalFadingEdgeEnabled(false);

        mCommentAdapter = new CommentAdapter(this, mZappData, mCommentList);

        // You can also just use setListAdapter(mAdapter) or
        // mPullRefreshListView.setAdapter(mFeedAdapter);
        feedListView.setAdapter(mCommentAdapter);

        feedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                hideMoreView();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mLayoutRecording = (RelativeLayout)findViewById(R.id.layout_recording);

    }
    
    private void hideKeyboard()
    {
    	((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.but_done:
                onBackPressed();
                hideKeyboard();
                break;
                
            case R.id.but_record:
            	onButRecord();
            	break;
            	
            case R.id.but_rec_play:
            	
            	//If the track is playing, pause and achange playButton text to "Play"
            	if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                {
            		mPlayer.pause();
                }
                //If the track is not player, play the track and change the play button to "Pause"
                else
                {
                	mPlayer.play();
                	new Thread(new Runnable() 
                    {
                        public void run() 
                        {
                        	mPlayer.write(mAudioData, 0, mnBufferSizeInBytes);
                        	mPlayer.setNotificationMarkerPosition(mnBufferSizeInBytes);
                        }
                    }).start();
                }
                
                updateButRecPlay();
            	break;
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in_vert, R.anim.pop_out_vert);
    }
    
    private void stopRecording()
    {
        if (mRecorder != null)
        {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
    
    private void updateButRecPlay()
    {
        if (mZappData.type == 0)    // alert
        {
            if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
            {
            	mbutRecPlay.setImageResource(R.drawable.btn_zapp_alert_pause);
            }
            else
            {
            	mbutRecPlay.setImageResource(R.drawable.btn_zapp_alert_play);
            }
        }
        else    // fun
        {
        	if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
            {
            	mbutRecPlay.setImageResource(R.drawable.btn_zapp_fun_pause);
            }
            else
            {
            	mbutRecPlay.setImageResource(R.drawable.btn_zapp_fun_play);
            }
        }
    }
    
    private void onButRecord()
    {
    	if (CommonUtils.mbRecording)
        {
    		CommonUtils.mbRecording = false;
    		
            stopRecording();
        	if (mRecordTimer != null)
            {
            	mRecordTimer.cancel();
                mRecordTimer.purge();
                mRecordTimer = null;
            }
            
            mLayoutRecording.setVisibility(View.GONE);
            
            // init record player
            if (mPlayer != null)
            {
            	mPlayer.release();
            	mPlayer = null;
            }
            
            
            
            final File fileRecord = new File(CommonUtils.getRecordFileName(false));

            int shortSizeInBytes = Short.SIZE / Byte.SIZE;
            mnBufferSizeInBytes = (int)(fileRecord.length() / shortSizeInBytes);
            mAudioData = new short[mnBufferSizeInBytes];

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(fileRecord)));

                        int i = 0;
                        while (input.available() > 0)
                        {
                            mAudioData[i] = input.readShort();
                            i++;
                        }

                        input.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
            
            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mnBufferSizeInBytes, AudioTrack.MODE_STREAM);
            mPlayer.setStereoVolume(1.0f, 1.0f);
            mPlayer.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener()
            {
    			@Override
    			public void onMarkerReached(AudioTrack arg0) 
    			{
    				mPlayer.stop();
    				updateButRecPlay();
    			}

    			@Override
    			public void onPeriodicNotification(AudioTrack arg0) 
    			{
    			}
            });

            mbutRecord.setImageResource(R.drawable.btn_comment_record);
            
            
            // set RecordPlay button
            updateButRecPlay();
            mbutRecPlay.setVisibility(View.VISIBLE);
            meditComment.setVisibility(View.VISIBLE);
  
            if (meditComment.requestFocus()) 
            {
            	((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(meditComment, InputMethodManager.SHOW_FORCED);
            }
        }
    	else
        {
    		meditComment.setVisibility(View.GONE);
            mbutRecPlay.setVisibility(View.GONE);
            mLayoutRecording.setVisibility(View.VISIBLE);
            
            stopRecording();
            mnElapsed = 0;
            
            TimerTask task = new TimerTask()
            {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            mtextRecording.setText(String.format("Recording... %ds", 10 - mnElapsed));

                            if (mnElapsed >= 10)
                            {
                            	onButRecord();
                            }
                            
                            mnElapsed ++;
                        }
                    });
                }
            };

            mRecordTimer = new Timer();
            mRecordTimer.scheduleAtFixedRate(task, 0, 1000);
            
            mbutRecord.setImageResource(R.drawable.btn_comment_stop);
            CommonUtils.mbRecording = true;
            
            int nMinBufferSize = AudioRecord.getMinBufferSize(CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, nMinBufferSize);
            CommonUtils.startBufferedWrite(mRecorder, this);
        }
    }

	@Override
	public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) 
	{
		if (actionId == EditorInfo.IME_ACTION_SEND)
		{
			if (CommonUtils.mbRecording)
				return false;
			
			mProgressDialog = ProgressDialog.show(this, "", "Processing Audio Data...");			
	        
			final ParseObject commentObj = ParseObject.create("Comments");
	        commentObj.put("zapp", mZappData.object);
	        commentObj.put("user", ParseUser.getCurrentUser());
	        commentObj.put("content", meditComment.getText().toString());
	        commentObj.put("username", CommonUtils.getUserNameToShow(ParseUser.getCurrentUser()));
	        commentObj.put("targetuser", mZappData.user);
	        commentObj.put("type", mZappData.type);
	        commentObj.put("zappfile", mZappData.zappFile.getUrl());
	        
	        if (mPlayer != null)
	        {
	        	mPlayer.stop();
	        	
	        	CommonUtils utils = CommonUtils.sharedObject();
	        	
	        	utils.encodeFile(CommonUtils.getRecordFileName(false), CommonUtils.getRecordFileName(true));
	        	
	        	// upload mp3 data;
	            File file = new File(CommonUtils.getRecordFileName(true));
	            int size = (int)file.length();
	            byte[] bytes = new byte[size];
	            
	            try 
	            {
	                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
	                buf.read(bytes, 0, bytes.length);
	                buf.close();
	            } 
	            catch (FileNotFoundException e) 
	            {
	                e.printStackTrace();
	            } 
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	            
	            ParseFile pFile = new ParseFile("comment.mp3", bytes);
	            commentObj.put("voice", pFile);
	        }
	        
	        mProgressDialog.setMessage("Uploading Comment...");
	        
	        commentObj.saveInBackground(new SaveCallback()
	        {
				@Override
				public void done(com.parse.ParseException arg0) 
				{
					CommentData comment = new CommentData(commentObj);
					mCommentList.add(0, comment);
		            
		            mZappData.nCommentCount++;
		            mZappData.object.put("commentcount", mZappData.nCommentCount);
		            mZappData.object.saveInBackground();
		            
		            mProgressDialog.dismiss();
		            
		            // restore comment input view
		            if (mRecorder != null)
		            {
			            mRecorder.release();
			            mRecorder = null;
		            }
		            
		            if (mPlayer != null)
		            {
			            mPlayer.release();
			            mPlayer = null;
		            }
		            mbutRecPlay.setVisibility(View.GONE);
		            
		            meditComment.setText("");
		            mCommentAdapter.notifyDataSetChanged();
		            hideKeyboard();

		            //
		            // send notification to commented user
		            //
		            ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
		            query.whereEqualTo("user", mZappData.user);
		            
		            // Send the notification.
		            ParsePush push = new ParsePush();
		            push.setQuery(query);
		            
		            HashMap<String, Object> params = new HashMap<String, Object>();
		            params.put("alert", String.format("%s commented you", CommonUtils.getUserNameToShow(ParseUser.getCurrentUser())));
		            params.put("badge", "Increment");
		            params.put("sound", "cheering.caf");
		            params.put("notifyType", "comment");
		            params.put("notifyZapp", mZappData.object.getObjectId());

		            JSONObject data = new JSONObject(params);

		            push.setData(data);
		            push.sendInBackground();
				}
	        });

	        return true;
		}
		
		return false;
	}
}
