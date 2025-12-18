package edu.univ.erp.api.instructor;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.erp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.TableModel;
import java.util.ArrayList;

public class InstructorApi{

    private static final Logger logger = LoggerFactory.getLogger(InstructorApi.class);

    public ApiResponse<String> computeFinalGradeForStudent(String username, int sectionId) {
        logger.info("Computing final grade for student {} in section {}", username, sectionId);
    try {
        int enrollmentId = erp.getEnrollmentId(username, sectionId);
        if (enrollmentId == -1)
            return ApiResponse.fail("Student not enrolled");

        String grade = erp.computeFinalGradeForEnrollment(enrollmentId);

        if (grade.equals("INCOMPLETE"))
            return ApiResponse.fail("Cannot compute: missing components");
        
        logger.info("Successfully computed final grade for student {} in section {}", username, sectionId);
        return ApiResponse.success(grade);

    } catch (Exception e) {
        logger.error("Error computing final grade for student {} in section {}", username, sectionId, e);
        return ApiResponse.fail(e.getMessage());
    }
}


    public ApiResponse<String> computeFinalGradesForSection(int sectionId) {
        logger.info("Computing final grades for section {}", sectionId);
    try {

        ArrayList<String> usernames = erp.getUsernamesInSection(sectionId);
        int success = 0;

        for (String user : usernames) {
            int eid = erp.getEnrollmentId(user, sectionId);
            String result = erp.computeFinalGradeForEnrollment(eid);

            if (!result.equals("INCOMPLETE")) {
                success++;
            }
        }
        logger.info("Successfully computed {} final grades for section {}", success, sectionId);
        return ApiResponse.success("Successfully computed " + success + " grades");

    } catch (Exception e) {
        logger.error("Error computing final grades for section {}", sectionId, e);
        return ApiResponse.fail(e.getMessage());
    }
  }

    public ApiResponse<ArrayList<String>> listSemesters(String username) {
        logger.info("Listing semesters for instructor {}", username);
      try {
        ArrayList<String> list = erp.getInstructorSemesters(username);
        logger.info("Successfully listed semesters for instructor {}", username);
        return ApiResponse.success("OK", list);
      } catch (Exception e) {
        logger.error("Error listing semesters for instructor {}", username, e);
        return ApiResponse.fail(e.getMessage());
      }
    }

    public ApiResponse<ArrayList<String>> listSectionsBySemester(String username, String semester, int year) {
        logger.info("Listing sections for instructor {} in semester {} year {}", username, semester, year);
      try {
        ArrayList<String> list = erp.getInstructorSectionsBySemester(username, semester, year);
        logger.info("Successfully listed sections for instructor {} in semester {} year {}", username, semester, year);
        return ApiResponse.success("OK", list);
      } catch (Exception e) {
        logger.error("Error listing sections for instructor {} in semester {} year {}", username, semester, year, e);
        return ApiResponse.fail(e.getMessage());
      }
    }



    public ApiResponse<ArrayList<ArrayList<String>>> listsectionsbyinstructor(String username) {
        logger.info("Listing sections by instructor {}", username);
        try {
            ArrayList<ArrayList<String>> list = erp.listSectionsByInstructor(username);
            logger.info("Successfully listed sections by instructor {}", username);
            return ApiResponse.success("ok" ,list);
        } catch (Exception e) {
            logger.error("Error listing sections by instructor {}", username, e);
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<String> addgrade(String username, String component, int sectionId,
                                   float score, float total, float weight) {
        logger.info("Adding grade for student {} in section {}, component: {}", username, sectionId, component);
    try {
        int enrollId = erp.getEnrollmentId(username, sectionId);

        boolean ok = erp.addGrade(enrollId, component, score, total, weight);
        if (ok) {
            logger.info("Successfully added grade for student {} in section {}", username, sectionId);
            return ApiResponse.success("OK");
        } else {
            logger.warn("Failed to add grade for student {} in section {}", username, sectionId);
            return ApiResponse.fail("Failed to add quiz grade");
        }

    } catch (Exception e) {
        logger.error("Error adding grade for student {} in section {}", username, sectionId, e);
        return ApiResponse.fail(e.getMessage());
    }
}
public ApiResponse<String> generatepdf(String outputPath ,  String sectionId , String instructorName , TableModel model) {
        logger.info("Generating PDF for section {}", sectionId);
        try {
            erp.generatePDF(outputPath, sectionId, instructorName, model);
            logger.info("Successfully generated PDF for section {}", sectionId);
            return ApiResponse.success("OK");
        } catch (Exception e) {
            logger.error("Error generating PDF for section {}", sectionId, e);
            return ApiResponse.fail(e.getMessage());
        }
    }

public ApiResponse<String> updategrade(String username,String component ,  int sectionId,
                                      float score, float total, float weight) {
    logger.info("Updating grade for student {} in section {}, component: {}", username, sectionId, component);
    try {
        int enrollId = erp.getEnrollmentId(username, sectionId);

        boolean ok = erp.updateGrade(enrollId, component, score, total, weight);
        if (ok) {
            logger.info("Successfully updated grade for student {} in section {}", username, sectionId);
            return ApiResponse.success("OK");
        } else {
            logger.warn("Failed to update grade for student {} in section {}", username, sectionId);
            return ApiResponse.fail("Failed to update quiz");
        }

    } catch (Exception e) {
        logger.error("Error updating grade for student {} in section {}", username, sectionId, e);
        return ApiResponse.fail(e.getMessage());
    }
}

public ApiResponse<ArrayList<String>> getgrade(String username, String component, int sectionId) {
    logger.info("Getting grade for student {} in section {}, component: {}", username, sectionId, component);
    try {
        int enrollId = erp.getEnrollmentId(username, sectionId);
        ArrayList<String> row = erp.getGrade(enrollId, component);
        logger.info("Successfully retrieved grade for student {} in section {}", username, sectionId);
        return ApiResponse.success("ok" ,row);

    } catch (Exception e) {
        logger.error("Error getting grade for student {} in section {}", username, sectionId, e);
        return ApiResponse.fail(e.getMessage());
    }
}

public ApiResponse<ArrayList<ArrayList<String>>> getroster(int sectionId) {
    logger.info("Getting roster for section {}", sectionId);
    try {
        ArrayList<ArrayList<String>> roster = erp.getSectionGradebook(sectionId);
        logger.info("Successfully retrieved roster for section {}", sectionId);
        return ApiResponse.success("ok" ,roster);

    } catch (Exception e) {
        logger.error("Error getting roster for section {}", sectionId, e);
        return ApiResponse.fail(e.getMessage());
    }
}







    public ApiResponse<ArrayList<String>> listSections(String username) {
        logger.info("Listing sections for instructor {}", username);
        try {
            ArrayList<String> sections = erp.getInstructorSections(username);
            logger.info("Successfully listed sections for instructor {}", username);
            return ApiResponse.success("OK" , sections);
        } catch (Exception e) {
            logger.error("Error listing sections for instructor {}", username, e);
            return ApiResponse.fail(e.getMessage());
        }
    }
}