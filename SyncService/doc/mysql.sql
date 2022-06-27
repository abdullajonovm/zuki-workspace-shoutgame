CREATE SCHEMA `sync` DEFAULT CHARACTER SET utf8mb4 ;

CREATE TABLE `sync`.`message` (
  `id` CHAR(36) NOT NULL,
  `subscriber_id` INT UNSIGNED NOT NULL,
  `message_type` VARCHAR(45) NOT NULL,
  `contextual_id` VARCHAR(45) NOT NULL,
  `engine_key` VARCHAR(45) NOT NULL,
  `payload` TEXT NOT NULL,
  `create_date` TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (`id`));