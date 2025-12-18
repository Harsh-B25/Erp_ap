import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.erp;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminApiExtraTest {

  // --------------------------
  // addinstructor()
  // --------------------------
  @Test
  void testAddInstructorSuccess() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // addInstructor is void, so use thenAnswer
      erpMock.when(() -> erp.addInstructor("alice", "CS")).thenAnswer(invocation -> null);

      ApiResponse<String> res = api.addinstructor("alice", "CS");

      assertTrue(res.isSuccess());
      assertEquals("OK", res.getMessage());
    }
  }

  @Test
  void testAddInstructorFail() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      erpMock.when(() -> erp.addInstructor("alice", "CS"))
          .thenThrow(new Exception("Instructor exists"));

      ApiResponse<String> res = api.addinstructor("alice", "CS");

      assertFalse(res.isSuccess());
      assertEquals("Instructor exists", res.getMessage());
    }
  }

  // --------------------------
  // addcourse()
  // --------------------------
  @Test
  void testAddCourseSuccess() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // addCourse is void
      erpMock.when(() -> erp.addCourse("CS101", "Intro CS", 4)).thenAnswer(invocation -> null);

      ApiResponse<String> res = api.addcourse("CS101", "Intro CS", 4);

      assertTrue(res.isSuccess());
      assertEquals("OK", res.getMessage());
    }
  }


  @Test
  void testAddCourseFail() {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // Use RuntimeException instead of checked Exception
      erpMock.when(() -> erp.addCourse("CS101", "Intro CS", 4))
          .thenThrow(new RuntimeException("Course already exists"));

      ApiResponse<String> res = api.addcourse("CS101", "Intro CS", 4);

      assertFalse(res.isSuccess());
      assertEquals("Course already exists", res.getMessage());
    }
  }

  // --------------------------
  // addsection()
  // --------------------------
  @Test
  void testAddSectionSuccess() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // addSection is void
      erpMock.when(() -> erp.addSection("CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025))
          .thenAnswer(invocation -> null);

      ApiResponse<String> res = api.addsection("CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025);

      assertTrue(res.isSuccess());
      assertEquals("OK", res.getMessage());
    }
  }

  @Test
  void testAddSectionFail() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      erpMock.when(() -> erp.addSection("CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025))
          .thenThrow(new Exception("Section conflict"));

      ApiResponse<String> res = api.addsection("CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025);

      assertFalse(res.isSuccess());
      assertEquals("Section conflict", res.getMessage());
    }
  }

  // --------------------------
  // delete_student()
  // --------------------------
  @Test
  void testDeleteStudentSuccess() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // deleteStudent is void
      erpMock.when(() -> erp.deleteStudent("bob")).thenAnswer(invocation -> null);

      ApiResponse<String> res = api.delete_stdent("bob");

      assertTrue(res.isSuccess());
      assertEquals("OK", res.getMessage());
    }
  }


  @Test
  void testDeleteStudentFail() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      erpMock.when(() -> erp.deleteStudent("bob"))
          .thenThrow(new Exception("Student not found"));

      ApiResponse<String> res = api.delete_stdent("bob");

      assertFalse(res.isSuccess());
      assertEquals("Student not found", res.getMessage());
    }
  }

  // --------------------------
  // delete_instructor()
  // --------------------------
  @Test
  void testDeleteInstructorSuccess() throws Exception {
    AdminApi api = new AdminApi();

    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // For void methods, do nothing (success)
      erpMock.when(() -> erp.deleteInstructor("alice")).thenAnswer(invocation -> null);

      ApiResponse<String> res = api.delete_instructor("alice");

      assertTrue(res.isSuccess());
      assertEquals("OK", res.getMessage());
    }
  }

  @Test
  void testDeleteInstructorFail() throws Exception {
    AdminApi api = new AdminApi();

    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // For void methods, throw exception to simulate failure
      erpMock.when(() -> erp.deleteInstructor("alice"))
          .thenThrow(new Exception("Instructor not found"));

      ApiResponse<String> res = api.delete_instructor("alice");

      assertFalse(res.isSuccess());
      assertEquals("Instructor not found", res.getMessage());}
  }

  // --------------------------
  // delete_course()
  // --------------------------
  @Test
  void testDeleteCourseSuccess() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // deleteCourse is void
      erpMock.when(() -> erp.deleteCourse("CS101")).thenAnswer(invocation -> null);

      ApiResponse<String> res = api.delete_course("CS101");

      assertTrue(res.isSuccess());
      assertEquals("OK", res.getMessage());
    }
  }

  @Test
  void testDeleteCourseFail() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      erpMock.when(() -> erp.deleteCourse("CS101"))
          .thenThrow(new Exception("Course not found"));

      ApiResponse<String> res = api.delete_course("CS101");

      assertFalse(res.isSuccess());
      assertEquals("Course not found", res.getMessage());
    }
  }

  // --------------------------
  // delete_section()
  // --------------------------
  @Test
  void testDeleteSectionSuccess() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // deleteSection is void
      erpMock.when(() -> erp.deleteSection(1)).thenAnswer(invocation -> null);

      ApiResponse<String> res = api.delete_section(1);

      assertTrue(res.isSuccess());
      assertEquals("OK", res.getMessage());
    }
  }

  @Test
  void testDeleteSectionFail() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      erpMock.when(() -> erp.deleteSection(1))
          .thenThrow(new Exception("Section not found"));

      ApiResponse<String> res = api.delete_section(1);

      assertFalse(res.isSuccess());
      assertEquals("Section not found", res.getMessage());
    }
  }

  // --------------------------
  // update_section()
  // --------------------------
  @Test
  void testUpdateSectionSuccess() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      // For void static methods that succeed
      erpMock.when(() -> erp.updateSection(1, "CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025))
          .thenAnswer(invocation -> null); // do nothing

      ApiResponse<String> res = api.update_section(1, "CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025);

      assertTrue(res.isSuccess());
      assertEquals("Section updated successfully.", res.getMessage());
    }
  }

  @Test
  void testUpdateSectionFail() throws Exception {
    AdminApi api = new AdminApi();
    try (MockedStatic<erp> erpMock = mockStatic(erp.class)) {
      erpMock.when(() -> erp.updateSection(1, "CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025))
          .thenThrow(new Exception("Update failed"));

      ApiResponse<String> res = api.update_section(1, "CS101", "alice", "Mon 10-12", "R101", 30, "Fall", 2025);

      assertFalse(res.isSuccess());
      assertEquals("Update failed", res.getMessage());
    }
  }
}