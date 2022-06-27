ALTER TABLE `gameplay`.`event` 
    DROP COLUMN `total_cash_prize_value` , 
    DROP COLUMN `amount` , 
    ADD COLUMN `num_grandprize_winners` INT NOT NULL DEFAULT 1  AFTER `highlighted` , 
    ADD COLUMN `num_network_winners` INT NOT NULL DEFAULT 1  AFTER `num_grandprize_winners` , 
    ADD COLUMN `num_players_min_payout` INT NOT NULL DEFAULT 0  AFTER `num_network_winners` , 
    ADD COLUMN `num_players_mid_payout` INT NOT NULL DEFAULT 0  AFTER `num_players_min_payout` , 
    ADD COLUMN `num_players_max_payout` INT NOT NULL DEFAULT 0  AFTER `num_players_mid_payout` , 
    ADD COLUMN `min_question_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `num_players_max_payout` , 
    ADD COLUMN `mid_question_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `min_question_payout_amt` , 
    ADD COLUMN `max_question_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `mid_question_payout_amt` , 
    ADD COLUMN `min_network_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `max_question_payout_amt` , 
    ADD COLUMN `mid_network_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `min_network_payout_amt` , 
    ADD COLUMN `max_network_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `mid_network_payout_amt` , 
    ADD COLUMN `min_grandprize_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `max_network_payout_amt` , 
    ADD COLUMN `mid_grandprize_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `min_grandprize_payout_amt` , 
    ADD COLUMN `max_grandprize_payout_amt` DECIMAL(9,2) NOT NULL DEFAULT 0.0  AFTER `mid_grandprize_payout_amt` ;
    


