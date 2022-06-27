#!/usr/bin/env python
#https://dev.mysql.com/downloads/connector/python/

import mysql.connector
import logging

class MySqlHelper:
    def __init__(self):
        self.log = logging.getLogger('tv.shout.sm')
        self.connected = False
        
    def start(self, propFileLocation, configDbDbKey, configDbPortKey, configDbUsernameKey, configDbPasswordKey):
        self.dbUrl = None
        self.dbPort = None
        dbUsername = None
        dbPassword = None
        
        propfile = open(propFileLocation, "r")
        for line in propfile:
            if line.startswith(configDbDbKey):
                self.dbUrl = line.split("=")[1][:-1]
                #print("DB: {0}".format(dbUrl))
            elif line.startswith(configDbPortKey):
                self.dbPort = line.split("=")[1][:-1]
                #print("PORT: {0}".format(dbPort))
            elif line.startswith(configDbUsernameKey):
                dbUsername = line.split("=")[1][:-1]
                #print("USER: {0}".format(dbUsername))
            elif line.startswith(configDbPasswordKey):
                dbPassword = line.split("=")[1][:-1]
                #print("PASS: {0}".format(dbPassword))
                
        propfile.close()
        
        if self.dbUrl is None or self.dbPort is None or dbUsername is None or dbPassword is None:
            raise ValueError('unable to read db connection information from {0}'.format(propFileLocation))
        
        self.con = mysql.connector.connect(user=dbUsername, password=dbPassword, host=self.dbUrl, port=self.dbPort)
        self.connected = True
        self.log.debug('successfully connected to mysql: {0}:{1}'.format(self.dbUrl, self.dbPort))
    
    def stop(self):
        if self.connected:
            self.con.close()
            self.log.debug('successfully disconnected from mysql: {0}:{1}'.format(self.dbUrl, self.dbPort))
        self.connected = False
        
    def getCorrectAnswerId(self, questionId):
        if not self.connected:
            raise ValueError('mysql is not connected')
        
        cursor = self.con.cursor()
        query = ("select id from shoutmillionaire.question_answer where question_id = %s and correct = 1")
        correctAnswerId = None
        cursor.execute(query, (questionId,))
        for (answerId) in cursor:
            correctAnswerId = answerId[0]
                
        cursor.close()
        
        return correctAnswerId        