-- MySQL dump 10.13  Distrib 9.5.0, for Win64 (x86_64)
--
-- Host: localhost    Database: auth_db
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

-- SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '3eadfd94-b576-11f0-abe3-bceca01a814e:1-595';

--
-- Current Database: `auth_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `auth_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `auth_db`;

--
-- Table structure for table `users_auth`
--

DROP TABLE IF EXISTS `users_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_auth` (
                              `user_id` int NOT NULL AUTO_INCREMENT,
                              `username` varchar(50) NOT NULL,
                              `role` enum('admin','student','instructor') NOT NULL,
                              `password_hash` varchar(255) NOT NULL,
                              `status` varchar(20) DEFAULT 'active',
                              `last_login` datetime DEFAULT NULL,
                              `old_password_hash` varchar(255) DEFAULT NULL,
                              `password_changed_at` datetime DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`user_id`),
                              UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_auth`
--

LOCK TABLES `users_auth` WRITE;
/*!40000 ALTER TABLE `users_auth` DISABLE KEYS */;
INSERT INTO `users_auth` VALUES (2,'harsh','student','$2a$10$71EsELzbd.cV8Va1GfnIXeu5gdU3HefxkiegxIBrERlP981pOMVv2','active','2025-11-28 20:27:34',NULL,'2025-11-21 19:33:43'),(3,'dhruv','instructor','$2a$10$XfrNHZ9up14lgHFjl6izOea8QXAPwAxBjqBAFLvPQZM6GNi.Rt.Gi','active','2025-11-28 20:27:53',NULL,'2025-11-21 23:51:37'),(4,'admin','admin','$2a$10$0nc1zr0ysW5JfI1LxCyE8.BxFYOxJ3KQYCEVOlD18wKyVA8vJN1KC','active','2025-11-28 20:36:35','$2a$10$ex.vPjMBO9dOr5rNAC.ErOpAFYdxpNQk21aSDOxnW6XJWPjmgcdCu','2025-11-28 20:36:49'),(5,'tom','student','$2a$10$HdL88a/dICBz6C9pen.HxOkxVrPPd3jHOwwuFfOGE7H5fuwwLO1gi','active','2025-11-26 01:07:28',NULL,'2025-11-22 14:54:32'),(11,'kirat','instructor','$2a$10$I09t/TJVZ2oOcR/vTPIE6OQydhnXZQ5n4ikBa22zuhsa6ldgLslOG','active',NULL,NULL,'2025-11-26 01:24:40'),(13,'stu1','student','$2a$10$QIiYPnqd2zCxF0YDs3InEe98/aTEGl93ByWgbW9nuxTUmZo8rVE26','active','2025-11-27 19:31:11',NULL,'2025-11-27 19:14:03'),(14,'stu2','student','$2a$10$oWCTIiwT6FvOcfDI..IWHOclFZrFPEyfq/P2a2u4608czYyJFYxQ.','active','2025-11-27 19:31:27',NULL,'2025-11-27 19:14:20'),(15,'stu3','student','$2a$10$zRIRd2UQ2ZYubAj2N9Yeuur7NS2iUxuKhfRtSHTHIxK/3t9i0e60a','active','2025-11-27 19:35:09',NULL,'2025-11-27 19:14:43');
/*!40000 ALTER TABLE `users_auth` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-28 20:37:32
