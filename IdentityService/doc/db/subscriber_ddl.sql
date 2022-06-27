CREATE DATABASE if not exists `gameplay`;
USE `gameplay`;

DROP TABLE if exists `gameplay`.`s_identity_realmadrid`;
DROP TABLE if exists `gameplay`.`s_identity_fb`;
DROP TABLE if exists `gameplay`.`s_subscriber_session`;
DROP TABLE if exists `gameplay`.`s_subscriber_address`;
DROP TABLE if exists `gameplay`.`s_subscriber_nickname_history`;
DROP TABLE if exists `gameplay`.`s_subscriber`;

CREATE TABLE `gameplay`.`s_subscriber` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `context_id` INT(10) UNSIGNED NOT NULL ,
  `firstname` VARCHAR(45) NOT NULL DEFAULT 'NYI',
  `lastname` VARCHAR(45) NOT NULL DEFAULT 'NYI',
  `nickname` varchar(50) NOT NULL,
  `nickname_set` INTEGER(1) NOT NULL DEFAULT 0,
  `facebook_user_flag` INTEGER(1) NOT NULL DEFAULT 0,
  `photo_url` VARCHAR(1024),
  `role` ENUM('USER','ADMIN','SHOUTCASTER','CELEBRITY') NOT NULL DEFAULT 'USER',
  `admin_role` ENUM('NONE','NORMAL','SUPER') NOT NULL DEFAULT 'NONE',
  `email` VARCHAR(128) NOT NULL,
  `passwd` VARCHAR(128) NOT NULL,
  `change_password` INTEGER(1) UNSIGNED NOT NULL default 0,
  `phone` VARCHAR(45) NULL,
  `phone_verified` INTEGER(1) UNSIGNED NOT NULL default 0,
  `active_flag` INTEGER(1) UNSIGNED NOT NULL default 1,
  `eula_flag` INTEGER(1) UNSIGNED NOT NULL default 0,
  `is_adult_flag` INTEGER(1) UNSIGNED NOT NULL default 0,
  `language_code` CHAR(2) NOT NULL default 'en',
  `mint_parent_subscriber_id` INTEGER UNSIGNED NULL,
  `ring1_subscriber_id` INTEGER UNSIGNED NULL,
  `ring2_subscriber_id` INTEGER UNSIGNED NULL,
  `ring3_subscriber_id` INTEGER UNSIGNED NULL,
  `ring4_subscriber_id` INTEGER UNSIGNED NULL,
  `create_date` TIMESTAMP NOT NULL,
  `update_date` TIMESTAMP NOT NULL,
  PRIMARY KEY(`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`s_subscriber_nickname_history` (
  `snh_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `old_nickname` varchar(50) NULL,
  `new_nickname` varchar(50) NOT NULL,
  `change_date` TIMESTAMP NOT NULL,
  PRIMARY KEY(`snh_id`),
  CONSTRAINT `FK_s_subscriber_nickname_history_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;
  
CREATE TABLE `gameplay`.`s_subscriber_address` (
  `address_id` INT NOT NULL AUTO_INCREMENT, 
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `addr1` VARCHAR(256) NOT NULL,
  `addr2` VARCHAR(256) NULL,
  `city` VARCHAR(64) NOT NULL,
  `state` VARCHAR(64) NULL,
  `zip` VARCHAR(64) NULL,
  `country_code` VARCHAR(2) NOT NULL DEFAULT 'US',
  `current_flag` INT(1) NOT NULL DEFAULT 0,
  `create_date` TIMESTAMP NOT NULL,
  `update_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`address_id`),
  CONSTRAINT `FK_s_subscriber_address_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`s_subscriber_session` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `device_id` VARCHAR(255) NOT NULL DEFAULT 'WEB',
  `session_key` CHAR(36) NULL,
  `device_model` VARCHAR(255) NULL,
  `device_name` VARCHAR(255) NULL,
  `device_version` VARCHAR(255) NULL,
  `os_name` VARCHAR(255) NULL,
  `os_type` VARCHAR(255) NULL,
  `app_id` VARCHAR(255) NULL,
  `app_version` VARCHAR(255) NULL,
  `added_date` TIMESTAMP NOT NULL,
  `last_authenticated_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`subscriber_id`, `device_id`),
  UNIQUE INDEX `session_key_UNIQUE` (`session_key` ASC),
  CONSTRAINT `FK_s_subscriber_session_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`s_identity_fb` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `facebook_id` varchar(255) NOT NULL,
  PRIMARY KEY(`subscriber_id`),
  UNIQUE INDEX `fb_facebook_id_UNIQUE` (`facebook_id` ASC),
  CONSTRAINT `FK_s_identity_fb_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`s_identity_realmadrid` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `realmadrid_id` varchar(255) NOT NULL,
  PRIMARY KEY(`subscriber_id`),
  UNIQUE INDEX `realmadrid_id_UNIQUE` (`realmadrid_id` ASC),
  CONSTRAINT `FK_s_identity_realmadrid_1` FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`s_invalid_nicknames` (
  `nickname` varchar(50) NOT NULL,
  PRIMARY KEY(`nickname`)
) ENGINE = InnoDB;

INSERT INTO `gameplay`.`s_invalid_nicknames` (`nickname`) VALUES ('shout');
INSERT INTO `gameplay`.`s_invalid_nicknames` (`nickname`) VALUES ('$hout');
INSERT INTO `gameplay`.`s_invalid_nicknames` (`nickname`) VALUES ('$h0ut');
INSERT INTO `gameplay`.`s_invalid_nicknames` (`nickname`) VALUES ('sh0ut');
  
  
### TODO: Run On Production ###

# 6/23/2016: To support Daily Millionaire

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
