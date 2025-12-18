package edu.univ.erp.api.reports;

import edu.univ.erp.api.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ReportsApi{
    private static final Logger logger = LoggerFactory.getLogger(ReportsApi.class);
    public ApiResponse<String> dummy(){
        logger.info("ReportsApi.dummy() called");
        return ApiResponse.success("Reports placeholder");
    }
}