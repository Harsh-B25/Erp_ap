package edu.univ.erp.UI;

// --- API Imports ---

import edu.univ.erp.UI.admin.AdminDashboardPanel;
import edu.univ.erp.UI.admin.CourseManagementPanel;
import edu.univ.erp.UI.admin.SectionManagementPanel;
import edu.univ.erp.UI.admin.UserManagementPanel;
import edu.univ.erp.UI.auth.ChangePasswordDialog;
import edu.univ.erp.UI.common.DeadlineBannerPanel;
import edu.univ.erp.UI.common.MaintenanceBannerPanel;
import edu.univ.erp.UI.common.ThemeToggle;
import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.UI.instructor.InstructorDashboardPanel;
import edu.univ.erp.UI.instructor.ViewSectionsPanel;
import edu.univ.erp.UI.student.CourseRegistrationPanel;
import edu.univ.erp.UI.student.StudentDashboardPanel;
import edu.univ.erp.UI.student.ViewCoursesPanel;
import edu.univ.erp.UI.student.ViewGradesPanel;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import edu.univ.erp.api.student.StudentApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The main application window (JFrame). Holds the session and builds the UI based on the user's
 * role. Manages the CardLayout and all navigation.
 */
class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

  public MultiLineCellRenderer() {
    setLineWrap(true);
    setWrapStyleWord(true);
    setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    setText(value == null ? "" : value.toString());

    if (isSelected) {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    } else {
      setBackground(table.getBackground());
      setForeground(table.getForeground());
    }

    setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);

    if (table.getRowHeight(row) < getPreferredSize().height) {
      table.setRowHeight(row, getPreferredSize().height);
    }

    return this;
  }
}

public class MainFrame extends JFrame {

  private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

  // --- Session and API ---
  private String session;
  private AdminApi adminApi;
  private AuthApi authApi;
  private CatalogApi catalogApi;
  private InstructorApi instructorApi;
  private MaintenanceApi maintenanceApi;
  private StudentApi studentApi;
  private String username;
  private JButton logoutButton;

  // --- Main UI Components ---
  private CardLayout cardLayout;
  private JPanel contentPanel;
  private JPanel navigationPanel;
  private MaintenanceBannerPanel maintenanceBanner;
  private boolean isMaintenanceOn = false;
  private HashMap<String, JPanel> panelCache;
  private DeadlineBannerPanel deadlineBanner;
  private boolean theme = true;

  private UserManagementPanel userManagementPanel;
  private CourseManagementPanel courseManagementPanel;
  private SectionManagementPanel sectionManagementPanel;

  public MainFrame(String session, String Username, JButton LogoutButton) {
    this.session = session;
    this.username = Username;
    this.logoutButton = LogoutButton;
    this.panelCache = new HashMap<>();
    initializeApis();
    setupMainLayout();
    buildRoleSpecificUI(session);
    checkMaintenanceStatus();
  }

  public void updateThemeflag(boolean flag) {
    this.theme = flag;
  }

  private void initializeApis() {
    this.adminApi = new AdminApi();
    this.authApi = new AuthApi();
    this.catalogApi = new CatalogApi();
    this.instructorApi = new InstructorApi();
    this.maintenanceApi = new MaintenanceApi();
    this.studentApi = new StudentApi();
  }

  private void setupMainLayout() {
    setTitle("University ERP System");
    setSize(1200, 900);
    setLayout(new BorderLayout());

    // --- Banner Panel ---
    JPanel bannerContainer = new JPanel();
    bannerContainer.setLayout(new BoxLayout(bannerContainer, BoxLayout.Y_AXIS));

    maintenanceBanner = new MaintenanceBannerPanel();
    deadlineBanner = new DeadlineBannerPanel();
    add(deadlineBanner, BorderLayout.NORTH);

    bannerContainer.add(maintenanceBanner);
    bannerContainer.add(deadlineBanner);

    add(bannerContainer, BorderLayout.NORTH);

    navigationPanel = new JPanel();
    navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
    navigationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(new JScrollPane(navigationPanel), BorderLayout.WEST);

    cardLayout = new CardLayout();
    contentPanel = new JPanel(cardLayout);
    add(contentPanel, BorderLayout.CENTER);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
  }

  private void addPanel(JPanel panel, String name) {
    contentPanel.add(panel, name);
    panelCache.put(name, panel);
  }

  private void buildRoleSpecificUI(String role) {
    switch (role.toUpperCase()) {
      case "STUDENT":
        buildStudentUI();
        break;
      case "INSTRUCTOR":
        buildInstructorUI();
        break;
      case "ADMIN":
        buildAdminUI();
        break;
    }
    navigationPanel.add(Box.createVerticalGlue());
  }

