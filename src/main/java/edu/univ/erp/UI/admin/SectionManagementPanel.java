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

public class SectionManagementPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SectionManagementPanel.class);

  private JTable sectionsTable;
  private DefaultTableModel model;
  private TableRowSorter<DefaultTableModel> sorter;

  // Search Widgets
  private JTextField searchField;
  private JComboBox<String> filterColumnSelector;

  private JComboBox<String> courseSelector;
  private JComboBox<String> instructorSelector;
  private JTextField dayTimeField;
  private JTextField roomField;
  private JSpinner capacitySpinner;
  private JTextField semesterField;
  private JTextField yearField;

  private JButton saveSectionButton;
  private JButton clearFormButton;
  private JButton deleteButton;

  private CatalogApi catalogApi = new CatalogApi();
  private AdminApi adminApi = new AdminApi();

  private String selectedSectionId = null;

  public SectionManagementPanel() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    add(createSearchPanel(), BorderLayout.NORTH);
    add(createTablePanel(), BorderLayout.CENTER);
    add(createFormPanel(), BorderLayout.SOUTH);
    addListeners();

    loadCourses();
    loadInstructors();
    refreshSections();

    this.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            refreshSections();
            loadCourses();
            loadInstructors();
            clearForm();
          }
        });
  }

  private JPanel createSearchPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(new JLabel("Search:"));
    searchField = new JTextField(15);
    panel.add(searchField);
    panel.add(new JLabel("Filter By:"));
    String[] options = {
      "All", "Section ID", "Course Code", "Title", "Instructor", "Semester", "Year"
    };
    filterColumnSelector = new JComboBox<>(options);
    panel.add(filterColumnSelector);
    return panel;
  }

  private JComponent createTablePanel() {
    String[] columns = {
      "Section ID",
      "Course Code",
      "Title",
      "Instructor",
      "Day/Time",
      "Room",
      "Capacity",
      "Semester",
      "Year"
    };

    model =
        new DefaultTableModel(columns, 0) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };

    sectionsTable = new JTable(model);
    sectionsTable.getTableHeader().setReorderingAllowed(false);

    sorter = new TableRowSorter<>(model);
    sectionsTable.setRowSorter(sorter);

    return new JScrollPane(sectionsTable);
  }

  private JPanel createFormPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Add / Edit Section"));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    // Course
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Course:"), gbc);
    gbc.gridx = 1;
    courseSelector = new JComboBox<>();
    panel.add(courseSelector, gbc);

    // Instructor
    gbc.gridx = 2;
    panel.add(new JLabel("Instructor:"), gbc);
    gbc.gridx = 3;
    instructorSelector = new JComboBox<>();
    panel.add(instructorSelector, gbc);

    // Day/time
    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("Day/Time:"), gbc);
    gbc.gridx = 1;
    dayTimeField = new JTextField(12);
    panel.add(dayTimeField, gbc);

    // Room
    gbc.gridx = 2;
    panel.add(new JLabel("Room:"), gbc);
    gbc.gridx = 3;
    roomField = new JTextField(10);
    panel.add(roomField, gbc);

    // Semester
    gbc.gridx = 0;
    gbc.gridy = 2;
    panel.add(new JLabel("Semester:"), gbc);
    gbc.gridx = 1;
    semesterField = new JTextField(10);
    panel.add(semesterField, gbc);

    // Year
    gbc.gridx = 2;
    panel.add(new JLabel("Year:"), gbc);
    gbc.gridx = 3;
    yearField = new JTextField(5);
    panel.add(yearField, gbc);

    // Capacity
    gbc.gridx = 4;
    panel.add(new JLabel("Capacity:"), gbc);
    gbc.gridx = 5;
    capacitySpinner = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
    panel.add(capacitySpinner, gbc);

    // Buttons
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 6;
    gbc.anchor = GridBagConstraints.EAST;
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    saveSectionButton = new JButton("Save");
    clearFormButton = new JButton("Clear");
    deleteButton = new JButton("Delete");

    buttonPanel.add(saveSectionButton);
    buttonPanel.add(clearFormButton);
    buttonPanel.add(deleteButton);

    panel.add(buttonPanel, gbc);

    return panel;
  }

  private void filter() {
    String text = searchField.getText();
    String selected = (String) filterColumnSelector.getSelectedItem();
    int colIndex = -1;

    switch (selected) {
      case "Section ID":
        colIndex = 0;
        break;
      case "Course Code":
        colIndex = 1;
        break;
      case "Title":
        colIndex = 2;
        break;
      case "Instructor":
        colIndex = 3;
        break;
      case "Semester":
        colIndex = 7;
        break;
      case "Year":
        colIndex = 8;
        break;
      case "All":
      default:
        colIndex = -1;
        break;
    }

    if (text.trim().length() == 0) {
      sorter.setRowFilter(null);
    } else {
      if (colIndex == -1) {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
      } else {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, colIndex));
      }
    }
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

    sectionsTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (e.getValueIsAdjusting()) return;

              int viewRow = sectionsTable.getSelectedRow();
              if (viewRow == -1) return;

              int row = sectionsTable.convertRowIndexToModel(viewRow);

              selectedSectionId = model.getValueAt(row, 0).toString();

              String course = model.getValueAt(row, 1).toString();
              String instructor = model.getValueAt(row, 3).toString();

              courseSelector.setSelectedItem(course);
              instructorSelector.setSelectedItem(instructor);

              dayTimeField.setText(model.getValueAt(row, 4).toString());
              roomField.setText(model.getValueAt(row, 5).toString());

              Object cap = model.getValueAt(row, 6);
              capacitySpinner.setValue(Integer.parseInt(cap.toString()));

              semesterField.setText(model.getValueAt(row, 7).toString());
              yearField.setText(model.getValueAt(row, 8).toString());

              courseSelector.setEnabled(false);
            });

    clearFormButton.addActionListener(e -> clearForm());
    saveSectionButton.addActionListener(e -> saveSection());
    deleteButton.addActionListener(e -> deleteSection());
  }

  private void loadCourses() {
    courseSelector.removeAllItems();
    ApiResponse<ArrayList<String>> response = catalogApi.listCourseCodes();
    if (response.isSuccess()) {
      for (String code : response.getData()) {
        courseSelector.addItem(code);
      }
    }
  }

  private void loadInstructors() {
    instructorSelector.removeAllItems();
    ApiResponse<ArrayList<String>> response = catalogApi.listInstructorUsernames();
    if (response.isSuccess()) {
      for (String instructor : response.getData()) {
        instructorSelector.addItem(instructor);
      }
    }
  }

  private void refreshSections() {
    model.setRowCount(0);
    ApiResponse<ArrayList<ArrayList<String>>> response = catalogApi.listSections();
    if (!response.isSuccess()) {
      String errorMessage = "Failed to load data: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error(errorMessage);
      return;
    }
    for (ArrayList<String> row : response.getData()) {
      model.addRow(row.toArray());
    }
  }

  public void refreshDropdowns() {
    loadCourses();
    loadInstructors();
  }

  private void saveSection() {
    String course = (String) courseSelector.getSelectedItem();
    String instructor = (String) instructorSelector.getSelectedItem();
    String dayTime = dayTimeField.getText().trim();
    String room = roomField.getText().trim();
    String sem = semesterField.getText().trim();
    String year = yearField.getText().trim();
    int cap = (Integer) capacitySpinner.getValue();

    // --- VALIDATION ---
    if (course == null
        || instructor == null
        || dayTime.isEmpty()
        || room.isEmpty()
        || sem.isEmpty()
        || year.isEmpty()) {
      Toast.show(this, "All fields are required.");
      log.warn("Section save attempt with empty fields.");
      return;
    }

    if (cap <= 0) {
      Toast.show(this, "Capacity must be a positive number.");
      log.warn("Section save attempt with non-positive capacity: {}", cap);
      return;
    }

    int yearInt;
    try {
      yearInt = Integer.parseInt(year);
      if (yearInt < 2000 || yearInt > 2100) {
        Toast.show(this, "Please enter a valid year (e.g., 2025).");
        log.warn("Section save attempt with invalid year: {}", year);
        return;
      }
    } catch (NumberFormatException e) {
      Toast.show(this, "Year must be a valid number.");
      log.warn("Section save attempt with invalid year format: {}", year);
      return;
    }

    ApiResponse<String> response;

    if (selectedSectionId == null) {
      log.info("Attempting to create new section for course: {}", course);
      response = adminApi.addsection(course, instructor, dayTime, room, cap, sem, yearInt);
    } else {
      log.info("Attempting to update section: {}", selectedSectionId);
      response =
          adminApi.update_section(
              Integer.parseInt(selectedSectionId),
              course,
              instructor,
              dayTime,
              room,
              cap,
              sem,
              yearInt);
    }

    if (response.isSuccess()) {
      Toast.show(this, "Section saved.");
      log.info("Section {} saved successfully.", selectedSectionId);
      refreshSections();
      clearForm();
    } else {
      String errorMessage = "Error: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error(
          "Failed to save section {}. Reason: {}", selectedSectionId, response.getMessage());
    }
  }

  private void deleteSection() {
    if (selectedSectionId == null) {
      Toast.show(this, "Select a section first.");
      return;
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Delete section " + selectedSectionId + "?",
            "Confirm",
            JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) return;

    log.info("Attempting to delete section: {}", selectedSectionId);
    ApiResponse<String> response = adminApi.delete_section(Integer.parseInt(selectedSectionId));

    if (response.isSuccess()) {
      Toast.show(this, "Section deleted.");
      log.info("Section {} deleted successfully.", selectedSectionId);
      refreshSections();
      clearForm();
    } else {
      String errorMessage = "Error: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error(
          "Failed to delete section {}. Reason: {}", selectedSectionId, response.getMessage());
    }
  }

  public void clearForm() {
    selectedSectionId = null;
    courseSelector.setEnabled(true);
    if (courseSelector.getItemCount() > 0) courseSelector.setSelectedIndex(0);
    if (instructorSelector.getItemCount() > 0) instructorSelector.setSelectedIndex(0);
    dayTimeField.setText("");
    roomField.setText("");
    semesterField.setText("");
    yearField.setText("");
    capacitySpinner.setValue(50);
    sectionsTable.clearSelection();
  }
}