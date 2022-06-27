#!/usr/bin/env python

import logging
from srd import SRD
import collector
from collector import CollectorResponse
import collectorSync
from collectorSync import CollectorSyncResponse
from SyncMessage import SyncMessage
import crypto
import hashlib
import json
import uuid

class SmNetwork:
    """Contains the high level API for making network calls. Each User must create an instance of this class
    """

    def __init__(self, srdEndpoint, email, deviceId):
        srd = SRD(srdEndpoint)
        self.collectorUrls = srd.action('publish', email)
        self.toWds = srd.wds(email)
        self.email = email
        self.subscriber = None

        self.log = logging.getLogger('tv.shout.sm')

        self.log.debug('SmNetwork initialized using email: {0}, deviceId: {1}, toWds: {2}'.format(email, deviceId, self.toWds))

        self.authHeaders = {
            'X-REST-DEVICE-ID': deviceId,
            'X-REST-APPLICATION-ID': 'SHOUT',
            'X-REST-APPLICATION-VERSION': '6.1',
            'deviceModel': 'tester',
            'deviceName': 'locust',
            'deviceVersion': '1.0',
            'deviceOsName': 'python',
            'deviceOsType': 'test'
        }        

        self.headers = {
            'X-REST-DEVICE-ID': deviceId,
            'X-REST-APPLICATION-ID': 'SHOUT',
            'X-REST-APPLICATION-VERSION': '6.1'
        }

    def login(self, password):
        response = collector.send_message(self.collectorUrls, '/auth/login', self.toWds, self.authHeaders,
            email = self.email,
            password = hashlib.sha256(password).hexdigest()
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)

        payload = response.json()
        encryptedSessionKey = payload['sessionKey']
        encryptedSubscriber = payload['subscriber']

        encryptKey = response.encryptKey
        sessionKey = crypto.aes_decrypt(encryptKey, encryptedSessionKey)
        subscriber = json.loads(crypto.aes_decrypt(encryptKey, encryptedSubscriber))

        self.headers['X-REST-SESSION-KEY'] = sessionKey

        self.log.debug('/auth/login succeeded for email: {0}'.format(self.email))
        return True

    def signup(self, password, nickname):
        birthdate = '1990-10-31T00:00:00.000Z'
        countryCode = 'US'
        languageCode = 'en'

        passAsSha256 = hashlib.sha256(password).hexdigest()
        passAsScryptOfSha256 = crypto.scrypt_encrypt(passAsSha256)

        response = collector.send_message(self.collectorUrls, '/auth/signup', self.toWds, self.authHeaders,
            email = self.email,
            password = passAsScryptOfSha256,
            nickName = nickname,
            birthDate = birthdate,
            languageCode = languageCode,
            countryCode = countryCode
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)

        payload = response.json()
        encryptedSessionKey = payload['sessionKey']
        encryptedSubscriber = payload['subscriber']

        encryptKey = response.encryptKey
        sessionKey = crypto.aes_decrypt(encryptKey, encryptedSessionKey)
        subscriber = json.loads(crypto.aes_decrypt(encryptKey, encryptedSubscriber))

        self.headers['X-REST-SESSION-KEY'] = sessionKey

        self.log.debug('/auth/signup succeeded for email: {0}'.format(self.email))
        return True

    def getBalance(self):
        response = collector.send_message(self.collectorUrls, '/shoutmillionaire/player/details', self.toWds, self.headers)
        response.wait_json()

        self.checkForFailure(asyncResponse=response)
            
        payload = response.json()
        balance = payload['balance']
        return balance

    def purchaseCredits(self):
        #grab a client token
        response = collector.send_message(self.collectorUrls, '/store/getClientToken', self.toWds, self.headers,
            demo="true"
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)
        
        payload = response.json()
        clientToken = payload['token']
        self.log.debug('clientToken: {0}'.format(clientToken))

        #grab the uuid of the first item in the list
        response = collector.send_message(self.collectorUrls, '/store/getItemsForVenue', self.toWds, self.headers,
            demo='true',
            venue='tv.shout.shoutmillionaire'
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)
        
        payload = response.json()
        itemUuid = payload['items'][0]['uuid']
        self.log.debug('itemUuid: {0}'.format(itemUuid))

        #make the purchase
        response = collector.send_message(self.collectorUrls, '/store/purchaseItem', self.toWds, self.headers,
            demo='true',
            itemUuid=itemUuid,
            nonce=clientToken
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)

    def joinGame(self, gameId):
        response = collector.send_message(self.collectorUrls, '/shoutmillionaire/game/join', self.toWds, self.headers,
            gameId=gameId
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)    

    def beginPoolPlay(self, gameId):
        response = collector.send_message(self.collectorUrls, '/shoutmillionaire/game/beginPoolPlay', self.toWds, self.headers,
            gameId=gameId
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)    

    def beginBracketPlay(self, gameId):
        response = collector.send_message(self.collectorUrls, '/shoutmillionaire/game/beginBracketPlay', self.toWds, self.headers,
            gameId=gameId
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)    

    def getQuestionDecryptKey(self, sqaId):
        response = collectorSync.send_message(self.collectorUrls, '/shoutmillionaire/question/getDecryptKey', self.toWds, self.headers,
            subscriberQuestionAnswerId = sqaId
        )
        
        self.checkForFailure(syncResponse=response)
        
        payload = response.json_response
        decryptKey = payload['decryptKey']
        return decryptKey

    def submitAnswer(self, sqaId, answerId):
        response = collector.send_message(self.collectorUrls, '/shoutmillionaire/question/submitAnswer', self.toWds, self.headers,
            subscriberQuestionAnswerId=sqaId,
            selectedAnswerId=answerId
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)    

    def getSyncMessages(self, gameId, fromDate):
        response = collector.send_message(self.collectorUrls, '/shoutmillionaire/game/getSyncMessages', self.toWds, self.headers,
            gameId=gameId,
            fromDate=fromDate.isoformat()
        )
        response.wait_json()

        self.checkForFailure(asyncResponse=response)
        
        #grab the payload, sort by createDate
        payload = response.json()
        sortedSyncMessages = sorted(payload['syncMessages'], key=lambda k: k['createDate'])
        
        #turn each item into a SyncMessage object 
        list = []
        for sm in sortedSyncMessages:
            actualSyncMessage = SyncMessage(sm) 
            list.append(actualSyncMessage)
            #self.log.debug("SM: {0}".format(actualSyncMessage))
            
        return list;

    def checkForFailure(self, asyncResponse=None, syncResponse=None):
        payload = None
        path = None
        httpResponseSuccess = True
        httpStatusCode = 200
        httpErrorMessage = None
        
        if syncResponse is not None:
            payload = syncResponse.json_response
            path = syncResponse.request_path
            httpResponseSuccess = syncResponse.success
            httpStatusCode = syncResponse.status_code
            if not httpResponseSuccess:
                httpErrorMessage = syncResponse.error_body
        
        elif asyncResponse is not None:
            payload = asyncResponse.json()
            path = asyncResponse.request_path
            httpResponseSuccess = asyncResponse.success
            httpStatusCode = asyncResponse.status_code
            if not httpResponseSuccess:
                httpErrorMessage = asyncResponse.error_body
        
        else:
            return
        
        #check for http success
        if not httpResponseSuccess:
            reason = 'bad http resposne code: {0}'.format(httpStatusCode)
            raise ValueError('call {0} failed for email: {1}. reason: {2}, message: {3}'.format(path, self.email, reason, httpErrorMessage))
        
        #check for mrsoa success
        success = payload['success']
        if not success:
            reason = 'UNKNOWN'
            message = None
            keys = payload.items()
            for key in keys:
                if key[0] == 'message':
                    message = key[1]
                elif key[0] != 'success':
                    reason = key[0]

            #self.log.error('call {0} failed for email: {1}. reason: {2}, message: {3}'.format(path, self.email, reason, message))
            #raise ValueError('call {0} failed for email: {1}. reason: {2}, message: {3}'.format(path, self.email, reason, message))
            msg = 'call {0} failed for email: {1}. reason: {2}, message: {3}'.format(path, self.email, reason, message)
            raise SmNetworkException(msg, reason)
    
class SmNetworkException(RuntimeError):
    def __init__(self, message, type):
        super(RuntimeError, self).__init__(message)
        self.type = type
