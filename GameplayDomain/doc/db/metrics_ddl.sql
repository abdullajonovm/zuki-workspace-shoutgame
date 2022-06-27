USE `gameplay`;

CREATE TABLE `gameplay`.`metrics_subscriber_event_score` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `event_id` INTEGER UNSIGNED NOT NULL,
  `event_date` TIMESTAMP NOT NULL,
  `points_earned` INTEGER NOT NULL,
  `total_questions_answered` INTEGER UNSIGNED NOT NULL, 
  `longest_question_streak` INTEGER UNSIGNED NOT NULL, 
  `leaderboard_rank` INTEGER UNSIGNED NOT NULL, 
  `avg_correct_voting_pct` DECIMAL(5,3) NOT NULL,
  PRIMARY KEY(`subscriber_id`, `event_id`),
  CONSTRAINT `FK_metrics_subscriber_event_processed_status_1` FOREIGN KEY `FK_metrics_subscriber_event_processed_status_1` (`subscriber_id`) REFERENCES `subscriber_ext` (`subscriber_id`),
  CONSTRAINT `FK_metrics_subscriber_event_processed_status_2` FOREIGN KEY `FK_metrics_subscriber_event_processed_status_2` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`metrics_prize` (
  `prize_id` INT NOT NULL AUTO_INCREMENT,
  `event_id` INTEGER UNSIGNED NULL,
  `prize_type` enum('CASH', 'COUPON', 'PRIZE') NOT NULL,
  `amount` DECIMAL(9,2) NULL,
  `description` VARCHAR(255) NULL,
  `prize_date` TIMESTAMP NOT NULL,
  PRIMARY KEY(`prize_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`metrics_subscriber_prize` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `prize_id` INT NOT NULL,
  PRIMARY KEY(`subscriber_id`, `prize_id`),
  CONSTRAINT `FK_metrics_subscriber_prize_1` FOREIGN KEY `FK_metrics_subscriber_prize_1` (`subscriber_id`) REFERENCES `subscriber_ext` (`subscriber_id`),
  CONSTRAINT `FK_metrics_subscriber_prize_2` FOREIGN KEY `FK_metrics_subscriber_prize_2` (`prize_id`) REFERENCES `metrics_prize` (`prize_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`metrics_event_processed_status` (
  `event_id` INTEGER UNSIGNED NOT NULL,
  `scoring` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `prizes` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY(`event_id`),
  CONSTRAINT `FK_metrics_event_processed_status_1` FOREIGN KEY `FK_metrics_event_processed_status_1` (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`metrics_cc_processed_status` (
  `cc_id` INTEGER UNSIGNED NOT NULL,
  `complete` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
  `num_events` INTEGER UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY(`cc_id`),
  CONSTRAINT `FK_metrics_cc_processed_status_1` FOREIGN KEY `FK_metrics_cc_processed_status_1` (`cc_id`) REFERENCES `cc_challenge` (`cc_id`)
) ENGINE = InnoDB;

CREATE TABLE `gameplay`.`metrics_subscriber_cc_data` (
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `cc_id` INTEGER UNSIGNED NOT NULL,
  `num_wins` INTEGER NOT NULL DEFAULT 0,
  `num_losses` INTEGER NOT NULL DEFAULT 0,
  `highest_level` INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY(`subscriber_id`, `cc_id`),
  CONSTRAINT `FK_metrics_subscriber_cc_data_1` FOREIGN KEY `FK_metrics_subscriber_cc_data_1` (`subscriber_id`) REFERENCES `subscriber_ext` (`subscriber_id`),
  CONSTRAINT `FK_metrics_subscriber_cc_data_2` FOREIGN KEY `FK_metrics_subscriber_cc_data_2` (`cc_id`) REFERENCES `cc_challenge` (`cc_id`)
) ENGINE = InnoDB;
