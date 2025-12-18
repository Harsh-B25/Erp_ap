package edu.univ.erp.UI.common;

import javax.swing.*;
import java.awt.*;

public class LogOut {
  JButton logoutButton = new JButton("Log Out");

  public LogOut() {
    logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
  }

  public JButton getLogoutButton() {
    return this.logoutButton;
  }

}