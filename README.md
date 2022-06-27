## Build Process ##

```
server/nginx-lua$ ./build-local.s # Base Web Data Store Origin Server

```

## Patched Source Code and Build/Docker scripts ##

### Resource Scripts ###

They are located in each Dockerfile's companion **resources** folder .

### Dockerfiles ###

1. mrsoa-server/docker/Dockerfile-local
2. MeincOverlord/server/snowl-collector/Dockerfile-local
3. MeincOverlord/server/snowl-mysql/Dockerfile-local
4. MeincOverlord/server/snowl-socketio/Dockerfile-local
5. server/nginx-lua/Dockerfile-local
6. server/wds/docker/Dockerfile-local
7. MeincOverlord/server/snowl-nomad-web-proxy/Dockerfile-local
8. MeincOverlord/server/snowl-wds-origin/Dockerfile-local
9. MeincOverlord/server/snowl-web/Dockerfile-local
10. MeincOverlord/server/snowl-wms-origin/Dockerfile-local
11. server/srd-server/docker/Dockerfile-local
12. MeincOverlord/server/snowl-srd-server/Dockerfile-local

### Build Scripts ###

1. mrsoa-server/docker/build-local.sh
2. MeincOverlord/server/snowl-collector/build-local.sh
3. MeincOverlord/server/snowl-mysql/build-local.sh
4. MeincOverlord/server/snowl-socketio/build-local.sh
5. server/nginx-lua/build-local.sh
6. server/wds/docker/build-local.sh
7. MeincOverlord/server/snowl-nomad-web-proxy/build-local.sh
8. MeincOverlord/server/snowl-wds-origin/build-local.sh
9. MeincOverlord/server/snowl-web/build-local.sh
10. MeincOverlord/server/snowl-wms-origin/build-local.sh
11. server/srd-server/docker/build-local.sh
12. MeincOverlord/server/snowl-srd-server/build-local.sh

## Environment Variables ##
1. DEV_SUDO_PASS: Some scripts require the sudo passord. In order to avoid linking it (aka hardcode) in scripts, please stored in this env variable.

## Adopting Minimalistic Ubuntu Image for SRD-Server ##

Since I faced unsurmountable issues when buildging srd-server from alpine:3.4, I switched to ubuntu:18.04 minimalistic image as suggested
[here](https://techinplanet.com/docker-image-with-alpine-linux-an-executable-file-is-definitely-there-but-cannot-be-found-while-trying-to-execute/).


## Server Recovery Log ###

This recovery is a guidance for DevOps as it teaches the bootstrap and shutdown order.

### (Wed mar 30th 2022) snowl-web  ###

Replaced wds:1.6 with http:2.4 base image.

### snowl-mysql ###
### snowl-wds-origin ###
### snowl-wms-origin ###
### snowl-socketio ###
### snowl-collector ###
### snowl-srd-server ###
### #snowl-nomad-web-proxy ###