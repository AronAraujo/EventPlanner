package eventplanner.controller;

import eventplanner.model.Event;
import eventplanner.util.DataManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventController {

    private List<Event> events;

    public EventController() {
        events = DataManager.load();
    }

    public void addEvent(Event event) {
        events.add(event);
        DataManager.save(events);
    }

    public void updateEvent(Event updated) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(updated.getId())) {
                events.set(i, updated);
                break;
            }
        }
        DataManager.save(events);
    }

    public void deleteEvent(String id) {
        events.removeIf(e -> e.getId().equals(id));
        DataManager.save(events);
    }

    public List<Event> getEventsOnDate(LocalDate date) {
        return events.stream()
            .filter(e -> e.getDate().equals(date))
            .sorted((a, b) -> a.getTime().compareTo(b.getTime()))
            .collect(Collectors.toList());
    }

    public List<LocalDate> getDatesWithEvents(int year, int month) {
        return events.stream()
            .filter(e -> e.getDate().getYear() == year && e.getDate().getMonthValue() == month)
            .map(Event::getDate)
            .distinct()
            .collect(Collectors.toList());
    }

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

    // Verifica eventos cujo lembrete cai nas próximas 24h
    public List<Event> getUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);
        List<Event> reminders = new ArrayList<>();

        for (Event e : events) {
            LocalDateTime eventDateTime = LocalDateTime.of(e.getDate(), e.getTime());
            LocalDateTime reminderTrigger = eventDateTime.minusHours(e.getReminder().getHoursBeforeEvent());

            if (!reminderTrigger.isBefore(now) && !reminderTrigger.isAfter(in24h)) {
                reminders.add(e);
            }
        }
        return reminders;
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }
}