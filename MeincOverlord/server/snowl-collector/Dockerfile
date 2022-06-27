FROM scm.shoutgameplay.com:5000/mrsoa:1.7.6
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Shout Collector Server"

COPY deploy/* /dclone/$MRSOA_HOME/deploy/
COPY osgi/*   /dclone/$MRSOA_HOME/osgi/

COPY pre-launch.sh /usr/local/sbin/pre-launch_10_collector.sh
RUN  chmod 755 /usr/local/sbin/*.sh

COPY log4j.xml   /dclone/$MRSOA_HOME/conf/
RUN  cd /$MRSOA_HOME/lib && ln -s ../conf/log4j.xml
COPY mrsoa.xargs /dclone/$MRSOA_HOME/conf/

COPY meinc.properties.ctmpl /dclone/$MRSOA_HOME/conf/
RUN  chmod 640 /dclone/$MRSOA_HOME/conf/*.ctmpl

RUN find /dclone/opt/meinc -exec chown $MRSOA_USER:$MRSOA_GROUP {} + -not -user $MRSOA_USER -or -not -group $MRSOA_GROUP

COPY firebase_config.json.ctmpl /$MEINC_HOME/keys/
COPY apns.keyfile.p12.ctmpl /$MEINC_HOME/keys/
RUN  chown $MRSOA_USER:$MRSOA_GROUP /$MEINC_HOME/keys/*.ctmpl && chmod 640 /$MEINC_HOME/keys/*.ctmpl

CMD ["-template", "$MEINC_HOME/keys/firebase_config.json.ctmpl:$MEINC_HOME/keys/firebase_config.json", \
     "-template", "$MEINC_HOME/keys/apns.keyfile.p12.ctmpl:$MEINC_HOME/keys/apns.keyfile.p12"]
