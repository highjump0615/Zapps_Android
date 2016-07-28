package com.iliayugai.zapp.data;

import java.util.Date;

import com.iliayugai.zapp.utils.CommonUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by aaa on 14-8-14.
 */
public class NotifyData
{
	public ParseUser user;
	public String strUsername;
	public int notificationType;
	public ParseObject zapp;
	public String strAudioFile;
	public int type;
	public Date date;
	public ParseObject object;

	public NotifyData(ParseObject notificationObject)
    {
	    strUsername = notificationObject.getString("username");
	    type = notificationObject.getInt("type");
	    date = notificationObject.getUpdatedAt();
	    zapp = notificationObject.getParseObject("zapp");
	    strAudioFile = notificationObject.getString("zappfile");
	    object = notificationObject;
        
        // set user info
        ParseUser userInfo = notificationObject.getParseUser("user");
        ParseUser userToSet = CommonUtils.mParseUserMap.get(userInfo.getObjectId());
        if (userToSet == null)
        {
        	CommonUtils.mParseUserMap.put(userInfo.getObjectId(), userInfo);
        	userToSet = userInfo;
        }
        user = userToSet;
    }
}
