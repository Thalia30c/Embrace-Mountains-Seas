-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: mountains_and_seas
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Table structure for table `game_rank`
--

DROP TABLE IF EXISTS `game_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_rank` (
  `rank_id` tinyint NOT NULL AUTO_INCREMENT COMMENT '段位顺序',
  `rank_name` varchar(16) NOT NULL COMMENT '段位名称',
  `sub_rank` varchar(8) NOT NULL COMMENT '小段',
  `need_total_score` int NOT NULL COMMENT '升级所需总积分(到本段满)',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rank_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='游戏等级段位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_rank`
--

LOCK TABLES `game_rank` WRITE;
/*!40000 ALTER TABLE `game_rank` DISABLE KEYS */;
INSERT INTO `game_rank` VALUES (1,'山稚','三段',0,'初始段位，不掉段，积分下限 0'),(2,'山稚','二段',100,'山稚三段→二段需要累计 100'),(3,'山稚','一段',200,'山稚二段→一段累计 200'),(4,'山精','三段',350,'山稚一段→山精三段累计 350'),(5,'山精','二段',500,'山精三段→二段累计 500'),(6,'山精','一段',650,'山精二段→一段累计 650'),(7,'山巫','三段',850,'山精一段→山巫三段累计 850'),(8,'山巫','二段',1050,'山巫三段→二段累计 1050'),(9,'山巫','一段',1250,'山巫二段→一段累计 1250'),(10,'灵狩','三段',1500,'山巫一段→灵狩三段累计 1500'),(11,'灵狩','二段',1750,'灵狩三段→二段累计 1750'),(12,'灵狩','一段',2000,'灵狩二段→一段累计 2000'),(13,'荒伯','三段',2300,'灵狩一段→荒伯三段累计 2300'),(14,'荒伯','二段',2600,'荒伯三段→二段累计 2600'),(15,'荒伯','一段',2900,'荒伯二段→一段累计 2900'),(16,'山君','三段',3250,'荒伯一段→山君三段累计 3250'),(17,'山君','二段',3600,'山君三段→二段累计 3600'),(18,'山君','一段',3950,'山君二段→一段累计 3950'),(19,'山海尊','一段',4500,'最高段位，无更高等级；积分溢出只累积，不再升级');
/*!40000 ALTER TABLE `game_rank` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-28 13:25:49
