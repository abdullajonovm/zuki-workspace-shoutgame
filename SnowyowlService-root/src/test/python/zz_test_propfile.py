#!/usr/bin/env python
#https://dev.mysql.com/downloads/connector/python/

import mysql.connector

CONFIG_PROP_FILE_LOCATION = '/Volumes/Encrypted Data/ShoutMeinc/shoutmillionaire.properties'
CONFIG_DB_DB_KEY = 'dc4.db.db'
CONFIG_DB_PORT_KEY = 'dc4.db.port'
CONFIG_DB_USERNAME_KEY = 'dc4.db.username'
CONFIG_DB_PASSWORD_KEY = 'dc4.db.password'

dbUrl = None
dbPort = None
dbUsername = None
dbPassword = None

propfile = open(CONFIG_PROP_FILE_LOCATION, "r")
for line in propfile:
    if line.startswith(CONFIG_DB_DB_KEY):
        dbUrl = line.split("=")[1][:-1]
        print("DB: {0}".format(dbUrl))
    elif line.startswith(CONFIG_DB_PORT_KEY):
        dbPort = line.split("=")[1][:-1]
        print("PORT: {0}".format(dbPort))
    elif line.startswith(CONFIG_DB_USERNAME_KEY):
        dbUsername = line.split("=")[1][:-1]
        print("USER: {0}".format(dbUsername))
    elif line.startswith(CONFIG_DB_PASSWORD_KEY):
        dbPassword = line.split("=")[1][:-1]
        print("PASS: {0}".format(dbPassword))
        
propfile.close()

if dbUrl is None or dbPort is None or dbUsername is None or dbPassword is None:
    raise ValueError('unable to read db connection information')

questionId = '1c4f5123-4529-487d-a599-f91e6010d25d';
knownAnswerId = '91463b8c-0b85-4ae2-8b79-b093560efc72'

con = mysql.connector.connect(user=dbUsername, password=dbPassword, host=dbUrl, port=dbPort)
cursor = con.cursor()
query = ("select id from shoutmillionaire.question_answer where question_id = %s and correct = 1")
correctAnswerId = None
cursor.execute(query, (questionId,))
for (answerId) in cursor:
    correctAnswerId = answerId[0]
        
cursor.close()
con.close()

print('correct answer id: {0}'.format(correctAnswerId))
    