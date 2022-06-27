CREATE DATABASE  IF NOT EXISTS `gameplay` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `gameplay`;
-- MySQL dump 10.13  Distrib 5.1.34, for apple-darwin9.5.0 (i386)
--
-- Host: meinc-db-2.crpubohacrga.us-east-1.rds.amazonaws.com    Database: gameplay
-- ------------------------------------------------------
-- Server version	5.1.50-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `metadata_relationship`
--

DROP TABLE IF EXISTS `metadata_relationship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata_relationship` (
  `metadata_relationship_id` int(11) NOT NULL AUTO_INCREMENT,
  `metadata_relationship_type_id` int(11) NOT NULL,
  `metadata_id_1` int(11) NOT NULL,
  `metadata_id_2` int(11) NOT NULL,
  PRIMARY KEY (`metadata_relationship_id`),
  KEY `fk_mdr_1` (`metadata_relationship_type_id`),
  KEY `fk_mdr_2` (`metadata_id_1`),
  KEY `fk_mdr_3` (`metadata_id_2`),
  CONSTRAINT `fk_mdr_1` FOREIGN KEY (`metadata_relationship_type_id`) REFERENCES `metadata_relationship_type` (`metadata_relationship_type_id`),
  CONSTRAINT `fk_mdr_2` FOREIGN KEY (`metadata_id_1`) REFERENCES `metadata` (`metadata_id`),
  CONSTRAINT `fk_mdr_3` FOREIGN KEY (`metadata_id_2`) REFERENCES `metadata` (`metadata_id`)
) ENGINE=InnoDB AUTO_INCREMENT=350 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadata_relationship`
--

LOCK TABLES `metadata_relationship` WRITE;
/*!40000 ALTER TABLE `metadata_relationship` DISABLE KEYS */;
INSERT INTO `metadata_relationship` VALUES (1,1,1,4),(2,1,1,5),(3,1,2,6),(4,1,2,7),(5,1,3,8),(6,1,3,9),(7,1,4,10),(8,1,4,11),(9,1,4,12),(10,1,5,13),(11,1,5,14),(12,1,5,15),(13,1,6,16),(14,1,6,17),(15,1,6,18),(16,1,6,19),(17,1,7,20),(18,1,7,21),(19,1,7,22),(20,1,7,23),(21,1,8,24),(22,1,8,25),(23,1,8,26),(24,1,9,27),(25,1,9,28),(26,1,9,29),(27,1,10,30),(28,1,10,31),(29,1,10,32),(30,1,10,33),(31,1,10,34),(32,1,11,35),(33,1,11,36),(34,1,11,37),(35,1,11,38),(36,1,11,39),(37,1,12,40),(38,1,12,41),(39,1,12,42),(40,1,12,43),(41,1,12,44),(42,1,13,45),(43,1,13,46),(44,1,13,47),(45,1,13,48),(46,1,13,49),(47,1,14,50),(48,1,14,51),(49,1,14,52),(50,1,14,53),(51,1,14,54),(52,1,15,55),(53,1,15,56),(54,1,15,57),(55,1,15,58),(56,1,15,59),(57,1,16,60),(58,1,16,61),(59,1,16,62),(60,1,16,63),(61,1,17,64),(62,1,17,65),(63,1,17,66),(64,1,17,67),(65,1,18,68),(66,1,18,69),(67,1,18,70),(68,1,18,71),(69,1,19,72),(70,1,19,73),(71,1,19,74),(72,1,19,75),(73,1,20,76),(74,1,20,77),(75,1,20,78),(76,1,20,79),(77,1,21,80),(78,1,21,81),(79,1,21,82),(80,1,21,83),(81,1,22,84),(82,1,22,85),(83,1,22,86),(84,1,22,87),(85,1,23,88),(86,1,23,89),(87,1,23,90),(88,1,23,91),(89,1,24,92),(90,1,24,93),(91,1,24,94),(92,1,24,95),(93,1,24,96),(94,1,25,97),(95,1,25,98),(96,1,25,99),(97,1,25,100),(98,1,25,101),(99,1,25,102),(100,1,26,103),(101,1,26,104),(102,1,26,105),(103,1,26,106),(104,1,26,107),(105,1,27,108),(106,1,27,109),(107,1,27,110),(108,1,27,111),(109,1,27,112),(110,1,28,113),(111,1,28,114),(112,1,28,115),(113,1,28,116),(114,1,28,117),(115,1,29,118),(116,1,29,119),(117,1,29,120),(118,1,29,121),(121,1,125,126),(122,2,126,127),(123,2,40,128),(124,2,30,129),(125,2,41,130),(126,2,35,131),(127,2,36,132),(128,2,45,133),(129,2,50,134),(130,2,37,135),(131,2,55,136),(132,2,46,137),(133,2,38,138),(134,2,57,139),(135,2,56,140),(136,2,47,141),(137,2,42,142),(138,2,39,143),(139,2,51,144),(140,2,31,145),(141,2,48,146),(142,2,32,147),(143,2,53,148),(144,2,43,149),(145,2,33,150),(146,2,58,151),(147,2,52,152),(148,2,59,153),(149,2,49,154),(150,2,34,155),(151,2,54,156),(152,2,44,157),(153,2,54,158),(154,2,53,159),(155,2,52,160),(156,2,51,161),(157,2,50,162),(158,2,55,163),(159,2,56,164),(160,2,57,165),(161,2,58,166),(163,2,59,168),(164,2,45,169),(165,2,46,170),(166,2,47,171),(167,2,48,172),(168,2,49,173),(169,2,40,174),(170,2,41,175),(171,2,42,176),(172,2,43,177),(173,2,44,178),(174,2,35,179),(175,2,36,180),(176,2,37,181),(177,2,38,182),(178,2,39,183),(179,2,30,184),(180,2,31,185),(181,2,32,186),(182,2,33,187),(183,2,34,188),(184,2,92,189),(185,2,93,190),(186,2,94,191),(187,2,95,192),(188,2,96,193),(189,2,97,194),(190,2,98,195),(191,2,99,196),(192,2,100,197),(193,2,101,198),(194,2,102,199),(195,2,103,200),(196,2,104,201),(197,2,105,202),(198,2,106,203),(199,2,107,204),(200,2,108,205),(201,2,109,206),(202,2,110,207),(203,2,111,208),(204,2,112,209),(205,2,113,210),(206,2,114,211),(207,2,115,212),(208,2,116,213),(209,2,117,214),(210,2,118,215),(211,2,119,216),(212,2,120,217),(213,2,121,218),(214,2,60,219),(215,2,61,220),(216,2,62,221),(217,2,63,222),(218,2,64,223),(219,2,65,224),(220,2,66,225),(221,2,67,226),(222,2,68,227),(223,2,69,228),(224,2,70,229),(225,2,71,230),(226,2,72,231),(227,2,73,232),(228,2,74,233),(229,2,75,234),(230,2,76,235),(231,2,77,236),(232,2,78,237),(233,2,79,238),(234,2,80,239),(235,2,81,240),(236,2,82,241),(237,2,83,242),(238,2,84,243),(239,2,85,244),(240,2,86,245),(241,2,87,246),(242,2,88,247),(243,2,89,248),(244,2,90,249),(245,2,91,250),(249,1,254,255),(250,2,255,256),(252,1,257,258),(253,2,258,260),(254,1,261,262),(255,1,262,263),(256,1,263,264),(257,2,264,265),(258,1,262,266),(259,1,266,267),(260,2,267,268),(261,1,262,269),(262,1,269,270),(263,2,270,271),(264,1,262,272),(265,1,272,273),(266,2,273,274),(267,1,262,275),(268,1,275,276),(269,2,276,277),(270,1,262,278),(272,1,278,280),(273,2,280,281),(274,1,275,282),(275,2,282,283),(276,1,262,284),(277,1,284,285),(278,2,285,286),(279,1,278,287),(280,2,287,288),(281,1,272,289),(282,2,289,290),(283,1,275,291),(284,2,291,292),(285,1,262,293),(286,1,293,294),(287,2,294,295),(288,1,262,296),(289,1,296,297),(290,2,297,298),(291,1,262,299),(292,1,299,300),(293,2,300,301),(295,1,262,303),(296,1,303,304),(297,2,304,305),(298,1,293,306),(299,2,306,307),(300,1,309,310),(301,2,310,311),(302,1,352,313),(303,2,313,314),(304,1,352,315),(305,2,315,316),(306,1,352,317),(307,2,317,318),(308,1,352,319),(309,2,319,320),(310,1,352,321),(311,2,321,322),(312,1,352,323),(313,2,323,324),(314,1,352,325),(315,2,325,326),(316,1,352,327),(317,2,327,328),(318,1,352,329),(319,2,329,330),(320,1,354,331),(321,2,331,332),(322,1,354,333),(323,2,333,334),(324,1,354,335),(325,2,335,336),(326,1,354,337),(327,2,337,338),(328,1,354,339),(329,2,339,340),(330,1,354,341),(331,2,341,342),(332,1,354,343),(333,2,343,344),(334,1,354,345),(335,2,345,346),(336,1,354,347),(337,2,347,348),(338,1,356,349),(339,2,349,350),(340,1,312,351),(341,1,351,352),(342,1,312,353),(343,1,353,354),(344,1,312,355),(345,1,355,356),(346,1,312,357),(347,1,357,358),(348,1,358,359),(349,2,359,360);
/*!40000 ALTER TABLE `metadata_relationship` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadata_type`
--

