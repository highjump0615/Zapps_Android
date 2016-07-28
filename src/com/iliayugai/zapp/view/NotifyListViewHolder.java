package com.iliayugai.zapp.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.zapp.data.NotifyData;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.utils.OnSwipeTouchListener;
import com.iliayugai.zapp.widget.RoundedAvatarDrawable;
import com.iliayugai.zapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by aaa on 14-8-14.
 */
public class NotifyListViewHolder implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
    private final String TAG = NotifyListViewHolder.class.getSimpleName();

    private static Resources mResources;
    private Activity mActivity;

    public TextView mTextContent;
    public ParseImageView mImagePhoto;
    public ImageView mButPlay;

    private boolean mbPlaying;

    public NotifyData mNotifyData;

    private RoundedAvatarDrawable mDefaultAvatarDrawable;
    
    private String mObjectId = "";
    private String mUserId = "";
    
    private MediaPlayer mPlayer = null;

    public NotifyListViewHolder(View listItemLayout, View.OnClickListener onClickListener)
    {
        mActivity = (Activity) onClickListener;
        mResources = mActivity.getResources();

        // scaling
        Config.scaleLayout(mActivity, "notify", listItemLayout, true);

        Typeface smallTextTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");

        // content text
        mTextContent = (TextView) listItemLayout.findViewById(R.id.text_content);
        mTextContent.setTypeface(smallTextTypeface);

        // play button
        mButPlay = (ImageView) listItemLayout.findViewById(R.id.but_play);
        mButPlay.setOnClickListener(this);

        // user photo
        mImagePhoto = (ParseImageView) listItemLayout.findViewById(R.id.but_user);
        mDefaultAvatarDrawable = new RoundedAvatarDrawable(mResources, R.drawable.profile_photo_default);
        mImagePhoto.setImageDrawable(mDefaultAvatarDrawable);
        mImagePhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "Image Photo");
            }
        });
    }

    public void fillContent(final NotifyData notifyData, int position)
    {
        mNotifyData = notifyData;
        
        mbPlaying = false;
        
        String strText;
        if (mNotifyData.notificationType == 0)   // like
        {
            strText = mNotifyData.strUsername + " liked your zapp";
        }
        else
        {
            strText = mNotifyData.strUsername + " commented your zapp";
        }
        
//        NSMutableAttributedString *strAttributedText = [[NSMutableAttributedString alloc] initWithString:strText];
//        [strAttributedText addAttribute:NSFontAttributeName
//                                  value:helveticaBoldFont
//                                  range:NSMakeRange(0, mNotifyData.strUsername.length)];
//        
//        [self.mLblText setAttributedText:strAttributedText];
        
        if (mNotifyData.type == 0)   // alert
        {
        	mButPlay.setImageResource(R.drawable.btn_zapp_alert_play);
        }
        else
        {
        	mButPlay.setImageResource(R.drawable.btn_zapp_fun_play);
        }        

        // set up audio
        if (!mObjectId.equals(mNotifyData.object.getObjectId()))
        {
        	// content
	        mTextContent.setText(strText);
	    
	        // user photo loading
	        mImagePhoto.setImageDrawable(mDefaultAvatarDrawable);
	        
	        ParseUser userInfo = mNotifyData.user;
	        mUserId = userInfo.getObjectId();
	        
	        userInfo.fetchIfNeededInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject parseObject, ParseException e) 
				{
					if (e == null)
					{
						if (!mUserId.equals(parseObject.getObjectId()))
						{
							return;
						}
						
						CommonUtils.setUserPhoto((ParseUser)parseObject, mImagePhoto, mUserId);
					}
				}
		    });
            
            // load zapp data
            mButPlay.setEnabled(false);
            
            // set zapp audio
		    if (mPlayer != null)
		    {
		    	mPlayer.release();
		    	mPlayer = null;
		    }
		    
		    mPlayer = new MediaPlayer();
		    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		    
		    try
		    {
		    	mPlayer.setDataSource(mNotifyData.strAudioFile);
		    }
		    catch (IOException e)
		    {
		    	e.printStackTrace();
		    }
		    
		    mPlayer.prepareAsync();
		    mPlayer.setOnPreparedListener(this);
		    mPlayer.setOnCompletionListener(this);
            
            
            mObjectId = mNotifyData.object.getObjectId();
        }
    }

	@Override
	public void onCompletion(MediaPlayer arg0) 
	{
		stopPlaying();
	}

	@Override
	public void onPrepared(MediaPlayer arg0) 
	{
		mButPlay.setEnabled(true);
	}
	
	public void stopPlaying()
	{
	    mbPlaying = false;
	    
	    if (mNotifyData.type == 0)   // alert
        {
        	mButPlay.setImageResource(R.drawable.btn_zapp_alert_play);
        }
        else
        {
        	mButPlay.setImageResource(R.drawable.btn_zapp_fun_play);
        }

	    if (mPlayer.isPlaying())
	    {
	        mPlayer.pause();
	    }
	    
//	    CommonUtils *utils = [CommonUtils sharedObject];
//	    utils.mCurrentPlayer = nil;
	}

	@Override
	public void onClick(View arg0) 
	{
		int id = arg0.getId();
		ParseObject obj;
		
		switch (id)
		{
		case R.id.but_play:
//			if (CommonUtils.mCurrentPlayer)
//		    {
//		        if (utils.mCurrentPlayer != mPlayer)
//		        {
//		            return;
//		        }
//		    }
		    
		    if (mbPlaying)
		    {
		        mPlayer.pause();
		        stopPlaying();
		    }
		    else
		    {
		    	if (mNotifyData.type == 0)   // alert
		        {
		        	mButPlay.setImageResource(R.drawable.btn_zapp_alert_pause);
		        }
		        else
		        {
		        	mButPlay.setImageResource(R.drawable.btn_zapp_fun_pause);
		        }
		        
		        mPlayer.start();
		        
//		        CommononUtils.mCurrentPlayer = CommonUtilities = mPlayer;
		        
		        mbPlaying = true;
		    }
			break;
		}
	}

}


