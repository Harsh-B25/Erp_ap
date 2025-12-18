package edu.univ.erp.api.student;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.erp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.TableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class StudentApi{

    private static final Logger logger = LoggerFactory.getLogger(StudentApi.class);

  public ApiResponse<String> enrollStudent(String studentUsername, int sectionId) {
    try {
      if (erp.isMaintenanceOn()) {
        throw new Exception("Maintenance Mode is ON");
      }

      // Check deadline for the specific section's semester
      if (!checkIfBeforeDeadline(sectionId).getData()) {
        throw new Exception("Cannot enroll after the deadline.");
      }

      // --- NEW CHECK: Duplicate Course Enrollment ---
      if (erp.isEnrolledInCourse(studentUsername, sectionId)) {
        return ApiResponse.fail("Cannot enroll twice in the same course.");
      }
      // ----------------------------------------------

      erp.enrollStudent(studentUsername, sectionId);
      return ApiResponse.success("OK");

    } catch (Exception e) {
      return ApiResponse.fail(e.getMessage());
    }
  }

    public ApiResponse<String> drop(String username , int sectionId) {
        logger.info("Dropping student {} from section {}", username, sectionId);
        try {
            if(erp.isMaintenanceOn()== true){
                throw new Exception("Maintenance Mode is ON") ;
            }
            if (!checkIfBeforeDeadline(sectionId).getData()) {
                throw new Exception("Cannot drop after the deadline.");
            }
            erp.dropStudent(username, sectionId);
            logger.info("Successfully dropped student {} from section {}", username, sectionId);
            return ApiResponse.success("OK");
        } catch (Exception e) {
            logger.error("Error dropping student {} from section {}", username, sectionId, e);
            return ApiResponse.fail(e.getMessage());
        }

    }
    public ApiResponse<Integer> capacity(int sectionid) {
        logger.info("Getting capacity for section {}", sectionid);
        try {
            int z = erp.getSectionCapacity(sectionid) ;
            logger.info("Successfully retrieved capacity for section {}", sectionid);
            return ApiResponse.success("OK" ,z);

        } catch (Exception e) {
            logger.error("Error getting capacity for section {}", sectionid, e);
            return ApiResponse.fail(e.getMessage());
        }

    }
    public ApiResponse<ArrayList<String>> listSemesters(String username) {
        logger.info("Listing semesters for student {}", username);
    try {
        ArrayList<String> semesters = erp.listSemestersForStudent(username);
        logger.info("Successfully listed semesters for student {}", username);
        return ApiResponse.success("OK", semesters);
    } catch (Exception e) {
        logger.error("Error listing semesters for student {}", username, e);
        return ApiResponse.fail(e.getMessage());
    }

}

public ApiResponse<Boolean> checkIfBeforeDeadline(int sectionId) {
    logger.info("Checking deadline for section {}", sectionId);
    try {
      // 1. Get semester and year for the given section
      ApiResponse<Map<String, String>> sectionDetails = new edu.univ.erp.api.admin.AdminApi().getSectionDetails(sectionId);
      if (!sectionDetails.isSuccess() || sectionDetails.getData() == null) {
        throw new Exception("Could not retrieve section details.");
      }
      String semester = sectionDetails.getData().get("semester");
      int year = Integer.parseInt(sectionDetails.getData().get("year"));

      // 2. Get the deadline for that specific semester
      String deadlineDateString = erp.getSemesterDeadline(semester, year);
      if (deadlineDateString == null || deadlineDateString.isEmpty()) {
        logger.warn("No specific deadline set for {}-{}. Allowing action.", semester, year);
        return ApiResponse.success("OK", true); // Default to allowed if no deadline is set
      }

      LocalDate deadline = LocalDate.parse(deadlineDateString);
      boolean isBefore = !LocalDate.now().isAfter(deadline);
      logger.info("Deadline for {}-{} is {}. Check result: {}", semester, year, deadline, isBefore);
      return ApiResponse.success("OK", isBefore);
    } catch (Exception e) {
        logger.error("Error checking deadline for section {}", sectionId, e);
        return ApiResponse.fail(e.getMessage());
    }
}

public ApiResponse<Boolean> isRegistrationOpen() {
    logger.info("Checking if registration is open for the current semester.");
    try {
      // 1. Get the current active semester from admin settings (e.g., "Fall-2024")
      ApiResponse<String> currentSemesterRes = new edu.univ.erp.api.admin.AdminApi().getCurrentSemester();
      if (!currentSemesterRes.isSuccess() || currentSemesterRes.getData() == null) {
        logger.warn("Registration is closed because no active semester is set.");
        return ApiResponse.success("No active semester", false);
      }
      String[] parts = currentSemesterRes.getData().split("-");
      String semester = parts[0];
      int year = Integer.parseInt(parts[1]);

      // 2. Get the deadline for that specific semester
      String deadlineDateString = erp.getSemesterDeadline(semester, year);
      if (deadlineDateString == null || deadlineDateString.isEmpty()) {
        logger.warn("No specific deadline set for {}-{}. Assuming registration is open.", semester, year);
        return ApiResponse.success("OK", true); // Default to open if no deadline is set
      }

      LocalDate deadline = LocalDate.parse(deadlineDateString);
      boolean isBefore = !LocalDate.now().isAfter(deadline);
      logger.info("Registration deadline for {}-{} is {}. Open: {}", semester, year, deadline, isBefore);
      return ApiResponse.success("OK", isBefore);

    } catch (Exception e) {
      logger.error("Error checking if registration is open.", e);
      return ApiResponse.fail(e.getMessage());
    }
}

public ApiResponse<ArrayList<ArrayList<String>>> getSemesterGradebook(
            String username, String semester, int year) {
        logger.info("Getting semester gradebook for student {}, semester {}, year {}", username, semester, year);
        try {
            ArrayList<ArrayList<String>> data =
                    erp.getStudentSemesterGrades(username, semester, year);
            logger.info("Successfully retrieved semester gradebook for student {}", username);
            return new ApiResponse<>(true, "OK", data);

        } catch (Exception e) {
            logger.error("Error getting semester gradebook for student {}", username, e);
            return new ApiResponse<>(false, e.getMessage(), null);
        }
    }

    public ApiResponse<String> generateSemesterTranscriptPDF(
            String outputPath, String username, String semester, int year , TableModel model , int credits , String scgpa) {
        logger.info("Generating semester transcript PDF for student {}", username);
        try {
            erp.generateSemesterTranscriptPDF(outputPath, username, semester, year, model , credits , scgpa);
            logger.info("Successfully generated semester transcript PDF for student {}", username);
            return ApiResponse.success("Transcript PDF generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating semester transcript PDF for student {}", username, e);
            return ApiResponse.fail(e.getMessage());
        }
    }
}