ALTER TABLE `gameplay`.`default_scoring` 
    DROP COLUMN `minimum_payout_amount` , 
    DROP COLUMN `allow_tie` , 
    DROP COLUMN `meinc_payout_socialnetworking` , 
    DROP COLUMN `meinc_payout_instantwinners` , 
    DROP COLUMN `meinc_payout_grandprize` , 
    DROP COLUMN `full_threshold_count` , 
    DROP COLUMN `social_network_bucket_amount` , 
    DROP COLUMN `instant_winners_bucket_amount` , 
    DROP COLUMN `grandprize_bucket_amount` , 
    DROP COLUMN `twothirds_threshold_count` , 
    DROP COLUMN `onethird_threshold_count` ;
