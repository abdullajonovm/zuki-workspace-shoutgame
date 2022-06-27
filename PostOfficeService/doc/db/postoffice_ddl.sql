DROP database IF EXISTS `postoffice`;

CREATE database `postoffice`;

USE `postoffice`;

CREATE TABLE `email_optout` (
  `from_address` varchar(255) NOT NULL,
  `to_address` varchar(255) NOT NULL,
  `opted_out_date` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`from_address`,`to_address`)
) ENGINE=InnoDB;

