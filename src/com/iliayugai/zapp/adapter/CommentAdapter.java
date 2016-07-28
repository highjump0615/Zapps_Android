package com.iliayugai.zapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.iliayugai.zapp.data.CommentData;
import com.iliayugai.zapp.data.NotifyData;
import com.iliayugai.zapp.data.ZappData;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.view.CommentListViewHolder;
import com.iliayugai.zapp.view.IViewHolder;
import com.iliayugai.zapp.view.NotifyListViewHolder;
import com.iliayugai.zapp.view.ZappListViewHolder;
import com.iliayugai.zapp.R;

import java.util.ArrayList;

/**
 * Created by aaa on 14-8-14.
 */
public class CommentAdapter extends BaseAdapter
{
    private static final String TAG = CommentAdapter.class.getSimpleName();

    private static final int TYPE_ZAPP = 0;
    private static final int TYPE_COMMENT = 1;

    private Context mContext;
    private ArrayList<CommentData> mValues;

    private LayoutInflater mInflater;

    private ZappData mZappData;

    public CommentAdapter(Context context, ZappData zData, ArrayList<CommentData> values)
    {
        mContext = context;
        mValues = values;
        mZappData = zData;

        mInflater = LayoutInflater.from(mContext);

        if (Config.DEBUG ) Log.d(TAG, "parent context is " + context.toString());
    }

    @Override
    public int getCount()
    {
        return mValues.size() + 1;
    }

    @Override
    public Object getItem(int i)
    {
        if (i == 0)
        {
            return mZappData;
        }

        return mValues.get(i-1);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        IViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null)
        {
            switch (type)
            {
                case TYPE_ZAPP:
                    convertView = mInflater.inflate(R.layout.layout_zapp_item, null);
                    holder = new ZappListViewHolder(convertView, (View.OnClickListener)mContext);
                    break;

                case TYPE_COMMENT:
                    convertView = mInflater.inflate(R.layout.layout_comment_item, null);
                    holder = new CommentListViewHolder(convertView, (View.OnClickListener)mContext);
                    break;

                default:
                    break;
            }

            convertView.setTag(holder);
        }
        else
        {
            holder = (IViewHolder)convertView.getTag();
        }

        switch (type)
        {
            case TYPE_ZAPP:
                ((ZappListViewHolder)holder).fillContent(mZappData, -1);
                break;

            case TYPE_COMMENT:
                ((CommentListViewHolder)holder).fillContent(mValues.get(position - 1), position);
                break;
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position == 0)
            return TYPE_ZAPP;

        return TYPE_COMMENT;
    }
}
