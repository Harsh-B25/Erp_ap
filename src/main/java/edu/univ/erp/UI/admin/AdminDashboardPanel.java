package edu.univ.erp.UI.admin;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.common.ApiResponse;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Year;

public class AdminDashboardPanel extends JPanel {

  private JLabel statusLabel;
  private JButton maintenanceModeToggle;
  private JButton manageUsersButton;
  private JButton manageCoursesButton;
  private JButton manageSectionsButton;
  private JButton backupButton;
  private JButton restoreButton;

  // --- DEADLINE & SEMESTER CONTROLS ---
  private JButton updateDeadlineButton;
  private JLabel currentDeadlineValue;
  private DatePicker datePicker;
  private JComboBox<String> semesterSelector;
  private JTextField yearField;

  private JTextField notificationTextField;
  private JButton sendNotificationButton;
  private JButton manageNotificationsButton;

  private AdminApi adminApi = new AdminApi();

  public AdminDashboardPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel titleLabel = new JLabel("Admin Control Panel");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(titleLabel);

    add(Box.createRigidArea(new Dimension(0, 20)));
    add(createSystemStatusPanel());
    add(Box.createRigidArea(new Dimension(0, 20)));
    add(createQuickActionsPanel());
    add(Box.createRigidArea(new Dimension(0, 20)));
    add(createBackupPanel());
    add(Box.createRigidArea(new Dimension(0, 20)));

    // Updated Deadline Panel
    add(createDeadlinePanel());

    add(Box.createRigidArea(new Dimension(0, 20)));
    add(createNotificationPanel());
    add(Box.createVerticalGlue());

