package com.example.kenneth.examproject.Interfaces;

import com.example.kenneth.examproject.Models.Event;
import java.util.List;

public interface EventSelectorInterface {
    void onEventSelected(int position);
    List<Event> getEventList();
    List<Event> getEventListDay();
    List<Event> getEventListWeek();
    Event getCurrentSelection();
}
