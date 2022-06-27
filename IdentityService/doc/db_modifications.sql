DROP TABLE if exists gameplay.subscriber_validate;

DROP TABLE if exists `gameplay`.`subscriber_signup_queue`;
CREATE TABLE `gameplay`.`subscriber_signup_queue`
(
  `subscriber_id` INTEGER UNSIGNED NOT NULL,
  `device_id` VARCHAR(255) NOT NULL DEFAULT 'WEB',
  `device_token` varchar(255) NOT NULL,
  PRIMARY KEY(`subscriber_id`)
) ENGINE = InnoDB;

ALTER TABLE `facebook`.`auth_subscriber` DROP COLUMN `subscriber_id` , CHANGE COLUMN `facebook_id` `facebook_id` VARCHAR(255) NOT NULL  FIRST 
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`facebook_id`) 
, ADD UNIQUE INDEX `access_token_UNIQUE` (`access_token` ASC) 
, DROP INDEX `facebook_id` ;

delete from facebook.callback where service_name = 'GamePlayService' and version = '3.0' and namespace = 'default' ;

ALTER TABLE `gameplay`.`blog_entry` DROP FOREIGN KEY `FK_blog_entry_1` ;
ALTER TABLE `gameplay`.`blog_entry` 
  ADD CONSTRAINT `FK_blog_entry_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
  
ALTER TABLE `gameplay`.`current_rankings` DROP FOREIGN KEY `fk_current_rankings_2` ;
ALTER TABLE `gameplay`.`current_rankings` 
  ADD CONSTRAINT `fk_current_rankings_2`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
  
ALTER TABLE `gameplay`.`event_subscriber` DROP FOREIGN KEY `fk_subscriber_id` ;
ALTER TABLE `gameplay`.`event_subscriber` 
  ADD CONSTRAINT `fk_subscriber_id`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );

ALTER TABLE `gameplay`.`external_someone` DROP FOREIGN KEY `fk_external_someone_1` , DROP FOREIGN KEY `fk_external_someone_2` ;
ALTER TABLE `gameplay`.`external_someone` 
  ADD CONSTRAINT `fk_external_someone_1`
  FOREIGN KEY (`creator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_external_someone_2`
  FOREIGN KEY (`updator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );

