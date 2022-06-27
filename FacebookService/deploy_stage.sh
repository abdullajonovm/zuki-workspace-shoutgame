#!/bin/bash

mvn install
echo -e "cd stage/services/\nlcd target\nput facebook-service-1.0.jar" | sftp -b - stage.shoutgameplay.com
ssh stage.shoutgameplay.com "echo \"blorgor0()\" | sudo -S -u mrsoa cp stage/services/* /opt/meinc/mrsoa/deploy/ && rm stage/services/*"
echo ""

