package edu.univ.erp.UI.student;

import edu.univ.erp.UI.MainFrame;
import edu.univ.erp.UI.common.DeadlineBannerPanel;
import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.student.StudentApi;
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

public class CourseRegistrationPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(CourseRegistrationPanel.class);

  private JTextField searchField;
  private JTable courseCatalogTable;
  private DefaultTableModel catalogModel;
  private JButton registerButton;
  private JTable mySectionsTable;
  private DefaultTableModel mySectionsModel;
  private JButton dropButton;
  private String username;
  private DeadlineBannerPanel deadlineBanner;
  private JComboBox<String> filterColumnSelector;
  private TableRowSorter<DefaultTableModel> catalogSorter;

  // --- APIs ---
  private CatalogApi catalogApi;
  private StudentApi studentApi;

  public CourseRegistrationPanel(String username, DeadlineBannerPanel deadlineBanner) {
    // 1. Initialize APIs
    this.catalogApi = new CatalogApi();
    this.studentApi = new StudentApi();
    this.username = username;
    this.deadlineBanner = deadlineBanner;
    setLayout(new BorderLayout());

    JPanel topPanel = createTopPanel();
    JPanel bottomPanel = createBottomPanel();

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
    splitPane.setResizeWeight(0.6);
    add(splitPane, BorderLayout.CENTER);

    // 2. Load Data immediately
    this.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            refreshCatalog();
            refreshMySections();
            MainFrame.updateDeadlineBanner(deadlineBanner);
          }
        });
  }

  private JPanel createTopPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel title = new JLabel("Course Catalog:");
    title.setFont(new Font("SansSerif", Font.BOLD, 18));
    panel.add(title, BorderLayout.NORTH);

    JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    filterPanel.add(new JLabel("Search:"));
    searchField = new JTextField(15);
    filterPanel.add(searchField);

    filterPanel.add(new JLabel("Filter By:"));
    String[] filterOptions = {
      "All",
      "Section ID",
      "Code",
      "Title",
      "Instructor",
      "Day/Time",
      "Room",
      "Capacity",
      "Sem",
      "Year"
    };
    filterColumnSelector = new JComboBox<>(filterOptions);
    filterPanel.add(filterColumnSelector);

    panel.add(filterPanel, BorderLayout.NORTH);
    JPanel headerWrapper = new JPanel(new BorderLayout());
    headerWrapper.add(title, BorderLayout.NORTH);
    headerWrapper.add(filterPanel, BorderLayout.SOUTH);
    panel.add(headerWrapper, BorderLayout.NORTH);

    // --- Create Table Model ---

    String[] columnNames = {
      "Section ID",
      "Code",
      "Title",
      "Instructor",
      "Day/Time",
      "Room",
      "Current Capacity",
      "Capacity",
      "Sem",
      "Year"
    };
    catalogModel =
        new DefaultTableModel(columnNames, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false; // Make table read-only
          }
        };

    courseCatalogTable = new JTable(catalogModel);
    courseCatalogTable.getTableHeader().setReorderingAllowed(false);

    // --- Set up Sorter and Filter ---
    catalogSorter = new TableRowSorter<>(catalogModel);
    courseCatalogTable.setRowSorter(catalogSorter);

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

    // Listener for Column Selection
    filterColumnSelector.addActionListener(e -> filter());

    JScrollPane scrollPane = new JScrollPane(courseCatalogTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    registerButton = new JButton("Register");
    buttonPanel2.add(registerButton);
    panel.add(buttonPanel2, BorderLayout.SOUTH);
    registerButton.addActionListener(e -> registerForSelectedSection());

    return panel;
  }

  private JPanel createBottomPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel title = new JLabel("My Registered Courses:");
    title.setFont(new Font("SansSerif", Font.BOLD, 18));
    panel.add(title, BorderLayout.NORTH);

    String[] columnNames = {"Section ID", "Code", "Title", "Day/Time", "Room"};

    mySectionsModel =
        new DefaultTableModel(columnNames, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false; // Make table read-only
          }
        };
    mySectionsTable = new JTable(mySectionsModel);
    mySectionsTable.getTableHeader().setReorderingAllowed(false);

    JScrollPane scrollPane = new JScrollPane(mySectionsTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    dropButton = new JButton("Drop");
    buttonPanel.add(dropButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    dropButton.addActionListener(e -> dropSelectedSection());

    return panel;
  }

  private void dropSelectedSection() {
    int row = mySectionsTable.getSelectedRow();
    if (row == -1) {
      Toast.show(this, "Select a section to drop.");
      return;
    }

    String sectionId = (String) mySectionsModel.getValueAt(row, 0);
    log.info("User {} attempting to drop section {}", username, sectionId);
    ApiResponse<String> res = studentApi.drop(username, Integer.parseInt(sectionId));

    if (res.isSuccess()) {
      Toast.show(this, "Section dropped successfully.");
      log.info("User {} successfully dropped section {}", username, sectionId);
      refreshMySections();
      refreshCatalog();
    } else {
      Toast.show(this, res.getMessage());
      log.error(
          "User {} failed to drop section {}. Reason: {}",
          username,
          sectionId,
          res.getMessage());
    }
  }

  /** Fetches data from CatalogApi and fills the table. */
  public void refreshCatalog() {
    // Clear existing rows
    catalogModel.setRowCount(0);

    // Get the current active semester from the admin settings
    ApiResponse<String> currentSemesterRes = new edu.univ.erp.api.admin.AdminApi().getCurrentSemester();
    if (!currentSemesterRes.isSuccess() || currentSemesterRes.getData() == null) {
      Toast.show(this, "Registration is currently closed (no active semester).");
      log.warn("Could not determine current semester for registration.");
      return;
    }
    String currentSemesterStr = currentSemesterRes.getData(); // e.g., "Fall-2024"
    String[] parts = currentSemesterStr.split("-");
    String currentSemesterName = parts[0];
    String currentSemesterYear = parts[1];

    // Call API to get all sections
    ApiResponse<ArrayList<ArrayList<String>>> response = catalogApi.listSections();

    if (response.isSuccess()) {
      ArrayList<ArrayList<String>> data = response.getData();
      for (ArrayList<String> rowData : data) {
        String sectionSemester = rowData.get(7);
        String sectionYear = rowData.get(8);

        // ONLY add the section to the table if it matches the current active semester
        if (sectionSemester.equals(currentSemesterName) && sectionYear.equals(currentSemesterYear)) {
          catalogModel.addRow(
              new Object[] {
                rowData.get(0), rowData.get(1), rowData.get(2), rowData.get(3),
                rowData.get(4), rowData.get(5),
                String.valueOf(catalogApi.capacity(Integer.parseInt(rowData.get(0))).getData()),
                rowData.get(6), rowData.get(7), rowData.get(8)
              });
        }
      }
    } else {
      String errorMessage = "Failed to load catalog: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error(errorMessage);
    }
  }

  private void filter() {
    String text = searchField.getText();
    int columnIndex = filterColumnSelector.getSelectedIndex();

    if (text.trim().length() == 0) {
      catalogSorter.setRowFilter(null);
    } else {
      if (columnIndex == 0) {
        // Search ALL columns
        catalogSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
      } else {
        catalogSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex - 1));
      }
    }
  }

  public void refreshMySections() {
    mySectionsModel.setRowCount(0); // Clear

    ApiResponse<ArrayList<ArrayList<String>>> response =
        catalogApi.listStudentsSections(username);

    if (response.isSuccess()) {
      for (ArrayList<String> row : response.getData()) {
        mySectionsModel.addRow(row.toArray());
      }
    } else {
      String errorMessage = "Failed to load your sections: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error("Failed to load sections for user {}. Reason: {}", username, response.getMessage());
    }
  }

  /** Handles the register button click. */
  private void registerForSelectedSection() {
    int selectedRow = courseCatalogTable.getSelectedRow();
    if (selectedRow == -1) {
      Toast.show(this, "Please select a section to register.");
      return;
    }

    // Get Section ID (Column 0)
    String sectionIdStr = (String) catalogModel.getValueAt(selectedRow, 0);
    String courseCode = (String) catalogModel.getValueAt(selectedRow, 1);
    int capacity = Integer.parseInt((String) catalogModel.getValueAt(selectedRow, 7));
    int currentCapacity = Integer.parseInt((String) catalogModel.getValueAt(selectedRow, 6));

    String studentId = this.username;

    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Register for " + courseCode + "?",
            "Confirm Registration",
            JOptionPane.YES_NO_OPTION);

    if (choice == JOptionPane.YES_OPTION) {
      log.info("User {} attempting to register for section {}", username, sectionIdStr);
      // Call StudentApi to register (Logic to be implemented)
      if (currentCapacity >= capacity) {
        Toast.show(this, "Cannot register: Section is full.");
        log.warn(
            "User {} failed to register for section {}. Reason: Section is full.",
            username,
            sectionIdStr);
        return;
      }
      ApiResponse<String> res = studentApi.enrollStudent(studentId, Integer.parseInt(sectionIdStr));
      if (res.isSuccess()) {
        Toast.show(this, "Registered successfully.");
        log.info("User {} successfully registered for section {}", username, sectionIdStr);
        refreshCatalog();
        refreshMySections(); // NEW
      } else {
        Toast.show(this, res.getMessage());
        log.error(
            "User {} failed to register for section {}. Reason: {}",
            username,
            sectionIdStr,
            res.getMessage());
      }
    }
  }

  // --- Public Getters ---
  public JTextField getSearchField() {
    return searchField;
  }

  public JTable getCourseCatalogTable() {
    return courseCatalogTable;
  }

  public JButton getRegisterButton() {
    return registerButton;
  }

  public JTable getMySectionsTable() {
    return mySectionsTable;
  }

  public JButton getDropButton() {
    return dropButton;
  }
}