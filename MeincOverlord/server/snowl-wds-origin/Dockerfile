FROM scm.shoutgameplay.com:5000/wds:1.6
MAINTAINER Matt Pontius <mpontius@shout.tv>
LABEL description="Snowy Owl Web Data Store Origin Server"

RUN  cd /dclone/etc/nginx/sites-available \
  && rm shout_wds_cache shout_wms_origin \
  && cd /dclone/etc/nginx/sites-enabled \
  && ln -s ../sites-available/shout_wds_origin