    refreshCurrentDeadlineDisplay();
  }

  private JPanel createDeadlinePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder("Active Semester & Deadline"));

    // ---------------------------------------------------
    // Row 1: Select Semester & Year
    // ---------------------------------------------------
    JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

    selectionPanel.add(new JLabel("Semester:"));
    String[] sems = {"Monsoon", "Winter", "Summer"};
    semesterSelector = new JComboBox<>(sems);
    selectionPanel.add(semesterSelector);

    selectionPanel.add(new JLabel("Year:"));
    yearField = new JTextField(String.valueOf(Year.now().getValue()), 4);
    selectionPanel.add(yearField);

    // ---------------------------------------------------
    // Row 2: Date Picker
    // ---------------------------------------------------
    JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    datePanel.add(new JLabel("Reg. Deadline:"));

    DatePickerSettings datePickerSettings = new DatePickerSettings();
    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");


    datePicker = new DatePicker(datePickerSettings);
    datePickerSettings.setVetoPolicy(date -> !date.isBefore(LocalDate.now())); // Block past dates
    datePanel.add(datePicker);

    // ---------------------------------------------------
    // Row 3: Action Button & Status
    // ---------------------------------------------------
    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    updateDeadlineButton = new JButton("Set Active & Update");
    currentDeadlineValue = new JLabel("No active semester set.");
    currentDeadlineValue.setFont(new Font("SansSerif", Font.ITALIC, 11));

    actionPanel.add(updateDeadlineButton);
    actionPanel.add(Box.createHorizontalStrut(10));
    actionPanel.add(currentDeadlineValue);

    panel.add(selectionPanel);
    panel.add(datePanel);
    panel.add(actionPanel);

    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

    // --- LISTENER ---
    updateDeadlineButton.addActionListener(e -> handleUpdateDeadline());

    return panel;
  }

  private void handleUpdateDeadline() {
    String sem = (String) semesterSelector.getSelectedItem();
    String yearStr = yearField.getText().trim();
    LocalDate date = datePicker.getDate();

    if (yearStr.isEmpty() || date == null) {
      Toast.show(this, "Please enter a valid year and select a date.");
      return;
    }

    try {
      int year = Integer.parseInt(yearStr);
      String deadline = date.toString();

      // 1. Update the specific deadline for this Sem/Year
      ApiResponse<Boolean> deadlineRes = adminApi.updateSemesterDeadline(sem, year, deadline);

      // 2. Set this Sem/Year as the GLOBALLY ACTIVE semester (for student view)
      ApiResponse<Boolean> activeRes = adminApi.setCurrentSemester(sem, year);

      if (deadlineRes.isSuccess() && activeRes.isSuccess()) {
        Toast.show(this, "Updated! Active: " + sem + "-" + year);
        refreshCurrentDeadlineDisplay();
      } else {
        Toast.show(this, "Error: " + deadlineRes.getMessage());
      }

    } catch (NumberFormatException ex) {
      Toast.show(this, "Year must be a number.");
    }
  }

  private void refreshCurrentDeadlineDisplay() {
    // Fetch the currently active semester to display its status
    ApiResponse<String> activeRes = adminApi.getCurrentSemester();
    if (activeRes.isSuccess() && activeRes.getData() != null) {
      String activeSemStr = activeRes.getData(); // e.g., "Monsoon-2025"
      try {
        String[] parts = activeSemStr.split("-");
        String sem = parts[0];
        int year = Integer.parseInt(parts[1]);

        // Get the deadline for this active semester
        ApiResponse<String> deadlineRes = adminApi.getSemesterDeadline(sem, year);
        String date = (deadlineRes.getData() != null) ? deadlineRes.getData() : "None";

        currentDeadlineValue.setText("Active: " + activeSemStr + " | Deadline: " + date);

      } catch (Exception e) {
        currentDeadlineValue.setText("Active: " + activeSemStr);
      }
    } else {
      currentDeadlineValue.setText("No active semester set.");
    }
  }

  private JPanel createSystemStatusPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
    panel.setBorder(BorderFactory.createTitledBorder("System Status"));
    panel.add(new JLabel("Maintenance Mode:"));
    statusLabel = new JLabel("[Current Status]");
    statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    panel.add(statusLabel);
    maintenanceModeToggle = new JButton("Toggle Maintenance Mode");
    panel.add(maintenanceModeToggle);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel createBackupPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
    panel.setBorder(BorderFactory.createTitledBorder("Backup and Restore"));
    backupButton = new JButton("Backup");
    restoreButton = new JButton("Restore");
    panel.add(backupButton);
    panel.add(restoreButton);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel createQuickActionsPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
    panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
    manageUsersButton = new JButton("Manage Users");
    manageCoursesButton = new JButton("Manage Courses");
    manageSectionsButton = new JButton("Manage Sections");
    panel.add(manageUsersButton);
    panel.add(manageCoursesButton);
    panel.add(manageSectionsButton);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel createNotificationPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder("Notifications"));
    JPanel sendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    notificationTextField = new JTextField(25);
    sendNotificationButton = new JButton("Send");
    sendPanel.add(new JLabel("New Notification:"));
    sendPanel.add(notificationTextField);
    sendPanel.add(sendNotificationButton);
    JPanel managePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    manageNotificationsButton = new JButton("Manage Notifications");
    managePanel.add(manageNotificationsButton);
    panel.add(sendPanel);
    panel.add(managePanel);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  // --- Public Getters ---
  public JLabel getStatusLabel() { return statusLabel; }
  public JButton getMaintenanceModeToggle() { return maintenanceModeToggle; }
  public JButton getManageUsersButton() { return manageUsersButton; }
  public JButton getManageCoursesButton() { return manageCoursesButton; }
  public JButton getManageSectionsButton() { return manageSectionsButton; }
  public JButton getManageBackupButton() { return backupButton; }
  public JButton getManageRestoreButton() { return restoreButton; }
  public JButton getUpdateDeadlineButton() { return updateDeadlineButton; }
  public JLabel getCurrentDeadlineValue() { return currentDeadlineValue; }
  public DatePicker getDatePicker() { return datePicker; }
  public JTextField getNotificationTextField() { return notificationTextField; }
  public JButton getSendNotificationButton() { return sendNotificationButton; }
  public JButton getManageNotificationsButton() { return manageNotificationsButton; }
}