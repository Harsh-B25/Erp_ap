package edu.univ.erp.UI.student;

import edu.univ.erp.UI.common.DeadlineBannerPanel;
import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.student.StudentApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class ViewGradesPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(ViewGradesPanel.class);

  private JComboBox<String> semesterDropdown;
  private JButton searchButton;

  private JTable gradesTable;

  private JLabel totalCreditsLabel;
  private JLabel sgpaLabel;
  private JButton downloadTranscriptButton;

  private StudentApi studentApi = new StudentApi();
  private String username;
  private DeadlineBannerPanel deadlineBanner;

  public ViewGradesPanel(String username, DeadlineBannerPanel deadlineBanner) {
    this.username = username;
    this.deadlineBanner = deadlineBanner;
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    add(createTopPanel(), BorderLayout.NORTH);
    add(createCenterPanel(), BorderLayout.CENTER);
    add(createSummaryPanel(), BorderLayout.SOUTH);

    addListeners();

    // Hide banner by default
    if (deadlineBanner != null) deadlineBanner.setVisible(false);

    this.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            log.debug("ViewGradesPanel shown for user: {}", username); // Debug
            loadSemestersIntoDropdown();
            if (deadlineBanner != null) deadlineBanner.setVisible(false);
          }
        });
  }

  private JPanel createTopPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));

    JLabel titleLabel = new JLabel("My Grades:");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

    semesterDropdown = new JComboBox<>();
    semesterDropdown.setPreferredSize(new Dimension(200, 25)); // Slightly wider
    semesterDropdown.addItem("Select Semester");

    searchButton = new JButton("Search");

    searchPanel.add(new JLabel("Semester:"));
    searchPanel.add(semesterDropdown);
    searchPanel.add(searchButton);

    panel.add(titleLabel, BorderLayout.WEST);
    panel.add(searchPanel, BorderLayout.EAST);

    return panel;
  }

  private JPanel createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));

    String[] columnNames = {
      "S.no",
      "Course Code",
      "Title",
      "Credits",
      "Quiz",
      "Midterm",
      "Final",
      "Grade",
      "Grade Point"
    };

    gradesTable = new JTable(new DefaultTableModel(columnNames, 0));
    gradesTable.getTableHeader().setReorderingAllowed(false);

    JScrollPane scrollPane = new JScrollPane(gradesTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSummaryPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Summary"));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    // Total Credits
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Total Credits Earned:"), gbc);

    gbc.gridx = 1;
    totalCreditsLabel = new JLabel("0");
    totalCreditsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    panel.add(totalCreditsLabel, gbc);

    // SGPA
    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("SGPA:"), gbc);

    gbc.gridx = 1;
    sgpaLabel = new JLabel("0.00");
    sgpaLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    panel.add(sgpaLabel, gbc);

    // Transcript Button
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.gridheight = 2;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.EAST;

    downloadTranscriptButton = new JButton("Download Transcript");
    panel.add(downloadTranscriptButton, gbc);

    return panel;
  }


  // ===============================================================
  //  LOAD SEMESTERS INTO DROPDOWN
  // ===============================================================
  private void loadSemestersIntoDropdown() {
    semesterDropdown.removeAllItems();
    semesterDropdown.addItem("Select Semester");

    log.debug("Fetching semesters for: {}", username); // Debug

    ApiResponse<ArrayList<String>> res = studentApi.listSemesters(username);

    if (!res.isSuccess()) {
      log.error("API Error: {}", res.getMessage());
      Toast.show(this, "Failed to load semesters: " + res.getMessage());
      return;
    }

    ArrayList<String> semesters = res.getData();
    log.debug("Semesters found: {}", semesters.size()); // Debug

    if (semesters.isEmpty()) {
      semesterDropdown.addItem("No Enrollments Found");
    } else {
      for (String sem : semesters) {
        semesterDropdown.addItem(sem);
      }
    }
  }

  // ===============================================================
  //  LOAD GRADEBOOK FOR SELECTED SEMESTER
  // ===============================================================
  private void loadGradebookForSemester(String semester, int year) {

    ApiResponse<ArrayList<ArrayList<String>>> res =
        studentApi.getSemesterGradebook(username, semester, year);

    if (!res.isSuccess()) {
      Toast.show(this, "Failed: " + res.getMessage());
      log.error(
          "Failed to load gradebook for user {}, semester {}, year {}. Reason: {}",
          username,
          semester,
          year,
          res.getMessage());
      return;
    }

    DefaultTableModel m = (DefaultTableModel) gradesTable.getModel();
    m.setRowCount(0);

    int index = 1;
    // Use a counter for valid courses included in SGPA calculation
    int validCoursesCount = 0;
    float totalGradePoints = 0f;
    int totalCredits = 0;

    for (ArrayList<String> row : res.getData()) {
      // Row structure from erp.getStudentSemesterGrades:
      // 0=Code, 1=Title, 2=Credits, 3=Quiz%, 4=Mid%, 5=Fin%, 6=FinalWeighted(or empty)

      String finalWeightedStr = "";
      if (row.size() > 6) finalWeightedStr = row.get(6);

      float weightedScore = 0f;
      boolean hasScore = false;

      if (finalWeightedStr != null && !finalWeightedStr.isEmpty()) {
        try {
          weightedScore = Float.parseFloat(finalWeightedStr);
          hasScore = true;
        } catch (NumberFormatException e) {
          // ignore
        }
      }

      int credits = 0;
      try {
        credits = Integer.parseInt(row.get(2));
      } catch (NumberFormatException e) {
        credits = 0;
      }

      if (!hasScore) {
        // Incomplete grade
        m.addRow(
            new Object[] {
              index++,
              row.get(0), // Code
              row.get(1), // Title
              row.get(2), // Credits
              row.get(3), // Quiz
              row.get(4), // Mid
              row.get(5), // Final
              "N/A",
              "N/A"
            });
      } else {
        // Has final grade
        String letterGrade = floatToGrade(weightedScore);
        double points = gradeToPoint(letterGrade);

        // SGPA calculation: sum(points * credits) / sum(credits)
        totalGradePoints += (points * credits);
        totalCredits += credits;
        validCoursesCount++;

        m.addRow(
            new Object[] {
              index++,
              row.get(0),
              row.get(1),
              row.get(2),
              row.get(3),
              row.get(4),
              row.get(5),
              letterGrade,
              String.format("%.2f", points)
            });
      }
    }

    // Update Labels
    totalCreditsLabel.setText(String.valueOf(totalCredits));

    if (totalCredits > 0) {
      double sgpa = totalGradePoints / totalCredits;
      sgpaLabel.setText(String.format("%.2f", sgpa));
    } else {
      sgpaLabel.setText("0.00");
    }
  }

  private String floatToGrade(float f) {
    if (f >= 95) return "A+";
    else if (f >= 90) return "A";
    else if (f >= 80) return "A-";
    else if (f >= 70) return "B";
    else if (f >= 60) return "B-";
    else if (f >= 50) return "C";
    else if (f >= 40) return "C-";
    else if (f >= 30) return "D";
    else return "F";
  }

  private double gradeToPoint(String grade) {
    switch (grade) {
      case "A+":
        return 10.0;
      case "A":
        return 10.0;
      case "A-":
        return 9.0;
      case "B":
        return 8.0;
      case "B-":
        return 7.0;
      case "C":
        return 6.0;
      case "C-":
        return 5.0;
      case "D":
        return 4.0;
      case "F":
        return 0.0;
      default:
        return 0.0;
    }
  }

  private void addListeners() {
    searchButton.addActionListener(
        e -> {
          String sem = (String) semesterDropdown.getSelectedItem();

          if (sem == null
              || sem.equals("Select Semester")
              || sem.equals("No Enrollments Found")) {
            Toast.show(this, "Please select a valid semester.");
            return;
          }

          try {

            int dash = sem.lastIndexOf('-');
            String semester = sem.substring(0, dash);
            int year = Integer.parseInt(sem.substring(dash + 1));

            loadGradebookForSemester(semester, year);

          } catch (Exception ex) {
            log.error("Error parsing semester/year from string: {}", sem, ex);
            Toast.show(this, "Error parsing semester/year: " + ex.getMessage());
          }
        });

    downloadTranscriptButton.addActionListener(
        e -> {
          String sgpa = sgpaLabel.getText();
          if (sgpa.equals("0.00") || totalCreditsLabel.getText().equals("0")) {
            Toast.show(this, "No grades recorded for this semester yet.");
            return;
          }

          String semSel = (String) semesterDropdown.getSelectedItem();
          if (semSel == null || semSel.equals("Select Semester")) return;

          try {
            String[] parts = semSel.split("-");
            String sem = parts[0];
            int year = Integer.parseInt(parts[parts.length - 1]);

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Transcript PDF");
            chooser.setSelectedFile(
                new java.io.File("transcript_" + sem + "_" + year + ".pdf"));

            int opt = chooser.showSaveDialog(this);
            if (opt != JFileChooser.APPROVE_OPTION) return;

            java.io.File file = chooser.getSelectedFile();

            studentApi.generateSemesterTranscriptPDF(
                file.getAbsolutePath(),
                this.username,
                sem,
                year,
                gradesTable.getModel(),
                Integer.parseInt(totalCreditsLabel.getText()),
                sgpaLabel.getText());

            Toast.show(this, "Transcript saved to:\n" + file.getAbsolutePath());
            log.info(
                "Transcript for user {} saved to: {}", username, file.getAbsolutePath());

          } catch (Exception ex) {
            log.error("Error generating PDF transcript for user {}", username, ex);
            Toast.show(this, "Error generating PDF:\n" + ex.getMessage());
          }
        });
  }

  // Public Getters
  public JTable getGradesTable() {
    return gradesTable;
  }

  public JComboBox<String> getSemesterDropdown() {
    return semesterDropdown;
  }

  public JButton getSearchButton() {
    return searchButton;
  }
}