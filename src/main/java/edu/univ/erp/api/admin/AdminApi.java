package edu.univ.erp.api.admin;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.Auth;
import edu.univ.erp.service.erp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class AdminApi {
  private static final Logger logger = LoggerFactory.getLogger(AdminApi.class);

  // --- Update specific semester deadline ---
  public ApiResponse<Boolean> updateSemesterDeadline(String semester, int year, String newDeadline) {
    logger.info("Updating deadline for {} {} to {}", semester, year, newDeadline);
    try {
      boolean success = erp.updateSemesterDeadline(semester, year, newDeadline);
      return ApiResponse.success("OK", success);
    } catch (Exception e) {
      logger.error("Error updating semester deadline.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  // --- Set active semester ---
  public ApiResponse<Boolean> setCurrentSemester(String semester, int year) {
    logger.info("Setting active semester to {} {}", semester, year);
    try {
      boolean success = erp.setCurrentSemester(semester, year);
      return ApiResponse.success("OK", success);
    } catch (Exception e) {
      logger.error("Error setting active semester.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  // --- Get specific semester deadline ---
  public ApiResponse<String> getSemesterDeadline(String semester, int year) {
    try {
      String deadline = erp.getSemesterDeadline(semester, year);
      return ApiResponse.success("OK", deadline);
    } catch (Exception e) {
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<Boolean> checkEnrollments(int sectionId) {
    logger.info("Checking enrollments for section ID: {}", sectionId);
    try {
      boolean hasStudents = erp.hasEnrollments(sectionId);
      logger.info("Successfully checked enrollments for section ID: {}", sectionId);
      return ApiResponse.success("OK", hasStudents);
    } catch (Exception e) {
      logger.error("Error checking enrollments for section ID: {}", sectionId, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> addNotification(String msg) {
    logger.info("Adding notification: {}", msg);
    try {
      String result = erp.addNotification(msg);
      logger.info("Successfully added notification.");
      return ApiResponse.success(result);
    } catch (Exception e) {
      logger.error("Error adding notification: {}", msg, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<ArrayList<ArrayList<String>>> listNotifications() {
    logger.info("Listing notifications.");
    try {
      ArrayList<ArrayList<String>> notifications = erp.listNotifications();
      logger.info("Successfully listed notifications.");
      return ApiResponse.success("ok" , notifications);
    } catch (Exception e) {
      logger.error("Error listing notifications.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> deleteNotification(int id) {
    logger.info("Deleting notification with ID: {}", id);
    try {
      String result = erp.deleteNotification(id);
      logger.info("Successfully deleted notification with ID: {}", id);
      return ApiResponse.success(result);
    } catch (Exception e) {
      logger.error("Error deleting notification with ID: {}", id, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> adduser_erp(String username, String rollNo, int year, String program){
    logger.info("Adding user to ERP: username={}, rollNo={}, year={}, program={}", username, rollNo, year, program);
    try {
      erp.addStudent(username, rollNo, year, program);
      logger.info("Successfully added user to ERP: {}", username);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error adding user to ERP: {}", username, e);
      return ApiResponse.fail(e.getMessage());
    }

  }
  public ApiResponse<String> getDeadline(){
    logger.info("Getting deadline.");
    try {
      String deadline = erp.getDeadline();
      logger.info("Successfully retrieved deadline.");
      return ApiResponse.success("OK" ,deadline);
    } catch (Exception e) {
      logger.error("Error getting deadline.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }
  public ApiResponse<Boolean> updateDeadline(String newDeadline){
    logger.info("Updating deadline to: {}", newDeadline);
    try {
      erp.updateDeadline(newDeadline);
      logger.info("Successfully updated deadline.");
      return ApiResponse.success("OK" , true);
    } catch (Exception e) {
      logger.error("Error updating deadline.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<Boolean> backup(){
    logger.info("Starting database backup.");
    try {
      erp.backupDatabase();
      Auth.backupDatabase();
      logger.info("Database backup successful.");
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Database backup failed.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }
  public ApiResponse<Boolean> restore(){
    logger.info("Starting database restore.");
    try {
      Auth.restore();
      erp.restore();
      logger.info("Database restore successful.");
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Database restore failed.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> update_course(String code, String title, int credits) {
    logger.info("Updating course: code={}, title={}, credits={}", code, title, credits);
    try {
      boolean ok = erp.updateCourse(code, title, credits);
      if (ok) {
        logger.info("Course updated successfully: {}", code);
        return ApiResponse.success("Course updated");
      } else {
        logger.warn("Course update failed for code: {}", code);
        return ApiResponse.fail("Update failed");
      }
    } catch (Exception e) {
      logger.error("Error updating course: {}", code, e);
      return ApiResponse.fail(e.getMessage());
    }
  }


  public ApiResponse<String> addinstructor(String username, String department){
    logger.info("Adding instructor: username={}, department={}", username, department);
    try {
      erp.addInstructor(username, department);
      logger.info("Successfully added instructor: {}", username);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error adding instructor: {}", username, e);
      return ApiResponse.fail(e.getMessage());
    }

  }
  public ApiResponse<String> addcourse(String code, String title, int credits){
    logger.info("Adding course: code={}, title={}, credits={}", code, title, credits);
    try {
      erp.addCourse(code, title, credits);
      logger.info("Successfully added course: {}", code);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error adding course: {}", code, e);
      return ApiResponse.fail(e.getMessage());
    }
  }
  public ApiResponse<String> addsection(String courseCode, String instructorUsername, String dayTime,
                                        String room, int capacity, String semester, int year){
    logger.info("Adding section: courseCode={}, instructorUsername={}, dayTime={}, room={}, capacity={}, semester={}, year={}", courseCode, instructorUsername, dayTime, room, capacity, semester, year);
    try {
      erp.addSection(courseCode, instructorUsername, dayTime, room, capacity, semester, year);
      logger.info("Successfully added section for course: {}", courseCode);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error adding section for course: {}", courseCode, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> setmaintenance(boolean isOn){
    logger.info("Setting maintenance mode to: {}", isOn);
    try {
      erp.setMaintenance(isOn);
      logger.info("Successfully set maintenance mode to: {}", isOn);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error setting maintenance mode.", e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> delete_stdent(String username){
    logger.info("Deleting student: {}", username);
    try {
      erp.deleteStudent(username);
      logger.info("Successfully deleted student: {}", username);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error deleting student: {}", username, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> delete_instructor(String username){
    logger.info("Deleting instructor: {}", username);
    try {
      erp.deleteInstructor(username);
      logger.info("Successfully deleted instructor: {}", username);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error deleting instructor: {}", username, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> delete_course(String code){
    logger.info("Deleting course: {}", code);
    try {
      erp.deleteCourse(code);
      logger.info("Successfully deleted course: {}", code);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error deleting course: {}", code, e);
      return ApiResponse.fail(e.getMessage());
    }
  }


  public ApiResponse<String> delete_section(int sectionId){
    logger.info("Deleting section with ID: {}", sectionId);
    try {
      if(checkEnrollments(sectionId).getData()) {
        throw new Exception("This section already has students enrolled.");
      }
      erp.deleteSection(sectionId);
      logger.info("Successfully deleted section with ID: {}", sectionId);
      return ApiResponse.success("OK");
    } catch (Exception e) {
      logger.error("Error deleting section with ID: {}", sectionId, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> update_section(int sectionId,
                                            String courseCode,
                                            String instructorUsername,
                                            String dayTime,
                                            String room,
                                            int capacity,
                                            String semester,
                                            int year) {
    logger.info("Updating section with ID: {}", sectionId);
    try {
      erp.updateSection(sectionId, courseCode, instructorUsername,
          dayTime, room, capacity, semester, year);
      logger.info("Successfully updated section with ID: {}", sectionId);
      return ApiResponse.success("Section updated successfully.");

    } catch (Exception e) {
      logger.error("Error updating section with ID: {}", sectionId, e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<String> getCurrentSemester() {
    logger.info("Fetching current semester");
    try {
      String semester = erp.getCurrentSemester();
      logger.info("Successfully fetched current semester: {}", semester);
      return ApiResponse.success("OK", semester);
    } catch (Exception e) {
      logger.error("Error fetching current semester", e);
      return ApiResponse.fail(e.getMessage());
    }
  }

  public ApiResponse<Map<String, String>> getSectionDetails(int sectionId) {
    logger.info("Fetching details for section ID: {}", sectionId);
    try {
      Map<String, String> details = erp.getSectionDetails(sectionId);
      logger.info("Successfully fetched details for section ID: {}", sectionId);
      return ApiResponse.success("OK", details);
    } catch (Exception e) {
      logger.error("Error fetching details for section ID: {}", sectionId, e);
      return ApiResponse.fail(e.getMessage());
    }
  }
}