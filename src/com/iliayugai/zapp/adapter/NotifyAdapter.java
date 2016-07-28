package com.iliayugai.zapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.iliayugai.zapp.data.NotifyData;
import com.iliayugai.zapp.utils.Config;
import com.iliayugai.zapp.view.NotifyListViewHolder;
import com.iliayugai.zapp.R;

import java.util.ArrayList;

/**
 * Created by aaa on 14-8-14.
 */
public class NotifyAdapter extends ArrayAdapter<NotifyData>
{
    private static final String TAG = NotifyAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<NotifyData> mValues;

    public NotifyAdapter(Context context, ArrayList<NotifyData> values)
    {
        super(context, R.layout.layout_notify_item, values);

        mContext = context;
        mValues = values;

        if (Config.DEBUG ) Log.d(TAG, "parent context is " + context.toString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;
        NotifyListViewHolder viewHolder;

        if (rowView == null)
        {
            rowView = LayoutInflater.from(mContext).inflate(R.layout.layout_notify_item, null);
            viewHolder = new NotifyListViewHolder(rowView, (View.OnClickListener) mContext);

            rowView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (NotifyListViewHolder) rowView.getTag();
        }

        viewHolder.fillContent(mValues.get(position), position);

//        new FillContentTask(position, viewHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

        return rowView;
    }
}
