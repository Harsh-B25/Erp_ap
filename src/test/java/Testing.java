import edu.univ.erp.UI.common.DeadlineBannerPanel;
import edu.univ.erp.service.erp;
import edu.univ.erp.UI.student.ViewGradesPanel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class Testing {

  // ------------------------------
  // Test hashPassword
  // ------------------------------
  @Test
  void testHashPassword() {
    String plain = "mySecret123";
    String hash = erp.hashPassword(plain);

    assertNotNull(hash);                       // Hash is not null
    assertNotEquals(plain, hash);             // Hash differs from plain text
    assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$")); // BCrypt prefix
  }

  // ------------------------------
  // Test verifyPassword
  // ------------------------------
  @Test
  void testVerifyPasswordMatch() {
    String plain = "mySecret123";
    String hash = erp.hashPassword(plain);

    assertTrue(erp.verifyPassword(plain, hash)); // Correct password
  }

  @Test
  void testVerifyPasswordMismatch() {
    String plain = "mySecret123";
    String hash = erp.hashPassword(plain);

    assertFalse(erp.verifyPassword("wrongPassword", hash)); // Wrong password
  }

  // ------------------------------
  // Test computeFinal
  // ------------------------------
  @Test
  void testComputeFinalNormal() throws Exception {
    Method method = erp.class.getDeclaredMethod("computeFinal",
        String.class, String.class, String.class,
        String.class, String.class, String.class,
        String.class, String.class, String.class);
    method.setAccessible(true);

    String result = (String) method.invoke(null, "80", "100", "20",
        "90", "100", "30",
        "70", "100", "50");
    assertEquals("78.00", result); // Weighted total: 16 + 27 + 35 = 78
  }

  @Test
  void testComputeFinalMissingComponents() throws Exception {
    Method method = erp.class.getDeclaredMethod("computeFinal",
        String.class, String.class, String.class,
        String.class, String.class, String.class,
        String.class, String.class, String.class);
    method.setAccessible(true);

    String result = (String) method.invoke(null, "80", "100", "20",
        "90", "100", "30",
        "", "", "");
    assertEquals("43.00", result); // Only quiz + midterm counted
  }

  @Test
  void testComputeFinalAllEmpty() throws Exception {
    Method method = erp.class.getDeclaredMethod("computeFinal",
        String.class, String.class, String.class,
        String.class, String.class, String.class,
        String.class, String.class, String.class);
    method.setAccessible(true);

    String result = (String) method.invoke(null, "", "", "",
        "", "", "",
        "", "", "");
    assertEquals("", result); // No components
  }

  @Test
  void testComputeFinalInvalidNumbers() throws Exception {
    Method method = erp.class.getDeclaredMethod("computeFinal",
        String.class, String.class, String.class,
        String.class, String.class, String.class,
        String.class, String.class, String.class);
    method.setAccessible(true);

    String result = (String) method.invoke(null, "abc", "100", "20",
        "90", "100", "30",
        "70", "100", "50");
    assertEquals("", result); // Invalid input
  }

  // ------------------------------
  // Test gradeToPoint
  // ------------------------------
  @Test
  void testGradeToPointMapping() throws Exception {
    DeadlineBannerPanel deadlineBannerPanel = new DeadlineBannerPanel();
    ViewGradesPanel panel = new ViewGradesPanel("harsh", deadlineBannerPanel);
    Method method = ViewGradesPanel.class.getDeclaredMethod("gradeToPoint", String.class);
    method.setAccessible(true);

    assertEquals(10.0, (double) method.invoke(panel, "A+"));
    assertEquals(10.0, (double) method.invoke(panel, "A"));
    assertEquals(9.0, (double) method.invoke(panel, "A-"));
    assertEquals(8.0, (double) method.invoke(panel, "B"));
    assertEquals(7.0, (double) method.invoke(panel, "B-"));
    assertEquals(6.0, (double) method.invoke(panel, "C"));
    assertEquals(5.0, (double) method.invoke(panel, "C-"));
    assertEquals(4.0, (double) method.invoke(panel, "D"));
    assertEquals(0.0, (double) method.invoke(panel, "F"));

    // Invalid / default cases
    assertEquals(0.0, (double) method.invoke(panel, "Z"));
    assertEquals(0.0, (double) method.invoke(panel, ""));
  }
}