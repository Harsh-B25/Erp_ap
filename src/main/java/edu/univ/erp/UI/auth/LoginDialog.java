package edu.univ.erp.UI.auth;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.common.ApiResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * The modal Login Dialog.
 * This class connects to the AuthApi and authenticates the user.
 */
public class LoginDialog extends JDialog {

  private LoginPanel loginPanel; // The panel with the fields
  private AuthApi authApi;
  private String authenticatedSession;
  private String username;

  public LoginDialog(Frame parent) {
    super(parent, "Login", true); // 'true' = modal

    this.authApi = new AuthApi();
    this.authenticatedSession = null;

    loginPanel = new LoginPanel();    

    loginPanel.setOpaque(false);


    JPanel wrapperPanel = new JPanel(new GridBagLayout()) {
      private BufferedImage backgroundImage;


      {
        try {

          URL imageUrl = getClass().getResource("/iiitd.jpeg");
          if (imageUrl != null) {
            backgroundImage = ImageIO.read(imageUrl);
          } else {

            System.err.println("Background image not found!");
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
          g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
      }
    };

    wrapperPanel.add(loginPanel);
    this.setContentPane(wrapperPanel);

    // --- THIS IS THE CONNECTION ---
    loginPanel.getLoginButton().addActionListener(e -> onLoginClick());
    this.getRootPane().setDefaultButton(loginPanel.getLoginButton());
    

    this.setSize(1200, 900);
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.setLocationRelativeTo(parent);
  }


  private void onLoginClick() {
    String username = loginPanel.getUsernameField().getText();
    String password = new String(loginPanel.getPasswordField().getPassword());

    if (username.isEmpty() || password.isEmpty()) {
      loginPanel.getErrorLabel().setText("Username and password cannot be empty.");
      return;
    }

    ApiResponse<String> response = authApi.login(username, password);

    if (response.isSuccess()) {
      ApiResponse<String> response2 = authApi.getRole(username);
      this.authenticatedSession = response2.getData();
      this.username = username;
      this.dispose();
    } else {
      loginPanel.getErrorLabel().setText(response.getMessage());
    }
  }

  public String getUsername() {
    return this.username;
  }


  public String getAuthenticatedSession() {
    return this.authenticatedSession;
  }
}