package com.iliayugai.zapp.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.zapp.CommentActivity;
import com.iliayugai.zapp.ProfileActivity;
import com.iliayugai.zapp.SigninActivity;
import com.iliayugai.zapp.SplashActivity;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.utils.OnSwipeTouchListener;
import com.iliayugai.zapp.widget.RoundedAvatarDrawable;
import com.iliayugai.zapp.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by aaa on 14-8-14.
 */
public class ZappListViewHolder extends IViewHolder implements
		View.OnClickListener, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnCompletionListener {
	private final String TAG = ZappListViewHolder.class.getSimpleName();

	private static Resources mResources;
	private Activity mActivity;

	public TextView mTextContent;
	public TextView mTextUsername;
	public ParseImageView mImagePhoto;
	public TextView mTextDistance;
	public TextView mTextTime;
	public TextView mTextPlaynum;
	public TextView mTextLikenum;
	public TextView mTextCommentnum;
	public ImageView mButPlay;
	public ImageView mButLike;
	public ImageView mButComment;
	public LinearLayout mLayoutWave;
	public ImageView mImageWave;

	public ImageView mButDelete;
	public ImageView mButReport;
	public ImageView mButShare;

	public RelativeLayout mLayoutFront;
	private boolean mbSwiped;
	private float mfSwipeOffset;

	private TranslateAnimation mAnimationMove;
	private TranslateAnimation mSwipeshow;

	public ZappData mZappData;

	private View mRootView;
	private String mZappId = "";
	private String mUserId = "";

	private RoundedAvatarDrawable mDefaultAvatarDrawable;

	public AsyncHttpClient mClient = null;
	private MediaPlayer mPlayer = null;

//	private AudioTrack mPlayer = null;
	byte[] mAudioData;

	private int mnPlayBufSize;
//	public Timer mPlayTimer;

	public ZappListViewHolder(View listItemLayout,
			View.OnClickListener onClickListener) {
		mActivity = (Activity) onClickListener;
		mResources = mActivity.getResources();

		mRootView = listItemLayout;

		// scaling
		Config.scaleLayout(mActivity, "zapp", listItemLayout, true);

		Typeface smallTextTypeface = Typeface.createFromAsset(
				mActivity.getAssets(), "fonts/HelveticaNeueLTPro-Md.otf");
		Typeface largeTextTypeface = Typeface.createFromAsset(
				mActivity.getAssets(), "fonts/HelveticaNeueLTPro-Bd.otf");

		// content text
		mTextContent = (TextView) listItemLayout
				.findViewById(R.id.text_content);
		mTextContent.setTypeface(smallTextTypeface);

		// wave
		mLayoutWave = (LinearLayout) listItemLayout
				.findViewById(R.id.layout_wave);
		mLayoutWave.setVisibility(View.GONE);

		mImageWave = (ImageView) listItemLayout.findViewById(R.id.image_wave);

		// play button
		mButPlay = (ImageView) listItemLayout.findViewById(R.id.but_play);
		mButPlay.setOnClickListener(this);

		// user photo
		mImagePhoto = (ParseImageView) listItemLayout
				.findViewById(R.id.but_user);
		mDefaultAvatarDrawable = new RoundedAvatarDrawable(mResources,
				R.drawable.profile_photo_default);
		mImagePhoto.setImageDrawable(mDefaultAvatarDrawable);
		mImagePhoto.setOnClickListener(this);

		// zapp by
		TextView textZappby = (TextView) listItemLayout
				.findViewById(R.id.text_zappby);
		textZappby.setTypeface(smallTextTypeface);

		// username
		mTextUsername = (TextView) listItemLayout
				.findViewById(R.id.text_username);
		mTextUsername.setTypeface(smallTextTypeface);

		// like button
		mButLike = (ImageView) listItemLayout.findViewById(R.id.but_like);
		mButLike.setOnClickListener(this);

		// comment button
		mButComment = (ImageView) listItemLayout.findViewById(R.id.but_comment);
		mButComment.setOnClickListener(this);

		// comment count
		mTextCommentnum = (TextView) listItemLayout
				.findViewById(R.id.text_commentnum);
		mTextCommentnum.setTypeface(largeTextTypeface);

		// distance
		mTextDistance = (TextView) listItemLayout
				.findViewById(R.id.text_distance);
		mTextDistance.setTypeface(smallTextTypeface);

		// time
		mTextTime = (TextView) listItemLayout.findViewById(R.id.text_time);
		mTextTime.setTypeface(smallTextTypeface);

		// play number
		mTextPlaynum = (TextView) listItemLayout
				.findViewById(R.id.text_playnum);
		mTextPlaynum.setTypeface(smallTextTypeface);

		// like number
		mTextLikenum = (TextView) listItemLayout
				.findViewById(R.id.text_likenum);
		mTextLikenum.setTypeface(smallTextTypeface);

		//
		// swipe
		//
		mLayoutFront = (RelativeLayout) listItemLayout
				.findViewById(R.id.layout_front);
		listItemLayout.setOnTouchListener(new OnSwipeTouchListener(
				listItemLayout.getContext()) {
			@Override
			public void onSwipeLeft() {
				if (!mbSwiped) {
					mfSwipeOffset = (CommonUtils.dpToPx(
							Config.DESIGN_WIDTH_PHONE, mRootView.getContext()) - mResources
							.getDimension(R.dimen.zapp_but_report_leftMargin))
							* Config.mWidthScaleFactor;

					// mSwipeshow = new TranslateAnimation(0f, -mfSwipeOffset,
					// 0f, 0f);
					// mSwipeshow.setDuration(200);
					// mSwipeshow.setInterpolator(new LinearInterpolator());
					// mSwipeshow.setFillAfter(true);
					// mSwipeshow.setAnimationListener(swipeShowAnimationListener);
					//
					// mLayoutFront.startAnimation(mSwipeshow);

					ObjectAnimator leftmove = ObjectAnimator.ofFloat(
							mLayoutFront, "translationX", 0f, -mfSwipeOffset);
					leftmove.setDuration(200);
					leftmove.setInterpolator(new LinearInterpolator());
					leftmove.start();

					mbSwiped = true;

					mButReport.setClickable(true);
					mButShare.setClickable(true);
				}
			}

			@Override
			public void onSwipeRight() {
				closeSwipe(true);
			}
		});

		// buttons on back
		mButShare = (ImageView) listItemLayout.findViewById(R.id.but_share);
		mButShare.setOnClickListener(this);

		mButReport = (ImageView) listItemLayout.findViewById(R.id.but_report);
		mButReport.setOnClickListener(this);

		mnPlayBufSize = AudioTrack.getMinBufferSize(CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
	}

	private void closeSwipe(boolean animation) {
		if (mbSwiped) {
			ObjectAnimator rightmove = ObjectAnimator.ofFloat(mLayoutFront,
					"translationX", -mfSwipeOffset, 0f);

			if (animation) {
				rightmove.setDuration(200);
			} else {
				rightmove.setDuration(0);
			}

			rightmove.setInterpolator(new LinearInterpolator());
			rightmove.start();

			mbSwiped = false;
		}

		mButReport.setClickable(false);
		mButShare.setClickable(false);

	}

	public void fillContent(final ZappData zappData, int position) {
		mZappData = zappData;

		if (!mZappId.equals(mZappData.object.getObjectId())) {
			closeSwipe(false);

			// content
			mTextContent.setText(mZappData.strDescription);

			// user photo loading
			mImagePhoto.setImageDrawable(mDefaultAvatarDrawable);

			ParseUser userInfo = mZappData.user;
			mUserId = userInfo.getObjectId();

			userInfo.fetchIfNeededInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject parseObject, ParseException e) {
					if (e == null) {
						if (!mUserId.equals(parseObject.getObjectId())) {
							return;
						}

						CommonUtils.setUserPhoto((ParseUser) parseObject,
								mImagePhoto, mUserId);
					}
				}
			});

			// username label
			mTextUsername.setText(mZappData.strUsername);

			mButPlay.setEnabled(false);

			mZappId = mZappData.strId;

			// set zapp audio
			if (mPlayer != null) {
				mPlayer.release();
				mPlayer = null;
			}

			mPlayer = new MediaPlayer();
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

			try {
				mPlayer.setDataSource(mZappData.zappFile.getUrl());
			} catch (IOException e) {
				e.printStackTrace();
			}

//			 mPlayer.prepareAsync();
			mPlayer.setOnPreparedListener(this);
			mPlayer.setOnCompletionListener(this);

//			if (mClient != null)
//			{
//				mClient.cancelAllRequests(true);
//				mClient = null;
//			}
//
//			mClient = new AsyncHttpClient();
//			mClient.get(mZappData.zappFile.getUrl(), new AsyncHttpResponseHandler()
//			{
//				@Override
//				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3)
//				{
//				}
//
//				@Override
//				public void onSuccess(int arg0, Header[] arg1, byte[] arg2)
//				{
//					if (mPlayer != null)
//					{
//						mPlayer.release();
//						mPlayer = null;
//					}
//
////					mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC,
////											CommonUtils.SAMPLE_RATE,
////											AudioFormat.CHANNEL_OUT_MONO,
////											AudioFormat.ENCODING_PCM_16BIT,
////											mnPlayBufSize,
////											AudioTrack.MODE_STREAM);
////
//// // mPlayer.write(arg2, 0, arg2.length);
//					mAudioData = arg2.clone();
////					mPlayer.setStereoVolume(1.0f, 1.0f);
//
//					mButPlay.setEnabled(true);
//
//					Log.d(TAG, "zapp size: " + arg2.length);
//				}
//			});
		}

		updateButtonImage();

		// distance
		ParseGeoPoint currentPoint = CommonUtils
				.geoPointFromLocation(CommonUtils.currentLocation);
		ParseGeoPoint zappPoint = (ParseGeoPoint) mZappData.object
				.get("location");

		double distanceDouble = currentPoint.distanceInKilometersTo(zappPoint);
		mTextDistance.setText(String.format("%.1f Km", distanceDouble));

		// time
		mTextTime.setText(CommonUtils.getTimeString(mZappData.date));

		// play count
		mTextPlaynum.setText(String.format("%d", mZappData.nPlayCount));

		// like count
		mTextLikenum.setText(String.format("%d", mZappData.nLikeCount));

		// setlike
		if (mZappData.bLiked > 0) // liked
		{
			mButLike.setImageDrawable(mResources
					.getDrawable(R.drawable.btn_zapp_liked));
			mButLike.setEnabled(true);
		} else if (mZappData.bLiked == 0) // unliked
		{
			mButLike.setImageDrawable(mResources
					.getDrawable(R.drawable.btn_zapp_unliked));
			mButLike.setEnabled(true);
		} else // undetermined
		{
			// [self.mButLike setImage:[UIImage
			// imageNamed:@"zapp_unliked_but.png"]
			// forState:UIControlStateNormal];
			mButLike.setEnabled(false);
		}

		// set comments
		if (mZappData.nCommentCount > 0) {
			mButComment.setImageDrawable(mResources
					.getDrawable(R.drawable.btn_zapp_commented));
			mTextCommentnum.setVisibility(View.VISIBLE);
			mTextCommentnum.setText(String
					.format("%d", mZappData.nCommentCount));
		} else {
			mButComment.setImageDrawable(mResources
					.getDrawable(R.drawable.btn_zapp_uncommented));
			mTextCommentnum.setVisibility(View.INVISIBLE);
		}

		if (position < 0) // from comment
		{
			mButComment.setVisibility(View.GONE);
			mTextCommentnum.setVisibility(View.GONE);

			mButLike.setLayoutParams(mButComment.getLayoutParams());
		}
	}

	private void updateButtonImage() 
	{
		if (mPlayer.isPlaying())
//		if (mPlayer != null && mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
		{
			if (mZappData.type == 0) // alert
			{
				mButPlay.setImageResource(R.drawable.btn_zapp_alert_pause);
				mImageWave.setImageResource(R.drawable.alert_play_wave);
			} else if (mZappData.type == 1) // fun
			{
				mButPlay.setImageResource(R.drawable.btn_zapp_fun_pause);
				mImageWave.setImageResource(R.drawable.fun_play_wave);
			}

			mLayoutWave.setVisibility(View.VISIBLE);
			mTextContent.setVisibility(View.INVISIBLE);
		}
		else 
		{
			if (mZappData.type == 0) // alert
			{
				mButPlay.setImageResource(R.drawable.btn_zapp_alert_play);
			} else if (mZappData.type == 1) // fun
			{
				mButPlay.setImageResource(R.drawable.btn_zapp_fun_play);
			}

			mLayoutWave.setVisibility(View.INVISIBLE);
			mTextContent.setVisibility(View.VISIBLE);

			if (mAnimationMove != null) {
				mAnimationMove.cancel();
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();

		ParseObject zappObj;

		switch (id) {
		case R.id.but_user:
			if (ParseUser.getCurrentUser() != mZappData.user) {
				ProfileActivity.mUser = mZappData.user;
				mActivity.startActivity(new Intent(mActivity,
						ProfileActivity.class));
				mActivity.overridePendingTransition(R.anim.anim_in,
						R.anim.anim_out);
			}
			break;

		case R.id.but_like:

			if (mZappData.bLiked == 0) // unliked
			{
				ParseUser currentUser = ParseUser.getCurrentUser();
				ParseObject like = ParseObject.create("Likes");
				like.put("zapp", mZappData.object);
				like.put("user", currentUser);
				like.put("username", CommonUtils.getUserNameToShow(currentUser));
				like.put("targetuser", mZappData.user);
				like.put("type", mZappData.type);
				like.put("zappfile", mZappData.zappFile.getUrl());

				like.saveInBackground();

				mZappData.likeObject = like;
				mZappData.nLikeCount++;

				mButLike.setImageDrawable(mResources
						.getDrawable(R.drawable.btn_zapp_liked));
				mZappData.bLiked = 1;

				// like animation

				//
				// send notification to liked user
				//
				ParseQuery<ParseInstallation> query = ParseInstallation
						.getQuery();
				query.whereEqualTo("user", mZappData.user);

				// Send the notification.
				ParsePush push = new ParsePush();
				push.setQuery(query);

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("alert", String.format("%s liked your zapp",
						CommonUtils.getUserNameToShow(ParseUser
								.getCurrentUser())));
				params.put("badge", "Increment");
				params.put("sound", "cheering.caf");
				params.put("notifyType", "like");
				params.put("notifyZapp", mZappData.object.getObjectId());

				JSONObject data = new JSONObject(params);

				push.setData(data);
				push.sendInBackground();
			} else // unlike
			{
				if (mZappData.likeObject != null) {
					mZappData.likeObject.deleteInBackground();

					mButLike.setImageDrawable(mResources
							.getDrawable(R.drawable.btn_zapp_unliked));
					mZappData.bLiked = 0;

					mZappData.nLikeCount--;
				}

				// unlike animation
			}

			mTextLikenum.setText(String.format("%d", mZappData.nLikeCount));

			zappObj = mZappData.object;
			zappObj.put("likecount", mZappData.nLikeCount);
			zappObj.saveInBackground();

			break;

		case R.id.but_comment:

			CommentActivity.mZappData = mZappData;
			mActivity
					.startActivity(new Intent(mActivity, CommentActivity.class));
			mActivity.overridePendingTransition(R.anim.anim_in_vert,
					R.anim.anim_out_vert);

			break;

		case R.id.but_play:

			if (mPlayer.isPlaying())
//			if (mPlayer != null && mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
			{
				mPlayer.pause();
			}
			else 
			{
				mPlayer.start();
				// // mPlayer.stop.();
				// // mPlayer.reloadStaticData();
//				mPlayer.play();
//				mPlayer.write(mAudioData, 0, mAudioData.length);

				mZappData.nPlayCount++;
				mTextPlaynum.setText(String.format("%d", mZappData.nPlayCount));

				zappObj = mZappData.object;
				zappObj.put("playcount", mZappData.nPlayCount);
				zappObj.saveInBackground();

				if (mAnimationMove == null) {
					float fAnimationValue = -mResources
							.getDimension(R.dimen.zapp_image_wave_width)
							* Config.mWidthScaleFactor / 2.0f;
					mAnimationMove = new TranslateAnimation(0f,
							fAnimationValue, 0f, 0f);
					mAnimationMove.setDuration(2000);
					mAnimationMove.setInterpolator(new LinearInterpolator());
					mAnimationMove.setRepeatCount(Animation.INFINITE);
					mAnimationMove.setRepeatMode(Animation.RESTART);
				}

				mImageWave.startAnimation(mAnimationMove);
			}

			updateButtonImage();

			break;

		case R.id.but_report:
			CommonUtils.onMoreReport(mActivity);
			break;

		case R.id.but_share:
			try {
				CommonUtils.mZappToShare = mZappData;
				Method showShare = mActivity.getClass().getMethod(
						"showShareView");
				showShare.invoke(mActivity);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		updateButtonImage();
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		mButPlay.setEnabled(true);
	}

	//
	// Animation.AnimationListener swipeShowAnimationListener = new
	// Animation.AnimationListener()
	// {
	// @Override
	// public void onAnimationStart(Animation animation)
	// {
	// }
	//
	// @Override
	// public void onAnimationEnd(Animation animation)
	// {
	// mLayoutFront.clearAnimation();
	//
	// RelativeLayout.LayoutParams params = new
	// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
	// RelativeLayout.LayoutParams.WRAP_CONTENT);
	// params.addRule(RelativeLayout.BELOW, R.id.layout_front);
	// mLayoutFront.setLayoutParams(params);
	// //// params.leftMargin -=
	// (int)(mResources.getDimension(R.dimen.zapp_but_report_leftMargin) *
	// Config.mWidthScaleFactor);
	// // frontLayout.setLayoutParams(params);
	// // frontLayout.layout();
	// }
	//
	// @Override
	// public void onAnimationRepeat(Animation animation)
	// {
	// }
	// };
}
