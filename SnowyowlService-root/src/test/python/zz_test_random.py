#!/usr/bin/env python

import random
import time
from tzlocal import get_localzone
from datetime import datetime

from SyncMessage import SyncMessage

# dialNetworkSpeedMinMs = 2500
# dialNetworkSpeedMaxMs = 9000
# 
# questionDecryptKeyWaitTimeMs = randint(dialNetworkSpeedMinMs, dialNetworkSpeedMaxMs)
# questionDecryptWaitStartTime = datetime.now(get_localzone())
# print("waiting for: {0}".format(questionDecryptKeyWaitTimeMs))
# 
# done = False
# while not done:
#     time.sleep(1)
#     now = datetime.now(get_localzone())
#     
#     elapsedTime = now - questionDecryptWaitStartTime
#     diffMs = 0
#     diffMs = diffMs + (elapsedTime.seconds * 1000)
#     diffMs = diffMs + (elapsedTime.microseconds / 1000)
#     
#     #print("elapsed time: {0}".format(elapsedTime))
#     #print("elapsed time (sec): {0}".format(elapsedTime.seconds))
#     #print("elapsed time (usec): {0}".format(elapsedTime.microseconds))
#     print("elapsed time (ms): {0}".format(diffMs))
#     
#     if diffMs >= questionDecryptKeyWaitTimeMs:
#         print("we have waited long enough")
#         done = True


#sm = SyncMessage({'messageType':'user_matched', 'createDate':'2017-07-20T10:10:10.000Z', 'payload': '{"subscriberQuestionAnswerId":"12345"}'})
# sm = SyncMessage({'messageType':'user_matched', 'createDate':'2017-07-20T10:10:10.000Z', 'payload': None})
# print sm
# sqaId = sm.payload['subscriberQuestionAnswerId']
# print sqaId

x = random.uniform(0,1)
print("{0} < .85? {1}".format(x, x < .85))