package com.example.kenneth.examproject.Interfaces;

import com.example.kenneth.examproject.Models.Event;

import java.util.ArrayList;

/**
 * Created by kasper on 24/04/15.
 */
public interface EventSelectorInterface {
    public void onEventSelected(int position);
    public ArrayList<Event> getEventList();
    public Event getCurrentSelection();
}
