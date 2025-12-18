package edu.univ.erp.UI.instructor;

import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.api.maintenance.MaintenanceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewSectionsPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(ViewSectionsPanel.class);

  private JComboBox<String> semesterSelector;
  private JComboBox<String> sectionSelector;
  private JTable gradebookTable;
  private JButton enterScoresButton;
  private JButton computeFinalGradesButton;
  private JButton viewClassStatisticsButton;
  private JButton exportGradesButton;

  // --- NEW: Global Weight Inputs ---
  private JTextField quizWeightField;
  private JTextField midWeightField;
  private JTextField finalWeightField;
  private JButton applyWeightsButton;

  private DefaultTableModel model;
  private List<ArrayList<String>> currentGradeData;

  private InstructorApi instructorApi = new InstructorApi();
  private String selectedSectionId = null;
  private String instructorUsername;
  private boolean isLoadingSemesters = false;

  public ViewSectionsPanel(String instructorUsername) {
    this.instructorUsername = instructorUsername;
    this.currentGradeData = new ArrayList<>();

    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


    JPanel topContainer = new JPanel();
    topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

    topContainer.add(createSelectionPanel());
    topContainer.add(Box.createRigidArea(new Dimension(0, 5)));
    topContainer.add(createWeightPanel()); // Add the new weight panel

    add(topContainer, BorderLayout.NORTH);
    add(createCenterPanel(), BorderLayout.CENTER);
    add(createBottomPanel(), BorderLayout.SOUTH);

    addListeners();

    this.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            loadSemesters();
          }
        });
  }

  private JPanel createSelectionPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

    panel.add(new JLabel("Semester:"));
    semesterSelector = new JComboBox<>();
    semesterSelector.setPreferredSize(new Dimension(150, 25));
    panel.add(semesterSelector);

    panel.add(new JLabel("Section:"));
    sectionSelector = new JComboBox<>(new String[] {"Select Semester First"});
    sectionSelector.setPreferredSize(new Dimension(300, 25));
    panel.add(sectionSelector);

    return panel;
  }

  // --- Create Weight Input Panel ---
  private JPanel createWeightPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
    panel.setBorder(BorderFactory.createTitledBorder("Set Class Weights"));

    panel.add(new JLabel("Quiz Wt:"));
    quizWeightField = new JTextField("20", 4);
    panel.add(quizWeightField);

    panel.add(new JLabel("Midterm Wt:"));
    midWeightField = new JTextField("30", 4);
    panel.add(midWeightField);

    panel.add(new JLabel("Final Wt:"));
    finalWeightField = new JTextField("50", 4);
    panel.add(finalWeightField);

    applyWeightsButton = new JButton("Apply to All");
    panel.add(applyWeightsButton);

    return panel;
  }

  private JPanel createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel("Student roster and grades (Relative Grading):"), BorderLayout.NORTH);

    String[] cols = {
      "Roll No", "Username", "Quiz", "Total", "Wt", "Midterm", "Total", "Wt", "Final", "Total",
      "Wt", "Grade"
    };

    model =
        new DefaultTableModel(cols, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            // Editable: Quiz / Mid / Final score, total, weight
            return c >= 2 && c <= 10;
          }
        };

    gradebookTable = new JTable(model);
    gradebookTable.getTableHeader().setReorderingAllowed(false);
    gradebookTable.setRowHeight(24);

    JScrollPane scroll = new JScrollPane(gradebookTable);
    panel.add(scroll, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createBottomPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    enterScoresButton = new JButton("Enter Scores");
    computeFinalGradesButton = new JButton("Compute Grades");
    viewClassStatisticsButton = new JButton("Statistics");
    exportGradesButton = new JButton("Export PDF");

    panel.add(enterScoresButton);
    panel.add(computeFinalGradesButton);
    panel.add(viewClassStatisticsButton);
    panel.add(exportGradesButton);

    return panel;
  }

  private void addListeners() {
    semesterSelector.addActionListener(
        e -> {
          if (isLoadingSemesters) return;
          String sem = (String) semesterSelector.getSelectedItem();
          if (sem != null && !sem.equals("Select...") && !sem.equals("Error loading")) {
            loadSectionsForSemester(sem);
          } else {
            sectionSelector.removeAllItems();
            sectionSelector.addItem("Select Semester First");
          }
        });

    sectionSelector.addActionListener(
        e -> {
          String sec = (String) sectionSelector.getSelectedItem();
          if (sec == null || sec.contains("Select") || sec.equals("Loading...")) return;
          try {
            selectedSectionId = sec.split(" - ")[0];
            loadGradebook();
          } catch (Exception ex) {
            log.error("Error parsing section ID from string: {}", sec, ex);
          }
        });

    // --- Apply Weights Listener ---
    applyWeightsButton.addActionListener(e -> applyWeightsToAllRows());

    enterScoresButton.addActionListener(e -> saveAllRows());

    computeFinalGradesButton.addActionListener(
        e -> {
          try {
            computeAllFinalGrades();
            Toast.show(this, "Relative grades computed successfully.");
            log.info("Computed final grades for section: {}", selectedSectionId);
          } catch (Exception ex) {
            Toast.show(this, ex.getMessage());
            log.error(
                "Error computing final grades for section: {}", selectedSectionId, ex);
          }
        });

    exportGradesButton.addActionListener(e -> exportGradesToCSV());

    viewClassStatisticsButton.addActionListener(
        e -> {
          try {
            showStats();
          } catch (Exception ex) {
            Toast.show(this, ex.getMessage());
            log.error("Error showing statistics for section: {}", selectedSectionId, ex);
          }
        });
  }

  // --- Logic to apply weights ---
  private void applyWeightsToAllRows() {
    try {
      float qW = Float.parseFloat(quizWeightField.getText().trim());
      float mW = Float.parseFloat(midWeightField.getText().trim());
      float fW = Float.parseFloat(finalWeightField.getText().trim());

      // Optional: Check if sum is 100 (warn only, don't block)
      if (Math.abs((qW + mW + fW) - 100) > 0.1) {
        int confirm =
            JOptionPane.showConfirmDialog(
                this,
                "Total weight is " + (qW + mW + fW) + "%. Continue?",
                "Weight Warning",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
      }

      // Update table model
      for (int r = 0; r < model.getRowCount(); r++) {
        model.setValueAt(String.valueOf(qW), r, 4); // Quiz Wt column
        model.setValueAt(String.valueOf(mW), r, 7); // Mid Wt column
        model.setValueAt(String.valueOf(fW), r, 10); // Final Wt column
      }

      Toast.show(
          this, "Weights applied to all rows. Remember to click 'Enter Scores' to save.");
      log.info(
          "Applied weights (Q:{}, M:{}, F:{}) to all rows for section: {}",
          qW,
          mW,
          fW,
          selectedSectionId);

    } catch (NumberFormatException e) {
      Toast.show(this, "Please enter valid numbers for weights.");
      log.warn("Invalid number format entered for weights.");
    }
  }

  // --- LOADING LOGIC ---

  private void loadSemesters() {
    isLoadingSemesters = true;
    semesterSelector.removeAllItems();
    sectionSelector.removeAllItems();
    sectionSelector.addItem("Select Semester First");

    ApiResponse<ArrayList<String>> res = instructorApi.listSemesters(instructorUsername);

    if (!res.isSuccess()) {
      semesterSelector.addItem("Error loading");
      log.error(
          "Failed to load semesters for instructor: {}. Reason: {}",
          instructorUsername,
          res.getMessage());
    } else {
      semesterSelector.addItem("Select...");
      for (String s : res.getData()) {
        semesterSelector.addItem(s);
      }
    }
    isLoadingSemesters = false;
  }

  private void loadSectionsForSemester(String semString) {
    sectionSelector.removeAllItems();
    try {
      String[] parts = semString.split(" - ");
      String sem = parts[0];
      int year = Integer.parseInt(parts[1]);

      ApiResponse<ArrayList<String>> res =
          instructorApi.listSectionsBySemester(instructorUsername, sem, year);

      if (!res.isSuccess() || res.getData().isEmpty()) {
        sectionSelector.addItem("No sections found");
      } else {
        sectionSelector.addItem("Select Section...");
        for (String s : res.getData()) {
          sectionSelector.addItem(s);
        }
      }
    } catch (Exception e) {
      sectionSelector.addItem("Error parsing date");
      log.error("Error parsing semester string: {}", semString, e);
    }
  }

  private void loadGradebook() {
    model.setRowCount(0);
    currentGradeData.clear();

    if (selectedSectionId == null) return;

    ApiResponse<ArrayList<ArrayList<String>>> res =
        instructorApi.getroster(Integer.parseInt(selectedSectionId));

    if (!res.isSuccess()) {
      Toast.show(this, "Failed: " + res.getMessage());
      log.error(
          "Failed to load gradebook for section: {}. Reason: {}",
          selectedSectionId,
          res.getMessage());
      return;
    }

    for (ArrayList<String> row : res.getData()) {
      model.addRow(row.toArray());
      currentGradeData.add(row);
    }

    // Autofill the weight fields based on the first student (if data exists)
    if (model.getRowCount() > 0) {
      String qw = (String) model.getValueAt(0, 4);
      String mw = (String) model.getValueAt(0, 7);
      String fw = (String) model.getValueAt(0, 10);

      if (qw != null && !qw.isEmpty()) quizWeightField.setText(qw);
      if (mw != null && !mw.isEmpty()) midWeightField.setText(mw);
      if (fw != null && !fw.isEmpty()) finalWeightField.setText(fw);
    }

    try {
      computeAllFinalGrades();
    } catch (Exception e) {
      // Ignore errors on initial load
    }
  }

  private void saveAllRows() {
    try {
      if (Boolean.parseBoolean(new MaintenanceApi().checkMaintenance().getData())) {
        throw new Exception("Maintenance mode is ON.");
      }

      for (int r = 0; r < model.getRowCount(); r++) {
        String username = val(r, 1);
        saveComponent(r, username, "quiz", 2, 3, 4);
        saveComponent(r, username, "midterm", 5, 6, 7);
        saveComponent(r, username, "final", 8, 9, 10);
      }
      loadGradebook();
      Toast.show(this, "Scores saved successfully.");
      log.info("Scores saved for section: {}", selectedSectionId);
    } catch (Exception e) {
      Toast.show(this, "Error saving scores: " + e.getMessage());
      log.error("Error saving scores for section: {}", selectedSectionId, e);
    }
  }

  private void saveComponent(
      int row, String username, String comp, int sIdx, int tIdx, int wIdx) throws Exception {
    String sc = val(row, sIdx);
    String tot = val(row, tIdx);
    String wt = val(row, wIdx);

    try {
      if (sc.isEmpty() || tot.isEmpty() || wt.isEmpty()) return;

      float scInt = Float.parseFloat(sc);
      float totInt = Float.parseFloat(tot);
      float wtFloat = Float.parseFloat(wt);

      if (scInt < 0) throw new Exception("Score cannot be negative for " + username);
      if (totInt <= 0) throw new Exception("Total marks must be > 0 for " + username);
      if (scInt > totInt) throw new Exception("Score exceeds total for " + username);

      instructorApi.updategrade(
          username, comp, Integer.parseInt(selectedSectionId), scInt, totInt, wtFloat);
    } catch (NumberFormatException e) {
      // Ignore
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  private String val(int r, int c) {
    Object o = model.getValueAt(r, c);
    return (o == null) ? "" : o.toString().trim();
  }

  // ==========================================================
  // RELATIVE GRADING COMPUTATION
  // ==========================================================
  private void computeAllFinalGrades() throws Exception {

    List<Double> totals = new ArrayList<>();
    int rowCount = model.getRowCount();

    // 1. Calculate all weighted totals first
    for (int r = 0; r < rowCount; r++) {
      try {
        double finalGrade = 0;
        // Quiz
        finalGrade += computeOne(r, 2, 3, 4);
        // Midterm
        finalGrade += computeOne(r, 5, 6, 7);
        // Final
        finalGrade += computeOne(r, 8, 9, 10);

        totals.add(finalGrade);
      } catch (Exception e) {
        totals.add(null); // Incomplete data
      }
    }

    // 2. Calculate Mean and Standard Deviation
    double mean = 0;
    double stdDev = 0;
    List<Double> validScores = new ArrayList<>();

    for (Double score : totals) {
      if (score != null) validScores.add(score);
    }

    if (!validScores.isEmpty()) {
      double sum = 0;
      for (Double s : validScores) sum += s;
      mean = sum / validScores.size();

      double sumSqDiff = 0;
      for (Double s : validScores) sumSqDiff += Math.pow(s - mean, 2);
      stdDev = Math.sqrt(sumSqDiff / validScores.size());
    }

    // 3. Assign Relative Grades
    for (int r = 0; r < rowCount; r++) {
      Double total = totals.get(r);
      if (total != null) {
        String letterGrade = getRelativeGrade(total, mean, stdDev);
        model.setValueAt(letterGrade, r, 11); // Autofill Grade Column
      } else {
        model.setValueAt("", r, 11);
      }
    }
  }

  private String getRelativeGrade(double score, double mean, double stdDev) {
    // Absolute override
    if (score >= 95) return "A+";
    if (score < 30.0) return "F";

    // Relative Slabs
    if (score >= mean + (1.5 * stdDev)) return "A";
    if (score >= mean + (1.0 * stdDev)) return "A-";
    if (score >= mean + (0.5 * stdDev)) return "B";
    if (score >= mean) return "B-";
    if (score >= mean - (0.5 * stdDev)) return "C";
    if (score >= mean - (1.0 * stdDev)) return "C-";
    if (score >= mean - (1.5 * stdDev)) return "D";
    return "F";
  }

  private double computeOne(int r, int sIdx, int tIdx, int wIdx) throws Exception {
    String s = val(r, sIdx);
    String t = val(r, tIdx);
    String w = val(r, wIdx);

    if (s.isEmpty() || t.isEmpty() || w.isEmpty()) throw new Exception("Incomplete data");

    double score = Double.parseDouble(s);
    double total = Double.parseDouble(t);
    double weight = Double.parseDouble(w);

    if (total == 0) return 0;

    return (score / total) * weight;
  }

  private void showStats() throws Exception {
    if (currentGradeData == null || currentGradeData.isEmpty()) {
      Toast.show(this, "No data available for statistics.");
      return;
    }

    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
    ClassStatsDialog statsDialog = new ClassStatsDialog(parentFrame, currentGradeData);
    statsDialog.setVisible(true);
  }

  public void exportGradesToCSV() {
    if (selectedSectionId == null) {
      Toast.show(this, "Please select a section first.");
      return;
    }
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setSelectedFile(new File("users_report.pdf"));
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        instructorApi.generatepdf(
            fileChooser.getSelectedFile().getAbsolutePath(),
            selectedSectionId,
            instructorUsername,
            gradebookTable.getModel());
        Toast.show(this, "PDF saved.");
        log.info(
            "Exported grades to PDF for section: {} at path: {}",
            selectedSectionId,
            fileChooser.getSelectedFile().getAbsolutePath());
      } catch (Exception ex) {
        Toast.show(this, "Error: " + ex.getMessage());
        log.error(
            "Error exporting grades to PDF for section: {}", selectedSectionId, ex);
      }
    }
  }
}