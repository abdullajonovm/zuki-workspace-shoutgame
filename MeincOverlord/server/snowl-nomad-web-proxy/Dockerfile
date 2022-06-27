FROM scm.shoutgameplay.com:5000/wds:1.6
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Snowl Nomad Web Proxy Server"

COPY etc /dclone/etc

RUN cd /etc/nginx/sites-enabled \
    && ln -s ../sites-available/nomad_web_proxy

CMD ["-template", "/etc/nginx/sites-available/nomad_web_proxy.ctmpl:/etc/nginx/sites-available/nomad_web_proxy"]
