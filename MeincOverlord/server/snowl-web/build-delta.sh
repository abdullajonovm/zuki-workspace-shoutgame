#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

if [ -e "sync.sh" ]; then
    ./sync.sh
fi

if [ "$1" == "-t" ]; then
    DOCKER_IMAGE_TAG="$2"
    shift 2
elif [ -f docker_image_tag ]; then
    DOCKER_IMAGE_TAG="$(<docker_image_tag)"
elif [ -x ../docker_image_tag.sh ]; then
    set -e; DOCKER_IMAGE_TAG="$(../docker_image_tag.sh)"; set +e
else
    echo "Missing -t parameter or docker_image_tag file or ../docker_image_tag.sh script" >&2
    exit 1
fi

IFS=$'\n'
for image_line in $(docker images --format "{{.Repository}} {{.Tag}} {{.ID}} {{.CreatedAt}}"); do
    image_tag="$(awk '{print $1 ":" $2}' <<<"$image_line")"
    if [ "$image_tag" == "$DOCKER_IMAGE_TAG" ]; then
        DOCKER_IMAGE_ID="$(awk '{print $3}' <<<"$image_line")"
        DOCKER_IMAGE_DATE="$(awk '{print $4 " " $5 " " $6}' <<<"$image_line")"
        break
    fi
done
unset IFS

if [ ! "$DOCKER_IMAGE_ID" -o ! "$DOCKER_IMAGE_DATE" ]; then
    echo "Cannot find existing Docker image $DOCKER_IMAGE_TAG" >&2
    exit 1
fi

DOCKER_TMP_DIR="docker_tmp"
mkdir -p "$DOCKER_TMP_DIR"

DOCKER_IMAGE_DATE_PATH="$DOCKER_TMP_DIR/docker_image_date"
touch -d "$DOCKER_IMAGE_DATE" "$DOCKER_IMAGE_DATE_PATH"

declare -a DOCKERFILE_COPY_LINES

DOCKERFILE_COPY_ARGS=
DOCKERFILE_COPY_PATHS=
for file in $(find www -type f -newer "$DOCKER_IMAGE_DATE_PATH"); do
    DOCKERFILE_COPY_LINES+=( "COPY [\"$file\", \"/dclone_delta/var/$file\"]" )
done

DOCKERFILE_COPY_ARGS=
for file in "web.nginx.conf.ctmpl"; do
    if [ "$file" -nt "$DOCKER_IMAGE_DATE_PATH" ]; then
        DOCKERFILE_COPY_ARGS="${DOCKERFILE_COPY_ARGS}${DOCKERFILE_COPY_ARGS:+", "}\"$file\""
    fi
done
if [ "$DOCKERFILE_COPY_ARGS" ]; then
    DOCKERFILE_COPY_LINES+=( "COPY [$DOCKERFILE_COPY_ARGS, \"/dclone_delta/etc/nginx/sites-available/\"]" )
fi

if [ ! "$DOCKERFILE_COPY_LINES" ]; then
    echo "No file changes found" >&2
    rm -rf "$DOCKER_TMP_DIR" "$DOCKER_IMAGE_DATE_PATH"
    exit 0
fi

DELTA_DOCKERFILE_PATH="$DOCKER_TMP_DIR/delta_dockerfile"

echo "FROM $DOCKER_IMAGE_TAG as builder" >"$DELTA_DOCKERFILE_PATH"

for copy_line in "${DOCKERFILE_COPY_LINES[@]}"; do
    echo "$copy_line" >>"$DELTA_DOCKERFILE_PATH"
done

echo "RUN find /dclone_delta/etc/nginx -not -user nginx -exec chown nginx:nginx {} + 2>/dev/null || true" >>"$DELTA_DOCKERFILE_PATH"
echo "RUN find /dclone_delta/var/www -not -user nginx -exec chown nginx:nginx {} + 2>/dev/null || true"   >>"$DELTA_DOCKERFILE_PATH"

echo "FROM $DOCKER_IMAGE_TAG" >>"$DELTA_DOCKERFILE_PATH"
echo "COPY --from=builder /dclone_delta /dclone/" >>"$DELTA_DOCKERFILE_PATH"

docker build -t "$DOCKER_IMAGE_TAG" -f "$DELTA_DOCKERFILE_PATH" .

rm -rf "$DOCKER_TMP_DIR" "$DOCKER_IMAGE_DATE_PATH"
