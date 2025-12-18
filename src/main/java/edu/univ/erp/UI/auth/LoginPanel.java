package edu.univ.erp.UI.auth;

import javax.swing.*;
import java.awt.*;


public class LoginPanel extends JPanel {

  // --- Component Fields ---
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JButton loginButton;
  private JLabel errorLabel;


  public LoginPanel() {
    setLayout(new GridBagLayout());

    JPanel formContainer = new JPanel(new GridBagLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 180));
        int arc = UIManager.getInt("Component.arc");
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    formContainer.setOpaque(false);
    formContainer.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

    // --- Components ---
    usernameField = new JTextField(20);
    passwordField = new JPasswordField(20);
    loginButton = new JButton("Login");
    errorLabel = new JLabel(" ");
    errorLabel.setForeground(new Color(255, 100, 100));
    errorLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));

    // --- Layout ---
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.EAST;
    gbc.gridx = 0;
    gbc.gridy = 0;
    JLabel username = new JLabel("Username:");
    username.setForeground(Color.WHITE);
    formContainer.add(username, gbc);

    gbc.gridy = 1;
    JLabel pass = new JLabel("Password:");
    pass.setForeground(Color.WHITE);
    formContainer.add(pass, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    formContainer.add(usernameField, gbc);

    gbc.gridy = 1;
    formContainer.add(passwordField, gbc);

    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    formContainer.add(errorLabel, gbc);

    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    formContainer.add(loginButton, gbc);

    add(formContainer, new GridBagConstraints());
  }

  // --- Public Getters ---
  public JTextField getUsernameField() { return usernameField; }
  public JPasswordField getPasswordField() { return passwordField; }
  public JButton getLoginButton() { return loginButton; }
  public JLabel getErrorLabel() { return errorLabel; }
  public JPanel getRootPanel() { return this; }
}