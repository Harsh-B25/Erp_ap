package edu.univ.erp.UI.auth;

import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;


public class ChangePasswordDialog extends JDialog {

  private static final Logger log = LoggerFactory.getLogger(ChangePasswordDialog.class);

  // --- Component Fields ---
  private JPasswordField oldPasswordField;
  private JPasswordField newPasswordField;
  private JPasswordField confirmPasswordField;
  private JButton okButton;
  private JButton cancelButton;
  private JLabel messageLabel;

  // --- Session and API ---
  private String session;
  private AuthApi authApi;
  private String username;
  private MaintenanceApi maintenanceApi;

  public ChangePasswordDialog(Frame parent, String session, String username) {
    super(parent, "Change Password", true);
    this.session = session;
    this.username = username;
    this.authApi = new AuthApi();
    this.maintenanceApi = new MaintenanceApi();

    // --- Create Main Panel ---
    JPanel contentPane = new JPanel(new BorderLayout(10, 10));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    contentPane.add(createFieldsPanel(), BorderLayout.CENTER);
    contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

    setContentPane(contentPane);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    pack();
    setLocationRelativeTo(parent);
  }

  private JPanel createFieldsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    // Title
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    JLabel titleLabel = new JLabel("Change Password:");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    panel.add(titleLabel, gbc);

    // Old Password
    gbc.gridy = 1;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    panel.add(new JLabel("Enter Old Password:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    oldPasswordField = new JPasswordField(20);
    panel.add(oldPasswordField, gbc);

    // New Password
    gbc.gridy = 2;
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Enter New Password:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    newPasswordField = new JPasswordField(20);
    panel.add(newPasswordField, gbc);

    // Confirm Password
    gbc.gridy = 3;
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Confirm New Password:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    confirmPasswordField = new JPasswordField(20);
    panel.add(confirmPasswordField, gbc);

    // Message Label
    gbc.gridy = 4;
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    messageLabel = new JLabel(" ");
    messageLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
    panel.add(messageLabel, gbc);

    return panel;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

    okButton = new JButton("OK");
    cancelButton = new JButton("Cancel");

    getRootPane().setDefaultButton(okButton);

    panel.add(okButton);
    panel.add(cancelButton);

    // --- Add Listeners ---
    okButton.addActionListener(e -> onChangePasswordClick());
    cancelButton.addActionListener(e -> dispose()); // Just close the dialog

    return panel;
  }

  private void onChangePasswordClick() {
    String oldPass = new String(oldPasswordField.getPassword());
    String newPass = new String(newPasswordField.getPassword());
    String confirmPass = new String(confirmPasswordField.getPassword());

    if (oldPass.isEmpty() || newPass.isEmpty()) {
      messageLabel.setForeground(Color.RED);
      messageLabel.setText("Passwords cannot be empty.");
      log.warn("Password change attempt with empty fields.");
      return;
    }
    if (!newPass.equals(confirmPass)) {
      messageLabel.setForeground(Color.RED);
      messageLabel.setText("New passwords do not match.");
      log.warn("Password change attempt with mismatched passwords.");
      return;
    }

    if (Boolean.parseBoolean(maintenanceApi.checkMaintenance().getData())) {
      Toast.show(this, "Maintenance mode is ON.");
      log.info("Password change blocked due to maintenance mode.");
    } else {
      log.info("Attempting to change password for user: {}", username);
      ApiResponse<String> response = authApi.changePassword(this.username, oldPass, newPass);

      if (response.isSuccess()) {
        messageLabel.setForeground(new Color(0, 128, 0));
        messageLabel.setText("Password changed successfully!");
        log.info("Password changed successfully for user: {}", username);
        // Clear fields after a short delay
        Timer timer = new Timer(2000, e -> dispose());
        timer.setRepeats(false);
        timer.start();
      } else {
        messageLabel.setForeground(Color.RED);
        messageLabel.setText(response.getMessage());
        log.error(
            "Password change failed for user: {}. Reason: {}", username, response.getMessage());
      }
    }
  }
}