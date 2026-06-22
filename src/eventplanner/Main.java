package eventplanner;

import eventplanner.controller.EventController;
import eventplanner.model.Event;
import eventplanner.view.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        // Garante execução na Event Dispatch Thread do Swing
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
        // Look and feel nativo do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        EventController controller = new EventController();

        JFrame frame = new JFrame("Event Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setPreferredSize(new Dimension(1100, 700));

        // Layout principal: sidebar esquerda (calendário) + painel direito (eventos do dia)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(300);
        split.setDividerSize(1);
        split.setBorder(null);

        // Usa array para permitir referência no lambda antes da atribuição
        DayPanel[] dayPanelRef = new DayPanel[1];

        // Painel direito de eventos
        dayPanelRef[0] = new DayPanel(controller, (event, action) -> {
            if ("delete".equals(action)) {
                int opt = JOptionPane.showConfirmDialog(frame,
                    "Excluir o evento \"" + event.getTitle() + "\"?",
                    "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    controller.deleteEvent(event.getId());
                    dayPanelRef[0].refreshEvents();
                }
            } else if ("edit".equals(action)) {
                EventDialog dlg = new EventDialog(frame, event, event.getDate());
                dlg.setVisible(true);
                if (dlg.getResult() != null) {
                    controller.updateEvent(dlg.getResult());
                    dayPanelRef[0].refreshEvents();
                }
            }
        });
        DayPanel dayPanel = dayPanelRef[0];
        dayPanel.showDate(LocalDate.now());

        // Painel esquerdo: calendário + botão "Novo Evento"
        CalendarPanel calendarPanel = new CalendarPanel(controller, date -> {
            dayPanel.showDate(date);
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Theme.BG_CARD);

        // Botão "+ Novo Evento"
        JButton btnNew = new JButton("+ Novo Evento") {
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
            LocalDate selected = calendarPanel.getSelectedDate();
            EventDialog dlg = new EventDialog(frame, null, selected);
            dlg.setVisible(true);
            if (dlg.getResult() != null) {
                controller.addEvent(dlg.getResult());
                calendarPanel.render();
                dayPanel.showDate(selected);
            }
        });

        // Botão "Buscar"
        JButton btnSearch = new JButton("🔍 Buscar") {
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

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        btnPanel.setBackground(Theme.BG_CARD);
        btnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 16, 16));
        btnPanel.add(btnNew);
        btnPanel.add(btnSearch);

        leftPanel.add(calendarPanel, BorderLayout.CENTER);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(dayPanel);

        frame.setContentPane(split);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Verificar lembretes ao iniciar
        var reminders = controller.getUpcomingReminders();
        if (!reminders.isEmpty()) {
            StringBuilder sb = new StringBuilder("Lembretes nas próximas 24h:\n");
            reminders.forEach(r -> sb.append("• ").append(r.getTitle()).append(" - ").append(r.getDate()).append("\n"));
            JOptionPane.showMessageDialog(frame, sb.toString(), "Lembretes", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
