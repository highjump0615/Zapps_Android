package com.iliayugai.zapp.view;

import java.io.IOException;

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
import android.widget.TextView;

import com.iliayugai.zapp.data.CommentData;
import com.iliayugai.zapp.data.NotifyData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.widget.RoundedAvatarDrawable;
import com.iliayugai.zapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by aaa on 14-8-14.
 */
public class CommentListViewHolder extends IViewHolder implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
    private final String TAG = CommentListViewHolder.class.getSimpleName();

    private static Resources mResources;
    private Activity mActivity;

    public TextView mTextUsername;
    public TextView mTextContent;
    public ParseImageView mImagePhoto;
    public ImageView mButPlay;
    
    private String mCommentId = "";
    private String mUserId = "";

    public CommentData mCommentData;

    private RoundedAvatarDrawable mDefaultAvatarDrawable;
    
    private MediaPlayer mPlayer = null;

    public CommentListViewHolder(View listItemLayout, View.OnClickListener onClickListener)
    {
        mActivity = (Activity) onClickListener;
        mResources = mActivity.getResources();

        // scaling
        Config.scaleLayout(mActivity, "comment", listItemLayout, true);

        Typeface smallTextTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");
        Typeface largeTextTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");

        // username text
        mTextUsername = (TextView) listItemLayout.findViewById(R.id.text_username);
        mTextUsername.setTypeface(largeTextTypeface);

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

    public void fillContent(final CommentData commentData, int position)
    {
        mCommentData = commentData;
        
        if (!mCommentId.equals(mCommentData.object.getObjectId()))
        {
        	// content
	        mTextContent.setText(mCommentData.strContent);
        
		    // user photo loading
	        mImagePhoto.setImageDrawable(mDefaultAvatarDrawable);
	        
	        ParseUser userInfo = mCommentData.user;
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
		    
		    		    
		    // username label
		    mTextUsername.setText(mCommentData.strUsername);
		    
		    mButPlay.setEnabled(false);
		    
		    mCommentId = mCommentData.strId;
		    
		    if (mPlayer != null)
		    {
		    	mPlayer.release();
		    	mPlayer = null;
		    }
		    
		    if (mCommentData.voiceFile != null)
	    	{
			    mPlayer = new MediaPlayer();
			    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			    
			    try
			    {
			    	mPlayer.setDataSource(mCommentData.voiceFile.getUrl());
			    }
			    catch (IOException e)
			    {
			    	e.printStackTrace();
			    }
			    
			    mPlayer.prepareAsync();
			    mPlayer.setOnPreparedListener(this);
			    mPlayer.setOnCompletionListener(this);
	    	}
        }

        updateButtonImage();
    }

    private void updateButtonImage()
    {
    	if (mPlayer != null)
    	{
    		mButPlay.setVisibility(View.VISIBLE);
    		
	        if (mPlayer.isPlaying())
	        {
	        	if (mCommentData.type == 0)	// alert
	            {
	                mButPlay.setImageResource(R.drawable.btn_zapp_alert_pause);
	            }
	            else if (mCommentData.type == 1)	// fun
	            {
	                mButPlay.setImageResource(R.drawable.btn_zapp_fun_pause);
	            }
	        }
	        else
	        {
	        	if (mCommentData.type == 0)	// alert
	            {
	                mButPlay.setImageResource(R.drawable.btn_zapp_alert_play);
	            }
	            else if (mCommentData.type == 1)	// fun
	            {
	                mButPlay.setImageResource(R.drawable.btn_zapp_fun_play);
	            }
	        }
    	}
    	else
    	{
    		mButPlay.setVisibility(View.GONE);
    	}
    }

	@Override
	public void onCompletion(MediaPlayer arg0) 
	{
		stopPlaying();
		updateButtonImage();
	}

	@Override
	public void onPrepared(MediaPlayer arg0) 
	{
		mButPlay.setEnabled(true);
	}
	
	public void stopPlaying()
	{
		if (mPlayer.isPlaying())
	    {
			mPlayer.stop();
	    }

		updateButtonImage();
	    
//	    CommonUtils *utils = [CommonUtils sharedObject];
//	    utils.mCurrentPlayer = nil;
	}

	@Override
	public void onClick(View arg0) 
	{
		int id = arg0.getId();
		
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
		
		    if (mPlayer.isPlaying())
		    {
		        mPlayer.pause();
		        stopPlaying();
		    }
		    else
		    {
		    	mPlayer.start();
//		        CommononUtils.mCurrentPlayer = CommonUtilities = mPlayer;
		    }
	        updateButtonImage();
			break;
		}
	}
}


