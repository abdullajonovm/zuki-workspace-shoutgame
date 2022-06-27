FROM alpine:3.4
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Base SRD Server"

RUN apk add --no-cache vim \
    && apk add --no-cache ca-certificates openssl \
    && apk add --no-cache bash \
    && update-ca-certificates

RUN wget -O /tmp/consul-template.tgz https://s3.amazonaws.com/nomad-static.shout.tv/consul-template_0.19.3-with_patch_1f7c022-linux_amd64.tgz \
    && tar -xzf /tmp/consul-template.tgz \
    && rm  -f   /tmp/consul-template.tgz \
    && mv consul-template /usr/bin/

COPY consul-template.sh /usr/local/sbin/
RUN  chmod +x /usr/local/sbin/consul-template.sh

COPY target/srd-server /usr/local/bin/
RUN  chmod +x /usr/local/bin/srd-server
RUN  mkdir -p /etc/srd-server

RUN echo "alias ll='ls -l --color=auto'" >>/root/.bashrc

VOLUME /mnt/host

ENTRYPOINT ["/bin/bash", "/usr/local/sbin/consul-template.sh", \
            "-log-level=debug", \
            "-exec-reload-signal=SIGHUP", \
            "-exec", "srd-server -p 8080 -n 0.0.0.0 --reuse-port"]
