FROM scm.shoutgameplay.com:5000/nginx-lua:1.10.3-alpine-2
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Base Web Data Store Server"

RUN apk add --no-cache vim \
    && apk add --no-cache ca-certificates openssl \
    && apk add --no-cache bash \
    && apk add --no-cache curl jq \
    && update-ca-certificates

#RUN wget -O /tmp/consul-template.tgz https://releases.hashicorp.com/consul-template/0.18.5/consul-template_0.18.5_linux_amd64.tgz \
#RUN wget -O /tmp/consul-template.tgz https://releases.hashicorp.com/consul-template/0.19.3/consul-template_0.19.3_linux_amd64.tgz \
RUN wget -O /tmp/consul-template.tgz https://s3.amazonaws.com/nomad-static.shout.tv/consul-template_0.19.3-with_patch_1f7c022-linux_amd64.tgz \
    && tar -xzf /tmp/consul-template.tgz \
    && rm  -f   /tmp/consul-template.tgz \
    && mv consul-template /usr/bin/

ADD  http://localhost:40080/consul-cli.sh /usr/local/bin/
COPY wds.sh /usr/local/sbin/
ADD  http://localhost:40080/dclone.sh /usr/local/sbin/
ADD  http://localhost:40080/save-certs.sh /usr/local/sbin/
ADD  http://localhost:40080/pre-launch.sh /usr/local/sbin/
ADD  http://localhost:40080/consul-template.sh /usr/local/sbin/
RUN  chmod 755 /usr/local/sbin \
  && chmod 755 /usr/local/sbin/*.sh \
  && chmod 755 /usr/local/bin/*.sh

COPY etc /dclone/etc
RUN  mkdir -p /dclone/etc/nginx/sites-enabled && \
     chmod 700 /dclone/etc/ssl/private

RUN rm -f /etc/nginx/sites-enabled/*

RUN echo "alias ll='ls -l --color=auto'" >>/root/.bashrc

VOLUME /mnt/host

ENTRYPOINT ["/bin/bash", "/usr/local/sbin/wds.sh"]
