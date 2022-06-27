FROM anapsix/alpine-java:8u121b13_server-jre_unlimited
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Base MrSOA Server"

RUN apk update \
    && apk add vim \
    && apk add ca-certificates openssl \
    && update-ca-certificates

#RUN wget -O /tmp/consul-template.tgz https://releases.hashicorp.com/consul-template/0.18.5/consul-template_0.18.5_linux_amd64.tgz \
#RUN wget -O /tmp/consul-template.tgz https://releases.hashicorp.com/consul-template/0.19.3/consul-template_0.19.3_linux_amd64.tgz \
RUN wget -O /tmp/consul-template.tgz https://s3.amazonaws.com/nomad-static.shout.tv/consul-template_0.19.3-with_patch_1f7c022-linux_amd64.tgz \
    && tar -xzf /tmp/consul-template.tgz \
    && rm  -f   /tmp/consul-template.tgz \
    && mv consul-template /usr/bin/

COPY mrsoa.sh /usr/local/sbin/
ADD  http://localhost:40080/pre-launch.sh /usr/local/sbin/
ADD  http://localhost:40080/consul-template.sh /usr/local/sbin/
ADD  http://localhost:40080/dclone.sh /usr/local/sbin/
RUN  chmod 755 /usr/local/sbin/*.sh

## Install MrSOA
RUN ln /opt/jdk/bin/java /opt/jdk/bin/mrsoa_java

ENV MEINC_HOME=/opt/meinc
ENV MRSOA_HOME=$MEINC_HOME/mrsoa
ENV MRSOA_USER="mrsoa"
ENV MRSOA_GROUP="mrsoa"

RUN addgroup -S "$MRSOA_GROUP"
RUN adduser -S -h /opt/meinc/mrsoa -H -G "$MRSOA_GROUP" "$MRSOA_USER" -s /bin/bash

COPY opt /opt

RUN touch $MRSOA_HOME/conf/meinc.properties \
    && chmod 640 $MRSOA_HOME/conf/meinc.properties \
    && touch $MRSOA_HOME/conf/meinc.properties.ctmpl \
    && chmod 640 $MRSOA_HOME/conf/meinc.properties.ctmpl \
    && touch $MRSOA_HOME/conf/mrsoa.conf \
    && chown $MRSOA_USER:$MRSOA_GROUP $MRSOA_HOME/conf/mrsoa.conf \
    && chmod 600 $MRSOA_HOME/conf/mrsoa.conf

RUN chown -R $MRSOA_USER:$MRSOA_GROUP /opt/meinc

RUN mkdir -p /dclone/$MRSOA_HOME/conf && \
    mv $MRSOA_HOME/conf/* /dclone/$MRSOA_HOME/conf/

RUN chown -R $MRSOA_USER:$MRSOA_GROUP /dclone/opt/meinc

RUN mkdir -p /var/run/mrsoa
RUN chown $MRSOA_USER:$MRSOA_GROUP /var/run/mrsoa
RUN chmod 770 /var/run/mrsoa

RUN echo "alias ll='ls -l --color=auto'" >>/root/.bashrc

VOLUME /mnt/host

ENTRYPOINT ["/bin/bash", "/usr/local/sbin/mrsoa.sh"]
