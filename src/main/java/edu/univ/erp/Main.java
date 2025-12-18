package edu.univ.erp;

import edu.univ.erp.UI.MainFrame;
import edu.univ.erp.UI.auth.LoginDialog;
import edu.univ.erp.UI.common.AnimationUtil;
import edu.univ.erp.UI.common.LogOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException {
    logger.info("Application starting.");

    FileWriter fileWriter = new FileWriter("logs/app.log");
    fileWriter.write("");
    fileWriter.close();


    UIManager.put("Button.arc", 12);
    UIManager.put("Component.arc", 12);
    UIManager.put("ProgressBar.arc", 12);
    UIManager.put("TextComponent.arc", 12);
    // modern cross-platform Look and Feel
    com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme.setup();

    // Run the Swing application on the Event Dispatch Thread
    
    SwingUtilities.invokeLater(() -> {

      // 1. Show the Login Dialog. The app will pause here.
      AtomicReference<LoginDialog> loginDialogRef = new AtomicReference<>(new LoginDialog(null));
      LoginDialog loginDialog = loginDialogRef.get();
      AnimationUtil.fadeIn(loginDialog); // Apply fade-in animation
      loginDialog.setVisible(true);

      // --- Code resumes after login dialog is closed ---

      // 2. Get the session object from the dialog
      AtomicReference<String> sessionRef = new AtomicReference<>(loginDialog.getAuthenticatedSession());
      String session = sessionRef.get();
      if (session != null) {
        // --- Login was successful! ---
        logger.info("Login successful for user: {}", loginDialog.getUsername());

        // 3. Launch the MainFrame, passing it the session
        LogOut logger_ = new LogOut();
        JButton logoutButton = logger_.getLogoutButton();
        
        AtomicReference<MainFrame> mainFrameRef = new AtomicReference<>(new MainFrame(session, loginDialog.getUsername(), logoutButton));
        MainFrame mainFrame = mainFrameRef.get();
        mainFrame.setVisible(true);

        logoutButton.addActionListener((e) -> {
          MainFrame mf = mainFrameRef.get();
          if (mf != null) {
            mf.setVisible(false);
            mf.dispose();
          }
          LoginDialog ld = loginDialogRef.get();
          if (ld != null) {
            ld.setVisible(false);
            ld.dispose();
          }
          mainFrameRef.set(null);
          loginDialogRef.set(new LoginDialog(null));
          LoginDialog newLogin = loginDialogRef.get();
          AnimationUtil.fadeIn(newLogin); // Apply fade-in animation
          newLogin.setVisible(true);

          sessionRef.set(newLogin.getAuthenticatedSession());
          String newSession = sessionRef.get();
          if (newSession !=null) {
            // --- Login was successful! ---
            logger.info("Login successful for user: {}", newLogin.getUsername());

            // 3. Launch the MainFrame, passing it the session
            MainFrame newMain = new MainFrame(newSession, newLogin.getUsername(), logoutButton);
            mainFrameRef.set(newMain);
            newMain.setVisible(true);

          } else {
            // User closed the dialog or failed login
            logger.warn("Login cancelled or failed. Exiting.");
            System.exit(0);
          }
        });

      } else {
        // User closed the dialog or failed login
        logger.warn("Login cancelled or failed. Exiting.");
        System.exit(0);
      }
    });
    logger.info("Application started.");
    };
  }