package edu.univ.erp.UI.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/** A custom toast notification window that fades in and out. */
public class Toast extends JWindow {

  private float opacity = 1.0f;

  // Default color scheme
  private static final Color DEFAULT_BACKGROUND = new Color(40, 41, 50);
  private static final Color DEFAULT_TEXT = Color.WHITE;
  private static final Color DEFAULT_BORDER = new Color(59, 62, 75);
  private static final int CORNER_RADIUS = 12;

  public Toast(String message, Component parent) {
    this(message, parent, DEFAULT_BORDER);
  }

  public Toast(String message, Component parent, Color borderColor) {

    setBackground(new Color(0, 0, 0, 0));

    setLayout(new BorderLayout());

    RoundedPanel panel = new RoundedPanel(new BorderLayout(), CORNER_RADIUS);
    panel.setBackground(DEFAULT_BACKGROUND);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    int maxWidth = 350;
    Font messageFont = new Font("SansSerif", Font.BOLD, 14);

    FontMetrics metrics = getFontMetrics(messageFont);
    int textWidth = metrics.stringWidth(message);

    String htmlMessage;
    if (textWidth > maxWidth) {

      htmlMessage = "<html><body style='width: " + maxWidth + "px; text-align: center;'>" + message + "</body></html>";
    } else {

      htmlMessage = "<html><body style='text-align: center;'>" + message + "</body></html>";
    }

    JLabel messageLabel = new JLabel(htmlMessage);
    messageLabel.setForeground(DEFAULT_TEXT);
    messageLabel.setFont(messageFont);
    messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

    panel.add(messageLabel, BorderLayout.CENTER);
    add(panel, BorderLayout.CENTER);


    pack();


    if (parent != null && parent.isShowing()) {
      Point loc = parent.getLocationOnScreen();

      int x = loc.x + parent.getWidth() - this.getWidth() - 20;
      int y = loc.y + parent.getHeight() - this.getHeight() - 40;
      setLocation(x, y);
    } else {

      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation(screen.width / 2 - this.getWidth() / 2, screen.height - 100);
    }
  }

  public void showToast() {
    setVisible(true);


    Timer timer = new Timer(3000, e -> fadeOut());
    timer.setRepeats(false);
    timer.start();
  }

  private void fadeOut() {
    final Timer fadeTimer = new Timer(40, null);
    fadeTimer.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        opacity -= 0.05f;
        if (opacity <= 0.0f) {
          fadeTimer.stop();
          dispose();
        } else {
          setOpacity(Math.max(opacity, 0));
        }
      }
    });
    fadeTimer.start();
  }

  public static void show(Component parent, String message) {
    new Toast(message, parent).showToast();
  }

  public static void show(Component parent, String message, Color borderColor) {
    new Toast(message, parent, borderColor).showToast();
  }
}