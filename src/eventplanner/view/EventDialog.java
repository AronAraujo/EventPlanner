package eventplanner.view;

import eventplanner.model.Event;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Popup window (JDialog) used both for creating a new event and
 * editing an existing one — the same form serves both cases,
 * depending on whether the constructor receives an event (editing)
 * or null (creating a new one).
 *
 * This is where validation of required fields (title, date,
 * time) happens, implemented in the onSave() method.
 */
public class EventDialog extends JDialog {

    private JTextField txtTitle, txtLocation, txtHour, txtMinute;
    private JTextArea txtDescription;
    private JComboBox<Event.Category> cmbCategory;
    private JComboBox<Event.ReminderTime> cmbReminder;
    private JSpinner spnDate;
    private JTextField txtAttendees;

    private Event result = null;       // created/edited event, read by Main.java after closing
    private Event editing;              // if not null, we are editing this event
    private LocalDate preselectedDate;  // preselected date (from the calendar selection)

    /**
     * @param owner            parent window (used to center the dialog)
     * @param editing          event to edit, or null if creating a new one
     * @param preselectedDate  initial suggested date in the form
     */
    public EventDialog(Frame owner, Event editing, LocalDate preselectedDate) {
        super(owner, editing == null ? "New Event" : "Edit Event", true);
        this.editing = editing;
        this.preselectedDate = preselectedDate;
        buildUI();
        if (editing != null) fillForm(editing); // if editing, pre-fill fields with existing data
        pack();
        setMinimumSize(new Dimension(480, 580));
        setLocationRelativeTo(owner);
    }

    /**
     * Builds the entire form UI: title, input fields,
     * and Cancel/Save buttons at the bottom.
     */
    private void buildUI() {
        setBackground(Theme.BG_PRIMARY);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_PRIMARY);
        root.setBorder(new EmptyBorder(24, 28, 20, 28));

        // dialog title ("New Event" or "Edit Event")
        JLabel title = new JLabel(editing == null ? "New Event" : "Edit Event");
        title.setFont(Theme.fontBold(20));
        title.setForeground(Theme.TEXT_DARK);
        root.add(title, BorderLayout.NORTH);

