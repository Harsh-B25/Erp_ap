package edu.univ.erp.api.maintenance;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.service.erp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MaintenanceApi {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceApi.class);

    public ApiResponse<String> setMaintenance(boolean isOn){
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
    public ApiResponse<String> checkMaintenance(){
        logger.info("Checking maintenance mode.");
        try {
            boolean status = erp.isMaintenanceOn();
            logger.info("Maintenance mode status: {}", status);
            return ApiResponse.success("OK", Boolean.toString(status));
        } catch (Exception e) {
            logger.error("Error checking maintenance mode.", e);
            return ApiResponse.fail(e.getMessage());
        }
    }
}