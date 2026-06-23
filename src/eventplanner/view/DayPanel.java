package eventplanner.view;

import eventplanner.controller.EventController;
import eventplanner.model.Event;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Right-side panel of the main window: displays the list of
 * events for the currently selected date in the calendar.
 *
 * Each event appears as a "card" with title, time, location,
 * category (with color), and Edit/Delete buttons. When the list
 * is empty, it shows a message indicating that there are no events.
 */
public class DayPanel extends JPanel {

    private EventController controller;
    private LocalDate currentDate; // currently displayed day
    private BiConsumer<Event, String> onAction; // callback: action is "edit" or "delete"

    private JLabel lblDate;          // title "Sunday, 28 June"
    private JPanel eventsContainer;  // where event cards are placed
    private JScrollPane scroll;      // enables scrolling if many events exist

    public DayPanel(EventController controller, BiConsumer<Event, String> onAction) {
        this.controller = controller;
        this.onAction = onAction;
        this.currentDate = LocalDate.now();

        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PRIMARY);

        buildHeader();
        buildList();
    }

    /** Builds the header with the day name and a separator line. */
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PRIMARY);
        header.setBorder(new EmptyBorder(20, 20, 14, 20));

        lblDate = new JLabel();
        lblDate.setFont(Theme.fontBold(16));
        lblDate.setForeground(Theme.TEXT_DARK);
        header.add(lblDate, BorderLayout.WEST);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_PRIMARY);
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    /** Builds the scrollable area where event cards are displayed. */
    private void buildList() {
        eventsContainer = new JPanel();
        // Vertical BoxLayout: stacks cards vertically
        eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
        eventsContainer.setBackground(Theme.BG_PRIMARY);
        eventsContainer.setBorder(new EmptyBorder(8, 16, 16, 16));

        scroll = new JScrollPane(eventsContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Changes the displayed date and updates the event list.
     * Called from Main.java when the user clicks a date in the calendar.
     *
     * @param date new date to display
     */
    public void showDate(LocalDate date) {
        this.currentDate = date;
        // formats date in Portuguese style, e.g. "Sunday, 28 June"
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("pt", "BR"));
        String formatted = date.format(fmt);
        formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
        lblDate.setText(formatted);
        refreshEvents();
    }

    /**
     * Rebuilds the list of event cards for the current day.
     * Called whenever something changes (create, edit, delete event)
     * so the UI always reflects the latest state.
     */
    public void refreshEvents() {
        eventsContainer.removeAll();
        List<Event> events = controller.getEventsOnDate(currentDate);

        if (events.isEmpty()) {
            // friendly message when there are no events on this day
            JPanel empty = new JPanel(new BorderLayout());
            empty.setBackground(Theme.BG_PRIMARY);
            empty.setBorder(new EmptyBorder(40, 0, 0, 0));
            JLabel lbl = new JLabel("No events on this day", SwingConstants.CENTER);
            lbl.setFont(Theme.fontItalic(13));
            lbl.setForeground(Theme.TEXT_LIGHT);
            empty.add(lbl, BorderLayout.CENTER);
            eventsContainer.add(empty);
        } else {
            for (Event e : events) {
                eventsContainer.add(eventCard(e));
                eventsContainer.add(Box.createVerticalStrut(10)); // space between cards
            }
        }

        eventsContainer.revalidate();
        eventsContainer.repaint();
    }

    /**
     * Creates the visual card for an event: title, time/location, category,
     * participant count (if any), and action buttons.
     *
     * @param event event to represent
     * @return panel ready to be added to the list
     */
    private JPanel eventCard(Event event) {
        Color catColor = Theme.getCategoryColor(event.getCategory().getColor());

        // overridden paintComponent to draw rounded background
        // and a colored left bar (category color)
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // left-side color bar indicating event category
                g2.setColor(catColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // central block: title, time/location, category, participants
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleLbl = new JLabel(event.getTitle());
        titleLbl.setFont(Theme.fontBold(14));
        titleLbl.setForeground(Theme.TEXT_DARK);

        String timeStr = event.getTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLbl = new JLabel("🕐 " + timeStr +
            (event.getLocation().isEmpty() ? "" : "  •  📍 " + event.getLocation()));
        timeLbl.setFont(Theme.fontRegular(12));
        timeLbl.setForeground(Theme.TEXT_MID);

        JLabel catLbl = new JLabel(event.getCategory().getLabel());
        catLbl.setFont(Theme.fontRegular(11));
        catLbl.setForeground(catColor);
        catLbl.setBorder(new EmptyBorder(3, 0, 0, 0));

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(3));
        content.add(timeLbl);
        content.add(catLbl);

        // only shows participants line if any are registered
        if (!event.getAttendees().isEmpty()) {
            JLabel att = new JLabel("👥 " + event.getAttendees().size() + " participant(s)");
            att.setFont(Theme.fontRegular(11));
            att.setForeground(Theme.TEXT_LIGHT);
            content.add(att);
        }

        // right block: action buttons (Edit / Delete)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);

        // we use text ("Edit"/"Delete") instead of emojis (✏/🗑) because
        // emojis may not render correctly on all systems/fonts,
        // sometimes appearing as "..." on some Windows machines
        JButton btnEdit = iconBtn("Edit", Theme.ACCENT);
        JButton btnDel = iconBtn("Delete", new Color(0xE85D75));

        // triggers callback received in constructor (actual handling is done
        // in Main.java, which decides what to do)
        btnEdit.addActionListener(e -> onAction.accept(event, "edit"));
        btnDel.addActionListener(e -> onAction.accept(event, "delete"));

        actions.add(btnEdit);
        actions.add(btnDel);

        card.add(content, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);

        // subtle visual hover effect on the card
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { card.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { card.repaint(); }
        });

        return card;
    }

    /**
     * Creates a simple action button (no background/border),
     * used for "Edit" and "Delete" in each event card.
     */
    private JButton iconBtn(String label, Color color) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.fontRegular(12));
        btn.setForeground(color);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(60, 30)); // wider to fit text
        return btn;
    }
}