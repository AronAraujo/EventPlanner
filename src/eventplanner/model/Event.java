package eventplanner.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Event {

    public enum Category {
        MEETING("Reunião", "#4A90D9"),
        BIRTHDAY("Aniversário", "#E85D75"),
        APPOINTMENT("Consulta", "#7BC67E"),
        PERSONAL("Pessoal", "#F5A623"),
        OTHER("Outro", "#9B59B6");

        private final String label;
        private final String color;

        Category(String label, String color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public String getColor() { return color; }

        @Override
        public String toString() { return label; }
    }

    public enum ReminderTime {
        ONE_HOUR("1 hora antes", 1),
        THREE_HOURS("3 horas antes", 3),
        ONE_DAY("1 dia antes", 24),
        THREE_DAYS("3 dias antes", 72),
        ONE_WEEK("1 semana antes", 168);

        private final String label;
        private final int hoursBeforeEvent;

        ReminderTime(String label, int hoursBeforeEvent) {
            this.label = label;
            this.hoursBeforeEvent = hoursBeforeEvent;
        }

        public String getLabel() { return label; }
        public int getHoursBeforeEvent() { return hoursBeforeEvent; }

        @Override
        public String toString() { return label; }
    }

    private String id;
    private String title;
    private LocalDate date;
    private LocalTime time;
    private String location;
    private String description;
    private Category category;
    private ReminderTime reminder;
    private List<String> attendees;

    public Event(String title, LocalDate date, LocalTime time, String location,
                 String description, Category category, ReminderTime reminder) {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.category = category;
        this.reminder = reminder;
        this.attendees = new ArrayList<>();
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public ReminderTime getReminder() { return reminder; }
    public void setReminder(ReminderTime reminder) { this.reminder = reminder; }

    public List<String> getAttendees() { return attendees; }
    public void setAttendees(List<String> attendees) { this.attendees = attendees; }

    public void addAttendee(String attendee) { this.attendees.add(attendee); }

    @Override
    public String toString() {
        return title + " - " + date + " " + time;
    }
}