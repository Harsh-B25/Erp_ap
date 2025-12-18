package edu.univ.erp.UI.student;

import edu.univ.erp.UI.common.DeadlineBannerPanel;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.common.ApiResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

/**
 * The student's main dashboard panel.
 * Displays a welcome message and a WEEKLY timetable using a ROW-based layout.
 */
public class StudentDashboardPanel extends JPanel {

    private JLabel welcomeLabel;
    private JButton courseRegistrationButton;
    private JButton viewCoursesButton;
    private JButton viewGradesButton;
    private JButton notificationsButton;
    private DeadlineBannerPanel deadlineBanner;
    private String username;
    private CatalogApi catalogApi;

    // Store the daily table models so we can refresh them
    private ArrayList<DefaultTableModel> dayModels;

    public StudentDashboardPanel(String username , DeadlineBannerPanel deadlineBanner) {
        this.username = username;
        this.catalogApi = new CatalogApi();
        this.dayModels = new ArrayList<>();
        this.deadlineBanner = deadlineBanner;

        // Use BorderLayout for the main container
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- TOP: Welcome & Nav ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(welcomeLabel);
        
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel navPanel = createQuickNavPanel();
        navPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(navPanel);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Weekly Timetable (Rows) ---
        // We use a vertical BoxLayout inside a ScrollPane
        JPanel weekContainer = new JPanel();
        weekContainer.setLayout(new BoxLayout(weekContainer, BoxLayout.Y_AXIS));
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
        String currentDay = getCurrentDayName(); // e.g. "Mon"

        for (String day : days) {
            JPanel dayPanel = createDayRowPanel(day, day.equalsIgnoreCase(currentDay));
            weekContainer.add(dayPanel);
            weekContainer.add(Box.createRigidArea(new Dimension(0, 10))); // Gap between days
        }
        
        // Wrap in ScrollPane in case there are many classes
        JScrollPane mainScroll = new JScrollPane(weekContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        // Load Data
        refreshTimetable();
        deadlineBanner.setVisible(false); // Hide by default
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshTimetable();
                deadlineBanner.setVisible(false); // Hide by default
                // MainFrame.updateDeadlineBanner(deadlineBanner) ;
            }
        });
    }
    
    /**
     * Creates a horizontal row panel for a single day.
     * Layout: [ Day Label ] [ Table of Classes ]
     */
    private JPanel createDayRowPanel(String dayName, boolean isToday) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150)); // Limit max height

        // --- LEFT: Day Label ---
        JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
        dayLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        dayLabel.setPreferredSize(new Dimension(80, 0)); // Fixed width for alignment
        dayLabel.setOpaque(true);
        
        if (isToday) {
            dayLabel.setBackground(new Color(52, 152, 219)); // Blue highlight
            dayLabel.setForeground(Color.WHITE);
            dayLabel.setText(dayName);
            dayLabel.setPreferredSize(new Dimension(100, 0)); // Slightly wider for text
            panel.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
        } else {
            // dayLabel.setBackground(new Color(240, 240, 240)); // Light Gray
            // dayLabel.setForeground(Color.DARK_GRAY);
        }
        panel.add(dayLabel, BorderLayout.WEST);

        // --- CENTER: Classes Table ---
        String[] cols = {"Time", "Code", "Room"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.getTableHeader().setReorderingAllowed(false);
        
        // Center text in cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        // Set column widths to ensure alignment across different day panels
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Time
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Code
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Room

        // Store model for refreshing
        dayModels.add(model); 

        // Wrap table in panel to handle headers correctly without scrollbars inside rows
        JPanel tablePanel = new JPanel(new BorderLayout());
        JTableHeader header = table.getTableHeader();
        
        // --- HEADER COLOR CHANGE ---
        header.setBackground(new Color(38, 121, 219)); // Dark Orange
        header.setForeground(Color.WHITE);            // White Text
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setOpaque(true);
        // ---------------------------
        
        tablePanel.add(header, BorderLayout.NORTH);
        tablePanel.add(table, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }

    public void refreshTimetable() {
        // 1. Clear all day models
        for (DefaultTableModel m : dayModels) {
            m.setRowCount(0);
        }

        // 2. Fetch all student sections
        ApiResponse<ArrayList<ArrayList<String>>> response = catalogApi.listStudentsSections(username);
        
        if (response.isSuccess()) {
            for (ArrayList<String> row : response.getData()) {
                // Data: 0=ID, 1=Code, 2=Title, 3=DayTime, 4=Room...
                String code = row.get(1);
                String dayTime = row.get(3); // e.g. "Mon, Thur 9:30-11:00"
                String room = row.get(4);
                
                if (dayTime == null) continue;

                // 3. Parse DayTime string
                // Extract time by removing letters and commas
                String timeOnly = dayTime.replaceAll("[a-zA-Z,]+", "").trim();
                
                // Check for day occurrences and add to corresponding model (0=Mon, 1=Tue...)
                if (dayTime.contains("Mon")) addToDay(0, timeOnly, code, room);
                if (dayTime.contains("Tue")) addToDay(1, timeOnly, code, room);
                if (dayTime.contains("Wed")) addToDay(2, timeOnly, code, room);
                if (dayTime.contains("Thu")) addToDay(3, timeOnly, code, room);
                if (dayTime.contains("Fri")) addToDay(4, timeOnly, code, room);
            }
        }
        
        // 4. Add placeholder for empty days
        for (DefaultTableModel m : dayModels) {
            if (m.getRowCount() == 0) {
                m.addRow(new Object[]{"", "No classes", ""});
            }
        }
    }
    
    private void addToDay(int dayIndex, String time, String code, String room) {
        if (dayIndex < 0 || dayIndex >= dayModels.size()) return;
        dayModels.get(dayIndex).addRow(new Object[]{ time, code, room });
    }

    private String getCurrentDayName() {
        return LocalDate.now().getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
    
    private JPanel createQuickNavPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        // panel.setBorder(BorderFactory.createTitledBorder("Quick Navigation"));

        courseRegistrationButton = new JButton("Register");
        viewCoursesButton = new JButton("View Courses");
        viewGradesButton = new JButton("View Grades");
        notificationsButton = new JButton("Notifications");
        
        // Make buttons taller
        Dimension btnSize = new Dimension(150, 40);
        courseRegistrationButton.setPreferredSize(btnSize);
        viewCoursesButton.setPreferredSize(btnSize);
        viewGradesButton.setPreferredSize(btnSize);
        notificationsButton.setPreferredSize(btnSize);

        panel.add(courseRegistrationButton);
        panel.add(viewCoursesButton);
        panel.add(viewGradesButton);
        panel.add(notificationsButton);
        
        panel.setMaximumSize(new Dimension(800, 50)); 
        return panel;
    }

    // --- Getters ---
    public JButton getCourseRegistrationButton() { return courseRegistrationButton; }
    public JButton getViewCoursesButton() { return viewCoursesButton; }
    public JButton getViewGradesButton() { return viewGradesButton; }
    public JButton getNotificationsButton() { return notificationsButton; }

  public void setWelcomeMessage(String name) {
        welcomeLabel.setText("Welcome, " + name + "!");
    }
}