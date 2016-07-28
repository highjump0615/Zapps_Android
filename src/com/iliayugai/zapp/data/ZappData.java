package com.iliayugai.zapp.data;

import java.util.Date;

import android.util.Log;

import com.iliayugai.zapp.utils.CommonUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by aaa on 14-8-14.
 */
public class ZappData
{
    public static final int HOME_ALERT = 0;
    public static final int HOME_ALL = 1;
    public static final int HOME_FUN = 2;

    public String strId;
    public String strUsername;
    public int type;
    public String strDescription;
    public ParseFile zappFile;
    public Date date;
    public ParseUser user;

    public ParseObject object;

    public int bLiked; // 1: like, 0: unliked, -1: not determinded
    public int nLikeCount;
    public int nCommentCount;
    public int nPlayCount;
    
    public ParseQuery<ParseObject> mQuery = null;
    public ParseObject likeObject;

//    @property (strong) id tableViewCell;

    public ZappData(ParseObject zappObject)
    {
    	fillData(zappObject);
    }
    
    public void fillData(ParseObject zappObject)
    {
    	strId = zappObject.getObjectId();
        strUsername = zappObject.getString("username");
        type = zappObject.getInt("type");
        strDescription = zappObject.getString("description");
        zappFile = zappObject.getParseFile("zapp");
        date = zappObject.getCreatedAt();        
        object = zappObject;
        bLiked = -1;
        nLikeCount = zappObject.getInt("likecount");
        nCommentCount = zappObject.getInt("commentcount");
        nPlayCount = zappObject.getInt("playcount");
        
        // set user info
        ParseUser userInfo = zappObject.getParseUser("user");
        ParseUser userToSet = CommonUtils.mParseUserMap.get(userInfo.getObjectId());
        if (userToSet == null)
        {
        	CommonUtils.mParseUserMap.put(userInfo.getObjectId(), userInfo);
        	userToSet = userInfo;
        }
        user = userToSet;
    }
}
