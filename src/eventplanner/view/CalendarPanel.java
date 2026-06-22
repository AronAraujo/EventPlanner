package eventplanner.view;

import eventplanner.controller.EventController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class CalendarPanel extends JPanel {

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private EventController controller;
    private Consumer<LocalDate> onDateSelected;

    private JLabel lblMonth;
    private JPanel gridPanel;

    private static final String[] DAYS = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};

    public CalendarPanel(EventController controller, Consumer<LocalDate> onDateSelected) {
        this.controller = controller;
        this.onDateSelected = onDateSelected;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_CARD);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        buildHeader();
        buildGrid();
        render();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_CARD);
        header.setBorder(new EmptyBorder(20, 20, 12, 20));

        lblMonth = new JLabel("", SwingConstants.CENTER);
        lblMonth.setFont(Theme.fontBold(16));
        lblMonth.setForeground(Theme.TEXT_DARK);

        JButton prev = navBtn("‹");
        JButton next = navBtn("›");
        prev.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); render(); });
        next.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); render(); });

        JButton todayBtn = new JButton("Hoje");
        todayBtn.setFont(Theme.fontRegular(12));
        todayBtn.setForeground(Theme.ACCENT);
        todayBtn.setBorderPainted(false);
        todayBtn.setContentAreaFilled(false);
        todayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        todayBtn.addActionListener(e -> {
            currentMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            render();
            if (onDateSelected != null) onDateSelected.accept(selectedDate);
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setBackground(Theme.BG_CARD);
        left.add(prev);
        left.add(next);
        left.add(todayBtn);

        header.add(left, BorderLayout.WEST);
        header.add(lblMonth, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
    }

    private void buildGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_CARD);
        wrapper.setBorder(new EmptyBorder(0, 12, 12, 12));

        // Day names row
        JPanel dayNames = new JPanel(new GridLayout(1, 7, 4, 0));
        dayNames.setBackground(Theme.BG_CARD);
        for (String d : DAYS) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(Theme.fontBold(11));
            lbl.setForeground(Theme.TEXT_LIGHT);
            lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
            dayNames.add(lbl);
        }
        wrapper.add(dayNames, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(6, 7, 4, 4));
        gridPanel.setBackground(Theme.BG_CARD);
        wrapper.add(gridPanel, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    public void render() {
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);
        lblMonth.setText(monthName + " " + currentMonth.getYear());

        gridPanel.removeAll();

        List<LocalDate> datesWithEvents = controller.getDatesWithEvents(
            currentMonth.getYear(), currentMonth.getMonthValue());

        LocalDate first = currentMonth.atDay(1);
        int startDow = first.getDayOfWeek().getValue() % 7; // 0=Dom

        LocalDate today = LocalDate.now();

        // Fill empty cells before first day
        for (int i = 0; i < startDow; i++) {
            gridPanel.add(emptyCell());
        }

        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selectedDate);
            boolean hasEvents = datesWithEvents.contains(date);
            gridPanel.add(dayCell(date, isToday, isSelected, hasEvents));
        }

        // Fill remaining cells
        int used = startDow + daysInMonth;
        int remaining = 42 - used;
        for (int i = 0; i < remaining; i++) {
            gridPanel.add(emptyCell());
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel dayCell(LocalDate date, boolean isToday, boolean isSelected, boolean hasEvents) {
        JPanel cell = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                if (isSelected) bg = Theme.SELECTED_BG;
                else if (isToday) bg = Theme.ACCENT_LIGHT;
                else bg = Theme.BG_CARD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cell.setOpaque(false);
        cell.setPreferredSize(new Dimension(44, 44));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel num = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        num.setFont(isToday || isSelected ? Theme.fontBold(13) : Theme.fontRegular(13));
        num.setForeground(isSelected ? Color.WHITE : isToday ? Theme.ACCENT : Theme.TEXT_DARK);
        cell.add(num, BorderLayout.CENTER);

        if (hasEvents) {
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected ? Color.WHITE : Theme.ACCENT);
                    g2.fillOval(getWidth()/2 - 3, 1, 6, 6);
                    g2.dispose();
                }
            };
            dot.setOpaque(false);
            dot.setPreferredSize(new Dimension(10, 10));
            cell.add(dot, BorderLayout.SOUTH);
        }

        cell.addMouseListener(new MouseAdapter() {
            Color savedBg;
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedDate = date;
                render();
                if (onDateSelected != null) onDateSelected.accept(date);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) cell.setBackground(Theme.HOVER_BG);
                cell.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cell.setBackground(null);
                cell.repaint();
            }
        });

        return cell;
    }

    private JPanel emptyCell() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private JButton navBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.fontBold(18));
        btn.setForeground(Theme.TEXT_MID);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public LocalDate getSelectedDate() { return selectedDate; }
}