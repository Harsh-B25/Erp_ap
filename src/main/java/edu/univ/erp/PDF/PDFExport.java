package edu.univ.erp.PDF;

import edu.univ.erp.UI.common.Toast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class PDFExport extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(PDFExport.class);

  private JButton generatePdfButton;
  private JButton saveAsButton;
  private JButton openPdfButton;

  private String lastGeneratedPdfPath = null;

  public PDFExport() {
    setLayout(new BorderLayout(10, 10));

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

    generatePdfButton = new JButton("Generate PDF");
    saveAsButton = new JButton("Save As...");
    openPdfButton = new JButton("Open PDF");

    topPanel.add(generatePdfButton);
    topPanel.add(saveAsButton);
    topPanel.add(openPdfButton);

    add(topPanel, BorderLayout.NORTH);

    addListeners();
  }

  private void addListeners() {

    // -------------------------------------------------
    // 1️⃣ GENERATE PDF
    // -------------------------------------------------
    generatePdfButton.addActionListener(
        (ActionEvent e) -> {
          try {
            lastGeneratedPdfPath = "users_report.pdf";

            generatePDF(lastGeneratedPdfPath);

            Toast.show(this, "PDF generated successfully:\n" + lastGeneratedPdfPath);
            log.info("PDF generated at: {}", lastGeneratedPdfPath);

          } catch (Exception ex) {
            log.error("Error generating PDF", ex);
            Toast.show(this, "Error: " + ex.getMessage());
          }
        });

    // -------------------------------------------------
    // 2️⃣ SAVE AS…
    // -------------------------------------------------
    saveAsButton.addActionListener(
        e -> {
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle("Save PDF As");
          chooser.setSelectedFile(new File("users_report.pdf"));

          int option = chooser.showSaveDialog(this);

          if (option == JFileChooser.APPROVE_OPTION) {

            File selected = chooser.getSelectedFile();

            try {
              generatePDF(selected.getAbsolutePath());
              lastGeneratedPdfPath = selected.getAbsolutePath();

              Toast.show(this, "PDF saved successfully:\n" + lastGeneratedPdfPath);
              log.info("PDF saved at: {}", lastGeneratedPdfPath);

            } catch (Exception ex) {
              log.error("Error saving PDF", ex);
              Toast.show(this, "Error saving PDF:\n" + ex.getMessage());
            }
          }
        });

    // -------------------------------------------------
    // 3️⃣ OPEN PDF (in system viewer)
    // -------------------------------------------------
    openPdfButton.addActionListener(
        e -> {
          if (lastGeneratedPdfPath == null) {
            Toast.show(this, "No PDF generated yet!");
            return;
          }

          try {
            Desktop.getDesktop().open(new File(lastGeneratedPdfPath));
          } catch (Exception ex) {
            log.error("Cannot open PDF", ex);
            Toast.show(this, "Cannot open PDF: " + ex.getMessage());
          }
        });
  }

  // -----------------------------------------------------------------
  //  PDF GENERATION
  // -----------------------------------------------------------------
  private void generatePDF(String outputPath) throws Exception {

    String url = "jdbc:mysql://localhost:3306/auth_db";
    String user = "root";
    String password = "CPS@root009";

    com.lowagie.text.Document document = new com.lowagie.text.Document();
    com.lowagie.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(outputPath));
    document.open();

    com.lowagie.text.Font titleFont =
        com.lowagie.text.FontFactory.getFont(
            com.lowagie.text.FontFactory.HELVETICA_BOLD, 18, java.awt.Color.BLUE);

    com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("Users Report", titleFont);

    title.setAlignment(com.lowagie.text.Paragraph.ALIGN_CENTER);
    document.add(title);
    document.add(new com.lowagie.text.Paragraph("Generated on: " + new java.util.Date()));
    document.add(new com.lowagie.text.Paragraph(" "));

    com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
    table.setWidthPercentage(100);

    addHeader(table, "ID");
    addHeader(table, "Username");
    addHeader(table, "Role");
    addHeader(table, "Last Login");

    Class.forName("com.mysql.cj.jdbc.Driver");
    Connection conn = DriverManager.getConnection(url, user, password);

    ResultSet rs =
        conn.createStatement()
            .executeQuery("SELECT user_id, username, role, last_login FROM users_auth");

    while (rs.next()) {
      table.addCell(String.valueOf(rs.getInt("user_id")));
      table.addCell(rs.getString("username"));
      table.addCell(rs.getString("role"));
      table.addCell(String.valueOf(rs.getTimestamp("last_login")));
    }

    document.add(table);
    document.close();
    conn.close();
  }

  private void addHeader(com.lowagie.text.pdf.PdfPTable table, String text) {

    com.lowagie.text.Font headerFont =
        com.lowagie.text.FontFactory.getFont(
            com.lowagie.text.FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);

    com.lowagie.text.pdf.PdfPCell cell =
        new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Paragraph(text, headerFont));

    cell.setBackgroundColor(java.awt.Color.DARK_GRAY);
    cell.setHorizontalAlignment(com.lowagie.text.pdf.PdfPCell.ALIGN_CENTER);

    table.addCell(cell);
  }
}