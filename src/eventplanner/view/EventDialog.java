package eventplanner.view;

import eventplanner.model.Event;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class EventDialog extends JDialog {

    private JTextField txtTitle, txtLocation, txtHour, txtMinute;
    private JTextArea txtDescription;
    private JComboBox<Event.Category> cmbCategory;
    private JComboBox<Event.ReminderTime> cmbReminder;
    private JSpinner spnDate;
    private JTextField txtAttendees;

    private Event result = null;
    private Event editing;
    private LocalDate preselectedDate;

    public EventDialog(Frame owner, Event editing, LocalDate preselectedDate) {
        super(owner, editing == null ? "Novo Evento" : "Editar Evento", true);
        this.editing = editing;
        this.preselectedDate = preselectedDate;
        buildUI();
        if (editing != null) fillForm(editing);
        pack();
        setMinimumSize(new Dimension(480, 580));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setBackground(Theme.BG_PRIMARY);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_PRIMARY);
        root.setBorder(new EmptyBorder(24, 28, 20, 28));

        // Header
        JLabel title = new JLabel(editing == null ? "Novo Evento" : "Editar Evento");
        title.setFont(Theme.fontBold(20));
        title.setForeground(Theme.TEXT_DARK);
        root.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setBackground(Theme.BG_PRIMARY);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 0, 16, 0));

        txtTitle = styledField("Ex: Reunião de equipe");
        txtLocation = styledField("Ex: Sala 3 / Online");
        txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(Theme.fontRegular(13));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        txtAttendees = styledField("Nome <email>, separados por vírgula");

        cmbCategory = new JComboBox<>(Event.Category.values());
        cmbCategory.setFont(Theme.fontRegular(13));
        cmbReminder = new JComboBox<>(Event.ReminderTime.values());
        cmbReminder.setFont(Theme.fontRegular(13));

        // Date spinner
        LocalDate startDate = preselectedDate != null ? preselectedDate : LocalDate.now();
        SpinnerDateModel dm = new SpinnerDateModel();
        spnDate = new JSpinner(dm);
        JSpinner.DateEditor de = new JSpinner.DateEditor(spnDate, "dd/MM/yyyy");
        spnDate.setEditor(de);
        spnDate.setFont(Theme.fontRegular(13));
        java.util.Date d = java.util.Date.from(startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        spnDate.setValue(d);

        // Time fields
        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        timeRow.setBackground(Theme.BG_PRIMARY);
        txtHour = new JTextField("09", 3);
        txtMinute = new JTextField("00", 3);
        styleField(txtHour);
        styleField(txtMinute);
        timeRow.add(txtHour);
        timeRow.add(new JLabel(":"));
        timeRow.add(txtMinute);

        form.add(field("Título *", txtTitle));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Data *", spnDate));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Hora *  (HH : MM)", timeRow));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Local", txtLocation));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Categoria", cmbCategory));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Lembrete", cmbReminder));
        form.add(Box.createVerticalStrut(10));
        form.add(fieldArea("Descrição", txtDescription));
        form.add(Box.createVerticalStrut(10));
        form.add(field("Participantes", txtAttendees));

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Theme.BG_PRIMARY);

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(Theme.fontRegular(13));
        btnCancel.setForeground(Theme.TEXT_MID);
        btnCancel.setBorderPainted(false);
        btnCancel.setContentAreaFilled(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = roundButton(editing == null ? "Criar Evento" : "Salvar");
        btnSave.addActionListener(e -> onSave());

        btns.add(btnCancel);
        btns.add(btnSave);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void onSave() {
        String titleText = txtTitle.getText().trim();
        if (titleText.isEmpty()) {
            showError("O título não pode estar vazio.");
            return;
        }

        LocalDate date;
        try {
            java.util.Date selected = (java.util.Date) spnDate.getValue();
            date = selected.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        } catch (Exception ex) {
            showError("Data inválida.");
            return;
        }

        LocalTime time;
        try {
            int h = Integer.parseInt(txtHour.getText().trim());
            int m = Integer.parseInt(txtMinute.getText().trim());
            if (h < 0 || h > 23 || m < 0 || m > 59) throw new IllegalArgumentException();
            time = LocalTime.of(h, m);
        } catch (Exception ex) {
            showError("Hora inválida. Use formato 0-23 para hora e 0-59 para minutos.");
            return;
        }

        Event.Category cat = (Event.Category) cmbCategory.getSelectedItem();
        Event.ReminderTime rem = (Event.ReminderTime) cmbReminder.getSelectedItem();

        if (editing != null) {
            editing.setTitle(titleText);
            editing.setDate(date);
            editing.setTime(time);
            editing.setLocation(txtLocation.getText().trim());
            editing.setDescription(txtDescription.getText().trim());
            editing.setCategory(cat);
            editing.setReminder(rem);
            editing.getAttendees().clear();
            parseAttendees(editing);
            result = editing;
        } else {
            result = new Event(titleText, date, time,
                txtLocation.getText().trim(),
                txtDescription.getText().trim(), cat, rem);
            parseAttendees(result);
        }
        dispose();
    }

    private void parseAttendees(Event evt) {
        String raw = txtAttendees.getText().trim();
        if (!raw.isEmpty()) {
            for (String a : raw.split(",")) {
                String s = a.trim();
                if (!s.isEmpty()) evt.addAttendee(s);
            }
        }
    }

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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public Event getResult() { return result; }
}