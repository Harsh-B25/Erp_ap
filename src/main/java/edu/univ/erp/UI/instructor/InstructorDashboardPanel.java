package edu.univ.erp.UI.instructor;

import edu.univ.erp.UI.common.Toast;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.instructor.InstructorApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class InstructorDashboardPanel extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(InstructorDashboardPanel.class);

  private JLabel welcomeLabel;
  private JTable mySectionsTable;
  private DefaultTableModel tableModel;
  private TableRowSorter<DefaultTableModel> sorter;
  private JButton manageSectionButton;

  // Sorting UI
  private JComboBox<String> sortColumnSelector;
  private JButton sortButton;
  private JButton notifyButton;

  private String username;
  private InstructorApi instructorApi;

  public InstructorDashboardPanel(String username) {
    this.username = username;
    this.instructorApi = new InstructorApi();

    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // --- TOP: Welcome & Sort Controls ---
    JPanel topPanel = new JPanel(new BorderLayout());

    welcomeLabel = new JLabel("Welcome, " + username + "!");
    welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
    welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    topPanel.add(welcomeLabel, BorderLayout.NORTH);

    notifyButton = new JButton("Notifications");
    topPanel.add(notifyButton, BorderLayout.WEST);

    // Sort Panel
    JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    sortPanel.add(new JLabel("Sort by:"));

    // Columns available for sorting
    // Indices: 0=ID, 1=Code, 2=Title, 3=Day/Time, 4=Room, 5=Capacity, 6=Sem, 7=Year
    String[] sortOptions = {"Section ID", "Course Code", "Course Title", "Capacity", "Year"};
    sortColumnSelector = new JComboBox<>(sortOptions);
    sortPanel.add(sortColumnSelector);

    sortButton = new JButton("Sort");
    sortButton.addActionListener(e -> applySort());
    sortPanel.add(sortButton);

    topPanel.add(sortPanel, BorderLayout.SOUTH);

    add(topPanel, BorderLayout.NORTH);

    // --- CENTER: Table ---
    JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
    centerPanel.add(new JLabel("My Sections:"), BorderLayout.NORTH);

    String[] columnNames = {
      "Section ID", "Code", "Title", "Day/Time", "Room", "Capacity", "Sem", "Year"
    };

    tableModel =
        new DefaultTableModel(columnNames, 0) {
          @Override
          public boolean isCellEditable(int row, int col) {
            return false;
          }

          // Override getColumnClass to ensure numbers sort numerically, not alphabetically
          @Override
          public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 5 || columnIndex == 7) { // ID, Cap, Year
              return Integer.class;
            }
            return String.class;
          }
        };

    mySectionsTable = new JTable(tableModel);
    mySectionsTable.getTableHeader().setReorderingAllowed(false);

    // Setup Sorter
    sorter = new TableRowSorter<>(tableModel);
    mySectionsTable.setRowSorter(sorter);

    centerPanel.add(new JScrollPane(mySectionsTable), BorderLayout.CENTER);
    add(centerPanel, BorderLayout.CENTER);

    // --- BOTTOM: Button ---
    manageSectionButton = new JButton("Manage Section");
    add(manageSectionButton, BorderLayout.SOUTH);

    // --- AUTO-REFRESH ---
    refreshTable();

    this.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            refreshTable();
          }
        });
  }

  public void refreshTable() {
    tableModel.setRowCount(0);
    ApiResponse<ArrayList<ArrayList<String>>> response =
        instructorApi.listsectionsbyinstructor(username);

    if (response.isSuccess()) {
      for (ArrayList<String> row : response.getData()) {
        // We must convert numeric strings to Integers for correct sorting
        Object[] rowData = new Object[row.size()];
        for (int i = 0; i < row.size(); i++) {
          String val = row.get(i);
          // Columns 0 (ID), 5 (Capacity), 7 (Year) are integers
          if (i == 0 || i == 5 || i == 7) {
            try {
              rowData[i] = Integer.parseInt(val);
            } catch (NumberFormatException e) {
              rowData[i] = val; // Fallback
            }
          } else {
            rowData[i] = val;
          }
        }
        tableModel.addRow(rowData);
      }
    } else {
      String errorMessage = "Error loading sections: " + response.getMessage();
      Toast.show(this, errorMessage);
      log.error(
          "Failed to load sections for instructor {}. Reason: {}",
          username,
          response.getMessage());
    }
  }

  private void applySort() {
    String selected = (String) sortColumnSelector.getSelectedItem();
    if (selected == null) return;

    int colIndex = 0;
    switch (selected) {
      case "Section ID":
        colIndex = 0;
        break;
      case "Course Code":
        colIndex = 1;
        break;
      case "Course Title":
        colIndex = 2;
        break;
      case "Capacity":
        colIndex = 5;
        break;
      case "Year":
        colIndex = 7;
        break;
    }

    // Toggle order or default to Ascending Order
    ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
    keys.add(new RowSorter.SortKey(colIndex, SortOrder.ASCENDING));
    sorter.setSortKeys(keys);
    sorter.sort();
  }

  // --- Public Getters ---
  public JLabel getWelcomeLabel() {
    return welcomeLabel;
  }

  public JTable getMySectionsTable() {
    return mySectionsTable;
  }

  public JButton getManageSectionButton() {
    return manageSectionButton;
  }

  public JButton getNotifyButton() {
    return notifyButton;
  }
}