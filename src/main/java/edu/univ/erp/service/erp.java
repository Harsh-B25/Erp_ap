package edu.univ.erp.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class erp {

  private static final String URL_AUTH = "jdbc:mysql://localhost:3306/auth_db";
  private static final String URL_ERP = "jdbc:mysql://localhost:3306/erp";
  private static final String USER = "root";
  private static final String PASSWORD = "harsh@102";
  private static final String host = "localhost";
  private static final String dbName = "erp";
  private static final String sqlFilePath = "src/main/java/edu/univ/erp/backup/backup.sql";

  // Hashes a plain text password using BCrypt with a salt round of 10.
  public static String hashPassword(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
  }

  // Updates an existing grade record or inserts a new one if it doesn't exist.
  public static boolean updateGrade(int enrollmentId, String component, float score, float totalScore, float weightage) {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement stmt = conn.prepareStatement(
          "UPDATE grades SET score=?, total_score=?, weightage=? " +
              "WHERE enrollment_id=? AND component=?"
      );

      stmt.setFloat(1, score);
      stmt.setFloat(2, totalScore);
      stmt.setFloat(3, weightage);
      stmt.setInt(4, enrollmentId);
      stmt.setString(5, component);

      int rows = stmt.executeUpdate();
      System.out.println("Updated rows: " + rows);
      if(rows == 0) {
        // No existing record, insert new
        boolean e = addGrade(enrollmentId, component, score, totalScore, weightage);
        return e ;
      }
      return rows > 0;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  // Checks if a student is already enrolled in any section of the target course to prevent duplicate course enrollment.
  public static boolean isEnrolledInCourse(String studentUsername, int newSectionId) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // 1. Find the course_id for the new section
      PreparedStatement courseStmt = conn.prepareStatement(
          "SELECT course_id FROM sections WHERE section_id = ?"
      );
      courseStmt.setInt(1, newSectionId);
      ResultSet rsCourse = courseStmt.executeQuery();

      if (!rsCourse.next()) {
        throw new Exception("Section not found.");
      }
      int targetCourseId = rsCourse.getInt("course_id");

      // 2. Find the student_id
      PreparedStatement studentStmt = conn.prepareStatement(
          "SELECT s.student_id FROM students s " +
              "JOIN auth_db.users_auth u ON s.user_id = u.user_id " +
              "WHERE u.username = ?"
      );
      studentStmt.setString(1, studentUsername);
      ResultSet rsStudent = studentStmt.executeQuery();

      if (!rsStudent.next()) {
        throw new Exception("Student not found.");
      }
      int studentId = rsStudent.getInt("student_id");

      // 3. Check if student is already enrolled in ANY section of this course
      String checkSql =
          "SELECT count(*) FROM enrollments e " +
              "JOIN sections s ON e.section_id = s.section_id " +
              "WHERE e.student_id = ? AND s.course_id = ?";

      PreparedStatement checkStmt = conn.prepareStatement(checkSql);
      checkStmt.setInt(1, studentId);
      checkStmt.setInt(2, targetCourseId);

      ResultSet rsCheck = checkStmt.executeQuery();
      if (rsCheck.next()) {
        return rsCheck.getInt(1) > 0;
      }

      return false;

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Generates a PDF report for a specific section's gradebook using OpenPDF.
  public static void generatePDF(
      String filepath,
      String sectionId,
      String instructorName,
      TableModel model
  ) throws Exception {

    Document document = new Document(PageSize.A4.rotate());
    PdfWriter.getInstance(document, new FileOutputStream(filepath));
    document.open();

    // HEADER
    Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, Color.BLUE);
    Font smallFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);

    Paragraph institute = new Paragraph("Indraprastha Institute of Information Technology", titleFont);
    institute.setAlignment(Element.ALIGN_CENTER);
    document.add(institute);

    document.add(new Paragraph("Section Grade Report", titleFont));
    document.add(new Paragraph(" "));

    document.add(new Paragraph("Section ID : " + sectionId, smallFont));
    document.add(new Paragraph("Instructor : " + instructorName, smallFont));
    document.add(new Paragraph("Date       : " + new java.util.Date(), smallFont));
    document.add(new Paragraph(" "));

    // TABLE
    PdfPTable table = new PdfPTable(model.getColumnCount());
    table.setWidthPercentage(100);

    Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    Font cellFont = new Font(Font.HELVETICA, 11);

    // Add headers
    for (int c = 0; c < model.getColumnCount(); c++) {
      PdfPCell cell = new PdfPCell(new Phrase(model.getColumnName(c), headerFont));
      cell.setBackgroundColor(Color.DARK_GRAY);
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      cell.setPadding(5);
      table.addCell(cell);
    }

    // Add rows
    double quizSum = 0, midSum = 0, finalSum = 0;
    int quizCount = 0, midCount = 0, finalCount = 0;

    int total_score = 0 ;
    int total_count =0 ;

    for (int r = 0; r < model.getRowCount(); r++) {
      for (int c = 0; c < model.getColumnCount(); c++) {
        Object val = model.getValueAt(r, c);
        PdfPCell cell = new PdfPCell(new Phrase(val == null ? "" : val.toString(), cellFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        table.addCell(cell);
      }

      // Collect averages
      try {
        double q = Double.parseDouble(model.getValueAt(r, 2).toString());
        quizSum += q;
        quizCount++;
      } catch (Exception ignored) {}

      try {
        double m = Double.parseDouble(model.getValueAt(r, 5).toString());
        midSum += m;
        midCount++;
      } catch (Exception ignored) {}

      try {
        double f = Double.parseDouble(model.getValueAt(r, 8).toString());
        finalSum += f;
        finalCount++;
      } catch (Exception ignored) {}

      try {
        double finalGrade = 0;
        try{
          finalGrade += computeOne(r, 2, 3, 4 , model);
          finalGrade += computeOne(r, 5, 6, 7, model);
          finalGrade += computeOne(r, 8, 9, 10, model);
          total_score += finalGrade ;
          total_count++ ;
        } catch (Exception e){
          throw new Exception(e.getMessage());
        }

      } catch (Exception ignored) {

      }

    }

    document.add(table);
    document.add(new Paragraph(" "));

    // FOOTER STATISTICS
    Font statsFont = new Font(Font.HELVETICA, 13, Font.BOLD, Color.BLACK);

    document.add(new Paragraph("Average Scores:", statsFont));

    document.add(new Paragraph(
        "AVG Quiz: " + (quizCount == 0 ? "N/A" : String.format("%.2f", quizSum / quizCount)),
        smallFont));

    document.add(new Paragraph(
        "AVG Midterm: " + (midCount == 0 ? "N/A" : String.format("%.2f", midSum / midCount)),
        smallFont));

    document.add(new Paragraph(
        "AVG Final: " + (finalCount == 0 ? "N/A" : String.format("%.2f", finalSum / finalCount)),
        smallFont));
    document.add(new Paragraph(
        "AVG Final Grade: " + (total_count == 0 ? "N/A" : String.format("%.2f", (double)total_score / total_count)),
        smallFont));

    document.close();

  }

  // Helper method to compute a single weighted component score from table model data.
  public static double computeOne(int r, int sIdx, int tIdx, int wIdx , TableModel model) throws Exception {
    String s = val(r, sIdx , model);
    String t = val(r, tIdx, model);
    String w = val(r, wIdx, model);

    if (s.isEmpty() || t.isEmpty() || w.isEmpty())  throw new Exception("Incomplete data");

    return (Double.parseDouble(s) / Double.parseDouble(t)) * Double.parseDouble(w);
  }

  // Helper method to safely extract string values from the table model.
  public static String val(int r, int c , TableModel model) {
    Object o = model.getValueAt(r, c);
    return (o == null) ? "" : o.toString().trim();
  }


  // Retrieves a list of semesters (e.g., "Fall-2024") where a student has enrollments.
  public static ArrayList<String> listSemestersForStudent(String username) throws Exception {
    ArrayList<String> semesters = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT DISTINCT s.semester, s.year " +
              "FROM enrollments e " +
              "JOIN sections s ON e.section_id = s.section_id " +
              "JOIN students st ON e.student_id = st.student_id " +
              "JOIN auth_db.users_auth u ON st.user_id = u.user_id " +
              "WHERE u.username = ? " +
              "ORDER BY s.year DESC, s.semester ASC";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, username);

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        semesters.add(rs.getString("semester") + "-" + rs.getInt("year"));
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return semesters;
  }

  // Fetches the complete gradebook for a student for a specific semester, including component scores.
  public static ArrayList<ArrayList<String>> getStudentSemesterGrades(
      String username, String semester, int year) throws Exception {

    ArrayList<ArrayList<String>> finalTable = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // 1. Get all enrollments for this user for the given semester + year
      String sqlEnrollments =
          "SELECT e.enrollment_id, s.section_id, c.code, c.title, c.credits " +
              "FROM enrollments e " +
              "JOIN students st ON e.student_id = st.student_id " +
              "JOIN auth_db.users_auth u ON st.user_id = u.user_id " +
              "JOIN sections s ON e.section_id = s.section_id " +
              "JOIN courses c ON s.course_id = c.course_id " +
              "WHERE u.username = ? AND s.semester = ? AND s.year = ?";

      PreparedStatement stmt = conn.prepareStatement(sqlEnrollments);
      stmt.setString(1, username);
      stmt.setString(2, semester);
      stmt.setInt(3, year);

      ResultSet rs = stmt.executeQuery();

      // 2. For each enrollment -> fetch its 3 components
      while (rs.next()) {

        int enrollmentId = rs.getInt("enrollment_id");
        String courseCode = rs.getString("code");
        String title = rs.getString("title");
        int credits = rs.getInt("credits");

        // Default values if no rows exist
        String quizScore = "", quizTotal = "", quizWeight = "";
        String midScore = "", midTotal = "", midWeight = "";
        String finalScore = "", finalTotal = "", finalWeight = "";
        String computedFinal = "";

        float quizpercent = 0.0f ;
        float midpercent = 0.0f ;
        float finalpercent = 0.0f ;

        // 3. Query components for this enrollment
        String sqlGrades =
            "SELECT component, score, total_score, weightage " +
                "FROM grades WHERE enrollment_id = ?";

        PreparedStatement stmtGrades = conn.prepareStatement(sqlGrades);
        stmtGrades.setInt(1, enrollmentId);

        ResultSet rsG = stmtGrades.executeQuery();

        while (rsG.next()) {

          String comp = rsG.getString("component");

          switch (comp.toLowerCase()) {
            case "quiz":
              quizScore = rsG.getString("score");
              quizTotal = rsG.getString("total_score");
              quizWeight = rsG.getString("weightage");
              break;

            case "midterm":
              midScore = rsG.getString("score");
              midTotal = rsG.getString("total_score");
              midWeight = rsG.getString("weightage");
              break;

            case "final":
              finalScore = rsG.getString("score");
              finalTotal = rsG.getString("total_score");
              finalWeight = rsG.getString("weightage");
              break;
          }
        }

        // 4. Compute final grade (if possible)
        float weighted = 0.0f ;
        boolean hasQuiz = !quizScore.isEmpty();
        boolean hasMid = !midScore.isEmpty();
        boolean hasFinal = !finalScore.isEmpty();

        if (hasQuiz)

          quizpercent= (Float.parseFloat(quizScore) / Float.parseFloat(quizTotal))
              * Float.parseFloat(quizWeight);
        weighted += quizpercent ;



        if (hasMid)
          midpercent = (Float.parseFloat(midScore) / Float.parseFloat(midTotal))
              * Float.parseFloat(midWeight);
        weighted += midpercent ;

        if (hasFinal)
          finalpercent = (Float.parseFloat(finalScore) / Float.parseFloat(finalTotal))
              * Float.parseFloat(finalWeight);
        weighted += finalpercent ;


        if (hasQuiz && hasMid && hasFinal)
          computedFinal = String.format("%.2f", weighted);

        // 5. Build row
        ArrayList<String> row = new ArrayList<>();
        row.add(courseCode);   // 0
        row.add(title);        // 1
        row.add(String.valueOf(credits)); // 2
        row.add(String.format("%.2f", quizpercent)+ "%");    // 3
        row.add(String.format("%.2f", midpercent)+ "%");     // 4
        row.add(String.format("%.2f", finalpercent)+ "%");   // 5

        row.add(computedFinal); // 6 final weighted grade

        finalTable.add(row);

      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return finalTable;
  }


  // Inserts a new grade record into the grades table.
  public static boolean addGrade(int enrollmentId, String component, float score, float totalScore, float weightage) {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement stmt = conn.prepareStatement(
          "INSERT INTO grades (enrollment_id, component, score, total_score, weightage) " +
              "VALUES (?, ?, ?, ?, ?)"
      );

      stmt.setInt(1, enrollmentId);
      stmt.setString(2, component);
      stmt.setFloat(3, score);
      stmt.setFloat(4, totalScore);
      stmt.setFloat(5, weightage);

      return stmt.executeUpdate() > 0;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  // Calculates the final letter grade for an enrollment based on weighted component scores.
  public static String computeFinalGradeForEnrollment(int enrollmentId) throws Exception {

    float quizScore = -1, quizTotal = -1, quizWeight = -1;
    float midScore = -1, midTotal = -1, midWeight = -1;
    float finScore = -1, finTotal = -1, finWeight = -1;

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "SELECT component, score, total_score, weightage FROM grades WHERE enrollment_id=?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setInt(1, enrollmentId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        String comp = rs.getString("component").toUpperCase();
        float score = Float.parseFloat(rs.getString("score"));
        float total = Float.parseFloat(rs.getString("total_score"));
        float weight = Float.parseFloat(rs.getString("weightage"));

        switch (comp) {
          case "QUIZ":
            quizScore = score;
            quizTotal = total;
            quizWeight = weight;
            break;

          case "MIDTERM":
            midScore = score;
            midTotal = total;
            midWeight = weight;
            break;

          case "FINAL":
            finScore = score;
            finTotal = total;
            finWeight = weight;
            break;
        }
      }

      // REQUIREMENT: All 3 components must exist
      if (quizScore < 0 || midScore < 0 || finScore < 0)
        return "INCOMPLETE";

      // Compute weighted score
      float quizPct = (quizScore / quizTotal) * quizWeight;
      float midPct  = (midScore / midTotal) * midWeight;
      float finPct  = (finScore / finTotal) * finWeight;

      float totalPercentage = quizPct + midPct + finPct;

      // Convert to letter grade
      String letter;
      if (totalPercentage >= 90) letter = "A";
      else if (totalPercentage >= 80) letter = "B";
      else if (totalPercentage >= 70) letter = "C";
      else if (totalPercentage >= 60) letter = "D";
      else letter = "F";

      // Save final grade inside FINAL row
      PreparedStatement update = conn.prepareStatement(
          "UPDATE grades SET final_grade=? WHERE enrollment_id=? AND component='FINAL'"
      );
      update.setString(1, letter);
      update.setInt(2, enrollmentId);
      update.executeUpdate();

      return letter;

    } catch (Exception e) {
      throw new Exception("Error computing final grade: " + e.getMessage());
    }
  }

  // Retrieves a list of usernames for all students enrolled in a given section.
  public static ArrayList<String> getUsernamesInSection(int sectionId) throws Exception {

    ArrayList<String>table = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT e.enrollment_id, u.username, s.roll_no " +
              "FROM enrollments e " +
              "JOIN students s ON e.student_id = s.student_id " +
              "JOIN auth_db.users_auth u ON s.user_id = u.user_id " +
              "WHERE e.section_id = ?";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setInt(1, sectionId);

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        table.add(rs.getString("username"));

      }


    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return table;
  }

  // Generates a PDF transcript of a student's grades for a semester.
  public static void generateSemesterTranscriptPDF(
      String filepath,
      String username,
      String semester,
      int year,
      TableModel model ,
      int credits,
      String scgpa
  ) throws Exception {

    String studentName = username;


    Document document = new Document(PageSize.A4);
    PdfWriter.getInstance(document, new FileOutputStream(filepath));
    document.open();

    // Fonts
    Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
    Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    Font cellFont = new Font(Font.HELVETICA, 10);
    Font metaFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);

    // --- Header ---
    Paragraph inst = new Paragraph("Indraprastha Institute of Information Technology Delhi", titleFont);
    inst.setAlignment(Element.ALIGN_CENTER);
    document.add(inst);

    Paragraph reportTitle = new Paragraph("Semester Transcript", new Font(Font.HELVETICA, 14, Font.BOLD));
    reportTitle.setAlignment(Element.ALIGN_CENTER);
    reportTitle.setSpacingBefore(6f);
    reportTitle.setSpacingAfter(10f);
    document.add(reportTitle);

    // Student meta
    PdfPTable meta = new PdfPTable(new float[]{1, 1});
    meta.setWidthPercentage(100);

    PdfPCell left = new PdfPCell();
    left.setBorder(PdfPCell.NO_BORDER);
    left.addElement(new Paragraph("Student: " + studentName, metaFont));
    meta.addCell(left);

    PdfPCell right = new PdfPCell();
    right.setBorder(PdfPCell.NO_BORDER);
    right.addElement(new Paragraph("Semester: " + semester + " " + year, metaFont));
    right.addElement(new Paragraph("Date: " + new java.util.Date(), metaFont));
    meta.addCell(right);

    document.add(meta);
    document.add(Chunk.NEWLINE);

    // --- Table ---
    int cols = model.getColumnCount();
    PdfPTable table = new PdfPTable(cols);
    table.setWidthPercentage(100);
    table.setSpacingBefore(6f);
    table.setSpacingAfter(6f);

    // header row
    for (int c = 0; c < cols; c++) {
      PdfPCell h = new PdfPCell(new Phrase(model.getColumnName(c), headerFont));
      h.setBackgroundColor(Color.DARK_GRAY);
      h.setHorizontalAlignment(Element.ALIGN_CENTER);
      h.setPadding(6);
      table.addCell(h);
    }

    // iterate rows
    int rows = model.getRowCount();

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        Object o = model.getValueAt(r, c);
        String text = (o == null) ? "" : o.toString();
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
      }
    }

    document.add(table);

    // --- Footer ---
    document.add(Chunk.NEWLINE);

    PdfPTable stats = new PdfPTable(new float[]{1, 1});
    stats.setWidthPercentage(100);

    PdfPCell st1 = new PdfPCell(new Phrase("Total Credits", headerFont));
    st1.setBackgroundColor(Color.GRAY); st1.setHorizontalAlignment(Element.ALIGN_CENTER);
    PdfPCell st2 = new PdfPCell(new Phrase("SGPA", headerFont));
    st2.setBackgroundColor(Color.GRAY); st2.setHorizontalAlignment(Element.ALIGN_CENTER);

    stats.addCell(st1); stats.addCell(st2);

    PdfPCell v1 = new PdfPCell(new Phrase(String.valueOf( credits), cellFont));
    v1.setHorizontalAlignment(Element.ALIGN_CENTER);
    PdfPCell v2 = new PdfPCell(new Phrase(scgpa, cellFont));
    v2.setHorizontalAlignment(Element.ALIGN_CENTER);

    stats.addCell(v1); stats.addCell(v2);

    document.add(stats);

    document.close();
  }


  // Retrieves the gradebook data for a specific section, including student details and component scores.
  public static ArrayList<ArrayList<String>> getSectionGradebook(int sectionId) throws Exception {

    ArrayList<ArrayList<String>> result = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Step 1: Load all students in this section
      String sqlStudents =
          "SELECT e.enrollment_id, u.username, s.roll_no " +
              "FROM enrollments e " +
              "JOIN students s ON e.student_id = s.student_id " +
              "JOIN auth_db.users_auth u ON s.user_id = u.user_id " +
              "WHERE e.section_id = ?";

      PreparedStatement st1 = conn.prepareStatement(sqlStudents);
      st1.setInt(1, sectionId);

      ResultSet rs1 = st1.executeQuery();

      while (rs1.next()) {

        int enrId = rs1.getInt("enrollment_id");
        String username = rs1.getString("username");
        String roll = rs1.getString("roll_no");

        // Step 2: Load grades for this enrollment
        String sqlGrades =
            "SELECT component, score, total_score, weightage " +
                "FROM grades WHERE enrollment_id = ?";

        PreparedStatement st2 = conn.prepareStatement(sqlGrades);
        st2.setInt(1, enrId);

        ResultSet rs2 = st2.executeQuery();

        // EMPTY defaults
        String quizS="", quizT="", quizW="";
        String midS="", midT="", midW="";
        String finS="", finT="", finW="";
        String finalGrade="";

        while (rs2.next()) {
          String c = rs2.getString("component");

          switch (c.toLowerCase()) {
            case "quiz":
              quizS = rs2.getString("score");
              quizT = rs2.getString("total_score");
              quizW = rs2.getString("weightage");
              break;
            case "midterm":
              midS = rs2.getString("score");
              midT = rs2.getString("total_score");
              midW = rs2.getString("weightage");
              break;
            case "final":
              finS = rs2.getString("score");
              finT = rs2.getString("total_score");
              finW = rs2.getString("weightage");
              break;
          }
        }

        // Step 3: Compute final grade if possible
        finalGrade = computeFinal(quizS, quizT, quizW, midS, midT, midW, finS, finT, finW);

        // Build row for JTable
        ArrayList<String> row = new ArrayList<>();
        row.add(roll);
        row.add(username);

        row.add(quizS); row.add(quizT); row.add(quizW);
        row.add(midS);  row.add(midT);  row.add(midW);
        row.add(finS);  row.add(finT);  row.add(finW);

        row.add(finalGrade);

        result.add(row);
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return result;
  }

  // Helper method to compute the final grade score based on string inputs for components.
  private static String computeFinal(
      String qS, String qT, String qW,
      String mS, String mT, String mW,
      String fS, String fT, String fW) {

    double total = 0;
    boolean any = false;

    try {
      if (!qS.isEmpty() && !qT.isEmpty() && !qW.isEmpty()) {
        total += (Double.parseDouble(qS) / Double.parseDouble(qT)) * Double.parseDouble(qW);
        any = true;
      }
      if (!mS.isEmpty() && !mT.isEmpty() && !mW.isEmpty()) {
        total += (Double.parseDouble(mS) / Double.parseDouble(mT)) * Double.parseDouble(mW);
        any = true;
      }
      if (!fS.isEmpty() && !fT.isEmpty() && !fW.isEmpty()) {
        total += (Double.parseDouble(fS) / Double.parseDouble(fT)) * Double.parseDouble(fW);
        any = true;
      }
    } catch (Exception e) {
      return "";
    }

    if (!any) return "";
    return String.format("%.2f", total);
  }


  // Retrieves the enrollment ID for a student in a specific section.
  public static int getEnrollmentId(String studentUsername, int sectionId) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {

      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT e.enrollment_id FROM enrollments e " +
              "JOIN students s ON e.student_id = s.student_id " +
              "JOIN auth_db.users_auth u ON s.user_id = u.user_id " +
              "WHERE u.username=? AND e.section_id=?";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, studentUsername);
      stmt.setInt(2, sectionId);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt("enrollment_id");
      } else {
        throw new Exception("Student is NOT enrolled in that section");
      }
    }
  }

  // Fetches the score, total score, and weightage for a specific grade component.
  public static ArrayList<String> getGrade(int enrollmentId, String component) {
    ArrayList<String> row = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement stmt = conn.prepareStatement(
          "SELECT score, total_score, weightage FROM grades " +
              "WHERE enrollment_id=? AND component=?"
      );

      stmt.setInt(1, enrollmentId);
      stmt.setString(2, component);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        row.add(String.valueOf(rs.getFloat("score")));
        row.add(String.valueOf(rs.getFloat("total_score")));
        row.add(String.valueOf(rs.getFloat("weightage")));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return row;
  }

  // Verifies a plain text password against a stored BCrypt hash.
  public static boolean verifyPassword(String plainPassword, String storedHash) {
    return BCrypt.checkpw(plainPassword, storedHash);
  }

  // Adds a new student record to the ERP database, linking to the Auth database user.
  public static void addStudent(String username, String rollNo, int year, String program) throws Exception {
    try (Connection connAuth = DriverManager.getConnection(URL_AUTH, USER, PASSWORD);
         Connection connERP = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {

      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement check = connAuth.prepareStatement(
          "SELECT user_id FROM users_auth WHERE username=? AND role='student'");
      check.setString(1, username);
      ResultSet rs = check.executeQuery();

      if (!rs.next()) {
        System.out.println("❌ No student found in users_auth with username: " + username);
        throw new Exception("No student found in users_auth with username: " + username);
      }

      int userId = rs.getInt("user_id");

      PreparedStatement stmt = connERP.prepareStatement(
          "INSERT INTO students (user_id, roll_no, year, program) VALUES (?, ?, ?, ?)");
      stmt.setInt(1, userId);
      stmt.setString(2, rollNo);
      stmt.setInt(3, year);
      stmt.setString(4, program);

      int rows = stmt.executeUpdate();
      System.out.println("✅ " + rows + " student(s) added.");

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Adds a new instructor record to the ERP database, linking to the Auth database user.
  public static void addInstructor(String username, String department) throws Exception {
    try (Connection connAuth = DriverManager.getConnection(URL_AUTH, USER, PASSWORD);
         Connection connERP = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {

      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement check = connAuth.prepareStatement(
          "SELECT user_id FROM users_auth WHERE username=? AND role='instructor'");
      check.setString(1, username);
      ResultSet rs = check.executeQuery();

      if (!rs.next()) {
        System.out.println("❌ No instructor found in users_auth: " + username);
        throw new Exception("No instructor found in users_auth with username: " + username);
      }

      int userId = rs.getInt("user_id");

      PreparedStatement stmt = connERP.prepareStatement(
          "INSERT INTO instructors (user_id, department) VALUES (?, ?)");
      stmt.setInt(1, userId);
      stmt.setString(2, department);

      int rows = stmt.executeUpdate();
      System.out.println("✅ " + rows + " instructor(s) added.");

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Returns a list of all course codes available in the database.
  public static ArrayList<String> listCourseCodes() throws Exception {
    ArrayList<String> list = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement stmt = conn.prepareStatement(
          "SELECT code FROM courses ORDER BY code"
      );

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        list.add(rs.getString("code"));
      }

    } catch (Exception e) {
      throw new Exception("Error fetching course list: " + e.getMessage());
    }

    return list;
  }

  // Returns a list of all instructor usernames.
  public static ArrayList<String> listInstructorUsernames() throws Exception {
    ArrayList<String> list = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement stmt = conn.prepareStatement(
          "SELECT u.username FROM auth_db.users_auth u " +
              "JOIN instructors i ON u.user_id = i.user_id ORDER BY u.username"
      );

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        list.add(rs.getString("username"));
      }

    } catch (Exception e) {
      throw new Exception("Error fetching instructor list: " + e.getMessage());
    }

    return list;
  }

  // Adds a new course to the courses table.
  public static void addCourse(String code, String title, int credits) {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement stmt = conn.prepareStatement(
          "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)");
      stmt.setString(1, code);
      stmt.setString(2, title);
      stmt.setInt(3, credits);

      int rows = stmt.executeUpdate();
      System.out.println("✅ " + rows + " course(s) added.");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Toggles the global maintenance mode setting in the database.
  public static void setMaintenance(boolean isOn) throws Exception {
    String sql = "INSERT INTO settings (`key`, `value`) VALUES ('maintenance', ?) " +
        "ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      stmt.setString(1, isOn ? "true" : "false");
      stmt.executeUpdate();
      System.out.println("⚙️ Maintenance mode set to: " + (isOn ? "ON" : "OFF"));
    }catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Retrieves the global deadline setting (deprecated, see semester-specific deadline).
  public static String getDeadline() throws Exception {
    String deadline = "";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "SELECT value FROM settings WHERE `key` = 'deadline'";
      PreparedStatement stmt = conn.prepareStatement(sql);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        deadline = rs.getString("value");
      }

    } catch (Exception e) {
      throw new Exception("Failed to fetch deadline: " + e.getMessage());
    }

    return deadline;
  }

  // Updates the global deadline setting (deprecated).
  public static boolean updateDeadline(String newDeadline) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "INSERT INTO settings (`key`, `value`) VALUES ('global_deadline', ?) " +
          "ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";
      PreparedStatement stmt = conn.prepareStatement(sql);

      stmt.setString(1, newDeadline);

      int updated = stmt.executeUpdate();
      return updated > 0;

    } catch (Exception e) {
      throw new Exception("Failed to update deadline: " + e.getMessage());
    }
  }

  // Updates the global deadline setting using UPDATE syntax (deprecated).
  public static boolean updateDeadline_DEPRECATED(String newDeadline) throws Exception {

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "UPDATE settings SET value = ? WHERE `key` = 'deadline'";
      PreparedStatement stmt = conn.prepareStatement(sql);

      stmt.setString(1, newDeadline);

      int updated = stmt.executeUpdate();
      return updated > 0;

    } catch (Exception e) {
      throw new Exception("Failed to update deadline: " + e.getMessage());
    }
  }

  // Checks the maintenance mode status from the database.
  public static boolean isMaintenanceOn() {
    String sql = "SELECT `value` FROM settings WHERE `key` = 'maintenance'";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      if (rs.next()) {
        return rs.getString("value").equalsIgnoreCase("true");
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  // Restores the database from a backup SQL file using the mysql command line tool.
  public static boolean restore() throws Exception {
    try {
      System.out.println("Starting restore...");

      ProcessBuilder pb = new ProcessBuilder(
          "mysql",
          "-h", host,
          "-u", USER,
          "-p" + PASSWORD
      );

      pb.redirectErrorStream(true);
      Process process = pb.start();

      // send SQL file to mysql
      Files.copy(Paths.get(sqlFilePath), process.getOutputStream());
      process.getOutputStream().close();

      int status = process.waitFor();

      if (status == 0) {
        System.out.println("Restore completed successfully!");
      } else {
        System.out.println("Restore FAILED! Exit code: " + status);
      }

    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }

  // Backs up the database to an SQL file using the mysqldump command line tool.
  public static boolean backupDatabase() throws Exception {

    try {
      System.out.println("Starting backup...");

      ProcessBuilder pb = new ProcessBuilder(
          "mysqldump",
          "-h", host,
          "-u", USER,
          "-p" + PASSWORD,
          "--databases", dbName
      );

      pb.redirectErrorStream(false);
      pb.redirectOutput(new File(sqlFilePath));

      Process process = pb.start();

      InputStream err = process.getErrorStream();
      new Thread(() -> {
        try {
          err.transferTo(System.out);
        } catch (Exception ignored) {}
      }).start();

      int code = process.waitFor();

      return code == 0;

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Returns the current enrollment count for a specific section.
  public static int getSectionCapacity(int sectionId) throws Exception {
    int count = 0;

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "SELECT COUNT(*) AS cnt FROM enrollments WHERE section_id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setInt(1, sectionId);

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        count = rs.getInt("cnt");
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return count;
  }

  // Updates details of an existing section in the database.
  public static void updateSection(int sectionId,
                                   String courseCode,
                                   String instructorUsername,
                                   String dayTime,
                                   String room,
                                   int capacity,
                                   String semester,
                                   int year) throws Exception {

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // 1. Get course_id
      PreparedStatement courseStmt = conn.prepareStatement(
          "SELECT course_id FROM courses WHERE code=?");
      courseStmt.setString(1, courseCode);
      ResultSet courseRs = courseStmt.executeQuery();

      if (!courseRs.next()) {
        throw new Exception("Course not found: " + courseCode);
      }
      int courseId = courseRs.getInt("course_id");

      // 2. Get instructor_id
      PreparedStatement instStmt = conn.prepareStatement(
          "SELECT i.instructor_id FROM instructors i " +
              "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
              "WHERE u.username=?");
      instStmt.setString(1, instructorUsername);
      ResultSet instRs = instStmt.executeQuery();

      if (!instRs.next()) {
        throw new Exception("Instructor not found: " + instructorUsername);
      }
      int instructorId = instRs.getInt("instructor_id");

      // 3. Update Section
      PreparedStatement stmt = conn.prepareStatement(
          "UPDATE sections SET " +
              "course_id=?, instructor_id=?, day_time=?, room=?, " +
              "capacity=?, semester=?, year=? " +
              "WHERE section_id=?");

      stmt.setInt(1, courseId);
      stmt.setInt(2, instructorId);
      stmt.setString(3, dayTime);
      stmt.setString(4, room);
      stmt.setInt(5, capacity);
      stmt.setString(6, semester);
      stmt.setInt(7, year);
      stmt.setInt(8, sectionId);

      int rows = stmt.executeUpdate();

      if (rows == 0) {
        throw new Exception("No section found with ID: " + sectionId);
      }

      System.out.println("✅ Section updated successfully.");

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Adds a new section to the database, ensuring no duplicate sections for the same instructor and course.
  public static void addSection(String courseCode, String instructorUsername, String dayTime,
                                String room, int capacity, String semester, int year) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // 1. Get course_id
      PreparedStatement courseStmt = conn.prepareStatement(
          "SELECT course_id FROM courses WHERE code=?");
      courseStmt.setString(1, courseCode);
      ResultSet courseRs = courseStmt.executeQuery();
      if (!courseRs.next()) {
        System.out.println("❌ Course not found: " + courseCode);
        throw new Exception("Course not found: " + courseCode);
      }
      int courseId = courseRs.getInt("course_id");

      // 2. Get instructor_id
      PreparedStatement instrStmt = conn.prepareStatement(
          "SELECT i.instructor_id FROM instructors i " +
              "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
              "WHERE u.username=?");
      instrStmt.setString(1, instructorUsername);
      ResultSet instrRs = instrStmt.executeQuery();
      if (!instrRs.next()) {
        System.out.println("❌ Instructor not found: " + instructorUsername);
        throw new Exception("Instructor not found: " + instructorUsername);
      }
      int instructorId = instrRs.getInt("instructor_id");

      // 3. DUPLICATE CHECK
      PreparedStatement checkStmt = conn.prepareStatement(
          "SELECT section_id FROM sections WHERE course_id=? AND instructor_id=?");
      checkStmt.setInt(1, courseId);
      checkStmt.setInt(2, instructorId);

      ResultSet checkRs = checkStmt.executeQuery();
      if (checkRs.next()) {
        System.out.println("⚠️  Duplicate section found: instructor already teaches this course");
        throw new Exception("Duplicate section: instructor already teaches this course.");
      }

      // 4. Insert new section
      PreparedStatement insertStmt = conn.prepareStatement(
          "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?)");
      insertStmt.setInt(1, courseId);
      insertStmt.setInt(2, instructorId);
      insertStmt.setString(3, dayTime);
      insertStmt.setString(4, room);
      insertStmt.setInt(5, capacity);
      insertStmt.setString(6, semester);
      insertStmt.setInt(7, year);

      int rows = insertStmt.executeUpdate();
      System.out.println("✅ " + rows + " section(s) added.");

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Enrolls a student in a section, checking for existing enrollments to prevent duplicates.
  public static boolean enrollStudent(String studentUsername, int sectionId) throws Exception  {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      PreparedStatement stmt = conn.prepareStatement(
          "SELECT s.student_id FROM students s JOIN auth_db.users_auth u ON s.user_id=u.user_id WHERE u.username=?");
      stmt.setString(1, studentUsername);
      ResultSet rs = stmt.executeQuery();

      if (!rs.next()) {
        System.out.println("❌ Student not found: " + studentUsername);
        throw new Exception("Student not found: " + studentUsername);
      }

      int studentId = rs.getInt("student_id");

      PreparedStatement enroll = conn.prepareStatement(
          "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'enrolled')");
      enroll.setInt(1, studentId);
      enroll.setInt(2, sectionId);

      int rows = enroll.executeUpdate();
      System.out.println("✅ " + rows + " enrollment(s) added.");
      return rows > 0;
    } catch (java.sql.SQLIntegrityConstraintViolationException e1) {
      throw new Exception("You are already enrolled in this section. No duplicate registration allowed.");
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Drops a student from a section, handling foreign key constraints if grades exist.
  public static boolean dropStudent(String studentUsername, int sectionId) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Step 1: Find student_id using username
      PreparedStatement stmt = conn.prepareStatement(
          "SELECT s.student_id FROM students s " +
              "JOIN auth_db.users_auth u ON s.user_id = u.user_id " +
              "WHERE u.username = ?");
      stmt.setString(1, studentUsername);
      ResultSet rs = stmt.executeQuery();

      if (!rs.next()) {
        System.out.println("❌ Student not found: " + studentUsername);
        throw new Exception("Student not found: " + studentUsername);
      }

      int studentId = rs.getInt("student_id");

      // Step 2: Drop (delete) the enrollment
      PreparedStatement drop = conn.prepareStatement(
          "DELETE FROM enrollments WHERE student_id=? AND section_id=?");
      drop.setInt(1, studentId);
      drop.setInt(2, sectionId);

      int rows = drop.executeUpdate();

      if (rows > 0) {
        System.out.println("🗑️  Student dropped from section successfully.");
        return true;
      } else {
        System.out.println("⚠️  Student was not enrolled in this section.");
        throw new Exception("Student was not enrolled in this section.");
      }

    } catch (java.sql.SQLIntegrityConstraintViolationException e) {
      // This specific exception catches the foreign key constraint failure
      throw new Exception("You have already been graded for this course.");
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Inserts a new grade record directly given IDs (simplified method).
  public static void addGrade(String username, int sectionId, String component, float score, String finalGrade) {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Step 1: Find student_id using username
      PreparedStatement studentStmt = conn.prepareStatement(
          "SELECT s.student_id FROM students s " +
              "JOIN auth_db.users_auth u ON s.user_id = u.user_id WHERE u.username=?");
      studentStmt.setString(1, username);
      ResultSet rs1 = studentStmt.executeQuery();
      if (!rs1.next()) {
        System.out.println("❌ Student not found.");
        return;
      }
      int studentId = rs1.getInt("student_id");

      // Step 2: Find enrollment_id
      PreparedStatement enrollStmt = conn.prepareStatement(
          "SELECT enrollment_id FROM enrollments WHERE student_id=? AND section_id=?");
      enrollStmt.setInt(1, studentId);
      enrollStmt.setInt(2, sectionId);
      ResultSet rs2 = enrollStmt.executeQuery();
      if (!rs2.next()) {
        System.out.println("❌ Student is not enrolled in this section.");
        return;
      }
      int enrollmentId = rs2.getInt("enrollment_id");

      // Step 3: Insert grade
      PreparedStatement gradeStmt = conn.prepareStatement(
          "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, ?, ?, ?)");
      gradeStmt.setInt(1, enrollmentId);
      gradeStmt.setString(2, component);
      gradeStmt.setFloat(3, score);
      gradeStmt.setString(4, finalGrade);
      gradeStmt.executeUpdate();

      System.out.println("✅ Grade added successfully for " + username);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Deletes a student and all associated records (grades, enrollments) from the database.
  public static void deleteStudent(String username) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Find student_id
      PreparedStatement find = conn.prepareStatement(
          "SELECT s.student_id FROM students s JOIN auth_db.users_auth u ON s.user_id=u.user_id WHERE u.username=?"
      );
      find.setString(1, username);
      ResultSet rs = find.executeQuery();

      if (!rs.next()) throw new Exception("Student not found: " + username);

      int studentId = rs.getInt("student_id");

      // Delete child records
      conn.prepareStatement("DELETE FROM grades WHERE enrollment_id IN (SELECT enrollment_id FROM enrollments WHERE student_id=" + studentId + ")").executeUpdate();
      conn.prepareStatement("DELETE FROM enrollments WHERE student_id=" + studentId).executeUpdate();

      // Delete student
      PreparedStatement deleteStudent = conn.prepareStatement("DELETE FROM students WHERE student_id=?");
      deleteStudent.setInt(1, studentId);
      deleteStudent.executeUpdate();

      System.out.println("🗑️ Student deleted: " + username);

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Deletes an instructor and all their taught sections from the database.
  public static void deleteInstructor(String username) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Get instructor_id
      PreparedStatement find = conn.prepareStatement(
          "SELECT i.instructor_id FROM instructors i JOIN auth_db.users_auth u ON i.user_id=u.user_id WHERE u.username=?"
      );
      find.setString(1, username);
      ResultSet rs = find.executeQuery();

      if (!rs.next()) throw new Exception("Instructor not found: " + username);

      int instructorId = rs.getInt("instructor_id");

      // Delete sections taught by instructor
      PreparedStatement stmt = conn.prepareStatement(
          "DELETE FROM sections WHERE instructor_id = ?"
      );
      stmt.setInt(1, instructorId);

      int rows = stmt.executeUpdate();
      // Delete instructor
      PreparedStatement deleteInst = conn.prepareStatement("DELETE FROM instructors WHERE instructor_id=?");
      deleteInst.setInt(1, instructorId);
      deleteInst.executeUpdate();

      System.out.println("🗑️ Instructor deleted: " + username);

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Deletes a course and all related sections, enrollments, and grades from the database.
  public static void deleteCourse(String courseCode) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Get course_id
      PreparedStatement find = conn.prepareStatement("SELECT course_id FROM courses WHERE code=?");
      find.setString(1, courseCode);
      ResultSet rs = find.executeQuery();

      if (!rs.next()) throw new Exception("Course not found: " + courseCode);

      int courseId = rs.getInt("course_id");

      // Delete grades of enrollments in course sections
      conn.prepareStatement(
          "DELETE FROM grades WHERE enrollment_id IN (" +
              "SELECT enrollment_id FROM enrollments WHERE section_id IN (" +
              "SELECT section_id FROM sections WHERE course_id=" + courseId + "))"
      ).executeUpdate();

      // Delete enrollments
      conn.prepareStatement(
          "DELETE FROM enrollments WHERE section_id IN (" +
              "SELECT section_id FROM sections WHERE course_id=" + courseId + ")"
      ).executeUpdate();

      // Delete sections
      conn.prepareStatement("DELETE FROM sections WHERE course_id=" + courseId).executeUpdate();

      // Delete course
      PreparedStatement delete = conn.prepareStatement("DELETE FROM courses WHERE course_id=?");
      delete.setInt(1, courseId);
      delete.executeUpdate();

      System.out.println("🗑️ Course deleted: " + courseCode);

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Retrieves a list of all sections with details for the catalog view.
  public static ArrayList<ArrayList<String>> listAllSections2D() throws Exception {
    ArrayList<ArrayList<String>> table = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT s.section_id, c.code, c.title, u.username AS instructor, " +
              "s.day_time, s.room, s.capacity, s.semester, s.year " +
              "FROM sections s " +
              "JOIN courses c ON s.course_id = c.course_id " +
              "JOIN instructors i ON s.instructor_id = i.instructor_id " +
              "JOIN auth_db.users_auth u ON i.user_id = u.user_id";

      PreparedStatement stmt = conn.prepareStatement(sql);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        ArrayList<String> row = new ArrayList<>();

        row.add(String.valueOf(rs.getInt("section_id")));
        row.add(rs.getString("code"));
        row.add(rs.getString("title"));
        row.add(rs.getString("instructor"));
        row.add(rs.getString("day_time"));
        row.add(rs.getString("room"));
        row.add(String.valueOf(rs.getInt("capacity")));
        row.add(rs.getString("semester"));
        row.add(String.valueOf(rs.getInt("year")));

        table.add(row);
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return table;
  }

  // Updates the title and credits of a course.
  public static boolean updateCourse(String code, String newTitle, int newCredits) throws Exception {
    String sql = "UPDATE courses SET title = ?, credits = ? WHERE code = ?";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      Class.forName("com.mysql.cj.jdbc.Driver");

      stmt.setString(1, newTitle);
      stmt.setInt(2, newCredits);
      stmt.setString(3, code);

      int rows = stmt.executeUpdate();

      if (rows > 0) {
        System.out.println("✔ Course updated: " + code);
        return true;
      } else {
        throw new Exception("Course not found: " + code);
      }

    } catch (Exception e) {
      throw new Exception("Error updating course: " + e.getMessage());
    }
  }

  // Retrieves a combined list of students and instructors for the admin user management panel.
  public static ArrayList<ArrayList<String>> viewallstudents() throws Exception {
    ArrayList<ArrayList<String>> table = new ArrayList<>();

    Class.forName("com.mysql.cj.jdbc.Driver");

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {

      String sql =
          "SELECT u.user_id, u.username, u.role, s.program " +
              "FROM students s " +
              "JOIN auth_db.users_auth u ON s.user_id = u.user_id " ;


      PreparedStatement stmt = conn.prepareStatement(sql);
      ResultSet rs = stmt.executeQuery();

      String Sql2 = "Select u.user_id , u.username , u.role , i.department " + "From instructors i " + "JOIN auth_db.users_auth u ON i.user_id = u.user_id "  ;
      PreparedStatement stmt2 = conn.prepareStatement(Sql2);
      ResultSet rs2 = stmt2.executeQuery();

      while (rs.next()) {
        ArrayList<String> row = new ArrayList<>();
        row.add(String.valueOf(rs.getInt("user_id")));
        row.add(rs.getString("username"));
        row.add(rs.getString("role"));
        row.add(rs.getString("program"));
        table.add(row);
      }

      while (rs2.next()) {
        ArrayList<String> row = new ArrayList<>();
        row.add(String.valueOf(rs2.getInt("user_id")));
        row.add(rs2.getString("username"));
        row.add(rs2.getString("role"));
        row.add(rs2.getString("department"));
        table.add(row);
      }

    } catch (Exception e) {
      System.err.println("Error viewing all students: " + e.getMessage());
      throw new Exception("Database operation failed: " + e.getMessage());
    }

    return table;
  }

  // Retrieves all sections a specific student is enrolled in.
  public static ArrayList<ArrayList<String>> listStudentSections2D(String username) throws Exception {
    ArrayList<ArrayList<String>> table = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT s.section_id, c.code, c.title, s.day_time, s.room, s.semester, s.year " +
              "FROM enrollments e " +
              "JOIN sections s ON e.section_id = s.section_id " +
              "JOIN courses c ON s.course_id = c.course_id " +
              "JOIN students st ON e.student_id = st.student_id " +
              "JOIN auth_db.users_auth u ON st.user_id = u.user_id " +
              "WHERE u.username = ?";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        ArrayList<String> row = new ArrayList<>();

        row.add(String.valueOf(rs.getInt("section_id")));
        row.add(rs.getString("code"));
        row.add(rs.getString("title"));

        row.add(rs.getString("day_time"));
        row.add(rs.getString("room"));
        row.add(rs.getString("semester"));
        row.add(String.valueOf(rs.getInt("year")));

        table.add(row);
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return table;
  }

  // Retrieves a list of all courses with their details.
  public static ArrayList<ArrayList<String>> listCourses2D() throws Exception {
    ArrayList<ArrayList<String>> table = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "SELECT course_id, code, title, credits FROM courses";
      PreparedStatement stmt = conn.prepareStatement(sql);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        ArrayList<String> row = new ArrayList<>();
        row.add(rs.getString("code"));
        row.add(rs.getString("title"));
        row.add(String.valueOf(rs.getInt("credits")));
        table.add(row);
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return table;
  }

  // Retrieves a simple list of section details for a specific instructor.
  public static ArrayList<String> getInstructorSections(String instructorUsername) throws Exception {
    ArrayList<String> list = new ArrayList<>();

    String sql =
        "SELECT s.section_id, c.code, c.title " +
            "FROM sections s " +
            "JOIN instructors i ON s.instructor_id = i.instructor_id " +
            "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
            "JOIN courses c ON s.course_id = c.course_id " +
            "WHERE u.username = ?";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, instructorUsername);

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        list.add(
            rs.getInt("section_id") +
                " - " + rs.getString("code") +
                " (" + rs.getString("title") + ")"
        );
      }
    }
    return list;
  }

  // Retrieves detailed information about sections taught by a specific instructor.
  public static ArrayList<ArrayList<String>> listSectionsByInstructor(String instructorUsername) throws Exception {
    ArrayList<ArrayList<String>> table = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT s.section_id, c.code, c.title, s.day_time, s.room, " +
              "s.capacity, s.semester, s.year " +
              "FROM sections s " +
              "JOIN courses c ON s.course_id = c.course_id " +
              "JOIN instructors i ON s.instructor_id = i.instructor_id " +
              "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
              "WHERE u.username = ?";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, instructorUsername);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        ArrayList<String> row = new ArrayList<>();

        row.add(String.valueOf(rs.getInt("section_id")));
        row.add(rs.getString("code"));
        row.add(rs.getString("title"));
        row.add(rs.getString("day_time"));
        row.add(rs.getString("room"));
        row.add(String.valueOf(rs.getInt("capacity")));
        row.add(rs.getString("semester"));
        row.add(String.valueOf(rs.getInt("year")));

        table.add(row);
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return table;
  }

  // Deletes a section and all associated enrollments and grades from the database.
  public static void deleteSection(int sectionId) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Delete grades
      conn.prepareStatement(
          "DELETE FROM grades WHERE enrollment_id IN (" +
              "SELECT enrollment_id FROM enrollments WHERE section_id=" + sectionId + ")"
      ).executeUpdate();

      // Delete enrollments
      conn.prepareStatement("DELETE FROM enrollments WHERE section_id=" + sectionId).executeUpdate();

      // Delete section
      PreparedStatement delete = conn.prepareStatement("DELETE FROM sections WHERE section_id=?");
      delete.setInt(1, sectionId);
      delete.executeUpdate();

      System.out.println("🗑️ Section deleted: " + sectionId);

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  // Retrieves a distinct list of semesters taught by an instructor.
  public static ArrayList<String> getInstructorSemesters(String username) throws Exception {
    ArrayList<String> list = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT DISTINCT s.semester, s.year " +
              "FROM sections s " +
              "JOIN instructors i ON s.instructor_id = i.instructor_id " +
              "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
              "WHERE u.username = ? " +
              "ORDER BY s.year DESC, s.semester ASC";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        list.add(rs.getString("semester") + " - " + rs.getInt("year"));
      }
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
    return list;
  }

  // Retrieves sections taught by an instructor for a specific semester and year.
  public static ArrayList<String> getInstructorSectionsBySemester(String username, String semester, int year) throws Exception {
    ArrayList<String> list = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT s.section_id, c.code, c.title " +
              "FROM sections s " +
              "JOIN courses c ON s.course_id = c.course_id " +
              "JOIN instructors i ON s.instructor_id = i.instructor_id " +
              "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
              "WHERE u.username = ? AND s.semester = ? AND s.year = ?";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, username);
      stmt.setString(2, semester);
      stmt.setInt(3, year);

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        list.add(
            rs.getInt("section_id") +
                " - " + rs.getString("code") +
                " (" + rs.getString("title") + ")"
        );
      }
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
    return list;
  }

  // Adds a new notification message to the database.
  public static String addNotification(String message) throws Exception {

    String sql = "INSERT INTO notifications (message) VALUES (?)";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, message);
      stmt.executeUpdate();
      return "Notification added successfully.";

    } catch (Exception e) {
      throw new Exception("Error adding notification: " + e.getMessage());
    }
  }

  // Retrieves all notifications ordered by creation time.
  public static ArrayList<ArrayList<String>> listNotifications() throws Exception {

    ArrayList<ArrayList<String>> table = new ArrayList<>();

    String sql = "SELECT notification_id, message, created_at FROM notifications ORDER BY created_at DESC";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        ArrayList<String> row = new ArrayList<>();
        row.add(String.valueOf(rs.getInt("notification_id")));
        row.add(rs.getString("message"));
        row.add(String.valueOf(rs.getTimestamp("created_at")));
        table.add(row);
      }

    } catch (Exception e) {
      throw new Exception("Error fetching notifications: " + e.getMessage());
    }

    return table;
  }

  // Deletes a notification by its ID.
  public static String deleteNotification(int notificationId) throws Exception {

    String sql = "DELETE FROM notifications WHERE notification_id = ?";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, notificationId);
      int rows = stmt.executeUpdate();

      if (rows == 0) return "Notification not found.";
      return "Notification deleted successfully.";

    } catch (Exception e) {
      throw new Exception("Error deleting notification: " + e.getMessage());
    }
  }

  // Retrieves detailed information about a specific section by ID.
  public static Map<String, String> getSectionDetails(int sectionId) throws Exception {
    Map<String, String> details = new HashMap<>();

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql =
          "SELECT s.section_id, c.code, c.title, u.username AS instructor, " +
              "s.day_time, s.room, s.capacity, s.semester, s.year " +
              "FROM sections s " +
              "JOIN courses c ON s.course_id = c.course_id " +
              "JOIN instructors i ON s.instructor_id = i.instructor_id " +
              "JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
              "WHERE s.section_id = ?";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setInt(1, sectionId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        details.put("section_id", String.valueOf(rs.getInt("section_id")));
        details.put("code", rs.getString("code"));
        details.put("title", rs.getString("title"));
        details.put("instructor", rs.getString("instructor"));
        details.put("day_time", rs.getString("day_time"));
        details.put("room", rs.getString("room"));
        details.put("capacity", String.valueOf(rs.getInt("capacity")));
        details.put("semester", rs.getString("semester"));
        details.put("year", String.valueOf(rs.getInt("year")));
      } else {
        throw new Exception("Section not found with ID: " + sectionId);
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }

    return details;
  }

  // Retrieves the currently active semester setting.
  public static String getCurrentSemester() throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "SELECT value FROM settings WHERE `key` = 'current_semester'";
      PreparedStatement stmt = conn.prepareStatement(sql);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getString("value");
      } else {
        throw new Exception("System setting 'current_semester' is not defined.");
      }

    } catch (Exception e) {
      throw new Exception("Failed to fetch current semester: " + e.getMessage());
    }
  }

  // Checks if there are any enrollments for a specific section.
  public static boolean hasEnrollments(int sectionId) throws Exception {
    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD)) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setInt(1, sectionId);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
      return false;
    } catch (Exception e) {
      throw new Exception("Error checking enrollments: " + e.getMessage());
    }
  }

  // Updates or inserts the registration deadline for a specific semester.
  public static boolean updateSemesterDeadline(String semester, int year, String deadline) throws Exception {
    String key = "deadline_" + semester + "_" + year;
    String sql = "INSERT INTO settings (`key`, `value`) VALUES (?, ?) " +
        "ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      Class.forName("com.mysql.cj.jdbc.Driver");
      stmt.setString(1, key);
      stmt.setString(2, deadline);
      return stmt.executeUpdate() > 0;
    } catch (Exception e) {
      throw new Exception("Failed to update semester deadline: " + e.getMessage());
    }
  }

  // Sets the globally active semester for student registration views.
  public static boolean setCurrentSemester(String semester, int year) throws Exception {
    String value = semester + "-" + year;
    String sql = "INSERT INTO settings (`key`, `value`) VALUES ('current_semester', ?) " +
        "ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      Class.forName("com.mysql.cj.jdbc.Driver");
      stmt.setString(1, value);
      System.out.println("✅ Active semester set to: " + value);
      return stmt.executeUpdate() > 0;
    } catch (Exception e) {
      throw new Exception("Failed to set active semester: " + e.getMessage());
    }
  }

  // Retrieves the registration deadline for a specific semester.
  public static String getSemesterDeadline(String semester, int year) throws Exception {
    String key = "deadline_" + semester + "_" + year;
    String sql = "SELECT `value` FROM settings WHERE `key` = ?";

    try (Connection conn = DriverManager.getConnection(URL_ERP, USER, PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      Class.forName("com.mysql.cj.jdbc.Driver");
      stmt.setString(1, key);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getString("value");
      }
      return null;
    } catch (Exception e) {
      throw new Exception("Failed to get semester deadline: " + e.getMessage());
    }
  }

}