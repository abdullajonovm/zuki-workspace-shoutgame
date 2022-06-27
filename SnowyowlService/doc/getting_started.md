# Servers you'll need to care about for testing
* Everything is on DC4 right now
 * webdatastore: http://dc4-wds1.shoutgameplay.com
 * collector1: ssh meinc@dc4-collector1.shoutgameplay.com
 * SRD: http://dc4-static.shoutgameplay.com/srd2.json

# Get the project up and running
* make sure everything builds
 * cd &lt;workspace&gt;
 * bzr co bzr+ssh://scm.shoutgameplay.com/opt/bzr/shout/game/MeincOverlord/
 * cd &lt;workspace&gt;/MeincOverlord/bzr
 * ./checkout.sh
 * cd ..
 * ./bootstrap.sh
 * mvn clean install

# If you need to make a code change to one of the projects for some reason
* make the change, use maven to build (mvn clean install), and then use this command to deploy the project[s]:
 * cd &lt;workspace&gt;/MeincOverlord/fabric/
 * fab install_services:start=1,name="projname1;projname2;..." -Rdc4-collector -P

# Using the Admin tool
* Build the project test classes
 * Easiest way is from eclipse. Open up the ShoutMillionaireService project, go to the menu and select Clean.
 * alternatively, you can build it from the command line (if you want that route, i'll leave it as an exercise for you to figure out since i don't have those steps)
* cd &lt;workspace&gt;/ShoutMillionaireService
* You'll need to edit the tv.shout.sm.db.DbProvider.java file in the test package. It has a variable up top which is the location of a properties file: DB_PROP_FILE. Create your own properties file, and point this variable to that location.
* In the properties file you just created, you'll need to add these two entries:
 * dc4.db.username=[the root username. you can get this from the meinc.properties file on dc4]
 * dc4.db.password=[the root password. you can also get this from the meinc.properties file on dc4]
* mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass="tv.shout.sm.db.Admin" -Dexec.args=""
* the first thing it will do is prompt you which user you wish to use. it will be a comma-delimited list of emails (right now there's only one). type in the email you wish to use, and all calls will be made as that user. (directions are below on how to add more users).
* from here you can just follow the prompts
 * 0) list games - lists all of the currently PENDING and OPEN games. It will also give you the title of the most recently created game - i use this to sequentially name them as i'm testing, such as "Scott Test 1", "Scott Test 2", etc.
 * 1) open game - move a game from the PENDING to the OPEN state. This will publish it to the webdatastore (http://&lt;webdatastore&gt;/dm_games.json AND http://&lt;webdatastore&gt;/&lt;gameId&gt;/game.json). Once a game is OPEN, players can join the game and set themselves as available for pairing (more on this later)
 * 2) list rounds - list each of the rounds for a game. If you use the tool to create a ShoutMillionaire game (which you absolutely should), all rounds are initially in the PENDING state. After a game is OPEN, the first round is placed into the OPEN state (meaning it can accept players to join). All other rounds are moved to the VISIBLE state so they show up in the WDS docs mentioned above (also the wds docs are republished)
 * 3) start pool play - this will move the round from OPEN or FULL (if it can't accept any more players) to READY. Once in the READY state, the pairing engine will pick it up and being pairing.
 * 4) cancel game - if gameplay fails along the way and doesn't gracefully close due to a bug, you can use this to move it to the CANCELLED state. This wil cause the engines to stop processing it, and will also move all Rounds/Matches/Answers, etc.. to the CANCELLED state as well.
 * 5) clone game - if you've previously created a game that you like (for testing), you can use this to create a clone of that game. It will ask for a new name and start date, but will otherwise be identicaly to the game it was cloned from (except there are no pairings or matches for it yet). You can them move it through the state engine normally.
 * 6) create game - if you want to manually create a game from scratch, this is a wizard style interface that will ask you questions as necessary and create a game that is made to work with the ShoutMillionaire engines.
 * anything else - will exit the admin tool

