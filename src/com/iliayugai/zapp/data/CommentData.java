package com.iliayugai.zapp.data;

import java.util.Date;

import com.iliayugai.zapp.utils.CommonUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by aaa on 14-8-17.
 */
public class CommentData
{
	public String strId;
	public String strUsername;
	public String strContent;
	public int type;
	public Date date;
	public ParseUser user;
	public ParseObject object;
	public ParseFile voiceFile;

	public CommentData(ParseObject commentObject)
    {
		strId = commentObject.getObjectId();
	    strUsername = commentObject.getString("username");
	    strContent = commentObject.getString("content");
	    type = commentObject.getInt("type");
	    date = commentObject.getCreatedAt();
	    object = commentObject;
	    voiceFile = commentObject.getParseFile("voice");
	    
	    // set user info
        ParseUser userInfo = commentObject.getParseUser("user");
        ParseUser userToSet = CommonUtils.mParseUserMap.get(userInfo.getObjectId());
        if (userToSet == null)
        {
        	CommonUtils.mParseUserMap.put(userInfo.getObjectId(), userInfo);
        	userToSet = userInfo;
        }
        user = userToSet;
    }
}
