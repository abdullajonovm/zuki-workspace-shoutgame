INSERT INTO `gameplay`.`app` (`app_name`, `firewalled`, `iOS_bundle_id`, `android_bundle_id`, `windows_bundle_id`) VALUES ('TippingPoint', '1', 'tv.shout.tp', 'tv.shout.tp', 'tv.shout.tp');
select app_id into @APP_ID from gameplay.app where app_name = 'TippingPoint';
insert into gameplay.app_language(app_id, language_code, default_flag) values (@APP_ID, 'en', 1);
