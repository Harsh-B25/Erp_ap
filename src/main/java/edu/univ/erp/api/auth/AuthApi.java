package edu.univ.erp.api.auth;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.Auth;
import edu.univ.erp.service.erp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AuthApi {

    private static final Logger logger = LoggerFactory.getLogger(AuthApi.class);

   public ApiResponse<String> login(String u, String p) {
    logger.info("Login attempt for user: {}", u);
    try {
        Auth.login(u, p);  // returns true OR throws
        logger.info("Login successful for user: {}", u);
        return ApiResponse.success("Login Successful");

    } catch (Exception e) {
        logger.warn("Login failed for user: {}. Reason: {}", u, e.getMessage());
        return ApiResponse.fail(e.getMessage());
    }
}

    public ApiResponse<String> getRole(String u) {
        logger.info("Getting role for user: {}", u);
        try {
            String role = Auth.getrole(u);
            logger.info("Successfully retrieved role for user: {}", u);
            return ApiResponse.success("OK", role);
        } catch (Exception e) {
            logger.error("Error getting role for user: {}", u, e);
            return ApiResponse.fail(e.getMessage());
        }
    }


    public ApiResponse<String> insertUser(String u, String r, String p) {
        logger.info("Inserting user: {}, role: {}", u, r);
        try {
            Auth.insertUser(u, r, p);   // void → must wrap in try/catch
            logger.info("Successfully inserted user: {}", u);
            return ApiResponse.success("OK");
        } catch (Exception e) {
            logger.error("Error inserting user: {}", u, e);
            return ApiResponse.fail(e.getMessage());
        }
    }
    

    public ApiResponse<String> deleteUser(String u) {
        logger.info("Deleting user: {}", u);
        try {
            Auth.deleteUser(u);
            logger.info("Successfully deleted user: {}", u);
            return ApiResponse.success("OK");
        } catch (Exception e) {
            logger.error("Error deleting user: {}", u, e);
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<String> changePassword(String u, String oldp, String newp) {
        logger.info("Changing password for user: {}", u);
        try {
            if(erp.isMaintenanceOn()== true){
                throw new Exception("Maintenance Mode is ON") ;
            }
            Auth.changePassword(u, oldp, newp);
            logger.info("Successfully changed password for user: {}", u);
            return ApiResponse.success("OK");
        } catch (Exception e) {
            logger.error("Error changing password for user: {}", u, e);
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<String> setStatus(String u, String status) {
        logger.info("Setting status for user: {}, status: {}", u, status);
        try {
            Auth.changeUserStatus(u, status);
            logger.info("Successfully set status for user: {}", u);
            return ApiResponse.success("OK");
        } catch (Exception e) {
            logger.error("Error setting status for user: {}", u, e);
            return ApiResponse.fail(e.getMessage());
        }
    }
}