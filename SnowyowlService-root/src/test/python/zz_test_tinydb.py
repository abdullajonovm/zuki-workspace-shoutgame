#!/usr/bin/env python
#pip install tinydb
#http://tinydb.readthedocs.io/en/latest/getting-started.html

from tzlocal import get_localzone
from datetime import datetime
#import os
from tinydb import TinyDB, Query

db = TinyDB('db.json')

#val2 = {'inuse': False, 'username': 'stest11@shoutgp.com', 'password': 'foobars', 'deviceId': '5f7ed1f6-cd28-4e2a-98b9-6963f32e60e0'}
#db.insert(val2)
#print(db.all())

for item in db:
    print item

Item = Query()
#result = db.search(Item.inuse == False)
#print result

print('\n')

db.update({'inuse': True}, Item.username == 'stest11@shoutgp.com')

for item in db:
    print item
