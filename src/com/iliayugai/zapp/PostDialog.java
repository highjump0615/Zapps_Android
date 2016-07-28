package com.iliayugai.zapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aaa on 14-8-19.
 */
public class PostDialog extends Dialog implements View.OnClickListener
{
    private static final String TAG = PostDialog.class.getSimpleName();

    int layoutRes;
    Context context;

    private ImageView mbutPlay;
    private ImageView mbutAlert;
    private ImageView mbutFun;
    private ImageView mbutShare;
    private ImageView mbutFacebook;
    private ImageView mbutTwitter;

    private SeekBar mSeekbar;
    private EditText meditDesc;
    private short[] mAudioData;

    private int mnCurrentDistance;

    private TextView mtextCurrentDist;

//    private MediaPlayer mPlayer = null;
    private AudioTrack mPlayer = null;
    private int mnBufferSizeInBytes;

    private int mnMode = 0; // 0: alert, 1: fun
    private int mnShareMode = -1;	// 0: facebook, 1: twitter
    
    private ProgressDialog mProgressDialog;
    
    public boolean mbResult = false; 

    public PostDialog(Context context)
    {
        super(context);
        this.context = context;
    }

    public PostDialog(Context context, int resLayout)
    {
        super(context);
        this.context = context;
        this.layoutRes = resLayout;
    }

    public PostDialog(Context context, int theme, int resLayout)
    {
        super(context, theme);
        this.context = context;
        this.layoutRes = resLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);

        initView();

//        mnPlayBufSize = AudioTrack.getMinBufferSize(CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

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
				updateView();
			}

			@Override
			public void onPeriodicNotification(AudioTrack arg0) 
			{
				Log.d(TAG, "onPeriodicNotification");
			}
        	
        });

//            mPlayer.setDataSource(CommonUtils.getRecordFileName());
//            mPlayer.prepare();

