package com.iliayugai.zapp;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.CommonUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class MyPushNotificationReceiver extends BroadcastReceiver 
{
	private static final String TAG = MyPushNotificationReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		try 
		{
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");

            Iterator iterator = json.keys();
            while (iterator.hasNext()) 
            {
                String key = (String) iterator.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));

                if (key.equals("notifyType")) 
                {
                    CommonUtils.mStrNotifyType = json.getString(key);
                } 
                else if (key.equals("notifyZapp")) 
                {
                    String strNotifyBlogId = json.getString(key);
                    CommonUtils.mNotifyZappObject = ParseObject.createWithoutData("Zapps", strNotifyBlogId);
                }
            }

            checkNotification(context);

        } 
		catch (JSONException e) 
		{
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
	}
	
	private void checkNotification(Context context) 
	{
        if (!TextUtils.isEmpty(CommonUtils.mStrNotifyType)) 
        {
            if (CommonUtils.mStrNotifyType.equals("zapp")) 
            {
        	    CommonUtils.mNotifyZappObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {

        			@Override
        			public void done(ParseObject parseObject, ParseException e) 
        			{
        				if (e == null)
        				{
        					ZappData zapp = new ZappData(parseObject);
        					CommonUtils.mZappList.add(0, zapp);
        				}
        			}
        	    });

                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("notificationtype", CommonUtils.mStrNotifyType);
                context.startActivity(intent);
            }
            
            CommonUtils.mStrNotifyType = "";
        }
    }
}
