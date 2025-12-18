package edu.univ.erp.UI.admin;

import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class CourseManagementPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(CourseManagementPanel.class);

  private JTable coursesTable;
  private DefaultTableModel model;
  private TableRowSorter<DefaultTableModel> sorter;

  private JTextField searchField;
  private JComboBox<String> filterColumnSelector;

  private JTextField courseCodeField;
  private JTextField courseTitleField;
  private JSpinner creditsSpinner;

  private JButton saveCourseButton;
  private JButton clearFormButton;
  private JButton deleteCourseButton;

  private CatalogApi catalogApi = new CatalogApi();
  private AdminApi adminApi = new AdminApi();

  public CourseManagementPanel() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    add(createSearchPanel(), BorderLayout.NORTH);
    add(createTablePanel(), BorderLayout.CENTER);
    add(createFormPanel(), BorderLayout.SOUTH);

    addListeners();

    refreshCourses();

    this.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            refreshCourses();
          }
        });
  }

  private JPanel createSearchPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    panel.add(new JLabel("Search:"));
    searchField = new JTextField(15);
    panel.add(searchField);

    panel.add(new JLabel("Filter By:"));
    String[] options = {"All", "Course Code", "Title", "Credits"};
    filterColumnSelector = new JComboBox<>(options);
    panel.add(filterColumnSelector);

    return panel;
  }

  private JComponent createTablePanel() {
    String[] columns = {"Course Code", "Title", "Credits"};
    model =
        new DefaultTableModel(columns, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    coursesTable = new JTable(model);
    coursesTable.getTableHeader().setReorderingAllowed(false);

    sorter = new TableRowSorter<>(model);
    coursesTable.setRowSorter(sorter);

    return new JScrollPane(coursesTable);
  }

  private JPanel createFormPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Add / Edit Course"));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Course Code:"), gbc);
    gbc.gridx = 1;
    courseCodeField = new JTextField(10);
    panel.add(courseCodeField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("Course Title:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    courseTitleField = new JTextField(25);
    panel.add(courseTitleField, gbc);

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.NONE;
    panel.add(new JLabel("Credits:"), gbc);
    gbc.gridx = 3;
    creditsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9, 1));
    panel.add(creditsSpinner, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 4;
    gbc.anchor = GridBagConstraints.EAST;
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    saveCourseButton = new JButton("Save");
    clearFormButton = new JButton("Clear");
    deleteCourseButton = new JButton("Delete");
    buttonPanel.add(saveCourseButton);
    buttonPanel.add(clearFormButton);
    buttonPanel.add(deleteCourseButton);
    panel.add(buttonPanel, gbc);

    return panel;
  }

  private void addListeners() {
    searchField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              public void insertUpdate(DocumentEvent e) {
                filter();
              }

              public void removeUpdate(DocumentEvent e) {
                filter();
              }

              public void changedUpdate(DocumentEvent e) {
                filter();
              }
            });

    filterColumnSelector.addActionListener(e -> filter());

    coursesTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                int viewRow = coursesTable.getSelectedRow();
                if (viewRow != -1) {
                  int modelRow = coursesTable.convertRowIndexToModel(viewRow);
                  courseCodeField.setText(model.getValueAt(modelRow, 0).toString());
                  courseTitleField.setText(model.getValueAt(modelRow, 1).toString());
                  creditsSpinner.setValue(
                      Integer.parseInt(model.getValueAt(modelRow, 2).toString()));
                  courseCodeField.setEnabled(false);
                }
              }
            });

    clearFormButton.addActionListener(e -> clearForm());
    saveCourseButton.addActionListener(e -> saveCourse());
    deleteCourseButton.addActionListener(e -> deleteCourse());
  }

  private void filter() {
    String text = searchField.getText();
    int colIndex = filterColumnSelector.getSelectedIndex();
    if (text.trim().length() == 0) {
      sorter.setRowFilter(null);
    } else {
      if (colIndex == 0) {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
      } else {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, colIndex - 1));
      }
    }
  }

  private void saveCourse() {
    String code = courseCodeField.getText().trim();
    String title = courseTitleField.getText().trim();
    int credits = (Integer) creditsSpinner.getValue();

    // --- VALIDATION ---
    if (code.isEmpty() || title.isEmpty()) {
      Toast.show(this, "Course Code and Title cannot be empty.");
      log.warn("Course save attempt with empty code or title.");
      return;
    }
    if (credits <= 0) {
      Toast.show(this, "Credits must be a positive number.");
      log.warn("Course save attempt with non-positive credits: {}", credits);
      return;
    }

    boolean editing = !courseCodeField.isEnabled();
    ApiResponse<String> response;

    if (editing) {
      log.info("Attempting to update course: {}", code);
      response = adminApi.update_course(code, title, credits);
    } else {
      log.info("Attempting to create course: {}", code);
      response = adminApi.addcourse(code, title, credits);
    }

    if (response.isSuccess()) {
      Toast.show(this, "Course saved successfully!");
      log.info("Course {} saved successfully.", code);
      refreshCourses();
      clearForm();
    } else {
      String errorMessage = "Error: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error("Failed to save course {}. Reason: {}", code, response.getMessage());
    }
  }

  private void deleteCourse() {
    int viewRow = coursesTable.getSelectedRow();
    if (viewRow == -1) {
      Toast.show(this, "Please select a course to delete.");
      return;
    }
    int modelRow = coursesTable.convertRowIndexToModel(viewRow);
    String code = (String) model.getValueAt(modelRow, 0);

    int confirm =
        JOptionPane.showConfirmDialog(
            this, "Delete course: " + code + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    log.info("Attempting to delete course: {}", code);
    ApiResponse<String> response = adminApi.delete_course(code);

    if (response.isSuccess()) {
      Toast.show(this, "Course deleted.");
      log.info("Course {} deleted successfully.", code);
      refreshCourses();
      clearForm();
    } else {
      String errorMessage = "Error: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error("Failed to delete course {}. Reason: {}", code, response.getMessage());
    }
  }

  private void refreshCourses() {
    model.setRowCount(0);
    ApiResponse<ArrayList<ArrayList<String>>> response = catalogApi.listCourses();
    if (response.isSuccess()) {
      for (ArrayList<String> row : response.getData()) {
        model.addRow(row.toArray());
      }
    } else {
      String errorMessage = "Failed to load courses: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error(errorMessage);
    }
  }

  private void clearForm() {
    courseCodeField.setText("");
    courseTitleField.setText("");
    creditsSpinner.setValue(1);
    courseCodeField.setEnabled(true);
    coursesTable.clearSelection();
  }
}