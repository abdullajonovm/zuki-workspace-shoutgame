ALTER TABLE `gameplay`.`event` 
ADD COLUMN `sponsor` VARCHAR(45) NULL  AFTER `private_evt` , 
ADD COLUMN `coupon` VARCHAR(45) NULL  AFTER `sponsor` , 
ADD COLUMN `amount` DECIMAL(10,2) NULL DEFAULT 0 AFTER `coupon` ;
