CREATE SCHEMA `shoutmillionaire` DEFAULT CHARACTER SET utf8mb4 ;

CREATE TABLE `shoutmillionaire`.`multi_localization` (
  `uuid` CHAR(36) NOT NULL,
  `type` VARCHAR(45) NOT NULL,
  `language_code` VARCHAR(5) NOT NULL,
  `value` TEXT NOT NULL,
  PRIMARY KEY (`uuid`),
  INDEX `ml_type_idx` (`type` ASC));
  
CREATE TABLE `shoutmillionaire`.`question_category_list` (
  `id` CHAR(36) NOT NULL,
  `category_key` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`));
  
 CREATE TABLE `shoutmillionaire`.`question` (
  `id` CHAR(36) NOT NULL,
  `difficulty` INT NOT NULL,
  `source` VARCHAR(255) NULL,
  `media_url` VARCHAR(255) NULL,
  `media_type` VARCHAR(45) NULL,
  `create_date` TIMESTAMP NOT NULL,
  `expiration_date` TIMESTAMP NULL,
  `usage_count` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(45) NOT NULL DEFAULT 'PUBLISHED',
  PRIMARY KEY (`id`));
 
CREATE TABLE `shoutmillionaire`.`question_language_codes` (
  `question_id` CHAR(36) NOT NULL,
  `language_code` VARCHAR(5) NOT NULL,
  PRIMARY KEY (`question_id`, `language_code`),
  CONSTRAINT `question_language_fk1`
    FOREIGN KEY (`question_id`)
    REFERENCES `shoutmillionaire`.`question` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `shoutmillionaire`.`question_forbidden_country_codes` (
  `question_id` CHAR(36) NOT NULL,
  `country_code` VARCHAR(5) NOT NULL,
  PRIMARY KEY (`question_id`, `country_code`),
  CONSTRAINT `question_country_fk1`
    FOREIGN KEY (`question_id`)
    REFERENCES `shoutmillionaire`.`question` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `shoutmillionaire`.`question_categories` (
  `question_id` CHAR(36) NOT NULL,
  `category_id` CHAR(36) NOT NULL,
  PRIMARY KEY (`question_id`, `category_id`),
  INDEX `question_category_fk2_idx` (`category_id` ASC),
  CONSTRAINT `question_category_fk1`
    FOREIGN KEY (`question_id`)
    REFERENCES `shoutmillionaire`.`question` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `question_category_fk2`
    FOREIGN KEY (`category_id`)
    REFERENCES `shoutmillionaire`.`question_category_list` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `shoutmillionaire`.`question_answer` (
  `id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `media_url` VARCHAR(255) NULL,
  `media_type` VARCHAR(45) NULL,
  `correct` INT(1) NULL,
  `survey_percent` INT NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `question_answer_fk1_idx` (`question_id` ASC),
  CONSTRAINT `question_answer_fk1`
    FOREIGN KEY (`question_id`)
    REFERENCES `shoutmillionaire`.`question` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `shoutmillionaire`.`match_question` (
  `id` CHAR(36) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `match_id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `question_value` VARCHAR(45) NOT NULL,
  `match_question_status` VARCHAR(45) NOT NULL,
  `won_subscriber_id` INT UNSIGNED NULL,
  `determination` VARCHAR(45) NULL,
  `create_date` TIMESTAMP NOT NULL,
  `completed_date` TIMESTAMP NULL,
  INDEX `matchquestion_fk1_idx` (`question_id` ASC),
  CONSTRAINT `matchquestion_fk1`
    FOREIGN KEY (`question_id`)
    REFERENCES `shoutmillionaire`.`question` (`id`)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  PRIMARY KEY (`id`));
 
CREATE TABLE `shoutmillionaire`.`subscriber_question_answer` (
  `id` CHAR(36) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `match_id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `match_question_id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `selected_answer_id` CHAR(36) NULL,
  `question_decrypt_key` CHAR(255) NULL,
  `question_presented_timestamp` TIMESTAMP(6) NULL DEFAULT NULL,
  `duration_milliseconds` INT NULL,
  `determination` VARCHAR(45) NULL,
  `won` INT(1) NOT NULL DEFAULT 0,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `sqa_fk1_idx` (`question_id` ASC),
  INDEX `sqa_fk2_idx` (`match_question_id` ASC),
  INDEX `sqa_fk3_idx` (`selected_answer_id` ASC),
  CONSTRAINT `sqa_fk1`
    FOREIGN KEY (`question_id`)
    REFERENCES `shoutmillionaire`.`question` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sqa_fk2`
    FOREIGN KEY (`match_question_id`)
    REFERENCES `shoutmillionaire`.`match_question` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sqa_fk3`
    FOREIGN KEY (`selected_answer_id`)
    REFERENCES `shoutmillionaire`.`question_answer` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE `shoutmillionaire`.`bot_player` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `busy_flag` INT(1) NOT NULL DEFAULT 0,
  `game_id` CHAR(36) NULL,
  `last_used_date` TIMESTAMP NULL,
  PRIMARY KEY (`subscriber_id`));

CREATE TABLE `shoutmillionaire`.`phone_verification_code` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `phone` VARCHAR(45) NOT NULL,
  `code` VARCHAR(45) NOT NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`subscriber_id`, `code`));  