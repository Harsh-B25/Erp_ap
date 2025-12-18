package edu.univ.erp.UI.admin;

import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

public class UserManagementPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(UserManagementPanel.class);

  private JTabbedPane tabbedPane;
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JComboBox<String> roleSelector;
  private JButton createUserButton;
  private JPanel studentFieldsPanel;
  private JTextField rollNumberField;
  private JTextField programField;
  private JTextField yearField;
  private JPanel instructorFieldsPanel;
  private JTextField departmentField;
  private JTable usersTable;
  private DefaultTableModel usersmodel;

  private CatalogApi catalogApi = new CatalogApi();
  private AdminApi adminApi = new AdminApi();
  private AuthApi authapi = new AuthApi();

  private String currentAdminUsername;
  private String session;

  public UserManagementPanel(String username, String session) {
    this.currentAdminUsername = username;
    this.session = session;
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Add New User", createAddUserPanel());
    tabbedPane.addTab("View All Users", createViewUsersPanel());
    add(tabbedPane, BorderLayout.CENTER);
  }

  private JPanel createAddUserPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Username:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    usernameField = new JTextField(20);
    panel.add(usernameField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Password:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    passwordField = new JPasswordField(20);
    panel.add(passwordField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Role:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    String[] roles = {"STUDENT", "INSTRUCTOR", "ADMIN"};
    roleSelector = new JComboBox<>(roles);
    panel.add(roleSelector, gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    studentFieldsPanel = createStudentFieldsPanel();
    panel.add(studentFieldsPanel, gbc);

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    instructorFieldsPanel = createInstructorFieldsPanel();
    panel.add(instructorFieldsPanel, gbc);

    gbc.gridx = 1;
    gbc.gridy = 6;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    createUserButton = new JButton("Create User");
    panel.add(createUserButton, gbc);

    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.gridwidth = 2;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.VERTICAL;
    panel.add(Box.createVerticalGlue(), gbc);

    roleSelector.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            updateRoleSpecificFields((String) e.getItem());
          }
        });

    createUserButton.addActionListener(e -> createUser());

    updateRoleSpecificFields((String) roleSelector.getSelectedItem());
    return panel;
  }

  private JPanel createStudentFieldsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Student Details"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Roll Number:"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    rollNumberField = new JTextField(15);
    panel.add(rollNumberField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Program:"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    programField = new JTextField(15);
    panel.add(programField, gbc);

    gbc.gridx = 2;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Year:"), gbc);
    gbc.gridx = 3;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    yearField = new JTextField(5);
    panel.add(yearField, gbc);
    return panel;
  }

  private JPanel createInstructorFieldsPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(BorderFactory.createTitledBorder("Instructor Details"));
    panel.add(new JLabel("Department:"));
    departmentField = new JTextField(15);
    panel.add(departmentField);
    return panel;
  }

  private void updateRoleSpecificFields(String role) {
    if (role == null) return;
    switch (role) {
      case "STUDENT":
        studentFieldsPanel.setVisible(true);
        instructorFieldsPanel.setVisible(false);
        break;
      case "INSTRUCTOR":
        studentFieldsPanel.setVisible(false);
        instructorFieldsPanel.setVisible(true);
        break;
      case "ADMIN":
      default:
        studentFieldsPanel.setVisible(false);
        instructorFieldsPanel.setVisible(false);
        break;
    }
    if (this.getTopLevelAncestor() != null) {
      ((Window) this.getTopLevelAncestor()).repaint();
    }
  }

  private JPanel createViewUsersPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    String[] columnNames = {"User ID", "Username", "Role", "Department/Program"};
    usersmodel = new DefaultTableModel(columnNames, 0);
    usersTable = new JTable(usersmodel);
    usersTable.getTableHeader().setReorderingAllowed(false);
    panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(new JButton("Edit Selected"));
    JButton deleteButton = new JButton("Deactivate/Delete Selected");
    buttonPanel.add(deleteButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    deleteButton.addActionListener(e -> deleteSelectedUser());

    refreshCatalog();

    return panel;
  }

  private boolean performSudoCheck(String actionName) {
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Security Check: To " + actionName + ","));
    panel.add(new JLabel("please confirm YOUR password (" + currentAdminUsername + "):"));
    JPasswordField pf = new JPasswordField();
    panel.add(pf);

    int result =
        JOptionPane.showConfirmDialog(
            this, panel, "Admin Verification", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String password = new String(pf.getPassword());
      // Attempt login with current admin credentials to verify password
      ApiResponse<String> loginRes = authapi.login(currentAdminUsername, password);
      if (loginRes.isSuccess()) {
        return true;
      } else {
        Toast.show(this, "Incorrect password. Action cancelled.");
        log.warn("Sudo check failed for user: {}", currentAdminUsername);
        return false;
      }
    }
    return false; // Cancelled
  }

  private void deleteSelectedUser() {
    int row = usersTable.getSelectedRow();

    if (row == -1) {
      Toast.show(this, "Please select a user to delete.");
      return;
    }

    String username = (String) usersmodel.getValueAt(row, 1);
    String role = (String) usersmodel.getValueAt(row, 2);

    // --- 2FA CHECK FOR ADMIN DELETION ---
    if ("admin".equalsIgnoreCase(role)) {
      // Prevent deleting yourself
      if (this.currentAdminUsername.equalsIgnoreCase(username)) {
        Toast.show(this, "Error: Cannot delete yourself.");
        log.warn("Admin user {} attempted to delete themselves.", currentAdminUsername);
        return;
      }

      // Prompt for password
      if (!performSudoCheck("delete an ADMIN user")) {
        return;
      }
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete user: " + username + " ?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) return;

    log.info("Attempting to delete user: {}", username);
    ApiResponse<String> response = null;
    ApiResponse<String> response2 = null;

    if (role.equalsIgnoreCase("student")) {
      response = adminApi.delete_stdent(username);
      response2 = authapi.deleteUser(username);
    } else if (role.equalsIgnoreCase("instructor")) {
      response = adminApi.delete_instructor(username);
      response2 = authapi.deleteUser(username);
    } else if (role.equalsIgnoreCase("admin")) {
      // Admin has no student/instructor table entry, only Auth
      response = authapi.deleteUser(username);
      response2 = ApiResponse.success("OK"); // dummy so checks pass
    } else {
      Toast.show(this, "Unknown role type!");
      log.error("Unknown role type '{}' for user {}", role, username);
      return;
    }

    if (response.isSuccess() && response2.isSuccess()) {
      Toast.show(this, "User deleted successfully.");
      log.info("User {} deleted successfully.", username);
      refreshCatalog();
    } else {
      String errorMessage =
          "Error: " + response.getMessage() + " | " + response2.getMessage();
      Toast.show(this, errorMessage);
      log.error("Failed to delete user {}. Reason: {}", username, errorMessage);
    }
  }

  private void createUser() {
    String username = usernameField.getText().trim().toLowerCase();
    String password = new String(passwordField.getPassword()).trim();
    String role = (String) roleSelector.getSelectedItem();
    role = role.toLowerCase();

    if (username.isEmpty() || password.isEmpty()) {
      Toast.show(this, "All fields must be filled.");
      log.warn("User creation attempt with empty fields.");
      return;
    }

    // --- 2FA CHECK FOR ADMIN CREATION ---
    if ("admin".equalsIgnoreCase(role)) {
      if (!performSudoCheck("create a new ADMIN user")) {
        return;
      }
    }

    log.info("Attempting to create user: {} with role: {}", username, role);
    ApiResponse<String> r1; // auth create
    ApiResponse<String> r2 = ApiResponse.success("OK"); // profile create

    // --- CREATE AUTH USER ---
    r1 = authapi.insertUser(username, role, password);

    if (!r1.isSuccess()) {
      Toast.show(this, "Auth Creation Failed: " + r1.getMessage());
      log.error("Auth creation failed for user {}. Reason: {}", username, r1.getMessage());
      return;
    }

    // --- CREATE PROFILE ---
    if (role.equalsIgnoreCase("STUDENT")) {
      String roll = rollNumberField.getText().trim();
      String program = programField.getText().trim();
      String yearStr = yearField.getText().trim();

      if (roll.isEmpty() || program.isEmpty() || yearStr.isEmpty()) {
        Toast.show(this, "Student fields cannot be empty.");
        log.warn("Student creation for user {} failed due to empty fields.", username);
        return;
      }

      int year;
      try {
        year = Integer.parseInt(yearStr);
      } catch (Exception ex) {
        Toast.show(this, "Year must be a number.");
        log.warn("Invalid year format for user {}: {}", username, yearStr);
        return;
      }
      r2 = adminApi.adduser_erp(username, roll, year, program);

    } else if (role.equalsIgnoreCase("INSTRUCTOR")) {
      String dept = departmentField.getText().trim();
      if (dept.isEmpty()) {
        Toast.show(this, "Department cannot be empty.");
        log.warn("Instructor creation for user {} failed due to empty department.", username);
        return;
      }
      r2 = adminApi.addinstructor(username, dept);
    }

    if (r1.isSuccess() && r2.isSuccess()) {
      Toast.show(this, "User created successfully!");
      log.info("User {} created successfully.", username);
      refreshCatalog();

      // Clear fields
      usernameField.setText("");
      passwordField.setText("");
      rollNumberField.setText("");
      programField.setText("");
      yearField.setText("");
      departmentField.setText("");
    } else {
      String errorMessage = "Error: " + r2.getMessage();
      Toast.show(this, errorMessage);
      log.error("Profile creation failed for user {}. Reason: {}", username, r2.getMessage());
    }
  }

  public void refreshCatalog() {
    usersmodel.setRowCount(0);
    ApiResponse<ArrayList<ArrayList<String>>> response = catalogApi.view_users();

    if (response.isSuccess()) {
      ArrayList<ArrayList<String>> data = response.getData();
      for (ArrayList<String> rowData : data) {
        usersmodel.addRow(rowData.toArray());
      }
    } else {
      String errorMessage = "Failed to load catalog: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error(errorMessage);
    }
  }
}