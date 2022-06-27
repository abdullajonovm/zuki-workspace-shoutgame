drop table `gameplay`.`non_cash_prize`;

ALTER TABLE `gameplay`.`event` DROP COLUMN `max_grandprize_payout_amt` , DROP COLUMN `mid_grandprize_payout_amt` , DROP COLUMN `min_grandprize_payout_amt` , DROP COLUMN `max_network_payout_amt` , DROP COLUMN `mid_network_payout_amt` , DROP COLUMN `min_network_payout_amt` , DROP COLUMN `max_question_payout_amt` , DROP COLUMN `mid_question_payout_amt` , DROP COLUMN `min_question_payout_amt` , DROP COLUMN `num_players_max_payout` , DROP COLUMN `num_players_mid_payout` , DROP COLUMN `num_players_min_payout` , DROP COLUMN `num_network_winners` , DROP COLUMN `num_grandprize_winners` , ADD COLUMN `scoring_data` TEXT NOT NULL DEFAULT ''  AFTER `min_game_purse` ;

