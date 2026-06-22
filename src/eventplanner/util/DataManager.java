package eventplanner.util;

import eventplanner.model.Event;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static final String DATA_DIR = "data";
    private static final String FILE_PATH = DATA_DIR + "/events.csv";
    private static final String SEPARATOR = "§";

    public static void save(List<Event> events) {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
                pw.println("id§title§date§time§location§description§category§reminder§attendees");
                for (Event e : events) {
                    String attendees = String.join("|", e.getAttendees());
                    pw.printf("%s§%s§%s§%s§%s§%s§%s§%s§%s%n",
                        e.getId(),
                        escape(e.getTitle()),
                        e.getDate(),
                        e.getTime(),
                        escape(e.getLocation()),
                        escape(e.getDescription()),
                        e.getCategory().name(),
                        e.getReminder().name(),
                        attendees
                    );
                }
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar dados: " + ex.getMessage());
        }
    }

    public static List<Event> load() {
        List<Event> events = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return events;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;
                try {
                    String[] parts = line.split("\\" + SEPARATOR, -1);
                    if (parts.length < 8) continue;

                    String id = parts[0];
                    String title = unescape(parts[1]);
                    LocalDate date = LocalDate.parse(parts[2]);
                    LocalTime time = LocalTime.parse(parts[3]);
                    String location = unescape(parts[4]);
                    String description = unescape(parts[5]);
                    Event.Category category = Event.Category.valueOf(parts[6]);
                    Event.ReminderTime reminder = Event.ReminderTime.valueOf(parts[7]);

                    Event event = new Event(title, date, time, location, description, category, reminder);
                    event.setId(id);

                    if (parts.length > 8 && !parts[8].isEmpty()) {
                        for (String att : parts[8].split("\\|")) {
                            if (!att.trim().isEmpty()) event.addAttendee(att.trim());
                        }
                    }
                    events.add(event);
                } catch (Exception ex) {
                    System.err.println("Linha inválida ignorada: " + line);
                }
            }
        } catch (IOException ex) {
            System.err.println("Erro ao carregar dados: " + ex.getMessage());
        }
        return events;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\n", "\\n").replace(SEPARATOR, " ");
    }

    private static String unescape(String s) {
        return s == null ? "" : s.replace("\\n", "\n");
    }
}