# Using the ApiTest tool
* Build the project test classes (as mentioned above. if you've already done it for the admin tool, you don't need to do it again)
* cd &lt;workspace&gt;/ShoutMillionaireService
* mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass="tv.shout.sm.test.ApiTest" -Dexec.args=""
* unlike the admin tool, once you complete an action with the ApiTest, it exits and you need to run it again to do something else.
* 1) unauthenticated call...
 * 1) getQuestionCategoriesFromCategoryKeys - mostly done as a test while developing to make sure things worked, but the clients might eventually use it to get a list of categories to present to the user for filtering what subjects they are interested in
* 2) authenticated call...
 * it will then ask which user you wish to be authenticated as. right now i only have shawker@me-inc.com (subscriber id 8) hardcoded. you can add yourself easy enough. Just edit the Users interface (in the test package: tv.shout.sm.admin.Users.java).
 * I will go through these options backwards to save the best for last... :)
 * 3) player... - has various calls that return information that a client might need to know about the player (how many creds they have, what games they've played, are currently playing, etc.)
 * 2) store... - has the methods necessary to view and purchase creds (which you will need if you want to actually play a test game)
 * 1) game... - all the game related methods for testing
  * 1) joinGame - using the id of a game gained from the admin tool, join that game. note that the game must be OPEN before you can join
  * 2) leaveGame - if for some reason you decide you don't want to be part of the game, this will unjoin you
  * 3) setAvailability - this kicks things off. enter a gameId and then a true or false value. false means you don't want to be paired, true means you're ready to be paired once the first round starts. this will also start a game loop in the ApiTest that will begin polling the sync messages document waiting for mesages so that you can play a game. More on this below.

# Playing a game using the tools
 * you'll want 3 terminal windows open.
  * in one, ssh into the collector server and tail the logs (/opt/meinc/mrsoa/logs/meinc_debug.log) - this will let you see if an exception happens
  * in another, run the Admin app
  * in the third, run the ApiTest app
 * Using the admin tool, either create or clone a game
 * Using the admin tool, open that game
 * Using the ApiTest, purchase some creds
 * Using the ApiTest, join the game
 * Using the ApiTest, setAvailability to true for that game
 * Using the admin tool, list the rounds for the game and then start pool play for the first round
 * In the ApiTest console window, you will see a bunch of messages going by. eventually you should get a user_matched, and then a question.
 * answer the questions (correct answers have an asterisk), and eventually the game will end (if all goes well)

# Under the hood - what's going on and what you'll need to do in your own client
 * Most of what you'll need can be gleaned from examining the code in the ApiTest class (and associated helper classes that it uses). But basically it goes like this:
  * There is a global WDS document that lists all of the OPEN and INPLAY games (http://&lt;wds&gt;/dm_games.json).
  * Using that list, you can pull up details on any specific game using the gameId with the following WDS url: http://&lt;wds&gt;/&lt;gameId&gt;/game.json
  * The store API's in the ApiTest show how to list and purchase creds. You'll want to grab an auth token, make the call to get the items, and then call the purchase method with the chosen item. Right now it's using the mock store, so it doesn't actually charge anything. but a simple flick of the switch and it could use the real store with the same apis.
  * Once you have a game and some credits, you're ready to join the game and set your availability to true.
  * At this point, what you do in your client depends on how you wish to implement things. Right now, there are no push notificaitons from the server, so the only way to know the state of things is to pull the sync document. I will detail the sync messages below. But basically the idea is that a game has a start time. everyone who wishes to play joins ahead of time, and then at (or near) the designated time the game will start (or be cancelled due to lack of player interest). The clients begin polling a little before the game gets going.
  * Once the game starts, everyone is randomly paired for each of the POOL rounds, and assigned a rank for each match. If there is an odd man out, a bot player is added to the game which will randomly answer each question. At the end of the pool play matches, the ranks are totalled and an overall rank is given to each player.
  * After the final POOL round, everyone is moved into the first BRACKET round and paired according to their ranking from the POOL rounds. Play continues as normal.
  * At the end of a BRACKET round, everyone receives an updated ranking, and anyone who lost has a "life" decremented. If your lives reach 0, you will receive an 'eliminated' sync message and must wait for the game_result. Anyone who won or lost but still has lives left will move on to the next BRACKET round.
  * BRACKET rounds are created by the system as-needed (i.e. as long as there is more than one player with a life left a new BRACKET round will be created when the last one completes).
  * Once the system reaches a single winner, the game will end and a game_result sync message will be sent to everyone.

# The sync messages 
* the doc can be published via an HTTP POST to http://&lt;collector&gt;/sync/generateSyncDoc with the usual headers and the following url form encoded params:
 * toWds
 * fromDate - ISO8601 date of when to begin listing sync messages from (the messages will be &gt;>= to this date)
 * contextualId - optional, but for the ShoutMillionaire purposes, always pass in the gameId for this value
* the WDS doc will be published here: http://&lt;wds&gt;/&lt;subscriberEmailHash&gt;/&lt;gameId&gt;/syncDoc.json . Use the usual methods to determine if the doc is fresh or if you need to continue waiting for it to be published, or if there is no change.
* the message sorting is undefined, so sort in the client via createDate ASC to get the oldest at top and work your way through them.
* in my test client, i keep a Set&lt;String&gt; of sync messages that i have already processed so that i don't accidentally process a message twice. (i.e. i filter out any messages i've seen before, then sort the resulting list and go through them from oldest to newest, taking appropriate action).
* Here is the format of a SyncMessage json:
```json
{
    "id" : string-uuid,
    "subscriberId" : int,
    "messageType": string-enum[joined_game,abandoned_game,joined_round,abandoned_round,user_matched,question,question_result,match_result,game_result],
    "contextualId": optString-gameId,
    "engineKey": string[SHOUT_MILLIONAIRE],
    "payload": optString-json,
    "createDate": string-iso8601
}
```
* The payload will depend on the type of message. See below