  // =================================================================
  // STUDENT UI BUILDER
  // =================================================================
  private void buildStudentUI() {
    // 1. Build Navigation Buttons

    addNavButton("Dashboard", "STUDENT_DASHBOARD");
    addNavButton("Register for Courses", "COURSE_REGISTRATION");
    addNavButton("My Courses", "VIEW_COURSES");
    addNavButton("My Grades", "VIEW_GRADES");
    navigationPanel.add(ThemeToggle.addToggleTheme(theme));
    navigationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    addChangePasswordButton();
    navigationPanel.add(this.logoutButton);

    // 2. Create and Add Pages
    StudentDashboardPanel studentDashboard = new StudentDashboardPanel(this.username, deadlineBanner);
    studentDashboard.setWelcomeMessage(this.username);
    addPanel(studentDashboard, "STUDENT_DASHBOARD");

    // -- Wire up dashboard buttons (as per your .form file) --
    studentDashboard
        .getCourseRegistrationButton()
        .addActionListener(e -> cardLayout.show(contentPanel, "COURSE_REGISTRATION"));
    studentDashboard
        .getViewCoursesButton()
        .addActionListener(e -> cardLayout.show(contentPanel, "VIEW_COURSES"));
    studentDashboard
        .getViewGradesButton()
        .addActionListener(e -> cardLayout.show(contentPanel, "VIEW_GRADES"));
    studentDashboard
        .getNotificationsButton()
        .addActionListener(
            e -> {
              notificationDialog();
            });

    addPanel(new CourseRegistrationPanel(this.username, deadlineBanner), "COURSE_REGISTRATION");
    addPanel(new ViewCoursesPanel(this.username, deadlineBanner), "VIEW_COURSES");
    addPanel(new ViewGradesPanel(this.username, deadlineBanner), "VIEW_GRADES");

    cardLayout.show(contentPanel, "STUDENT_DASHBOARD");
  }

  public void notificationDialog() {
    JDialog dialog = new JDialog((Frame) null, "My Notifications", true);
    dialog.setSize(600, 400);
    dialog.setLayout(new BorderLayout(10, 10));

    DefaultTableModel model =
        new DefaultTableModel(new String[] {"ID", "Message", "Created At"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false; // Make table read-only
          }
        };

    JTable table = new JTable(model);
    table.getTableHeader().setReorderingAllowed(false);
    table.getColumnModel().getColumn(1).setCellRenderer(new MultiLineCellRenderer());

    // Load notifications
    ApiResponse<ArrayList<ArrayList<String>>> res = adminApi.listNotifications();

    if (res.isSuccess()) {
      for (var row : res.getData()) {
        model.addRow(row.toArray());
      }
    }

    dialog.add(new JScrollPane(table), BorderLayout.CENTER);

    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }

  // =================================================================
  // INSTRUCTOR UI BUILDER
  // =================================================================
  private void buildInstructorUI() {
    addNavButton("Dashboard", "INSTRUCTOR_DASHBOARD");
    addNavButton("My Sections", "VIEW_SECTIONS");
    navigationPanel.add(ThemeToggle.addToggleTheme(theme));
    navigationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    addChangePasswordButton();
    navigationPanel.add(this.logoutButton);

    InstructorDashboardPanel instructorDashboard = new InstructorDashboardPanel(this.username);
    instructorDashboard.getWelcomeLabel().setText("Welcome, " + this.username + "!");
    addPanel(instructorDashboard, "INSTRUCTOR_DASHBOARD");

    instructorDashboard
        .getManageSectionButton()
        .addActionListener(e -> cardLayout.show(contentPanel, "VIEW_SECTIONS"));
    instructorDashboard
        .getNotifyButton()
        .addActionListener(
            e -> {
              notificationDialog();
            });
    addPanel(new ViewSectionsPanel(this.username), "VIEW_SECTIONS");

    cardLayout.show(contentPanel, "INSTRUCTOR_DASHBOARD");
  }

