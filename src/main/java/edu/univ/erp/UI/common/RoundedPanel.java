package edu.univ.erp.UI.common;

import javax.swing.*;
import java.awt.*;


public class RoundedPanel extends JPanel {
  private int cornerRadius;

  public RoundedPanel(LayoutManager layout, int radius) {
    super(layout);
    this.cornerRadius = radius;

    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    g2d.setColor(getBackground());
    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

    g2d.dispose();
  }
}