```
         +-------------+                +----------------+
         |             |                |                |
+--------> joined_game |        +-------> abandoned_game |
         |             |                |                |
         +------+------+                +----------------+
                |
                |
                |
         +------v-------+               +-----------------+
         |              |               |                 |
  +------> joined_round |       +-------> abandoned_round |
  |      |              |               |                 |
  |      +------+-------+               +-----------------+
  |             |
  |             |
  |             |
  |      +------v-------+
  |      |              |
  |      | user_matched |
  |      |              |
  |      +------+-------+
  |             |
  |             |
  |             |
  |      +------v---+
  |      |          |
  |  +---> question |
  |  |   |          |
  |  |   +------+---+
  |  |          |
  |  |          |
  |  |          |
  |  |   +------v----------+
  |  |   |                 |
  |  +---+ question_result |
  |      |                 |
  |      +------+----------+
  |             |
  |             |
  |             |
  |      +------v-------+            +------------+
  |      |              |            |            |
  +------+ match_result +------------> eliminated |
         |              |            |            |
         +------+-------+            +-----+------+
                |                          |
                |                          |
                |                          |
         +------v------+                   |
         |             |                   |
         | game_result <-------------------+
         |             |
         +-------------+
```

## joined_game sync message
* A player has called the http://&lt;collector&gt;/shoutmillionaire/game/join API
 * toWds
 * gameId
* The game must be in the OPEN state
* The player must have an allowed language code, not be in a forbidden country, and be using an allowed app id
 * The client knows all of this data from the game.json WDS document, and should thus filter ahead of time to only show games the user can actually join
* The player must not have already joined the game (unless they also abandoned the game afterwards)
* there is no payload for this message type

## abandoned_game sync message
* A player has called the http://&lt;collector&gt;/shoutmillionaire/game/leave API
 * toWds
 * gameId
* The game must be in the OPEN state
* The player must have previously joined the game
* there is no payload for this message type
 
## joined_round sync message 
* A player has called the http://&lt;collector&gt;/shoutmillionaire/game/setAvailability API
 * toWds
 * demo - if present and set to true, will use the mock store, otherwise will use the real store
 * available - boolean, set to true
 * gameId
* OR a player has played a round, and there is another round and they have been automatically added to it
* the player must have already joined the game, have enough creds to join the round, and the round status must be OPEN
* the payload will be 
```json
{
    "roundPlayer": RoundPlayer(see below)
}
```

## abandoned_round sync message
* A player has called the http://&lt;collector&gt;/shoutmillionaire/game/setAvailability API
 * toWds
 * demo - if present and set to true, will use the mock store, otherwise will use the real store
 * available - boolean, set to false
 * gameId
