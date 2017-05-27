package com.example.kenneth.examproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kenneth.examproject.Models.Event;
import com.example.kenneth.examproject.R;

import java.util.List;

/**
 * Created by Kenneth on 20-04-2017.
 */

public class MonthListAdapter extends BaseAdapter {

    private Context context;
    private List<Event> events;
    private Event event = null;

    public MonthListAdapter(Context c, List<Event> taskList){
        events = taskList;
        context = c;
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
            convertView = eventInflater.inflate(R.layout.month_list_item, null);
        }

        event = events.get(position);
        if(event !=null) {
            TextView nameView = (TextView) convertView.findViewById(R.id.eventNameTV);
            nameView.setText(event.getName());

            TextView dateView = (TextView) convertView.findViewById(R.id.eventDateTV);
            dateView.setText(event.getStartTime().substring(5,10));
        }
        return convertView;
    }
}
