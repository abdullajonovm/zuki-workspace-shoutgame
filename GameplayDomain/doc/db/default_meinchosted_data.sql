#only execute this one time.  once these subscribers exist, no longer run this unles meinc_hosted gets reset.
#also, this assumes thie ID's are 2,3,4,5,6
INSERT INTO `meinc_hosted`.`subscriber` (`firstname`, `lastname`, `title`, `username`, `passwd`, `email`, `change_password`, `public_flag`, `active_flag`, `eula_flag`, `country`, `create_date`) VALUES ('Groove', '', NULL, 'groove', 'xxx', 'NOEMAIL@NOEMAIL.COM', '0', '1', '1', '1', '840', '2010-12-07');
INSERT INTO `meinc_hosted`.`subscriber` (`firstname`, `lastname`, `username`, `passwd`, `email`, `change_password`, `public_flag`, `active_flag`, `eula_flag`, `country`, `create_date`) VALUES ('Boler', ' ', 'boler', 'xxx', 'NOEMAIL@NOEMAIL.COM', '0', '1', '1', '1', '840', '2010-12-07');
INSERT INTO `meinc_hosted`.`subscriber` (`firstname`, `lastname`, `username`, `passwd`, `email`, `change_password`, `public_flag`, `active_flag`, `eula_flag`, `country`, `create_date`) VALUES ('Lockedon', ' ', 'lockedon', 'xxx', 'NOEMAIL@NOEMAIL.COM', '0', '1', '1', '1', '840', '2010-12-07');
INSERT INTO `meinc_hosted`.`subscriber` (`firstname`, `lastname`, `username`, `passwd`, `email`, `change_password`, `public_flag`, `active_flag`, `eula_flag`, `country`, `create_date`) VALUES ('Harpring', ' ', 'harpring', 'xxx', 'NOEMAIL@NOEMAIL.COM', '0', '1', '1', '1', '840', '2010-12-07');
INSERT INTO `meinc_hosted`.`subscriber` (`firstname`, `lastname`, `username`, `passwd`, `email`, `change_password`, `public_flag`, `active_flag`, `eula_flag`, `country`, `create_date`) VALUES ('Tanner', ' ', 'tanner', 'xxx', 'NOEMAIL@NOEMAIL.COM', '0', '1', '1', '1', '840', '2010-12-07');

INSERT INTO `meinc_hosted`.`account` (`account_id`, `type_id`, `status_id`, `name`, `subdomain`, `postal_code`, `country`) VALUES ('3', '3', '3', 'Gameplay', 'gameplay', '11111', '840');
INSERT INTO `meinc_hosted`.`account_reference` (`account_id`, `reference_id`) VALUES ('3', '1');