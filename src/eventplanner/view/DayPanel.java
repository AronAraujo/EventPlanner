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

public class DayPanel extends JPanel {

    private EventController controller;
    private LocalDate currentDate;
    private BiConsumer<Event, String> onAction; // action: "edit" or "delete"

    private JLabel lblDate;
    private JPanel eventsContainer;
    private JScrollPane scroll;

    public DayPanel(EventController controller, BiConsumer<Event, String> onAction) {
        this.controller = controller;
        this.onAction = onAction;
        this.currentDate = LocalDate.now();

        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PRIMARY);

        buildHeader();
        buildList();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PRIMARY);
        header.setBorder(new EmptyBorder(20, 20, 14, 20));

        lblDate = new JLabel();
        lblDate.setFont(Theme.fontBold(16));
        lblDate.setForeground(Theme.TEXT_DARK);
        header.add(lblDate, BorderLayout.WEST);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_PRIMARY);
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    private void buildList() {
        eventsContainer = new JPanel();
        eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
        eventsContainer.setBackground(Theme.BG_PRIMARY);
        eventsContainer.setBorder(new EmptyBorder(8, 16, 16, 16));

        scroll = new JScrollPane(eventsContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    public void showDate(LocalDate date) {
        this.currentDate = date;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("pt", "BR"));
        String formatted = date.format(fmt);
        formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
        lblDate.setText(formatted);
        refreshEvents();
    }

    public void refreshEvents() {
        eventsContainer.removeAll();
        List<Event> events = controller.getEventsOnDate(currentDate);

        if (events.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setBackground(Theme.BG_PRIMARY);
            empty.setBorder(new EmptyBorder(40, 0, 0, 0));
            JLabel lbl = new JLabel("Nenhum evento neste dia", SwingConstants.CENTER);
            lbl.setFont(Theme.fontItalic(13));
            lbl.setForeground(Theme.TEXT_LIGHT);
            empty.add(lbl, BorderLayout.CENTER);
            eventsContainer.add(empty);
        } else {
            for (Event e : events) {
                eventsContainer.add(eventCard(e));
                eventsContainer.add(Box.createVerticalStrut(10));
            }
        }

        eventsContainer.revalidate();
        eventsContainer.repaint();
    }

    private JPanel eventCard(Event event) {
        Color catColor = Theme.getCategoryColor(event.getCategory().getColor());

        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Left color bar
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

        // Content
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

        if (!event.getAttendees().isEmpty()) {
            JLabel att = new JLabel("👥 " + event.getAttendees().size() + " participante(s)");
            att.setFont(Theme.fontRegular(11));
            att.setForeground(Theme.TEXT_LIGHT);
            content.add(att);
        }

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);

        JButton btnEdit = iconBtn("✏", Theme.ACCENT);
        JButton btnDel = iconBtn("🗑", new Color(0xE85D75));

        btnEdit.addActionListener(e -> onAction.accept(event, "edit"));
        btnDel.addActionListener(e -> onAction.accept(event, "delete"));

        actions.add(btnEdit);
        actions.add(btnDel);

        card.add(content, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);

        // Hover
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { card.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { card.repaint(); }
        });

        return card;
    }

    private JButton iconBtn(String icon, Color color) {
        JButton btn = new JButton(icon);
        btn.setFont(Theme.fontRegular(14));
        btn.setForeground(color);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(30, 30));
        return btn;
    }
}