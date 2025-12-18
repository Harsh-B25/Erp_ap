package edu.univ.erp.UI.student;

import edu.univ.erp.UI.common.DeadlineBannerPanel;
import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.common.ApiResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
public class ViewCoursesPanel extends JPanel {

    private JTable coursesTable;
    private DefaultTableModel tableModel;
    
    // --- Data ---
    private String username;
    private CatalogApi catalogApi;
    private DeadlineBannerPanel deadlineBanner;

    public ViewCoursesPanel(String username , DeadlineBannerPanel deadlineBanner) {
        this.username = username;
        this.deadlineBanner = deadlineBanner;
        this.catalogApi = new CatalogApi();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 1. Title
        JLabel titleLabel = new JLabel("My Registered Courses:");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);
        
        // 2. Table Setup
        // Columns match erp.listStudentSections2D (7 columns)
        String[] columnNames = {"Section ID", "Code", "Title", "Day/Time", "Room", "Sem", "Year"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        coursesTable = new JTable(tableModel); 
        
        // Fix columns in place
        coursesTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // 3. Load Data
        // refreshTable();
        deadlineBanner.setVisible(false); // Hide by default
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshTable();
                deadlineBanner.setVisible(false); // Hide by default
            }
        });
    }

    public void refreshTable() {
        tableModel.setRowCount(0); // Clear existing rows

        // Call API to get sections for this specific student
        ApiResponse<ArrayList<ArrayList<String>>> response = catalogApi.listStudentsSections(username);

        if (response.isSuccess()) {
            for (ArrayList<String> row : response.getData()) {
                tableModel.addRow(row.toArray());
            }
        } else {
             System.err.println("Could not load courses: " + response.getMessage());
        }
    }


}