DROP TABLE IF EXISTS `metadata_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata_type` (
  `metadata_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`metadata_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadata_type`
--

LOCK TABLES `metadata_type` WRITE;
/*!40000 ALTER TABLE `metadata_type` DISABLE KEYS */;
INSERT INTO `metadata_type` VALUES (1,'Sports League'),(2,'Sports Conference'),(3,'Sports Team'),(4,'Logo 24x24'),(5,'Logo 64x64'),(6,'Entertainment'),(8,'Entertainment Item'),(10,'team-logo-77x56'),(11,'short_name'),(12,'Other'),(13,'Other Item'),(14,'College'),(15,'College Conference'),(16,'College Team'),(17,'Event');
/*!40000 ALTER TABLE `metadata_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadata_relationship_type`
--

DROP TABLE IF EXISTS `metadata_relationship_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata_relationship_type` (
  `metadata_relationship_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`metadata_relationship_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadata_relationship_type`
--

LOCK TABLES `metadata_relationship_type` WRITE;
/*!40000 ALTER TABLE `metadata_relationship_type` DISABLE KEYS */;
INSERT INTO `metadata_relationship_type` VALUES (1,'parent-child'),(2,'attribute');
/*!40000 ALTER TABLE `metadata_relationship_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadata`
--

DROP TABLE IF EXISTS `metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata` (
  `metadata_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `metadata_type_id` int(11) NOT NULL,
  `order` int(11) NOT NULL DEFAULT '1',
  `active` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`metadata_id`),
  KEY `fk_md_1` (`metadata_type_id`),
  CONSTRAINT `fk_md_1` FOREIGN KEY (`metadata_type_id`) REFERENCES `metadata_type` (`metadata_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=361 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadata`
--

LOCK TABLES `metadata` WRITE;
/*!40000 ALTER TABLE `metadata` DISABLE KEYS */;
INSERT INTO `metadata` VALUES (1,'NBA',1,1,1),(2,'NFL',1,2,1),(3,'MLB',1,3,1),(4,'Eastern Conference',2,1,1),(5,'Western Conference',2,2,1),(6,'American Football Conference',2,1,1),(7,'National Football Conference',2,2,1),(8,'National League',2,1,1),(9,'American League',2,1,1),(10,'Atlantic',2,1,1),(11,'Central',2,2,1),(12,'Southeast',2,3,1),(13,'Southwest',2,1,1),(14,'Northwest',2,2,1),(15,'Pacific',2,3,1),(16,'AFC East',2,1,1),(17,'AFC North',2,2,1),(18,'AFC South',2,3,1),(19,'AFC West',2,4,1),(20,'NFC East',2,1,1),(21,'NFC North',2,2,1),(22,'NFC South',2,3,1),(23,'NFC West',2,4,1),(24,'NL East',2,1,1),(25,'NL Central',2,2,1),(26,'NL West',2,3,1),(27,'AL East',2,1,1),(28,'ALCentral',2,2,1),(29,'AL West',2,3,1),(30,'Boston Celtics',3,1,1),(31,'New Jersey Nets',3,2,1),(32,'New York Knicks',3,3,1),(33,'Philadelphia 76ers',3,4,1),(34,'Toronto Raptors',3,5,1),(35,'Chicago Bulls',3,1,1),(36,'Cleveland Cavaliers',3,2,1),(37,'Detroit Pistons',3,3,1),(38,'Indiana Pacers',3,4,1),(39,'Milwaukee Bucks',3,5,1),(40,'Atlanta Hawks',3,1,1),(41,'Charlotte Bobcats',3,2,1),(42,'Miami Heat',3,3,1),(43,'Orlando Magic',3,4,1),(44,'Washington Wizards',3,5,1),(45,'Dallas Mavericks',3,1,1),(46,'Houston Rockets',3,2,1),(47,'Memphis Grizzlies',3,3,1),(48,'New Orleans Hornets',3,4,1),(49,'San Antonio Spurs',3,5,1),(50,'Denver Nuggets',3,1,1),(51,'Minnesota Timberwolves',3,2,1),(52,'Portland Trail Blazers',3,3,1),(53,'Oklahoma City Thunder',3,4,1),(54,'Utah Jazz',3,5,1),(55,'Golden State Warriors',3,1,1),(56,'Los Angeles Lakers',3,2,1),(57,'Los Angeles Clippers',3,3,1),(58,'Phoenix Suns',3,4,1),(59,'Sacramento Kings',3,5,1),(60,'New England Patriots',3,1,1),(61,'New York Jets',3,2,1),(62,'Miami Dolphins',3,3,1),(63,'Buffalo Bills',3,4,1),(64,'Pittsburgh Steelers',3,1,1),(65,'Baltimore Ravens',3,2,1),(66,'Cleveland Browns',3,3,1),(67,'Cincinnati Bengals',3,4,1),(68,'Indianapolis Colts',3,1,1),(69,'Jacksonville Jaguars',3,2,1),(70,'Houston Texans',3,3,1),(71,'Tennessee Titans',3,4,1),(72,'Kansas City Chiefs',3,1,1),(73,'San Diego Chargers',3,2,1),(74,'Oakland Raiders',3,3,1),(75,'Denver Broncos',3,4,1),(76,'Philadelphia Eagles',3,1,1),(77,'New York Giants',3,2,1),(78,'Dallas Cowboys',3,3,1),(79,'Washington Redskins',3,4,1),(80,'Chicago Bears',3,1,1),(81,'Green Bay Packers',3,2,1),(82,'Detroit Lions',3,3,1),(83,'Minnesota Vikings',3,4,1),(84,'Atlanta Falcons',3,1,1),(85,'New Orleans Saints',3,2,1),(86,'Tampa Bay Buccaneers',3,3,1),(87,'Carolina Panthers',3,4,1),(88,'Seattle Seahawks',3,1,1),(89,'St. Louis Rams',3,2,1),(90,'San Francisco 49ers',3,3,1),(91,'Arizona Cardinals',3,4,1),(92,'Atlanta Braves',3,1,1),(93,'Florida Marlins',3,2,1),(94,'New York Mets',3,3,1),(95,'Washington Nationals',3,4,1),(96,'Philadelphia Phillies',3,5,1),(97,'Houston Astros',3,1,1),(98,'Milwaukee Brewers',3,2,1),(99,'St. Louis Cardinals',3,3,1),(100,'Chicago Cubs',3,4,1),(101,'Pittsburgh Pirates',3,5,1),(102,'Cincinnati Reds',3,6,1),(103,'Arizona Diamondbacks',3,1,1),(104,'Los Angeles Dodgers',3,2,1),(105,'San Francisco Giants',3,3,1),(106,'San Diego Padres',3,4,1),(107,'Colorado Rockies',3,5,1),(108,'Toronto Blue Jays',3,1,1),(109,'Baltimore Orioles',3,2,1),(110,'Tampa Bay Rays',3,3,1),(111,'Boston Red Sox',3,4,1),(112,'New York Yankees',3,5,1),(113,'Cleveland Indians',3,1,1),(114,'Kansas City Royals',3,2,1),(115,'Detroit Tigers',3,3,1),(116,'Minnesota Twins',3,4,1),(117,'Chicago White Sox',3,5,1),(118,'Los Angeles Angels',3,1,1),(119,'Oakland Athletics',3,2,1),(120,'Seattle Mariners',3,3,1),(121,'Texas Rangers',3,4,1),(125,'Awards Show',6,1,1),(126,'Oscars',8,1,1),(127,'oscars.png',10,1,1),(128,'AtlantaHawks.png',10,1,1),(129,'BostonCeltics.png',10,1,1),(130,'CharlotteBobcats.png',10,1,1),(131,'ChicagoBulls.png',10,1,1),(132,'ClevelandCavaliers.png',10,1,1),(133,'DallasMavericks.png',10,1,1),(134,'DenverNuggets.png',10,1,1),(135,'DetroitPistons.png',10,1,1),(136,'GoldenStateWarriors.png',10,1,1),(137,'HoustonRockets.png',10,1,1),(138,'IndianaPacers.png',10,1,1),(139,'LosAngelesClippers.png',10,1,1),(140,'LosAngelesLakers.png',10,1,1),(141,'MemphisGrizzlies.png',10,1,1),(142,'MiamiHeat.png',10,1,1),(143,'MilwaukeeBucks.png',10,1,1),(144,'MinnesotaTimberwolves.png',10,1,1),(145,'NewJerseyNets.png',10,1,1),(146,'NewOrleansHornets.png',10,1,1),(147,'NewYorkKnicks.png',10,1,1),(148,'OklahomaCityThunder.png',10,1,1),(149,'OrlandoMagic.png',10,1,1),(150,'Philadelphia76ers.png',10,1,1),(151,'PhoenixSuns.png',10,1,1),(152,'PortlandTrailBlazers.png',10,1,1),(153,'SacramentoKings.png',10,1,1),(154,'SanAntonioSpurs.png',10,1,1),(155,'TorontoRaptors.png',10,1,1),(156,'UtahJazz.png',10,1,1),(157,'WashingtonWizards.png',10,1,1),(158,'Salt Lake',11,2,1),(159,'Oklahoma',11,2,1),(160,'Portland',11,2,1),(161,'Minneapolis',11,2,1),(162,'Denver',11,2,1),(163,'Oakland',11,2,1),(164,'Los Angeles',11,2,1),(165,'Los Angeles',11,2,1),(166,'Phoenix',11,2,1),(168,'Sacramento',11,2,1),(169,'Dallas',11,2,1),(170,'Houston',11,2,1),(171,'Memphis',11,2,1),(172,'New Orleans',11,2,1),(173,'San Antonio',11,2,1),(174,'Atlanta',11,2,1),(175,'Charlotte',11,2,1),(176,'Miami',11,2,1),(177,'Orlando',11,2,1),(178,'Washington, D.C.',11,2,1),(179,'Chicago',11,2,1),(180,'Cleveland',11,2,1),(181,'Detroit',11,2,1),(182,'Indianapolis',11,2,1),(183,'Milwaukee',11,2,1),(184,'Boston',11,2,1),(185,'New Jersey',11,2,1),(186,'New York',11,2,1),(187,'Philadelphia',11,2,1),(188,'Toronto',11,2,1),(189,'AtlantaBraves.png',10,1,1),(190,'FloridaMarlins.png',10,1,1),(191,'NewYorkMets.png',10,1,1),(192,'WashingtonNationals.png',10,1,1),(193,'PhiladelphiaPhillies.png',10,1,1),(194,'HoustonAstros.png',10,1,1),(195,'MilwaukeeBrewers.png',10,1,1),(196,'StLouisCardinals.png',10,1,1),(197,'ChicagoCubs.png',10,1,1),(198,'PittsburghPirates.png',10,1,1),(199,'CincinnatiReds.png',10,1,1),(200,'ArizonaDiamondBacks.png',10,1,1),(201,'LosAngelesDodgers.png',10,1,1),(202,'SanFranciscoGiants.png',10,1,1),(203,'SanDiegoPadres.png',10,1,1),(204,'ColoradoRockies.png',10,1,1),(205,'TorontoBlueJays.png',10,1,1),(206,'BaltimoreOrioles.png',10,1,1),(207,'TampaBayRays.png',10,1,1),(208,'BostonRedSox.png',10,1,1),(209,'NewYorkYankees.png',10,1,1),(210,'ClevelandIndians.png',10,1,1),(211,'KansasCityRoyals.png',10,1,1),(212,'DetroitTigers.png',10,1,1),(213,'MinnesotaTwins.png',10,1,1),(214,'ChicagoWhiteSox.png',10,1,1),(215,'LosAngelesAngels.png',10,1,1),(216,'OaklandAthletics.png',10,1,1),(217,'SeattleMariners.png',10,1,1),(218,'TexasRangers.png',10,1,1),(219,'NewEnglandPatriots.png',10,1,1),(220,'NewYorkJets.png',10,1,1),(221,'MiamiDolphins.png',10,1,1),(222,'BuffaloBills.png',10,1,1),(223,'PittsburghSteelers.png',10,1,1),(224,'BaltimoreRavens.png',10,1,1),(225,'ClevelandBrowns.png',10,1,1),(226,'CincinnatiBengals.png',10,1,1),(227,'IndianapolisColts.png',10,1,1),(228,'JacksonvilleJaguars.png',10,1,1),(229,'HoustonTexans.png',10,1,1),(230,'TennesseeTitans.png',10,1,1),(231,'KansasCityChiefs.png',10,1,1),(232,'SanDiegoChargers.png',10,1,1),(233,'OaklandRaiders.png',10,1,1),(234,'DenverBroncos.png',10,1,1),(235,'PhiladelphiaEagles.png',10,1,1),(236,'NewYorkGiants.png',10,1,1),(237,'DallasCowboys.png',10,1,1),(238,'WashingtonRedskins.png',10,1,1),(239,'ChicagoBears.png',10,1,1),(240,'GreenBayPackers.png',10,1,1),(241,'DetroitLions.png',10,1,1),(242,'MinnesotaVikings.png',10,1,1),(243,'AtlantaFalcons.png',10,1,1),(244,'NewOrleansSaints.png',10,1,1),(245,'TampaBayBuccaneers.png',10,1,1),(246,'CarolinaPanthers.png',10,1,1),(247,'SeattleSeahawks.png',10,1,1),(248,'StLouisRams.png',10,1,1),(249,'SanFrancisco49ers.png',10,1,1),(250,'ArizonaCardinals.png',10,1,1),(254,'Racing',12,1,1),(255,'NASCAR',13,1,1),(256,'NascarSprintCup.png',10,1,1),(257,'Golf',1,4,1),(258,'PGA',3,1,1),(259,'pga.png',10,1,1),(260,'PGA.png',10,1,1),(261,'Men\'s Basketball',14,1,1),(262,'Division 1',15,1,1),(263,'Pacific Twelve',15,1,1),(264,'Arizona',16,1,1),(265,'Arizona.png',10,1,1),(266,'Horizon League',15,2,1),(267,'Butler',16,1,1),(268,'Butler.png',10,1,1),(269,'West Coast',15,3,1),(270,'BYU',16,1,1),(271,'BYU.png',10,1,1),(272,'Big East',15,4,1),(273,'Connecticut',16,1,1),(274,'Connecticut.png',10,1,1),(275,'ACC',15,5,1),(276,'Duke',16,1,1),(277,'Duke.png',10,1,1),(278,'SEC',15,6,1),(280,'Florida',16,1,1),(281,'Florida.png',10,1,1),(282,'Florida State',16,2,1),(283,'FloridaState.png',10,1,1),(284,'Big 12',15,7,1),(285,'Kansas',16,1,1),(286,'Kansas.png',10,1,1),(287,'Kentucky',16,2,1),(288,'Kentucky.png',10,1,1),(289,'Marquette',16,2,1),(290,'Marquette.png',10,1,1),(291,'North Carolina',16,3,1),(292,'NorthCarolina.png',10,1,1),(293,'Big Ten',15,8,1),(294,'Ohio State',16,1,1),(295,'OhioState.png',10,1,1),(296,'Atlantic 10',15,9,1),(297,'Richmond',16,1,1),(298,'Richmond.png',10,1,1),(299,'Mountain West',15,10,1),(300,'San Diego State',16,1,1),(301,'SanDiegoState.png',10,1,1),(303,'Colonial Athletic',15,11,1),(304,'VCU',16,1,1),(305,'VCU.png',10,1,1),(306,'Wisconsin',16,2,1),(307,'Wisconsin.png',10,1,1),(308,'NFL Draft',3,1,1),(309,'Sports',12,2,1),(310,'NFL Draft',13,1,1),(311,'NflDraft.png',10,1,1),(312,'MLS',1,5,1),(313,'Chicago Fire',3,1,1),(314,'ChicagoFire.png',10,1,1),(315,'Columbus Crew',3,2,1),(316,'ColumbusCrew.png',10,1,1),(317,'D.C. United',3,3,1),(318,'DCUnited.png',10,1,1),(319,'Houston Dynamo',3,4,1),(320,'HoustonDynamo.png',10,1,1),(321,'New England Revolution',3,5,1),(322,'NewEnglandRevolution.png',10,1,1),(323,'New York Red Bulls',3,6,1),(324,'RedBullNewYork.png',10,1,1),(325,'Philadelphia Union',3,7,1),(326,'PhiladelphiaUnion.png',10,1,1),(327,'Sporting Kansas City',3,8,1),(328,'SportingKansasCity.png',10,1,1),(329,'Toronto FC',3,9,1),(330,'TorontoFC.png',10,1,1),(331,'Chivas USA',3,1,1),(332,'CDChivas.png',10,1,1),(333,'Colorado Rapids',3,2,1),(334,'ColoradoRapids.png',10,1,1),(335,'FC Dallas',3,3,1),(336,'FCDallas.png',10,1,1),(337,'Los Angeles Galaxy',3,4,1),(338,'LAGalaxy.png',10,1,1),(339,'Portland Timbers',3,5,1),(340,'PortlandTimbers.png',10,1,1),(341,'Real Salt Lake',3,6,1),(342,'RealSaltLake.png',10,1,1),(343,'San Jose Earthquakes',3,7,1),(344,'SanJoseEarthquakes.png',10,1,1),(345,'Seattle Sounders FC',3,8,1),(346,'SeattleSoundersFC.png',10,1,1),(347,'Vancouver Whitecaps FC',3,9,1),(348,'VancouverWhitecapsFC.png',10,1,1),(349,'Montreal',3,1,1),(350,'MontrealMLS2012.png',10,1,1),(351,'Eastern',2,1,1),(352,'Eastern Conference',2,1,1),(353,'Western',2,2,1),(354,'Western Conference',2,1,1),(355,'Expansion',2,3,1),(356,'Expansion',2,1,1),(357,'FIFA',2,4,1),(358,'FIFA',2,1,1),(359,'Monterrey Mexico',3,1,1),(360,'MonterreyMexico.png',10,1,1);
/*!40000 ALTER TABLE `metadata` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-05-11  4:56:09
