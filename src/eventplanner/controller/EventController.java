package eventplanner.controller;

import eventplanner.model.Event;
import eventplanner.util.DataManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the system event list: create, edit, delete, and search
 * (CRUD operations), as well as determining which reminders should be triggered.
 *
 * This class keeps the event list in memory and maintains it
 * synchronized with the CSV file through DataManager — every time
 * something changes (add, edit, or delete), the file is automatically
 * saved again.
 *
 */
public class EventController {

    // List of all events, loaded from the file at startup
    private List<Event> events;

    /**
     * When the controller is created, it immediately loads the saved events.
     * If the file does not exist (first execution), it starts with an empty list.
     */
    public EventController() {
        events = DataManager.load();
    }

    /**
     * Adds a new event and immediately saves it to the file.
     *
     * @param event event to be added
     */
    public void addEvent(Event event) {
        events.add(event);
        DataManager.save(events);
    }

    /**
     * Updates an existing event by locating it through its ID and replacing it
     * with the new data. If the ID is not found, nothing happens.
     *
     * @param updated event containing the updated data (same ID as the original)
     */
    public void updateEvent(Event updated) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(updated.getId())) {
                events.set(i, updated);
                break; // already found it, no need to continue searching
            }
        }
        DataManager.save(events);
    }

    /**
     * Removes an event from the list by ID and saves the change to the file.
     *
     * @param id identifier of the event to remove
     */
    public void deleteEvent(String id) {
        events.removeIf(e -> e.getId().equals(id));
        DataManager.save(events);
    }

    /**
     * Returns all events for a specific date, sorted by
     * time. Used by the panel that
     * displays "events of the day" when the user clicks a date in the calendar.
     *
     * @param date date to filter by
     * @return list of events for that day, sorted by time
     */
    public List<Event> getEventsOnDate(LocalDate date) {
        return events.stream()
            .filter(e -> e.getDate().equals(date))               // only events on this date
            .sorted((a, b) -> a.getTime().compareTo(b.getTime())) // sort by time
            .collect(Collectors.toList());
    }

    /**
     * Returns the list of days that contain at least one
     * event within a specific month/year. Used by the calendar
     * to visually highlight which days have scheduled events (with a dot).
     *
     * @param year year to check
     * @param month month to check (1 to 12)
     * @return list of distinct dates that have events in that month
     */
    public List<LocalDate> getDatesWithEvents(int year, int month) {
        return events.stream()
            .filter(e -> e.getDate().getYear() == year && e.getDate().getMonthValue() == month)
            .map(Event::getDate)
            .distinct() // multiple events on the same day count only once
            .collect(Collectors.toList());
    }

    /**
     * Searches for events containing the keyword in any of the
     * text fields: title, location, description, or category.
     * The search is case-insensitive.
     *
     * @param keyword word or text fragment to search for
     * @return list of matching events
     */
    public List<Event> search(String keyword) {
        String kw = keyword.toLowerCase().trim();
        return events.stream()
            .filter(e ->
                e.getTitle().toLowerCase().contains(kw) ||
                e.getLocation().toLowerCase().contains(kw) ||
                e.getDescription().toLowerCase().contains(kw) ||
                e.getCategory().getLabel().toLowerCase().contains(kw)
            )
            .collect(Collectors.toList());
    }

    /**
     * Checks which events have a reminder that should be triggered within
     * the next 24 hours from now. Since this is the compact version
     * of the project (without a background thread), this check only
     * runs once when the program starts.
     *
     * Logic: each event has a reminder lead time
     * (e.g., 1 hour before). We calculate the exact time when the reminder
     * should be triggered (event time MINUS the lead time), and
     * check whether that time falls between now and the next 24 hours.
     *
     * @return list of events whose reminder falls within the next 24-hour window
     */
    public List<Event> getUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);
        List<Event> reminders = new ArrayList<>();

        for (Event e : events) {
            // combines the event date and time into a single LocalDateTime
            LocalDateTime eventDateTime = LocalDateTime.of(e.getDate(), e.getTime());

            // subtracts the reminder lead time to determine WHEN it should trigger
            LocalDateTime reminderTrigger = eventDateTime.minusHours(e.getReminder().getHoursBeforeEvent());

            // the reminder is included if the trigger time falls
            // between "now" and "now + 24h"
            if (!reminderTrigger.isBefore(now) && !reminderTrigger.isAfter(in24h)) {
                reminders.add(e);
            }
        }
        return reminders;
    }

    /**
     * Returns a copy of the complete event list.
     * We return a copy (new ArrayList<>(events)) instead of the original
     * list to prevent callers from accidentally modifying the controller's
     * internal list.
     *
     * @return copy of the complete event list
     */
    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }
}