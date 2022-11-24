-- MySQL dump 10.13  Distrib 8.0.31, for macos12 (x86_64)
--
-- Host: 127.0.0.1    Database: test
-- ------------------------------------------------------
-- Server version	8.0.31

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `extension` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1595600006978707457,'张三','{\"age\": 25, \"sex\": \"男\", \"addr\": \"上海浦东\", \"city\": \"上海\", \"phone\": \"131\", \"family\": [\"dad\", \"mum\"]}'),(1595600007066787841,'李四','{\"age\": 25, \"sex\": \"男\", \"addr\": \"上海浦东\", \"city\": \"上海\", \"phone\": \"131\", \"family\": [\"dad\", \"mum\"]}'),(1595600007070982145,'王五','{\"age\": 27, \"sex\": \"男\", \"addr\": \"北京海淀\", \"city\": \"北京\", \"phone\": \"131\", \"family\": [\"dad\", \"mum\"]}'),(1595600007070982146,'小红','{\"age\": 18, \"sex\": \"女\", \"addr\": \"北京海淀\", \"city\": \"北京\", \"phone\": \"131\", \"family\": [\"dad\", \"mum\"]}'),(1595600007070982147,'小红1','{\"age\": 18, \"sex\": \"女\", \"addr\": \"北京海淀\", \"city\": \"北京\", \"phone\": \"131\", \"family\": [\"dad\", \"mum\"]}'),(1595600007070982148,'小红1','{\"age\": 18, \"sex\": \"女\", \"addr\": \"上海浦西\", \"city\": \"上海\", \"phone\": \"131\", \"family\": [\"dad\", \"mum\"]}'),(1595600007070982149,'小红11','{\"age\": 28, \"sex\": \"女\", \"addr\": \"上海浦西\", \"city\": \"上海\", \"phone\": \"131\", \"family\": [\"dad\", \"mum\"]}');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-11-24 14:04:49