//        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
//        {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer)
//            {
//                updateView();
//            }
//        });
    }

    public void initView()
    {
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Config.mHeightScaleFactor /= 1.3f;
        Config.scaleLayout(context, "post", findViewById(R.id.layout_root), true);
        Config.mHeightScaleFactor *= 1.3f;

        mSeekbar = (SeekBar)findViewById(R.id.seekbar_distnace);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mSeekbar.getLayoutParams();
        params.width = (int)(CommonUtils.dpToPx(R.dimen.post_seekbar_distance_width, context) * Config.mWidthScaleFactor);
        mSeekbar.setLayoutParams(params);
        mSeekbar.setProgress(49);
        mSeekbar.setMax(99);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                setCurrentDistance(i+1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // button
        mbutPlay = (ImageView)findViewById(R.id.but_post_play);
        mbutPlay.setOnClickListener(this);

        mbutAlert = (ImageView)findViewById(R.id.but_seg_alert);
        mbutAlert.setOnClickListener(this);

        mbutFun = (ImageView)findViewById(R.id.but_seg_fun);
        mbutFun.setOnClickListener(this);

        mbutShare = (ImageView)findViewById(R.id.but_post);
        mbutShare.setOnClickListener(this);

        mbutFacebook = (ImageView)findViewById(R.id.but_facebook);
        mbutFacebook.setOnClickListener(this);

        mbutTwitter = (ImageView)findViewById(R.id.but_twitter);
        mbutTwitter.setOnClickListener(this);

        // set font
        Typeface helveticaFont = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");
        Typeface helveticaBoldFont = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");

        // text view
        meditDesc = (EditText)findViewById(R.id.edit_post_desc);
        meditDesc.setTypeface(helveticaFont);

        TextView textView = (TextView)findViewById(R.id.text_broad_dist);
        textView.setTypeface(helveticaFont);

        mtextCurrentDist = (TextView)findViewById(R.id.text_dist_current);
        mtextCurrentDist.setTypeface(helveticaBoldFont);

        textView = (TextView)findViewById(R.id.text_dist_min);
        textView.setTypeface(helveticaFont);

        textView = (TextView)findViewById(R.id.text_dist_max);
        textView.setTypeface(helveticaFont);

        textView = (TextView)findViewById(R.id.text_also_share);
        textView.setTypeface(helveticaFont);

        setCurrentDistance(50);
    }

    public void setCurrentDistance(int nVal)
    {
        mnCurrentDistance = nVal;
        mtextCurrentDist.setText(String.format("%dkm", nVal));
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.but_post_play:

                if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                {
                    mPlayer.pause();
                }
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

                updateView();

                break;

            case R.id.but_seg_alert:
                mnMode = 0;
                updateView();
                break;

            case R.id.but_seg_fun:
                mnMode = 1;
                updateView();
                break;
                
            case R.id.but_post:
            	
            	onSaveShare();
            	
            	break;
            	
            case R.id.but_facebook:
            	
            	if (mnShareMode != 0)
                {
            		mnShareMode = 0;
                }
                else
                {
                	mnShareMode = -1;
                }
                
                updateView();
                
            	break;
            	
        	case R.id.but_twitter:
        		
        		if (mnShareMode != 1)
        	    {
        			mnShareMode = 1;
        	    }
        	    else
        	    {
        	    	mnShareMode = -1;
        	    }
        	    
        	    updateView();
        	    
        		break;
        }
    }

    private void updateView()
    {
        if (mnMode == 0)    // alert
        {
            if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
            {
                mbutPlay.setBackgroundResource(R.drawable.btn_zapp_alert_pause);
            }
            else
            {
                mbutPlay.setBackgroundResource(R.drawable.btn_zapp_alert_play);
            }

            mbutAlert.setBackgroundResource(R.drawable.save_seg_alert_on);
            mbutFun.setBackgroundResource(R.drawable.btn_save_seg_fun_bg);
            mbutShare.setBackgroundResource(R.drawable.btn_alert_share_bg);
        }
        else    // fun
        {
        	if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
            {
                mbutPlay.setBackgroundResource(R.drawable.btn_zapp_fun_pause);
            }
            else
            {
                mbutPlay.setBackgroundResource(R.drawable.btn_zapp_fun_play);
            }

            mbutAlert.setBackgroundResource(R.drawable.btn_save_seg_alert_bg);
            mbutFun.setBackgroundResource(R.drawable.save_seg_fun_on);
            mbutShare.setBackgroundResource(R.drawable.btn_fun_share_bg);
        }
        
        mbutFacebook.setBackgroundResource(R.drawable.btn_save_facebook_bg);
        mbutTwitter.setBackgroundResource(R.drawable.btn_save_twitter_bg);
        
        if (mnShareMode == 0)       // facebook
        {
        	mbutFacebook.setBackgroundResource(R.drawable.save_facebook_on_but);
            mbutTwitter.setBackgroundResource(R.drawable.btn_save_twitter_bg);
        }
        else if (mnShareMode == 1)  // twitter
        {
        	mbutFacebook.setBackgroundResource(R.drawable.btn_save_facebook_bg);
            mbutTwitter.setBackgroundResource(R.drawable.save_twitter_on_but);
        }
    }
    
    private void onSaveShare()
    {
    	if (TextUtils.isEmpty(meditDesc.getText().toString())) 
    	{
    		CommonUtils.createErrorAlertDialog(context, "Alert", "Input the description").show();
    		return;
        }
    	
    	mProgressDialog = ProgressDialog.show(context, "", "Processing Audio Data...");
    	CommonUtils utils = CommonUtils.sharedObject();
    	
    	utils.encodeFile(CommonUtils.getRecordFileName(false), CommonUtils.getRecordFileName(true));
    	
    	// save to zapp objects
        final ParseObject zappObj = ParseObject.create("Zapps");
        zappObj.put("username", CommonUtils.getUserNameToShow(ParseUser.getCurrentUser()));
        zappObj.put("type", mnMode);
        zappObj.put("user", ParseUser.getCurrentUser());
        zappObj.put("description", meditDesc.getText().toString());
        zappObj.put("range", mSeekbar.getProgress() + 1);

        // save location
        ParseGeoPoint currentPoint = CommonUtils.geoPointFromLocation(CommonUtils.currentLocation);
        zappObj.put("location", currentPoint);
        
        // upload mp3 data;
        mProgressDialog.setMessage("Uploading Zapp Data...");
        
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
        
        ParseFile pFile = new ParseFile("zapp.mp3", bytes);
        zappObj.put("zapp", pFile);
        
        mProgressDialog.setMessage("Saving Data...");
        zappObj.saveInBackground(new SaveCallback()
        {
			@Override
			public void done(ParseException arg0) 
			{
				ZappData zappNew = new ZappData(zappObj);
		        zappNew.bLiked = 0;
		        CommonUtils.mZappList.add(0, zappNew);
		        
		        // update zapp count of the user
		        ParseUser currentUser = ParseUser.getCurrentUser();
		        int nZappCount = currentUser.getInt("zappcount");
		        currentUser.put("zappcount", nZappCount + 1);
		        currentUser.saveInBackground();
		        
		        // facebook twitter share
		        
		        //
		        // send notification to the user around here
		        //
		        ParseQuery<ParseUser> query = ParseUser.getQuery();
		        query.whereEqualTo("distanceFilter", true);
		        query.whereEqualTo("objectId", currentUser.getObjectId());

		        ParseGeoPoint point = CommonUtils.geoPointFromLocation(CommonUtils.currentLocation);
		        query.whereWithinKilometers("location", point, currentUser.getDouble("distance"));
		        
		        query.findInBackground(new FindCallback<ParseUser>() 
		        {
		            @Override
		            public void done(final List<ParseUser> userList, ParseException e) 
		            {
		                if (e == null) 
		                {
							ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
							pushQuery.whereContainedIn("user", userList);
							
							// Send the notification.
				            ParsePush push = new ParsePush();
				            push.setQuery(pushQuery);
				            
				            HashMap<String, Object> params = new HashMap<String, Object>();
				            params.put("alert", meditDesc.getText().toString());
				            params.put("badge", "Increment");
				            params.put("sound", "cheering.caf");
				            params.put("notifyType", "zapp");
				            params.put("notifyZapp", zappObj.getObjectId());

				            JSONObject data = new JSONObject(params);

				            push.setData(data);
				            push.sendInBackground();
		                }
		            }
		        });
		        
		        mProgressDialog.dismiss();
		        
		        mbResult = true;
		        dismiss();				
			}
        });
    }
}
