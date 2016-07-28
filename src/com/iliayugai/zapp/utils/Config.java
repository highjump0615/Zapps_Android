package com.iliayugai.zapp.utils;

import com.iliayugai.zapp.R;
import com.iliayugai.zapp.R.dimen;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class Config 
{
    public static final boolean DEBUG = true;
	private static final boolean LOG_SCALING = false;
	
	public static final int DESIGN_WIDTH_PHONE = 320;
	public static final int DESIGN_HEIGHT_PHONE = 480;
	
	public static int mRealScreenWidth = 0;
	
	public static float mWidthScaleFactor = 0.0f;
	public static float mHeightScaleFactor = 0.0f;
	public static float mFontScaleFactor = 0.0f;
	
	public static Resources mResources = null;

	public static void calculateScaleFactor(Activity activity) 
	{
        final Resources resources = activity.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();
//        if (Utils.isTablet(activity)) {
//            mScaleFactor = metrics.widthPixels / (DESIGN_WIDTH_TABLET * metrics.scaledDensity);
//        }
//        else {

        mWidthScaleFactor = metrics.widthPixels / (DESIGN_WIDTH_PHONE * metrics.scaledDensity);
        mHeightScaleFactor = metrics.heightPixels / (DESIGN_HEIGHT_PHONE * metrics.scaledDensity);
        mHeightScaleFactor *= 1.2f;
//        }
        mFontScaleFactor = mWidthScaleFactor / metrics.density;
        mRealScreenWidth = metrics.widthPixels;

//        mScaleFactor = 1.0f;
//        mFontScaleFactor = 1.0f;
    }
	
	public static void scaleLayout(final Context context, final String prefix, final View root, final boolean useHeight)
	{
        if (mResources == null)
            mResources = context.getResources();

        String entryName = null;

        try 
        {
            entryName = mResources.getResourceEntryName(root.getId());
        } 
        catch (Throwable throwable) 
        {
            if (LOG_SCALING) 
            	Log.e("scaleLayout", throwable.getMessage());
        }

        if (root instanceof SeekBar)
        {
            return;
        }

        // first self scaling
        if (entryName != null && !(root instanceof ScrollView)) 
        {

            ViewParent parentView = root.getParent();

            if (parentView instanceof LinearLayout || parentView instanceof RelativeLayout) 
            {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();

                // Get width, height, marginLeft, marginTop, marginBottom, marginRight
                if (params != null) 
                {
                    try 
                    {
                        params.width = (int) (getDimension(prefix, entryName, "size") * Config.mWidthScaleFactor);
                        params.height = (int) (getDimension(prefix, entryName, "size") * Config.mWidthScaleFactor);
                    } 
                    catch (Throwable exc) 
                    {
                        try 
                        {
                            params.width = (int) (getDimension(prefix, entryName, "width") * Config.mWidthScaleFactor);
                        } 
                        catch (Throwable throwable) 
                        {
                            if (LOG_SCALING) 
                            	Log.e("scaleLayout()", throwable.getMessage());
                        }

                        try 
                        {
                            params.height = (int) (getDimension(prefix, entryName, "height") * Config.mWidthScaleFactor);
                        } 
                        catch (Throwable throwable) 
                        {
                            if (LOG_SCALING) 
                            	Log.e("scaleLayout()", throwable.getMessage());
                        }
                    }

                    try 
                    {
                        int margin = (int) (getDimension(prefix, entryName, "margin") * mWidthScaleFactor);
                        params.setMargins(margin, margin, margin, margin);
                    } 
                    catch (Throwable exc) 
                    {
                        try 
                        {
                            params.leftMargin = (int) (getDimension(prefix, entryName, "leftMargin") * mWidthScaleFactor);
                        }
                        catch (Throwable throwable) {
                            if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                        }

                        try {
                            if (useHeight)
                            {
                                params.topMargin = (int) (getDimension(prefix, entryName, "topMargin") * mHeightScaleFactor);
                            }
                            else
                            {
                                params.topMargin = (int) (getDimension(prefix, entryName, "topMargin") * mWidthScaleFactor);
                            }
                        } 
                        catch (Throwable throwable) 
                        {
                            if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                        }

                        try 
                        {
                            params.rightMargin = (int) (getDimension(prefix, entryName, "rightMargin") * mWidthScaleFactor);
                        } 
                        catch (Throwable throwable) 
                        {
                            if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                        }

                        try 
                        {
                            if (useHeight)
                            {
                                params.bottomMargin = (int) (getDimension(prefix, entryName, "bottomMargin") * mHeightScaleFactor);
                            }
                            else
                            {
                                params.bottomMargin = (int) (getDimension(prefix, entryName, "bottomMargin") * mWidthScaleFactor);
                            }
                        }
                        catch (Throwable throwable) 
                        {
                            if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                        }
                    }

                    root.setLayoutParams(params);
                } 
                else 
                {
                    if (LOG_SCALING)
                        Log.e("scaleLayout()", "LayoutParams == null for R.id." + String.format("%s_%s", prefix, entryName));
                }
            } 
            else if (parentView instanceof ViewGroup) 
            {
                ViewGroup.LayoutParams params = root.getLayoutParams();

                if (params != null) {
                    try {
                        params.width = (int) (getDimension(prefix, entryName, "size") * Config.mWidthScaleFactor);
                        params.height = params.width;
                    } 
                    catch (Throwable exc) 
                    {
                        try 
                        {
                            params.width = (int) (getDimension(prefix, entryName, "width") * Config.mWidthScaleFactor);
                        } 
                        catch (Throwable throwable) 
                        {
                            if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                        }

                        try 
                        {
                            params.height = (int) (getDimension(prefix, entryName, "height") * Config.mWidthScaleFactor);
                        } 
                        catch (Throwable throwable) 
                        {
                            if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                        }
                    }

                    root.setLayoutParams(params);
                } 
                else 
                {
                    if (LOG_SCALING)
                        Log.e("scaleLayout()", "LayoutParams == null for R.id." + String.format("%s_%s", prefix, entryName));
                }
            } 
            else 
            {
                if (LOG_SCALING)
                    Log.e("scaleLayout()", "Skip processing for R.id." + String.format("%s_%s", prefix, entryName));
            }

            try 
            {
                int padding = (int) (getDimension(prefix, entryName, "padding") * Config.mWidthScaleFactor);

                root.setPadding(padding, padding, padding, padding);

            } 
            catch (Throwable throwable1) 
            {
                // paddingLeft, paddingTop, paddingRight, paddingBottom
                int paddingLeft = 0, paddingTop = 0, paddingRight = 0, paddingBottom = 0;

                try 
                {
                    paddingLeft = (int) (getDimension(prefix, entryName, "paddingLeft") * Config.mWidthScaleFactor);
                } 
                catch (Throwable throwable) 
                {
                    if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                }

                try 
                {
                    if (useHeight)
                    {
                        paddingTop = (int) (getDimension(prefix, entryName, "paddingTop") * Config.mHeightScaleFactor);
                    }
                    else
                    {
                        paddingTop = (int) (getDimension(prefix, entryName, "paddingTop") * Config.mWidthScaleFactor);
                    }
                }
                catch (Throwable throwable) {
                    if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                }

                try 
                {
                    paddingRight = (int) (getDimension(prefix, entryName, "paddingRight") * Config.mWidthScaleFactor);
                } 
                catch (Throwable throwable) 
                {
                    if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                }

                try 
                {
                    if (useHeight)
                    {
                        paddingBottom = (int) (getDimension(prefix, entryName, "paddingBottom") * Config.mHeightScaleFactor);
                    }
                    else
                    {
                        paddingBottom = (int) (getDimension(prefix, entryName, "paddingBottom") * Config.mWidthScaleFactor);
                    }
                }
                catch (Throwable throwable) 
                {
                    if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                }

                root.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            }

            // Text size scale
            if (root instanceof TextView) 
            {
                try 
                {
                    float fontSize = getDimension(prefix, entryName, "font_size") * Config.mFontScaleFactor;
                    ((TextView) root).setTextSize(fontSize);
                }
                catch (Throwable throwable) 
                {
                    if (LOG_SCALING) Log.e("scaleLayout()", throwable.getMessage());
                }
            }
        } 
        else 
        {
            if (LOG_SCALING)
                Log.d("scaleLayout()", "Skip processing because current view has not tag");
        }

        // If has children view, process them
        if (root instanceof ViewGroup) 
        {
            ViewGroup viewGroup = (ViewGroup) root;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
                scaleLayout(context, prefix, viewGroup.getChildAt(i), true);
        }
    }
	
	private static float getDimension(final String activityName, final String entryName, final String fieldName) throws Throwable 
	{
		String realFieldName = String.format("%s_%s_%s", activityName, entryName, fieldName);
		int id = getDimensionId(realFieldName);
		return mResources.getDimension(id);
	}
	
	private static int getDimensionId(final String fieldName) throws Throwable 
	{
        Class<dimen> dimensionClass = R.dimen.class;

        try 
        {
            java.lang.reflect.Field field = dimensionClass.getField(fieldName);
            return field.getInt(null);
        } 
        catch (NoSuchFieldException e) 
        {
            throw new Throwable("could not find R.dimen." + fieldName);
        } 
        catch (IllegalAccessException e) 
        {
            throw new Throwable("could not find R.dimen." + fieldName);
        }
    }
}
