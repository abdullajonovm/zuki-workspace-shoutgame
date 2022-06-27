CREATE SCHEMA `contest` DEFAULT CHARACTER SET utf8mb4 ;

CREATE TABLE `contest`.`multi_localization` (
  `uuid` CHAR(36) NOT NULL,
  `type` VARCHAR(45) NOT NULL,
  `language_code` VARCHAR(5) NOT NULL,
  `value` TEXT NOT NULL,
  PRIMARY KEY (`uuid`, `type`),
  INDEX `ml_type_idx` (`type` ASC));
  
 CREATE TABLE `contest`.`game` (
  `id` CHAR(36) NOT NULL,
  `game_engine` VARCHAR(45) NOT NULL,
  `engine_type` VARCHAR(45) NOT NULL,
  `game_photo_url` VARCHAR(512) NULL,
  `game_status` VARCHAR(45) NOT NULL,
  `bracket_elimination_count` INT NULL,
  `allow_bots` INT(1) NOT NULL DEFAULT 1,
  `payout_calculation_method` VARCHAR(45) NOT NULL DEFAULT 'STATIC',
  `payout_house_take_percentage` DECIMAL(5,2) NOT NULL DEFAULT 0,
  `payout_percentage_of_users_to_award` DECIMAL(5,2) NOT NULL DEFAULT 0,
  `include_activity_answers_before_scoring` INT(1) NOT NULL DEFAULT 0,
  `pending_date` TIMESTAMP NOT NULL,
  `cancelled_date` TIMESTAMP NULL,
  `open_date` TIMESTAMP NULL,
  `inplay_date` TIMESTAMP NULL,
  `closed_date` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `game_status_idx` (`game_status` ASC));

 CREATE TABLE `contest`.`game_app_ids` (
  `game_id` CHAR(36) NOT NULL,
  `app_id` INT NOT NULL,
  PRIMARY KEY (`game_id`, `app_id`),
  CONSTRAINT `game_app_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
  
 CREATE TABLE `contest`.`game_language_codes` (
  `game_id` CHAR(36) NOT NULL,
  `language_code` VARCHAR(5) NOT NULL,
  PRIMARY KEY (`game_id`, `language_code`),
  CONSTRAINT `game_language_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `contest`.`game_forbidden_country_codes` (
  `game_id` CHAR(36) NOT NULL,
  `country_code` CHAR(2) NOT NULL,
  PRIMARY KEY (`game_id`, `country_code`),
  CONSTRAINT `game_country_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `contest`.`round` (
  `id` CHAR(36) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `round_type` VARCHAR(45) NOT NULL,
  `round_status` VARCHAR(45) NOT NULL,
  `round_sequence` INT UNSIGNED NOT NULL,
  `final_round` INT(1) NOT NULL DEFAULT 0,
  `round_purse` DOUBLE NULL,
  `current_player_count` INT NOT NULL DEFAULT 0,
  `maximum_player_count` INT NOT NULL,
  `minimum_match_count` INT NOT NULL,
  `maximum_match_count` INT NULL,
  `cost_per_player` DOUBLE NULL,
  `round_activity_type` VARCHAR(255) NOT NULL,
  `round_activity_value` VARCHAR(45) NOT NULL,
  `minimum_activity_to_win_count` INT NOT NULL,
  `maximum_activity_count` INT NULL,
  `activity_minimum_difficulty` INT NULL,
  `activity_maximum_difficulty` INT NULL,
  `activity_maximum_duration_seconds` INT NOT NULL,
  `player_maximum_duration_seconds` INT NOT NULL,
  `duration_between_activities_seconds` INT NOT NULL DEFAULT 10,
  `match_global` INT(1) NOT NULL DEFAULT 0,
  `maximum_duration_minutes` INT NULL,
  `match_player_count` INT NOT NULL,
  `pending_date` TIMESTAMP NOT NULL,
  `cancelled_date` TIMESTAMP NULL,
  `visible_date` TIMESTAMP NULL,
  `expected_open_date` TIMESTAMP NULL,
  `open_date` TIMESTAMP NULL,
  `inplay_date` TIMESTAMP NULL,
  `closed_date` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `round_fk1_idx` (`game_id` ASC),
  CONSTRAINT `round_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE `contest`.`game_player` (
  `id` CHAR(36) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `rank` DOUBLE NULL,
  `payout_payment_id` VARCHAR(255) NULL,
  `payout_awarded_amount` DOUBLE NULL,
  `payout_venue` VARCHAR(255) NULL,
  `payout_completed` INT(1) NOT NULL DEFAULT 0,
  `determination` VARCHAR(45) NOT NULL,
  `countdown_to_elimination` INT NULL,
  `next_round_id` CHAR(36) NULL,
  `last_round_id` CHAR(36) NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `gameplayer_fk1_idx` (`game_id` ASC),
  INDEX `gameplayer_fk2_idx` (`next_round_id` ASC),
  INDEX `gameplayer_fk3_idx` (`last_round_id` ASC),
  CONSTRAINT `gameplayer_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `gameplayer_fk2`
    FOREIGN KEY (`next_round_id`)
    REFERENCES `contest`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `gameplayer_fk3`
    FOREIGN KEY (`last_round_id`)
    REFERENCES `contest`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION    
);

CREATE TABLE `contest`.`round_categories` (
  `round_category_id` INT NOT NULL AUTO_INCREMENT,
  `round_id` CHAR(36) NOT NULL,
  `category` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`round_category_id`),
  INDEX `round_categories_fk1_idx` (`round_id` ASC),
  INDEX `round_categories_idx1` (`category` ASC),
  CONSTRAINT `round_categories_fk1`
    FOREIGN KEY (`round_id`)
    REFERENCES `contest`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `contest`.`round_player` (
  `id` CHAR(36) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `played_match_count` INT NULL,
  `determination` VARCHAR(45) NOT NULL,
  `receipt_id` VARCHAR(255) NULL,
  `amount_paid` DOUBLE NULL,
  `refunded` INT(1) NULL,
  `skill_answer_correct_pct` DOUBLE NULL,
  `skill_average_answer_ms` INT NULL,
  `rank` DOUBLE NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `roundplayer_fk1_idx` (`game_id` ASC),
  INDEX `roundplayer_fk2_idx` (`round_id` ASC),
  CONSTRAINT `roundplayer_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `roundplayer_fk2`
    FOREIGN KEY (`round_id`)
    REFERENCES `contest`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `contest`.`match` (
  `id` CHAR(36) NOT NULL,
  `game_engine` VARCHAR(45) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `match_status` VARCHAR(45) NOT NULL,
  `won_subscriber_id` INT UNSIGNED NULL,
  `minimum_activity_to_win_count` INT NOT NULL,
  `maximum_activity_count` INT NULL,
  `actual_activity_count` INT NULL,
  `send_next_question_at` TIMESTAMP(6) NULL,
  `determination` VARCHAR(45) NOT NULL,
  `start_date` TIMESTAMP NULL,
  `complete_date` TIMESTAMP NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `match_fk1_idx` (`game_id` ASC),
  INDEX `match_fk2_idx` (`round_id` ASC),
  CONSTRAINT `match_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `match_fk2`
    FOREIGN KEY (`round_id`)
    REFERENCES `contest`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
    
CREATE TABLE `contest`.`match_player` (
  `id` CHAR(36) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `match_id` CHAR(36) NOT NULL,
  `round_player_id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `determination` VARCHAR(45) NOT NULL,
  `score` DOUBLE NULL,
  `create_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `matchplayer_fk1_idx` (`game_id` ASC),
  INDEX `matchplayer_fk2_idx` (`round_id` ASC),
  INDEX `matchplayer_fk3_idx` (`match_id` ASC),
  INDEX `matchplayer_fk4_idx` (`round_player_id` ASC),
  CONSTRAINT `matchplayer_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `matchplayer_fk2`
    FOREIGN KEY (`round_id`)
    REFERENCES `contest`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `matchplayer_fk3`
    FOREIGN KEY (`match_id`)
    REFERENCES `contest`.`match` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `matchplayer_fk4`
    FOREIGN KEY (`round_player_id`)
    REFERENCES `contest`.`round_player` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
 
CREATE TABLE `contest`.`match_queue` (
  `id` CHAR(36) NOT NULL,
  `game_id` CHAR(36) NOT NULL,
  `round_id` CHAR(36) NOT NULL,
  `round_player_id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `cancelled` INT(1) NOT NULL DEFAULT 0,
  `enqueue_timestamp` TIMESTAMP NOT NULL,
  `dequeue_timestamp` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `matchqueue_fk1_idx` (`game_id` ASC),
  INDEX `matchqueue_fk2_idx` (`round_id` ASC),
  INDEX `matchqueue_fk3_idx` (`round_player_id` ASC),
  CONSTRAINT `matchqueue_fk1`
    FOREIGN KEY (`game_id`)
    REFERENCES `contest`.`game` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `matchqueue_fk2`
    FOREIGN KEY (`round_id`)
    REFERENCES `contest`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `matchqueue_fk3`
    FOREIGN KEY (`round_player_id`)
    REFERENCES `contest`.`round_player` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


CREATE TABLE `contest`.`cash_pool` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `amount` DECIMAL(15,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`subscriber_id`));

 CREATE TABLE `contest`.`cash_pool_transaction` (
  `cashpool_transaction_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `amount` DECIMAL(15,2) NOT NULL,
  `description` VARCHAR(128) NOT NULL,
  `receipt_id` INT UNSIGNED NULL COMMENT 'if this is a purchase, the store.receipt id',
  `context_uuid` CHAR(36) NULL COMMENT 'the game_uuid or round_uuid, if applicable',
  `transaction_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`cashpool_transaction_id`));