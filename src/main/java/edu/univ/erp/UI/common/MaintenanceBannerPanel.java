package edu.univ.erp.UI.common;

import javax.swing.*;
import java.awt.*;


public class MaintenanceBannerPanel extends JPanel {

  public MaintenanceBannerPanel() {
    setBackground(new Color(255, 243, 205));
    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 229, 153)));

    JLabel messageLabel = new JLabel("System is in Maintenance Mode. All editing functions are disabled.");
    messageLabel.setForeground(new Color(133, 100, 4));
    messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

    Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");
    messageLabel.setIcon(warningIcon);

    add(messageLabel);
    setVisible(false);
  }

  public void setBannerVisible(boolean isVisible) {
    setVisible(isVisible);
  }
}