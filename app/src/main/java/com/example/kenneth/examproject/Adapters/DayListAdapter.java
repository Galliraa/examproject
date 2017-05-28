package com.example.kenneth.examproject.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.kenneth.examproject.Models.Event;
import com.example.kenneth.examproject.R;

import java.util.List;

import static com.example.kenneth.examproject.R.id.parent;

/**
 * Created by Kenneth on 20-04-2017.
 */

public class DayListAdapter extends BaseAdapter {

    private Context context;
    private List<Event> events;
    private Event event = null;
    private NetworkImageView imageView;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;


    public DayListAdapter(Context c, List<Event> taskList){
        events = taskList;
        context = c;

        mRequestQueue = Volley.newRequestQueue(c);
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get("url");
            }
        });
    }
    @Override
    public int getCount()
    {
        if(events != null)
            return events.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position)
    {
        if(events != null)
            return events.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView ==  null) {
            LayoutInflater eventInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = eventInflater.inflate(R.layout.day_list_item, null);
        }

        event = events.get(position);
        if(event !=null) {

            //ImageView eventImageView = (ImageView) convertView.findViewById(R.id.eventIV);
            //eventImageView.setImageBitmap(event.getEventImage());

            TextView nameView = (TextView) convertView.findViewById(R.id.eventNameTV);
            if (event.getName() != null)
                nameView.setText(event.getName());
            else
                nameView.setText(context.getString(R.string.noName));

            TextView dateView = (TextView) convertView.findViewById(R.id.eventDateTV);
            if (event.getStartTime() != null)
                dateView.setText(context.getString(R.string.dateDesc)+" "+event.getStartTime().substring(5,10)+" "+context.getString(R.string.timeDesc)+" "+event.getStartTime().substring(11,16));
            else
                dateView.setText(context.getString(R.string.noDate));

            TextView descView = (TextView) convertView.findViewById(R.id.eventDescTV);
            if (event.getDescrition() != null)
            {
                String s;
                if(event.getDescrition().length()>100) {
                    s = event.getDescrition().substring(0, 100);
                }
                else
                    s = event.getDescrition();
                descView.setText(s);
            }

            else
                descView.setText(context.getString(R.string.noDesc));


            imageView = (NetworkImageView) convertView.findViewById(R.id.eventIV);
            imageView.setImageUrl(event.getEventImage(), mImageLoader);
        }
        return convertView;
    }
}
