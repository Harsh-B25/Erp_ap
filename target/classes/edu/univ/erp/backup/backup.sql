-- MySQL dump 10.13  Distrib 9.5.0, for Win64 (x86_64)
--
-- Host: localhost    Database: erp
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

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '3eadfd94-b576-11f0-abe3-bceca01a814e:1-595';

--
-- Current Database: `erp`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `erp` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `erp`;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `course_id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `credits` int NOT NULL,
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (1,'MTH101','LA',4),(2,'CSE101','IP',4),(3,'COM301','TCom',2),(4,'ECE101','DC',4),(7,'CSE102','Data Structures and Algorithms',4),(8,'CSE103','Computer Organization',4),(9,'CSE104','Operating Systems',4),(10,'CSE105','Computer Networks',4),(11,'CSE106','Database Management Systems',4),(12,'CSE107','Analysis and Design of Algorithms',4),(13,'CSE108','Artificial Intelligence',4),(14,'CSE109','Machine Learning',4),(15,'CSE110','Deep Learning',4),(16,'CSE111','Natural Language Processing',4),(17,'CSE112','Computer Vision',4),(18,'CSE113','Software Engineering',4),(19,'CSE114','Network Security',4),(20,'CSE115','Cryptography',4),(21,'CSE116','Distributed Systems',4),(22,'CSE117','Cloud Computing',4),(23,'CSE118','Big Data Analytics',4),(24,'CSE119','Human Computer Interaction',4),(25,'CSE120','Compilers',4),(26,'CSE121','Theory of Computation',4),(27,'CSE122','Mobile Computing',4),(28,'CSE123','Internet of Things',4),(29,'CSE124','Blockchain Technology',4),(30,'CSE125','Quantum Computing',4),(31,'CSE126','Information Retrieval',4),(32,'CSE127','Reinforcement Learning',4),(33,'CSE128','Robotics',4),(34,'CSE129','Digital Image Processing',4),(35,'CSE130','Advanced Programming',4),(37,'ECE102','Analog Electronics',4),(38,'ECE103','Signals and Systems',4),(39,'ECE104','Principles of Communication',4),(40,'ECE105','Digital Signal Processing',4),(41,'ECE106','Control Systems',4),(42,'ECE107','Embedded Systems',4),(43,'ECE108','VLSI Design',4),(44,'ECE109','Wireless Communication',4),(45,'ECE110','Optical Communication',4),(46,'ECE111','Antenna Theory',4),(47,'ECE112','Information Theory and Coding',4),(48,'ECE113','Microprocessors and Interfacing',4),(49,'ECE114','Digital System Design',4),(50,'ECE115','RF Engineering',4),(51,'ECE116','Satellite Communication',4),(52,'ECE117','Nanoelectronics',4),(53,'ECE118','Semiconductor Devices',4),(54,'ECE119','Radar Systems',4),(55,'ECE120','Internet of Things (ECE)',4),(57,'MTH102','Calculus',4),(58,'MTH103','Probability and Statistics',4),(59,'MTH104','Discrete Mathematics',4),(60,'MTH105','Differential Equations',4),(61,'MTH106','Numerical Methods',4),(62,'MTH107','Complex Analysis',4),(63,'MTH108','Optimization',4),(64,'MTH109','Number Theory',4),(65,'MTH110','Graph Theory',4),(66,'MTH111','Stochastic Processes',4),(67,'MTH112','Real Analysis',4),(68,'MTH113','Abstract Algebra',4),(69,'MTH114','Topology',4),(70,'MTH115','Functional Analysis',4),(71,'DES101','Introduction to Design',2),(72,'DES102','Visual Design',2),(73,'DES103','Product Design',2),(74,'DES104','Design Thinking',2),(75,'DES105','Human Computer Interaction (Des)',2),(76,'DES106','Animation Principles',2),(77,'DES107','3D Modeling',2),(78,'DES108','Typography',2),(79,'DES109','Game Design',2),(80,'DES110','User Experience Design',2),(81,'SSH101','Critical Thinking',2),(82,'SSH102','Introduction to Psychology',2),(83,'SSH103','Sociology',2),(84,'SSH104','Ethics in AI',2),(85,'SSH105','Philosophy of Mind',2),(86,'SSH106','Academic Writing',2),(87,'SSH107','Introduction to Film',2),(88,'SSH108','Political Science',2),(89,'SSH109','Urban Sociology',2),(90,'SSH110','Cognitive Psychology',2),(91,'SSH111','History of Technology',2),(92,'SSH112','Digital Humanities',2),(93,'SSH113','Public Policy',2),(94,'SSH114','Gender Studies',2),(95,'SSH115','Science Fiction and Society',2),(96,'ECO101','Microeconomics',4),(97,'ECO102','Macroeconomics',4),(98,'ECO103','Game Theory',4),(99,'ECO104','Econometrics',4),(100,'ECO105','Development Economics',4),(101,'ECO106','Behavioral Economics',4),(102,'ECO107','Public Finance',4),(103,'ECO108','International Trade',4),(104,'ECO109','Financial Economics',4),(105,'ECO110','Money and Banking',4),(106,'CSE201','Intelligent Systems',4),(107,'CSE301','New COurse 2',4);
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments` (
  `enrollment_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int DEFAULT NULL,
  `section_id` int DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `student_id` (`student_id`,`section_id`),
  KEY `section_id` (`section_id`),
  CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`),
  CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`section_id`) REFERENCES `sections` (`section_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
INSERT INTO `enrollments` VALUES (1,1,2,'enrolled'),(2,1,1,'enrolled'),(3,1,3,'enrolled'),(4,1,4,'enrolled'),(5,2,1,'enrolled'),(21,6,1,'enrolled'),(22,7,1,'enrolled'),(24,8,1,'enrolled'),(25,1,8,'enrolled');
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades` (
  `grade_id` int NOT NULL AUTO_INCREMENT,
  `enrollment_id` int DEFAULT NULL,
  `component` varchar(50) DEFAULT NULL,
  `score` float DEFAULT NULL,
  `total_score` float DEFAULT NULL,
  `weightage` float DEFAULT NULL,
  `final_grade` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`grade_id`),
  KEY `enrollment_id` (`enrollment_id`),
  CONSTRAINT `grades_ibfk_1` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`enrollment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
INSERT INTO `grades` VALUES (1,2,'quiz',10,20,25,NULL),(2,2,'midterm',50,70,35,NULL),(3,2,'final',65,75,40,NULL),(4,5,'quiz',15,20,25,NULL),(5,5,'midterm',47,70,35,NULL),(6,5,'final',65,75,40,NULL),(7,3,'quiz',10,25,20,NULL),(8,1,'quiz',20,25,25,NULL),(9,1,'midterm',35,35,35,NULL),(10,1,'final',30,40,40,NULL),(11,3,'midterm',20,30,30,NULL),(12,4,'quiz',20,20,20,NULL),(13,4,'midterm',40,40,40,NULL),(14,4,'final',40,40,40,NULL),(15,3,'final',25,40,50,NULL),(22,21,'quiz',5,20,25,NULL),(23,21,'midterm',20,70,35,NULL),(24,21,'final',33,75,40,NULL),(25,22,'quiz',9,20,25,NULL),(26,22,'midterm',36,70,35,NULL),(27,22,'final',49,75,40,NULL),(28,24,'quiz',2,20,25,NULL),(29,24,'midterm',66,70,35,NULL),(30,24,'final',54,75,40,NULL),(31,25,'quiz',0,10,20,NULL),(32,25,'midterm',0,10,30,NULL),(33,25,'final',0,10,50,NULL);
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instructors`
--

DROP TABLE IF EXISTS `instructors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instructors` (
  `instructor_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `department` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`instructor_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `instructors_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `auth_db`.`users_auth` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instructors`
--

LOCK TABLES `instructors` WRITE;
/*!40000 ALTER TABLE `instructors` DISABLE KEYS */;
INSERT INTO `instructors` VALUES (1,3,'Bio'),(2,11,'Math');
/*!40000 ALTER TABLE `instructors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `message` varchar(500) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (10,'test notifications','2025-11-28 14:46:10'),(12,'new notification','2025-11-28 14:51:37');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sections`
--

DROP TABLE IF EXISTS `sections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sections` (
  `section_id` int NOT NULL AUTO_INCREMENT,
  `course_id` int DEFAULT NULL,
  `instructor_id` int DEFAULT NULL,
  `day_time` varchar(50) DEFAULT NULL,
  `room` varchar(50) DEFAULT NULL,
  `capacity` int NOT NULL,
  `semester` varchar(20) DEFAULT NULL,
  `year` int DEFAULT NULL,
  PRIMARY KEY (`section_id`),
  KEY `course_id` (`course_id`),
  KEY `instructor_id` (`instructor_id`),
  CONSTRAINT `sections_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`),
  CONSTRAINT `sections_ibfk_2` FOREIGN KEY (`instructor_id`) REFERENCES `instructors` (`instructor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sections`
--

LOCK TABLES `sections` WRITE;
/*!40000 ALTER TABLE `sections` DISABLE KEYS */;
INSERT INTO `sections` VALUES (1,3,1,'Mon, Wed 12:30-2:00','C102',400,'Monsoon',2025),(2,2,1,'Tue, Thur 9:30-11:00','C101',500,'Monsoon',2025),(3,4,1,'Tue, Thur 11:00-12:30','C101',500,'Monsoon',2025),(4,1,1,'Fri, Mon 2:00-4:00','C202',500,'Monsoon',2025),(8,7,1,'Tue, Thu 08:30 - 10:00','C03',83,'Monsoon',2025),(9,8,1,'Fri, Sat 10:00 - 11:30','C02',61,'Monsoon',2025),(10,9,1,'Tue, Thu 08:30 - 10:00','C03',93,'Monsoon',2025),(11,10,1,'Fri, Sat 08:30 - 10:00','C101',64,'Monsoon',2025),(12,11,1,'Tue, Thu 10:00 - 11:30','C03',78,'Monsoon',2025),(13,12,1,'Mon, Wed 08:30 - 10:00','C11',63,'Monsoon',2025),(14,13,1,'Mon, Wed 08:30 - 10:00','A006',61,'Monsoon',2025),(15,14,1,'Tue, Thu 11:30 - 13:00','C01',64,'Monsoon',2025),(16,15,1,'Fri, Sat 08:30 - 10:00','A006',90,'Monsoon',2025),(17,16,1,'Tue, Thu 08:30 - 10:00','A006',69,'Monsoon',2025),(18,17,1,'Fri, Sat 10:00 - 11:30','C101',84,'Monsoon',2025),(19,18,1,'Mon, Wed 11:30 - 13:00','A005',99,'Monsoon',2025),(20,19,1,'Mon, Wed 11:30 - 13:00','C13',75,'Monsoon',2025),(21,20,1,'Mon, Wed 10:00 - 11:30','C03',82,'Monsoon',2025),(22,21,1,'Mon, Wed 11:30 - 13:00','C13',80,'Monsoon',2025),(23,22,1,'Tue, Thu 10:00 - 11:30','C03',62,'Monsoon',2025),(24,23,1,'Tue, Thu 08:30 - 10:00','C13',60,'Monsoon',2025),(25,24,1,'Mon, Wed 08:30 - 10:00','C102',72,'Monsoon',2025),(26,25,1,'Fri, Sat 08:30 - 10:00','C13',95,'Monsoon',2025),(27,26,1,'Mon, Wed 10:00 - 11:30','C02',79,'Monsoon',2025),(28,27,1,'Mon, Wed 10:00 - 11:30','B004',65,'Monsoon',2025),(29,28,1,'Fri, Sat 08:30 - 10:00','C02',93,'Monsoon',2025),(30,29,1,'Mon, Wed 10:00 - 11:30','B004',73,'Monsoon',2025),(31,30,1,'Mon, Wed 10:00 - 11:30','C201',93,'Monsoon',2025),(32,31,1,'Tue, Thu 10:00 - 11:30','C102',96,'Monsoon',2025),(33,32,1,'Mon, Wed 11:30 - 13:00','C12',68,'Monsoon',2025),(34,33,1,'Tue, Thu 11:30 - 13:00','C01',83,'Monsoon',2025),(35,34,1,'Tue, Thu 11:30 - 13:00','C12',93,'Monsoon',2025),(36,35,1,'Tue, Thu 08:30 - 10:00','C02',61,'Monsoon',2025),(37,37,1,'Tue, Thu 08:30 - 10:00','C13',65,'Monsoon',2025),(38,38,1,'Tue, Thu 11:30 - 13:00','C102',69,'Monsoon',2025),(39,39,1,'Tue, Thu 08:30 - 10:00','C11',62,'Monsoon',2025),(40,40,1,'Tue, Thu 11:30 - 13:00','C02',78,'Monsoon',2025),(41,41,1,'Mon, Wed 08:30 - 10:00','C02',70,'Monsoon',2025),(42,42,1,'Tue, Thu 08:30 - 10:00','A006',85,'Monsoon',2025),(43,43,1,'Mon, Wed 08:30 - 10:00','C12',63,'Monsoon',2025),(44,44,1,'Tue, Thu 11:30 - 13:00','B004',62,'Monsoon',2025),(45,45,1,'Mon, Wed 11:30 - 13:00','C12',73,'Monsoon',2025),(46,46,1,'Mon, Wed 10:00 - 11:30','A005',94,'Monsoon',2025),(47,47,1,'Fri, Sat 10:00 - 11:30','A005',81,'Monsoon',2025),(48,48,1,'Mon, Wed 11:30 - 13:00','C102',83,'Monsoon',2025),(49,49,1,'Mon, Wed 10:00 - 11:30','A005',68,'Monsoon',2025),(50,50,1,'Fri, Sat 10:00 - 11:30','C01',88,'Monsoon',2025),(51,51,1,'Tue, Thu 08:30 - 10:00','B004',67,'Monsoon',2025),(52,52,1,'Fri, Sat 08:30 - 10:00','C101',81,'Monsoon',2025),(53,53,1,'Mon, Wed 08:30 - 10:00','C03',73,'Monsoon',2025),(54,54,1,'Mon, Wed 10:00 - 11:30','A006',86,'Monsoon',2025),(55,55,1,'Tue, Thu 10:00 - 11:30','C12',63,'Monsoon',2025),(56,57,1,'Fri, Sat 08:30 - 10:00','B004',77,'Monsoon',2025),(57,58,1,'Tue, Thu 10:00 - 11:30','C201',100,'Monsoon',2025),(58,59,1,'Tue, Thu 08:30 - 10:00','A006',70,'Monsoon',2025),(59,60,1,'Fri, Sat 10:00 - 11:30','A006',63,'Monsoon',2025),(60,61,1,'Tue, Thu 11:30 - 13:00','C11',73,'Monsoon',2025),(61,62,1,'Mon, Wed 11:30 - 13:00','B004',100,'Monsoon',2025),(62,63,1,'Fri, Sat 08:30 - 10:00','A005',69,'Monsoon',2025),(63,64,1,'Fri, Sat 08:30 - 10:00','C11',75,'Monsoon',2025),(64,65,1,'Tue, Thu 11:30 - 13:00','A005',83,'Monsoon',2025),(65,66,1,'Tue, Thu 10:00 - 11:30','C11',93,'Monsoon',2025),(66,67,1,'Mon, Wed 11:30 - 13:00','C02',90,'Monsoon',2025),(67,68,1,'Tue, Thu 11:30 - 13:00','A006',92,'Monsoon',2025),(68,69,1,'Tue, Thu 11:30 - 13:00','B004',79,'Monsoon',2025),(69,70,1,'Fri, Sat 08:30 - 10:00','C02',83,'Monsoon',2025),(70,71,1,'Tue, Thu 10:00 - 11:30','C02',60,'Monsoon',2025),(71,72,1,'Tue, Thu 10:00 - 11:30','A006',88,'Monsoon',2025),(72,73,1,'Tue, Thu 10:00 - 11:30','A006',65,'Monsoon',2025),(73,74,1,'Mon, Wed 08:30 - 10:00','A005',75,'Monsoon',2025),(74,75,1,'Tue, Thu 08:30 - 10:00','B004',89,'Monsoon',2025),(75,76,1,'Tue, Thu 08:30 - 10:00','A005',89,'Monsoon',2025),(76,77,1,'Tue, Thu 11:30 - 13:00','C02',91,'Monsoon',2025),(77,78,1,'Mon, Wed 08:30 - 10:00','C02',99,'Monsoon',2025),(78,79,1,'Tue, Thu 08:30 - 10:00','A006',91,'Monsoon',2025),(79,80,1,'Mon, Wed 10:00 - 11:30','C01',82,'Monsoon',2025),(80,81,1,'Fri, Sat 10:00 - 11:30','A005',68,'Monsoon',2025),(81,82,1,'Tue, Thu 11:30 - 13:00','A006',90,'Monsoon',2025),(82,83,1,'Mon, Wed 10:00 - 11:30','C102',85,'Monsoon',2025),(83,84,1,'Tue, Thu 08:30 - 10:00','C13',69,'Monsoon',2025),(84,85,1,'Mon, Wed 10:00 - 11:30','C01',71,'Monsoon',2025),(85,86,1,'Fri, Sat 10:00 - 11:30','B004',87,'Monsoon',2025),(86,87,1,'Mon, Wed 08:30 - 10:00','B004',86,'Monsoon',2025),(87,88,1,'Mon, Wed 11:30 - 13:00','A006',74,'Monsoon',2025),(88,89,1,'Fri, Sat 10:00 - 11:30','C201',73,'Monsoon',2025),(89,90,1,'Fri, Sat 08:30 - 10:00','C12',80,'Monsoon',2025),(90,91,1,'Tue, Thu 08:30 - 10:00','C201',73,'Monsoon',2025),(91,92,1,'Tue, Thu 10:00 - 11:30','B004',71,'Monsoon',2025),(92,93,1,'Mon, Wed 10:00 - 11:30','A006',70,'Monsoon',2025),(93,94,1,'Fri, Sat 08:30 - 10:00','C12',61,'Monsoon',2025),(94,95,1,'Mon, Wed 11:30 - 13:00','C02',84,'Monsoon',2025),(95,96,1,'Fri, Sat 08:30 - 10:00','C102',83,'Monsoon',2025),(96,97,1,'Tue, Thu 08:30 - 10:00','C03',95,'Monsoon',2025),(97,98,1,'Tue, Thu 11:30 - 13:00','C03',82,'Monsoon',2025),(98,99,1,'Mon, Wed 11:30 - 13:00','C01',60,'Monsoon',2025),(99,100,1,'Fri, Sat 10:00 - 11:30','A006',73,'Monsoon',2025),(100,101,1,'Tue, Thu 10:00 - 11:30','A006',92,'Monsoon',2025),(101,102,1,'Tue, Thu 10:00 - 11:30','C12',79,'Monsoon',2025),(102,103,1,'Tue, Thu 10:00 - 11:30','C201',87,'Monsoon',2025),(103,104,1,'Tue, Thu 10:00 - 11:30','C13',67,'Monsoon',2025),(104,105,1,'Fri, Sat 08:30 - 10:00','C11',92,'Monsoon',2025),(105,3,2,'Mon, Wed 10:00-11:00','C201',50,'Monsoon',2025);
/*!40000 ALTER TABLE `sections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `settings` (
  `key` varchar(50) NOT NULL,
  `value` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES ('current_semester','Monsoon-2025'),('deadline','2025-11-26'),('deadline_Monsoon_2025','2025-11-28'),('global_deadline','2025-11-28'),('maintenance','false'),('maintenance_on','false');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `student_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `roll_no` varchar(20) NOT NULL,
  `program` varchar(50) DEFAULT NULL,
  `year` int DEFAULT NULL,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `roll_no` (`roll_no`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `students_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `auth_db`.`users_auth` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (1,2,'2024236','CSAI',2024),(2,5,'123456','cse',2025),(6,13,'2025001','CSE',2025),(7,14,'2025002','CSB',2025),(8,15,'2025003','CSSS',2025);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
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
