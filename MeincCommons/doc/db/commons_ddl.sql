DROP DATABASE IF EXISTS `team`;
CREATE DATABASE `team`;
USE `team`;

CREATE TABLE  `account_services` (
  `account_id` int(10) unsigned NOT NULL,
  `team_namespace` varchar(256) NOT NULL,
  `team_service` varchar(256) NOT NULL,
  `team_endpoint` varchar(256) NOT NULL,
  PRIMARY KEY  (`account_id`)
) ENGINE=InnoDB;

CREATE TABLE `master_subscriber_id` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL DEFAULT NULL AUTO_INCREMENT,
  PRIMARY KEY(`subscriber_id`)
) ENGINE = InnoDB;

# This may go to another database/service in the future
CREATE TABLE  `subscriber_prefs` (
  `subscriber_id` int(10) unsigned NOT NULL,
  `cell_number` varchar(45) default NULL,
  `wireless_carrier` int(10) unsigned default NULL,
  PRIMARY KEY  (`subscriber_id`)
) ENGINE=InnoDB;

# This may go to another database/service in the future
CREATE TABLE `munge`
(
  `munge_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `namespace` varchar(20) NOT NULL,
  `original_value` varchar(1024) NOT NULL,
  `munged_value` varchar(128) NOT NULL,
  `created_date` datetime NOT NULL,
  `expires_date` datetime NOT NULL,
  PRIMARY KEY (`munge_id`),
  KEY `ns_orig_idx` (`namespace`,`original_value`(767),`expires_date`),
  KEY `ns_mung_idx` (`namespace`,`munged_value`,`expires_date`),
  KEY `munge_created_idx` (`expires_date`)
) ENGINE=InnoDB;

INSERT INTO `account_services` (account_id, team_namespace, team_service, team_endpoint)
VALUES (2, 'phoenix-service', 'HostedTeamService', 'jms://localhost');

INSERT INTO `master_subscriber_id` (subscriber_id)
VALUES (3);
