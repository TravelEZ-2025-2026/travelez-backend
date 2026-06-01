package com.example.travelez.backend.infrastructure.googlecalendar;

public interface GoogleCalendarService {
    boolean hasCalendarScope(Long userId);
    String buildCalendarAuthorizationUrl(Long itineraryId);
    void syncItineraryToCalendar(Long itineraryId, Long userId);
}
