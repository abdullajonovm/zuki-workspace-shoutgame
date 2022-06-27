DROP database IF EXISTS `trigger`;
CREATE database `trigger`;
USE `trigger`;

create table `trigger`.`analytics` (
    `analytics_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    analytics_type ENUM(
    	'EVENT_STARTED', 'EVENT_ENDED', 'EVENT_JOINED', 'QUESTION_STARTED', 'QUESTION_ENDED', 'QUESTION_SCORED', 'QUESTION_ANSWERED', 'VIPBOX_JOINED', 'VIPBOX_CREATED', 
    	'SHOUTOUT_SENT', 'CONTEST_WON', 'SUBSCRIBER_SIGNED_UP', 'POWERUP_USED', 'CREDS_PURCHASED', 'CREDS_EXCHANGED', 'PASS_PURCHASED', 'PASS_TOPPED_OFF', 'CREDS_AWARDED', 
    	'PUSH_SENDING','PUSH_SENT','PUSH_ERROR','POWERUP_AMOUNTS_POST_TOPOFF','POWERUP_AMOUNTS_PRE_TOPOFF') NOT NULL,
    `subscriber_id` INTEGER UNSIGNED NULL,
    `cc_id` INTEGER UNSIGNED NULL,
    `event_id` INTEGER UNSIGNED NULL,
    `vipbox_id` INTEGER UNSIGNED NULL,
    `question_id` INTEGER UNSIGNED NULL,
    `answer_id` INT(10) UNSIGNED NULL,
    `contest_id` INTEGER UNSIGNED NULL,
    `powerup_type` VARCHAR(255) NULL,
    `powerup_pass_type` VARCHAR(255) NULL,
    `powerup_creds_amount` INTEGER NULL,
    `num_clock_freeze` INT(10) UNSIGNED NULL,
    `num_fan_check` INT(10) UNSIGNED NULL,
    `num_vote2` INT(10) UNSIGNED NULL,
    `num_mulligan` INT(10) UNSIGNED NULL,
    `num_throwdown` INT(10) UNSIGNED NULL,
    `num_safety_net` INT(10) UNSIGNED NULL,
    `device_os_name` VARCHAR(255) NULL,
    `subscriber_email` VARCHAR(255) NULL,
    `num_creds` INT(10) UNSIGNED NULL,
    `vtt_id` INT(10) UNSIGNED NULL,
    `push_provider` VARCHAR(255) NULL,
    `device_id` VARCHAR(255) NULL,
    `message_id` VARCHAR(255) NULL,
    `timestamp` DATETIME NOT NULL,
    PRIMARY KEY(`analytics_id`)
) ENGINE = InnoDB;
