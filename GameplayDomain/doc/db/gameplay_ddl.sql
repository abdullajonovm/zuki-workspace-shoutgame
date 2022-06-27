DROP DATABASE if exists `gameplay`;
CREATE DATABASE `gameplay` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `gameplay`;

CREATE  TABLE `gameplay`.`metadata_type` (
  `metadata_type_id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`metadata_type_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`metadata` (
  `metadata_id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NOT NULL ,
  `metadata_type_id` INT NOT NULL ,
  `order` INT NOT NULL DEFAULT 1 ,
  `active` INT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`metadata_id`) ,
  CONSTRAINT `fk_md_1` FOREIGN KEY `fk_md_1` (`metadata_type_id`) REFERENCES `metadata_type` (`metadata_type_id`),
  INDEX `IDX_md_order` (`order` ASC),
  INDEX `IDX_md_order_type` (`metadata_type_id` ASC, `order` ASC)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`event` (
  `event_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_date` DATETIME NOT NULL,
  `name` VARCHAR(254) NOT NULL,
  `status` ENUM('NEW','INPROGRESS','COMPLETE') NOT NULL,
  `textwire_keyword` VARCHAR(25),
  `expected_start_date` DATETIME NOT NULL,
  `expected_stop_date` DATETIME NOT NULL,
  `actual_start_date` DATETIME,
  `actual_stop_date` DATETIME,
  `private_evt` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `sponsor` VARCHAR(45) NULL,
  `coupon` VARCHAR(45) NULL,
  `league_id` INT NULL DEFAULT 301,
  `highlighted` INT(1) NOT NULL DEFAULT 0,
  `min_game_purse` DECIMAL(9,2) NOT NULL DEFAULT 0.0,
  `scoring_data` TEXT NOT NULL DEFAULT '',
  `marketing_html` TEXT NULL,
  `last_updated` DATETIME,
  PRIMARY KEY(`event_id`),
  UNIQUE INDEX `textwire_keyword_UNIQUE` (`textwire_keyword` ASC),
  CONSTRAINT `FK_event_league_id` FOREIGN KEY `FK_event_league_id` (`league_id`) REFERENCES `metadata` (`metadata_id`),
  INDEX `IDX_event_status` (`status` ASC),
  INDEX `IDX_event_expected_start_date` (`expected_start_date` ASC),
  INDEX `IDX_event_hilighted` (`highlighted` ASC),
  INDEX `IDX_event_private_evt` (`private_evt` ASC),
  INDEX `IDX_event_createdate` (`created_date` DESC)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`event_onInsert`
BEFORE INSERT ON `gameplay`.`event`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`event_OnUpdate`
BEFORE UPDATE ON `gameplay`.`event`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `vip_box_aggregate_score_values`
(
    `event_id` INTEGER UNSIGNED NOT NULL,
    `really_slow_answer_penalty_percent` INTEGER UNSIGNED NOT NULL,
    `slow_answer_penalty_percent` INTEGER UNSIGNED NOT NULL,
    `fast_answer_bonus_percent` INTEGER UNSIGNED NOT NULL,
    `really_fast_answer_bonus_percent` INTEGER UNSIGNED NOT NULL,
    `no_answer_adjustment` INTEGER NOT NULL,
    `wrong_answer_adjustment` INTEGER NOT NULL,
    `num_winning_vip_boxes` INTEGER UNSIGNED NOT NULL,
    `num_winners_per_vip_box` INTEGER UNSIGNED NOT NULL,
    `invalid_vip_box_ids_str` TEXT NOT NULL,
    `payout_amounts_json` TEXT NOT NULL,
    PRIMARY KEY (`event_id`),
    CONSTRAINT `fk_vipboxaggregate_1` FOREIGN KEY `fk_vipboxaggregate_1` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`event_results`
(
  `id` INT NOT NULL AUTO_INCREMENT ,
  `event_id` INT UNSIGNED NOT NULL ,
  `results` TEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_eventresults_eventid` FOREIGN KEY `fk_eventresults_eventid` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`event_subscriber` (
  `event_id` INTEGER UNSIGNED NOT NULL ,
  `subscriber_id` INTEGER UNSIGNED NOT NULL ,
  PRIMARY KEY (`event_id`, `subscriber_id`) ,
  CONSTRAINT `fk_event_id` FOREIGN KEY `fk_event_id` (`event_id`) REFERENCES `event` (`event_id`),
  CONSTRAINT `fk_subscriber_id` FOREIGN KEY `fk_subscriber_id` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  INDEX `IDX_event_subscriber_eventid` (`event_id` ASC)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`event_resource` (
  `resource_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `event_id` INTEGER UNSIGNED NOT NULL,
  `resource_url` VARCHAR(255) NOT NULL,
  `sort_order` VARCHAR(45) NOT NULL,
  PRIMARY KEY(`resource_id`, `event_id`),
  CONSTRAINT `FK_event_resource_1` FOREIGN KEY `FK_event_resource_1` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`blog_entry` (
  `blog_entry_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `event_id` INTEGER UNSIGNED NOT NULL,
  `vipbox_id` INTEGER UNSIGNED NOT NULL DEFAULT 0,
  `message` TEXT NOT NULL,
  `post_date` DATETIME NOT NULL,
  `approved` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `attached_image_url` VARCHAR(255),
  `attached_image_source` ENUM('FACEBOOK','TWITTER'),
  PRIMARY KEY(`blog_entry_id`),
  CONSTRAINT `FK_blog_entry_1` FOREIGN KEY `FK_blog_entry_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `FK_blog_entry_2` FOREIGN KEY `FK_blog_entry_2` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`poll` (
  `poll_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_date` TIMESTAMP NOT NULL,
  `type` INTEGER UNSIGNED NOT NULL,
  `status` ENUM('NEW','INPROGRESS','COMPLETE') NOT NULL,
  `question_text` VARCHAR(254) NOT NULL,
  `grace_period_s` INT NOT NULL DEFAULT 60,
  `started_date` DATETIME,
  `stopped_date` DATETIME,
  `point_value` INTEGER,
  `number_of_winners` INTEGER,
  `minutes_to_autoclose` INTEGER UNSIGNED,
  `sponsor_message` VARCHAR(254),
  `last_updated` DATETIME,
  PRIMARY KEY(`poll_id`),
  INDEX `IDX_poll_status` (`poll_id` ASC, `status` ASC),
  INDEX `IDX_poll_status_only` (`status` ASC),
  INDEX `IDX_poll_type_only` (`type` ASC),
  INDEX `IDX_poll_type` (`poll_id` ASC, `type` ASC),
  INDEX `IDX_poll_poll_status_type` (`poll_id` ASC, `type` ASC, `status` ASC)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`poll_onInsert`
BEFORE INSERT ON `gameplay`.`poll`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`poll_OnUpdate`
BEFORE UPDATE ON `gameplay`.`poll`
FOR EACH ROW
BEGIN
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW());
update `gameplay`.`event` e, `gameplay`.`event_poll` ep set e.last_updated = NOW()
WHERE e.event_id = ep.event_id
AND ep.poll_id = NEW.poll_id;
END$$

DELIMITER ;

CREATE TABLE `gameplay`.`poll_answers` (
  `answer_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `poll_id` INTEGER UNSIGNED NOT NULL,
  `display_order` INTEGER(2) NOT NULL,
  `answer_code` VARCHAR(10) NOT NULL,
  `answer_text` VARCHAR(254) NOT NULL,
  `answer_is_correct` INTEGER(1) DEFAULT 0,
  PRIMARY KEY(`answer_id`),
  CONSTRAINT `FK_poll_answers` FOREIGN KEY `FK_poll_answers` (`poll_id`) REFERENCES `poll` (`poll_id`),
  INDEX `IDX_poll_answers_poll_answer_code` (`poll_id` ASC, `answer_code` ASC)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`poll_response` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `poll_id` INTEGER UNSIGNED NOT NULL,
  `answer_id` INTEGER UNSIGNED NOT NULL,
  `response_text` VARCHAR(254) NOT NULL,
  `answer_date` DATETIME NOT NULL,
  PRIMARY KEY(`subscriber_id`,`poll_id`),
  CONSTRAINT `FK_poll_response_subscriber` FOREIGN KEY `FK_poll_response_subscriber` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `FK_poll_response_poll_id` FOREIGN KEY `FK_poll_response_poll_id` (`poll_id`) REFERENCES `poll` (`poll_id`),
  CONSTRAINT `FK_poll_response_answer` FOREIGN KEY `FK_poll_response_answer` (`answer_id`) REFERENCES `poll_answers` (`answer_id`),
  UNIQUE INDEX `UIDX_poll_response_sub_answer` (`subscriber_id` ASC, `answer_id` ASC),
  INDEX `IDX_poll_response_answer_date` (`answer_date` ASC),
  INDEX `IDX_poll_response_poll_answer` (`poll_id` ASC, `answer_id` ASC)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`subscriber_child_counted` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `child_id` INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(`subscriber_id`, `child_id`),
  CONSTRAINT `FK_subscriber_child_counted_1` FOREIGN KEY `FK_subscriber_child_counted_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `FK_subscriber_child_counted_2` FOREIGN KEY `FK_subscriber_child_counted_2` (`child_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`event_poll` (
  `event_id` INTEGER UNSIGNED NOT NULL,
  `poll_id` INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(`event_id`, `poll_id`),
  CONSTRAINT `FK_event_poll_event_id` FOREIGN KEY `FK_event_poll_event_id` (`event_id`) REFERENCES `event` (`event_id`),
  CONSTRAINT `FK_event_poll_poll_id` FOREIGN KEY `FK_event_poll_poll_id` (`poll_id`) REFERENCES `poll` (`poll_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`event_poll_onInsert`
BEFORE INSERT ON `gameplay`.`event_poll`
FOR EACH ROW
update `gameplay`.`event` e set last_updated = NOW()
WHERE e.event_id = NEW.event_id$$

DELIMITER ;

CREATE  TABLE `gameplay`.`current_rankings` (
  `event_id` INT UNSIGNED NOT NULL ,
  `subscriber_id` INT UNSIGNED NOT NULL ,
  `rank` INT NOT NULL ,
  `calculation_date` DATETIME NULL ,
  PRIMARY KEY (`event_id`, `subscriber_id`) ,
  CONSTRAINT `fk_current_rankings_1` FOREIGN KEY `fk_current_rankings_1`  (`event_id`) REFERENCES `gameplay`.`event` (`event_id`),
  CONSTRAINT `fk_current_rankings_2` FOREIGN KEY `fk_current_rankings_2`  (`subscriber_id`) REFERENCES `gameplay`.`s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`default_scoring` (
  `event_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT ,
  `all_correct_bonus` INT NOT NULL ,
  `all_answered_bonus` INT NOT NULL ,
  `really_slow_penalty` INT NOT NULL ,
  `slow_penalty` INT NOT NULL ,
  `fast_bonus` INT NOT NULL ,
  `really_fast_bonus` INT NOT NULL ,
  `wrong_answer_penalty` INT NOT NULL ,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK_default_scoring_event_id` FOREIGN KEY `FK_default_scoring_event_id` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`log_skip` (
  `event_id` INT NOT NULL ,
  `subscriber_id` INT NOT NULL ,
  `skip_date` DATETIME NOT NULL ,
  PRIMARY KEY (`event_id`, `subscriber_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`metadata_relationship_type` (
  `metadata_relationship_type_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`metadata_relationship_type_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`metadata_relationship` (
  `metadata_relationship_id` INT NOT NULL AUTO_INCREMENT ,
  `metadata_relationship_type_id` INT NOT NULL,
  `metadata_id_1` INT NOT NULL,
  `metadata_id_2` INT NOT NULL,
  PRIMARY KEY (`metadata_relationship_id`),
  CONSTRAINT `fk_mdr_1` FOREIGN KEY `fk_mdr_1` (`metadata_relationship_type_id`) REFERENCES `metadata_relationship_type` (`metadata_relationship_type_id`),
  CONSTRAINT `fk_mdr_2` FOREIGN KEY `fk_mdr_2` (`metadata_id_1`) REFERENCES `metadata` (`metadata_id`),
  CONSTRAINT `fk_mdr_3` FOREIGN KEY `fk_mdr_3` (`metadata_id_2`) REFERENCES `metadata` (`metadata_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`event_team` (
  `event_id` INTEGER UNSIGNED NOT NULL,
  `team_id` INT NOT NULL ,
  `order` INT NOT NULL DEFAULT 1 ,
  PRIMARY KEY (`event_id`, `team_id`),
  CONSTRAINT `fk_event_team_1` FOREIGN KEY `fk_event_team_1` (`event_id`) REFERENCES `event` (`event_id`),
  CONSTRAINT `fk_event_team_2` FOREIGN KEY `fk_event_team_2` (`team_id`) REFERENCES `metadata` (`metadata_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`subscriber_team` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `team_id` INT NOT NULL ,
  PRIMARY KEY (`subscriber_id`, `team_id`),
  CONSTRAINT `fk_sub_team_1` FOREIGN KEY `fk_sub_team_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_sub_team_2` FOREIGN KEY `fk_sub_team_2` (`team_id`) REFERENCES `metadata` (`metadata_id`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`network_winners` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `last_win_date` DATETIME NOT NULL,
  PRIMARY KEY (`subscriber_id`),
  CONSTRAINT `fk_networkwinners_1` FOREIGN KEY `fk_networkwinners_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`event_analytics`
(
  `event_id` INT UNSIGNED NOT NULL,
  `results` TEXT NOT NULL,
  PRIMARY KEY (`event_id`) ,
  CONSTRAINT `fk_eventanalytics_1` FOREIGN KEY `fk_eventanalytics_1` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`question_coupon`
(
  `poll_id` INTEGER UNSIGNED NOT NULL,
  `checkit_state` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`poll_id`),
  CONSTRAINT `FK_qc_poll_id` FOREIGN KEY `FK_qc_poll_id` (`poll_id`) REFERENCES `poll` (`poll_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`event_media`
(
  `event_id` INTEGER UNSIGNED NOT NULL,
  `photo_url` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `fk_eventmedia_1` FOREIGN KEY `fk_eventmedia_1` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`question_media`
(
  `poll_id` INTEGER UNSIGNED NOT NULL,
  `type` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0, /* 0 means before as in with question, 1 means after (reveal) */
  `photo_url` VARCHAR(255) NULL,
  `video_url` VARCHAR(255) NULL,
  `audio_url` VARCHAR(255) NULL,
  `text` VARCHAR(255) NULL,
  `caption` VARCHAR(255) NULL,
  PRIMARY KEY (`poll_id`, `type`),
  CONSTRAINT `FK_quesmed_poll_id` FOREIGN KEY `FK_quesmed_poll_id` (`poll_id`) REFERENCES `poll` (`poll_id`)
) ENGINE = InnoDB;


CREATE TABLE `gameplay`.`subscriber_coupon_winner`
(
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `event_id` INTEGER UNSIGNED NOT NULL,
  `win_date` DATETIME NOT NULL,
  PRIMARY KEY (`subscriber_id`, `event_id`),
  CONSTRAINT `fk_subcoupon_1` FOREIGN KEY `fk_subcoupon_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_subcoupon_2` FOREIGN KEY `fk_subcoupon_2` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

create table `gameplay`.`landing_page` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `content` TEXT NOT NULL,
    PRIMARY KEY(`id`)
) ENGINE = InnoDB;

create table `gameplay`.`event_landing_page` (
    `event_id` INTEGER UNSIGNED NOT NULL,
    `landing_page_id` INTEGER UNSIGNED NOT NULL,
    PRIMARY KEY(`event_id`)
) ENGINE = InnoDB;

create table `gameplay`.`registration_v2` (
    `subscriber_id` INTEGER UNSIGNED NOT NULL,
    `device_id` VARCHAR(255) NOT NULL DEFAULT 'WEB',
    `registration_token` VARCHAR(15) NOT NULL,
    `status` ENUM('pending','validated','duplicate_phone') NOT NULL,
    `registration_date` DATETIME NOT NULL,
    PRIMARY KEY(`subscriber_id`),
    CONSTRAINT `fk_registration_v2_1` FOREIGN KEY `fk_registration_v2_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
    UNIQUE INDEX `registration_v2_device_id_UNIQUE` (`device_id` ASC),
    UNIQUE INDEX `registration_v2_registration_token_UNIQUE` (`registration_token` ASC)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`external_someone` (
  `external_someone_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `external_type` ENUM('FACEBOOK_ID','SUBSCRIBER_ID','NICKNAME','CELL_NUMBER', 'EMAIL') NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `creator_id` INTEGER UNSIGNED NOT NULL,
  `create_date` DATETIME NOT NULL,
  `updator_id` INTEGER UNSIGNED NULL,
  `update_date` DATETIME NULL,
  PRIMARY KEY (`external_someone_id`),
  CONSTRAINT `fk_external_someone_1` FOREIGN KEY `fk_external_someone_1` (`creator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_external_someone_2` FOREIGN KEY `fk_external_someone_2` (`updator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  INDEX `external_someone_value` (`value` ASC),
  INDEX (`external_type`),
  UNIQUE KEY `type_and_value` (`external_type`,`value`)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`pending_action` (
  `pending_action_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `originating_subscriber_id` INTEGER UNSIGNED NOT NULL,
  `external_someone_id` INTEGER UNSIGNED NOT NULL,
  `action_type` ENUM('JOIN_VIP_BOX_INVITATION', 'JOIN_SHOUT_INVITATION', 'WON_COUPON') NOT NULL,
  `payload` VARCHAR(255) NULL,
  `creator_id` INTEGER UNSIGNED NOT NULL,
  `create_date` DATETIME NOT NULL,
  `updator_id` INTEGER UNSIGNED NULL,
  `update_date` DATETIME NULL,
  PRIMARY KEY (`pending_action_id`),
  CONSTRAINT `fk_pending_action_1` FOREIGN KEY `fk_pending_action_1` (`originating_subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_pending_action_2` FOREIGN KEY `fk_pending_action_2` (`external_someone_id`) REFERENCES `external_someone` (`external_someone_id`),
  CONSTRAINT `fk_pending_action_3` FOREIGN KEY `fk_pending_action_3` (`creator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_pending_action_4` FOREIGN KEY `fk_pending_action_4` (`updator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  INDEX `idx_pending_action_1` (`external_someone_id` ASC)
  INDEX (`action_type`),
  INDEX (`payload`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`location` (
  `location_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NULL,
  `addr1` VARCHAR(128) NULL,
  `addr2` VARCHAR(128) NULL,
  `city` VARCHAR(64) NULL,
  `state` VARCHAR(32) NULL,
  `postal_code` VARCHAR(16) NULL,
  `country_code` INTEGER NULL,
  `longitude` DECIMAL(9,6) NULL,
  `latitude` DECIMAL(9,6) NULL,
  `creator_id` INTEGER UNSIGNED NOT NULL,
  `create_date` DATETIME NOT NULL,
  `updator_id` INTEGER UNSIGNED NULL,
  `update_date` DATETIME NULL,
  PRIMARY KEY (`location_id`),
  CONSTRAINT `fk_location_1` FOREIGN KEY `fk_location_1` (`creator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_location_2` FOREIGN KEY `fk_location_2` (`updator_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`org` (
  `org_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_org_id` INT UNSIGNED NULL,
  `name` VARCHAR(128) NULL,
  `addr1` VARCHAR(128) NULL,
  `addr2` VARCHAR(128) NULL,
  `city` VARCHAR(64) NULL,
  `state` VARCHAR(32) NULL,
  `postal_code` VARCHAR(16) NULL,
  `country_code` INTEGER NULL,
  `location_id` INT UNSIGNED NULL,
  `creator_id` INTEGER UNSIGNED NOT NULL,
  `create_date` DATETIME NOT NULL,
  `updator_id` INTEGER UNSIGNED NULL,
  `update_date` DATETIME NULL,
  PRIMARY KEY (`org_id`),
  CONSTRAINT `fk_org_1` FOREIGN KEY `fk_org_1` (`creator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_org_2` FOREIGN KEY `fk_org_2` (`updator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `fk_org_3` FOREIGN KEY `fk_org_3` (`parent_org_id`) REFERENCES `org` (`org_id`),
  CONSTRAINT `fk_org_4` FOREIGN KEY `fk_org_4` (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`vipbox_member` (
  `vipbox_member_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `member_role` ENUM('OWNER','ADMIN','MEMBER','VIEWER','CELEBRITY') NOT NULL,
  `member_status` ENUM('ACTIVE','INACTIVE','PENDING_ADDITION','PENDING_REMOVAL','PENDING_APPROVAL') NOT NULL,
  `nickname` VARCHAR(32) NOT NULL,
  `avatar_url` VARCHAR(256) NULL,
  `twitter_handle` VARCHAR(256) NULL,
  `hidden_member` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `score_delta` INT NOT NULL DEFAULT 0,
  `creator_id` INTEGER UNSIGNED NOT NULL,
  `create_date` DATETIME NOT NULL,
  `updator_id` INTEGER UNSIGNED NULL,
  `update_date` DATETIME NULL,
  PRIMARY KEY (`vipbox_member_id`),
  CONSTRAINT FOREIGN KEY (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT FOREIGN KEY (`creator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT FOREIGN KEY (`updator_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`vipbox` (
  `vipbox_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `type` ENUM('PERSONAL','CELEBRITY','SPONSOR','ONE_ON_ONE','CC_ROUND') NOT NULL,
  `invite_code` VARCHAR(16) NULL,
  `avatar_url` VARCHAR(256) NULL,
  `owner_name` VARCHAR(256) NULL,
  `owner_id` INTEGER UNSIGNED NOT NULL,
  `payout_rule_engine_name` VARCHAR(128) NULL DEFAULT 'default',
  `deal_engine_name` VARCHAR(128) NULL DEFAULT 'default',
  `is_default` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `status` ENUM('ACTIVE','INACTIVE','PENDING_ADDITION','PENDING_REMOVAL','DELETED') NOT NULL,
  `open_membership` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `location_checkin_required` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `auto_create` INTEGER(1) UNSIGNED NOT NULL DEFAULT 1,
  `org_id` INT UNSIGNED NULL,
  `location_id` INT UNSIGNED NULL,
  `vendor_id` INT NULL,
  `creator_id` INTEGER UNSIGNED NOT NULL,
  `create_date` DATETIME NOT NULL,
  `updator_id` INTEGER UNSIGNED NULL,
  `update_date` DATETIME NULL,
  PRIMARY KEY (`vipbox_id`),
  CONSTRAINT FOREIGN KEY (`owner_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT FOREIGN KEY (`org_id`) REFERENCES `org` (`org_id`),
  CONSTRAINT FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT FOREIGN KEY (`vendor_id`) REFERENCES `vendor` (`vendor_id`),
  CONSTRAINT FOREIGN KEY (`creator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT FOREIGN KEY (`updator_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  INDEX `vipbox_name` (`name` ASC),
  UNIQUE INDEX (`invite_code` ASC)
) ENGINE = InnoDB;


CREATE TABLE `gameplay`.`vipbox_members` (
  `vipbox_id` INT UNSIGNED NOT NULL,
  `vipbox_member_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`vipbox_id`, `vipbox_member_id`),
  CONSTRAINT FOREIGN KEY (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`),
  CONSTRAINT FOREIGN KEY (`vipbox_member_id`) REFERENCES `vipbox_member` (`vipbox_member_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`vipbox_sponsors` (
  `vipbox_id` INT UNSIGNED NOT NULL,
  `sponsor_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`vipbox_id`, `sponsor_id`),
  CONSTRAINT FOREIGN KEY (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`vipbox_event` (
    `vipbox_id` INTEGER UNSIGNED NOT NULL,
    `event_id` INTEGER UNSIGNED NOT NULL,
    `marketing_html` TEXT,
    PRIMARY KEY (`vipbox_id`, `event_id`),
    CONSTRAINT FOREIGN KEY (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`),
    CONSTRAINT FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`vipbox_invite_codes` (
    `vipbox_id` INTEGER UNSIGNED NOT NULL,
    `invite_code` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`invite_code`),
    CONSTRAINT FOREIGN KEY (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`company_vipbox` (
    `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    `company_name` VARCHAR(255) NOT NULL,
    `company_subscriber_id` INTEGER UNSIGNED NOT NULL,
    `vipbox_id` INTEGER UNSIGNED NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX (`company_name` ASC)
) ENGINE = InnoDB;

CREATE  TABLE `gameplay`.`vendor` (
  `vendor_id` INT NOT NULL AUTO_INCREMENT ,
  `abbr` VARCHAR(9) NOT NULL ,
  `icon_url` VARCHAR(128) NOT NULL ,
  `primary_color` VARCHAR(9) NOT NULL ,
  `bg_color` VARCHAR(9) NOT NULL ,
  `high_gradient_color` VARCHAR(9) NULL ,
  `low_gradient_color` VARCHAR(9) NULL ,
  `text_color` VARCHAR(9) NULL ,
  `checkbox_sd_url` VARCHAR(128) NOT NULL ,
  `checkbox_hd_url` VARCHAR(128) NOT NULL ,
  PRIMARY KEY (`vendor_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`vendor_code` (
    `vendor_id` INT NOT NULL,
    `code` VARCHAR(128) NOT NULL ,
    PRIMARY KEY (`vendor_id`, `code`),
    INDEX `vendor_vc1` (`code` ASC) ,
    CONSTRAINT `vendor_fk1` FOREIGN KEY `vendor_fk1` (`vendor_id`) REFERENCES `vendor` (`vendor_id`)
) ENGINE = InnoDB;

CREATE TABLE `subscriber_answer` (
 `subscriber_id` int unsigned NOT NULL,
 `event_id` int unsigned NOT NULL,
 `question_id` int unsigned NOT NULL,
 `answer_id` int unsigned NOT NULL,
 `alternate_answer_id` INTEGER UNSIGNED,
 `relative_answer_time_ms` int unsigned NOT NULL,
 `latitude` DECIMAL(9,6) DEFAULT NULL,
 `longitude` DECIMAL(9,6) DEFAULT NULL,
 `geo_error_margin` int DEFAULT NULL,
 `vipbox_ids` text,
 `answer_received_date` datetime NOT NULL,
 `create_date` datetime NOT NULL,
 `update_date` datetime NOT NULL,
 PRIMARY KEY (`subscriber_id`,`event_id`,`question_id`),
 KEY `subscriber_answer_answer_received_date` (`answer_received_date`),
 CONSTRAINT `fk_subscriber_answer_1` FOREIGN KEY `fk_subscriber_answer_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
 CONSTRAINT `fk_subscriber_answer_2` FOREIGN KEY `fk_subscriber_answer_2` (`event_id`) REFERENCES `event` (`event_id`),
 CONSTRAINT `fk_subscriber_answer_3` FOREIGN KEY `fk_subscriber_answer_3` (`question_id`) REFERENCES `poll` (`poll_id`),
 CONSTRAINT `fk_subscriber_answer_4` FOREIGN KEY `fk_subscriber_answer_4` (`answer_id`) REFERENCES `poll_answers` (`answer_id`)
) ENGINE=InnoDB;

-- drop table contest_campaign;
-- drop table contest_default_config;
-- drop table contest_payout_rule;
-- drop table contest_scoring_rule;
-- drop table contest_template_campaign;
-- drop table contest_template_default;
-- drop table contest_template_payout_rule;
-- drop table contest_template_scoring_rule;
-- drop table payout_rule;
-- drop table payout_rule_type;
-- drop table scoring_rule;
-- drop table scoring_rule_type;
-- drop table contest_template;
-- drop table contest;

CREATE TABLE `gameplay`.`scoring_rule_type` (
  `scoring_rule_type_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(254) NOT NULL,
  `description` TEXT NOT NULL,
  `config_json_schema` TEXT NULL,
  `active` INTEGER(1) UNSIGNED NOT NULL DEFAULT 1,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`scoring_rule_type_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`scoring_rule_type_onInsert`
BEFORE INSERT ON `gameplay`.`scoring_rule_type`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`scoring_rule_type_OnUpdate`
BEFORE UPDATE ON `gameplay`.`scoring_rule_type`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`scoring_rule` (
  `scoring_rule_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `scoring_rule_type_id` INTEGER UNSIGNED NOT NULL,
  `name` VARCHAR(254) NOT NULL,
  `algorithm_name` VARCHAR(254) NOT NULL,
  `description` TEXT NOT NULL,
  `config_json_schema` TEXT NULL,
  `active` INTEGER(1) UNSIGNED NOT NULL DEFAULT 1,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`scoring_rule_id`),
  CONSTRAINT `FK_scoring_rule_scoring_rule_type_id` FOREIGN KEY `FK_scoring_rule_scoring_rule_type_id` (`scoring_rule_type_id`) REFERENCES `scoring_rule_type` (`scoring_rule_type_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`scoring_rule_onInsert`
BEFORE INSERT ON `gameplay`.`scoring_rule`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`scoring_rule_OnUpdate`
BEFORE UPDATE ON `gameplay`.`scoring_rule`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`payout_rule_type` (
  `payout_rule_type_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(254) NOT NULL,
  `description` TEXT NOT NULL,
  `config_json_schema` TEXT NULL,
  `active` INTEGER(1) UNSIGNED NOT NULL DEFAULT 1,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`payout_rule_type_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`payout_rule_type_onInsert`
BEFORE INSERT ON `gameplay`.`payout_rule_type`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`payout_rule_type_OnUpdate`
BEFORE UPDATE ON `gameplay`.`payout_rule_type`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`payout_rule` (
  `payout_rule_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `payout_rule_type_id` INTEGER UNSIGNED NOT NULL,
  `name` VARCHAR(254) NOT NULL,
  `algorithm_name` VARCHAR(254) NOT NULL,
  `description` TEXT NOT NULL,
  `config_json_schema` TEXT NULL,
  `active` INTEGER(1) UNSIGNED NOT NULL DEFAULT 1,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`payout_rule_id`),
  CONSTRAINT `FK_payout_rule_payout_rule_type_id` FOREIGN KEY `FK_payout_rule_payout_rule_type_id` (`payout_rule_type_id`) REFERENCES `payout_rule_type` (`payout_rule_type_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`payout_rule_onInsert`
BEFORE INSERT ON `gameplay`.`payout_rule`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`payout_rule_OnUpdate`
BEFORE UPDATE ON `gameplay`.`payout_rule`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest` (
  `contest_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(254) NOT NULL,
  `description` TEXT NOT NULL,
  `type` ENUM('EVENT', 'CHANNEL', 'LEAGUE', 'GLOBAL', 'VIPBOX', 'QUESTION') NOT NULL,
  `vipbox_type` ENUM('NONE', 'VIPBOX', 'ALL_VIPBOXES') NOT NULL,
  `primary_ref_id` INTEGER UNSIGNED,
  `vipbox_id` INTEGER UNSIGNED,
  `contest_template_id` INT(10) UNSIGNED NULL,
  `status` ENUM('NEW', 'INPROGRESS', 'COMPLETE') NOT NULL,
  `start_date` DATETIME NULL,
  `end_date` DATETIME NULL,
  `marketing_html` TEXT NOT NULL,
  `rules_html` TEXT NOT NULL,
  `prizes_html` TEXT NOT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_id`),
  CONSTRAINT `FK_contest_vipbox_id` FOREIGN KEY `FK_contest_vipbox_id` (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`),
  CONSTRAINT `FK_contest_ctemplate_id` FOREIGN KEY (`contest_template_id` ) REFERENCES `gameplay`.`contest_template` (`contest_template_id`),
  INDEX `IDX_contest_type` (`type` ASC),
  INDEX `IDX_contest_vipbox_type` (`vipbox_type` ASC),
  INDEX `IDX_contest_type_vipbox_type` (`type` ASC, `vipbox_type` ASC),
  INDEX `IDX_contest_primary_ref_id` (`primary_ref_id` ASC),
  INDEX `IDX_contest_vipbox_id` (`vipbox_id` ASC),
  INDEX `FK_contest_ctemplate_id_idx` (`contest_template_id` ASC),
  INDEX `IDX_contest_primary_ref_id_type` (`primary_ref_id` ASC, `type` ASC),
  INDEX `IDX_contest_primary_ref_id_type_vipbox_id_vipbox_type` (`primary_ref_id` ASC, `type` ASC, `vipbox_id` ASC, `vipbox_type` ASC)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_onInsert`
BEFORE INSERT ON `gameplay`.`contest`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest_scoring_rule` (
  `contest_scoring_rule_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_id` INTEGER UNSIGNED NOT NULL,
  `scoring_rule_id` INTEGER UNSIGNED NOT NULL,
  `config` TEXT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_scoring_rule_id`),
  UNIQUE INDEX(`contest_id`, `scoring_rule_id`),
  CONSTRAINT `FK_contest_rule_contest_id` FOREIGN KEY `FK_contest_rule_contest_id` (`contest_id`) REFERENCES `contest` (`contest_id`),
  CONSTRAINT `FK_contest_scoring_rule_scoring_rule_id` FOREIGN KEY `FK_contest_scoring_rule_scoring_rule_id` (`scoring_rule_id`) REFERENCES `scoring_rule` (`scoring_rule_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_scoring_rule_onInsert`
BEFORE INSERT ON `gameplay`.`contest_scoring_rule`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_scoring_rule_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_scoring_rule`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest_payout_rule` (
  `contest_payout_rule_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_id` INTEGER UNSIGNED NOT NULL,
  `payout_rule_id` INTEGER UNSIGNED NOT NULL,
  `config` TEXT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_payout_rule_id`),
  UNIQUE INDEX(`contest_id`, `payout_rule_id`),
  CONSTRAINT `FK_contest_payout_rule_contest_id` FOREIGN KEY `FK_contest_payout_rule_contest_id` (`contest_id`) REFERENCES `contest` (`contest_id`),
  CONSTRAINT `FK_contest_payout_rule_payout_rule_id` FOREIGN KEY `FK_contest_payout_rule_payout_rule_id` (`payout_rule_id`) REFERENCES `payout_rule` (`payout_rule_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_payout_rule_onInsert`
BEFORE INSERT ON `gameplay`.`contest_payout_rule`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_payout_rule_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_payout_rule`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest_campaign` (
  `contest_campaign_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_id` INTEGER UNSIGNED NOT NULL,
  `campaign_id` INTEGER UNSIGNED NOT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_campaign_id`),
  UNIQUE INDEX(`contest_id`, `campaign_id`),
  CONSTRAINT `FK_contest_campaign_contest_id` FOREIGN KEY `FK_contest_campaign_contest_id` (`contest_id`) REFERENCES `contest` (`contest_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_campaign_onInsert`
BEFORE INSERT ON `gameplay`.`contest_campaign`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_campaign_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_campaign`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

-- primary_ref_id, start_date, end_date, vipbox_id, campaignId

CREATE TABLE `gameplay`.`contest_template` (
  `contest_template_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(254) NOT NULL,
  `description` TEXT NOT NULL,
  `type` ENUM('EVENT', 'CHANNEL', 'LEAGUE', 'GLOBAL', 'VIPBOX', 'QUESTION') NOT NULL,
  `vipbox_type` ENUM('NONE', 'VIPBOX', 'ALL_VIPBOXES') NOT NULL,
  `marketing_html` TEXT NOT NULL,
  `rules_html` TEXT NOT NULL,
  `prizes_html` TEXT NOT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_template_id`),
  INDEX `IDX_contest_type` (`type` ASC),
  INDEX `IDX_contest_vipbox_type` (`vipbox_type` ASC),
  INDEX `IDX_contest_type_vipbox_type` (`type` ASC, `vipbox_type` ASC)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_template_onInsert`
BEFORE INSERT ON `gameplay`.`contest_template`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_template_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_template`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest_template_scoring_rule` (
  `contest_template_scoring_rule_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_template_id` INTEGER UNSIGNED NOT NULL,
  `scoring_rule_id` INTEGER UNSIGNED NOT NULL,
  `config` TEXT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_template_scoring_rule_id`),
  UNIQUE INDEX(`contest_template_id`, `scoring_rule_id`),
  CONSTRAINT `FK_contest_template_rule_contest_template_id` FOREIGN KEY `FK_contest_template_rule_contest_template_id` (`contest_template_id`) REFERENCES `contest_template` (`contest_template_id`),
  CONSTRAINT `FK_contest_template_scoring_rule_scoring_rule_id` FOREIGN KEY `FK_contest_template_scoring_rule_scoring_rule_id` (`scoring_rule_id`) REFERENCES `scoring_rule` (`scoring_rule_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_template_scoring_rule_onInsert`
BEFORE INSERT ON `gameplay`.`contest_template_scoring_rule`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_template_scoring_rule_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_template_scoring_rule`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;


CREATE TABLE `gameplay`.`contest_template_payout_rule` (
  `contest_template_payout_rule_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_template_id` INTEGER UNSIGNED NOT NULL,
  `payout_rule_id` INTEGER UNSIGNED NOT NULL,
  `config` TEXT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_template_payout_rule_id`),
  UNIQUE INDEX(`contest_template_id`, `payout_rule_id`),
  CONSTRAINT `FK_contest_template_payout_rule_contest_template_id` FOREIGN KEY `FK_contest_template_payout_rule_contest_template_id` (`contest_template_id`) REFERENCES `contest_template` (`contest_template_id`),
  CONSTRAINT `FK_contest_template_payout_rule_payout_rule_id` FOREIGN KEY `FK_contest_template_payout_rule_payout_rule_id` (`payout_rule_id`) REFERENCES `payout_rule` (`payout_rule_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_template_payout_rule_onInsert`
BEFORE INSERT ON `gameplay`.`contest_template_payout_rule`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_template_payout_rule_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_template_payout_rule`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest_template_campaign` (
  `contest_template_campaign_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_template_id` INTEGER UNSIGNED NOT NULL,
  `campaign_id` INTEGER UNSIGNED NOT NULL,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_template_campaign_id`),
  UNIQUE INDEX(`contest_template_id`, `campaign_id`),
  CONSTRAINT `FK_contest_template_campaign_contest_id` FOREIGN KEY `FK_contest_template_campaign_contest_id` (`contest_template_id`) REFERENCES `contest_template` (`contest_template_id`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_template_campaign_onInsert`
BEFORE INSERT ON `gameplay`.`contest_template_campaign`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_template_campaign_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_template_campaign`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest_template_default` (
  `contest_template_default_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_template_id` INTEGER UNSIGNED NOT NULL,
  `type` ENUM('EVENT', 'CHANNEL', 'LEAGUE', 'GLOBAL', 'VIPBOX', 'QUESTION') NOT NULL,
  `vipbox_type` ENUM('NONE', 'VIPBOX', 'ALL_VIPBOXES') NOT NULL,
  `primary_ref_id` INTEGER UNSIGNED,
  `vipbox_id` INTEGER UNSIGNED,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_template_default_id`),
  CONSTRAINT `FK_contest_template_default_contest_template_id` FOREIGN KEY `FK_contest_template_default_contest_template_id` (`contest_template_id`) REFERENCES `contest_template` (`contest_template_id`),
  CONSTRAINT `FK_contest_template_default_vipbox_id` FOREIGN KEY `FK_contest_template_default_vipbox_id` (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`),
  INDEX `IDX_contest_template_default_type` (`type` ASC),
  INDEX `IDX_contest_template_default_vipbox_type` (`vipbox_type` ASC),
  INDEX `IDX_contest_template_default_type_vipbox_type` (`type` ASC, `vipbox_type` ASC),
  INDEX `IDX_contest_template_default_primary_ref_id` (`primary_ref_id` ASC),
  INDEX `IDX_contest_template_default_vipbox_id` (`vipbox_id` ASC),
  INDEX `IDX_contest_template_default_primary_ref_id_type` (`primary_ref_id` ASC, `type` ASC),
  INDEX `IDX_contest_template_dflt_prim_ref_vid_vtype` (`primary_ref_id` ASC, `type` ASC, `vipbox_id` ASC, `vipbox_type` ASC)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_template_default_onInsert`
BEFORE INSERT ON `gameplay`.`contest_template_default`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_template_default_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_template_default`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`contest_default_config` (
  `contest_default_config_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `scope_type` ENUM('EVENT', 'CHANNEL', 'LEAGUE', 'GLOBAL', 'EVENT_VIPBOX', 'VIPBOX') NOT NULL,
  `config` TEXT NULL,
  `primary_ref_id` INTEGER UNSIGNED,
  `vipbox_id` INTEGER UNSIGNED,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`contest_default_config_id`),
  CONSTRAINT `FK_contest_default_config_vipbox_id` FOREIGN KEY `FK_contest_default_config_vipbox_id` (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`),
  INDEX `IDX_contest_default_config_scope_type` (`scope_type` ASC),
  INDEX `IDX_contest_dfl_cfg_prim_id_scope_type` (`primary_ref_id`, `scope_type` ASC)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`contest_default_config_onInsert`
BEFORE INSERT ON `gameplay`.`contest_default_config`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`contest_default_config_OnUpdate`
BEFORE UPDATE ON `gameplay`.`contest_default_config`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;

CREATE TABLE `gameplay`.`champions_challenge` (
  `champions_challenge_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `contest_id` INTEGER UNSIGNED NULL,
  `name` VARCHAR(255) NOT NULL,
  `status` ENUM('DRAFT','PUBLISHED','CANCELLED','COMPLETE') NOT NULL DEFAULT 'DRAFT',
  `create_date` DATETIME NOT NULL,
  `last_update` DATETIME NULL,
  PRIMARY KEY(`champions_challenge_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`champions_challenge_round` (
  `round_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `champions_challenge_id` INTEGER UNSIGNED NOT NULL,
  `round` INTEGER UNSIGNED NOT NULL,
  `contest_template_id` INTEGER UNSIGNED NOT NULL,
  `final` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY(`round_id`),
  CONSTRAINT `FK_champions_challenge_round_1` FOREIGN KEY `FK_champions_challenge_round_1` (`champions_challenge_id`) REFERENCES `champions_challenge` (`champions_challenge_id`),
  CONSTRAINT `FK_champions_challenge_round_2` FOREIGN KEY `FK_champions_challenge_round_2` (`contest_template_id`) REFERENCES `contest` (`contest_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`champions_challenge_events` (
  `champions_challenge_id` INTEGER UNSIGNED NOT NULL,
  `event_id` INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(`champions_challenge_id`, `event_id`),
  CONSTRAINT `FK_champions_challenge_events_1` FOREIGN KEY `FK_champions_challenge_events_1` (`champions_challenge_id`) REFERENCES `champions_challenge` (`champions_challenge_id`),
  CONSTRAINT `FK_champions_challenge_events_2` FOREIGN KEY `FK_champions_challenge_events_2` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;
  
 CREATE TABLE `gameplay`.`one_on_one` (
  `one_on_one_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `champions_challenge_id` INTEGER UNSIGNED NOT NULL,
  `vipbox_id` INTEGER UNSIGNED NOT NULL,
  `round_id` INTEGER UNSIGNED NOT NULL,
  `event_id` INTEGER UNSIGNED NOT NULL,
  `end_date` TIMESTAMP NOT NULL,
  PRIMARY KEY(`one_on_one_id`),
  CONSTRAINT `FK_one_on_one_1` FOREIGN KEY `FK_one_on_one_1` (`champions_challenge_id`) REFERENCES `champions_challenge` (`champions_challenge_id`),
  CONSTRAINT `FK_one_on_one_2` FOREIGN KEY `FK_one_on_one_2` (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`),
  CONSTRAINT `FK_one_on_one_3` FOREIGN KEY `FK_one_on_one_3` (`round_id`) REFERENCES `champions_challenge_round` (`round_id`),
  CONSTRAINT `FK_one_on_one_4` FOREIGN KEY `FK_one_on_one_4` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

#is this supposed to be cc_1on1_result?
# CREATE TABLE `gameplay`.`one_on_one_result` (
#  `one_on_one_id` INTEGER UNSIGNED NOT NULL,
#  `subscriber_id` INTEGER UNSIGNED NOT NULL,
#  `status` ENUM('NONE','LOST','WON_BELOW','WON_ABOVE') NOT NULL DEFAULT 'NONE',
#  `status_date` TIMESTAMP NOT NULL,
#  `action` ENUM('NONE','REDEEMED','ADVANCED') NOT NULL,
#  `action_date` TIMESTAMP NOT NULL,
#  PRIMARY KEY(`one_on_one_id`,`subscriber_id`),
#  CONSTRAINT `FK_one_on_one_result_1` FOREIGN KEY `FK_one_on_one_result_1` (`one_on_one_id`) REFERENCES `one_on_one` (`one_on_one_id`),
#  CONSTRAINT `FK_one_on_one_result_2` FOREIGN KEY `FK_one_on_one_result_2` (`subscriber_id`) REFERENCES `subscriber_ext` (`subscriber_id`)
#) ENGINE = InnoDB;

 CREATE TABLE `gameplay`.`round_vipbox` (
  `round_id` INTEGER UNSIGNED NOT NULL,
  `vipbox_id` INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(`round_id`,`vipbox_id`),
  CONSTRAINT `FK_round_vipbox_1` FOREIGN KEY `FK_round_vipbox_1` (`round_id`) REFERENCES `champions_challenge_round` (`round_id`),
  CONSTRAINT `FK_round_vipbox_2` FOREIGN KEY `FK_round_vipbox_2` (`vipbox_id`) REFERENCES `vipbox` (`vipbox_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`powerup_types` (
  `powerup_type_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `powerup_type` VARCHAR(255) NOT NULL,
  `question_combo` INT(1) UNSIGNED NOT NULL DEFAULT 0,
  `event_limit` INT UNSIGNED NOT NULL DEFAULT 1,
  PRIMARY KEY(`powerup_type_id`),
  UNIQUE INDEX `powerup_types_type_UNIQUE` (`powerup_type` ASC)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`powerup_usage` (
  `powerup_usage_id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `subscriber_id`  INT UNSIGNED NOT NULL ,
  `subscriber_entitlement_uuid` CHAR(36) NOT NULL ,
  `powerup_type_id` INT UNSIGNED NOT NULL,
  `question_id` INT UNSIGNED NULL ,
  `event_id` INT UNSIGNED NULL ,
  `cc_id` INT UNSIGNED NULL, 
  `usage_status` ENUM('RESERVED', 'CONSUMED') NOT NULL ,
  PRIMARY KEY (`powerup_usage_id`) ,
  CONSTRAINT `FK_powerup_usage_1` FOREIGN KEY `FK_powerup_usage_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`),
  CONSTRAINT `FK_powerup_usage_4` FOREIGN KEY `FK_powerup_usage_4` (`powerup_type_id`) REFERENCES `powerup_types` (`powerup_type_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`powerup_pass` (
  `pp_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `pp_type` ENUM('PASS_1DAY', 'PASS_1WEEK', 'PASS_1MONTH') NOT NULL,
  `subscriber_id`  INT UNSIGNED NOT NULL, 
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY(`pp_id`),
  CONSTRAINT `FK_powerup_pass_1` FOREIGN KEY `FK_powerup_pass_1` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`powerup_pass_event` (
  `event_id` INT UNSIGNED NOT NULL, 
  `subscriber_id` INT UNSIGNED NOT NULL, 
  `granted_date` TIMESTAMP NOT NULL,
  PRIMARY KEY(`event_id`,`subscriber_id`),
  CONSTRAINT `FK_powerup_pass_event_1` FOREIGN KEY `FK_powerup_pass_event_1` (`event_id`) REFERENCES `event` (`event_id`),
  CONSTRAINT `FK_powerup_pass_event_2` FOREIGN KEY `FK_powerup_pass_event_2` (`subscriber_id`) REFERENCES `s_subscriber` (`subscriber_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`powerup_pass_entitlements` (
  `pp_type` ENUM('PASS_1DAY', 'PASS_1WEEK', 'PASS_1MONTH') NOT NULL,
  `entitlement_id` INT UNSIGNED NOT NULL,
  `amount` INT UNSIGNED NOT NULL,
  PRIMARY KEY(`pp_type`,`entitlement_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`throwdown` (
  `throwdown_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `from_subscriber_id` INTEGER UNSIGNED NOT NULL,
  `to_subscriber_id` INTEGER UNSIGNED NOT NULL,
  `event_id` INTEGER UNSIGNED NOT NULL,
  `vipbox_id` INTEGER UNSIGNED NOT NULL,
  `question_id` INTEGER UNSIGNED,
  `status` ENUM('NEW', 'CANCELLED', 'REJECTED', 'ACCEPTED', 'OPEN', 'CLOSED') NOT NULL,
  `points` INTEGER UNSIGNED NOT NULL DEFAULT 100,
  `message` TEXT DEFAULT NULL,
  `response_date` DATETIME,
  `result_date` DATETIME,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` DATETIME,
  PRIMARY KEY(`throwdown_id`),
  CONSTRAINT `FK_throwdown_event_id` FOREIGN KEY `FK_throwdown_event_id` (`event_id`) REFERENCES `event` (`event_id`),
  CONSTRAINT `FK_throwdown_question_id` FOREIGN KEY `FK_throwdown_question_id` (`question_id`) REFERENCES `poll` (`poll_id`),
  INDEX `IDX_throwdown_question_status` (`question_id`, `status`),
  INDEX `IDX_throwdown_event_status` (`event_id`, `status`)
) ENGINE = InnoDB;

DELIMITER $$

CREATE
TRIGGER `gameplay`.`throwdown_onInsert`
BEFORE INSERT ON `gameplay`.`throwdown`
FOR EACH ROW
SET NEW.last_updated = IFNULL(NEW.last_updated, NOW())$$

CREATE
TRIGGER `gameplay`.`throwdown_OnUpdate`
BEFORE UPDATE ON `gameplay`.`throwdown`
FOR EACH ROW
SET NEW.last_updated = NOW()$$

DELIMITER ;
