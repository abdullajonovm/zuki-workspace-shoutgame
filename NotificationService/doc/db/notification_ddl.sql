DROP database IF EXISTS `notification`;

CREATE database `notification`;

USE `notification`;

create table `notification`.`callback` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `namespace` varchar(255),
    `service_name` varchar(255) NOT NULL,
    `version` varchar(255) NOT NULL,
    `method_name` varchar(767) NOT NULL,
    `notification_type` varchar(255) NOT NULL,
    PRIMARY KEY(`id`),
    INDEX(`notification_type`),
    INDEX(`namespace`),
    INDEX(`service_name`),
    INDEX(`version`),
    INDEX(`method_name`)
) ENGINE = InnoDB;

create table `notification`.`notification` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `type` varchar(50) NOT NULL,
    `context_id` INTEGER UNSIGNED NOT NULL DEFAULT 0,
    `sender` INTEGER UNSIGNED NOT NULL,
    `recipient` INTEGER UNSIGNED NOT NULL,
    `status` varchar(50) NOT NULL,
    `message` varchar(255) NOT NULL,
    `description` TEXT NOT NULL,
    `action_type` varchar(50) NOT NULL,
    `action_taken` varchar(50),
    `payload` TEXT,
    `created` DATETIME NOT NULL,
    `last_updated_by` INTEGER UNSIGNED NOT NULL,
    `last_updated` DATETIME NOT NULL,
    PRIMARY KEY(`id`),
    INDEX(`type`),
    INDEX(`context_id`),
    INDEX(`sender`),
    INDEX(`recipient`),
    INDEX(`status`),
    INDEX(`action_type`),
    INDEX(`action_taken`),
    INDEX(`created`),
    INDEX(`last_updated_by`),
    INDEX(`last_updated`)
) ENGINE = InnoDB;

create table `notification`.`pref_types` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` varchar(60) NOT NULL,
    `description` varchar(255) NOT NULL,
    `possible_values` TEXT NOT NULL,
    PRIMARY KEY(`id`)
) ENGINE = InnoDB;

create table `notification`.`pref` (
    `pref_type` INTEGER UNSIGNED NOT NULL,
    `subscriber_id` INTEGER UNSIGNED NOT NULL,
    `value` varchar(25) NOT NULL,
    `created` DATETIME NOT NULL,
    `last_updated` DATETIME NOT NULL,
    PRIMARY KEY (`pref_type`, `subscriber_id`),
    INDEX(`subscriber_id`),
    INDEX(`value`),
    INDEX(`created`),
    INDEX(`last_updated`)
) ENGINE = InnoDB;

CREATE  TABLE `notification`.`on_leaderboard_notified` (
  `subscriber_id` INT(11) NOT NULL ,
  `event_id` INT(11) NOT NULL ,
  PRIMARY KEY (`subscriber_id`, `event_id`) 
) ENGINE = InnoDB;