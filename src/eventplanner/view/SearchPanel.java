package eventplanner.view;

import eventplanner.controller.EventController;
import eventplanner.model.Event;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Popup window for searching events by keyword.
 *
 * The search happens "live": every key typed triggers a new search and
 * updates the results list (no need to press Enter or click any button).
 */
public class SearchPanel extends JDialog {

    private EventController controller;
    private JTextField txtSearch;
    private JPanel resultsPanel; // where search results are displayed

    public SearchPanel(Frame owner, EventController controller) {
        super(owner, "Search Events", true);
        this.controller = controller;
        buildUI();
        setSize(520, 500);
        setLocationRelativeTo(owner);
    }

    /** Builds the UI: title, search bar, and scrollable results area. */
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.BG_PRIMARY);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Search Events");
        title.setFont(Theme.fontBold(18));
        title.setForeground(Theme.TEXT_DARK);

        // search bar with magnifying glass icon
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(Theme.BG_CARD);
        searchBar.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel icon = new JLabel("🔍");
        icon.setFont(Theme.fontRegular(14));
        txtSearch = new JTextField();
        txtSearch.setFont(Theme.fontRegular(13));
        txtSearch.setBorder(null);
        txtSearch.setBackground(Theme.BG_CARD);

        searchBar.add(icon, BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);

        // triggers search on every key release — makes it "live search"
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { doSearch(); }
        });

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Theme.BG_PRIMARY);

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);

        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(Theme.BG_PRIMARY);
        center.add(searchBar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }

    /**
     * Searches events using the text entered (EventController.search)
     * and redraws the results list. If the field is empty, the list
     * is cleared (it does not show all events by default).
     */
    private void doSearch() {
        resultsPanel.removeAll();
        String q = txtSearch.getText().trim();
        if (q.isEmpty()) {
            resultsPanel.revalidate();
            resultsPanel.repaint();
            return;
        }

        List<Event> results = controller.search(q);

        if (results.isEmpty()) {
            JLabel none = new JLabel("No results found.");
            none.setFont(Theme.fontItalic(13));
            none.setForeground(Theme.TEXT_LIGHT);
            none.setBorder(new EmptyBorder(20, 4, 0, 0));
            resultsPanel.add(none);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // creates one result row per event found
            for (Event e : results) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(Theme.BG_CARD);
                row.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(10, 14, 10, 14)
                ));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                // colored dot indicating event category
                Color catColor = Theme.getCategoryColor(e.getCategory().getColor());
                JLabel dot = new JLabel("●");
                dot.setFont(Theme.fontBold(16));
                dot.setForeground(catColor);

                JPanel info = new JPanel(new BorderLayout());
                info.setBackground(Theme.BG_CARD);

                JLabel name = new JLabel(e.getTitle());
                name.setFont(Theme.fontBold(13));
                name.setForeground(Theme.TEXT_DARK);

                JLabel when = new JLabel(
                    e.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " at " +
                    e.getTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                    "  |  " + e.getCategory().getLabel()
                );
                when.setFont(Theme.fontRegular(11));
                when.setForeground(Theme.TEXT_MID);

                info.add(name, BorderLayout.CENTER);
                info.add(when, BorderLayout.SOUTH);

                row.add(dot, BorderLayout.WEST);
                row.add(info, BorderLayout.CENTER);

                resultsPanel.add(row);
                resultsPanel.add(Box.createVerticalStrut(6));
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
}