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
-- Table structure for table `card`
--

DROP TABLE IF EXISTS `card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `card` (
  `id` tinyint NOT NULL COMMENT '卡牌编号1‑24',
  `c_name` varchar(16) NOT NULL COMMENT '名字',
  `c_level` varchar(4) NOT NULL COMMENT '品级：黄/玄',
  `hp` tinyint NOT NULL COMMENT '血量',
  `skill_name` varchar(20) DEFAULT NULL COMMENT '技能名',
  `damage_or_shield` varchar(8) NOT NULL COMMENT '伤害点数(H护盾)，H1/H2，无',
  `passive_code` varchar(4) DEFAULT NULL COMMENT '被动技能编码，关联passive_skill.code，无则NULL',
  `counter_id` tinyint DEFAULT NULL COMMENT '克制对象卡牌id，引用本表card.id',
  `is_compound` tinyint NOT NULL DEFAULT '0' COMMENT '复合型判断：0否，1是',
  PRIMARY KEY (`id`),
  KEY `fk_card_passive` (`passive_code`),
  KEY `fk_card_counter` (`counter_id`),
  CONSTRAINT `fk_card_counter` FOREIGN KEY (`counter_id`) REFERENCES `card` (`id`),
  CONSTRAINT `fk_card_passive` FOREIGN KEY (`passive_code`) REFERENCES `passive_skill` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卡牌信息汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `card`
--

LOCK TABLES `card` WRITE;
/*!40000 ALTER TABLE `card` DISABLE KEYS */;
INSERT INTO `card` VALUES (1,'文文','黄',8,'鸣刺','1',NULL,10,0),(2,'狸力','黄',9,'掘撞','2',NULL,14,0),(3,'鮯鮯鱼','黄',9,'六足扑击','2',NULL,22,0),(4,'讙','黄',10,'三目冲击','3',NULL,9,0),(5,'孟槐','黄',10,'御凶盾','H1',NULL,12,0),(6,'旋龟','黄',11,'硬甲','H2',NULL,23,0),(7,'旄牛','黄',11,'厚皮','H2',NULL,16,0),(8,'鯈鱼','黄',8,'','无','P01',1,0),(9,'灌灌','黄',9,'','无','P02',2,0),(10,'茈鱼','黄',9,'','无','P03',15,0),(11,'䖪鼠','黄',8,'撕咬','1','P04',5,1),(12,'山膏','黄',10,'冲撞','2','P05',6,1),(13,'精卫','玄',12,'衔石击','5',NULL,20,0),(14,'妴胡','玄',13,'獠牙扑杀','5',NULL,18,0),(15,'狌狌','玄',13,'迅扑','5',NULL,19,0),(16,'狡','玄',14,'虎牙突击','6',NULL,17,0),(17,'鹿蜀','玄',14,'谣音护佑','H5',NULL,13,0),(18,'当康','玄',15,'丰穰之甲','H5',NULL,21,0),(19,'葱聋','玄',14,'羊角壁垒','H4',NULL,24,0),(20,'赤鱬','玄',12,'','无','P06',8,0),(21,'类','玄',13,'','无','P07',11,0),(22,'水马','玄',13,'踏浪冲击','4','P08',3,1),(23,'蛫','玄',12,'钳击','4','P09',12,1),(24,'鸾鸟','玄',15,'翎羽斩','6','P10',7,1);
/*!40000 ALTER TABLE `card` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-27  0:17:15
