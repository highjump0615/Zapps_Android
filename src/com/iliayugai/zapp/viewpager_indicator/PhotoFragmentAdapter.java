package com.iliayugai.zapp.viewpager_indicator;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.iliayugai.zapp.R;
import com.parse.ParseFile;

/**
 * Created by aaa on 14-8-18.
 */
public class PhotoFragmentAdapter extends FragmentPagerAdapter
{
    protected static final int[] CONTENT = new int[]{
            R.drawable.profile_photo_default,
            R.drawable.profile_photo_default,
            R.drawable.profile_photo_default,
            R.drawable.profile_photo_default,
            R.drawable.profile_photo_default
    };

    protected static final String[] TITLES = {
            "", "", "", "", ""
    };
    
    public ArrayList<ParseFile> mUrlList = new ArrayList<ParseFile>();

    public int mCount = CONTENT.length;

    public PhotoFragmentAdapter(FragmentManager fm)
    {
        super(fm);
    }
    
    @Override
    public Fragment getItem(int position)
    {
    	int nCount = getCount();
    	
    	if (mUrlList.size() > 0)
    	{
    		return PhotoFragment.newInstance(mUrlList.get(position), nCount == (position + 1));
    	}
    	else
    	{
    		return PhotoFragment.newInstance();
    	}
    }

    @Override
    public int getCount() 
    {
    	int nCount = 1;
    	if (mUrlList.size() > 0)
    	{
    		nCount = mUrlList.size();
    	}

        return nCount;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return PhotoFragmentAdapter.TITLES[position];
    }

//    @Override
//    public int getIconResId(int index)
//    {
//        return CONTENT[index];
//    }

    public void setCount(int count)
    {
        if (count > 0 && count <= 10)
        {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}
