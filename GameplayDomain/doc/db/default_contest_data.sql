-- Default question contest magic
INSERT INTO `gameplay`.`scoring_rule_type`
(`scoring_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (1, 'Question Score', 'Assigns points to a player solely based on answering a single question', null, 1, NOW());

INSERT INTO `gameplay`.`scoring_rule`
(`scoring_rule_id`, `scoring_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (1, 1, 'Question Score Relative Time Decay', 'QuestionScoreRelativeTimeDecay',
        'Question points are scored based on relative time that decays to a percent of total', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule_type`
(`payout_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (1, 'Question Winner Payout', 'Pays out cash/prize to question winners', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule`
(`payout_rule_id`, `payout_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (1, 1, 'Question Winner Payout', 'QuestionWinnerPayout',
        'Question winners are payed out via the campaign of the associated question', null, 1, NOW());



-- Contest template for question competition
INSERT INTO `gameplay`.`contest_template` (`contest_template_id`, `name`, `description`, `type`, `vipbox_type`,
                                           `marketing_html`, `rules_html`, `prizes_html`, `created_date`)
VALUES (1, 'Event Question Contest', 'Payout cash/prizes to first players who answer correctly', 'QUESTION', 'NONE',
        'Players win cash/prizes for answering question correctly first', 'Rules for answering a question first', 'Sponsors will award cash/prizes to winners', NOW());

INSERT INTO `gameplay`.`contest_template_scoring_rule` (`contest_template_scoring_rule_id`, `contest_template_id`,
                                                        `scoring_rule_id`, `config`, `created_date`)
VALUES (1, 1, 1, null, NOW());

INSERT INTO `gameplay`.`contest_template_payout_rule` (`contest_template_payout_rule_id`, `contest_template_id`,
                                                        `payout_rule_id`, `config`, `created_date`)
VALUES (1, 1, 1, null, NOW());

-- Default template for all questions if none provided.
INSERT INTO `gameplay`.`contest_template_default` (`contest_template_default_id`, `contest_template_id`, `type`,
                                                   `vipbox_type`, `primary_ref_id`, `vipbox_id`, `created_date`)
VALUES (1, 1, 'QUESTION', 'NONE', null, null, NOW());

-- Default event leaderboard contest magic
INSERT INTO `gameplay`.`scoring_rule_type`
(`scoring_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (2, 'Cumulative Event Score', 'Ranks players based on cumulative score over an event', null, 1, NOW());

INSERT INTO `gameplay`.`scoring_rule`
(`scoring_rule_id`, `scoring_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (2, 2, 'Descending Cumulative Event Score', 'DescendingCumulativeEventScore',
        'Players are ranked from highest to lowest cumulatively over an event', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule_type`
(`payout_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (2, 'Event Leaderboard Payout', 'Pays out cash/prize to event leaderboard winners', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule`
(`payout_rule_id`, `payout_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (2, 2, 'Event Leaderboard Winner Payout', 'EventLeaderboardWinnerPayout',
        'Event winners are payed out via the campaign of the associated question from best to worst', null, 1, NOW());

-- Contest template for event leaderboard
INSERT INTO `gameplay`.`contest_template` (`contest_template_id`, `name`, `description`, `type`, `vipbox_type`,
                                           `marketing_html`, `rules_html`, `prizes_html`, `created_date`)
VALUES (2, 'Event Leaderboard Contest', 'Payout cash/prizes to the top scoring players on the event leaderboard', 'EVENT', 'NONE',
        'Players win cash/prizes for top scoring players on the event leaderboard', 'Rules for participating in event leaderboard', 'Sponsors will award cash/prizes to top players on leaderboard.', NOW());

INSERT INTO `gameplay`.`contest_template_scoring_rule` (`contest_template_scoring_rule_id`, `contest_template_id`,
                                                        `scoring_rule_id`, `config`, `created_date`)
VALUES (2, 2, 2, null, NOW());

INSERT INTO `gameplay`.`contest_template_payout_rule` (`contest_template_payout_rule_id`, `contest_template_id`,
                                                        `payout_rule_id`, `config`, `created_date`)
VALUES (2, 2, 2, null, NOW());

-- Default template for all event leaderboards if none provided.
INSERT INTO `gameplay`.`contest_template_default` (`contest_template_default_id`, `contest_template_id`, `type`,
                                                   `vipbox_type`, `primary_ref_id`, `vipbox_id`, `created_date`)
VALUES (2, 2, 'EVENT', 'NONE', null, null, NOW());

INSERT INTO `gameplay`.`contest_default_config` (`contest_default_config_id`, `scope_type`, `config`,
                                                 `primary_ref_id`, `vipbox_id`, `created_date`)
VALUES (1, 'EVENT', '{"eventLeaderboardContest": "true", "questionContest": "true"}', null, null, NOW());

-- Default template for all event leaderboards if none provided.
INSERT INTO `gameplay`.`contest_template_default` (`contest_template_default_id`, `contest_template_id`, `type`,
                                                   `vipbox_type`, `primary_ref_id`, `vipbox_id`, `created_date`)
VALUES (2, 2, 'EVENT', 'NONE', null, null, NOW());

INSERT INTO `gameplay`.`contest_default_config` (`contest_default_config_id`, `scope_type`, `config`,
                                                 `primary_ref_id`, `vipbox_id`, `created_date`)
VALUES (1, 'EVENT', '{"eventLeaderboardContest": "true", "questionContest": "true"}', null, null, NOW());

-- Streak type

INSERT INTO `gameplay`.`scoring_rule_type`
(`scoring_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (3, 'Streak', 'Assigns points to a player based on how many were answered correctly in a row', null, 1, NOW());

INSERT INTO `gameplay`.`scoring_rule`
(`scoring_rule_id`, `scoring_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (3, 3, 'Streak 3', 'Streak3',
        'Players are ranked based on getting at least three questions correct in a row', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule_type`
(`payout_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (3, 'Leaderboard Streak', 'Pays out cash/prize to top streak winners', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule`
(`payout_rule_id`, `payout_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (3, 3, 'Streak3 Winner Payout', 'Streak3WinnerPayout',
        'Event winners are payed out via the campaign associated with the contest', null, 1, NOW());

-- Contest template for streak

INSERT INTO `gameplay`.`contest_template` (`contest_template_id`, `name`, `description`, `type`, `vipbox_type`,
                                           `marketing_html`, `rules_html`, `prizes_html`, `created_date`)
VALUES (3, 'Event VipBox Streak Contest', 'Payout cash/prizes to the top streak scoring players on the vipbox leaderboard', 'EVENT_VIPBOX', 'NONE',
        'Players win cash/prizes for top scoring players on the event leaderboard', 'Rules for participating in event leaderboard', 'Sponsors will award cash/prizes to top players on leaderboard.', NOW());

INSERT INTO `gameplay`.`contest_template_scoring_rule` (`contest_template_scoring_rule_id`, `contest_template_id`,
                                                        `scoring_rule_id`, `config`, `created_date`)
VALUES (2, 2, 2, null, NOW());

INSERT INTO `gameplay`.`contest_template_payout_rule` (`contest_template_payout_rule_id`, `contest_template_id`,
                                                        `payout_rule_id`, `config`, `created_date`)
VALUES (2, 2, 2, null, NOW());


-- Random Lucky dog type

INSERT INTO `gameplay`.`scoring_rule_type`
(`scoring_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (4, 'Random Lucky Dog', 'A single player is picked to win randomly at the end of the event.', null, 1, NOW());

INSERT INTO `gameplay`.`scoring_rule`
(`scoring_rule_id`, `scoring_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (4, 4, 'RandomLuckyDog1', 'Random Lucky Dog',
        'A single player is picked to win randomly at the end of the event.', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule_type`
(`payout_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (4, 'Random Lucky Dog', 'A single player is picked to win randomly at the end of the event.', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule`
(`payout_rule_id`, `payout_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (4, 4, 'Random Lucky Dog', 'RandomLuckyDog1',
        'A single player is picked to win randomly at the end of the event.', null, 1, NOW());

-- Contest template for lucky dog

INSERT INTO `gameplay`.`contest_template` (`contest_template_id`, `name`, `description`, `type`, `vipbox_type`,
                                           `marketing_html`, `rules_html`, `prizes_html`, `created_date`)
VALUES (4, 'Random Lucky Dog', 'Random players given prizes', 'EVENT', 'NONE',
        'Random players given prizes', 'Random players given prizes', '<!DOCTYPE HTML> <html> <head>  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;">  <script type="text/javascript">  function updateOrientation() { }   </script>  <style type="text/css"> body {     margin: 0;     background-color: black;     color: white;     margin-left: 10px;     margin-right: 10px; }  .content { 	background-color: black; 	border: 1px solid black; 	padding-top: 6px; }  .title h1 { 	display: block; 	text-align: center; 	margin: 0; 	padding: 0; }  .title .imgTitle { 	text-align: center; }  .title img { 	margin-bottom: 3px; }  .adContainer { 	border: 1px solid white; 	border-radius: 6px; 	margin-left: auto; 	margin-right: auto; 	background-color: white; }  .adContainer table { 	margin-left: auto; 	margin-right: auto; 	width: 100%; }  .adContainer table td { 	text-align: center; 	vertical-align: middle; }  .adContainer table td.first {text-align: left; padding-left: 10px;} .adContainer table td.third {text-align: right; padding-right: 10px}  .mainAdTagLine {text-align: center; color: black}  .sponsors { 	margin-left: auto; 	margin-right: auto; 	text-align: center; }  .sponsor { 	display: inline-block; 	text-align: center; }  .sponsor img { 	border-radius: 5px; }   .mainAdTagLine {font-weight: bold; text-align: center; color: white}  .deron {border-radius: 12px;border: 1px solid #404040} .deron {border-radius: 12px;border: 1px solid #404040}  .rules { 	margin-left: auto; 	margin-right: auto; 	padding-bottom: 10px; }  @media screen and (max-width:320px) {     body {font: 12px Helvetica, sans-serif;}     .content {height: 258px;}     .title {margin-top: 2px;}     .title h1 {font-size: 14px;} 	.title .imgTitle{height: 57px} 	.adContainer {margin-top: 6px; width: 256px;}     .adContainer table {height: 83px;}      .first {width: 75px}     .third {width: 75px}     .deron {width: 65px;height: 52px;}     .ipad {width: 107px;height: 84px;}     .jersey {width: 58px;height: 83px;}     .shoe {width: 63px; height: 44px;}  	.mainAdTagLine {font-size: 16px;} 	.sponsors {margin-top: 8px;}     .sponsor {width: 37px; height: 27px; margin-right: 8px;}     .sponsor img {width: 37px; height: 27px; }      .rules h1 {font-size: 14px;margin-left: 5px;} }  @media screen and (min-width:640px) {      body {font: 24px Helvetica, sans-serif;}      .content {height: 516px;}      .title {margin-top: 10px;}      .title h1 {font-size: 28px;}      .title .imgTitle{height: 114px}      .adContainer {margin-top: 12px; width: 512px;} 	 .adContainer table {height: 166px;}       .first {width: 138px}      .third {width: 138px}     .deron {width: 129px;height: 104px;}     .ipad {width: 107px;height: 84px;}     .jersey {width: 116px;height: 166px;}     .shoe {width: 126px; height: 88px;}  	.mainAdTagLine {font-size: 32px;} 	.sponsors {margin-top: 18px;}     .sponsor {width: 74px; height: 54px; margin-right: 16px;}     .sponsor img {width: 74px; height: 54px; }      .rules h1 {font-size: 28px;margin-left: 10px;} }  .sponsor:last-child {margin-right: 0px;}  </style>  </head> <body> <div class="content"> 	<div class="title"> 		<div class="mainAdTagLine">Some Lucky Dog will win...</div> 	</div> 	<div class="adContainer"> 		<table> 			<tr> 				<td style="text-align: center"><img class="ipad" src="http://shout.tv/img/eventphotos/vipbox/ipad.png" /></td> 			</tr> 		</table> 	</div>      <div class="rules" >     	<h1>Prizes</h1> 		<ul> 			<li>A grand prize will be awarded</li> 			<li>A number of sponsor prizes will be awarded</li> 		</ul>  		<h1>Rules</h1> 		<ul> 			<li>Players from the event will be randomly selected to win the prizes</li> 			<li>Players must answer every question during the event to be eligible to win</li> 		</ul> 	</div>  </div> </body> </html>', NOW());

INSERT INTO `gameplay`.`contest_template_scoring_rule` (`contest_template_scoring_rule_id`, `contest_template_id`,
                                                        `scoring_rule_id`, `config`, `created_date`)
VALUES (4, 4, 4, null, NOW());

INSERT INTO `gameplay`.`contest_template_payout_rule` (`contest_template_payout_rule_id`, `contest_template_id`,
                                                        `payout_rule_id`, `config`, `created_date`)
VALUES (4, 4, 4, null, NOW());

-- Perfect Game type

INSERT INTO `gameplay`.`scoring_rule_type`
(`scoring_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (5, 'Perfect Game', 'Whoever gets every question right in an event wins amazing prize.', null, 1, NOW());

INSERT INTO `gameplay`.`scoring_rule`
(`scoring_rule_id`, `scoring_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (5, 5, 'PerfectGame1', 'Perfect Game',
        'Whoever gets every question right in an event wins amazing prize.', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule_type`
(`payout_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (5, 'Perfect Game', 'Whoever gets every question right in an event wins amazing prize.', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule`
(`payout_rule_id`, `payout_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (5, 5, 'Perfect Game', 'PerfectGame1',
        'Whoever gets every question right in an event wins amazing prize.', null, 1, NOW());

-- Contest template for lucky dog

INSERT INTO `gameplay`.`contest_template` (`contest_template_id`, `name`, `description`, `type`, `vipbox_type`,
                                           `marketing_html`, `rules_html`, `prizes_html`, `created_date`)
VALUES (5, 'Perfect Game', 'Prize to player with perfect game', 'EVENT', 'NONE',
        'Prize to player with perfect game', 'Prize to player with perfect game', '<!DOCTYPE HTML> <html> <head>  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;">  <script type="text/javascript">  function updateOrientation() { }   </script>  <style type="text/css"> body {     margin: 0;     background-color: black;     color: white;     margin-left: 10px;     margin-right: 10px; }  .content { 	background-color: black; 	border: 1px solid black; 	padding-top: 6px; }  .title h1 { 	display: block; 	text-align: center; 	margin: 0; 	padding: 0; }  .title .imgTitle { 	text-align: center; }  .title img { 	margin-bottom: 3px; }  .adContainer { 	border: 1px solid white; 	border-radius: 6px; 	margin-left: auto; 	margin-right: auto; 	background-color: white; }  .adContainer table { 	margin-left: auto; 	margin-right: auto; 	width: 100%; }  .adContainer table td { 	text-align: center; 	vertical-align: middle; }  .adContainer table td.first {text-align: left; padding-left: 10px;} .adContainer table td.third {text-align: right; padding-right: 10px}  .mainAdTagLine {text-align: center; color: black}  .sponsors { 	margin-left: auto; 	margin-right: auto; 	text-align: center; }  .sponsor { 	display: inline-block; 	text-align: center; }  .sponsor img { 	border-radius: 5px; }   .mainAdTagLine {font-weight: bold; text-align: center; color: white}  .deron {border-radius: 12px;border: 1px solid #404040} .deron {border-radius: 12px;border: 1px solid #404040}  .rules { 	margin-left: auto; 	margin-right: auto; 	padding-bottom: 10px; }  @media screen and (max-width:320px) {     body {font: 12px Helvetica, sans-serif;}     .content {height: 258px;}     .title {margin-top: 2px;}     .title h1 {font-size: 14px;} 	.title .imgTitle{height: 57px} 	.adContainer {margin-top: 6px; width: 256px;}     .adContainer table {height: 83px;}      .first {width: 75px}     .third {width: 75px}     .deron {width: 65px;height: 52px;}     .car {height: 66px;}     .jersey {width: 58px;height: 83px;}     .shoe {width: 63px; height: 44px;}  	.mainAdTagLine {font-size: 16px;} 	.sponsors {margin-top: 8px;}     .sponsor {width: 37px; height: 27px; margin-right: 8px;}     .sponsor img {width: 37px; height: 27px; }      .rules h1 {font-size: 14px;margin-left: 5px;} }  @media screen and (min-width:640px) {      body {font: 24px Helvetica, sans-serif;}      .content {height: 516px;}      .title {margin-top: 10px;}      .title h1 {font-size: 28px;}      .title .imgTitle{height: 114px}      .adContainer {margin-top: 12px; width: 512px;} 	 .adContainer table {height: 166px;}       .first {width: 138px}      .third {width: 138px}     .deron {width: 129px;height: 104px;}     .car {height: 132px;}     .jersey {width: 116px;height: 166px;}     .shoe {width: 126px; height: 88px;}  	.mainAdTagLine {font-size: 32px;} 	.sponsors {margin-top: 18px;}     .sponsor {width: 74px; height: 54px; margin-right: 16px;}     .sponsor img {width: 74px; height: 54px; }      .rules h1 {font-size: 28px;margin-left: 10px;} }  .sponsor:last-child {margin-right: 0px;}  </style>  </head> <body> <div class="content"> 	<div class="title"> 		<div class="mainAdTagLine">Perfect Game Prize...</div> 	</div> 	<div class="adContainer"> 		<table> 			<tr> 				<td style="text-align: center"><img class="car" src="http://shout.tv/img/eventphotos/vipbox/perfect-game-car.png" /></td> 			</tr> 		</table> 	</div>      <div class="rules" >     	<h1>Prizes</h1> 		<ul> 			<li>A grand prize will be awarded</li> 			<li>A number of sponsor prizes will be awarded</li> 		</ul>  		<h1>Rules</h1> 		<ul> 			<li>Players from the event will be randomly selected to win the prizes</li> 			<li>Players must answer every question during the event to be eligible to win</li> 		</ul>   	</div>  </div> </body> </html>', NOW());

INSERT INTO `gameplay`.`contest_template_scoring_rule` (`contest_template_scoring_rule_id`, `contest_template_id`,
                                                        `scoring_rule_id`, `config`, `created_date`)
VALUES (5, 5, 5, null, NOW());

INSERT INTO `gameplay`.`contest_template_payout_rule` (`contest_template_payout_rule_id`, `contest_template_id`,
                                                        `payout_rule_id`, `config`, `created_date`)
VALUES (5, 5, 5, null, NOW());



-- Create Leaderboard contest
INSERT INTO `gameplay`.`contest` (name, description, type, vipbox_type, primary_ref_id, vipbox_id, status, start_date, end_date, marketing_html, rules_html, prizes_html, created_date, last_updated)
VALUES('Leaderboard Cash', 'Payout cash/prizes to the top scoring players on the event leaderboard', 'EVENT', 'NONE', <event_id>, null, 'NEW', null, null,
       'Players win cash/prizes for top scoring players on the event leaderboard', 'Rules for participating in event leaderboard',
       '<!DOCTYPE HTML><html><head><meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;"><script type="text/javascript">function updateOrientation(){}</script><style type="text/css">body{    margin: 0;    background-color: black;    color: white;    margin-left: 10px;    margin-right: 10px;}.content{	background-color: black;	border: 1px solid black;	padding-top: 6px;}.title h1{	display: block;	text-align: center;	margin: 0;	padding: 0;}.title .imgTitle{	text-align: center;}.title img{	margin-bottom: 3px;}.adContainer{	border: 1px solid white;	border-radius: 6px;	margin-left: auto;	margin-right: auto;	background-color: white;}.adContainer table{	margin-left: auto;	margin-right: auto;	width: 100%;}.adContainer table td{	text-align: center;	vertical-align: middle;}.adContainer table td.first {text-align: left; padding-left: 10px;}.adContainer table td.third {text-align: right; padding-right: 10px}.mainAdTagLine {text-align: center; color: black}.sponsors{	margin-left: auto;	margin-right: auto;	text-align: center;}.sponsor{	display: inline-block;	text-align: center;}.sponsor img{	border-radius: 5px;}.mainAdTagLine {font-weight: bold; text-align: center; color: white}.deron {border-radius: 12px;border: 1px solid #404040}.deron {border-radius: 12px;border: 1px solid #404040}.rules{	margin-left: auto;	margin-right: auto;	padding-bottom: 10px;}@media screen and (max-width:320px){    body {font: 12px Helvetica, sans-serif;}    .content {height: 258px;}    .title {margin-top: 2px;}    .title h1 {font-size: 14px;}	.title .imgTitle{height: 57px}	.adContainer {margin-top: 6px; width: 256px;}    .adContainer table {height: 83px;}    .first {width: 75px}    .third {width: 75px}    .deron {width: 65px;height: 52px;}    .car {height: 66px;}    .jersey {width: 58px;height: 83px;}    .shoe {width: 63px; height: 44px;}	.mainAdTagLine {font-size: 16px;}	.sponsors {margin-top: 8px;}    .sponsor {width: 37px; height: 27px; margin-right: 8px;}    .sponsor img {width: 37px; height: 27px; }    .rules h1 {font-size: 14px;margin-left: 5px;}}@media screen and (min-width:640px){     body {font: 24px Helvetica, sans-serif;}     .content {height: 516px;}     .title {margin-top: 10px;}     .title h1 {font-size: 28px;}     .title .imgTitle{height: 114px}     .adContainer {margin-top: 12px; width: 512px;}	 .adContainer table {height: 166px;}     .first {width: 138px}     .third {width: 138px}    .deron {width: 129px;height: 104px;}    .car {height: 132px;}    .jersey {width: 116px;height: 166px;}    .shoe {width: 126px; height: 88px;}	.mainAdTagLine {font-size: 32px;}	.sponsors {margin-top: 18px;}    .sponsor {width: 74px; height: 54px; margin-right: 16px;}    .sponsor img {width: 74px; height: 54px; }    .rules h1 {font-size: 28px;margin-left: 10px;}}.sponsor:last-child {margin-right: 0px;}</style></head><body><div class="content">	<div class="title">		<div class="mainAdTagLine">Winner Prizes...</div>	</div>	<div class="adContainer">		<table>			<tr>				<td style="text-align: center"><img class="car" src="http://shout.tv/img/eventphotos/vipbox/leaderboard-cash.png" /></td>			</tr>		</table>	</div>    <div class="rules" >    	<h1>Prizes</h1>		<ul>			<li>Players earn cash and prizes by finishing a certain rank on the Leaderboard.</li>			<li>The number of winners (ex: top 50) and the  cash or prizes may vary across Gameplays.  The cash and prizes awarded will vary depending on how many SHOUTers are playing, number of sponsors, and Gameplay event</li>			<li>Players will be alerted immediately following the event if they have won prizes for the "Leaderboard Cash" Contest for that event. </li>		</ul>		<h1>Rules</h1>		<ul>			<li>Players receive points based on how quickly they answer and if they get the question correct.  SHOUT questions are open (can be answered)  5-7 minutes  after a question is sent.</li>			<li>After this time spread a question is then closed so it can be scored. Depending on the question it will be scored at different times across the event.</li>			<li>After a question is scored the Leaderboard will update and show you how you stack up against the rest of the SHOUTers. Remember, the quicker you answer the more points you receive for the "Leaderboard Cash" Contest!</li>		</ul>	</div></div></body></html>',
       now(), now());

INSERT INTO `gameplay`.`contest_scoring_rule`
(`contest_id`, `scoring_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 2, null, now(), now());

INSERT INTO `gameplay`.`payout_scoring_rule`
(`contest_id`, `payout_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 2, null, now(), now());


-- Create the dwill vipbox streak contest
INSERT INTO `gameplay`.`contest` (name, description, type, vipbox_type, primary_ref_id, vipbox_id, status, start_date, end_date, marketing_html, rules_html, prizes_html, created_date, last_updated)
VALUES('DWill VIP Box', 'Win by getting more than one quesiton correct in a row', 'EVENT', 'VIPBOX', <event_id>, <vipbox_id>, 'NEW', null, null, 'Win gear by getting more than question right in a row in Deron\'s VIPBOX',
'<!DOCTYPE HTML> <html> <head>  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;">  <script type="text/javascript">  function updateOrientation() { }   </script>  <style type="text/css"> body {     margin: 0;     background-color: black;     color: white;     margin-left: 10px;     margin-right: 10px; }  .content { 	background-color: black; 	border: 1px solid black; 	padding-top: 6px; }  .title h1 { 	display: block; 	text-align: center; 	margin: 0; 	padding: 0; }  .title .imgTitle { 	text-align: center; }  .title img { 	margin-bottom: 3px; }  .adContainer { 	border: 1px solid white; 	border-radius: 6px; 	margin-left: auto; 	margin-right: auto; 	background-color: white; }  .adContainer table { 	margin-left: auto; 	margin-right: auto; 	width: 100%; }  .adContainer table td { 	text-align: center; 	vertical-align: middle; }  .adContainer table td.first {text-align: left; padding-left: 10px;} .adContainer table td.third {text-align: right; padding-right: 10px}  .mainAdTagLine {text-align: center; color: black}  .sponsors { 	margin-left: auto; 	margin-right: auto; 	text-align: center; }  .sponsor { 	display: inline-block; 	text-align: center; }  .sponsor img { 	border-radius: 5px; }   .mainAdTagLine {font-weight: bold; text-align: center; color: white}  .deron {border-radius: 12px;border: 1px solid #404040} .deron {border-radius: 12px;border: 1px solid #404040}  .rules { 	margin-left: auto; 	margin-right: auto; 	padding-bottom: 10px; }  @media screen and (max-width:320px) {     body {font: 12px Helvetica, sans-serif;}     .content {height: 258px;}     .title {margin-top: 2px;}     .title h1 {font-size: 14px;} 	.title .imgTitle{height: 57px} 	.adContainer {margin-top: 6px; width: 256px;}     .adContainer table {height: 83px;}      .first {width: 75px}     .third {width: 75px}     .deron {width: 65px;height: 52px;}     .ipad {width: 53px;height: 42px;}     .jersey {width: 58px;height: 83px;}     .shoe {width: 63px; height: 44px;}  	.mainAdTagLine {font-size: 16px;} 	.sponsors {margin-top: 8px;}     .sponsor {width: 37px; height: 27px; margin-right: 8px;}     .sponsor img {width: 37px; height: 27px; }      .rules h1 {font-size: 14px;margin-left: 5px;} }  @media screen and (min-width:640px) {      body {font: 24px Helvetica, sans-serif;}      .content {height: 516px;}      .title {margin-top: 10px;}      .title h1 {font-size: 28px;}      .title .imgTitle{height: 114px}      .adContainer {margin-top: 12px; width: 512px;} 	 .adContainer table {height: 166px;}       .first {width: 138px}      .third {width: 138px}     .deron {width: 129px;height: 104px;}     .ipad {width: 107px;height: 84px;}     .jersey {width: 116px;height: 166px;}     .shoe {width: 126px; height: 88px;}  	.mainAdTagLine {font-size: 32px;} 	.sponsors {margin-top: 18px;}     .sponsor {width: 74px; height: 54px; margin-right: 16px;}     .sponsor img {width: 74px; height: 54px; }      .rules h1 {font-size: 28px;margin-left: 10px;} }  .sponsor:last-child {margin-right: 0px;}  </style>  </head> <body> <div class="content"> 	<div class="title"> 		<div class="imgTitle"><img class="deron" src="http://shout.tv/img/eventphotos/vipbox/deron-profile.jpg" /></div> 		<div class="mainAdTagLine">Join Deron\'s VIP Box to compete for these prizes... </div> 	</div> 	<div class="adContainer"> 		<table> 			<tr> 				<td class="first"><img class="ipad" src="http://shout.tv/img/eventphotos/vipbox/ipad.png" /></td> 				<td class="second"><img class="jersey" src="http://shout.tv/img/eventphotos/vipbox/deronjersey640.png" /></td> 				<td class="third"><img class="shoe" src="http://shout.tv/img/eventphotos/vipbox/deronshoe.png" /></td> 			</tr> 		</table> 	</div>      <div class="rules" >     	<h1>Prizes</h1> 		<ul> 			<li>Players earn prizes by finishing a certain rank in the VIP Streak Leaderboard</li> 			<li>The prizes awarded will vary depending on how many SHOUTers are playing, number of sponsors, and Gameplay event</li> 			<li>Players will be alerted immediately following the event if they have won prizes inside of Deron\'s VIP Streak Contest</li> 		</ul>  		<h1>Rules</h1> 		<ul> 			<li>A  player must join Deron Williams VIP Box in order to compete in the VIP Streak Contest</li> 			<li>To start a streak a player must correctly answer 2 questions in a row to receive bonus streak points</li> 			<li>A wrong or an unanswered question will result in a player\'s streak turning to 0</li> 		</ul>   	</div>  </div> </body> </html>', now(), now());

insert into gameplay.contest_campaign (`contest_id`, `campaign_id`, `created_date`, `last_updated`) values(<contest_id>, 1160, now(), now());

INSERT INTO `gameplay`.`contest_payout_rule`
(`contest_id`, `payout_rule_id`, `created_date`, `last_updated`)
VALUES (<contest_id>, 3, now(), now());

INSERT INTO `gameplay`.`contest_scoring_rule`
(`contest_id`, `scoring_rule_id`, `created_date`, `last_updated`)
VALUES (<contest_id>, 3, now(), now());

insert into vipbox_event values(<vipbox_id>, <event_id>, '<!DOCTYPE HTML> <html> <head>  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;">  <style type="text/css"> body {     margin: 0;     background-color: black;     color: white; }  .content { }  .adContainer { 	border: 1px solid white; 	border-radius: 6px; 	margin-left: auto; 	margin-right: auto; 	background-color: white; }  .adContainer table { 	margin-left: auto; 	margin-right: auto; 	width: 100%; }  .adContainer table td { 	text-align: center; 	vertical-align: middle; }  .adContainer table td.first {text-align: left; padding-left: 10px;} .adContainer table td.third {text-align: right; padding-right: 10px}  .mainAdTagLine {text-align: center; color: black}  .mainAdTagLine {font-weight: bold;color: white;}  @media screen and (max-width:320px) {     body {font: 12px Helvetica, sans-serif;} 	.adContainer {margin-top: 3px; width: 246px;}     .adContainer table {height: 83px;}      .first {width: 75px}     .third {width: 75px}     .ipad {width: 53px;height: 42px;}     .jersey {height: 73px;}     .shoe {width: 63px; height: 44px;}  	.mainAdTagLine {font-size: 16px;} }  @media screen and (min-width:640px) {      body {font: 24px Helvetica, sans-serif;}      .adContainer {margin-top: 6px; width: 492px;} 	 .adContainer table {height: 166px;}       .first {width: 138px}      .third {width: 138px}     .ipad {width: 107px;height: 84px;}     .jersey {height: 146px;}     .shoe {width: 126px; height: 88px;}  	.mainAdTagLine {font-size: 32px;} }  </style>  </head> <body> <div class="content">     <div class="mainAdTagLine">You are playing for these prizes!</div> 	<div class="adContainer"> 		<table> 			<tr> 				<td class="first"><img class="ipad" src="http://shout.tv/img/eventphotos/vipbox/ipad.png" /></td> 				<td class="second"><img class="jersey" src="http://shout.tv/img/eventphotos/vipbox/deronjersey640.png" /></td> 				<td class="third"><img class="shoe" src="http://shout.tv/img/eventphotos/vipbox/deronshoe.png" /></td> 			</tr> 		</table> 	</div> <div>  </div>  <div class="ad"> </div> </div> </body> </html>');

-- Create Random contest
INSERT INTO `gameplay`.`contest` (name, description, type, vipbox_type, primary_ref_id, vipbox_id, status, start_date, end_date, marketing_html, rules_html, prizes_html, created_date, last_updated)
VALUES('Random Lucky Dog', 'Random players given prizes', 'EVENT', 'NONE', <event_id>, null, 'NEW', null, null,
       'Random players given prizes', 'Random players given prizes',
       '<!DOCTYPE HTML> <html> <head>  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;">  <script type="text/javascript">  function updateOrientation() { }   </script>  <style type="text/css"> body {     margin: 0;     background-color: black;     color: white;     margin-left: 10px;     margin-right: 10px; }  .content { 	background-color: black; 	border: 1px solid black; 	padding-top: 6px; }  .title h1 { 	display: block; 	text-align: center; 	margin: 0; 	padding: 0; }  .title .imgTitle { 	text-align: center; }  .title img { 	margin-bottom: 3px; }  .adContainer { 	border: 1px solid white; 	border-radius: 6px; 	margin-left: auto; 	margin-right: auto; 	background-color: white; }  .adContainer table { 	margin-left: auto; 	margin-right: auto; 	width: 100%; }  .adContainer table td { 	text-align: center; 	vertical-align: middle; }  .adContainer table td.first {text-align: left; padding-left: 10px;} .adContainer table td.third {text-align: right; padding-right: 10px}  .mainAdTagLine {text-align: center; color: black}  .sponsors { 	margin-left: auto; 	margin-right: auto; 	text-align: center; }  .sponsor { 	display: inline-block; 	text-align: center; }  .sponsor img { 	border-radius: 5px; }   .mainAdTagLine {font-weight: bold; text-align: center; color: white}  .deron {border-radius: 12px;border: 1px solid #404040} .deron {border-radius: 12px;border: 1px solid #404040}  .rules { 	margin-left: auto; 	margin-right: auto; 	padding-bottom: 10px; }  @media screen and (max-width:320px) {     body {font: 12px Helvetica, sans-serif;}     .content {height: 258px;}     .title {margin-top: 2px;}     .title h1 {font-size: 14px;} 	.title .imgTitle{height: 57px} 	.adContainer {margin-top: 6px; width: 256px;}     .adContainer table {height: 83px;}      .first {width: 75px}     .third {width: 75px}     .deron {width: 65px;height: 52px;}     .ipad {width: 107px;height: 84px;}     .jersey {width: 58px;height: 83px;}     .shoe {width: 63px; height: 44px;}  	.mainAdTagLine {font-size: 16px;} 	.sponsors {margin-top: 8px;}     .sponsor {width: 37px; height: 27px; margin-right: 8px;}     .sponsor img {width: 37px; height: 27px; }      .rules h1 {font-size: 14px;margin-left: 5px;} }  @media screen and (min-width:640px) {      body {font: 24px Helvetica, sans-serif;}      .content {height: 516px;}      .title {margin-top: 10px;}      .title h1 {font-size: 28px;}      .title .imgTitle{height: 114px}      .adContainer {margin-top: 12px; width: 512px;} 	 .adContainer table {height: 166px;}       .first {width: 138px}      .third {width: 138px}     .deron {width: 129px;height: 104px;}     .ipad {width: 107px;height: 84px;}     .jersey {width: 116px;height: 166px;}     .shoe {width: 126px; height: 88px;}  	.mainAdTagLine {font-size: 32px;} 	.sponsors {margin-top: 18px;}     .sponsor {width: 74px; height: 54px; margin-right: 16px;}     .sponsor img {width: 74px; height: 54px; }      .rules h1 {font-size: 28px;margin-left: 10px;} }  .sponsor:last-child {margin-right: 0px;}  </style>  </head> <body> <div class="content"> 	<div class="title"> 		<div class="mainAdTagLine">Some Lucky Dog will win...</div> 	</div> 	<div class="adContainer"> 		<table> 			<tr> 				<td style="text-align: center"><img class="ipad" src="http://shout.tv/img/eventphotos/vipbox/ipad.png" /></td> 			</tr> 		</table> 	</div>      <div class="rules" >     	<h1>Prizes</h1> 		<ul> 			<li>A grand prize will be awarded</li> 			<li>A number of sponsor prizes will be awarded</li> 		</ul>  		<h1>Rules</h1> 		<ul> 			<li>Players from the event will be randomly selected to win the prizes</li> 			<li>Players must answer every question during the event to be eligible to win</li> 		</ul> 	</div>  </div> </body> </html>'
       now(), now());

INSERT INTO `gameplay`.`contest_scoring_rule`
(`contest_id`, `scoring_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 4, null, now(), now());

INSERT INTO `gameplay`.`payout_scoring_rule`
(`contest_id`, `payout_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 4, null, now(), now());

insert into gameplay.contest_campaign (`contest_id`, `campaign_id`, `created_date`, `last_updated`) values(<contest_id>, 56, now(), now());

-- Perfect game

INSERT INTO `gameplay`.`contest` (name, description, type, vipbox_type, primary_ref_id, vipbox_id, status, start_date, end_date, marketing_html, rules_html, prizes_html, created_date, last_updated)
VALUES('Perfect Game', 'Prize to player with perfect game', 'EVENT', 'NONE', <event_id>, null, 'NEW', null, null,
       'Prize to player with perfect game', 'Prize to player with perfect game',
       '<!DOCTYPE HTML> <html> <head>  <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0;">  <script type="text/javascript">  function updateOrientation() { }   </script>  <style type="text/css"> body {     margin: 0;     background-color: black;     color: white;     margin-left: 10px;     margin-right: 10px; }  .content { 	background-color: black; 	border: 1px solid black; 	padding-top: 6px; }  .title h1 { 	display: block; 	text-align: center; 	margin: 0; 	padding: 0; }  .title .imgTitle { 	text-align: center; }  .title img { 	margin-bottom: 3px; }  .adContainer { 	border: 1px solid white; 	border-radius: 6px; 	margin-left: auto; 	margin-right: auto; 	background-color: white; }  .adContainer table { 	margin-left: auto; 	margin-right: auto; 	width: 100%; }  .adContainer table td { 	text-align: center; 	vertical-align: middle; }  .adContainer table td.first {text-align: left; padding-left: 10px;} .adContainer table td.third {text-align: right; padding-right: 10px}  .mainAdTagLine {text-align: center; color: black}  .sponsors { 	margin-left: auto; 	margin-right: auto; 	text-align: center; }  .sponsor { 	display: inline-block; 	text-align: center; }  .sponsor img { 	border-radius: 5px; }   .mainAdTagLine {font-weight: bold; text-align: center; color: white}  .deron {border-radius: 12px;border: 1px solid #404040} .deron {border-radius: 12px;border: 1px solid #404040}  .rules { 	margin-left: auto; 	margin-right: auto; 	padding-bottom: 10px; }  @media screen and (max-width:320px) {     body {font: 12px Helvetica, sans-serif;}     .content {height: 258px;}     .title {margin-top: 2px;}     .title h1 {font-size: 14px;} 	.title .imgTitle{height: 57px} 	.adContainer {margin-top: 6px; width: 256px;}     .adContainer table {height: 83px;}      .first {width: 75px}     .third {width: 75px}     .deron {width: 65px;height: 52px;}     .car {height: 66px;}     .jersey {width: 58px;height: 83px;}     .shoe {width: 63px; height: 44px;}  	.mainAdTagLine {font-size: 16px;} 	.sponsors {margin-top: 8px;}     .sponsor {width: 37px; height: 27px; margin-right: 8px;}     .sponsor img {width: 37px; height: 27px; }      .rules h1 {font-size: 14px;margin-left: 5px;} }  @media screen and (min-width:640px) {      body {font: 24px Helvetica, sans-serif;}      .content {height: 516px;}      .title {margin-top: 10px;}      .title h1 {font-size: 28px;}      .title .imgTitle{height: 114px}      .adContainer {margin-top: 12px; width: 512px;} 	 .adContainer table {height: 166px;}       .first {width: 138px}      .third {width: 138px}     .deron {width: 129px;height: 104px;}     .car {height: 132px;}     .jersey {width: 116px;height: 166px;}     .shoe {width: 126px; height: 88px;}  	.mainAdTagLine {font-size: 32px;} 	.sponsors {margin-top: 18px;}     .sponsor {width: 74px; height: 54px; margin-right: 16px;}     .sponsor img {width: 74px; height: 54px; }      .rules h1 {font-size: 28px;margin-left: 10px;} }  .sponsor:last-child {margin-right: 0px;}  </style>  </head> <body> <div class="content"> 	<div class="title"> 		<div class="mainAdTagLine">Perfect Game Prize...</div> 	</div> 	<div class="adContainer"> 		<table> 			<tr> 				<td style="text-align: center"><img class="car" src="http://shout.tv/img/eventphotos/vipbox/perfect-game-car.png" /></td> 			</tr> 		</table> 	</div>      <div class="rules" >     	<h1>Prizes</h1> 		<ul> 			<li>A grand prize will be awarded</li> 			<li>Prizes vary from time to time</li> 		</ul>  		<h1>Rules</h1> 		<ul> 		    <li>Players must answer every question during the event to be eligible to win</li> 			<li>Players must have recieved a perfect score on all questions of an event to qualify</li> 		</ul>   	</div>  </div> </body> </html>'
       now(), now());

INSERT INTO `gameplay`.`contest_scoring_rule`
(`contest_id`, `scoring_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 5, null, now(), now());

INSERT INTO `gameplay`.`payout_scoring_rule`
(`contest_id`, `payout_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 5, null, now(), now());

insert into gameplay.contest_campaign (`contest_id`, `campaign_id`, `created_date`, `last_updated`) values(<contest_id>, 56, now(), now());

-- DWill Random VIP Box

INSERT INTO `gameplay`.`scoring_rule_type`
(`scoring_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (6, 'DWill VIP Box', 'Pick a random player to win Deron\'s gear', null, 1, NOW());

INSERT INTO `gameplay`.`scoring_rule`
(`scoring_rule_id`, `scoring_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (6, 6, 'DWill VIP Box', 'DWillRandomGiveAway',
        'Pick a random player to win Deron\'s gear', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule_type`
(`payout_rule_type_id`, `name`, `description`, `config_json_schema`, `active`,`created_date`)
VALUES (6, 'DWill VIP Box', 'Pick a random player to win Deron\'s gear', null, 1, NOW());

INSERT INTO `gameplay`.`payout_rule`
(`payout_rule_id`, `payout_rule_type_id`, `name`, `algorithm_name`, `description`, `config_json_schema`, `active`, `created_date`)
VALUES (6, 6, 'DWill VIP Box', 'DWillRandomGiveAway',
        'Pick a random player to win Deron\'s gear', null, 1, NOW());

INSERT INTO `gameplay`.`contest` (name, description, type, vipbox_type, primary_ref_id, vipbox_id, status, start_date, end_date, marketing_html, rules_html, prizes_html, created_date, last_updated)
VALUES('DWill VIP Box', 'DWill VIP Box', 'EVENT', 'NONE', <event_id>, null, 'NEW', null, null,
       'Prize to player with perfect game', 'Prize to player with perfect game',
       'Win My Gear',
       now(), now());

INSERT INTO `gameplay`.`contest_scoring_rule`
(`contest_id`, `scoring_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 6, null, now(), now());

INSERT INTO `gameplay`.`contest_payout_rule`
(`contest_id`, `payout_rule_id`, `config`, `created_date`,`last_updated`)
VALUES (<contest_id>, 6, null, now(), now());

insert into gameplay.contest_campaign (`contest_id`, `campaign_id`, `created_date`, `last_updated`) values(<contest_id>, 56, now(), now());