#!/usr/bin/env python

import dateutil.parser
import json

class SyncMessage:
    def __init__(self, dict):
        self.messageType = dict['messageType']
        self.createDate = dateutil.parser.parse(dict['createDate'])
        self.payload = json.loads(dict['payload'])
        
    def __str__(self):
        return "{0}  {1}   payload: {2}".format(self.createDate, self.messageType, self.payload)
    
    def __repr__(self):
        return "{0}  {1}   payload: {2}".format(self.createDate, self.messageType, self.payload)