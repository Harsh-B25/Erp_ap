package edu.univ.erp.PDF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main2 {

  private static final Logger logger = LoggerFactory.getLogger(Main2.class);

  public static void main(String[] args) {
    logger.info("Application started!");
    logger.debug("Debugging info...");
    logger.warn("This is a warning message.");
    logger.error("An error occurred!");
  }
}