ALTER TABLE `gameplay`.`location` DROP FOREIGN KEY `fk_location_1` , DROP FOREIGN KEY `fk_location_2` ;
ALTER TABLE `gameplay`.`location` 
  ADD CONSTRAINT `fk_location_1`
  FOREIGN KEY (`creator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_location_2`
  FOREIGN KEY (`updator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );

ALTER TABLE `gameplay`.`metrics_subscriber_cc_data` DROP FOREIGN KEY `FK_metrics_subscriber_cc_data_1` ;
ALTER TABLE `gameplay`.`metrics_subscriber_cc_data` 
  ADD CONSTRAINT `FK_metrics_subscriber_cc_data_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );

ALTER TABLE `gameplay`.`metrics_subscriber_event_score` DROP FOREIGN KEY `FK_metrics_subscriber_event_processed_status_1` ;
ALTER TABLE `gameplay`.`metrics_subscriber_event_score` 
  ADD CONSTRAINT `FK_metrics_subscriber_event_processed_status_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );

 ALTER TABLE `gameplay`.`metrics_subscriber_prize` DROP FOREIGN KEY `FK_metrics_subscriber_prize_1` ;
ALTER TABLE `gameplay`.`metrics_subscriber_prize` 
  ADD CONSTRAINT `FK_metrics_subscriber_prize_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`network_winners` DROP FOREIGN KEY `fk_networkwinners_1` ;
ALTER TABLE `gameplay`.`network_winners` 
  ADD CONSTRAINT `fk_networkwinners_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`org` DROP FOREIGN KEY `fk_org_1` , DROP FOREIGN KEY `fk_org_2` ;
ALTER TABLE `gameplay`.`org` 
  ADD CONSTRAINT `fk_org_1`
  FOREIGN KEY (`creator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_org_2`
  FOREIGN KEY (`updator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`pending_action` DROP FOREIGN KEY `fk_pending_action_1` , DROP FOREIGN KEY `fk_pending_action_3` , DROP FOREIGN KEY `fk_pending_action_4` ;
ALTER TABLE `gameplay`.`pending_action` 
  ADD CONSTRAINT `fk_pending_action_1`
  FOREIGN KEY (`originating_subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION, 
  ADD CONSTRAINT `fk_pending_action_3`
  FOREIGN KEY (`creator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_pending_action_4`
  FOREIGN KEY (`updator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
 
ALTER TABLE `gameplay`.`poll_response` DROP FOREIGN KEY `FK_poll_response_subscriber` ;
ALTER TABLE `gameplay`.`poll_response` 
  ADD CONSTRAINT `FK_poll_response_subscriber`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`powerup_pass` DROP FOREIGN KEY `FK_powerup_passe_1` ;
ALTER TABLE `gameplay`.`powerup_pass` 
  ADD CONSTRAINT `FK_powerup_passe_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`powerup_pass_event` DROP FOREIGN KEY `FK_powerup_pass_event_2` ;
ALTER TABLE `gameplay`.`powerup_pass_event` 
  ADD CONSTRAINT `FK_powerup_pass_event_2`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` )
  ON DELETE CASCADE;
 
ALTER TABLE `gameplay`.`powerup_usage` DROP FOREIGN KEY `FK_powerup_usage_1` ;
ALTER TABLE `gameplay`.`powerup_usage` 
  ADD CONSTRAINT `FK_powerup_usage_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`registration_v2` DROP FOREIGN KEY `fk_registration_v2_1` ;
ALTER TABLE `gameplay`.`registration_v2` 
  ADD CONSTRAINT `fk_registration_v2_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`subscriber_answer` DROP FOREIGN KEY `fk_subscriber_answer_1` ;
ALTER TABLE `gameplay`.`subscriber_answer` 
  ADD CONSTRAINT `fk_subscriber_answer_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`subscriber_child_counted` DROP FOREIGN KEY `FK_subscriber_child_counted_1` , DROP FOREIGN KEY `FK_subscriber_child_counted_2` ;
ALTER TABLE `gameplay`.`subscriber_child_counted` 
  ADD CONSTRAINT `FK_subscriber_child_counted_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `FK_subscriber_child_counted_2`
  FOREIGN KEY (`child_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`subscriber_coupon_winner` DROP FOREIGN KEY `fk_subcoupon_1` ;
ALTER TABLE `gameplay`.`subscriber_coupon_winner` 
  ADD CONSTRAINT `fk_subcoupon_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`subscriber_team` DROP FOREIGN KEY `fk_sub_team_1` ;
ALTER TABLE `gameplay`.`subscriber_team` 
  ADD CONSTRAINT `fk_sub_team_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`vipbox` DROP FOREIGN KEY `fk_vipbox_1` , DROP FOREIGN KEY `fk_vipbox_4` , DROP FOREIGN KEY `fk_vipbox_5` ;
ALTER TABLE `gameplay`.`vipbox` 
  ADD CONSTRAINT `fk_vipbox_1`
  FOREIGN KEY (`owner_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_vipbox_4`
  FOREIGN KEY (`creator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_vipbox_5`
  FOREIGN KEY (`updator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
ALTER TABLE `gameplay`.`vipbox_member` DROP FOREIGN KEY `fk_vipbox_member_1` , DROP FOREIGN KEY `fk_vipbox_member_2` , DROP FOREIGN KEY `fk_vipbox_member_3` ;
ALTER TABLE `gameplay`.`vipbox_member` 
  ADD CONSTRAINT `fk_vipbox_member_1`
  FOREIGN KEY (`subscriber_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_vipbox_member_2`
  FOREIGN KEY (`creator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` ), 
  ADD CONSTRAINT `fk_vipbox_member_3`
  FOREIGN KEY (`updator_id` )
  REFERENCES `gameplay`.`s_subscriber` (`subscriber_id` );
 
drop table `gameplay`.`subscriber_device_session`;
drop table `gameplay`.`subscriber_device`;
drop table `gameplay`.`subscriber_ext`;

CREATE TABLE `gameplay`.`s_subscriber_email` (
  `subscriber_id` INT UNSIGNED NOT NULL,
  `email` VARCHAR(128) NOT NULL,
  `email_type` ENUM('PAYPAL') NOT NULL,
  `verified` INT(1) NOT NULL DEFAULT 0,
  `create_date` TIMESTAMP NOT NULL,
  `verified_date` TIMESTAMP NULL,
  PRIMARY KEY (`subscriber_id`, `email`));

################## NOT APPLIED TO PRODUCTION ##################

ALTER TABLE gameplay.s_subscriber   
  ADD COLUMN date_of_birth DATETIME NULL DEFAULT NULL AFTER ring4_subscriber_id,
  ADD COLUMN photo_url_small VARCHAR(1024) NULL DEFAULT NULL AFTER photo_url,
  ADD COLUMN photo_url_large VARCHAR(1024) NULL DEFAULT NULL AFTER photo_url_small,
  CHANGE COLUMN passwd passwd VARCHAR(511) NOT NULL;

##ALTER TABLE gameplay.s_subscriber   ## -- had erroneously set DOB to a TIMESTAMP, had to change it on dc1 and dc4
##  CHANGE COLUMN date_of_birth date_of_birth DATETIME NULL DEFAULT NULL

  
CREATE TABLE gameplay.game_interaction_event (	  
    game_interaction_event_id int(10) unsigned NOT NULL AUTO_INCREMENT,    
    context_id int(11) NOT NULL,   
    event_type_key varchar(128) NOT NULL,    
    subscriber_id int(10) NULL,    
    association_id int(10) NULL,   
    target_type enum('GAME','ROUND','QUESTION','PLAYER','ALLPLAYERS','SYSTEM') CHARACTER SET utf8mb4 NOT NULL,
    target_description varchar(255),    
    points_value int(10) unsigned NOT NULL DEFAULT 0 , 
    purchase_amount DECIMAL NULL,  
    award_amount DECIMAL NULL,
    received_payload text NULL,    
    delivered_payload text NULL,   
    is_notification int(1) NOT NULL DEFAULT 0 ,   
    is_persisted int(1) NOT NULL DEFAULT 0 , 
    is_derived int(1) NOT NULL DEFAULT 0 ,   
    is_badge int(1) NOT NULL DEFAULT 0 ,
    is_question_won int(1) NOT NULL DEFAULT 0 ,   
    is_question_lost int(1) NOT NULL DEFAULT 0 ,  
    is_round_won int(1) NOT NULL DEFAULT 0 , 
    is_round_lost int(1) NOT NULL DEFAULT 0 ,
    created_date datetime NOT NULL,
    PRIMARY KEY (game_interaction_event_id)  
);   

CREATE TABLE gameplay.game_badge (	    
    game_badge_id int(10) unsigned NOT NULL AUTO_INCREMENT, 
    context_id int(10) NOT NULL,   
    subscriber_id int(10) NULL,    
    association_id int(10) NULL,   
    event_type_key varchar(128) NOT NULL,    
    badge_key varchar(128) NOT NULL,    
    created_date datetime NOT NULL,
    PRIMARY KEY (game_badge_id),   
    KEY INDEX_subscriber_badge (subscriber_id, badge_key)   
);   

CREATE TABLE gameplay.game_badge_resource (	 
    badge_key varchar(128) NOT NULL,    
    badge_name varchar(128) NOT NULL,   
    badge_overlay_threshold int(10),    
    badge_description varchar(255),
    badge_photo_url varchar(511),
    badge_set_key varchar(32),
    PRIMARY KEY (badge_key)   
);   

INSERT gameplay.game_badge_resource VALUES ('user_account_updated',         'Account updated, thanks', 0, '', 'http://shout.tv/img/assets/badges/trophy_badge.png', '');
INSERT gameplay.game_badge_resource VALUES ('user_account_profile_picture', 'Photo updated, nice',     0, '', 'http://shout.tv/img/assets/badges/trophy_badge.png', '');
INSERT gameplay.game_badge_resource VALUES ('player_played_first_round',    'Warm Up',          0, '', 'http://shout.tv/img/assets/badges/trophy_badge.png', '');
INSERT gameplay.game_badge_resource VALUES ('player_won_first_round',       'First win',        0, '', 'http://shout.tv/img/assets/badges/first_win_badge.png', '');

INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_5',       'The Contestant',   5,   '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_10',      'The Novice',       10,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_15',      'The Graduate',     20,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_25',      'The Professional', 25,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_50',      'The Finisher',     50,  '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_100',     'The Veteran',      100, '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_200',     'The Marathoner',   200, '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');
INSERT gameplay.game_badge_resource VALUES ('player_rounds_played_500',     'The Iron Player ', 500, '', 'http://shout.tv/img/assets/badges/rounds_played_badge.png', 'rounds_played');

INSERT gameplay.game_badge_resource VALUES ('player_cash_level_attained',   'In the Money', 0,  '', 'http://shout.tv/img/assets/badges/coin_badge.png', 'money');
INSERT gameplay.game_badge_resource VALUES ('player_cash_awarded',          'You won {0}',  0,  '', 'http://shout.tv/img/assets/badges/dollar_badge.png', 'award');
INSERT gameplay.game_badge_resource VALUES ('correct_answer_streak_5',      'Smarty',       5,  '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('correct_answer_streak_10',     'Wizard',       10, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('correct_answer_streak_15',     'Brainy',       15, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('correct_answer_streak_20',     'Genius',       20, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');
INSERT gameplay.game_badge_resource VALUES ('correct_answer_streak_25',     'Prodigy',      25, '', 'http://shout.tv/img/assets/badges/streak_badge.png', 'streak');

ALTER TABLE gameplay.subscriber_xp_history CHANGE COLUMN xp_type xp_type
  enum('QUESTION','LB_TOP_THIRD','LB_TOP_ONE_PERCENT','ONE_ON_ONE_WINNER','DAILY_MILLIONAIRE') NOT NULL DEFAULT 'QUESTION';