* the player must have already joined the game, and joined the round, and the round must be OPEN or FULL
* the payload will be 
```json
{
    "roundPlayer": RoundPlayer(see below)
}
```

## user_matched sync message
* A player has been paired in a match (happens automatically after joining a game/round and then game and round begins).
* the payload will be
```json
{
    "players": [
        MatchPlayer(see below),
        MatchPlayer
    ]
}
```
* one of the MatchPlayer records is yours, the other is your opponents (use the subscriberId to tell which is which)

## question sync message
* A match is a pairing of players in a round. A match consists of one or more questions. For each question in the match, there will be a question, question_result sync message pairing.
* Receiving a question sync message means there is a question ready to be answered.
* the payload will be
```json
{
    "question": string-encryptedQuestionjson-see below for the Question json,
    "subscriberQuestionAnswerId": string-uuid
}
```
* once retrieved, the user must call the  http://&lt;collector&gt;/shoutmillionaire/question/getDecryptKey API to get the decrypt key, decrypt the question, and then display it to the user.
 * toWds
 * subscriberQuestionAnswerId (from the payload json just retrieved)
 * response:
 ```json
 {
     "success": boolean,
     "decryptKey": string
 }
 ```
* as soon as getDecryptKey is called, the clock is ticking
* using the decrypt key response value, decrypt the question json (from the payload json just retrieved), and display the question
* once the user picks an answer, the http://&lt;collector&gt;/shoutmillionaire/question/submitAnswer API must be called to submit the answer
 * toWds
 * subscriberQuestionAnswerId
 * selectedAnswerId

## question_result sync message
* after the question has either been answered by all players, or the players have timed out, the question will be scored. After scoring, this message will detail the results of the match for each of the players involved.
* the payload will be
```json
{
    "matchQuestion": MatchQuestion(see below),
    "subscriberQuestionAnswers": [
        SubscriberQuestionAnswer(see below),
        SubscriberQuestionAnswer
    ]
}
```
* one of the SubscriberQuestionAnswer records is yours, the other is your opponents (use the subscriberId to tell which is which)
* once this is sent, if the match is over, a match_result sync message will be sent, otherwise another question sync message will be sent.

## match_result sync message
* one a match is over, this will be sent.
* the payload will be
```json
{
    "gamePlayer": GamePlayer(see below),
    "roundPlayer": RoundPlayer(see below)
}
```
* if there is another round to play, the next message will be a joined_round sync message, otherwise the next message will be a game_result sync message

## eliminated sync message
* if you run out of lives and get eliminated before the final round, you will receive this sync message.
* there is no payload for this message type

## game_result sync message
* once the game is over, this sync message will be sent
* the payload will be
```json
{
    "gamePlayer": GamePlayer(see below),
    "roundPlayers": [
        RoundPlayer(see below),
        ...
    ]
}
```

### GamePlayer json
```json
{
    "id": string-uuid,
    "gameId": string-uuid,
    "subscriberId": int,
    "rank": optDouble-null until game is over, otherwise contains overall ranking at game end,
    "payoutPaymentId": optString-maps to receiptId in the store.receipt table,
    "payoutAwardedAmount": optDouble-winning amount,
    "payoutVenue": optString-how were they paid,
    "payoutCompleted": boolean,
    "determination": string-enum[
        INPLAY-actively playing game,
        SIDELINES-in game, but sitting out this round (n/a for ShoutMillionaire),
        ELIMINATED,
        AWARDED-game is over, you have received winnings (if any),
        REMOVED-player called abandon_game,
        CANCELLED-the game was cancelled
    ],
    "nextRoundId": optString-uuid-if there is another round (null on last),
    "lastRoundId": optString-uuid-the round just played (null on first),
    "createDate": string-iso8601
}
```

### RoundPlayer json
```json
{
    "id": string-uuid,
    "gameId": string-uuid,
    "roundId": string-uuid,
    "subscriberId": int,
    "playedMatchCount": optInt-null (n/a for ShoutMillionaire),
    "determination": string-enum[WON, LOST, TIMEDOUT, ABANDONED, UNKNOWN, CANCELLED],
    "receiptId": optString-maps to store.receipt if there was a payout for the round,
    "amountPaid": optDouble-null unless there was a payout,
    "refunded": optBoolean-null, or true if there was refund given (paid to enter round, then round cancelled),
    "skillAnswerCorrectPct": optDouble-used for calculating rank-internal use only,
    "skillAverageAnswerMs": optLong-used for calculating rank-internal use only,
    "rank": optDouble-overall rank in the round,
    "createDate": string-iso8601
}
```

