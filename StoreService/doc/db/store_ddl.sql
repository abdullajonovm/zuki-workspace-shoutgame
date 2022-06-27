DROP database IF EXISTS `store`;

CREATE database `store`;

USE `store`;

create table `store`.`item` (
    `item_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `uuid` varchar(100) NOT NULL,
    `title` varchar(100) NOT NULL,
    `description` varchar(255) NOT NULL,
    `price` DECIMAL(7,2),
    `active` INTEGER(1) UNSIGNED NOT NULL DEFAULT 1,
    `duration_quantity` int(10) DEFAULT NULL,
    `duration_unit` enum('HOURS','DAYS','MONTHS','YEARS') DEFAULT NULL,
    PRIMARY KEY(`item_id`),
    KEY `active` (`active`),
    KEY `uuid_idx` (`uuid`)
) ENGINE = InnoDB;

create table `store`.`entitlement` (
    `entitlement_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `uuid` char(36) NOT NULL,
    `name` varchar(100),
    PRIMARY KEY(`entitlement_id`),
    INDEX(`uuid`)
) ENGINE = InnoDB;

create table `store`.`item_entitlement` (
    `item_id` INTEGER UNSIGNED NOT NULL ,
    `entitlement_id` INTEGER UNSIGNED NOT NULL ,
    `quantity` INTEGER UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY(`item_id`, `entitlement_id`),
    CONSTRAINT FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`),
    CONSTRAINT FOREIGN KEY (`entitlement_id`) REFERENCES `entitlement` (`entitlement_id`),
    INDEX(`item_id`),
    INDEX(`entitlement_id`)
) ENGINE = InnoDB;

create table `store`.`subscriber_entitlement` (
    `subscriber_entitlement_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `uuid` char(36) NOT NULL,
    `subscriber_id` INTEGER UNSIGNED NOT NULL,
    `entitlement_id` INTEGER UNSIGNED NOT NULL,
    `receipt_id` int(10) DEFAULT NULL,
    `delete_date` DATETIME NULL,
    `reserved_date` DATETIME NULL,
    `consumed_date` DATETIME NULL,
    `context_id` int(10) NULL,
    PRIMARY KEY (`subscriber_entitlement_id`),
    CONSTRAINT FOREIGN KEY (`entitlement_id`) REFERENCES `entitlement` (`entitlement_id`),
    CONSTRAINT `entitle_receipt_fk_idx` FOREIGN KEY (`receipt_id`) REFERENCES `receipt` (`receipt_id`),
    INDEX(`uuid`),
    INDEX(`entitlement_id`),
    INDEX(`receipt_id`)
) ENGINE = InnoDB;

create table `store`.`receipt` (
    `receipt_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `uuid` char(36) NOT NULL,
    `type` ENUM('ITUNES', 'GPLAY_ONETIME', 'GPLAY_RECURRING', 'CREDIT_CARD', 'INTERNAL') NOT NULL,
    `item_uuid` varchar(100) NOT NULL,
    `subscriber_id` INTEGER UNSIGNED NOT NULL,
    `payload` TEXT,
    `expiration_date` datetime DEFAULT NULL,
    `skip_verify` TINYINT(1) DEFAULT 0,
    `created_date` datetime NOT NULL,
    `updated_date` datetime NOT NULL,
    `subscripton_state` ENUM('ACTIVE','CANCELED') DEFAULT NULL,
    PRIMARY KEY (`receipt_id`),
    INDEX(`uuid`),
    INDEX(`subscriber_id`),
    KEY `item_uuid_fk_idx_idx` (`item_uuid`),
    CONSTRAINT `item_uuid_fk_idx` FOREIGN KEY (`item_uuid`) REFERENCES `item` (`uuid`)
) ENGINE = InnoDB;

#create table `store`.`coupon_code` (
#	`coupon_code_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
#	`coupon_code` char(8) NOT NULL,
#	`item_uuid` varchar(100) NOT NULL,
#	`create_date` DATETIME NOT NULL,
#	`redeem_date` DATETIME NULL,
#	`cancel_date` DATETIME NULL,
#	PRIMARY KEY(`coupon_code_id`),
#	UNIQUE KEY `coupon_code_UNIQUE` (`coupon_code`),
#	KEY `cc_item_uuid_fk_idx_idx` (`item_uuid`),
#	CONSTRAINT `cc_item_uuid_fk_idx` FOREIGN KEY (`item_uuid`) REFERENCES `item` (`uuid`)
#) ENGINE = InnoDB;

create table `store`.`item_price` (
    `item_id` INTEGER UNSIGNED NOT NULL ,
    `currency_code` CHAR(3) NOT NULL ,
    `price` DECIMAL(7,2) NOT NULL,
    `formatted_price` VARCHAR(12) NOT NULL,
    PRIMARY KEY(`item_id`, `currency_code`),
    CONSTRAINT FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`),
    INDEX(`item_id`)
) ENGINE = InnoDB;

################## NOT APPLIED TO PRODUCTION ##################

INSERT INTO store.item (store_bundle_id, uuid, title, description, price, active, duration_quantity, duration_unit)  
  VALUES ('com.shout.dailymillionaire','fbffed66-34b2-11e6-bb40-3a9288e13c40','Small Package','1 credit',1.99,1,0,'YEARS'); 
INSERT INTO store.item (store_bundle_id, uuid, title, description, price, active, duration_quantity, duration_unit)  
  VALUES ('com.shout.dailymillionaire','0cf94cdc-34b3-11e6-bb40-3a9288e13c40','Medium Package','4 credits',4.99,1,0,'YEARS'); 
INSERT INTO store.item (store_bundle_id, uuid, title, description, price, active, duration_quantity, duration_unit)  
  VALUES ('com.shout.dailymillionaire','1961ccf9-34b3-11e6-bb40-3a9288e13c40','Large Package','10 credits',9.99,1,0,'YEARS');
INSERT INTO store.item_price (item_id, currency_code, price, formatted_price)  
  SELECT item_id, 'USD', price, CONCAT('$', CAST(price AS char(12))) 
  FROM store.item WHERE store_bundle_id = 'com.shout.dailymillionaire';



