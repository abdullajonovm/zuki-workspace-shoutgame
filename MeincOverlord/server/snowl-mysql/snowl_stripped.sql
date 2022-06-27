CREATE DATABASE  IF NOT EXISTS `notification` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `notification`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: notification
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `callback`
--

DROP TABLE IF EXISTS `callback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `callback` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `namespace` varchar(255) DEFAULT NULL,
  `service_name` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  `method_name` varchar(767) NOT NULL,
  `notification_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `notification_type` (`notification_type`),
  KEY `namespace` (`namespace`),
  KEY `service_name` (`service_name`),
  KEY `version` (`version`),
  KEY `method_name` (`method_name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `callback`
--

LOCK TABLES `callback` WRITE;
/*!40000 ALTER TABLE `callback` DISABLE KEYS */;
/*!40000 ALTER TABLE `callback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(50) CHARACTER SET utf8mb4 NOT NULL,
  `context_id` int(11) unsigned NOT NULL DEFAULT '0',
  `sender` int(10) unsigned NOT NULL,
  `recipient` int(10) unsigned NOT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 NOT NULL,
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `action_type` varchar(50) CHARACTER SET utf8mb4 NOT NULL,
  `action_taken` varchar(50) CHARACTER SET utf8mb4 DEFAULT NULL,
  `payload` mediumtext CHARACTER SET utf8mb4 NOT NULL,
  `extras` text CHARACTER SET utf8mb4,
  `created` datetime NOT NULL,
  `last_updated_by` int(10) unsigned NOT NULL,
  `last_updated` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `sender` (`sender`),
  KEY `recipient` (`recipient`),
  KEY `status` (`status`),
  KEY `action_type` (`action_type`),
  KEY `action_taken` (`action_taken`),
  KEY `created` (`created`),
  KEY `last_updated_by` (`last_updated_by`),
  KEY `last_updated` (`last_updated`),
  KEY `context_id` (`context_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3409777 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pref`
--

DROP TABLE IF EXISTS `pref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pref` (
  `pref_type` int(10) unsigned NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `value` varchar(25) NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  PRIMARY KEY (`pref_type`,`subscriber_id`),
  KEY `created` (`created`),
  KEY `last_updated` (`last_updated`),
  KEY `value` (`value`),
  KEY `subscriber_id` (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pref`
--

LOCK TABLES `pref` WRITE;
/*!40000 ALTER TABLE `pref` DISABLE KEYS */;
/*!40000 ALTER TABLE `pref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pref_types`
--

DROP TABLE IF EXISTS `pref_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pref_types` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(60) NOT NULL,
  `description` varchar(255) NOT NULL,
  `possible_values` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pref_types`
--

LOCK TABLES `pref_types` WRITE;
/*!40000 ALTER TABLE `pref_types` DISABLE KEYS */;
INSERT INTO `pref_types` (`id`, `name`, `description`, `possible_values`) VALUES ('13', 'SM_ROUND_START', 'Shout Millionaire - Round Start', 'SMS,EMAIL,APP_PUSH,NONE');
/*!40000 ALTER TABLE `pref_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'snowyowl'
--

--
-- Dumping routines for database 'snowyowl'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

CREATE TABLE `snowyowl`.`subscriber_game_questions` (
  `game_id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  PRIMARY KEY (`subscriber_id`, `question_id`),
  INDEX `index2` (`game_id` ASC));

CREATE TABLE `snowyowl`.`subscriber_stats` (
  `subscriber_id` INT NOT NULL,
  `games_played` INT NOT NULL DEFAULT 0,
  `bracket_rounds_played` INT NOT NULL DEFAULT 0,
  `pool_rounds_played` INT NOT NULL DEFAULT 0,
  `questions_answered` INT NOT NULL DEFAULT 0,
  `questions_correct` INT NOT NULL DEFAULT 0,
  `cumulative_question_score` INT NOT NULL DEFAULT 0,
  `affiliate_plan_id` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`subscriber_id`));

CREATE TABLE `snowyowl`.`payout_table` (
  `game_id` CHAR(36) NOT NULL,
  `row_id` CHAR(36) NOT NULL,
  `rank_from` INT NOT NULL,
  `rank_to` INT NOT NULL,
  `amount` FLOAT NOT NULL,
  PRIMARY KEY (`game_id`, `row_id`));

CREATE TABLE `snowyowl`.`socket_io_log` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT NULL,
  `message_type` VARCHAR(45) NOT NULL,
  `message` TEXT NULL,
  `status` VARCHAR(45) NOT NULL,
  `sent_date` TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `snowyowl`.`tie_breaker_question` (
  `game_id` CHAR(36) NOT NULL,
  `match_id` CHAR(36) NOT NULL,
  `winner_subscriber_id` INT UNSIGNED NULL,
  PRIMARY KEY (`game_id`, `match_id`));

-- Dump completed on 2017-08-15 14:35:48
CREATE DATABASE  IF NOT EXISTS `contest` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `contest`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: contest
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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

CREATE TABLE `contest`.`bank_account` (
  `id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `bank_name` VARCHAR(128) NOT NULL,
  `checking_account_name` VARCHAR(128) NOT NULL,
  `routing_number` VARCHAR(45) NOT NULL,
  `account_number` VARCHAR(45) NOT NULL,
  `create_date` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `update_date` TIMESTAMP(6) NULL,
  PRIMARY KEY (`id`));

CREATE TABLE `contest`.`coupon_batch` (
  `batch_id` INT NOT NULL AUTO_INCREMENT,
  `batch_name` VARCHAR(255) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `create_date` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `expire_date` TIMESTAMP(6) NULL,
  PRIMARY KEY (`batch_id`));

CREATE TABLE `contest`.`coupon_code` (
  `coupon_id` INT NOT NULL AUTO_INCREMENT,
  `coupon_code` CHAR(36) NOT NULL,
  `batch_id` INT(11) UNSIGNED NOT NULL,
  `amount` DOUBLE NOT NULL,
  `create_date` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `expire_date` TIMESTAMP(6) NULL,
  `cancelled` INT NOT NULL DEFAULT 0,
  `cancelled_date` TIMESTAMP(6) NULL,
  `redeemed_by_subscriber_id` INT UNSIGNED NULL,
  `redeemed_date` TIMESTAMP(6) NULL,
  PRIMARY KEY (`coupon_id`));

CREATE TABLE `contest`.`min_age_by_region` (
  `country_region` CHAR(5) NOT NULL,
  `min_age` INT NOT NULL,
  PRIMARY KEY (`country_region`));

INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_AK',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_AL',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_AR',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_AZ',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_CA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_CO',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_CT',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_DC',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_DE',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_FL',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_GA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_HI',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_IA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_ID',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_IL',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_IN',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_KS',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_KY',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_LA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_MA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_MD',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_ME',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_MI',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_MN',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_MO',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_MS',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_MT',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_NC',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_ND',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_NE',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_NH',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_NJ',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_NM',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_NV',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_NY',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_OH',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_OK',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_OR',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_PA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_RI',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_SC',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_SD',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_TN',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_TX',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_UT',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_VA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_VT',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_WA',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_WI',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_WV',18);
INSERT INTO `contest`.`min_age_by_region` (`country_region`,`min_age`) VALUES ('US_WY',18);

--
-- Table structure for table `cash_pool_transaction2`
--

DROP TABLE IF EXISTS `cash_pool_transaction2`;

CREATE TABLE contest.`cash_pool_transaction2` (
  `cashpool_transaction_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `subscriber_id` int(10) unsigned NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `type` varchar(128) NOT NULL,
  `description` TEXT NULL,
  `current_pool_amount` decimal(15,2) NOT NULL DEFAULT 0,
  `current_bonus_amount` decimal(15,2) NOT NULL DEFAULT 0,
  `used_pool_amount` decimal(15,2) NULL,
  `used_bonus_amount` decimal(15,2) NULL,
  `receipt_id` int(10) unsigned DEFAULT NULL COMMENT 'if this is a purchase, the store.receipt id',
  `context_uuid` char(36) DEFAULT NULL COMMENT 'the game_uuid or round_uuid, if applicable',
  `transaction_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cashpool_transaction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=307 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `contest`.`manual_redeem_request` (
  `manual_redeem_request_id` INT NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT NOT NULL,
  `amount` DECIMAL(15,2) NOT NULL,
  `request_date` TIMESTAMP NOT NULL,
  `fulfilled_date` TIMESTAMP NULL,
  `cancelled_date` TIMESTAMP NULL,
  PRIMARY KEY (`manual_redeem_request_id`));

CREATE TABLE `snowyowl`.`game_payout` (
  `game_id` CHAR(36) NOT NULL,
  `payout_model_id` INT UNSIGNED NOT NULL,
  `minimum_payout_amount` DECIMAL(10,2) NOT NULL,
  `give_sponsor_player_winnings_back_to_sponsor` INT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`game_id`));

CREATE TABLE `snowyowl`.`payout_model` (
  `payout_model_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `base_player_count` VARCHAR(45) NOT NULL,
  `entrance_fee_amount` DECIMAL(10,2) NOT NULL,
  `active` INT(1) NOT NULL DEFAULT 1,
  `deactivation_reason` TEXT NULL,
  `creator_id` INT(11) NOT NULL,
  `deactivator_id` INT(11) NULL,
  `create_date` TIMESTAMP NOT NULL,
  `deactivated_date` TIMESTAMP NULL,
  PRIMARY KEY (`payout_model_id`));

CREATE TABLE `snowyowl`.`payout_model_round` (
  `payout_model_id` INT NOT NULL,
  `sort_order` INT NOT NULL,
  `description` VARCHAR(128) NOT NULL,
  `starting_player_count` INT NOT NULL,
  `eliminated_player_count` INT NOT NULL,
  `eliminated_payout_amount` DECIMAL(10,2) NOT NULL,
  `type` VARCHAR(45) NOT NULL DEFAULT 'CASH',
  `category` VARCHAR(45) NOT NULL DEFAULT 'PHYSICAL',
  PRIMARY KEY (`payout_model_id`, `sort_order`),
  CONSTRAINT `payout_model_round_fk1`
    FOREIGN KEY (`payout_model_id`)
    REFERENCES `snowyowl`.`payout_model` (`payout_model_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE `snowyowl`.`affiliate_plan` (
  `affiliate_plan_id` INT NOT NULL AUTO_INCREMENT,
  `current` INT NOT NULL,
  `affiliate_initial_payout_pct` DECIMAL(10,2) NOT NULL,
  `affiliate_returning_payout_pct` DECIMAL(10,2) NOT NULL,
  `player_initial_payout_pct` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`affiliate_plan_id`));

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game` (
  `id` char(36) NOT NULL,
  `game_type` varchar(45) NOT NULL DEFAULT 'DEFAULT',
  `game_engine` varchar(45) NOT NULL,
  `engine_type` VARCHAR(45) NOT NULL,
  `producer` VARCHAR(400) NULL,
  `game_photo_url` varchar(512) DEFAULT NULL,
  `game_status` varchar(45) NOT NULL,
  `bracket_elimination_count` int(11) DEFAULT NULL,
  `allow_bots` int(1) NOT NULL DEFAULT 1,
  `use_doctored_time_for_bots` INT(1) NOT NULL DEFAULT 1,
  `fill_with_bots` INT(1) NOT NULL DEFAULT 0,
  `max_bot_fill_count` INT UNSIGNED NULL,
  `pair_immediately` INT(1) NOT NULL DEFAULT 0,
  `can_appear_in_mobile` INT(1) NOT NULL DEFAULT 1,
  `production_game` INT(1) NOT NULL DEFAULT 1,
  `private_game` INT(1) NOT NULL DEFAULT 0,
  `invite_code` CHAR(6) NULL,
  `include_activity_answers_before_scoring` INT(1) NOT NULL DEFAULT 0,
  `starting_lives_count` INT NOT NULL DEFAULT 1,
  `additional_life_cost` DECIMAL(7,2) NOT NULL DEFAULT 0.0,
  `max_lives_count` INT NOT NULL DEFAULT 1,
  `guide_url` VARCHAR(256) NULL,
  `auto_start_pool_play` INT(1) NOT NULL DEFAULT 0,
  `auto_start_bracket_play` INT(1) NOT NULL DEFAULT 0,
  `auto_bracket_play_pre_start_notification_time_ms` INT NOT NULL DEFAULT 0,
  `pending_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cancelled_date` timestamp NULL DEFAULT NULL,
  `open_date` timestamp NULL DEFAULT NULL,
  `inplay_date` timestamp NULL DEFAULT NULL,
  `closed_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `game_status_idx` (`game_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

CREATE TABLE `contest`.`min_age_by_region` (
  `country_region` CHAR(5) NOT NULL,
  `min_age` INT NOT NULL,
  PRIMARY KEY (`country_region`));

CREATE TABLE `snowyowl`.`ineligible_subscribers` (
  `is_id` INT NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `email` VARCHAR(256) NOT NULL,
  `linked_subscriber_id` INT UNSIGNED NULL,
  `linked_email` VARCHAR(256) NULL,
  `reason` VARCHAR(45) NOT NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`is_id`));

CREATE TABLE `snowyowl`.`prohibited_subscribers` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `email` VARCHAR(256) NOT NULL,
  `reason` VARCHAR(45) NOT NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`subscriber_id`));

CREATE TABLE `snowyowl`.`subscriber_action_log` (
  `subscriber_action_log_id` INT NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT NOT NULL,
  `action` VARCHAR(45) NOT NULL,
  `reason` VARCHAR(45) NULL,
  `note` VARCHAR(512) NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`subscriber_action_log_id`));

--
-- Table structure for table `game_app_ids`
--

DROP TABLE IF EXISTS `game_app_ids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_app_ids` (
  `game_id` char(36) NOT NULL,
  `app_id` int(11) NOT NULL,
  PRIMARY KEY (`game_id`,`app_id`),
  CONSTRAINT `game_app_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_forbidden_country_codes`
--

DROP TABLE IF EXISTS `game_forbidden_country_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_forbidden_country_codes` (
  `game_id` char(36) NOT NULL,
  `country_code` char(2) NOT NULL,
  PRIMARY KEY (`game_id`,`country_code`),
  CONSTRAINT `game_country_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_language_codes`
--

DROP TABLE IF EXISTS `game_language_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_language_codes` (
  `game_id` char(36) NOT NULL,
  `language_code` varchar(5) NOT NULL,
  PRIMARY KEY (`game_id`,`language_code`),
  CONSTRAINT `game_language_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_player`
--

DROP TABLE IF EXISTS `game_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_player` (
  `id` char(36) NOT NULL,
  `game_id` char(36) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `freeplay` INT(1) NOT NULL DEFAULT 0,
  `rank` double DEFAULT NULL,
  `payout_payment_id` varchar(255) DEFAULT NULL,
  `payout_awarded_amount` decimal(10,2) DEFAULT NULL,
  `payout_venue` varchar(255) DEFAULT NULL,
  `payout_completed` int(1) NOT NULL DEFAULT '0',
  `determination` varchar(45) NOT NULL,
  `countdown_to_elimination` int(11) DEFAULT NULL,
  `next_round_id` char(36) DEFAULT NULL,
  `last_round_id` char(36) DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `gameplayer_fk1_idx` (`game_id`),
  KEY `gameplayer_fk2_idx` (`next_round_id`),
  KEY `gameplayer_fk3_idx` (`last_round_id`),
  CONSTRAINT `gameplayer_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `gameplayer_fk2` FOREIGN KEY (`next_round_id`) REFERENCES `round` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `gameplayer_fk3` FOREIGN KEY (`last_round_id`) REFERENCES `round` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match`
--

DROP TABLE IF EXISTS `match`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `match` (
  `id` char(36) NOT NULL,
  `game_engine` varchar(45) NOT NULL,
  `engine_type` VARCHAR(45) NOT NULL,
  `game_id` char(36) NOT NULL,
  `round_id` char(36) NOT NULL,
  `match_status` varchar(45) NOT NULL,
  `match_status_set_at` TIMESTAMP(6) NULL,
  `won_subscriber_id` int(10) unsigned DEFAULT NULL,
  `minimum_activity_to_win_count` int(11) NOT NULL,
  `maximum_activity_count` int(11) DEFAULT NULL,
  `actual_activity_count` int(11) DEFAULT NULL,
  `send_next_question_at` timestamp(6) NULL DEFAULT NULL,
  `determination` varchar(45) NOT NULL,
  `start_date` timestamp NULL DEFAULT NULL,
  `complete_date` timestamp NULL DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `match_fk1_idx` (`game_id`),
  KEY `match_fk2_idx` (`round_id`),
  CONSTRAINT `match_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `match_fk2` FOREIGN KEY (`round_id`) REFERENCES `round` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match_player`
--

DROP TABLE IF EXISTS `match_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `match_player` (
  `id` char(36) NOT NULL,
  `game_id` char(36) NOT NULL,
  `round_id` char(36) NOT NULL,
  `match_id` char(36) NOT NULL,
  `round_player_id` char(36) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `determination` varchar(45) NOT NULL,
  `score` double DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `matchplayer_fk1_idx` (`game_id`),
  KEY `matchplayer_fk2_idx` (`round_id`),
  KEY `matchplayer_fk3_idx` (`match_id`),
  KEY `matchplayer_fk4_idx` (`round_player_id`),
  CONSTRAINT `matchplayer_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `matchplayer_fk2` FOREIGN KEY (`round_id`) REFERENCES `round` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `matchplayer_fk3` FOREIGN KEY (`match_id`) REFERENCES `match` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `matchplayer_fk4` FOREIGN KEY (`round_player_id`) REFERENCES `round_player` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `match_queue`
--

DROP TABLE IF EXISTS `match_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `match_queue` (
  `id` char(36) NOT NULL,
  `game_id` char(36) NOT NULL,
  `round_id` char(36) NOT NULL,
  `round_player_id` char(36) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `cancelled` int(1) NOT NULL DEFAULT '0',
  `enqueue_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dequeue_timestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `matchqueue_fk1_idx` (`game_id`),
  KEY `matchqueue_fk2_idx` (`round_id`),
  KEY `matchqueue_fk3_idx` (`round_player_id`),
  CONSTRAINT `matchqueue_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `matchqueue_fk2` FOREIGN KEY (`round_id`) REFERENCES `round` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `matchqueue_fk3` FOREIGN KEY (`round_player_id`) REFERENCES `round_player` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `multi_localization`
--

DROP TABLE IF EXISTS `multi_localization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `multi_localization` (
  `uuid` char(36) NOT NULL,
  `type` varchar(45) NOT NULL,
  `language_code` varchar(5) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`uuid`,`type`, `language_code`),
  KEY `ml_type_idx` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('07362305-9e96-11e5-b784-86e93e99d7ba', 'systemMessage', 'en', 'Your verificaiton code is {0}');

CREATE TABLE contest.`phone_verification_code` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `phone` varchar(45) NOT NULL,
  `code` varchar(45) NOT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`subscriber_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contest.`countries` (
  `country_code` char(2) NOT NULL,
  `dial_code` varchar(16) NOT NULL,
  `sort_order` int(11) NOT NULL,
  PRIMARY KEY (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AD', 'en', 'Andorra');
insert into contest.countries (country_code, dial_code, sort_order) values ('AD', 376, 50);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AE', 'en', 'United Arab Emirates');
insert into contest.countries (country_code, dial_code, sort_order) values ('AE', 971, 2150);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AF', 'en', 'Afghanistan');
insert into contest.countries (country_code, dial_code, sort_order) values ('AF', 93, 10);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AG', 'en', 'Antigua and Barbuda');
insert into contest.countries (country_code, dial_code, sort_order) values ('AG', 1, 80);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AI', 'en', 'Anguilla');
insert into contest.countries (country_code, dial_code, sort_order) values ('AI', 1, 70);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AL', 'en', 'Albania');
insert into contest.countries (country_code, dial_code, sort_order) values ('AL', 355, 20);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AM', 'en', 'Armenia');
insert into contest.countries (country_code, dial_code, sort_order) values ('AM', 374, 100);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AN', 'en', 'Netherlands Antilles');
insert into contest.countries (country_code, dial_code, sort_order) values ('AN', 599, 1410);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AO', 'en', 'Angola');
insert into contest.countries (country_code, dial_code, sort_order) values ('AO', 244, 60);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AR', 'en', 'Argentina');
insert into contest.countries (country_code, dial_code, sort_order) values ('AR', 54, 90);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AS', 'en', 'American Samoa');
insert into contest.countries (country_code, dial_code, sort_order) values ('AS', 1, 40);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AT', 'en', 'Austria');
insert into contest.countries (country_code, dial_code, sort_order) values ('AT', 43, 130);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AU', 'en', 'Australia');
insert into contest.countries (country_code, dial_code, sort_order) values ('AU', 61, 120);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AW', 'en', 'Aruba');
insert into contest.countries (country_code, dial_code, sort_order) values ('AW', 297, 110);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_AZ', 'en', 'Azerbaijan');
insert into contest.countries (country_code, dial_code, sort_order) values ('AZ', 994, 140);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BA', 'en', 'Bosnia and Herzegovina');
insert into contest.countries (country_code, dial_code, sort_order) values ('BA', 387, 260);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BB', 'en', 'Barbados');
insert into contest.countries (country_code, dial_code, sort_order) values ('BB', 1, 180);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BD', 'en', 'Bangladesh');
insert into contest.countries (country_code, dial_code, sort_order) values ('BD', 880, 170);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BE', 'en', 'Belgium');
insert into contest.countries (country_code, dial_code, sort_order) values ('BE', 32, 200);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BF', 'en', 'Burkina Faso');
insert into contest.countries (country_code, dial_code, sort_order) values ('BF', 226, 320);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BG', 'en', 'Bulgaria');
insert into contest.countries (country_code, dial_code, sort_order) values ('BG', 359, 310);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BH', 'en', 'Bahrain');
insert into contest.countries (country_code, dial_code, sort_order) values ('BH', 973, 160);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BI', 'en', 'Burundi');
insert into contest.countries (country_code, dial_code, sort_order) values ('BI', 257, 330);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BJ', 'en', 'Benin');
insert into contest.countries (country_code, dial_code, sort_order) values ('BJ', 229, 220);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BL', 'en', 'Saint Barthelemy');
insert into contest.countries (country_code, dial_code, sort_order) values ('BL', 590, 1710);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BM', 'en', 'Bermuda');
insert into contest.countries (country_code, dial_code, sort_order) values ('BM', 1, 230);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BN', 'en', 'Brunei');
insert into contest.countries (country_code, dial_code, sort_order) values ('BN', 673, 300);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BO', 'en', 'Bolivia');
insert into contest.countries (country_code, dial_code, sort_order) values ('BO', 591, 250);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BR', 'en', 'Brazil');
insert into contest.countries (country_code, dial_code, sort_order) values ('BR', 55, 280);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BS', 'en', 'Bahamas');
insert into contest.countries (country_code, dial_code, sort_order) values ('BS', 1, 150);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BT', 'en', 'Bhutan');
insert into contest.countries (country_code, dial_code, sort_order) values ('BT', 975, 240);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BW', 'en', 'Botswana');
insert into contest.countries (country_code, dial_code, sort_order) values ('BW', 267, 270);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BY', 'en', 'Belarus');
insert into contest.countries (country_code, dial_code, sort_order) values ('BY', 375, 190);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_BZ', 'en', 'Belize');
insert into contest.countries (country_code, dial_code, sort_order) values ('BZ', 501, 210);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CA', 'en', 'Canada');
insert into contest.countries (country_code, dial_code, sort_order) values ('CA', 1, 360);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CD', 'en', 'Democratic Republic of Congo');
insert into contest.countries (country_code, dial_code, sort_order) values ('CD', 243, 520);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CF', 'en', 'Central African Republic');
insert into contest.countries (country_code, dial_code, sort_order) values ('CF', 236, 390);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CH', 'en', 'Switzerland');
insert into contest.countries (country_code, dial_code, sort_order) values ('CH', 41, 1980);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CI', 'en', 'Ivory Coast');
insert into contest.countries (country_code, dial_code, sort_order) values ('CI', 225, 970);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CK', 'en', 'Cook Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('CK', 682, 450);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CL', 'en', 'Chile');
insert into contest.countries (country_code, dial_code, sort_order) values ('CL', 56, 410);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CM', 'en', 'Cameroon');
insert into contest.countries (country_code, dial_code, sort_order) values ('CM', 237, 350);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CN', 'en', 'China');
insert into contest.countries (country_code, dial_code, sort_order) values ('CN', 86, 420);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CO', 'en', 'Colombia');
insert into contest.countries (country_code, dial_code, sort_order) values ('CO', 57, 430);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CR', 'en', 'Costa Rica');
insert into contest.countries (country_code, dial_code, sort_order) values ('CR', 506, 460);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CU', 'en', 'Cuba');
insert into contest.countries (country_code, dial_code, sort_order) values ('CU', 53, 480);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CV', 'en', 'Cape Verde');
insert into contest.countries (country_code, dial_code, sort_order) values ('CV', 238, 370);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CW', 'en', 'Curacao');
insert into contest.countries (country_code, dial_code, sort_order) values ('CW', 599, 490);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CY', 'en', 'Cyprus');
insert into contest.countries (country_code, dial_code, sort_order) values ('CY', 357, 500);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_CZ', 'en', 'Czech Republic');
insert into contest.countries (country_code, dial_code, sort_order) values ('CZ', 420, 510);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_DE', 'en', 'Germany');
insert into contest.countries (country_code, dial_code, sort_order) values ('DE', 49, 740);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_DJ', 'en', 'Djibouti');
insert into contest.countries (country_code, dial_code, sort_order) values ('DJ', 253, 540);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_DK', 'en', 'Denmark');
insert into contest.countries (country_code, dial_code, sort_order) values ('DK', 45, 530);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_DM', 'en', 'Dominica');
insert into contest.countries (country_code, dial_code, sort_order) values ('DM', 1, 550);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_DO', 'en', 'Dominican Republic');
insert into contest.countries (country_code, dial_code, sort_order) values ('DO', 1, 560);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_DZ', 'en', 'Algeria');
insert into contest.countries (country_code, dial_code, sort_order) values ('DZ', 213, 30);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_EC', 'en', 'Ecuador');
insert into contest.countries (country_code, dial_code, sort_order) values ('EC', 593, 580);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_EE', 'en', 'Estonia');
insert into contest.countries (country_code, dial_code, sort_order) values ('EE', 372, 630);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_EG', 'en', 'Egypt');
insert into contest.countries (country_code, dial_code, sort_order) values ('EG', 20, 590);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ER', 'en', 'Eritrea');
insert into contest.countries (country_code, dial_code, sort_order) values ('ER', 291, 620);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ES', 'en', 'Spain');
insert into contest.countries (country_code, dial_code, sort_order) values ('ES', 34, 1920);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ET', 'en', 'Ethiopia');
insert into contest.countries (country_code, dial_code, sort_order) values ('ET', 251, 640);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_FI', 'en', 'Finland');
insert into contest.countries (country_code, dial_code, sort_order) values ('FI', 358, 680);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_FJ', 'en', 'Fiji');
insert into contest.countries (country_code, dial_code, sort_order) values ('FJ', 679, 670);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_FK', 'en', 'Falkland (Malvinas) Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('FK', 500, 650);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_FM', 'en', 'Micronesia');
insert into contest.countries (country_code, dial_code, sort_order) values ('FM', 691, 1280);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_FO', 'en', 'Faroe Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('FO', 298, 660);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_FR', 'en', 'France');
insert into contest.countries (country_code, dial_code, sort_order) values ('FR', 33, 690);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GA', 'en', 'Gabon');
insert into contest.countries (country_code, dial_code, sort_order) values ('GA', 241, 710);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GB', 'en', 'United Kingdom');
insert into contest.countries (country_code, dial_code, sort_order) values ('GB', 44, 2160);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GD', 'en', 'Grenada');
insert into contest.countries (country_code, dial_code, sort_order) values ('GD', 1, 790);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GE', 'en', 'Georgia');
insert into contest.countries (country_code, dial_code, sort_order) values ('GE', 995, 730);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GH', 'en', 'Ghana');
insert into contest.countries (country_code, dial_code, sort_order) values ('GH', 233, 750);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GI', 'en', 'Gibraltar');
insert into contest.countries (country_code, dial_code, sort_order) values ('GI', 350, 760);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GL', 'en', 'Greenland');
insert into contest.countries (country_code, dial_code, sort_order) values ('GL', 299, 780);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GM', 'en', 'Gambia');
insert into contest.countries (country_code, dial_code, sort_order) values ('GM', 220, 720);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GN', 'en', 'Guinea');
insert into contest.countries (country_code, dial_code, sort_order) values ('GN', 224, 820);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GQ', 'en', 'Equatorial Guinea');
insert into contest.countries (country_code, dial_code, sort_order) values ('GQ', 240, 610);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GR', 'en', 'Greece');
insert into contest.countries (country_code, dial_code, sort_order) values ('GR', 30, 770);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GT', 'en', 'Guatemala');
insert into contest.countries (country_code, dial_code, sort_order) values ('GT', 502, 810);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GU', 'en', 'Guam');
insert into contest.countries (country_code, dial_code, sort_order) values ('GU', 1, 800);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GW', 'en', 'Guinea-Bissau');
insert into contest.countries (country_code, dial_code, sort_order) values ('GW', 245, 830);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_GY', 'en', 'Guyana');
insert into contest.countries (country_code, dial_code, sort_order) values ('GY', 592, 840);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_HK', 'en', 'Hong Kong');
insert into contest.countries (country_code, dial_code, sort_order) values ('HK', 852, 870);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_HN', 'en', 'Honduras');
insert into contest.countries (country_code, dial_code, sort_order) values ('HN', 504, 860);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_HR', 'en', 'Croatia');
insert into contest.countries (country_code, dial_code, sort_order) values ('HR', 385, 470);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_HT', 'en', 'Haiti');
insert into contest.countries (country_code, dial_code, sort_order) values ('HT', 509, 850);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_HU', 'en', 'Hungary');
insert into contest.countries (country_code, dial_code, sort_order) values ('HU', 36, 880);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ID', 'en', 'Indonesia');
insert into contest.countries (country_code, dial_code, sort_order) values ('ID', 62, 910);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_IE', 'en', 'Ireland');
insert into contest.countries (country_code, dial_code, sort_order) values ('IE', 353, 940);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_IL', 'en', 'Israel');
insert into contest.countries (country_code, dial_code, sort_order) values ('IL', 972, 950);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_IN', 'en', 'India');
insert into contest.countries (country_code, dial_code, sort_order) values ('IN', 91, 900);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_IQ', 'en', 'Iraq');
insert into contest.countries (country_code, dial_code, sort_order) values ('IQ', 964, 930);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_IR', 'en', 'Iran');
insert into contest.countries (country_code, dial_code, sort_order) values ('IR', 98, 920);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_IS', 'en', 'Iceland');
insert into contest.countries (country_code, dial_code, sort_order) values ('IS', 354, 890);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_IT', 'en', 'Italy');
insert into contest.countries (country_code, dial_code, sort_order) values ('IT', 39, 960);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_JM', 'en', 'Jamaica');
insert into contest.countries (country_code, dial_code, sort_order) values ('JM', 1, 980);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_JO', 'en', 'Jordan');
insert into contest.countries (country_code, dial_code, sort_order) values ('JO', 962, 1000);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_JP', 'en', 'Japan');
insert into contest.countries (country_code, dial_code, sort_order) values ('JP', 81, 990);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KE', 'en', 'Kenya');
insert into contest.countries (country_code, dial_code, sort_order) values ('KE', 254, 1020);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KG', 'en', 'Kyrgyzstan');
insert into contest.countries (country_code, dial_code, sort_order) values ('KG', 996, 1050);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KH', 'en', 'Cambodia');
insert into contest.countries (country_code, dial_code, sort_order) values ('KH', 855, 340);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KI', 'en', 'Kiribati');
insert into contest.countries (country_code, dial_code, sort_order) values ('KI', 686, 1030);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KM', 'en', 'Comoros');
insert into contest.countries (country_code, dial_code, sort_order) values ('KM', 269, 440);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KN', 'en', 'Saint Kitts and Nevis');
insert into contest.countries (country_code, dial_code, sort_order) values ('KN', 1, 1690);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KP', 'en', 'North Korea');
insert into contest.countries (country_code, dial_code, sort_order) values ('KP', 850, 1480);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KR', 'en', 'South Korea');
insert into contest.countries (country_code, dial_code, sort_order) values ('KR', 82, 1900);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KW', 'en', 'Kuwait');
insert into contest.countries (country_code, dial_code, sort_order) values ('KW', 965, 1040);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KY', 'en', 'Cayman Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('KY', 1, 380);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_KZ', 'en', 'Kazakhstan');
insert into contest.countries (country_code, dial_code, sort_order) values ('KZ', 7, 1010);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LA', 'en', 'Laos');
insert into contest.countries (country_code, dial_code, sort_order) values ('LA', 856, 1060);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LB', 'en', 'Lebanon');
insert into contest.countries (country_code, dial_code, sort_order) values ('LB', 961, 1080);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LC', 'en', 'Saint Lucia');
insert into contest.countries (country_code, dial_code, sort_order) values ('LC', 1, 1700);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LI', 'en', 'Liechtenstein');
insert into contest.countries (country_code, dial_code, sort_order) values ('LI', 423, 1120);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LK', 'en', 'Sri Lanka');
insert into contest.countries (country_code, dial_code, sort_order) values ('LK', 94, 1930);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LR', 'en', 'Liberia');
insert into contest.countries (country_code, dial_code, sort_order) values ('LR', 231, 1100);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LS', 'en', 'Lesotho');
insert into contest.countries (country_code, dial_code, sort_order) values ('LS', 266, 1090);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LT', 'en', 'Lithuania');
insert into contest.countries (country_code, dial_code, sort_order) values ('LT', 370, 1130);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LU', 'en', 'Luxembourg');
insert into contest.countries (country_code, dial_code, sort_order) values ('LU', 352, 1140);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LV', 'en', 'Latvia');
insert into contest.countries (country_code, dial_code, sort_order) values ('LV', 371, 1070);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_LY', 'en', 'Libya');
insert into contest.countries (country_code, dial_code, sort_order) values ('LY', 218, 1110);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MA', 'en', 'Morocco');
insert into contest.countries (country_code, dial_code, sort_order) values ('MA', 212, 1340);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MC', 'en', 'Monaco');
insert into contest.countries (country_code, dial_code, sort_order) values ('MC', 377, 1300);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MD', 'en', 'Moldova');
insert into contest.countries (country_code, dial_code, sort_order) values ('MD', 373, 1290);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ME', 'en', 'Montenegro');
insert into contest.countries (country_code, dial_code, sort_order) values ('ME', 382, 1320);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MF', 'en', 'Saint Martin');
insert into contest.countries (country_code, dial_code, sort_order) values ('MF', 590, 1720);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MG', 'en', 'Madagascar');
insert into contest.countries (country_code, dial_code, sort_order) values ('MG', 261, 1170);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MH', 'en', 'Marshall Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('MH', 692, 1230);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MK', 'en', 'Macedonia');
insert into contest.countries (country_code, dial_code, sort_order) values ('MK', 389, 1160);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ML', 'en', 'Mali');
insert into contest.countries (country_code, dial_code, sort_order) values ('ML', 223, 1210);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MM', 'en', 'Myanmar');
insert into contest.countries (country_code, dial_code, sort_order) values ('MM', 95, 1360);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MN', 'en', 'Mongolia');
insert into contest.countries (country_code, dial_code, sort_order) values ('MN', 976, 1310);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MO', 'en', 'Macau');
insert into contest.countries (country_code, dial_code, sort_order) values ('MO', 853, 1150);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MP', 'en', 'Northern Marianas');
insert into contest.countries (country_code, dial_code, sort_order) values ('MP', 1, 1490);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MR', 'en', 'Mauritania');
insert into contest.countries (country_code, dial_code, sort_order) values ('MR', 222, 1240);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MS', 'en', 'Montserrat');
insert into contest.countries (country_code, dial_code, sort_order) values ('MS', 1, 1330);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MT', 'en', 'Malta');
insert into contest.countries (country_code, dial_code, sort_order) values ('MT', 356, 1220);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MU', 'en', 'Mauritius');
insert into contest.countries (country_code, dial_code, sort_order) values ('MU', 230, 1250);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MV', 'en', 'Maldives');
insert into contest.countries (country_code, dial_code, sort_order) values ('MV', 960, 1200);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MW', 'en', 'Malawi');
insert into contest.countries (country_code, dial_code, sort_order) values ('MW', 265, 1180);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MX', 'en', 'Mexico');
insert into contest.countries (country_code, dial_code, sort_order) values ('MX', 52, 1270);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MY', 'en', 'Malaysia');
insert into contest.countries (country_code, dial_code, sort_order) values ('MY', 60, 1190);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_MZ', 'en', 'Mozambique');
insert into contest.countries (country_code, dial_code, sort_order) values ('MZ', 258, 1350);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NA', 'en', 'Namibia');
insert into contest.countries (country_code, dial_code, sort_order) values ('NA', 264, 1370);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NC', 'en', 'New Caledonia');
insert into contest.countries (country_code, dial_code, sort_order) values ('NC', 687, 1420);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NE', 'en', 'Niger');
insert into contest.countries (country_code, dial_code, sort_order) values ('NE', 227, 1450);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NG', 'en', 'Nigeria');
insert into contest.countries (country_code, dial_code, sort_order) values ('NG', 234, 1460);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NI', 'en', 'Nicaragua');
insert into contest.countries (country_code, dial_code, sort_order) values ('NI', 505, 1440);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NL', 'en', 'Netherlands');
insert into contest.countries (country_code, dial_code, sort_order) values ('NL', 31, 1400);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NO', 'en', 'Norway');
insert into contest.countries (country_code, dial_code, sort_order) values ('NO', 47, 1500);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NP', 'en', 'Nepal');
insert into contest.countries (country_code, dial_code, sort_order) values ('NP', 977, 1390);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NR', 'en', 'Nauru');
insert into contest.countries (country_code, dial_code, sort_order) values ('NR', 674, 1380);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NU', 'en', 'Niue');
insert into contest.countries (country_code, dial_code, sort_order) values ('NU', 683, 1470);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_NZ', 'en', 'New Zealand');
insert into contest.countries (country_code, dial_code, sort_order) values ('NZ', 64, 1430);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_OM', 'en', 'Oman');
insert into contest.countries (country_code, dial_code, sort_order) values ('OM', 968, 1510);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PA', 'en', 'Panama');
insert into contest.countries (country_code, dial_code, sort_order) values ('PA', 507, 1550);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PE', 'en', 'Peru');
insert into contest.countries (country_code, dial_code, sort_order) values ('PE', 51, 1580);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PF', 'en', 'French Polynesia');
insert into contest.countries (country_code, dial_code, sort_order) values ('PF', 689, 700);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PG', 'en', 'Papua New Guinea');
insert into contest.countries (country_code, dial_code, sort_order) values ('PG', 675, 1560);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PH', 'en', 'Philippines');
insert into contest.countries (country_code, dial_code, sort_order) values ('PH', 63, 1590);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PK', 'en', 'Pakistan');
insert into contest.countries (country_code, dial_code, sort_order) values ('PK', 92, 1520);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PL', 'en', 'Poland');
insert into contest.countries (country_code, dial_code, sort_order) values ('PL', 48, 1600);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PM', 'en', 'Saint Pierre and Miquelon');
insert into contest.countries (country_code, dial_code, sort_order) values ('PM', 508, 1730);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PR', 'en', 'Puerto Rico');
insert into contest.countries (country_code, dial_code, sort_order) values ('PR', 1, 1620);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PS', 'en', 'Palestine');
insert into contest.countries (country_code, dial_code, sort_order) values ('PS', 970, 1540);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PT', 'en', 'Portugal');
insert into contest.countries (country_code, dial_code, sort_order) values ('PT', 351, 1610);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PW', 'en', 'Palau');
insert into contest.countries (country_code, dial_code, sort_order) values ('PW', 680, 1530);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_PY', 'en', 'Paraguay');
insert into contest.countries (country_code, dial_code, sort_order) values ('PY', 595, 1570);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_QA', 'en', 'Qatar');
insert into contest.countries (country_code, dial_code, sort_order) values ('QA', 974, 1630);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_RE', 'en', 'Reunion');
insert into contest.countries (country_code, dial_code, sort_order) values ('RE', 262, 1640);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_RO', 'en', 'Romania');
insert into contest.countries (country_code, dial_code, sort_order) values ('RO', 40, 1650);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_RS', 'en', 'Serbia');
insert into contest.countries (country_code, dial_code, sort_order) values ('RS', 381, 1800);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_RU', 'en', 'Russia');
insert into contest.countries (country_code, dial_code, sort_order) values ('RU', 7, 1660);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_RW', 'en', 'Rwanda');
insert into contest.countries (country_code, dial_code, sort_order) values ('RW', 250, 1670);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SA', 'en', 'Saudi Arabia');
insert into contest.countries (country_code, dial_code, sort_order) values ('SA', 966, 1780);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SB', 'en', 'Solomon Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('SB', 677, 1870);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SC', 'en', 'Seychelles');
insert into contest.countries (country_code, dial_code, sort_order) values ('SC', 248, 1810);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SD', 'en', 'Sudan');
insert into contest.countries (country_code, dial_code, sort_order) values ('SD', 249, 1940);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SE', 'en', 'Sweden');
insert into contest.countries (country_code, dial_code, sort_order) values ('SE', 46, 1970);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SG', 'en', 'Singapore');
insert into contest.countries (country_code, dial_code, sort_order) values ('SG', 65, 1830);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SH', 'en', 'Saint Helena');
insert into contest.countries (country_code, dial_code, sort_order) values ('SH', 290, 1680);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SI', 'en', 'Slovenia');
insert into contest.countries (country_code, dial_code, sort_order) values ('SI', 386, 1860);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SK', 'en', 'Slovakia');
insert into contest.countries (country_code, dial_code, sort_order) values ('SK', 421, 1850);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SL', 'en', 'Sierra Leone');
insert into contest.countries (country_code, dial_code, sort_order) values ('SL', 232, 1820);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SM', 'en', 'San Marino');
insert into contest.countries (country_code, dial_code, sort_order) values ('SM', 378, 1760);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SN', 'en', 'Senegal');
insert into contest.countries (country_code, dial_code, sort_order) values ('SN', 221, 1790);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SO', 'en', 'Somalia');
insert into contest.countries (country_code, dial_code, sort_order) values ('SO', 252, 1880);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SR', 'en', 'Suriname');
insert into contest.countries (country_code, dial_code, sort_order) values ('SR', 597, 1950);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SS', 'en', 'South Sudan');
insert into contest.countries (country_code, dial_code, sort_order) values ('SS', 211, 1910);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ST', 'en', 'Sao Tome and Principe');
insert into contest.countries (country_code, dial_code, sort_order) values ('ST', 239, 1770);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SV', 'en', 'El Salvador');
insert into contest.countries (country_code, dial_code, sort_order) values ('SV', 503, 600);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SX', 'en', 'Sint Maarten');
insert into contest.countries (country_code, dial_code, sort_order) values ('SX', 1, 1840);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SY', 'en', 'Syria');
insert into contest.countries (country_code, dial_code, sort_order) values ('SY', 963, 1990);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_SZ', 'en', 'Swaziland');
insert into contest.countries (country_code, dial_code, sort_order) values ('SZ', 268, 1960);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TC', 'en', 'Turks and Caicos Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('TC', 1, 2110);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TD', 'en', 'Chad');
insert into contest.countries (country_code, dial_code, sort_order) values ('TD', 235, 400);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TG', 'en', 'Togo');
insert into contest.countries (country_code, dial_code, sort_order) values ('TG', 228, 2040);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TH', 'en', 'Thailand');
insert into contest.countries (country_code, dial_code, sort_order) values ('TH', 66, 2030);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TJ', 'en', 'Tajikistan');
insert into contest.countries (country_code, dial_code, sort_order) values ('TJ', 992, 2010);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TK', 'en', 'Tokelau');
insert into contest.countries (country_code, dial_code, sort_order) values ('TK', 690, 2050);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TL', 'en', 'East Timor');
insert into contest.countries (country_code, dial_code, sort_order) values ('TL', 670, 570);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TM', 'en', 'Turkmenistan');
insert into contest.countries (country_code, dial_code, sort_order) values ('TM', 993, 2100);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TN', 'en', 'Tunisia');
insert into contest.countries (country_code, dial_code, sort_order) values ('TN', 216, 2080);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TO', 'en', 'Tonga');
insert into contest.countries (country_code, dial_code, sort_order) values ('TO', 676, 2060);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TR', 'en', 'Turkey');
insert into contest.countries (country_code, dial_code, sort_order) values ('TR', 90, 2090);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TT', 'en', 'Trinidad and Tobago');
insert into contest.countries (country_code, dial_code, sort_order) values ('TT', 1, 2070);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TV', 'en', 'Tuvalu');
insert into contest.countries (country_code, dial_code, sort_order) values ('TV', 688, 2120);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TW', 'en', 'Taiwan');
insert into contest.countries (country_code, dial_code, sort_order) values ('TW', 886, 2000);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_TZ', 'en', 'Tanzania');
insert into contest.countries (country_code, dial_code, sort_order) values ('TZ', 255, 2020);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_UA', 'en', 'Ukraine');
insert into contest.countries (country_code, dial_code, sort_order) values ('UA', 380, 2140);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_UG', 'en', 'Uganda');
insert into contest.countries (country_code, dial_code, sort_order) values ('UG', 256, 2130);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_US', 'en', 'United States of America');
insert into contest.countries (country_code, dial_code, sort_order) values ('US', 1, 2170);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_UY', 'en', 'Uruguay');
insert into contest.countries (country_code, dial_code, sort_order) values ('UY', 598, 2190);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_UZ', 'en', 'Uzbekistan');
insert into contest.countries (country_code, dial_code, sort_order) values ('UZ', 998, 2200);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_VA', 'en', 'Vatican City');
insert into contest.countries (country_code, dial_code, sort_order) values ('VA', 379, 2220);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_VC', 'en', 'Saint Vincent and the Grenadines');
insert into contest.countries (country_code, dial_code, sort_order) values ('VC', 1, 1740);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_VE', 'en', 'Venezuela');
insert into contest.countries (country_code, dial_code, sort_order) values ('VE', 58, 2230);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_VG', 'en', 'British Virgin Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('VG', 1, 290);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_VI', 'en', 'U.S. Virgin Islands');
insert into contest.countries (country_code, dial_code, sort_order) values ('VI', 1, 2180);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_VN', 'en', 'Vietnam');
insert into contest.countries (country_code, dial_code, sort_order) values ('VN', 84, 2240);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_VU', 'en', 'Vanuatu');
insert into contest.countries (country_code, dial_code, sort_order) values ('VU', 678, 2210);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_WF', 'en', 'Wallis and Futuna');
insert into contest.countries (country_code, dial_code, sort_order) values ('WF', 681, 2250);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_WS', 'en', 'Samoa');
insert into contest.countries (country_code, dial_code, sort_order) values ('WS', 685, 1750);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_YE', 'en', 'Yemen');
insert into contest.countries (country_code, dial_code, sort_order) values ('YE', 967, 2260);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_YT', 'en', 'Mayotte');
insert into contest.countries (country_code, dial_code, sort_order) values ('YT', 262, 1260);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ZA', 'en', 'South Africa');
insert into contest.countries (country_code, dial_code, sort_order) values ('ZA', 27, 1890);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ZM', 'en', 'Zambia');
insert into contest.countries (country_code, dial_code, sort_order) values ('ZM', 260, 2270);
insert into contest.multi_localization (uuid, `type`, language_code, `value`) values ('f983f72c-e72c-4a69-be9a-1ab53276ecc5', 'cc_ZW', 'en', 'Zimbabwe');
insert into contest.countries (country_code, dial_code, sort_order) values ('ZW', 263, 2280);
INSERT INTO `contest`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('0dd0c00a-8c1c-43f5-a598-95f55d32aa93', 'systemMessage', 'en', 'Password Reset Request');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('7bf6cc56-24ed-4c9b-8933-f1cb01cf2bb2', 'systemMessage', 'en', 'You just earned ${0,number,##,###.00} on {1} since {2} just joined from your link on event {3}. Go here to see: {4}');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('abd2dbb5-d7ef-4ba5-9f73-36b8705854a4', 'systemMessage', 'en', 'You just earned ${0,number,##,###.00} on {1} since {2} just joined from your link. Go here to see: {3}');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('a9eb161d-59c3-44e5-999e-8029313f87bf', 'systemMessage', 'en', 'You just earned ${0,number,##,###.00} on {1}] since someone just joined from your link. Go here to see: {2}');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('6f7a6d63-fbf5-43bd-958e-23748e63870d', 'systemMessage', 'en', 'You received a referral payout');

--
-- Table structure for table `round`
--

DROP TABLE IF EXISTS `round`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `round` (
  `id` char(36) NOT NULL,
  `game_id` char(36) NOT NULL,
  `round_type` varchar(45) NOT NULL,
  `round_status` varchar(45) NOT NULL,
  `round_sequence` int(10) unsigned NOT NULL,
  `final_round` int(1) NOT NULL DEFAULT '0',
  `round_purse` DECIMAL(8,2) DEFAULT NULL,
  `current_player_count` int(11) NOT NULL DEFAULT '0',
  `maximum_player_count` int(11) NOT NULL,
  `minimum_match_count` int(11) NOT NULL,
  `maximum_match_count` int(11) DEFAULT NULL,
  `cost_per_player` DECIMAL(8,2) DEFAULT NULL,
  `round_activity_type` varchar(255) NOT NULL,
  `round_activity_value` varchar(45) NOT NULL,
  `minimum_activity_to_win_count` int(11) NOT NULL,
  `maximum_activity_count` int(11) DEFAULT NULL,
  `activity_minimum_difficulty` int(11) DEFAULT NULL,
  `activity_maximum_difficulty` int(11) DEFAULT NULL,
  `activity_maximum_duration_seconds` int(11) NOT NULL,
  `player_maximum_duration_seconds` int(11) NOT NULL,
  `duration_between_activities_seconds` int(11) NOT NULL DEFAULT '10',
  `match_global` int(1) NOT NULL DEFAULT '0',
  `maximum_duration_minutes` int(11) DEFAULT NULL,
  `match_player_count` int(11) NOT NULL,
  `pending_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cancelled_date` timestamp NULL DEFAULT NULL,
  `visible_date` timestamp NULL DEFAULT NULL,
  `expected_open_date` timestamp NULL DEFAULT NULL,
  `open_date` timestamp NULL DEFAULT NULL,
  `inplay_date` timestamp NULL DEFAULT NULL,
  `closed_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `round_fk1_idx` (`game_id`),
  CONSTRAINT `round_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `round_categories`
--

DROP TABLE IF EXISTS `round_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `round_categories` (
  `round_category_id` int(11) NOT NULL AUTO_INCREMENT,
  `round_id` char(36) NOT NULL,
  `category` varchar(255) NOT NULL,
  PRIMARY KEY (`round_category_id`),
  KEY `round_categories_fk1_idx` (`round_id`),
  KEY `round_categories_idx1` (`category`(191)),
  CONSTRAINT `round_categories_fk1` FOREIGN KEY (`round_id`) REFERENCES `round` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=908 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `round_player`
--

DROP TABLE IF EXISTS `round_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `round_player` (
  `id` char(36) NOT NULL,
  `game_id` char(36) NOT NULL,
  `round_id` char(36) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `played_match_count` int(11) DEFAULT NULL,
  `determination` varchar(45) NOT NULL,
  `receipt_id` varchar(255) DEFAULT NULL,
  `amount_paid` decimal(10,2) DEFAULT NULL,
  `refunded` int(1) DEFAULT NULL,
  `skill_answer_correct_pct` double DEFAULT NULL,
  `skill_average_answer_ms` int(11) DEFAULT NULL,
  `rank` double DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `roundplayer_fk1_idx` (`game_id`),
  KEY `roundplayer_fk2_idx` (`round_id`),
  CONSTRAINT `roundplayer_fk1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `roundplayer_fk2` FOREIGN KEY (`round_id`) REFERENCES `round` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'contest'
--

--
-- Dumping routines for database 'contest'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

--
-- Dumping events for database 'notification'
--

--
-- Dumping routines for database 'notification'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:14:00
CREATE DATABASE  IF NOT EXISTS `sync` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `sync`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: sync
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message` (
  `id` char(36) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `message_type` varchar(45) NOT NULL,
  `contextual_id` varchar(45) NOT NULL,
  `engine_key` varchar(45) NOT NULL,
  `payload` text NOT NULL,
  `create_date` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  INDEX `sm_01` (`contextual_id` ASC),
  INDEX `sm_02` (`subscriber_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'sync'
--

--
-- Dumping routines for database 'sync'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:14:06
CREATE DATABASE  IF NOT EXISTS `distdata` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `distdata`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: distdata
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `lock`
--

DROP TABLE IF EXISTS `lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lock` (
  `lock_name` varchar(20) NOT NULL,
  `create_date` datetime NOT NULL,
  PRIMARY KEY (`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lock`
--

LOCK TABLES `lock` WRITE;
/*!40000 ALTER TABLE `lock` DISABLE KEYS */;
/*!40000 ALTER TABLE `lock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'distdata'
--

--
-- Dumping routines for database 'distdata'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:14:09
CREATE DATABASE  IF NOT EXISTS `postoffice` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `postoffice`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: postoffice
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `email_optout`
--

DROP TABLE IF EXISTS `email_optout`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_optout` (
  `from_address` varchar(255) NOT NULL,
  `to_address` varchar(255) NOT NULL,
  `opted_out_date` datetime DEFAULT NULL,
  PRIMARY KEY (`from_address`,`to_address`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_optout`
--

LOCK TABLES `email_optout` WRITE;
/*!40000 ALTER TABLE `email_optout` DISABLE KEYS */;
/*!40000 ALTER TABLE `email_optout` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'postoffice'
--

--
-- Dumping routines for database 'postoffice'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:14:13
CREATE DATABASE  IF NOT EXISTS `team` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `team`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: team
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `munge`
--

DROP TABLE IF EXISTS `munge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `munge` (
  `munge_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `namespace` varchar(20) NOT NULL,
  `original_value` varchar(1024) NOT NULL,
  `munged_value` varchar(128) NOT NULL,
  `created_date` datetime NOT NULL,
  `expires_date` datetime NOT NULL,
  PRIMARY KEY (`munge_id`),
  KEY `ns_orig_idx` (`namespace`,`original_value`(767),`expires_date`),
  KEY `ns_mung_idx` (`namespace`,`munged_value`,`expires_date`),
  KEY `expires_idx` (`expires_date`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `munge`
--

LOCK TABLES `munge` WRITE;
/*!40000 ALTER TABLE `munge` DISABLE KEYS */;
/*!40000 ALTER TABLE `munge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'team'
--

--
-- Dumping routines for database 'team'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:14:16
CREATE DATABASE  IF NOT EXISTS `push` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `push`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: push
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `token_subscriber`
--

DROP TABLE IF EXISTS `token_subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `token_subscriber` (
  `token_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `app_bundle_id` varchar(255) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `device_uuid` varchar(255) NOT NULL,
  `device_token` varchar(255) NOT NULL,
  `device_type` varchar(50) NOT NULL,
  `last_registration` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `device_active` int(1) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `device_token` (`device_token`),
  KEY `subscriber_id` (`subscriber_id`),
  KEY `last_registration` (`last_registration`),
  KEY `device_uuid` (`device_uuid`),
  KEY `app_bundle_id` (`app_bundle_id`),
  KEY `device_type` (`device_type`),
  KEY `device_active` (`device_active`)
) ENGINE=InnoDB AUTO_INCREMENT=1386570 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `token_subscriber`
--

LOCK TABLES `token_subscriber` WRITE;
/*!40000 ALTER TABLE `token_subscriber` DISABLE KEYS */;
/*!40000 ALTER TABLE `token_subscriber` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'push'
--

--
-- Dumping routines for database 'push'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:14:19
CREATE DATABASE  IF NOT EXISTS `facebook` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `facebook`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: facebook
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `auth_subscriber`
--

DROP TABLE IF EXISTS `auth_subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth_subscriber` (
  `facebook_id` varchar(255) NOT NULL,
  `access_token` varchar(512) NOT NULL,
  PRIMARY KEY (`facebook_id`),
  UNIQUE KEY `access_token_UNIQUE` (`access_token`),
  KEY `access_token` (`access_token`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_subscriber`
--

LOCK TABLES `auth_subscriber` WRITE;
/*!40000 ALTER TABLE `auth_subscriber` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_subscriber` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `callback`
--

DROP TABLE IF EXISTS `callback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `callback` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `namespace` varchar(255) DEFAULT NULL,
  `service_name` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  `method_name` varchar(767) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `namespace` (`namespace`),
  KEY `service_name` (`service_name`),
  KEY `version` (`version`),
  KEY `method_name` (`method_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `callback`
--

LOCK TABLES `callback` WRITE;
/*!40000 ALTER TABLE `callback` DISABLE KEYS */;
/*!40000 ALTER TABLE `callback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'facebook'
--

--
-- Dumping routines for database 'facebook'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:14:24
CREATE DATABASE  IF NOT EXISTS `gameplay` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `gameplay`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: gameplay
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `app`
--

DROP TABLE IF EXISTS `app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app` (
  `app_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `app_name` varchar(128) NOT NULL,
  `firewalled` int(1) NOT NULL DEFAULT '0',
  `endpoint_method` enum('GET','POST') DEFAULT NULL,
  `endpoint_protocol` enum('http','https') DEFAULT NULL,
  `endpoint_port` int(10) unsigned DEFAULT NULL,
  `endpoint_url` varchar(256) DEFAULT NULL,
  `client_key` varchar(256) DEFAULT NULL,
  `iOS_bundle_id` varchar(255) DEFAULT NULL,
  `android_bundle_id` varchar(255) DEFAULT NULL,
  `windows_bundle_id` varchar(255) DEFAULT NULL,
  `vipbox_push_type` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `app_name_UNIQUE` (`app_name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app`
--

LOCK TABLES `app` WRITE;
/*!40000 ALTER TABLE `app` DISABLE KEYS */;
INSERT INTO `app` VALUES (6,'snowyowl',1,NULL,NULL,NULL,NULL,NULL,'tv.shout.snowyowl','tv.shout.snowyowl','tv.shout.snowyowl',NULL);
/*!40000 ALTER TABLE `app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_language`
--

DROP TABLE IF EXISTS `app_language`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_language` (
  `app_id` int(10) unsigned NOT NULL,
  `language_code` varchar(8) NOT NULL,
  `default_flag` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`app_id`,`language_code`),
  CONSTRAINT `fk_app_language_01` FOREIGN KEY (`app_id`) REFERENCES `app` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_language`
--

LOCK TABLES `app_language` WRITE;
/*!40000 ALTER TABLE `app_language` DISABLE KEYS */;
INSERT INTO `app_language` VALUES (6,'en',1);
/*!40000 ALTER TABLE `app_language` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `acra_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contest`.`acra_report` (
  `report_id` CHAR(36) NOT NULL,
  `context_id` INT NULL,
  `subscriber_id` INT NULL,
  `user_email` VARCHAR(255) NULL,
  `app_version_code` INT NULL,
  `app_version_name` VARCHAR(255) NULL,
  `app_package_name` VARCHAR(255) NULL,
  `phone_model` VARCHAR(255) NULL,
  `phone_brand` VARCHAR(255) NULL,
  `phone_product` VARCHAR(255) NULL,
  `phone_display` TEXT NULL,
  `phone_initial_config` TEXT NULL,
  `phone_crash_config` TEXT NULL,
  `android_version` VARCHAR(255) NULL,
  `android_build` TEXT NULL,
  `total_mem_size` INT NULL,
  `available_mem_size` INT NULL,
  `stacktrace` TEXT NOT NULL,
  `logcat` TEXT NULL,
  `app_start_date` TIMESTAMP(6) NULL,
  `crash_date` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `s_identity_fb`
--

DROP TABLE IF EXISTS `s_identity_fb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_identity_fb` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `facebook_id` varchar(255) NOT NULL,
  `facebook_app_id` varchar(255) NOT NULL,
  `context_id` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`subscriber_id`),
  UNIQUE KEY `fb_facebook_id_UNIQUE` (`facebook_id`,`context_id`),
  KEY `fb_context_id` (`context_id`),
  KEY `fb_facebook_id` (`facebook_id`),
  KEY `fb_app_id` (`facebook_app_id`),
  CONSTRAINT `FK_s_identity_fb_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_identity_fb`
--

LOCK TABLES `s_identity_fb` WRITE;
/*!40000 ALTER TABLE `s_identity_fb` DISABLE KEYS */;
/*!40000 ALTER TABLE `s_identity_fb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_identity_foreign_host`
--

DROP TABLE IF EXISTS `s_identity_foreign_host`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_identity_foreign_host` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `foreign_host_subscriber_id` varchar(255) NOT NULL,
  `foreign_host_app_id` varchar(255) NOT NULL,
  `context_id` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`subscriber_id`),
  UNIQUE KEY `s_identity_foreign_host_key_UNIQUE` (`foreign_host_subscriber_id`,`context_id`),
  KEY `key_s_identity_foreign_host_context_id` (`context_id`),
  KEY `key_s_identity_foreign_host_subscriber_id` (`foreign_host_subscriber_id`),
  KEY `key_s_identity_foreign_host_app_id` (`foreign_host_app_id`),
  CONSTRAINT `FK_s_identity_foreign_host_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_identity_foreign_host`
--

LOCK TABLES `s_identity_foreign_host` WRITE;
/*!40000 ALTER TABLE `s_identity_foreign_host` DISABLE KEYS */;
/*!40000 ALTER TABLE `s_identity_foreign_host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_invalid_nicknames`
--

DROP TABLE IF EXISTS `s_invalid_nicknames`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_invalid_nicknames` (
  `nickname` varchar(50) NOT NULL,
  PRIMARY KEY (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_invalid_nicknames`
--

LOCK TABLES `s_invalid_nicknames` WRITE;
/*!40000 ALTER TABLE `s_invalid_nicknames` DISABLE KEYS */;
INSERT INTO `s_invalid_nicknames` VALUES ('$h0ut'),('$hout'),('sh0ut'),('shout');
/*!40000 ALTER TABLE `s_invalid_nicknames` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_subscriber`
--

DROP TABLE IF EXISTS `s_subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_subscriber` (
  `subscriber_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `context_id` int(11) NOT NULL,
  `firstname` varchar(180) CHARACTER SET utf8mb4 NOT NULL DEFAULT 'NYI',
  `lastname` varchar(180) CHARACTER SET utf8mb4 NOT NULL DEFAULT 'NYI',
  `nickname` varchar(50) CHARACTER SET utf8mb4 NOT NULL,
  `nickname_set` int(1) NOT NULL DEFAULT '0',
  `facebook_user_flag` int(1) NOT NULL DEFAULT '0',
  `photo_url` varchar(1024) DEFAULT NULL,
  `photo_url_small` varchar(1024) DEFAULT NULL,
  `photo_url_large` varchar(1024) DEFAULT NULL,
  `role` enum('USER','ADMIN','SHOUTCASTER','CELEBRITY','TESTER') NOT NULL DEFAULT 'USER',
  `admin_role` enum('NONE','NORMAL','SUPER') NOT NULL DEFAULT 'NONE',
  `primary_identifier` varchar(255) NOT NULL,
  `email` varchar(128) DEFAULT NULL,
  `email_verified` int(1) NOT NULL DEFAULT '0',
  `passwd` varchar(511) NOT NULL,
  `passwd_set` int(1) NOT NULL DEFAULT '0',
  `change_password` int(1) unsigned NOT NULL DEFAULT '0',
  `encrypt_key` varchar(32) NOT NULL,
  `email_sha256_hash` varchar(255) NOT NULL,
  `email_hash_prefix` varchar(10) NOT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `phone_verified` int(1) unsigned NOT NULL DEFAULT '0',
  `active_flag` int(1) unsigned NOT NULL DEFAULT '1',
  `eula_flag` int(1) unsigned NOT NULL DEFAULT '0',
  `is_adult_flag` int(1) unsigned NOT NULL DEFAULT '0',
  `language_code` char(2) NOT NULL DEFAULT 'en',
  `currency_code` char(3) DEFAULT NULL,
  `from_country_code` char(2) DEFAULT NULL,
  `ship_country_code` char(2) DEFAULT NULL,
  `region` VARCHAR(64) NULL,
  `mint_parent_subscriber_id` int(10) unsigned DEFAULT NULL,
  `ring1_subscriber_id` int(10) unsigned DEFAULT NULL,
  `ring2_subscriber_id` int(10) unsigned DEFAULT NULL,
  `ring3_subscriber_id` int(10) unsigned DEFAULT NULL,
  `ring4_subscriber_id` int(10) unsigned DEFAULT NULL,
  `date_of_birth` datetime DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`subscriber_id`),
  UNIQUE KEY `nickname_context_id` (`context_id`,`nickname`),
  UNIQUE KEY `primary_id` (`primary_identifier`),
  UNIQUE KEY `email_context_id` (`context_id`,`email`),
  KEY `email` (`email`),
  KEY `passwd` (`passwd`),
  KEY `phone` (`phone`),
  KEY `nickname` (`nickname`),
  KEY `active_flag` (`active_flag`),
  KEY `context_id` (`context_id`),
  KEY `email_hash_prefix` (`email_hash_prefix`),
  KEY `create_date` (`create_date`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_subscriber`
--

LOCK TABLES `s_subscriber` WRITE;
/*!40000 ALTER TABLE `s_subscriber` DISABLE KEYS */;
INSERT INTO `s_subscriber` VALUES (8,6,'Scott','Hawker','yarell',1,1,'http://scotthawker.com/bb8.jpg',NULL,NULL,'ADMIN','SUPER','email://1/shawker@me-inc.com','shawker@me-inc.com',1,'$s0$f0801$uNu9ZTEN8Msr1BIr5PPovexqLyx5AiasLbVTadI6IqtHASo0zyhZT19H31Cho3m2s1ZMrXciXkXJcax5HHWwJA==$9aHjx/IMK0lokc0oINMBqfXFd7ytwJoRGSZy9Zx6qeAHmv7ICarbgbxwS+ztRaHVSPd2RSBI/jV9fEZvBI+hHA==',1,0,'c}LUK#}.99gUK!<q:r1f-I1V7MysCbAT','9ef4f9ee39676c401bea708661283921bb2c72bc2f95a30bb38b5a9fd485a997','9ef',NULL,0,1,1,1,'en','USD','US',NULL,null,11141,0,0,0,NULL,'2011-04-14 00:17:57','2017-07-14 16:44:39');
INSERT INTO `s_subscriber` VALUES (9,6,'Bruce','Grant','bxgrant',1,0,'http://images.goodsmile.info/cgm/images/product/20161124/6080/42526/large/9a0c2f74fe41f8fdb54366a48af824c4.jpg',NULL,NULL,'ADMIN','NONE','email://6/bxgrant@gmail.com','bxgrant@gmail.com',0,'$s0$c0801$DaYCZqPIib87aWAdDg2r0A==$wBDNnlXx0eKi0uoxTA0be1Xwkx63Q0NXkLzLdMjRoZA=',1,0,'bNYc/xx7S,aL/vls!|W==L<7b_:\'XOCs','4f06efa28667825008df61e81b94756a685ef8805c6b8d51c2d587ced56e0f0c','4f0',NULL,0,1,0,0,'en',NULL,'US',NULL,NULL,NULL,NULL,NULL,NULL,'1972-11-22 16:30:11','2017-09-16 22:30:31','2017-09-16 22:30:31');
INSERT INTO `s_subscriber` VALUES (10,6,'Aidan','Grant','aidan',1,0,'https://dfep0xlbws1ys.cloudfront.net/thumbsd9/3d/d93d7af33c48bfe04560c620be469e3a.jpg?response-cache-control=max-age=2628000',NULL,NULL,'ADMIN','NONE','email://6/mraidangrant@gmail.com','mraidangrant@gmail.com',0,'$s0$c0801$Tn2/y63CNwpU7vxzezOBJQ==$WzAW6HbIy+P2UCnHSMNGYZQW7hRto5VV8kgaWqPnzNc=',1,0,'U]gDhRk9(JV4}iMFtkMyLJN^M\'U_%NbH','ccfcd9c633fee6c11cd050fb613af10730655a68c9ca49ad785121837eafbb84','ccf',NULL,0,1,0,0,'en',NULL,'US',NULL,NULL,0,0,0,0,'1998-10-08 06:00:00','2017-09-11 19:34:04','2017-10-10 21:29:44');
/*!40000 ALTER TABLE `s_subscriber` ENABLE KEYS */;
UNLOCK TABLES;

CREATE TABLE `gameplay`.`s_subscriber_role` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `role` VARCHAR(45) NOT NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`subscriber_id`, `role`),
  CONSTRAINT `ssrole_fk1`
    FOREIGN KEY (`subscriber_id`)
    REFERENCES `gameplay`.`s_subscriber` (`subscriber_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

INSERT INTO `gameplay`.`s_subscriber_role` (`subscriber_id`, `role`, `create_date`) VALUES ('8',  'SUPERUSER', NOW());
INSERT INTO `gameplay`.`s_subscriber_role` (`subscriber_id`, `role`, `create_date`) VALUES ('9',  '*UPERUSER', NOW());
INSERT INTO `gameplay`.`s_subscriber_role` (`subscriber_id`, `role`, `create_date`) VALUES ('10', '*UPERUSER', NOW());

--
-- Table structure for table `s_subscriber_address`
--

DROP TABLE IF EXISTS `s_subscriber_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_subscriber_address` (
  `address_id` int(11) NOT NULL AUTO_INCREMENT,
  `subscriber_id` int(10) unsigned NOT NULL,
  `addr_type` enum('HOME','SHIPPING','BILLING') DEFAULT NULL,
  `addr1` varchar(256) DEFAULT NULL,
  `addr2` varchar(256) DEFAULT NULL,
  `city` varchar(64) DEFAULT NULL,
  `state` varchar(64) DEFAULT NULL,
  `zip` varchar(64) DEFAULT NULL,
  `country_code` varchar(2) NOT NULL DEFAULT 'US',
  `current_flag` int(1) NOT NULL DEFAULT '0',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`address_id`),
  KEY `FK_s_subscriber_address_1` (`subscriber_id`),
  CONSTRAINT `FK_s_subscriber_address_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_subscriber_address`
--

LOCK TABLES `s_subscriber_address` WRITE;
/*!40000 ALTER TABLE `s_subscriber_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `s_subscriber_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_subscriber_email`
--

DROP TABLE IF EXISTS `s_subscriber_email`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_subscriber_email` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `email` varchar(128) NOT NULL,
  `email_type` enum('PAYPAL') NOT NULL,
  `verified` int(1) NOT NULL DEFAULT '0',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `verified_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`subscriber_id`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_subscriber_email`
--

LOCK TABLES `s_subscriber_email` WRITE;
/*!40000 ALTER TABLE `s_subscriber_email` DISABLE KEYS */;
/*!40000 ALTER TABLE `s_subscriber_email` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_subscriber_nickname_history`
--

DROP TABLE IF EXISTS `s_subscriber_nickname_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_subscriber_nickname_history` (
  `snh_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `subscriber_id` int(10) unsigned NOT NULL,
  `old_nickname` varchar(50) DEFAULT NULL,
  `new_nickname` varchar(50) NOT NULL,
  `change_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`snh_id`),
  KEY `FK_s_subscriber_nickname_history_1` (`subscriber_id`),
  CONSTRAINT `FK_s_subscriber_nickname_history_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE=InnoDB AUTO_INCREMENT=83096 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_subscriber_nickname_history`
--

LOCK TABLES `s_subscriber_nickname_history` WRITE;
/*!40000 ALTER TABLE `s_subscriber_nickname_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `s_subscriber_nickname_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_subscriber_session`
--

DROP TABLE IF EXISTS `s_subscriber_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `s_subscriber_session` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `context_id` int(10) unsigned NOT NULL,
  `device_id` varchar(255) NOT NULL DEFAULT 'WEB',
  `session_key` char(36) DEFAULT NULL,
  `device_model` varchar(255) DEFAULT NULL,
  `device_name` varchar(255) DEFAULT NULL,
  `device_version` varchar(255) DEFAULT NULL,
  `os_name` varchar(255) DEFAULT NULL,
  `os_type` varchar(255) DEFAULT NULL,
  `app_id` varchar(255) DEFAULT NULL,
  `app_version` varchar(255) DEFAULT NULL,
  `added_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_authenticated_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`subscriber_id`,`device_id`),
  UNIQUE KEY `session_key_UNIQUE` (`session_key`),
  KEY `device_model` (`device_model`),
  KEY `device_id` (`device_id`),
  CONSTRAINT `FK_s_subscriber_session_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `s_subscriber_session`
--

LOCK TABLES `s_subscriber_session` WRITE;
/*!40000 ALTER TABLE `s_subscriber_session` DISABLE KEYS */;
INSERT INTO `gameplay`.`s_subscriber_session` VALUES ('8', '6', '6170b6711e251ad6', '5e47ce76-7faa-426f-a8cf-27c106cb86eb', 'Foo', 'Bar', '1.0', 'OS', 'Fun', 'snowyowl', '1', NOW(), NOW());
/*!40000 ALTER TABLE `s_subscriber_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'gameplay'
--

--
-- Dumping routines for database 'gameplay'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:15:51
CREATE DATABASE  IF NOT EXISTS `store` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `store`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: store
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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
-- Table structure for table `entitlement`
--

DROP TABLE IF EXISTS `entitlement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entitlement` (
  `entitlement_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` char(36) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`entitlement_id`),
  KEY `uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entitlement`
--

LOCK TABLES `entitlement` WRITE;
/*!40000 ALTER TABLE `entitlement` DISABLE KEYS */;
INSERT INTO `entitlement` VALUES (16,'b37fc74a-7c6c-11e7-970d-0242ac110004','snowyowl credit');
/*!40000 ALTER TABLE `entitlement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item` (
  `item_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `store_bundle_id` varchar(255) NOT NULL DEFAULT 'com.meinc.shout',
  `uuid` varchar(100) CHARACTER SET latin1 COLLATE latin1_general_cs DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `description` varchar(255) NOT NULL,
  `price` decimal(7,2) NOT NULL DEFAULT '0.00',
  `active` int(1) unsigned NOT NULL DEFAULT '1',
  `duration_quantity` int(10) DEFAULT NULL,
  `duration_unit` enum('HOURS','DAYS','MONTHS','YEARS') DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  KEY `active` (`active`),
  KEY `uuid_idx` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
INSERT INTO `item` VALUES (38,'tv.shout.snowyowl','e8056253-7c6c-11e7-970d-0242ac110004','$1','$1',1.00,1,NULL,NULL);
INSERT INTO `item` VALUES (39,'tv.shout.snowyowl','0b7c7d65-7c6d-11e7-970d-0242ac110004','$5','$5',5.00,1,NULL,NULL);
INSERT INTO `item` VALUES (40,'tv.shout.snowyowl','2252534e-7c6d-11e7-970d-0242ac110004','$10','$10',10.00,1,NULL,NULL);
INSERT INTO `item` VALUES (41,'tv.shout.snowyowl','a05ba3bf-9887-11e7-bc47-0242ac110008','$20','$20','20.00','1',NULL,NULL);
INSERT INTO `item` VALUES (42,'tv.shout.snowyowl','a05d16b9-9887-11e7-bc47-0242ac110008','$50','$50','50.00','1',NULL,NULL);
INSERT INTO `item` VALUES (43,'tv.shout.snowyowl','a05d3bc1-9887-11e7-bc47-0242ac110008','$100','$100','100.00','1',NULL,NULL);
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_entitlement`
--

DROP TABLE IF EXISTS `item_entitlement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item_entitlement` (
  `item_id` int(10) unsigned NOT NULL,
  `entitlement_id` int(10) unsigned NOT NULL,
  `quantity` int(10) NOT NULL DEFAULT '1',
  PRIMARY KEY (`item_id`,`entitlement_id`),
  KEY `item_id` (`item_id`),
  KEY `entitlement_id` (`entitlement_id`),
  CONSTRAINT `item_entitlement_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`),
  CONSTRAINT `item_entitlement_ibfk_2` FOREIGN KEY (`entitlement_id`) REFERENCES `entitlement` (`entitlement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_entitlement`
--

LOCK TABLES `item_entitlement` WRITE;
/*!40000 ALTER TABLE `item_entitlement` DISABLE KEYS */;
INSERT INTO `item_entitlement` VALUES (38,16,1);
INSERT INTO `item_entitlement` VALUES (39,16,5);
INSERT INTO `item_entitlement` VALUES (40,16,10);
INSERT INTO `item_entitlement` VALUES (41,16,20);
INSERT INTO `item_entitlement` VALUES (42,16,50);
INSERT INTO `item_entitlement` VALUES (43,16,100);
/*!40000 ALTER TABLE `item_entitlement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_price`
--

DROP TABLE IF EXISTS `item_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item_price` (
  `item_id` int(10) unsigned NOT NULL,
  `currency_code` char(3) NOT NULL,
  `price` decimal(7,2) NOT NULL,
  `formatted_price` varchar(12) NOT NULL,
  PRIMARY KEY (`item_id`,`currency_code`),
  KEY `item_id` (`item_id`),
  CONSTRAINT `item_price_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_price`
--

LOCK TABLES `item_price` WRITE;
/*!40000 ALTER TABLE `item_price` DISABLE KEYS */;
INSERT INTO `item_price` VALUES (38,'USD',1.00,'$1');
INSERT INTO `item_price` VALUES (39,'USD',5.00,'$5');
INSERT INTO `item_price` VALUES (40,'USD',10.00,'$10');
INSERT INTO `item_price` VALUES (41,'USD',20.00,'$20');
INSERT INTO `item_price` VALUES (42,'USD',50.00,'$50');
INSERT INTO `item_price` VALUES (43,'USD',100.00,'$100');
/*!40000 ALTER TABLE `item_price` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt`
--

DROP TABLE IF EXISTS `receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receipt` (
  `receipt_id` int(10) NOT NULL AUTO_INCREMENT,
  `uuid` char(36) NOT NULL,
  `type` enum('ITUNES','GPLAY_ONETIME','GPLAY_RECURRING','CREDIT_CARD','COUPON','INTERNAL','WIN_STORE','AUTHNET_CREDIT_CARD','BRAINTREE_CREDIT_CARD') NOT NULL,
  `item_uuid` varchar(100) CHARACTER SET latin1 COLLATE latin1_general_cs DEFAULT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `payload` text,
  `store_uid` varchar(128) DEFAULT NULL,
  `expiration_date` datetime DEFAULT NULL,
  `skip_verify` tinyint(1) NOT NULL DEFAULT '0',
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `subscription_state` enum('ACTIVE','CANCELED') DEFAULT NULL,
  PRIMARY KEY (`receipt_id`),
  UNIQUE KEY `store_uid_UNIQUE` (`store_uid`),
  KEY `uuid` (`uuid`),
  KEY `subscriber_id` (`subscriber_id`),
  KEY `item_uuid_fk_idx_idx` (`item_uuid`),
  CONSTRAINT `item_uuid` FOREIGN KEY (`item_uuid`) REFERENCES `item` (`uuid`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3465447 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt`
--

LOCK TABLES `receipt` WRITE;
/*!40000 ALTER TABLE `receipt` DISABLE KEYS */;
/*!40000 ALTER TABLE `receipt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriber_entitlement`
--

DROP TABLE IF EXISTS `subscriber_entitlement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscriber_entitlement` (
  `subscriber_entitlement_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `uuid` char(36) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `entitlement_id` int(10) unsigned NOT NULL,
  `entitlement_uuid` char(36) DEFAULT NULL,
  `receipt_id` int(10) DEFAULT NULL,
  `delete_date` datetime DEFAULT NULL,
  `reserved_date` datetime DEFAULT NULL,
  `consumed_date` datetime DEFAULT NULL,
  `context_id` int(10) DEFAULT NULL,
  PRIMARY KEY (`subscriber_entitlement_id`),
  KEY `uuid` (`uuid`),
  KEY `entitlement_id` (`entitlement_id`),
  KEY `receipt_id` (`receipt_id`),
  KEY `subscriber_id` (`subscriber_id`),
  KEY `delete_date` (`delete_date`),
  CONSTRAINT `entitle_receipt_fk_idx` FOREIGN KEY (`receipt_id`) REFERENCES `receipt` (`receipt_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `subscriber_entitlement_ibfk_1` FOREIGN KEY (`entitlement_id`) REFERENCES `entitlement` (`entitlement_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23901377 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber_entitlement`
--

LOCK TABLES `subscriber_entitlement` WRITE;
/*!40000 ALTER TABLE `subscriber_entitlement` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscriber_entitlement` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `customer_profile_mapping`;
CREATE TABLE `customer_profile_mapping` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `customer_profile_id` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`subscriber_id`)
);

CREATE TABLE `store`.`braintree_customer` (
  `subscriber_id` INT NOT NULL AUTO_INCREMENT,
  `customer_id` VARCHAR(36) NOT NULL,
  PRIMARY KEY (`subscriber_id`));

--
-- Dumping events for database 'store'
--

--
-- Dumping routines for database 'store'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:21:03
CREATE DATABASE  IF NOT EXISTS `snowyowl` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `snowyowl`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: dc4-db1.shoutgameplay.com    Database: snowyowl
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

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

DROP TABLE IF EXISTS `phone_verification_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `phone_verification_code` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `phone` VARCHAR(45) NOT NULL,
  `code` VARCHAR(45) NOT NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`subscriber_id`, `code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bot_player`
--

DROP TABLE IF EXISTS `bot_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bot_player` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `busy_flag` int(1) NOT NULL DEFAULT '0',
  `game_id` char(36) DEFAULT NULL,
  `last_used_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bot_player`
--

LOCK TABLES `bot_player` WRITE;
/*!40000 ALTER TABLE `bot_player` DISABLE KEYS */;
/*!40000 ALTER TABLE `bot_player` ENABLE KEYS */;
UNLOCK TABLES;

CREATE TABLE `snowyowl`.`sponsor_cash_pool` (
  `sponsor_cash_pool_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`sponsor_cash_pool_id`),
  UNIQUE INDEX `subscriber_id_UNIQUE` (`subscriber_id` ASC));

CREATE TABLE `snowyowl`.`sponsor_player` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `busy_flag` int(1) NOT NULL DEFAULT '0',
  `game_id` char(36) DEFAULT NULL,
  `sponsor_cash_pool_id` int(10) unsigned NULL,
  `last_used_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `snowyowl`.`sponsor_cash_pool_transaction` (
  `sponsor_cash_pool_transaction_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `sponsor_cash_pool_id` INT UNSIGNED NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `reason` VARCHAR(45) NOT NULL,
  `transaction_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`sponsor_cash_pool_transaction_id`));

--
-- Table structure for table `match_question`
--

DROP TABLE IF EXISTS `match_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `match_question` (
  `id` char(36) NOT NULL,
  `game_id` char(36) NOT NULL,
  `round_id` char(36) NOT NULL,
  `match_id` char(36) NOT NULL,
  `question_id` char(36) NOT NULL,
  `question_value` varchar(45) NOT NULL,
  `match_question_status` varchar(45) NOT NULL,
  `won_subscriber_id` int(10) unsigned DEFAULT NULL,
  `determination` varchar(45) DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `completed_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `matchquestion_fk1_idx` (`question_id`),
  CONSTRAINT `matchquestion_fk1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `match_question`
--

LOCK TABLES `match_question` WRITE;
/*!40000 ALTER TABLE `match_question` DISABLE KEYS */;
/*!40000 ALTER TABLE `match_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `multi_localization`
--

DROP TABLE IF EXISTS `multi_localization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `multi_localization` (
  `uuid` char(36) NOT NULL,
  `type` varchar(45) NOT NULL,
  `language_code` varchar(5) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`uuid`, `type`, `language_code`),
  KEY `ml_type_idx` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `multi_localization`
--

LOCK TABLES `multi_localization` WRITE;
/*!40000 ALTER TABLE `multi_localization` DISABLE KEYS */;
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('835650ff-7e1e-11e7-970d-0242ac110004', 'systemMessage', 'en', 'Pool Play has begun for {0}');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('22dd185b-7e20-11e7-970d-0242ac110004', 'systemMessage', 'en', 'Tournament Play for {0} begins in {1} minutes. ${2} is on the line!');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('07362305-9e96-11e5-b784-86e93e99d7ba', 'systemMessage', 'en', 'Your verification code is: {0}');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('e1f9d489-7928-42ec-93c8-63fa53664885', 'systemMessage', 'en', 'Pool Play has begun');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('cd620a64-2534-45d0-98f6-1159c4443049', 'systemMessage', 'en', 'Tournament Play begins soon');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('7651664a-7327-4082-aec6-ebba179a4630', 'systemMessage', 'en', 'Pool Play has begun');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('15083728-33c5-4df5-bb66-cdf83f019e9b', 'systemMessage', 'en', 'Tournament Play begins soon');

INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('ee994158-0133-4bb7-af72-58e9176d567f', 'systemMessage', 'en', 'You won a cash prize!');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('2454f9d3-c16e-4d13-bd4f-5e0ffa8203c0', 'systemMessage', 'en', 'Congratulations! You won ${0,number,#,###.##}');

INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('00502dfe-e2ca-4085-b8a0-5501a1fb3cb1','answerText','en','Ultron');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('013ae99b-6714-4054-8469-272d1cdb2785','answerText','en','Melvin');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('02990deb-1e97-4f29-a814-a90577131701','answerText','en','Madonna');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('029f2e3c-6b06-4294-bced-7b41168e355f','answerText','en','Mars');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('0461489b-4b81-4864-bdfa-83ce38c82696','answerText','en','Crawling on walls');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('0a9a0e4b-783d-435c-895b-6a2fa3752d1a','answerText','en','Frederick Douglass');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('106cb44b-db27-4f6f-8acc-eccf4dc1b09c','answerText','en','Teen Titans');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('1687aa6b-28b2-4f67-9704-28015990ef97','answerText','en','Charles Atlas');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('1895bfff-4535-4b73-ae76-c0d79393f58f','answerText','en','Jimmy Carter');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('1d0c3607-f24e-4d9b-add4-0b101c3d4ffc','answerText','en','Squadron Supreme');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('22c3bcb8-f125-415f-96ad-7ffa06ee493f','answerText','en','The Daily Bugle');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('23b9e636-e014-49e0-9099-a0a27b7239ec','answerText','en','6');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('246c948c-8b29-41f0-92fa-aebb4f1b3c24','answerText','en','Joe Louis');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('270faa73-6b84-4e06-b4d7-46befeeb9cea','answerText','en','ammeter');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('2734a0c2-8e57-4326-b3ad-fabf706cb610','answerText','en','Greece');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('2af5f94c-ec23-4749-bf37-7e996c97f296','answerText','en','52');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('2b0b1f34-fec0-46e5-b589-e8f181c0707a','answerText','en','Adams');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('2b8d8501-5114-44bb-805a-fc319fb0987d','answerText','en','1980');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('2c5fd519-db8c-420b-854f-58abf6b957d8','answerText','en','The Daily News');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('2e1c0a23-15b4-425d-9a81-55c517003f82','answerText','en','Charlie Brown');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('33b04bcb-bda3-47e2-999c-77c23e08441d','answerText','en','50');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('3a566a14-f512-4b2b-b80b-94d2aa24ca47','answerText','en','40');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('41c41507-f1cd-493a-94e6-5166d0a12ce9','answerText','en','8');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('453ee7d5-bf60-41ce-ad17-2aa186571fe4','answerText','en','Guardians of the Galaxy');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('45593743-0b77-49bc-8d91-50755363f7bf','answerText','en','Egypt');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('46639975-4b5f-4c2d-9bbe-003a1e4c470e','answerText','en','Marlon Brando');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('48db42bc-738d-4b11-a178-bbb544e9bf22','answerText','en','Brainiac');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('49878a72-fb76-403b-8ca2-0bf09c3e6d22','answerText','en','48');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('52f6b0ea-bf8a-41a1-a131-341e01107fd8','answerText','en','Tony Stark');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('532bf8a1-0590-4ad0-a804-3bd709c98b1b','answerText','en','Fantastic Four');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('59cf8858-8930-44dc-80ff-2f6c9cb6bead','answerText','en','8');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('5a09c42d-359c-400e-942f-3d2ce0e99a8b','answerText','en','7');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('5c41c4e8-a525-4ab7-b8da-65630f53d3a7','answerText','en','10');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('5d9992d7-8e82-4352-a999-ece1f960f67b','answerText','en','Acrobat');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('5f010a33-0c6a-4160-b7cd-6889d3d5c2ab','answerText','en','Bugs Bunny');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('65b1e157-d054-447f-8fc2-4898d5e5bee3','answerText','en','Sub-Mariner');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('65f72d23-531f-45fb-abad-106118d96144','answerText','en','Psychologist');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('693411f2-8210-4905-84f6-20fe86beb1db','answerText','en','Hank Pym');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('696a5a04-e9d1-4107-8687-0205fb08cc5a','answerText','en','Chuck Norris');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('6a2cadfb-f862-4fa0-a777-cc7848019667','answerText','en','Roosevelt');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('6d9a1166-f8de-438f-964d-bb941f431992','answerText','en','Fred Flintstone');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('6fee8d76-500d-4ab5-94a6-64ddb0bfee03','answerText','en','Mercury');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('706a22ef-9147-48ec-b2bf-927be067f3c4','answerText','en','Kazar');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('7a9df7af-658e-466a-8deb-ea7334cc3d0a','answerText','en','40 years');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('7c5341c9-0f81-4437-b136-eaeafac74209','answerText','en','Oprah Winfrey');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('7db1a99a-f35c-4c4a-a7c1-00ec51ee9eed','answerText','en','anemometer');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('812026bd-251d-4542-8cc3-a6821622a86a','answerText','en','The Penguin');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('8232788d-da33-4761-a16c-f27fca26f03b','answerText','en','Burt Reynolds');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('872cfc32-46b5-443b-92ae-41b294f17aae','answerText','en','5');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('8a852fb8-17d4-4da5-b26b-ecacba1f6ed4','answerText','en','The Joker');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('8d470a8c-a202-4303-80e5-314b12e4105f','answerText','en','Mickey Mouse');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('8e9437b5-490c-4611-914b-f0c137837134','answerText','en','70 years');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('8f4d3f09-dea7-4f10-ae00-2f0da8cb31d6','answerText','en','Manville');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('8f54c5ba-0aa1-46f6-a464-b6201f65c9ae','answerText','en','Doom Patrol');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('9091d3a9-f67a-4e41-ac13-5f67b3f1c4a2','answerText','en','Avengers');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('91463b8c-0b85-4ae2-8b79-b093560efc72','answerText','en','5');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('957c6a74-40cf-4563-8afa-6ca0239cd759','answerText','en','3');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('98c39cfc-39c1-4eff-a827-77aa7082ca52','answerText','en','The Daily Globe');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('9bf90bea-a177-46d9-9fef-2f317aee2d0f','answerText','en','4');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('9cdbf02e-be5c-44fa-b243-281ae0f32202','answerText','en','50 years');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('9d9ac35d-56a8-4b01-82fd-0e31c9c0e9bb','answerText','en','hygrometer');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('9edd016f-1c12-4b8e-935c-a164d5618cf5','answerText','en','Doctor Strange');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('a14d719d-df4a-4fe4-bba1-53c2dd6bca72','answerText','en','Weapons expert');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('a17e50ef-3ea6-4717-8cca-7bc9ff4dcfa1','answerText','en','Darkness');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('a2686f53-54bb-4c94-ae86-8929fac61458','answerText','en','India');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('a47001b5-38b4-473b-97b0-ea69e21f0f12','answerText','en','Washington');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('a7c8294c-c573-431c-b547-51677a967f28','answerText','en','Wonder Man');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('aa5a0f3c-fba9-4c68-a718-1979211eda3e','answerText','en','Never');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('ae7d0668-cd68-4281-a647-80240f1ff8ec','answerText','en','Reed Richards');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('afc7f6ab-b475-4ab1-8cf0-8c43832a454f','answerText','en','Panama');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('b24d26f4-bc7b-42e3-b0b9-a2475e4f096d','answerText','en','China');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('b56e5b34-9014-4308-aef0-0b5a678cd894','answerText','en','Brazil');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('b74c1dbe-6d48-4085-9c27-cc7ca6989e0f','answerText','en','Venus');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('b946980e-b276-4d65-b84a-5d0542927a78','answerText','en','Justice League');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('b9d2a10d-0381-4fb3-a1f3-2bdb9351111e','answerText','en','3');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('bb9edb39-13ab-4a30-b19f-2640c51a8703','answerText','en','Bill Cosby');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('bc08d0fe-bf3f-42fe-80f6-08d25abadee6','answerText','en','Booker T. Washington');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('bc44e5d2-9ea6-4a72-babe-37209d0d49b9','answerText','en','United States');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('bd99cbe2-93f6-4314-9f0e-5ea260c09a2e','answerText','en','Mortimer');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('be11ddf1-15d6-4be3-bafb-07c0992f807a','answerText','en','Sylvester Stallone');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('c205eb52-d94e-4720-b8df-88bda40ad8de','answerText','en','Dolph Lundgren');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('c2a92361-303e-4c03-ae1c-b0282a7e2726','answerText','en','Super Strength');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('c4378c81-33e2-42f7-a1ff-79d49ac37faa','answerText','en','Jefferson');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('c4b73c73-18c5-4a2d-8709-dc3e1fba86c3','answerText','en','Yellow');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('c4c98cb1-1e6f-406f-971f-4c52c91728d1','answerText','en','The Daily Planet');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('c64e08c5-5537-4a6b-8233-5b3102aeda1e','answerText','en','John');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('cbed7313-2189-4547-aea5-cc7e9908e0b6','answerText','en','Ra\'s al Ghul');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('cc20c110-56be-4f63-928a-8b84751f9dbd','answerText','en','Coldness');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('d17d2958-e3f4-472c-bd65-729853f7f69c','answerText','en','6');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('d296d400-59eb-4c9f-b26c-ef769a07cb8d','answerText','en','Jerry');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('d43e1cd1-7867-45b5-9d1e-618cc34f5cfb','answerText','en','Louis Armstrong');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('d4769c56-90ae-4d77-8397-dd871e2acd79','answerText','en','Quicksilver');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('d83fe403-34d3-4eea-9db4-211ee87dbb52','answerText','en','Race car driver');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('dab29907-f918-4e1f-8e2a-b5a7c623e8dc','answerText','en','Black Panther');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('daf9ad05-b431-4e85-87ce-b8e5b43dff2e','answerText','en','1940');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('df6e8c6c-4de6-4590-b688-a4afeb71d814','answerText','en','1960');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('e132ed97-2f2b-4ac9-8f58-ef617a450c06','answerText','en','Spider Sense');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('e351f2dd-d6db-4fcc-be0d-62bea0a9d204','answerText','en','Shoot webbing');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('e5d42257-3143-4924-825b-fd85329beb40','answerText','en','barometer');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('ec2314c0-9af8-48ec-a4c9-3989f244625e','answerText','en','60 years');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('edb74466-10af-4967-9254-88e135ae65c3','answerText','en','9');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('ee7095a7-bb06-4534-ac72-31ec14b63299','answerText','en','James');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('eeebe464-a814-4740-832c-5c7bff15548d','answerText','en','Joseph');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f1860dbc-bfa7-4906-a6ca-5309e7ae9680','answerText','en','Magneto');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f1f5bd9c-c25a-4fd1-a186-3b7d001eee73','answerText','en','Charlton Heston');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f3b142da-3eb8-4149-82d8-dc5d085bd777','answerText','en','Iron Fist');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f3d73a65-8a3e-4ee1-ad9e-efbdf61d7719','answerText','en','Suicide Squad');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f68654a7-4f38-48ac-9a32-70e799a5f1d4','answerText','en','United States');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f7ec30f0-e623-4f6a-9f2b-234ad10eeafc','answerText','en','Earth');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('fbc5d555-f79e-4777-a36e-1b86fcf82858','answerText','en','Michael Jackson');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('fcf9739d-24ea-4e8d-a18c-4a4a86e751da','answerText','en','Women');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('ff62d3b2-da40-412c-a7f9-43c909181856','answerText','en','Murgatroyd');

INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('0ed9fa29-2b15-481b-8dc1-4456e28dac55','questionText','en','Who was the first black American pictured on a U.S. postage stamp?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('131581f6-5577-45b7-b18b-36f877788b2f','questionText','en','Who is the Scarlet Witch\'s twin brother?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4','questionText','en','The Philadelphia mint started putting a \"P\" mint mark on quarters when?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('1c4f5123-4529-487d-a599-f91e6010d25d','questionText','en','How many rings are on the Olympic Symbol?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a','questionText','en','Which actor has the real name of Charles Carter?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('286209d2-b10f-49f7-a249-0e54ab164b1f','questionText','en','Which of the following entertainers was the highest paid for the two years 1988 and 1989?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('31e7249f-a36d-4c86-a388-ab96eab45eda','questionText','en','Edwin Jarvis serves as butler to what superteam?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('49101a25-dad7-48b8-9875-55b15080ab6c','questionText','en','Who played the iconic fictional boxer \"Rocky Balboa\"?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('4e363b9f-a037-44fb-9d9d-d0339bbc7790','questionText','en','Who was the first president of the United States?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('53532282-0ba6-47e8-bf4a-826ab3ba8f70','questionText','en','Which of the following is NOT a power Spiderman received when bitten by a radioactive spider?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('56baa5a2-1f7b-43e2-82e2-0527582bdb25','questionText','en','Who created the Vision?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('5b0330a9-76e3-4ac5-b241-e2f7f33e5c57','questionText','en','What was the average life expectancy of white males born in the U.S. just before the Civil War?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('6134db67-8492-439c-bbbb-f5f88bfa7864','questionText','en','Where is the Suez Canal?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('65e3cd93-592c-4cd2-9318-1e27e9f1ca95','questionText','en','In J. Edgar Hoover, what did the J stand for?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('70913388-8772-4ebc-bef7-da36a04cd0e4','questionText','en','What was Harley Quinn\'s original occupation?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('797c8379-c00b-4626-8154-e871241648d9','questionText','en','T\'Challa, the king of Wakanda, is also known as what superhero?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('7c0968e8-4701-47bc-8f24-908ae5344483','questionText','en','What is Green Lantern\'s weakness?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('816e1014-2011-4bde-9b70-770f52552ed6','questionText','en','What is the most populated country on earth?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('81de52ec-6f4d-4497-aa9a-567a76486002','questionText','en','Which of these characters turned 60 years old in 2010?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('866c9949-a42f-498f-9974-9a046e5af4ba','questionText','en','Which planet is closest to the sun?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('9bd8277a-a2bf-4471-a349-76816fb5e081','questionText','en','How many legs do Arachnids have?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('ba9b1a82-7dba-4aab-9295-85e9ba7fec5c','questionText','en','What was Mickey Mouse\'s original name?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8','questionText','en','How many states are in the USA?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('bf05f0ef-aa9a-432e-b232-bda90e13970f','questionText','en','Which of these villains is NOT an enemy of Batman?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('dd402319-975e-4807-acb7-209ab76f9d09','questionText','en','Which one of the following instruments is used to measure humidity?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f08c7e48-d32c-47ae-9274-e71a4b3fc8a3','questionText','en','What newspaper does Peter Parker work for?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('fc5c29cf-4300-4f28-9a32-8bfc938624ee','questionText','en','What superteam includes a sentient tree-like creature?');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('fdfcd6d0-2623-4c84-b770-6b6e2c27ee38','questionText','en','How many U.S. States border the Gulf of Mexico?');

INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('0b47f8f2-143c-4765-b904-2c4b8675c899','categoryName','en','Olympics');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('1a144f5f-e7f8-4086-ae01-fc0d45551c97','categoryName','en','Astronomy');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('21850e7d-158e-11e7-a82c-0242ac110004','categoryName','en','Comics');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('2e9f4804-560f-45de-ad39-a20c69920232','categoryName','en','Geography');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('36087979-7ad4-4b7d-875e-86cc173dcf65','categoryName','en','Science');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('6046e61a-6733-11e7-970d-0242ac110004','categoryName','en','Music');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('a6c007e7-444f-435c-8865-538b2adc8a9d','categoryName','en','U.S. History');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('ade9c90b-6733-11e7-970d-0242ac110004','categoryName','en','TV Shows');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('b504d206-7ebe-42e1-aa31-2925b4fe8e25','categoryName','en','Movies');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f5b450f1-997c-42fd-8022-4a211c8e3f5e','categoryName','en','Entertainment');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`,`type`,`language_code`,`value`) VALUES ('f8ac519f-c590-478e-bed1-19a953486d1e','categoryName','en','General Knowledge');

INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('d335b70e-76d8-4c87-b73f-2bc56bb1a214', 'systemMessage', 'en', 'The game is about to begin! Join now for a chance to win.');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('d8d50785-279d-48e1-9b90-0d32585c86d3', 'systemMessage', 'en', 'The game is about to begin');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('c8cdbdb1-2e5a-4169-87ae-ba3669ade8c7', 'systemMessage', 'en', 'Join now for a chance to win!');
INSERT INTO `snowyowl`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('987a2362-4d09-49f5-a771-fc64ef4fa420', 'systemMessage', 'en', 'Join now for a chance to win. Click <a href=\"https://{0}/play/game/{1}\">here</a>.');

/*!40000 ALTER TABLE `multi_localization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question` (
  `id` char(36) NOT NULL,
  `difficulty` int(11) NOT NULL,
  `source` varchar(255) DEFAULT NULL,
  `media_url` varchar(255) DEFAULT NULL,
  `media_type` varchar(45) DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expiration_date` timestamp NULL DEFAULT NULL,
  `usage_count` int(11) NOT NULL DEFAULT '0',
  `status` varchar(45) NOT NULL DEFAULT 'PUBLISHED',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('0ed9fa29-2b15-481b-8dc1-4456e28dac55',0,NULL,NULL,NULL,'2017-08-07 14:01:27',NULL,30,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('131581f6-5577-45b7-b18b-36f877788b2f',0,NULL,NULL,NULL,'2017-07-25 16:10:06',NULL,33,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4',0,NULL,NULL,NULL,'2017-08-07 13:44:48',NULL,27,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('1c4f5123-4529-487d-a599-f91e6010d25d',0,NULL,NULL,NULL,'2017-08-07 13:24:41',NULL,34,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a',0,NULL,NULL,NULL,'2017-07-25 16:09:33',NULL,38,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('286209d2-b10f-49f7-a249-0e54ab164b1f',0,NULL,NULL,NULL,'2017-07-28 08:37:13',NULL,29,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('31e7249f-a36d-4c86-a388-ab96eab45eda',0,NULL,NULL,NULL,'2017-07-28 08:37:13',NULL,24,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('49101a25-dad7-48b8-9875-55b15080ab6c',0,NULL,NULL,NULL,'2017-07-12 01:53:54',NULL,40,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('4e363b9f-a037-44fb-9d9d-d0339bbc7790',0,NULL,NULL,NULL,'2017-07-28 08:36:50',NULL,36,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('53532282-0ba6-47e8-bf4a-826ab3ba8f70',0,NULL,NULL,NULL,'2017-08-07 13:44:18',NULL,33,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('56baa5a2-1f7b-43e2-82e2-0527582bdb25',0,NULL,NULL,NULL,'2017-07-21 02:15:29',NULL,26,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('5b0330a9-76e3-4ac5-b241-e2f7f33e5c57',0,NULL,NULL,NULL,'2017-08-07 13:23:41',NULL,28,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('6134db67-8492-439c-bbbb-f5f88bfa7864',0,NULL,NULL,NULL,'2017-07-21 01:06:40',NULL,36,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('65e3cd93-592c-4cd2-9318-1e27e9f1ca95',0,NULL,NULL,NULL,'2017-08-07 14:29:28',NULL,33,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('70913388-8772-4ebc-bef7-da36a04cd0e4',5,NULL,NULL,NULL,'2017-08-07 13:54:26',NULL,3,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('797c8379-c00b-4626-8154-e871241648d9',0,NULL,NULL,NULL,'2017-07-25 16:10:17',NULL,24,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('7c0968e8-4701-47bc-8f24-908ae5344483',0,NULL,NULL,NULL,'2017-08-07 13:53:26',NULL,30,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('816e1014-2011-4bde-9b70-770f52552ed6',0,NULL,NULL,NULL,'2017-08-04 22:36:49',NULL,33,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('81de52ec-6f4d-4497-aa9a-567a76486002',0,NULL,NULL,NULL,'2017-08-07 14:30:01',NULL,36,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('866c9949-a42f-498f-9974-9a046e5af4ba',0,NULL,NULL,NULL,'2017-08-07 14:02:16',NULL,22,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('9bd8277a-a2bf-4471-a349-76816fb5e081',0,NULL,NULL,NULL,'2017-08-07 13:45:19',NULL,33,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('ba9b1a82-7dba-4aab-9295-85e9ba7fec5c',0,NULL,NULL,NULL,'2017-08-07 14:03:16',NULL,34,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8',0,NULL,NULL,NULL,'2017-08-07 13:23:11',NULL,26,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('bf05f0ef-aa9a-432e-b232-bda90e13970f',0,NULL,NULL,NULL,'2017-08-04 23:46:26',NULL,27,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('dd402319-975e-4807-acb7-209ab76f9d09',0,NULL,NULL,NULL,'2017-07-28 08:36:50',NULL,28,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('f08c7e48-d32c-47ae-9274-e71a4b3fc8a3',0,NULL,NULL,NULL,'2017-08-07 17:06:43',NULL,37,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('fc5c29cf-4300-4f28-9a32-8bfc938624ee',0,NULL,NULL,NULL,'2017-08-07 13:53:11',NULL,36,'PUBLISHED');
INSERT INTO `snowyowl`.`question` (`id`,`difficulty`,`source`,`media_url`,`media_type`,`create_date`,`expiration_date`,`usage_count`,`status`) VALUES ('fdfcd6d0-2623-4c84-b770-6b6e2c27ee38',0,NULL,NULL,NULL,'2017-08-04 23:45:26',NULL,38,'PUBLISHED');
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_answer`
--

DROP TABLE IF EXISTS `question_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_answer` (
  `id` char(36) NOT NULL,
  `question_id` char(36) NOT NULL,
  `media_url` varchar(255) DEFAULT NULL,
  `media_type` varchar(45) DEFAULT NULL,
  `correct` int(1) DEFAULT NULL,
  `survey_percent` int(11) DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `question_answer_fk1_idx` (`question_id`),
  CONSTRAINT `question_answer_fk1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_answer`
--

LOCK TABLES `question_answer` WRITE;
/*!40000 ALTER TABLE `question_answer` DISABLE KEYS */;
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('00502dfe-e2ca-4085-b8a0-5501a1fb3cb1','56baa5a2-1f7b-43e2-82e2-0527582bdb25',NULL,NULL,1,NULL,'2017-03-30 21:32:28');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('013ae99b-6714-4054-8469-272d1cdb2785','ba9b1a82-7dba-4aab-9295-85e9ba7fec5c',NULL,NULL,0,NULL,'2017-03-14 18:37:55');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('02990deb-1e97-4f29-a814-a90577131701','286209d2-b10f-49f7-a249-0e54ab164b1f',NULL,NULL,0,NULL,'2017-03-14 18:38:44');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('029f2e3c-6b06-4294-bced-7b41168e355f','866c9949-a42f-498f-9974-9a046e5af4ba',NULL,NULL,0,NULL,'2017-03-14 18:24:48');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('0461489b-4b81-4864-bdfa-83ce38c82696','53532282-0ba6-47e8-bf4a-826ab3ba8f70',NULL,NULL,0,NULL,'2017-03-30 21:18:29');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('0a9a0e4b-783d-435c-895b-6a2fa3752d1a','0ed9fa29-2b15-481b-8dc1-4456e28dac55',NULL,NULL,0,NULL,'2017-03-14 18:36:54');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('106cb44b-db27-4f6f-8acc-eccf4dc1b09c','31e7249f-a36d-4c86-a388-ab96eab45eda',NULL,NULL,0,NULL,'2017-03-30 21:24:16');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('1687aa6b-28b2-4f67-9704-28015990ef97','257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a',NULL,NULL,0,NULL,'2017-03-14 18:39:39');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('1895bfff-4535-4b73-ae76-c0d79393f58f','257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a',NULL,NULL,0,NULL,'2017-03-14 18:39:38');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('1d0c3607-f24e-4d9b-add4-0b101c3d4ffc','fc5c29cf-4300-4f28-9a32-8bfc938624ee',NULL,NULL,0,NULL,'2017-03-30 21:25:50');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('22c3bcb8-f125-415f-96ad-7ffa06ee493f','f08c7e48-d32c-47ae-9274-e71a4b3fc8a3',NULL,NULL,1,NULL,'2017-03-30 21:30:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('23b9e636-e014-49e0-9099-a0a27b7239ec','1c4f5123-4529-487d-a599-f91e6010d25d',NULL,NULL,0,NULL,'2017-03-14 18:32:04');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('246c948c-8b29-41f0-92fa-aebb4f1b3c24','0ed9fa29-2b15-481b-8dc1-4456e28dac55',NULL,NULL,1,NULL,'2017-03-14 18:36:55');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('270faa73-6b84-4e06-b4d7-46befeeb9cea','dd402319-975e-4807-acb7-209ab76f9d09',NULL,NULL,0,NULL,'2017-03-14 18:41:26');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('2734a0c2-8e57-4326-b3ad-fabf706cb610','6134db67-8492-439c-bbbb-f5f88bfa7864',NULL,NULL,0,NULL,'2017-03-14 18:23:57');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('2af5f94c-ec23-4749-bf37-7e996c97f296','bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8',NULL,NULL,0,NULL,'2017-03-14 18:27:24');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('2b0b1f34-fec0-46e5-b589-e8f181c0707a','4e363b9f-a037-44fb-9d9d-d0339bbc7790',NULL,NULL,0,NULL,'2017-03-14 18:22:33');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('2b8d8501-5114-44bb-805a-fc319fb0987d','1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4',NULL,NULL,1,NULL,'2017-03-14 18:35:15');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('2c5fd519-db8c-420b-854f-58abf6b957d8','f08c7e48-d32c-47ae-9274-e71a4b3fc8a3',NULL,NULL,0,NULL,'2017-03-30 21:30:33');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('2e1c0a23-15b4-425d-9a81-55c517003f82','81de52ec-6f4d-4497-aa9a-567a76486002',NULL,NULL,1,NULL,'2017-03-14 18:34:21');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('33b04bcb-bda3-47e2-999c-77c23e08441d','bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8',NULL,NULL,1,NULL,'2017-03-14 18:27:23');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('3a566a14-f512-4b2b-b80b-94d2aa24ca47','bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8',NULL,NULL,0,NULL,'2017-03-14 18:27:23');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('41c41507-f1cd-493a-94e6-5166d0a12ce9','1c4f5123-4529-487d-a599-f91e6010d25d',NULL,NULL,0,NULL,'2017-03-14 18:32:04');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('453ee7d5-bf60-41ce-ad17-2aa186571fe4','fc5c29cf-4300-4f28-9a32-8bfc938624ee',NULL,NULL,1,NULL,'2017-03-30 21:25:49');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('45593743-0b77-49bc-8d91-50755363f7bf','6134db67-8492-439c-bbbb-f5f88bfa7864',NULL,NULL,1,NULL,'2017-03-14 18:23:56');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('46639975-4b5f-4c2d-9bbe-003a1e4c470e','49101a25-dad7-48b8-9875-55b15080ab6c',NULL,NULL,0,NULL,'2017-03-14 18:33:19');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('48db42bc-738d-4b11-a178-bbb544e9bf22','bf05f0ef-aa9a-432e-b232-bda90e13970f',NULL,NULL,1,NULL,'2017-03-30 21:21:44');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('49878a72-fb76-403b-8ca2-0bf09c3e6d22','bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8',NULL,NULL,0,NULL,'2017-03-14 18:27:23');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('52f6b0ea-bf8a-41a1-a131-341e01107fd8','56baa5a2-1f7b-43e2-82e2-0527582bdb25',NULL,NULL,0,NULL,'2017-03-30 21:32:27');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('532bf8a1-0590-4ad0-a804-3bd709c98b1b','31e7249f-a36d-4c86-a388-ab96eab45eda',NULL,NULL,0,NULL,'2017-03-30 21:24:15');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('59cf8858-8930-44dc-80ff-2f6c9cb6bead','9bd8277a-a2bf-4471-a349-76816fb5e081',NULL,NULL,1,NULL,'2017-03-14 17:49:19');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('5a09c42d-359c-400e-942f-3d2ce0e99a8b','fdfcd6d0-2623-4c84-b770-6b6e2c27ee38',NULL,NULL,0,NULL,'2017-03-14 18:26:42');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('5c41c4e8-a525-4ab7-b8da-65630f53d3a7','9bd8277a-a2bf-4471-a349-76816fb5e081',NULL,NULL,0,NULL,'2017-03-14 17:49:20');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('5d9992d7-8e82-4352-a999-ece1f960f67b','70913388-8772-4ebc-bef7-da36a04cd0e4',NULL,NULL,0,NULL,'2017-07-06 21:12:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('5f010a33-0c6a-4160-b7cd-6889d3d5c2ab','81de52ec-6f4d-4497-aa9a-567a76486002',NULL,NULL,0,NULL,'2017-03-14 18:34:20');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('65b1e157-d054-447f-8fc2-4898d5e5bee3','797c8379-c00b-4626-8154-e871241648d9',NULL,NULL,0,NULL,'2017-03-30 21:26:52');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('65f72d23-531f-45fb-abad-106118d96144','70913388-8772-4ebc-bef7-da36a04cd0e4',NULL,NULL,1,NULL,'2017-07-06 21:12:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('693411f2-8210-4905-84f6-20fe86beb1db','56baa5a2-1f7b-43e2-82e2-0527582bdb25',NULL,NULL,0,NULL,'2017-03-30 21:32:27');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('696a5a04-e9d1-4107-8687-0205fb08cc5a','257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a',NULL,NULL,0,NULL,'2017-03-14 18:39:38');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('6a2cadfb-f862-4fa0-a777-cc7848019667','4e363b9f-a037-44fb-9d9d-d0339bbc7790',NULL,NULL,0,NULL,'2017-03-14 18:22:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('6d9a1166-f8de-438f-964d-bb941f431992','81de52ec-6f4d-4497-aa9a-567a76486002',NULL,NULL,0,NULL,'2017-03-14 18:34:21');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('6fee8d76-500d-4ab5-94a6-64ddb0bfee03','866c9949-a42f-498f-9974-9a046e5af4ba',NULL,NULL,1,NULL,'2017-03-14 18:24:48');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('706a22ef-9147-48ec-b2bf-927be067f3c4','797c8379-c00b-4626-8154-e871241648d9',NULL,NULL,0,NULL,'2017-03-30 21:26:52');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('7a9df7af-658e-466a-8deb-ea7334cc3d0a','5b0330a9-76e3-4ac5-b241-e2f7f33e5c57',NULL,NULL,1,NULL,'2017-03-14 18:40:39');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('7c5341c9-0f81-4437-b136-eaeafac74209','286209d2-b10f-49f7-a249-0e54ab164b1f',NULL,NULL,0,NULL,'2017-03-14 18:38:45');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('7db1a99a-f35c-4c4a-a7c1-00ec51ee9eed','dd402319-975e-4807-acb7-209ab76f9d09',NULL,NULL,0,NULL,'2017-03-14 18:41:26');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('812026bd-251d-4542-8cc3-a6821622a86a','bf05f0ef-aa9a-432e-b232-bda90e13970f',NULL,NULL,0,NULL,'2017-03-30 21:21:45');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('8232788d-da33-4761-a16c-f27fca26f03b','49101a25-dad7-48b8-9875-55b15080ab6c',NULL,NULL,0,NULL,'2017-03-14 18:33:18');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('872cfc32-46b5-443b-92ae-41b294f17aae','fdfcd6d0-2623-4c84-b770-6b6e2c27ee38',NULL,NULL,1,NULL,'2017-03-14 18:26:41');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('8a852fb8-17d4-4da5-b26b-ecacba1f6ed4','bf05f0ef-aa9a-432e-b232-bda90e13970f',NULL,NULL,0,NULL,'2017-03-30 21:21:44');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('8d470a8c-a202-4303-80e5-314b12e4105f','81de52ec-6f4d-4497-aa9a-567a76486002',NULL,NULL,0,NULL,'2017-03-14 18:34:21');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('8e9437b5-490c-4611-914b-f0c137837134','5b0330a9-76e3-4ac5-b241-e2f7f33e5c57',NULL,NULL,0,NULL,'2017-03-14 18:40:40');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('8f4d3f09-dea7-4f10-ae00-2f0da8cb31d6','ba9b1a82-7dba-4aab-9295-85e9ba7fec5c',NULL,NULL,0,NULL,'2017-03-14 18:37:54');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('8f54c5ba-0aa1-46f6-a464-b6201f65c9ae','fc5c29cf-4300-4f28-9a32-8bfc938624ee',NULL,NULL,0,NULL,'2017-03-30 21:25:49');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('9091d3a9-f67a-4e41-ac13-5f67b3f1c4a2','31e7249f-a36d-4c86-a388-ab96eab45eda',NULL,NULL,1,NULL,'2017-03-30 21:24:15');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('91463b8c-0b85-4ae2-8b79-b093560efc72','1c4f5123-4529-487d-a599-f91e6010d25d',NULL,NULL,1,NULL,'2017-03-14 18:32:03');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('957c6a74-40cf-4563-8afa-6ca0239cd759','fdfcd6d0-2623-4c84-b770-6b6e2c27ee38',NULL,NULL,0,NULL,'2017-03-14 18:26:41');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('98c39cfc-39c1-4eff-a827-77aa7082ca52','f08c7e48-d32c-47ae-9274-e71a4b3fc8a3',NULL,NULL,0,NULL,'2017-03-30 21:30:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('9bf90bea-a177-46d9-9fef-2f317aee2d0f','9bd8277a-a2bf-4471-a349-76816fb5e081',NULL,NULL,0,NULL,'2017-03-14 17:49:19');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('9cdbf02e-be5c-44fa-b243-281ae0f32202','5b0330a9-76e3-4ac5-b241-e2f7f33e5c57',NULL,NULL,0,NULL,'2017-03-14 18:40:39');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('9d9ac35d-56a8-4b01-82fd-0e31c9c0e9bb','dd402319-975e-4807-acb7-209ab76f9d09',NULL,NULL,1,NULL,'2017-03-14 18:41:26');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('9edd016f-1c12-4b8e-935c-a164d5618cf5','131581f6-5577-45b7-b18b-36f877788b2f',NULL,NULL,0,NULL,'2017-03-30 21:28:06');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('a14d719d-df4a-4fe4-bba1-53c2dd6bca72','70913388-8772-4ebc-bef7-da36a04cd0e4',NULL,NULL,0,NULL,'2017-07-06 21:12:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('a17e50ef-3ea6-4717-8cca-7bc9ff4dcfa1','7c0968e8-4701-47bc-8f24-908ae5344483',NULL,NULL,0,NULL,'2017-03-30 21:33:53');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('a2686f53-54bb-4c94-ae86-8929fac61458','816e1014-2011-4bde-9b70-770f52552ed6',NULL,NULL,0,NULL,'2017-03-14 18:25:42');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('a47001b5-38b4-473b-97b0-ea69e21f0f12','4e363b9f-a037-44fb-9d9d-d0339bbc7790',NULL,NULL,1,NULL,'2017-03-14 18:22:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('a7c8294c-c573-431c-b547-51677a967f28','131581f6-5577-45b7-b18b-36f877788b2f',NULL,NULL,0,NULL,'2017-03-30 21:28:05');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('aa5a0f3c-fba9-4c68-a718-1979211eda3e','1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4',NULL,NULL,0,NULL,'2017-03-14 18:35:16');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('ae7d0668-cd68-4281-a647-80240f1ff8ec','56baa5a2-1f7b-43e2-82e2-0527582bdb25',NULL,NULL,0,NULL,'2017-03-30 21:32:27');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('afc7f6ab-b475-4ab1-8cf0-8c43832a454f','6134db67-8492-439c-bbbb-f5f88bfa7864',NULL,NULL,0,NULL,'2017-03-14 18:23:57');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('b24d26f4-bc7b-42e3-b0b9-a2475e4f096d','816e1014-2011-4bde-9b70-770f52552ed6',NULL,NULL,1,NULL,'2017-03-14 18:25:42');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('b56e5b34-9014-4308-aef0-0b5a678cd894','816e1014-2011-4bde-9b70-770f52552ed6',NULL,NULL,0,NULL,'2017-03-14 18:25:42');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('b74c1dbe-6d48-4085-9c27-cc7ca6989e0f','866c9949-a42f-498f-9974-9a046e5af4ba',NULL,NULL,0,NULL,'2017-03-14 18:24:48');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('b946980e-b276-4d65-b84a-5d0542927a78','31e7249f-a36d-4c86-a388-ab96eab45eda',NULL,NULL,0,NULL,'2017-03-30 21:24:15');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('b9d2a10d-0381-4fb3-a1f3-2bdb9351111e','1c4f5123-4529-487d-a599-f91e6010d25d',NULL,NULL,0,NULL,'2017-03-14 18:32:03');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('bb9edb39-13ab-4a30-b19f-2640c51a8703','286209d2-b10f-49f7-a249-0e54ab164b1f',NULL,NULL,0,NULL,'2017-03-14 18:38:44');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('bc08d0fe-bf3f-42fe-80f6-08d25abadee6','0ed9fa29-2b15-481b-8dc1-4456e28dac55',NULL,NULL,0,NULL,'2017-03-14 18:36:55');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('bc44e5d2-9ea6-4a72-babe-37209d0d49b9','6134db67-8492-439c-bbbb-f5f88bfa7864',NULL,NULL,0,NULL,'2017-03-14 18:23:57');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('bd99cbe2-93f6-4314-9f0e-5ea260c09a2e','ba9b1a82-7dba-4aab-9295-85e9ba7fec5c',NULL,NULL,1,NULL,'2017-03-14 18:37:55');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('be11ddf1-15d6-4be3-bafb-07c0992f807a','49101a25-dad7-48b8-9875-55b15080ab6c',NULL,NULL,1,NULL,'2017-03-14 18:33:19');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('c205eb52-d94e-4720-b8df-88bda40ad8de','49101a25-dad7-48b8-9875-55b15080ab6c',NULL,NULL,0,NULL,'2017-03-14 18:33:19');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('c2a92361-303e-4c03-ae1c-b0282a7e2726','53532282-0ba6-47e8-bf4a-826ab3ba8f70',NULL,NULL,0,NULL,'2017-03-30 21:18:28');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('c4378c81-33e2-42f7-a1ff-79d49ac37faa','4e363b9f-a037-44fb-9d9d-d0339bbc7790',NULL,NULL,0,NULL,'2017-03-14 18:22:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('c4b73c73-18c5-4a2d-8709-dc3e1fba86c3','7c0968e8-4701-47bc-8f24-908ae5344483',NULL,NULL,1,NULL,'2017-03-30 21:33:54');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('c4c98cb1-1e6f-406f-971f-4c52c91728d1','f08c7e48-d32c-47ae-9274-e71a4b3fc8a3',NULL,NULL,0,NULL,'2017-03-30 21:30:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('c64e08c5-5537-4a6b-8233-5b3102aeda1e','65e3cd93-592c-4cd2-9318-1e27e9f1ca95',NULL,NULL,1,NULL,'2017-03-14 18:36:00');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('cbed7313-2189-4547-aea5-cc7e9908e0b6','bf05f0ef-aa9a-432e-b232-bda90e13970f',NULL,NULL,0,NULL,'2017-03-30 21:21:44');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('cc20c110-56be-4f63-928a-8b84751f9dbd','7c0968e8-4701-47bc-8f24-908ae5344483',NULL,NULL,0,NULL,'2017-03-30 21:33:54');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('d17d2958-e3f4-472c-bd65-729853f7f69c','9bd8277a-a2bf-4471-a349-76816fb5e081',NULL,NULL,0,NULL,'2017-03-14 17:49:19');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('d296d400-59eb-4c9f-b26c-ef769a07cb8d','65e3cd93-592c-4cd2-9318-1e27e9f1ca95',NULL,NULL,0,NULL,'2017-03-14 18:36:01');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('d43e1cd1-7867-45b5-9d1e-618cc34f5cfb','0ed9fa29-2b15-481b-8dc1-4456e28dac55',NULL,NULL,0,NULL,'2017-03-14 18:36:55');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('d4769c56-90ae-4d77-8397-dd871e2acd79','131581f6-5577-45b7-b18b-36f877788b2f',NULL,NULL,1,NULL,'2017-03-30 21:28:05');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('d83fe403-34d3-4eea-9db4-211ee87dbb52','70913388-8772-4ebc-bef7-da36a04cd0e4',NULL,NULL,0,NULL,'2017-07-06 21:12:34');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('dab29907-f918-4e1f-8e2a-b5a7c623e8dc','797c8379-c00b-4626-8154-e871241648d9',NULL,NULL,1,NULL,'2017-03-30 21:26:52');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('daf9ad05-b431-4e85-87ce-b8e5b43dff2e','1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4',NULL,NULL,0,NULL,'2017-03-14 18:35:15');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('df6e8c6c-4de6-4590-b688-a4afeb71d814','1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4',NULL,NULL,0,NULL,'2017-03-14 18:35:15');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('e132ed97-2f2b-4ac9-8f58-ef617a450c06','53532282-0ba6-47e8-bf4a-826ab3ba8f70',NULL,NULL,0,NULL,'2017-03-30 21:18:29');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('e351f2dd-d6db-4fcc-be0d-62bea0a9d204','53532282-0ba6-47e8-bf4a-826ab3ba8f70',NULL,NULL,1,NULL,'2017-03-30 21:18:29');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('e5d42257-3143-4924-825b-fd85329beb40','dd402319-975e-4807-acb7-209ab76f9d09',NULL,NULL,0,NULL,'2017-03-14 18:41:27');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('ec2314c0-9af8-48ec-a4c9-3989f244625e','5b0330a9-76e3-4ac5-b241-e2f7f33e5c57',NULL,NULL,0,NULL,'2017-03-14 18:40:40');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('edb74466-10af-4967-9254-88e135ae65c3','fdfcd6d0-2623-4c84-b770-6b6e2c27ee38',NULL,NULL,0,NULL,'2017-03-14 18:26:42');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('ee7095a7-bb06-4534-ac72-31ec14b63299','65e3cd93-592c-4cd2-9318-1e27e9f1ca95',NULL,NULL,0,NULL,'2017-03-14 18:36:00');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('eeebe464-a814-4740-832c-5c7bff15548d','65e3cd93-592c-4cd2-9318-1e27e9f1ca95',NULL,NULL,0,NULL,'2017-03-14 18:36:01');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('f1860dbc-bfa7-4906-a6ca-5309e7ae9680','131581f6-5577-45b7-b18b-36f877788b2f',NULL,NULL,0,NULL,'2017-03-30 21:28:05');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('f1f5bd9c-c25a-4fd1-a186-3b7d001eee73','257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a',NULL,NULL,1,NULL,'2017-03-14 18:39:39');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('f3b142da-3eb8-4149-82d8-dc5d085bd777','797c8379-c00b-4626-8154-e871241648d9',NULL,NULL,0,NULL,'2017-03-30 21:26:53');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('f3d73a65-8a3e-4ee1-ad9e-efbdf61d7719','fc5c29cf-4300-4f28-9a32-8bfc938624ee',NULL,NULL,0,NULL,'2017-03-30 21:25:49');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('f68654a7-4f38-48ac-9a32-70e799a5f1d4','816e1014-2011-4bde-9b70-770f52552ed6',NULL,NULL,0,NULL,'2017-03-14 18:25:43');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('f7ec30f0-e623-4f6a-9f2b-234ad10eeafc','866c9949-a42f-498f-9974-9a046e5af4ba',NULL,NULL,0,NULL,'2017-03-14 18:24:47');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('fbc5d555-f79e-4777-a36e-1b86fcf82858','286209d2-b10f-49f7-a249-0e54ab164b1f',NULL,NULL,1,NULL,'2017-03-14 18:38:44');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('fcf9739d-24ea-4e8d-a18c-4a4a86e751da','7c0968e8-4701-47bc-8f24-908ae5344483',NULL,NULL,0,NULL,'2017-03-30 21:33:54');
INSERT INTO `snowyowl`.`question_answer` (`id`,`question_id`,`media_url`,`media_type`,`correct`,`survey_percent`,`create_date`) VALUES ('ff62d3b2-da40-412c-a7f9-43c909181856','ba9b1a82-7dba-4aab-9295-85e9ba7fec5c',NULL,NULL,0,NULL,'2017-03-14 18:37:55');
/*!40000 ALTER TABLE `question_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_categories`
--

DROP TABLE IF EXISTS `question_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_categories` (
  `question_id` char(36) NOT NULL,
  `category_id` char(36) NOT NULL,
  PRIMARY KEY (`question_id`,`category_id`),
  KEY `question_category_fk2_idx` (`category_id`),
  CONSTRAINT `question_category_fk1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `question_category_fk2` FOREIGN KEY (`category_id`) REFERENCES `question_category_list` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_categories`
--

LOCK TABLES `question_categories` WRITE;
/*!40000 ALTER TABLE `question_categories` DISABLE KEYS */;
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('1c4f5123-4529-487d-a599-f91e6010d25d','0b47f8f2-143c-4765-b904-2c4b8675c899');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('866c9949-a42f-498f-9974-9a046e5af4ba','1a144f5f-e7f8-4086-ae01-fc0d45551c97');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('131581f6-5577-45b7-b18b-36f877788b2f','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('31e7249f-a36d-4c86-a388-ab96eab45eda','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('53532282-0ba6-47e8-bf4a-826ab3ba8f70','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('56baa5a2-1f7b-43e2-82e2-0527582bdb25','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('70913388-8772-4ebc-bef7-da36a04cd0e4','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('797c8379-c00b-4626-8154-e871241648d9','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('7c0968e8-4701-47bc-8f24-908ae5344483','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('bf05f0ef-aa9a-432e-b232-bda90e13970f','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('f08c7e48-d32c-47ae-9274-e71a4b3fc8a3','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('fc5c29cf-4300-4f28-9a32-8bfc938624ee','21850e7d-158e-11e7-a82c-0242ac110004');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('6134db67-8492-439c-bbbb-f5f88bfa7864','2e9f4804-560f-45de-ad39-a20c69920232');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8','2e9f4804-560f-45de-ad39-a20c69920232');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('fdfcd6d0-2623-4c84-b770-6b6e2c27ee38','2e9f4804-560f-45de-ad39-a20c69920232');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('5b0330a9-76e3-4ac5-b241-e2f7f33e5c57','36087979-7ad4-4b7d-875e-86cc173dcf65');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('dd402319-975e-4807-acb7-209ab76f9d09','36087979-7ad4-4b7d-875e-86cc173dcf65');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('0ed9fa29-2b15-481b-8dc1-4456e28dac55','a6c007e7-444f-435c-8865-538b2adc8a9d');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4','a6c007e7-444f-435c-8865-538b2adc8a9d');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('4e363b9f-a037-44fb-9d9d-d0339bbc7790','a6c007e7-444f-435c-8865-538b2adc8a9d');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('65e3cd93-592c-4cd2-9318-1e27e9f1ca95','a6c007e7-444f-435c-8865-538b2adc8a9d');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('1c4f5123-4529-487d-a599-f91e6010d25d','c036632e-eaf9-43f5-b9a9-ef778bc5b225');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a','f5b450f1-997c-42fd-8022-4a211c8e3f5e');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('286209d2-b10f-49f7-a249-0e54ab164b1f','f5b450f1-997c-42fd-8022-4a211c8e3f5e');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('49101a25-dad7-48b8-9875-55b15080ab6c','f5b450f1-997c-42fd-8022-4a211c8e3f5e');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('81de52ec-6f4d-4497-aa9a-567a76486002','f5b450f1-997c-42fd-8022-4a211c8e3f5e');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('ba9b1a82-7dba-4aab-9295-85e9ba7fec5c','f5b450f1-997c-42fd-8022-4a211c8e3f5e');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('816e1014-2011-4bde-9b70-770f52552ed6','f8ac519f-c590-478e-bed1-19a953486d1e');
INSERT INTO `snowyowl`.`question_categories` (`question_id`,`category_id`) VALUES ('9bd8277a-a2bf-4471-a349-76816fb5e081','f8ac519f-c590-478e-bed1-19a953486d1e');
/*!40000 ALTER TABLE `question_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_category_list`
--

DROP TABLE IF EXISTS `question_category_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_category_list` (
  `id` char(36) NOT NULL,
  `category_key` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_category_list`
--

LOCK TABLES `question_category_list` WRITE;
/*!40000 ALTER TABLE `question_category_list` DISABLE KEYS */;
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('0b47f8f2-143c-4765-b904-2c4b8675c899','OLYMPICS');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('1a144f5f-e7f8-4086-ae01-fc0d45551c97','ASTRONOMY');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('21850e7d-158e-11e7-a82c-0242ac110004','COMICS');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('2e9f4804-560f-45de-ad39-a20c69920232','GEOGRAPHY');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('36087979-7ad4-4b7d-875e-86cc173dcf65','SCIENCE');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('6046e61a-6733-11e7-970d-0242ac110004','MUSIC');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('a6c007e7-444f-435c-8865-538b2adc8a9d','US_HISTORY');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('ade9c90b-6733-11e7-970d-0242ac110004','TV_SHOWS');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('b504d206-7ebe-42e1-aa31-2925b4fe8e25','MOVIES');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('f5b450f1-997c-42fd-8022-4a211c8e3f5e','ENTERTAINMENT');
INSERT INTO `snowyowl`.`question_category_list` (`id`,`category_key`) VALUES ('f8ac519f-c590-478e-bed1-19a953486d1e','GENERAL_KNOWLEDGE');
/*!40000 ALTER TABLE `question_category_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_forbidden_country_codes`
--

DROP TABLE IF EXISTS `question_forbidden_country_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_forbidden_country_codes` (
  `question_id` char(36) NOT NULL,
  `country_code` varchar(5) NOT NULL,
  PRIMARY KEY (`question_id`,`country_code`),
  CONSTRAINT `question_country_fk1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_forbidden_country_codes`
--

LOCK TABLES `question_forbidden_country_codes` WRITE;
/*!40000 ALTER TABLE `question_forbidden_country_codes` DISABLE KEYS */;
/*!40000 ALTER TABLE `question_forbidden_country_codes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_language_codes`
--

DROP TABLE IF EXISTS `question_language_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_language_codes` (
  `question_id` char(36) NOT NULL,
  `language_code` varchar(5) NOT NULL,
  PRIMARY KEY (`question_id`,`language_code`),
  CONSTRAINT `question_language_fk1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_language_codes`
--

LOCK TABLES `question_language_codes` WRITE;
/*!40000 ALTER TABLE `question_language_codes` DISABLE KEYS */;
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('0ed9fa29-2b15-481b-8dc1-4456e28dac55','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('131581f6-5577-45b7-b18b-36f877788b2f','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('1bd87bb5-bb84-4f8b-9ba2-8081c6b8cfe4','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('1c4f5123-4529-487d-a599-f91e6010d25d','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('257c16b3-f76c-4fdb-80ec-8c9dbfd0b68a','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('286209d2-b10f-49f7-a249-0e54ab164b1f','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('31e7249f-a36d-4c86-a388-ab96eab45eda','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('49101a25-dad7-48b8-9875-55b15080ab6c','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('4e363b9f-a037-44fb-9d9d-d0339bbc7790','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('53532282-0ba6-47e8-bf4a-826ab3ba8f70','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('56baa5a2-1f7b-43e2-82e2-0527582bdb25','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('5b0330a9-76e3-4ac5-b241-e2f7f33e5c57','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('6134db67-8492-439c-bbbb-f5f88bfa7864','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('65e3cd93-592c-4cd2-9318-1e27e9f1ca95','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('70913388-8772-4ebc-bef7-da36a04cd0e4','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('797c8379-c00b-4626-8154-e871241648d9','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('7c0968e8-4701-47bc-8f24-908ae5344483','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('816e1014-2011-4bde-9b70-770f52552ed6','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('81de52ec-6f4d-4497-aa9a-567a76486002','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('866c9949-a42f-498f-9974-9a046e5af4ba','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('9bd8277a-a2bf-4471-a349-76816fb5e081','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('ba9b1a82-7dba-4aab-9295-85e9ba7fec5c','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('bc73a50c-0c7a-44d5-a3da-2c7aa6ed49a8','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('bf05f0ef-aa9a-432e-b232-bda90e13970f','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('dd402319-975e-4807-acb7-209ab76f9d09','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('f08c7e48-d32c-47ae-9274-e71a4b3fc8a3','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('fc5c29cf-4300-4f28-9a32-8bfc938624ee','en');
INSERT INTO `snowyowl`.`question_language_codes` (`question_id`,`language_code`) VALUES ('fdfcd6d0-2623-4c84-b770-6b6e2c27ee38','en');
/*!40000 ALTER TABLE `question_language_codes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriber_question_answer`
--

DROP TABLE IF EXISTS `subscriber_question_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscriber_question_answer` (
  `id` char(36) NOT NULL,
  `game_id` char(36) NOT NULL,
  `round_id` char(36) NOT NULL,
  `match_id` char(36) NOT NULL,
  `question_id` char(36) NOT NULL,
  `match_question_id` char(36) NOT NULL,
  `subscriber_id` int(10) unsigned NOT NULL,
  `selected_answer_id` char(36) DEFAULT NULL,
  `question_decrypt_key` char(255) DEFAULT NULL,
  `question_presented_timestamp` timestamp(6) NULL DEFAULT NULL,
  `duration_milliseconds` int(11) DEFAULT NULL,
  `determination` varchar(45) DEFAULT NULL,
  `won` int(1) NOT NULL DEFAULT '0',
  `create_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`),
  KEY `sqa_fk1_idx` (`question_id`),
  KEY `sqa_fk2_idx` (`match_question_id`),
  KEY `sqa_fk3_idx` (`selected_answer_id`),
  CONSTRAINT `sqa_fk1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sqa_fk2` FOREIGN KEY (`match_question_id`) REFERENCES `match_question` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sqa_fk3` FOREIGN KEY (`selected_answer_id`) REFERENCES `question_answer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber_question_answer`
--

LOCK TABLES `subscriber_question_answer` WRITE;
/*!40000 ALTER TABLE `subscriber_question_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscriber_question_answer` ENABLE KEYS */;
UNLOCK TABLES;

CREATE TABLE `snowyowl`.`game_stats` (
  `game_id` CHAR(36) NOT NULL,
  `remaining_players` INT UNSIGNED NULL DEFAULT 0,
  `freeplay_notification_sent` INT(1) NULL DEFAULT 0,
  `remaining_save_player_count` INT NULL,
  `twitch_console_followed_subscriber_id` INT(11) NULL,
  PRIMARY KEY (`game_id`));

--
-- Dumping events for database 'snowyowl'
--

--
-- Dumping routines for database 'snowyowl'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-08 15:21:17
