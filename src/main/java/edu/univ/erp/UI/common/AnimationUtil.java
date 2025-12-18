package edu.univ.erp.UI.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimationUtil {

  public static void fadeIn(Window window) {

    if (!GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
        .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
      return;
    }


    if (window instanceof JDialog) {
      if (!((JDialog) window).isUndecorated()) return;
    }
    if (window instanceof JFrame) {
      if (!((JFrame) window).isUndecorated()) return;
    }

    try {
      window.setOpacity(0.0f);

      final Timer timer = new Timer(30, null);
      timer.addActionListener(new ActionListener() {
        float opacity = 0.0f;
        @Override
        public void actionPerformed(ActionEvent e) {
          opacity += 0.05f;
          if (opacity >= 1.0f) {
            opacity = 1.0f;
            try { window.setOpacity(opacity); } catch(Exception ex) {}
            timer.stop();
          } else {
            try { window.setOpacity(opacity); } catch(Exception ex) {}
          }
        }
      });
      timer.start();
    } catch (Exception e) {
      System.err.println("Animation skipped: " + e.getMessage());
      try { window.setOpacity(1.0f); } catch(Exception ex) {}
    }
  }


}