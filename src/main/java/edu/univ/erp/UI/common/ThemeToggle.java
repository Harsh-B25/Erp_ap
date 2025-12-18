package edu.univ.erp.UI.common;

import javax.swing.*;
import java.awt.*;

public class ThemeToggle {
    private static boolean theme = true;

    public static void setLightTheme() {
        com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme.setup();
        refreshUI();
    }

    public static void setDarkTheme() {
        com.formdev.flatlaf.intellijthemes.FlatArcIJTheme.setup();
        refreshUI();
    }

    private static void refreshUI() {
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            w.repaint();
        }
    }

    public static JButton addToggleTheme(boolean flag) {
        theme = flag;
        JButton themeToggle = new JButton("Toggle Dark Mode");
        themeToggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        themeToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        themeToggle.addActionListener(e -> {
            if(!theme) {
                setLightTheme();
                theme = true;
            } else {
                setDarkTheme();
                theme = false;
            }

            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
            }
        });

        return themeToggle;
    }
}