### MatchPlayer json
```json
{
    "id": string-uuid,
    "gameId": string-uuid,
    "roundId": string-uuid,
    "matchId": string-uuid,
    "roundPlayerId": string-uuid,
    "subscriberId": int,
    "determination": string-enum[
        UNKNOWN-not yet scored,
        WON,
        LOST,
        CANCELLED
    ],
    "score": optDouble-score for the match (n/a for ShoutMillionaire),
    "createDate": string-iso8601
}
```

### Question json
```json
{
    "id": string-uuid,
    "difficulty": int[0=not set, 1=easy, 10=hard],
    "source": optString-where did the question come from,
    "languageCodes": [ string, ... ],
    "forbiddenCountryCodes": [ optString, ...],
    "questionText": { "<languageCode>": string, ... },
    "mediaUrl": optString,
    "mediaType": optString[png, jpg, mp3, mp4, avi, etc...],
    "questionCategoryUuids": [string, ... ],
    "answers": [
        QuestionAnswer(see below),
        ...
    ],
    "usageCount": int-how many times the question has been shown,
    "createDate": string-iso8601,
    "expirationDate": optString-iso8601,

}
```

### QuestionAnswer json
```json
{
    "id": string-uuid,
    "questionId": string-uuid,
    "answerText": { "<languageCode>": string, ... },
    "mediaUrl": optString,
    "mediaType": optString[png, jpg, mp3, mp4, avi, etc...],
    "correct": optBool-at least one answer must be marked correct for each question for ShoutMillionaire,
    "surveyPercent": optInt-survey says-n/a for ShoutMillionaire right now,
    "createDate": string-iso8601
}
```

### SubscriberQuestionAnswer json
```json
{
    "id": string-uuid,
    "gameId": string-uuid,
    "roundId": string-uuid,
    "matchId": string-uuid,
    "questionId": string-uuid,
    "matchQuestionId": string-uuid,
    "subscriberId": int,
    "selectedAnswerId": optString-null is not yet answered,
    "questionDecryptKey": string,
    "questionPresentedTimestamp": string-iso8601,
    "durationMilliseconds": optLong-how long it took to answer-null is not yet answered,
    "determination": string-enum[
        UNKNOWN-not yet scored,
        WON_TIME-both answered correct, you were faster,
        LOST_TIME-both answered correct, you were slower,
        WON_CORRECT-you got it correct, opponent got it incorrect,
        LOST_INCORRECT-you got it incorrect, opponent got it correct,
        WON_TIMEOUT-you got it correct, opponent timed out,
        LOST_TIMEOUT-you timed out, opponent got it correct,
        LOST_ALL_TIMEOUT-both timed out
    ],
    "won": boolean-did subscriber win-convenience for determination,
    "createDate": string-iso8601
}
```

### MatchQuestion json
```json
{
    "id": string-uuid,
    "gameId": string-uuid,
    "roundId": string-uuid,
    "matchId": string-uuid,
    "questionId": string-uuid,
    "questionValue": string (n/a for ShoutMillionaire),
    "matchQuestionStatus": string-enum[NEW, OPEN, PROCESSING, CLOSED, CANCELLED],
    "wonSubscriberId": optInt-null until there is a winner (possible there is no winner),
    "determination": string-enum[WINNER, NO_WINNER, TIE, UNKNOWN],
    "createDate": string-iso8601,
    "completedDate": optString-iso8601 (null until match is over)
}
```

# What's Next
* when selecting a question, take usage_count into account
* have a question manager that pre-chooses questions so they're ready to go when they're needed
* we haven't yet tied into any question provider services, so there are only about 50 questions
* send out push notifications for big events, like user_matched so constant polling by the clients isn't necessary (they can still poll while a game is in play, but that's ok because it's for a short time and has a definite begin/end)
* publish game after it closes so the WDS is up to date
* currently the engine isn't waiting between sending questions, it just sends the next one as soon as the previous one is scored
