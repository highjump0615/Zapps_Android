package com.iliayugai.zapp.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.view.ZappListViewHolder;

import android.app.Activity;

import com.iliayugai.zapp.R;

import java.util.ArrayList;

/**
 * Created by aaa on 14-8-14.
 */
public class ZappAdapter extends ArrayAdapter<ZappData>
{
    private static final String TAG = ZappAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ZappData> mValues;

    public ZappAdapter(Context context, ArrayList<ZappData> values)
    {
        super(context, R.layout.layout_zapp_item, values);

        mContext = context;
        mValues = values;

        if (Config.DEBUG ) Log.d(TAG, "parent context is " + context.toString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;
        ZappListViewHolder viewHolder;

        if (rowView == null)
        {
            rowView = LayoutInflater.from(mContext).inflate(R.layout.layout_zapp_item, null);
            viewHolder = new ZappListViewHolder(rowView, (View.OnClickListener) mContext);

            rowView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ZappListViewHolder) rowView.getTag();
        }

        viewHolder.fillContent(mValues.get(position), position);

//        new FillContentTask(position, viewHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

        return rowView;
    }

//    private class FillContentTask extends AsyncTask<Void, Void, Void> 
//    {
//        private int mPosition;
//        private ZappListViewHolder mHolder;
//
//        public FillContentTask(int position, ZappListViewHolder holder) 
//        {
//            mPosition = position;
//            mHolder = holder;
//        }
//
//        @Override
//        protected Void doInBackground(Void... arg0) 
//        {
////            ZappData zappData = mValues.get(mPosition);
////            String objectId = zappData.user.getObjectId();
////
////            if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
////                blogData.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
////                    @Override
////                    public void done(ParseObject parseObject, ParseException e) {
////
////                        if (e == null) {
////                            ParseUser user = (ParseUser) parseObject;
////                            String objectId = user.getObjectId();
////                            SkilledManager.mParseUserMap.put(objectId, user);
////                        }
////                    }
////                });
////            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) 
//        {
//            ((Activity) mContext).runOnUiThread(new Runnable() 
//            {
//                @Override
//                public void run() {
//                    mHolder.fillContent(mValues.get(mPosition), mPosition);
//                }
//            });
//        }
//    }

}