  // =================================================================
  // ADMIN UI BUILDER
  // =================================================================
  private void buildAdminUI() {
    addNavButton("Dashboard", "ADMIN_DASHBOARD");
    addNavButton("Manage Users", "MANAGE_USERS");
    addNavButton("Manage Courses", "MANAGE_COURSES");
    addNavButton("Manage Sections", "MANAGE_SECTIONS");
    navigationPanel.add(ThemeToggle.addToggleTheme(theme));
    navigationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    addChangePasswordButton();
    navigationPanel.add(this.logoutButton);

    AdminDashboardPanel adminDashboard = new AdminDashboardPanel();
    addPanel(adminDashboard, "ADMIN_DASHBOARD");
    userManagementPanel = new UserManagementPanel(this.username, this.session);
    courseManagementPanel = new CourseManagementPanel();
    sectionManagementPanel = new SectionManagementPanel();

    adminDashboard
        .getManageUsersButton()
        .addActionListener(e -> cardLayout.show(contentPanel, "MANAGE_USERS"));
    adminDashboard
        .getManageCoursesButton()
        .addActionListener(e -> cardLayout.show(contentPanel, "MANAGE_COURSES"));

    adminDashboard
        .getManageSectionsButton()
        .addActionListener(e -> cardLayout.show(contentPanel, "MANAGE_SECTIONS"));

    adminDashboard.getMaintenanceModeToggle().addActionListener(e -> toggleMaintenance());

    adminDashboard.getCurrentDeadlineValue().setText(adminApi.getDeadline().getData());

    adminDashboard
        .getUpdateDeadlineButton()
        .addActionListener(
            e -> {
              String selectedDate = adminDashboard.getDatePicker().getDate().toString(); // yyyy-MM-dd
              ApiResponse<Boolean> response = adminApi.updateDeadline(selectedDate);
              if (response.isSuccess()) {
                Toast.show(this, "Registration deadline updated to " + selectedDate);
                adminDashboard.getCurrentDeadlineValue().setText(selectedDate);
                log.info("Registration deadline updated to {}", selectedDate);
              } else {
                Toast.show(this, "Failed to update deadline: " + response.getMessage());
                log.error(
                    "Failed to update deadline. Reason: {}", response.getMessage());
              }
            });

    adminDashboard
        .getSendNotificationButton()
        .addActionListener(
            e -> {
              String text = adminDashboard.getNotificationTextField().getText().trim();
              if (text.isEmpty()) {
                Toast.show(this, "Notification cannot be empty.");
                log.warn("Attempted to send an empty notification.");
                return;
              }

              // Call backend: NotificationApi.addNotification(text)
              try {
                ApiResponse<String> res = adminApi.addNotification(text);
                if (res.isSuccess()) {
                  Toast.show(this, "Notification sent.");
                  adminDashboard.getNotificationTextField().setText("");
                  log.info("Notification sent: {}", text);
                } else {
                  Toast.show(this, res.getMessage());
                  log.error("Failed to send notification. Reason: {}", res.getMessage());
                }
                // System.out.println("text: " + text);
              } catch (Exception ex) {
                Toast.show(this, ex.getMessage());
                log.error("Exception while sending notification", ex);
              }
            });

    adminDashboard
        .getManageNotificationsButton()
        .addActionListener(
            e -> {
              showNotificationsDialog();
              // dialog.setVisible(true);
            });

    adminDashboard
        .getManageBackupButton()
        .addActionListener(
            e -> {
              int confirm =
                  JOptionPane.showConfirmDialog(
                      this,
                      "⚠ WARNING:  \"Are you sure you want to BACKUP the database?",
                      "Confirm Backup",
                      JOptionPane.YES_NO_OPTION,
                      JOptionPane.WARNING_MESSAGE);
              if (confirm == JOptionPane.YES_OPTION) {
                log.info("Backup initiated by user {}", username);
                ApiResponse<Boolean> response = adminApi.backup();
                if (response.isSuccess()) {
                  Toast.show(this, "Backup successful: " + "file saved at /backup folder");
                  log.info("Backup successful.");
                } else {
                  Toast.show(this, "Backup failed: " + response.getMessage());
                  log.error("Backup failed. Reason: {}", response.getMessage());
                }
              }
            });

    adminDashboard
        .getManageRestoreButton()
        .addActionListener(
            e -> {
              int confirm =
                  JOptionPane.showConfirmDialog(
                      this,
                      "⚠ WARNING: Restoring will overwrite ALL data!\nAre you absolutely sure?",
                      "Confirm Restore",
                      JOptionPane.YES_NO_OPTION,
                      JOptionPane.WARNING_MESSAGE);
              if (confirm == JOptionPane.YES_OPTION) {
                log.info("Restore initiated by user {}", username);
                ApiResponse<Boolean> response = adminApi.restore();
                if (response.isSuccess()) {
                  Toast.show(this, "Restore successful.");
                  log.info("Restore successful.");
                } else {
                  Toast.show(this, "Restore failed: " + response.getMessage());
                  log.error("Restore failed. Reason: {}", response.getMessage());
                }
              }
            });

    addPanel(userManagementPanel, "MANAGE_USERS");
    addPanel(courseManagementPanel, "MANAGE_COURSES");
    addPanel(sectionManagementPanel, "MANAGE_SECTIONS");

    cardLayout.show(contentPanel, "ADMIN_DASHBOARD");
  }

