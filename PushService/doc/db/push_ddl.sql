DROP DATABASE IF EXISTS `push`;

CREATE DATABASE `push`;

USE `push`;

create table `push`.`token_subscriber` (
    `subscriber_id` INTEGER UNSIGNED NOT NULL,
    `app_bundle_id` varchar(255),
    `device_uuid` varchar(255) NOT NULL,
    `device_token` varchar(255) NOT NULL,
    `device_type` varchar(50) NOT NULL,
    `last_registration` TIMESTAMP NOT NULL DEFAULT NOW(),
    `device_active` INTEGER(1) UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY (`subscriber_id`, `device_uuid`, `device_type`),
    INDEX (`subscriber_id`),
    INDEX (`app_bundle_id`),
    INDEX (`device_uuid`),
    UNIQUE INDEX (`device_token`),
    INDEX(`device_type`),
    INDEX (`last_registration`),
    INDEX (`device_active`)
) ENGINE = InnoDB;

create table `push`.`c2dm_config` (
    `os_type` VARCHAR(255) NOT NULL,
    `auth_token` VARCHAR(1024) NOT NULL,
    `last_update_date` TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`os_type`)
) ENGINE = InnoDB;

create table `push`.`mock_message` (
    `mock_message_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `device_token` varchar(255) NOT NULL,
    `payload` varchar(1024) NOT NULL,
    `push_date` TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`mock_message_id`),
    INDEX (`device_token`)
) ENGINE = InnoDB;

