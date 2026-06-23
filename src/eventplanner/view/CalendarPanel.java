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

/**
 * Monthly calendar panel (left side of the main window).
 *
 * Shows a 6-row x 7-column grid (42 cells) representing the weeks
 * of the month. Days with events get a small dot under the number,
 * and the selected day / today get a different background color.
 *
 * When the user clicks a date, the "onDateSelected" callback passed
 * in the constructor is called — that's how Main.java knows which
 * date to show in the day events panel.
 */
public class CalendarPanel extends JPanel {

    private YearMonth currentMonth;     // month/year currently being displayed
    private LocalDate selectedDate;     // currently selected day (highlighted)
    private EventController controller;
    private Consumer<LocalDate> onDateSelected; // callback fired when a day is clicked

    private JLabel lblMonth;   // "June 2026" title at the top
    private JPanel gridPanel;  // 42-cell grid of days

    private static final String[] DAYS = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};

    public CalendarPanel(EventController controller, Consumer<LocalDate> onDateSelected) {
        this.controller = controller;
        this.onDateSelected = onDateSelected;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_CARD);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        buildHeader(); // builds the top bar: month name + navigation buttons
        buildGrid();   // builds the grid structure (empty for now)
        render();      // fills the grid with the days of the current month
    }

    /**
     * Builds the top bar: "‹", "›" buttons (previous/next month),
     * a "Today" button, and the month name shown in the center.
     */
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_CARD);
        header.setBorder(new EmptyBorder(20, 20, 12, 20));

        lblMonth = new JLabel("", SwingConstants.CENTER);
        lblMonth.setFont(Theme.fontBold(16));
        lblMonth.setForeground(Theme.TEXT_DARK);

        JButton prev = navBtn("‹");
        JButton next = navBtn("›");
        // on click, just change the stored month and re-render the grid
        prev.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); render(); });
        next.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); render(); });

        JButton todayBtn = new JButton("Hoje");
        todayBtn.setFont(Theme.fontRegular(12));
        todayBtn.setForeground(Theme.ACCENT);
        todayBtn.setBorderPainted(false);
        todayBtn.setContentAreaFilled(false);
        todayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        todayBtn.addActionListener(e -> {
            // jumps the calendar back to today's month and selects today
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

    /**
     * Builds the grid structure: a row with the weekday names
     * (Sun, Mon...) and, below it, the 6x7 grid where the day
     * numbers will go (filled in later by the render() method).
     */
    private void buildGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_CARD);
        wrapper.setBorder(new EmptyBorder(0, 12, 12, 12));

        // row with the weekday names
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

        // 6 rows x 7 columns = 42 cells, enough for any month
        // (even when day 1 falls on a Saturday, there's still room)
        gridPanel = new JPanel(new GridLayout(6, 7, 4, 4));
        gridPanel.setBackground(Theme.BG_CARD);
        wrapper.add(gridPanel, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    /**
     * Redraws the whole calendar for the current month (currentMonth).
     * Called whenever the month changes, or when an event is created/edited
     * (to refresh which days have the "has event" dot).
     */
    public void render() {
        // builds the "June 2026" text with the first letter capitalized
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);
        lblMonth.setText(monthName + " " + currentMonth.getYear());

        gridPanel.removeAll(); // clears the grid before redrawing

        // asks the controller which days of this month have at least one event
        List<LocalDate> datesWithEvents = controller.getDatesWithEvents(
            currentMonth.getYear(), currentMonth.getMonthValue());

        LocalDate first = currentMonth.atDay(1);
        // figures out which column (0=Sunday) day 1 of the month falls on,
        // so we know how many empty cells to place before it
        int startDow = first.getDayOfWeek().getValue() % 7;

        LocalDate today = LocalDate.now();

        // fills empty cells before day 1 (e.g. if the month starts on a
        // Wednesday, the Sunday/Monday/Tuesday cells stay blank)
        for (int i = 0; i < startDow; i++) {
            gridPanel.add(emptyCell());
        }

        // creates one cell per day of the month
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selectedDate);
            boolean hasEvents = datesWithEvents.contains(date);
            gridPanel.add(dayCell(date, isToday, isSelected, hasEvents));
        }

        // fills the rest of the grid (up to 42 cells) with empty space
        int used = startDow + daysInMonth;
        int remaining = 42 - used;
        for (int i = 0; i < remaining; i++) {
            gridPanel.add(emptyCell());
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    /**
     * Creates the visual cell representing one day of the month.
     *
     * paintComponent is overridden to draw a rounded-corner background
     * (Swing doesn't support this natively) with different colors
     * depending on whether the day is selected, is today, or neither.
     *
     * @param date       the date this cell represents
     * @param isToday    true if this cell is today's date
     * @param isSelected true if this cell is the currently selected date
     * @param hasEvents  true if this day has at least one event (shows a dot)
     */
    private JPanel dayCell(LocalDate date, boolean isToday, boolean isSelected, boolean hasEvents) {
        JPanel cell = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // picks the background color: selected > today > normal
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

        // the day number, bold/colored differently if today or selected
        JLabel num = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        num.setFont(isToday || isSelected ? Theme.fontBold(13) : Theme.fontRegular(13));
        num.setForeground(isSelected ? Color.WHITE : isToday ? Theme.ACCENT : Theme.TEXT_DARK);
        cell.add(num, BorderLayout.CENTER);

        // if the day has an event, draw a small dot below the number
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

        // on click, selects this date and notifies whoever is "listening"
        // (in this case, Main.java, which tells the DayPanel to refresh)
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
                // simple hover effect (highlight on mouse over)
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

    /** Empty cell, used to fill the spaces before/after the month in the grid. */
    private JPanel emptyCell() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    /** Creates a plain borderless button, used for the navigation arrows. */
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

    /** Returns the date currently selected on the calendar. */
    public LocalDate getSelectedDate() { return selectedDate; }
}