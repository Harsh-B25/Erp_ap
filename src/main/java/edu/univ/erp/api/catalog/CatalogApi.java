package edu.univ.erp.api.catalog;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.Auth;
import edu.univ.erp.service.erp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class CatalogApi {
    private static final Logger logger = LoggerFactory.getLogger(CatalogApi.class);

    public ApiResponse<ArrayList<ArrayList<String>>> listCourses() {
        logger.info("Listing courses.");
        try {
            ArrayList<ArrayList<String>> courses = erp.listCourses2D();
            logger.info("Successfully listed courses.");
            return ApiResponse.success("OK", courses);
        } catch (Exception e) {
            logger.error("Error listing courses.", e);
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<Integer> capacity(int sectionid) {
        logger.info("Getting capacity for section {}", sectionid);
        try {
            int z = erp.getSectionCapacity(sectionid) ;
            logger.info("Successfully retrieved capacity for section {}", sectionid);
            return ApiResponse.success("OK", z);
        } catch (Exception e) {
            logger.error("Error getting capacity for section {}", sectionid, e);
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<ArrayList<ArrayList<String>>> listSections() {
        logger.info("Listing all sections.");
        try {
            ArrayList<ArrayList<String>> sections = erp.listAllSections2D();
            logger.info("Successfully listed all sections.");
            return ApiResponse.success("OK", sections);
        } catch (Exception e) {
            logger.error("Error listing all sections.", e);
            return ApiResponse.fail(e.getMessage());
        }
    }
    public ApiResponse<ArrayList<ArrayList<String>>> view_users() {
        logger.info("Viewing all users.");
        try {
            ArrayList<ArrayList<String>> users = erp.viewallstudents() ;
            ArrayList<ArrayList<String>> admin = Auth.view_admins() ;
            users.addAll(admin);

            logger.info("Successfully viewed all users.");
            return ApiResponse.success("OK", users);
        } catch (Exception e) {
            logger.error("Error viewing all users.", e);
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<ArrayList<ArrayList<String>>> listStudentsSections(String username) {
        logger.info("Listing student sections for user {}", username);
        try {
            ArrayList<ArrayList<String>> sections = erp.listStudentSections2D(username);
            logger.info("Successfully listed student sections for user {}", username);
            return ApiResponse.success("OK", sections);
        } catch (Exception e) {
            logger.error("Error listing student sections for user {}", username, e);
            return ApiResponse.fail(e.getMessage());
        }
    }


    public ApiResponse<ArrayList<String>> listCourseCodes() {
        logger.info("Listing course codes.");
        try {
            ArrayList<String> list = erp.listCourseCodes();
            logger.info("Successfully listed course codes.");
            return ApiResponse.success("ok" ,list);
        } catch (Exception e) {
            logger.error("Error listing course codes.", e);
            return ApiResponse.fail(e.getMessage());
        }
    }

    // --- LIST INSTRUCTOR USERNAMES ---
    public ApiResponse<ArrayList<String>> listInstructorUsernames() {
        logger.info("Listing instructor usernames.");
        try {
            ArrayList<String> list = erp.listInstructorUsernames();
            logger.info("Successfully listed instructor usernames.");
            return ApiResponse.success("ok" ,list);
        } catch (Exception e) {
            logger.error("Error listing instructor usernames.", e);
            return ApiResponse.fail(e.getMessage());
        }
    }



}