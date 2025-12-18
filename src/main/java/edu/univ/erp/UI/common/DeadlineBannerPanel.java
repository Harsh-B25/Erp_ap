package edu.univ.erp.UI.common;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.common.ApiResponse;

import javax.swing.*;
import java.awt.*;

public class DeadlineBannerPanel extends JPanel {

  public DeadlineBannerPanel() {
    setBackground(new Color(38, 121, 219));
    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0, 69)));


    String displayText = "No active registration deadline.";
    AdminApi adminApi = new AdminApi();

    // 1. Get the Active Semester
    ApiResponse<String> activeRes = adminApi.getCurrentSemester();
    if (activeRes.isSuccess() && activeRes.getData() != null) {
      String activeSemStr = activeRes.getData(); // e.g., "Monsoon-2025"

      try {
        String[] parts = activeSemStr.split("-");
        String sem = parts[0];
        int year = Integer.parseInt(parts[1]);

        // 2. Get the deadline for this semester
        ApiResponse<String> deadlineRes = adminApi.getSemesterDeadline(sem, year);
        if (deadlineRes.isSuccess() && deadlineRes.getData() != null) {
          displayText = "Registration Deadline for " + activeSemStr + ": " + deadlineRes.getData();
        }
      } catch (Exception e) {
        // Fallback if formatting is weird
        displayText = "Registration Deadline: Check Dashboard";
      }
    }


    JLabel messageLabel = new JLabel(displayText);
    messageLabel.setForeground(new Color(255, 255, 255));
    messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

    Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");
    messageLabel.setIcon(warningIcon);

    add(messageLabel);
    setVisible(false);
  }

}