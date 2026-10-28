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
-- Table structure for table `passive_skill`
--

DROP TABLE IF EXISTS `passive_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `passive_skill` (
  `code` varchar(4) NOT NULL COMMENT '技能编码 P01~P10',
  `skill_name` varchar(32) NOT NULL COMMENT '技能名称',
  `effect_type` char(1) NOT NULL COMMENT '效果类型:A进攻增益 D防御增益 H治疗 R清除 N减益 B封锁 C复制 E反弹',
  `target` char(1) NOT NULL COMMENT 'S己方 E对手',
  `effect_value` int NOT NULL COMMENT '效果数值，百分比存整数如50',
  `duration_round` tinyint NOT NULL COMMENT '持续回合：0立即，1瞬发，2持续2回合',
  `effect_desc` varchar(200) NOT NULL COMMENT '效果文字说明',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='被动技能汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `passive_skill`
--

LOCK TABLES `passive_skill` WRITE;
/*!40000 ALTER TABLE `passive_skill` DISABLE KEYS */;
INSERT INTO `passive_skill` VALUES ('P01','微光','A','S',1,2,'己方主动牌伤害+1，持续2回合，不可叠加'),('P02','清心','R','E',0,0,'清除对手全部持续被动，立即生效'),('P03','淡息','N','E',1,1,'对手本回合进攻伤害-1，最低为0'),('P04','疾闪','B','E',0,1,'命中后封锁对手下一回合法术出牌权'),('P05','聒噪','A','S',1,2,'己方后续所有进攻牌伤害+1'),('P06','人鱼润愈','H','S',2,1,'回复己方2点血量，不超初始上限'),('P07','同调','C','E',0,1,'复制对手本回合法术瞬发效果'),('P08','水障','D','S',2,2,'己方护盾数值+2'),('P09','黏滞','N','E',50,2,'对手下一张进攻牌伤害减半(50%)'),('P10','祥光','E','E',1,2,'己方护盾承伤时反弹1点伤害给攻击者');
/*!40000 ALTER TABLE `passive_skill` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-28 13:25:48
