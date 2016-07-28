package com.iliayugai.zapp.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.iliayugai.zapp.HomeActivity;
import com.iliayugai.zapp.adapter.ZappAdapter;
import com.iliayugai.zapp.data.CommentData;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.widget.RoundedAvatarDrawable;
import com.iliayugai.zapp.R;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

public class CommonUtils 
{
	static
    {
        System.loadLibrary("mp3lame");
    }
	public native void initEncoder(int numChannels, int sampleRate, int bitRate, int mode, int quality);
    public native void destroyEncoder();
    public native int encodeFile(String sourcePath, String targetPath);
    
    public static boolean mbRecording = false;
    
    static CommonUtils mUtils = null;
    public static HashMap<String, ParseUser> mParseUserMap = new HashMap<String, ParseUser>();
    
    private static final String TAG = CommonUtils.class.getSimpleName();

    public static final int SAMPLE_RATE = 16000;
    
    public static Location currentLocation = null;
    
    public static RoundedAvatarDrawable mDefaultAvatarDrawable = null;
    public static final int GALLERY_OPEN_FOR_PHOTO_REQUEST_CODE = 100;
    
    public static ArrayList<ZappData> mZappList = new ArrayList<ZappData>();
    public static boolean mbNeedRefresh = false;
    
	public static boolean mButtonPressed = false;

    // facebook login
    private static boolean mbSuccessFacebook = false;
    private static ProgressDialog mProgressDialog;
    
    // share
    public static ZappData mZappToShare;
    public static CommentData mCommentToShare;
    
    /* Variables for Push notification */
    public static String mStrNotifyType;
    public static ParseObject mNotifyZappObject;

    public static CommonUtils sharedObject() 
    {
    	if (mUtils == null) 
    	{
    		mUtils = new CommonUtils();
    	}
    	
    	return mUtils;
    }
    
    public void initMp3Encoder()
    {
    	int NUM_CHANNELS = 1;
    	int BITRATE = 128;
    	int MODE = 1;
    	int QUALITY = 2;
    	
    	initEncoder(NUM_CHANNELS, SAMPLE_RATE, BITRATE, MODE, QUALITY);
    }


