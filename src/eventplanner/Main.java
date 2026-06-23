package eventplanner;

import eventplanner.controller.EventController;
import eventplanner.model.Event;
import eventplanner.view.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Program entry point.
 *
 * Builds the main window: splits the screen into two side-by-side parts
 * (JSplitPane) — calendar on the left, day events on the right —
 * and checks for pending reminders as soon as the program starts.
 */
public class Main {

    public static void main(String[] args) {
        // Ensures execution on Swing's Event Dispatch Thread
        // (all Swing UI must run on this specific thread)
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
     
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // controller is the "brain" of the system — stores events and
        // loads whatever is saved in the CSV file
        EventController controller = new EventController();

        JFrame frame = new JFrame("Event Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setPreferredSize(new Dimension(1100, 700));

        // Main layout: left sidebar (calendar) + right panel (day events)
        // JSplitPane divides the screen into two areas with a draggable divider
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(300);
        split.setDividerSize(1);
        split.setBorder(null);

        // Uses array to allow reference inside lambda before assignment
        DayPanel[] dayPanelRef = new DayPanel[1];

        // Right panel: list of events for the selected day
        // The second parameter is a callback function triggered when the user
        // clicks "Edit" or "Delete" on an event in the list
        dayPanelRef[0] = new DayPanel(controller, (event, action) -> {
            if ("delete".equals(action)) {
                // asks for confirmation before deleting, to avoid accidental clicks
                int opt = JOptionPane.showConfirmDialog(frame,
                    "Delete event \"" + event.getTitle() + "\"?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    controller.deleteEvent(event.getId());
                    dayPanelRef[0].refreshEvents(); // updates the UI list
                }
            } else if ("edit".equals(action)) {
                // opens the same event creation form, but pre-filled
                EventDialog dlg = new EventDialog(frame, event, event.getDate());
                dlg.setVisible(true);
                if (dlg.getResult() != null) {
                    controller.updateEvent(dlg.getResult());
                    dayPanelRef[0].refreshEvents();
                }
            }
        });

        DayPanel dayPanel = dayPanelRef[0];
        dayPanel.showDate(LocalDate.now()); // on startup, show today's date

        // Left panel: calendar + "New Event" button
        // The second parameter is called when the user clicks a date
        // in the calendar — it tells DayPanel to show that day's events
        CalendarPanel calendarPanel = new CalendarPanel(controller, date -> {
            dayPanel.showDate(date);
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Theme.BG_CARD);

        // "+ New Event" button — overrides paintComponent
        JButton btnNew = new JButton("+ New Event") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnNew.setFont(Theme.fontBold(13));
        btnNew.setForeground(Color.WHITE);
        btnNew.setContentAreaFilled(false);
        btnNew.setBorderPainted(false);
        btnNew.setFocusPainted(false);
        btnNew.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNew.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        btnNew.addActionListener(e -> {
            // opens empty form (null = creating new, not editing)
            // already with the date selected in the calendar
            LocalDate selected = calendarPanel.getSelectedDate();
            EventDialog dlg = new EventDialog(frame, null, selected);
            dlg.setVisible(true);
            if (dlg.getResult() != null) {
                controller.addEvent(dlg.getResult());
                calendarPanel.render();        // redraws calendar (may highlight new day)
                dayPanel.showDate(selected);    // updates day list
            }
        });

        // "Search" button — same rounded-corner trick, different color
        JButton btnSearch = new JButton("🔍 Search") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSearch.setFont(Theme.fontRegular(13));
        btnSearch.setForeground(Theme.ACCENT);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearch.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSearch.addActionListener(e -> {
            SearchPanel sp = new SearchPanel(frame, controller);
            sp.setVisible(true);
        });

        // combines both buttons into one panel, stacked vertically
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        btnPanel.setBackground(Theme.BG_CARD);
        btnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 16, 16));
        btnPanel.add(btnNew);
        btnPanel.add(btnSearch);

        leftPanel.add(calendarPanel, BorderLayout.CENTER);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        // builds split: calendar on the left, day events on the right
        split.setLeftComponent(leftPanel);
        split.setRightComponent(dayPanel);

        frame.setContentPane(split);
        frame.pack();
        frame.setLocationRelativeTo(null); // centers the window
        frame.setVisible(true);

        // Checks reminders on startup — only once when program opens
        var reminders = controller.getUpcomingReminders();
        if (!reminders.isEmpty()) {
            StringBuilder sb = new StringBuilder("Reminders in the next 24h:\n");
            reminders.forEach(r -> sb.append("• ").append(r.getTitle()).append(" - ").append(r.getDate()).append("\n"));
            JOptionPane.showMessageDialog(frame, sb.toString(), "Reminders", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}