  // =================================================================
  // HELPER METHODS
  // =================================================================

  private void addNavButton(String text, String cardName) {
    JButton button = new JButton(text);
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.addActionListener(e -> cardLayout.show(contentPanel, cardName));
    navigationPanel.add(button);
    navigationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
  }

  private void showNotificationsDialog() {

    JDialog dialog = new JDialog((Frame) null, "Manage Notifications", true);
    dialog.setSize(600, 400);
    dialog.setLayout(new BorderLayout(10, 10));

    DefaultTableModel model =
        new DefaultTableModel(new String[] {"ID", "Message", "Created At"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false; // Make table read-only
          }
        };

    JTable table = new JTable(model);

    // Load notifications
    // AdminApi api = new AdminApi();
    ApiResponse<ArrayList<ArrayList<String>>> res = adminApi.listNotifications();

    if (res.isSuccess()) {
      for (var row : res.getData()) {
        model.addRow(row.toArray());
      }
    }

    table.getTableHeader().setReorderingAllowed(false);
    table.getColumnModel().getColumn(1).setCellRenderer(new MultiLineCellRenderer());

    dialog.add(new JScrollPane(table), BorderLayout.CENTER);

    JButton deleteBtn = new JButton("Delete Selected");
    dialog.add(deleteBtn, BorderLayout.SOUTH);

    deleteBtn.addActionListener(
        e -> {
          int row = table.getSelectedRow();
          if (row == -1) {
            Toast.show(dialog, "Select a notification.");
            return;
          }

          int id = Integer.parseInt(model.getValueAt(row, 0).toString());
          ApiResponse<String> delRes = adminApi.deleteNotification(id);

          Toast.show(dialog, delRes.getMessage());

          if (delRes.isSuccess()) {
            model.removeRow(row);
            log.info("Notification with ID {} deleted.", id);
          } else {
            log.error(
                "Failed to delete notification with ID {}. Reason: {}",
                id,
                delRes.getMessage());
          }
        });

    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }

  private void addChangePasswordButton() {
    JButton button = new JButton("Change Password");
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    // This button opens a dialog, it doesn't swap a card
    button.addActionListener(
        e -> {
          ChangePasswordDialog dialog = new ChangePasswordDialog(this, session, username);
          dialog.setVisible(true);
        });
    navigationPanel.add(button);
    navigationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
  }

  private void checkMaintenanceStatus() {
    ApiResponse<String> response = maintenanceApi.checkMaintenance();
    if (response.isSuccess()) {
      this.isMaintenanceOn = Boolean.parseBoolean(response.getData());
    }
    updateMaintenanceBanner();

    AdminDashboardPanel adminDashboard = (AdminDashboardPanel) panelCache.get("ADMIN_DASHBOARD");
    if (adminDashboard != null) {
      adminDashboard.getStatusLabel().setText(isMaintenanceOn ? "ON" : "OFF");
    }
  }

  private void toggleMaintenance() {
    boolean newStatus = !this.isMaintenanceOn;
    ApiResponse<String> response = maintenanceApi.setMaintenance(newStatus);

    if (response.isSuccess()) {
      this.isMaintenanceOn = newStatus;
      updateMaintenanceBanner(); // Update banner for other users immediately
      Toast.show(this, "Maintenance Mode set to " + (isMaintenanceOn ? "ON" : "OFF"));
      log.info("Maintenance mode set to {}", (isMaintenanceOn ? "ON" : "OFF"));

      AdminDashboardPanel adminDashboard = (AdminDashboardPanel) panelCache.get("ADMIN_DASHBOARD");
      if (adminDashboard != null) {
        adminDashboard.getStatusLabel().setText(isMaintenanceOn ? "ON" : "OFF");
      }
    } else {
      Toast.show(this, "Failed to update maintenance mode: " + response.getMessage());
      log.error(
          "Failed to update maintenance mode. Reason: {}", response.getMessage());
    }
  }

  private void updateMaintenanceBanner() {
    String role = session.toUpperCase();
    if (role.equals("STUDENT") || role.equals("INSTRUCTOR") || role.equals("ADMIN")) {
      maintenanceBanner.setBannerVisible(this.isMaintenanceOn);
    } else {
      maintenanceBanner.setBannerVisible(false);
    }
  }

  public static void updateDeadlineBanner(DeadlineBannerPanel deadlineBanner) {

    StudentApi studentApi = new StudentApi();

    // Check if the registration period for the current semester is active.
    ApiResponse<Boolean> response = studentApi.isRegistrationOpen();

    if (response.isSuccess() && response.getData()) {
      deadlineBanner.setVisible(true);
    } else {
      deadlineBanner.setVisible(false);
    }
  }
}