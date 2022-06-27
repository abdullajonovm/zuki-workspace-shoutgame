#!/usr/bin/env python

import logging
from tinydb import TinyDB, Query
import uuid
import json
from SmNetwork import SmNetwork, SmNetworkException
from MySqlHelper import MySqlHelper
from SyncMessage import SyncMessage
from tzlocal import get_localzone
from datetime import datetime
import dateutil.parser
import time
import random 
import crypto

SRD_ENDPOINT = 'http://dc4-static.shoutgameplay.com/srd2.json'
USER_DB_NAME = 'db-dc4_user.json';
 
CONFIG_PROP_FILE_LOCATION = '/Volumes/Encrypted Data/ShoutMeinc/shoutmillionaire.properties'
CONFIG_DB_DB_KEY = 'dc4.db.db'
CONFIG_DB_PORT_KEY = 'dc4.db.port'
CONFIG_DB_USERNAME_KEY = 'dc4.db.username'
CONFIG_DB_PASSWORD_KEY = 'dc4.db.password'

GAME_ID = '73790d4c-133e-4547-b057-d8ae78de5912'
 
class User:
    """A user of the system. It contains the logic necessary to play a game
    """
    
    #User state enum
    WAITING, \
    JOINING_GAME, \
    JOINED_GAME, \
    WAITING_FOR_JOINED_ROUND, \
    WAITING_FOR_PAIRING, \
    WAITING_FOR_QUESTION, \
    WAITING_TO_REQUEST_DECRYPT_KEY, \
    WAITING_TO_ANSWER, \
    WAITING_FOR_QUESTION_RESULT, \
    WAITING_FOR_POST_QUESTION_RESULT, \
    WAITING_FOR_BRACKET_POST_MATCH_RESULT, \
    DONE = range(12)
    
    ROUND_STATE_NONE, ROUND_STATE_POOL, ROUND_STATE_BRACKET = range(3)
    
    def __init__(self, mysql):
        self.log = logging.getLogger('tv.shout.sm')
        self.mysql = mysql
        self._initializeFromDb_()
        self.net = SmNetwork(SRD_ENDPOINT, self.email, self.deviceId)
        self._getReadyToPlay_()
        self.state = User.WAITING
        self.roundState = User.ROUND_STATE_NONE
        
        #TODO: allow setting of these dials based on some type of percentage check
        self.dialNetworkSpeedMinMs = 250
        self.dialNetworkSpeedMaxMs = 2500
        
        self.dialAnswerSpeedMinMs = 3000
        self.dialAnswerSpeedMaxMs = 9000
        
        self.dialQuestionCorrectPercentage = .85
                
    def _initializeFromDb_(self):
    #find out which user to be. re-use if possible, otherwise create a new user
        db = TinyDB(USER_DB_NAME)
        #for item in db:
        #    self.log.debug('>>> ' + str(item))
        
        #search for any records that are not in use
        Item = Query()
        result = db.search(Item.inUse == False)
        #self.log.debug(">>> there are {0} db items with inUse==False".format(len(result)))
        if len(result) > 0:
            #use the first item in the list
            userData = result[0]
            
            #self.log.debug("using this existing db item: {0}".format(userData))
            
            #notify the db that this item is now in use
            db.update({'inUse': True}, Item.email == userData['email'])
            
            #store off relevant data
            self.email = userData['email']
            self.password = userData['password']
            self.deviceId = userData['deviceId']
            self.nickname = userData['nickname']
            
            self.log.debug('initializing user from existing record. email: {0}, deviceId: {1}'.format(self.email, self.deviceId))
            
        else:
            #no available records. create one
            xuuid = str(uuid.uuid4())
            self.email = 'tst_' + xuuid + '@shoutgp.com'
            self.nickname = 'tst_' + xuuid
            self.password = str(uuid.uuid4())
            self.deviceId = str(uuid.uuid4())
            
            #self.log.debug(">>> creating a new db item")
            
            db.insert({'inUse': True, 'email': self.email, 'password': self.password, 'deviceId': self.deviceId, 'nickname': self.nickname})
            
            self.log.debug('initializing user from new record. email: {0}, deviceId: {1}'.format(self.email, self.deviceId))

    def _getStateName_(self):
        if self.state == User.WAITING:
            return 'WAITING'
        elif self.state == User.JOINING_GAME:
            return 'JOINING_GAME'
        elif self.state == User.JOINED_GAME:
            return 'JOINED_GAME'
        elif self.state == User.WAITING_FOR_JOINED_ROUND:
            return 'WAITING_FOR_JOINED_ROUND'
        elif self.state == User.WAITING_FOR_PAIRING:
            return 'WAITING_FOR_PAIRING'
        elif self.state == User.WAITING_FOR_QUESTION:
            return 'WAITING_FOR_QUESTION'
        elif self.state == User.WAITING_TO_REQUEST_DECRYPT_KEY:
            return 'WAITING_TO_REQUEST_DECRYPT_KEY'
        elif self.state == User.WAITING_TO_ANSWER:
            return 'WAITING_TO_ANSWER'
        elif self.state == User.WAITING_FOR_QUESTION_RESULT:
            return 'WAITING_FOR_QUESTION_RESULT'
        elif self.state == User.WAITING_FOR_POST_QUESTION_RESULT:
            return 'WAITING_FOR_POST_QUESTION_RESULT'
        elif self.state == User.WAITING_FOR_BRACKET_POST_MATCH_RESULT:
            return 'WAITING_FOR_POST_QUESTION_RESULT'
        else:
            return 'UNKNOWN STATE: ' + str(self.state)
        
    #login (or signup), and make sure credits have been purchased
    def _getReadyToPlay_(self):
        #first try a login
        try:
            self.log.debug('attempting to login via {0}'.format(self.email))
            self.net.login(self.password)
        except ValueError:
            self.log.debug('unable to login. trying signup using {0}'.format(self.email))
            self.net.signup(self.password, self.nickname)
            
        balance = self.net.getBalance()
        self.log.debug('balance for '+self.email+': ${:0,.2f}'.format(balance))
        if balance < 1.0:
            self.log.debug('{0} is purchasing more credits.'.format(self.email))
            self.net.purchaseCredits()
            
    def joinGame(self, gameId):
        if self.state != User.WAITING:
            raise ValueError("User must be in the WAITING state to join a game. Current state: {0}".format(_getStateName_()))
        
        self.state = User.JOINING_GAME
        
        #use a time in the past for the initial retrieval
        lowwatermark = dateutil.parser.parse('2001-07-19T22:17:00.683Z')
        #self.highwatermark = datetime.now(get_localzone())
            
        try:
            self.net.joinGame(gameId)
        except SmNetworkException as e:
            if e.type == 'alreadyJoined':
                self.log.info('already joined game')
            else:
                raise e
            
        self.gameId = gameId
        
        #wait until the joined_game sync message arrives
        foundJoinedGame = False
        while not foundJoinedGame:
            #sleep for 100ms
            time.sleep(.1)
            syncMessages = self.net.getSyncMessages(self.gameId, lowwatermark)
            for sm in syncMessages:
                if sm.messageType == 'joined_game':
                    self.highwatermark = sm.createDate
                    self.log.debug("found joined_game sync message. createDate: {0}".format(self.highwatermark))
                    foundJoinedGame = True
                    break
        
        self.state = User.JOINED_GAME
        self.log.debug("{0} joined game {1}".format(self.email, gameId))
        
    def playPoolRound(self):
        if self.state != User.JOINED_GAME:
            raise ValueError("User must be in the JOINED_GAME state to play a pool round. Current state: {0}".format(_getStateName_()))
        
        self.state = User.WAITING_FOR_JOINED_ROUND
        self.roundState = User.ROUND_STATE_POOL
        self.net.beginPoolPlay(self.gameId)
        
    def playBracketRound(self, gameId):
        self.gameId = gameId
        
        #make sure the user has already joined the game (as a side-effect, the highwatermark is set)
        joinedGame = False
        lowwatermark = dateutil.parser.parse('2001-07-19T22:17:00.683Z')
        syncMessages = self.net.getSyncMessages(self.gameId, lowwatermark)
        for sm in syncMessages:
            self.highwatermark = sm.createDate
            if sm.messageType == 'joined_game':
                joinedGame = True
            elif sm.messageType == 'abandoned_game':
                joinedGame = False
        
        if joinedGame:
            self.state = User.JOINED_GAME
        
        if self.state != User.JOINED_GAME:
            raise ValueError("User must be in the JOINED_GAME state to play the bracket rounds. Current state: {0}".format(_getStateName_()))
        
        self.state = User.WAITING_FOR_JOINED_ROUND
        self.roundState = User.ROUND_STATE_BRACKET
        
        #this actually isn't necessary. it does nothing on the server. everyone in the game is automatically moved into the bracket rounds
        #self.net.beginBracketPlay(self.gameId)
        
    def controlLoop(self):
        if self.state == User.WAITING:
            self.log.warn("controlLoop called while in WAITING state")
            return
        elif self.state == User.JOINING_GAME:
            self.log.warn("controlLoop called while in JOINING_GAME state")
            return
        elif self.state == User.JOINED_GAME:
            self.log.warn("controlLoop called while in JOINED_GAME state")
            return
        
        elif self.state == User.WAITING_FOR_JOINED_ROUND: #TODO: BUG: should be WAITING_FOR_JOINED_OR_ABANDONED_ROUND 
            self.log.debug("waiting to join round...")
            syncMessages = self.net.getSyncMessages(self.gameId, self.highwatermark)
            for sm in syncMessages:
                if sm.messageType == 'joined_round':
                    self._handleJoinedRoundSyncMessage(sm)
                    break;
                #elif sm.messageType == 'abandoned_round':
                #    self.state = User.DONE
                #    break;
                    
        elif self.state == User.WAITING_FOR_PAIRING:
            self.log.debug("waiting to be paired...")
            syncMessages = self.net.getSyncMessages(self.gameId, self.highwatermark)
            for sm in syncMessages:
                if sm.messageType == 'user_matched':
                    self.highwatermark = sm.createDate
                    self.log.debug("received user_matched sync message\n{0}".format(sm))
                    self.state = User.WAITING_FOR_QUESTION
                    break;
                
        elif self.state == User.WAITING_FOR_QUESTION:
            self.log.debug("waiting for question...")
            syncMessages = self.net.getSyncMessages(self.gameId, self.highwatermark)
            for sm in syncMessages:
                if sm.messageType == 'question':
                    self._handleQuestionSyncMessage(sm)
                    break
                
        elif self.state == User.WAITING_TO_REQUEST_DECRYPT_KEY:
            self.log.debug("waiting to request question decrypt key...")
            
            #has the user waited long enough to request the decrypt key?
            if self._getMsSinceDate(self.questionDecryptWaitStartTime) >= self.questionDecryptKeyWaitTimeMs:
                #grab the decrypt key (a sync call) and decrypt the question
                decryptKey = self.net.getQuestionDecryptKey(self.sqaId)
                self.question = json.loads(crypto.aes_decrypt(decryptKey, self.encryptedQuestionAndAnswersBody))
                self.log.debug("\nQUESTION\n {0}".format(self.question))
                
                #using the ANSWER_SPEED dial, determine how long to wait until answering the question
                self.answerWaitTimeMs = random.randint(self.dialAnswerSpeedMinMs, self.dialAnswerSpeedMaxMs)
                self.answerWaitStartTime = datetime.now(get_localzone())
                self.state = User.WAITING_TO_ANSWER
                
        elif self.state == User.WAITING_TO_ANSWER:
            self.log.debug('waiting to answer question...')
            
            #has the user waited long enough to answer the question?
            if self._getMsSinceDate(self.answerWaitStartTime) >= self.answerWaitTimeMs:
                #determine if the user should get the question correct or not using the QUESTION_CORRECT_PERCENTAGE dial
                shouldGetCorrect = random.uniform(0,1) < self.dialQuestionCorrectPercentage
                self.log.debug("should get correct: {0}".format(shouldGetCorrect))
                
                #find the correct answerId (need regardless of whether answering correct or incorrect)
                correctAnswerId = self.mysql.getCorrectAnswerId(self.question['id'])
                self.log.debug('correctAnswerId: {0}'.format(correctAnswerId))
                
                #submit answer
                if shouldGetCorrect:
                    self.net.submitAnswer(self.sqaId, correctAnswerId)
                else:
                    incorrectAnswerId = None
                    for answer in self.question['answers']:
                        if answer['id'] != correctAnswerId:
                            incorrectAnswerId = answer['id']
                            break
                        
                    self.net.submitAnswer(self.sqaId, incorrectAnswerId)
                
                self.state =  User.WAITING_FOR_QUESTION_RESULT
                
        elif self.state ==  User.WAITING_FOR_QUESTION_RESULT:
            self.log.debug("waiting for question result...")
            
            syncMessages = self.net.getSyncMessages(self.gameId, self.highwatermark)
            for sm in syncMessages:
                if sm.messageType == 'question_result':
                    self.highwatermark = sm.createDate
                    self.log.debug("received question_result sync message\n{0}".format(sm))
                    
                    self.state = User.WAITING_FOR_POST_QUESTION_RESULT
                    break
                
        elif self.state == User.WAITING_FOR_POST_QUESTION_RESULT:
            self.log.debug('waiting for next question OR match_result...')
            
            syncMessages = self.net.getSyncMessages(self.gameId, self.highwatermark)
            for sm in syncMessages:
                if sm.messageType == 'question':
                    self._handleQuestionSyncMessage(sm)
                    break
                elif sm.messageType == 'match_result':
                    self.highwatermark = sm.createDate
                    self.log.debug("received match_result sync message\n{0}".format(sm))
                    
                    #if this is POOL play, we're done, otherwise there are other things to check for
                    if self.roundState == User.ROUND_STATE_POOL:
                        self.state = User.DONE
                    else:
                        self.state = User.WAITING_FOR_BRACKET_POST_MATCH_RESULT
                    break
                
        elif self.state == User.WAITING_FOR_BRACKET_POST_MATCH_RESULT:
            self.log.debug('waiting for joined_round OR eliminated OR game_result...')
            
            syncMessages = self.net.getSyncMessages(self.gameId, self.highwatermark)
            for sm in syncMessages:
                if sm.messageType == 'eliminated':
                    self.highwatermark = sm.createDate
                    self.log.debug("received eliminated sync message\n{0}".format(sm))
                    self.state = User.DONE
                    break
                    
                elif sm.messageType == 'game_result':
                    self.highwatermark = sm.createDate
                    self.log.debug("received game_result sync message\n{0}".format(sm))
                    self.state = User.DONE
                    break
                    
                elif sm.messageType == 'joined_round':
                    self._handleJoinedRoundSyncMessage(sm)
                    break
            
            
    
    def _getMsSinceDate(self, date):
        """how many ms have elapsed from the given date until now
        """
        now = datetime.now(get_localzone())
        elapsedTime = now - date
        diffMs = 0
        diffMs = diffMs + (elapsedTime.seconds * 1000)
        diffMs = diffMs + (elapsedTime.microseconds / 1000)
        
        return diffMs
        
    def _handleQuestionSyncMessage(self, sm):
        self.highwatermark = sm.createDate
        self.log.debug("received question sync message\n{0}".format(sm))
        
        # off the question information from the payload
        self.sqaId = sm.payload['subscriberQuestionAnswerId']
        self.encryptedQuestionAndAnswersBody = sm.payload['question']
        
        #using the NETWORK_SPEED dial, determine how long to wait until requesting the decrypt key
        self.questionDecryptKeyWaitTimeMs = random.randint(self.dialNetworkSpeedMinMs, self.dialNetworkSpeedMaxMs)
        self.questionDecryptWaitStartTime = datetime.now(get_localzone())
        self.state = User.WAITING_TO_REQUEST_DECRYPT_KEY
        
    def _handleJoinedRoundSyncMessage(self, sm):
        self.highwatermark = sm.createDate
        self.log.debug("received joined_round sync message\n{0}".format(sm))
        self.state = User.WAITING_FOR_PAIRING

        
def free_all_users():
    db = TinyDB(USER_DB_NAME)
    result = db.search(Query().inUse == True)
    if len(result) > 0:
        for item in result:
            db.update({'inUse': False}, Query().email == item['email'])
    
def tester():
    logging.basicConfig()
    log = logging.getLogger('tv.shout.sm')
    log.setLevel(logging.DEBUG)
    
    free_all_users()
    
    mysql = MySqlHelper()
    mysql.start(CONFIG_PROP_FILE_LOCATION, CONFIG_DB_DB_KEY, CONFIG_DB_PORT_KEY, CONFIG_DB_USERNAME_KEY, CONFIG_DB_PASSWORD_KEY)
    
    user1 = User(mysql)
    #user2 = User(mysql)
    
    #user1.joinGame(GAME_ID)
    #user1.playPoolRound()
    user1.playBracketRound(GAME_ID)
    
    while user1.state is not User.DONE:
        time.sleep(.5)
        user1.controlLoop()
        
    mysql.stop()
    
if __name__ == "__main__": tester()