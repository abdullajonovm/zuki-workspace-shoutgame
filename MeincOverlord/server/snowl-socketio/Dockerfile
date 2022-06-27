FROM node:8.3.0-alpine
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Snowy Owl socket.io Server"

RUN apk add --no-cache bash

ENV SOCKETIO_USER=socketio
ENV SOCKETIO_GROUP=socketio

RUN addgroup -S "$SOCKETIO_GROUP"
RUN adduser -S -h /app -H -G "$SOCKETIO_GROUP" "$SOCKETIO_USER" -s /bin/bash

RUN npm install socket.io

COPY socketio.sh /usr/local/sbin/
ADD  http://localhost:40080/pre-launch.sh /usr/local/sbin/
ADD  http://localhost:40080/dclone.sh /usr/local/sbin/
RUN  chmod 755 /usr/local/sbin/*.sh

COPY pre-launch.sh /usr/local/sbin/pre-launch_00_socketio.sh
COPY app.js /dclone/app/
RUN  chown -R $SOCKETIO_USER:$SOCKETIO_GROUP /dclone/app

RUN echo "alias ll='ls -l --color=auto'" >>/root/.bashrc

EXPOSE 8080

ENTRYPOINT ["/bin/bash", "/usr/local/sbin/socketio.sh"]
