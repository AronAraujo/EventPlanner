package eventplanner.util;

import eventplanner.model.Event;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for saving and loading the event list from a CSV file.
 *
 * The file is stored at data/events.csv. We use "§" as the separator
 * between fields (instead of the standard comma) because fields such as
 * the description contain free text and may include commas.
 *
 * Each CSV line represents one event, in the following order:
 * id § title § date § time § location § description § category § reminder § attendees
 *
 * The attendees list is stored in the last field, and each
 * attendee inside it is separated by "|".
 */
public class DataManager {

    // Directory and path where the data file is stored
    private static final String DATA_DIR = "data";
    private static final String FILE_PATH = DATA_DIR + "/events.csv";

    // Separator between fields in each CSV line
    private static final String SEPARATOR = "§";

    /**
     * Saves the entire event list to the CSV file, overwriting
     * any previous content. This method is called whenever data changes.
     *
     * @param events list of events to save
     */
    public static void save(List<Event> events) {
        try {
            // Ensures the "data" directory exists before attempting to write
            Files.createDirectories(Paths.get(DATA_DIR));

            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
                // First line of the file is the header
                pw.println("id§title§date§time§location§description§category§reminder§attendees");

                for (Event e : events) {
                    // Joins the attendees list into a single string separated by "|"
                    String attendees = String.join("|", e.getAttendees());

                    // Writes one line per event, following the order defined above
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
            // Prevents the application from crashing if the file cannot be written
            // Only logs the error to the console
            System.err.println("Erro ao salvar dados: " + ex.getMessage());
        }
    }

    /**
     * Loads the event list from the CSV file.
     *
     * If the file does not exist yet,
     * returns an empty list. If any line in the file
     * is corrupted or malformed, that line is ignored and
     * loading continues normally with the remaining lines.
     *
     * @return list of loaded events (empty if no file exists)
     */
    public static List<Event> load() {
        List<Event> events = new ArrayList<>();
        File file = new File(FILE_PATH);

        // File does not exist yet — probably the first execution
        if (!file.exists()) return events;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip the header line
                if (firstLine) { firstLine = false; continue; }

                // Skip empty lines
                if (line.trim().isEmpty()) continue;

                try {
                    // Splits the line into fields using the "§" separator
                    // The "-1" argument ensures trailing empty fields are preserved
                    String[] parts = line.split("\\" + SEPARATOR, -1);

                    // Malformed line — ignore and continue
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

                    // Attendees field is optional — process only if it exists and is not empty
                    if (parts.length > 8 && !parts[8].isEmpty()) {
                        for (String att : parts[8].split("\\|")) {
                            if (!att.trim().isEmpty()) event.addAttendee(att.trim());
                        }
                    }
                    events.add(event);

                } catch (Exception ex) {
                    // If a SINGLE line is corrupted (invalid date, unknown category,
                    // etc.), ignore only that line and continue reading
                    // the rest of the file — this way a partially corrupted
                    // file does not prevent the application from starting
                    System.err.println("Linha inválida ignorada: " + line);
                }
            }
        } catch (IOException ex) {
            System.err.println("Erro ao carregar dados: " + ex.getMessage());
        }
        return events;
    }

    /**
     * Prepares a string to be safely stored in the CSV file.
     * Replaces line breaks with the literal "\n" sequence (otherwise it would break
     * the one-line-per-event format) and removes the "§" separator
     * if it happens to appear in user-entered text.
     */
    private static String escape(String s) {
        return s == null ? "" : s.replace("\n", "\\n").replace(SEPARATOR, " ");
    }

    /**
     * Reverses the escaping performed during saving — converts the literal "\n"
     * sequence back into an actual line break when loading the file.
     */
    private static String unescape(String s) {
        return s == null ? "" : s.replace("\\n", "\n");
    }
}