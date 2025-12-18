import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.Auth;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * FULL TEST SUITE FOR AuthApi
 *
 * Uses:
 * - JUnit5
 * - Mockito-inline (required for mocking static methods)
 *
 * We DO NOT touch database. All Auth.* and erp.* calls are faked using static mocking.
 */
public class AuthApiTest {

    // -------------------------------------------------------------------------
    //  TEST: login()
    // -------------------------------------------------------------------------
    @Test
    public void testLoginSuccess() throws Exception {

        AuthApi api = new AuthApi();

        // Mock Auth.login()
        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {

            authMock.when(() -> Auth.login("harsh", "1234")).thenReturn(true);

            ApiResponse<String> res = api.login("harsh", "1234");

            assertTrue(res.isSuccess());
            assertEquals("Login Successful", res.getMessage());

            authMock.verify(() -> Auth.login("harsh", "1234"));
        }
    }

    @Test
    public void testLoginFailureWrongPassword() throws Exception {

        AuthApi api = new AuthApi();

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {

            authMock.when(() -> Auth.login("harsh", "wrong"))
                    .thenThrow(new Exception("Incorrect password. Attempt 1/5"));

            ApiResponse<String> res = api.login("harsh", "wrong");

            assertFalse(res.isSuccess());
            assertEquals("Incorrect password. Attempt 1/5", res.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    //  TEST: getRole()
    // -------------------------------------------------------------------------
    @Test
    public void testGetRoleSuccess() throws Exception {

        AuthApi api = new AuthApi();

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {

            authMock.when(() -> Auth.getrole("harsh")).thenReturn("admin");

            ApiResponse<String> res = api.getRole("harsh");

            assertTrue(res.isSuccess());
            assertEquals("OK", res.getMessage());
            assertEquals("admin", res.getData());

            authMock.verify(() -> Auth.getrole("harsh"));
        }
    }

    @Test
    public void testGetRoleUserNotFound() throws Exception {

        AuthApi api = new AuthApi();

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {

            authMock.when(() -> Auth.getrole("xyz"))
                    .thenThrow(new Exception("User not found"));

            ApiResponse<String> res = api.getRole("xyz");

            assertFalse(res.isSuccess());
            assertEquals("User not found", res.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    //  TEST: insertUser()
    // -------------------------------------------------------------------------
    @Test
    public void testInsertUserSuccess() throws Exception {

        AuthApi api = new AuthApi();

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {

            authMock.when(() -> Auth.insertUser("alpha", "admin", "pass"))
                    .thenReturn(true);

            ApiResponse<String> res = api.insertUser("alpha", "admin", "pass");

            assertTrue(res.isSuccess());
            assertEquals("OK", res.getMessage());

            authMock.verify(() -> Auth.insertUser("alpha", "admin", "pass"));
        }
    }

    @Test
    public void testInsertUserFail() throws Exception {

        AuthApi api = new AuthApi();

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {

            authMock.when(() -> Auth.insertUser("alpha", "admin", "pass"))
                    .thenThrow(new Exception("Duplicate username"));

            ApiResponse<String> res = api.insertUser("alpha", "admin", "pass");

            assertFalse(res.isSuccess());
            assertEquals("Duplicate username", res.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    //  TEST: deleteUser()
    // -------------------------------------------------------------------------
    @Test
    public void testDeleteUserSuccess() throws Exception {

        AuthApi api = new AuthApi();

        try (MockedStatic<Auth> authMock = mockStatic(Auth.class)) {

            authMock.when(() -> Auth.deleteUser("bob")).thenReturn(true);

            ApiResponse<String> res = api.deleteUser("bob");

            assertTrue(res.isSuccess());
            assertEquals("OK", res.getMessage());
        }
    }
    
}