    /**
     * Move to destination activity class with animate transition.
     */
    public static void moveNextActivity(Activity source, Class<?> destinationClass, boolean removeSource)
    {
        source.startActivity(new Intent(source, destinationClass));

        if (removeSource)
        {
            source.finish();
        }

        source.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    /**
     * Convert image uri to file
     */
    public static String/*File*/ convertImageUriToFile(Context context, Uri imageUri) 
    {
        Cursor cursor = null;
        try 
        {
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID/*, MediaStore.Images.ImageColumns.ORIENTATION*/};
            cursor = context.getContentResolver().query(
                    imageUri,
                    projection, // Which columns to return
                    null,       // WHERE clause; which rows to return (all rows)
                    null,       // WHERE clause selection arguments (none)
                    null);      // Order-by clause (ascending by name)

            int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);

            if (cursor.moveToFirst()) 
            {
                //String orientation = cursor.getString(orientation_ColumnIndex);
                return cursor.getString(file_ColumnIndex)/*new File(cursor.getString(file_ColumnIndex))*/;
            }
            return null;
        }
        finally 
        {
            if (cursor != null) 
            {
                cursor.close();
            }
        }
    }
    
    /**
     * Get bitmap from internal image file.
     */
    public static Bitmap getBitmapFromUri(Uri fileUri) 
    {
        // bitmap factory
        BitmapFactory.Options options = new BitmapFactory.Options();

        // downsizing photoImage as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 8;
        options.inMutable = true;

        return BitmapFactory.decodeFile(fileUri.getPath(), options);
    }
    
    /**
     * directory name to store captured images and videos
     */
    private static final String IMAGE_DIRECTORY_NAME = "zapp_captured_image";
    
    /*
     * returning photoImage / video
     */
    public static File getOutputMediaFile(boolean isImageType/*int type*/) 
    {
        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) 
        {
            if (!mediaStorageDir.mkdirs()) 
            {
                Log.d(TAG, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (isImageType) 
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } 
        else 
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        }

        return mediaFile;
    }

    /**
     * directory name to store captured images and videos
     */
    private static final String RECORD_DIRECTORY_NAME = "zapp_record";

    /*
     * returning photoImage / video
     */
    public static String getRecordFileName(boolean raw)
    {
        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), RECORD_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d(TAG, "Oops! Failed create " + RECORD_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String strFile = mediaStorageDir.getPath() + File.separator + "record.";
        
        if (raw)
        {
        	strFile += "mp3";
        }
        else
        {
        	strFile += "pcm";
        }        	

        return strFile;
    }

    /**
     * Convert dp to pixel unit
     */
    public static int dpToPx(int dp, Context ctx)
    {
        Resources r = ctx.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static Bitmap takeScreenShot(Activity activity)
    {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public static Bitmap fastblur(Bitmap sentBitmap, int radius)
    {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
    
    /**
     * Create error AlertDialog.
     */
    public static Dialog createErrorAlertDialog(final Context context, String title, String message) 
    {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
    }
    
    /**
     * Get passed time from given date
     *
     * @param date
     * @return
     */
    public static String getTimeString(Date date) 
    {
    	String strTime = "";
        Calendar thatDay = Calendar.getInstance();
        thatDay.setTime(date);

        Calendar today = Calendar.getInstance();

        long diff = today.getTimeInMillis() - thatDay.getTimeInMillis(); //result in millis

        long second = diff / 1000;
        int min = (int) second / 60;
        int hour = min / 60;
        int day = hour / 24;
        int month = day / 30;
        int year = month / 12;

        if (min < 60) 
        {
            strTime = String.format("%d min", min);
        }
        else if (min >= 60 && min < 60 * 24) 
        {
            if(hour < 24) 
            {
                strTime = String.format("%d hour", hour);
            }
        }
        else if (day < 31) 
        {
        	strTime = String.format("%d day", day);
        }
        else if (month < 12) 
        {
        	strTime = String.format("%d month", month);
        }
        else 
        {
        	strTime = String.format("%d year", year);
        }
        
        return strTime;
    }
    
    /**
     * Get user name from ParseUser variable.
     */
    public static String getUserNameToShow(ParseUser user) 
    {
        if (user == null) 
        	return "";

        String userName = user.getString("fullname");

        if (!TextUtils.isEmpty(userName))
            return userName;
        else
            return user.getUsername();
    }
    
    /*
     * Helper method to get the Parse GEO point representation of a location
     */
    public static ParseGeoPoint geoPointFromLocation(Location loc) 
    {
    	double dLatitude = 0;
    	double dLongitude = 0;
    	
    	if (loc != null)
    	{
    		dLatitude = loc.getLatitude();
    		dLongitude = loc.getLongitude();
    	}
    	
    	return new ParseGeoPoint(dLatitude, dLongitude);
    }
    
    public static void setUserPhoto(final ParseUser user, final ParseImageView imageView, final String userId)
    {
    	if (mDefaultAvatarDrawable == null)
    	{
    		mDefaultAvatarDrawable = new RoundedAvatarDrawable(Config.mResources, R.drawable.profile_photo_default);
    	}
    	
    	ParseFile fileImgPhoto = user.getParseFile("photothumb");						
		
		if (fileImgPhoto == null)
		{							
			fileImgPhoto = user.getParseFile("photo");
		}
		
		if (fileImgPhoto != null)
		{
			imageView.setParseFile(fileImgPhoto);
			imageView.setPlaceholder(mDefaultAvatarDrawable);

			try
			{			
				imageView.loadInBackground(new GetDataCallback() {

					@Override
					public void done(byte[] bytes, ParseException e) 
					{
						if (e == null)
						{
							if (!userId.equals(user.getObjectId()))
							{
								return;
							}
							
							imageView.setImageDrawable(new RoundedAvatarDrawable(bytes));
						}
					}
					
				});
			}
			catch (RejectedExecutionException ex)
			{
				if (Config.DEBUG)
					ex.printStackTrace();
			}
		}
    }
    
    public static void getLikeCommentInfo(final ZappData zapp, final ZappAdapter adapter)
    {
    	if (zapp.mQuery != null)
    	{
    		try
			{
	    		zapp.mQuery.cancel();
	    		zapp.mQuery = null;
			}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    	
    	zapp.mQuery = ParseQuery.getQuery("Likes");
    	zapp.mQuery.whereEqualTo("zapp", zapp.object);
    	zapp.mQuery.whereEqualTo("user", ParseUser.getCurrentUser());
    	
    	zapp.bLiked = -1;
    	
    	zapp.mQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> zappObjectList, ParseException e) 
            {
            	if (e == null) 
                {
            		if (zappObjectList.size() > 0) 
            		{
            			zapp.likeObject = zappObjectList.get(0);
                        zapp.bLiked = 1;
                    }
                    else 
                    {
                        zapp.bLiked = 0;
                    }
            		
            		adapter.notifyDataSetChanged();
                }
            	else 
                {
                    Log.d(TAG, e.getMessage()); 
                }
            	
            	zapp.mQuery = null;
            }
    	});
    }
    
    public static String getMimeType(Context context, Uri fileUri) 
	{
        ContentResolver cr = context.getContentResolver();
        String mimeType = cr.getType(fileUri);

        Log.d(TAG, "returned mime_type = " + mimeType);
        return mimeType;
    }
    
    //
    // post utility
    //
    public static void sendMail(Context context, String dstAddr, String subject, String text) 
    {
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		
		String[] address = {dstAddr};
		emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, text);
		context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
    
    public static void onMoreReport(Activity activity) 
    {
        sendMail(activity, "info@skilledapp.co", "Report", "");
    }
    
    public static void startBufferedWrite(final AudioRecord recorder, final Context context)
    {
        int nMinBufferSize = AudioRecord.getMinBufferSize(CommonUtils.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recorder.startRecording();

        final File fileRecord = new File(CommonUtils.getRecordFileName(false));
        if (fileRecord.exists())
        {
            fileRecord.delete();
        }
        final short[] buffer = new short[nMinBufferSize];

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DataOutputStream output = null;
                try
                {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileRecord)));
                    while (mbRecording)
                    {
                        int readSize = recorder.read(buffer, 0, buffer.length);
                        for (int i = 0; i < readSize; i++)
                        {
                            output.writeShort(buffer[i]);
                        }

                        Log.d(TAG, String.format("writing %d bytes", readSize));
                    }
                }
                catch (IOException e)
                {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    if (output != null)
                    {
                        try
                        {
                            output.flush();
                        }
                        catch (IOException e)
                        {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        finally
                        {
                            try
                            {
                                output.close();
                            }
                            catch (IOException e)
                            {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        }).start();
    }
    
    /**
     * Makes HttpURLConnection and returns InputStream
     */
    private static InputStream getHttpConnection(String urlString) throws IOException 
    {
        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try 
        {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) 
            {
                stream = httpConnection.getInputStream();
            }
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        return stream;
    }
    
    /**
     * Read photoImage data from web url
     */
    public static byte[] downloadRemoteFile(String fileUrl) 
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try 
        {
            Log.d(TAG, "download remote file URL = " + fileUrl);

            InputStream inputStream = null;

            try 
            {
                inputStream = getHttpConnection(fileUrl);

                byte[] bytes = new byte[1024];

                while (inputStream.read(bytes) != -1)
                    byteArrayOutputStream.write(bytes);
            } 
            catch (IOException e1) 
            {
                e1.printStackTrace();
            }

            if (inputStream != null) inputStream.close();
        } 
        catch (Exception e) 
        {
            if (Config.DEBUG) e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }
    
    /**
     * AsyncTask to get image data from remote file.
     */
    public static class GetImageDataTask extends AsyncTask<String, Void, byte[]> 
    {
        @Override
        protected byte[] doInBackground(String... params) 
        {
            return downloadRemoteFile(params[0]);
        }

    }

    
	////////////////////////////////////////////////////////////////////////////////
	// Facebook Post
	////////////////////////////////////////////////////////////////////////////////
	
	public static boolean hasPublishPermission() 
	{
		Session session = ParseFacebookUtils.getSession();
		return session != null && session.getPermissions().contains("email") && session.getPermissions().contains("publish_actions") && session.getPermissions().contains("publish_stream");
	}
	
	private static void onLoadingStopped() 
	{
        if (mProgressDialog != null)
        {
        	mProgressDialog.dismiss();
        	mProgressDialog = null;
        }
        mButtonPressed = false;
    }
	
	private static void makeUserWithFacebookInfo(final Activity activity) 
	{
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(final GraphUser user, Response response) 
                    {
                        if (user != null) 
                        {
                            final ParseUser currentUser = ParseUser.getCurrentUser();

                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            //query.orderByAscending("email");
                            query.whereEqualTo("email", user.getProperty("email"));
                            query.countInBackground(new CountCallback() 
                            {
                                @Override
                                public void done(int i, ParseException e) 
                                {
                                    if (e == null && i > 0) 
                                    {
                                        CommonUtils.createErrorAlertDialog(activity, "Alert", String.format("%s is already existing", user.getProperty("email"))).show();

                                        currentUser.deleteInBackground();
                                        Session.getActiveSession().closeAndClearTokenInformation();
                                        onLoadingStopped();
                                    } 
                                    else 
                                    {
                                        if (user.getUsername() != null)
                                            currentUser.setUsername(user.getUsername());

                                        if (user.getName() != null)
                                            currentUser.put("fullname", user.getName());

                                        currentUser.setEmail((String) user.getProperty("email"));

                                        // Populate the JSON object
                                        String facebookId = user.getId();

                                        String profilePictureURL = String.format("https://graph.facebook.com/%s/picture?type=large&return_ssl_resources=1", facebookId);

                                        CommonUtils.GetImageDataTask getImageDataTask = new CommonUtils.GetImageDataTask();
                                        getImageDataTask.execute(profilePictureURL);
                                        try 
                                        {
                                            byte[] imageData = getImageDataTask.get();

                                            if (imageData != null) 
                                            {
                                                ParseFile photoFile = new ParseFile(imageData);
                                                currentUser.put("photo", photoFile);
                                            }
                                        } 
                                        catch (InterruptedException e1) 
                                        {
                                            e1.printStackTrace();
                                        } 
                                        catch (ExecutionException e1) 
                                        {
                                            e1.printStackTrace();
                                        }

                                        // By specifying no write privileges for the ACL, we can ensure the role cannot be altered.
                                        ParseACL roleACL = new ParseACL();
                                        roleACL.setPublicReadAccess(true);
                                        roleACL.setReadAccess(currentUser, true);
                                        roleACL.setWriteAccess(currentUser, true);
                                        currentUser.setACL(roleACL);
                                        currentUser.put("distanceFilter", false);
                                        currentUser.put("distance", 50);

                                        currentUser.saveInBackground();

                                        onLoadingStopped();
                                        
                                        Log.d(TAG, "Move to home activity");
                                        
                                        // Show the user info
                                        CommonUtils.moveNextActivity(activity, HomeActivity.class, true);
                                    }
                                }
                            });
                        } 
                        else if (response.getError() != null) 
                        {
                            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY) || 
                            	(response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) 
                            {
                                Log.d(TAG, "The facebook session was invalidated.");
                                ParseUser.logOut();
                            } 
                            else 
                            {
                                Log.d(TAG, "Some other error: " + response.getError().getErrorMessage());
                            }

                            onLoadingStopped();
                        }
                    }
                }
        );
        request.executeAsync();
    }
	
	public static void onFacebookLogin(final Activity activity)
	{
		mbSuccessFacebook = false;
		
		mProgressDialog = ProgressDialog.show(activity, "", "Loading Facebook...");
		
//		List<String> permissions = Arrays.asList(
//                ParseFacebookUtils.Permissions.User.ABOUT_ME,
//                ParseFacebookUtils.Permissions.User.EMAIL,
//                ParseFacebookUtils.Permissions.User.LOCATION);

//        ParseFacebookUtils.logIn(permissions, this, new LogInCallback()
		ParseFacebookUtils.logIn(activity, new LogInCallback()
        {
            @Override
            public void done(final ParseUser user, ParseException err) 
            {
                if (user == null) 
                {
                    onLoadingStopped();

                    if (Config.DEBUG) 
                    	Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                } 
                else if (user.isNew()) 
                {
                    if (Config.DEBUG) 
                    	Log.d(TAG, "User signed up and logged in through Facebook!");

                    if (!CommonUtils.hasPublishPermission()) 
                    {
                    	mProgressDialog.setMessage("Setting Permissons...");
                    	
                        // Request publish permission
                        Session.NewPermissionsRequest newPermissionsRequest =
                                new Session.NewPermissionsRequest(activity, Arrays.asList("publish_actions", "publish_stream", "email"));
                        newPermissionsRequest.setCallback(new Session.StatusCallback() 
                        {
                            @Override
                            public void call(Session session, SessionState state, Exception exception) 
                            {
                            	final ParseUser currentUser = ParseUser.getCurrentUser();
                            	
                                if (exception == null) 
                                {
                                    ParseFacebookUtils.saveLatestSessionData(user);

                                    // Fetch Facebook user info if the session is active
                                    if (session != null && session.isOpened()) 
                                    {
                                    	Log.d(TAG, "makeUserWithFacebookInfo in call");
                                        makeUserWithFacebookInfo(activity);
                                        mbSuccessFacebook = true;
                                    } 
                                    else 
                                    {
                                    	if (!mbSuccessFacebook)
                                    	{
	                                    	Log.d(TAG, "onLoadingStopped in call");
	                                        onLoadingStopped();
	                                        
	                                        currentUser.logOut();
	                                        currentUser.deleteInBackground();
                                    	}
                                    }
                                } 
                                else 
                                {
                                    CommonUtils.createErrorAlertDialog(activity, "Alert", "Could not request publish permissions").show();
                                    onLoadingStopped();
                                    
                                    currentUser.logOut();
                                    currentUser.deleteInBackground();
                                }
                            }
                        });
                        ParseFacebookUtils.getSession().requestNewPublishPermissions(newPermissionsRequest);
                    } 
                    else 
                    {
                        ParseFacebookUtils.saveLatestSessionData(user);

                        // Fetch Facebook user info if the session is active
                        if (ParseFacebookUtils.getSession() != null && ParseFacebookUtils.getSession().isOpened()) 
                        {
                        	Log.d(TAG, "makeUserWithFacebookInfo in else");
                            makeUserWithFacebookInfo(activity);
                        } 
                        else 
                        {
                        	Log.d(TAG, "onLoadingStopped in else");
                            onLoadingStopped();
                        }
                    }
                } 
                else 
                {
                    onLoadingStopped();

                    if (Config.DEBUG) Log.d(TAG, "User logged in through Facebook!");

                    CommonUtils.moveNextActivity(activity, HomeActivity.class, true);
                }
            }
        });
	}

	/*
     * Twitter in Parse
     */
    public void onTwitterLogin(final Activity activity) 
    {
        mProgressDialog = ProgressDialog.show(activity, "", "Loading Twitter...");

        ParseTwitterUtils.logIn(activity, new LogInCallback() 
        {
            @Override
            public void done(ParseUser user, ParseException err) 
            {
                if (user == null) 
                {
                    Log.d(TAG, "Uh oh. The user cancelled the Twitter login.");
                    onLoadingStopped();
                } 
                else if (user.isNew()) 
                {
                    Log.d(TAG, "User signed up and logged in through Twitter!");

                    makeUserWithTwitterInfo(activity);
                } 
                else 
                {
                    Log.d(TAG, "User logged in through Twitter!");

                    onLoadingStopped();
                    CommonUtils.moveNextActivity(activity, HomeActivity.class, true);
                }
            }
        });
    }
    
    private class GetHttpResponseTask extends AsyncTask<String, Void, String> 
    {
        @Override
        protected String doInBackground(String... params) 
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet verifyGet = new HttpGet(params[0]);
            ParseTwitterUtils.getTwitter().signRequest(verifyGet);

            try 
            {
                HttpResponse response = client.execute(verifyGet);
                if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
                {
                    return EntityUtils.toString(response.getEntity());
                }
            } 
            catch (IOException e) 
            {
                if (Config.DEBUG) e.printStackTrace();
            }

            return null;
        }
    }
    
    private void makeUserWithTwitterInfo(final Activity activity) 
    {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.setUsername(ParseTwitterUtils.getTwitter().getScreenName());

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", currentUser.getUsername());
        query.countInBackground(new CountCallback() 
        {
            @Override
            public void done(int i, ParseException e) 
            {
                if (e == null && i > 0) 
                {
                    CommonUtils.createErrorAlertDialog(activity, "Alert", String.format("%s is already existing", currentUser.getUsername())).show();

                    currentUser.deleteInBackground();
                    onLoadingStopped();
                } 
                else 
                {
                    String urlShow = String.format("https://api.twitter.com/1.1/users/show.json?screen_name=%s", currentUser.getUsername());
                    GetHttpResponseTask asyncTask = new GetHttpResponseTask();

                    try 
                    {
                        asyncTask.execute(urlShow);
                        String result = asyncTask.get();

                        if (result != null) 
                        {
                            JSONObject resultObject = new JSONObject(result);

                            currentUser.put("fullname", resultObject.getString("name"));

                            String profilePictureURL = resultObject.getString("profile_image_url_https");

                            CommonUtils.GetImageDataTask getImageDataTask = new CommonUtils.GetImageDataTask();
                            getImageDataTask.execute(profilePictureURL);
                            try 
                            {
                                byte[] imageData = getImageDataTask.get();

                                if (imageData != null) 
                                {
                                    ParseFile photoFile = new ParseFile(imageData);
                                    currentUser.put("photo", photoFile);
                                }
                            } 
                            catch (InterruptedException e1) 
                            {
                                e1.printStackTrace();
                            } 
                            catch (ExecutionException e1) 
                            {
                                e1.printStackTrace();
                            }

                            // By specifying no write privileges for the ACL, we can ensure the role cannot be altered.
                            ParseACL roleACL = new ParseACL();
                            roleACL.setPublicReadAccess(true);
                            roleACL.setReadAccess(currentUser, true);
                            roleACL.setWriteAccess(currentUser, true);
                            currentUser.setACL(roleACL);
                            
                            currentUser.put("distanceFilter", false);
                            currentUser.put("distance", 50);

                            currentUser.saveInBackground();

                            // Show the user info
                            CommonUtils.moveNextActivity(activity, HomeActivity.class, true);
                        }
                    } 
                    catch (InterruptedException e1) 
                    {
                        e1.printStackTrace();
                    }
                    catch (ExecutionException e1) 
                    {
                        e1.printStackTrace();
                    } 
                    catch (JSONException e1) 
                    {
                        e1.printStackTrace();
                    }

                    onLoadingStopped();
                }
            }
        });
    }
    
    public static void shareToFacebook(final Activity activity, UiLifecycleHelper uiHelper)
    {
    	ParseUser currentUser = ParseUser.getCurrentUser();
    	
    	
    	
    	if (FacebookDialog.canPresentShareDialog(activity.getApplicationContext(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) 
    	{
		    // Publish the post using the Share Dialog
		    FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(activity)
//		    		.setApplicationName(applicationName)
		    		.setDescription(mZappToShare.strDescription)
		    		.setLink(mZappToShare.zappFile.getUrl())
		            .build();
//		    uiHelper.trackPendingDialogCall(shareDialog.present());
		    
		    shareDialog.present();
    	} 
    	else 
    	{
    		if (!ParseFacebookUtils.isLinked(currentUser)) 
        	{
                createErrorAlertDialog(activity, "Warning",
                        "Facebook share is only available for the users logged in with Facebook").show();
                return;                    
        	}
    		
			Bundle params = new Bundle();
//		    params.putString("name", "Facebook SDK for Android");
//		    params.putString("caption", "Build great social apps and get more installs.");
		    params.putString("description", mZappToShare.strDescription);
		    params.putString("link", mZappToShare.zappFile.getUrl());
//		    params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

		    WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(activity, Session.getActiveSession(), params))
		    		.setOnCompleteListener(new WebDialog.OnCompleteListener()
		    		{
	                    @Override
	                    public void onComplete(Bundle values, FacebookException error) 
	                    {
	                        if (error == null) 
	                        {
	                            // When the story is posted, echo the success
	                            // and the post Id.
	                            final String postId = values.getString("post_id");
	                            if (postId != null) 
	                            {
	                            	Toast.makeText(activity, "Posted to Facebook Successfully", Toast.LENGTH_LONG).show();
	                            } 
	                            else 
	                            {
	                                // User clicked the Cancel button
	                            	Toast.makeText(activity, "Cancelled publishing", Toast.LENGTH_LONG).show();
	                            }
	                        } 
	                        else if (error instanceof FacebookOperationCanceledException) 
	                        {
	                            // User clicked the "x" button
	                        	Toast.makeText(activity, "Cancelled publishing", Toast.LENGTH_LONG).show();
	                        } 
	                        else 
	                        {
	                            // Generic, ex: network error
	                        	Toast.makeText(activity, "Error occured publishing", Toast.LENGTH_LONG).show();
	                        }
	                    }
	                }).build();
		    
		    feedDialog.show();
		}
    }
    
    public static void shareToTwitter(final Activity activity)
    {
    	String twitterLink;

        twitterLink = String.format("http://www.twitter.com/intent/tweet?url=%s&text=%s", 
                    CommonUtils.mZappToShare.zappFile.getUrl(), CommonUtils.mZappToShare.strDescription);

        Log.d(TAG, "twitter post link = " + twitterLink);

        try 
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterLink));
            activity.startActivity(intent);
        } 
        catch (Exception e) 
        {
        	Toast.makeText(activity, "Exception occured : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