        // form panel with vertically stacked fields
        JPanel form = new JPanel();
        form.setBackground(Theme.BG_PRIMARY);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 0, 16, 0));

        txtTitle = styledField("Ex: Team meeting");
        txtLocation = styledField("Ex: Room 3 / Online");
        txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(Theme.fontRegular(13));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        txtAttendees = styledField("Name <email>, separated by comma");

        // combos are pre-filled with enum options
        cmbCategory = new JComboBox<>(Event.Category.values());
        cmbCategory.setFont(Theme.fontRegular(13));
        cmbReminder = new JComboBox<>(Event.ReminderTime.values());
        cmbReminder.setFont(Theme.fontRegular(13));

        // date field: JSpinner configured to show only date (dd/MM/yyyy)
        LocalDate startDate = preselectedDate != null ? preselectedDate : LocalDate.now();
        SpinnerDateModel dm = new SpinnerDateModel();
        spnDate = new JSpinner(dm);
        JSpinner.DateEditor de = new JSpinner.DateEditor(spnDate, "dd/MM/yyyy");
        spnDate.setEditor(de);
        spnDate.setFont(Theme.fontRegular(13));

        // convert LocalDate to java.util.Date (required by JSpinner)
        java.util.Date d = java.util.Date.from(startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        spnDate.setValue(d);

        // time field: two simple JTextFields (hour and minute) side by side
        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        timeRow.setBackground(Theme.BG_PRIMARY);
        txtHour = new JTextField("09", 3);
        txtMinute = new JTextField("00", 3);
        styleField(txtHour);
        styleField(txtMinute);
        timeRow.add(txtHour);
        timeRow.add(new JLabel(":"));
        timeRow.add(txtMinute);

        // build full form
        form.add(field("Title *", txtTitle));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Date *", spnDate));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Time *  (HH : MM)", timeRow));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Location", txtLocation));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Category", cmbCategory));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Reminder", cmbReminder));
        form.add(Box.createVerticalStrut(10));
        form.add(fieldArea("Description", txtDescription));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Participants", txtAttendees));

        root.add(form, BorderLayout.CENTER);

        // Cancel / Save buttons aligned to the right
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Theme.BG_PRIMARY);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(Theme.fontRegular(13));
        btnCancel.setForeground(Theme.TEXT_MID);
        btnCancel.setBorderPainted(false);
        btnCancel.setContentAreaFilled(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose()); // closes without saving

        JButton btnSave = roundButton(editing == null ? "Create Event" : "Save");
        btnSave.addActionListener(e -> onSave());

        btns.add(btnCancel);
        btns.add(btnSave);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);
    }

    /**
     * Called when clicking "Create Event"/"Save".
     * Validates required fields and either creates or updates an Event.
     *
     * This is where "Exception Handling" is implemented:
     * each validation shows a friendly message (showError)
     * instead of crashing the program.
     */
    private void onSave() {
        // --- title validation (required) ---
        String titleText = txtTitle.getText().trim();
        if (titleText.isEmpty()) {
            showError("Title cannot be empty.");
            return;
        }

        // --- date validation ---
        LocalDate date;
        try {
            java.util.Date selected = (java.util.Date) spnDate.getValue();
            date = selected.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        } catch (Exception ex) {
            showError("Invalid date.");
            return;
        }

        // --- time validation ---
        LocalTime time;
        try {
            int h = Integer.parseInt(txtHour.getText().trim());
            int m = Integer.parseInt(txtMinute.getText().trim());
            if (h < 0 || h > 23 || m < 0 || m > 59) throw new IllegalArgumentException();
            time = LocalTime.of(h, m);
        } catch (Exception ex) {
            showError("Invalid time. Use 0-23 for hours and 0-59 for minutes.");
            return;
        }

        Event.Category cat = (Event.Category) cmbCategory.getSelectedItem();
        Event.ReminderTime rem = (Event.ReminderTime) cmbReminder.getSelectedItem();

        if (editing != null) {
            // edit mode: update existing event
            editing.setTitle(titleText);
            editing.setDate(date);
            editing.setTime(time);
            editing.setLocation(txtLocation.getText().trim());
            editing.setDescription(txtDescription.getText().trim());
            editing.setCategory(cat);
            editing.setReminder(rem);
            editing.getAttendees().clear(); // avoid duplicates
            parseAttendees(editing);
            result = editing;
        } else {
            // creation mode: build new event
            result = new Event(titleText, date, time,
                txtLocation.getText().trim(),
                txtDescription.getText().trim(), cat, rem);
            parseAttendees(result);
        }
        dispose();
    }

    /**
     * Reads the attendees field (comma-separated text)
     * and adds each entry to the event.
     */
    private void parseAttendees(Event evt) {
        String raw = txtAttendees.getText().trim();
        if (!raw.isEmpty()) {
            for (String a : raw.split(",")) {
                String s = a.trim();
                if (!s.isEmpty()) evt.addAttendee(s);
            }
        }
    }

    /**
     * Fills the form with an existing event (edit mode).
     */
    private void fillForm(Event e) {
        txtTitle.setText(e.getTitle());
        java.util.Date d = java.util.Date.from(e.getDate().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        spnDate.setValue(d);
        txtHour.setText(String.format("%02d", e.getTime().getHour()));
        txtMinute.setText(String.format("%02d", e.getTime().getMinute()));
        txtLocation.setText(e.getLocation());
        txtDescription.setText(e.getDescription());
        cmbCategory.setSelectedItem(e.getCategory());
        cmbReminder.setSelectedItem(e.getReminder());
        txtAttendees.setText(String.join(", ", e.getAttendees()));
    }

    // --- UI styling helpers ---

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(Theme.fontRegular(13));
        f.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
    }

    /** Builds a label + field block used for each form row. */
    private JPanel field(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Theme.BG_PRIMARY);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontBold(12));
        lbl.setForeground(Theme.TEXT_MID);
        p.add(lbl, BorderLayout.NORTH);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(comp, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Same as field(), but for multiline text areas (Description). */
    private JPanel fieldArea(String label, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Theme.BG_PRIMARY);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontBold(12));
        lbl.setForeground(Theme.TEXT_MID);
        p.add(lbl, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(new LineBorder(Theme.BORDER, 1, true));
        p.add(sp, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Rounded primary button used for "Create Event"/"Save". */
    private JButton roundButton(String text) {
        JButton btn = new JButton(text) {
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
        btn.setFont(Theme.fontBold(13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 38));
        return btn;
    }

    /** Shows a friendly error message without exposing technical details. */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /** Final result: the created/edited Event, or null if cancelled. */
    public Event getResult() { return result; }
}