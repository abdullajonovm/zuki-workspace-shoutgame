INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('f8ac519f-c590-478e-bed1-19a953486d1e', 'GENERAL_KNOWLEDGE');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('36087979-7ad4-4b7d-875e-86cc173dcf65', 'SCIENCE');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('c036632e-eaf9-43f5-b9a9-ef778bc5b225', 'SPORTS');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('d5733bf1-f8db-40af-9ab7-fa69dad71a86', 'BASEBALL');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('0b47f8f2-143c-4765-b904-2c4b8675c899', 'OLYMPICS');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('6baf8a3a-97a2-46be-afe3-17dcc8728319', 'GOLF');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('a6c007e7-444f-435c-8865-538b2adc8a9d', 'US_HISTORY');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('2e9f4804-560f-45de-ad39-a20c69920232', 'GEOGRAPHY');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('1a144f5f-e7f8-4086-ae01-fc0d45551c97', 'ASTRONOMY');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('f5b450f1-997c-42fd-8022-4a211c8e3f5e', 'ENTERTAINMENT');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('b504d206-7ebe-42e1-aa31-2925b4fe8e25', 'MOVIES');
INSERT INTO `shoutmillionaire`.`question_category_list` (`id`, `category_key`) VALUES ('21850e7d-158e-11e7-a82c-0242ac110004', 'COMICS');

INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('f8ac519f-c590-478e-bed1-19a953486d1e', 'categoryName', 'en', 'General Knowledge');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('36087979-7ad4-4b7d-875e-86cc173dcf65', 'categoryName', 'en', 'Science');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('c036632e-eaf9-43f5-b9a9-ef778bc5b225', 'categoryName', 'en', 'Sports');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('d5733bf1-f8db-40af-9ab7-fa69dad71a86', 'categoryName', 'en', 'Baseball');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('0b47f8f2-143c-4765-b904-2c4b8675c899', 'categoryName', 'en', 'Olympics');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('6baf8a3a-97a2-46be-afe3-17dcc8728319', 'categoryName', 'en', 'Golf');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('a6c007e7-444f-435c-8865-538b2adc8a9d', 'categoryName', 'en', 'U.S. History');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('2e9f4804-560f-45de-ad39-a20c69920232', 'categoryName', 'en', 'Geography');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('1a144f5f-e7f8-4086-ae01-fc0d45551c97', 'categoryName', 'en', 'Astronomy');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('f5b450f1-997c-42fd-8022-4a211c8e3f5e', 'categoryName', 'en', 'Entertainment');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('b504d206-7ebe-42e1-aa31-2925b4fe8e25', 'categoryName', 'en', 'Movies');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('21850e7d-158e-11e7-a82c-0242ac110004', 'categoryName', 'en', 'Comics');

#STORE INFO
SET @ENTITLEMENT_UUID = '608bc230-4551-11e7-a82c-0242ac110004';
insert into store.entitlement (uuid, `name`) values (@ENTITLEMENT_UUID, 'ShoutMillionaireCashPool');
SELECT MAX(entitlement_id) INTO @ENTITLEMENT_ID FROM store.entitlement;

SET @ONEDOLLAR_UUID = '4a4c29a7-4552-11e7-a82c-0242ac110004';
insert into store.item (store_bundle_id, uuid, title, description, price) values ('tv.shout.shoutmillionaire', @ONEDOLLAR_UUID, '$1', '$1', 1.00);
SELECT item_id INTO @ONEDOLLAR_ITEM_ID FROM store.item WHERE store_bundle_id = 'tv.shout.shoutmillionaire' and title = '$1' LIMIT 1;

SET @FIVEDOLLAR_UUID = '6737921d-4552-11e7-a82c-0242ac110004';
insert into store.item (store_bundle_id, uuid, title, description, price) values ('tv.shout.shoutmillionaire', @FIVEDOLLAR_UUID, '$5', '$5', 5.00);
SELECT item_id INTO @FIVEDOLLAR_ITEM_ID FROM store.item WHERE store_bundle_id = 'tv.shout.shoutmillionaire' and title = '$5' LIMIT 1;

SET @TENDOLLAR_UUID = '8d6b206f-4552-11e7-a82c-0242ac110004';
insert into store.item (store_bundle_id, uuid, title, description, price) values ('tv.shout.shoutmillionaire', @TENDOLLAR_UUID, '$10', '$10', 10.00);
SELECT item_id INTO @TENDOLLAR_ITEM_ID FROM store.item WHERE store_bundle_id = 'tv.shout.shoutmillionaire' and title = '$10' LIMIT 1;

insert into store.item_entitlement (item_id, entitlement_id, quantity) VALUES (@ONEDOLLAR_ITEM_ID, @ENTITLEMENT_ID, 1);
insert into store.item_entitlement (item_id, entitlement_id, quantity) VALUES (@FIVEDOLLAR_ITEM_ID, @ENTITLEMENT_ID, 5);
insert into store.item_entitlement (item_id, entitlement_id, quantity) VALUES (@TENDOLLAR_ITEM_ID, @ENTITLEMENT_ID, 10);

INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('835650ff-7e1e-11e7-970d-0242ac110004', 'systemMessage', 'en', 'Pool Play has begun for {0}');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('22dd185b-7e20-11e7-970d-0242ac110004', 'systemMessage', 'en', 'Tournament Play for {0} begins in {1} minutes. ${2} is on the line!');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('07362305-9e96-11e5-b784-86e93e99d7ba', 'systemMessage', 'en', 'Your verification code is: {0}');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('e1f9d489-7928-42ec-93c8-63fa53664885', 'systemMessage', 'en', 'Pool Play has begun');
INSERT INTO `shoutmillionaire`.`multi_localization` (`uuid`, `type`, `language_code`, `value`) VALUES ('cd620a64-2534-45d0-98f6-1159c4443049', 'systemMessage', 'en', 'Tournament Play begins soon');

INSERT INTO `notification`.`pref_types` (`id`, `name`, `description`, `possible_values`) VALUES ('13', 'SM_ROUND_START', 'Shout Millionaire - Round Start', 'SMS,EMAIL,APP_PUSH,NONE');
