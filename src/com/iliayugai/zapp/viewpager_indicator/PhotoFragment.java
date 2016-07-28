package com.iliayugai.zapp.viewpager_indicator;

import java.net.URI;
import java.util.concurrent.RejectedExecutionException;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.widget.RoundedAvatarDrawable;
import com.iliayugai.zapp.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;

/**
 * Created by aaa on 14-8-18.
 */
public class PhotoFragment extends Fragment
{
    private static final String KEY_CONTENT = "PhotoFragment:Content";

    public static PhotoFragment newInstance()
    {
        PhotoFragment fragment = new PhotoFragment();
        return fragment;
    }
    
    public static PhotoFragment newInstance(ParseFile file, boolean isLast)
    {
        PhotoFragment fragment = new PhotoFragment();
        
        fragment.mPhotoFile = file;
        fragment.mIsLast = isLast;

        return fragment;
    }

    private int mContentResId = 0;
    private ParseFile mPhotoFile;
    private boolean mIsLast = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

//        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT))
//        {
//            mContentResId = savedInstanceState.getInt(KEY_CONTENT);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ParseImageView imageView = new ParseImageView(getActivity());
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        imageView.setImageResource(R.drawable.profile_photo_default);

        if (mPhotoFile != null)
        {
	        imageView.setParseFile(mPhotoFile);
			imageView.setPlaceholder(getResources().getDrawable(R.drawable.profile_photo_default));
	
			try
			{			
				imageView.loadInBackground();
			}
			catch (RejectedExecutionException ex)
			{
				if (Config.DEBUG)
					ex.printStackTrace();
			}
        }

        
//        if (mContentResId != 0) 
//        	imageView.setBackgroundResource(mContentResId);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
//
        RelativeLayout layout = new RelativeLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.addView(imageView);
//
//        if (mIsLast)
//        {
//        }

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
//        super.onSaveInstanceState(outState);
//        outState.putInt(KEY_CONTENT, mContentResId);
    }

}
