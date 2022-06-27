DROP database IF EXISTS `facebook`;

CREATE DATABASE `facebook`;

USE `facebook`;

create table `facebook`.`auth_subscriber` (
    `facebook_id` varchar(255) NOT NULL,
    `access_token` varchar(255) NOT NULL,
    PRIMARY KEY (`facebook_id`),
    INDEX (`access_token`)
) ENGINE = InnoDB;

create table `facebook`.`callback` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `namespace` varchar(255),
    `service_name` varchar(255) NOT NULL,
    `version` varchar(255) NOT NULL,
    `method_name` varchar(767) NOT NULL,
    PRIMARY KEY(`id`),
    INDEX(`namespace`),
    INDEX(`service_name`),
    INDEX(`version`),
    INDEX(`method_name`)
) ENGINE = InnoDB;
