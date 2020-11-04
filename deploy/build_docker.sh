#!/usr/bin/env bash

APP_NAME=config-client
#TAG_VERSION=${TAG_VERSION:-latest}


#GITHUB_ENV=/config/gacc.ini
#docker run -v /tmp/config:/config  -ti --rm $APP_NAME /gacc \
#    $CONFIGURATION_SERVER_USERNAME \
#    $CONFIGURATION_SERVER_PASSWORD \
#    deployment \
#    development \
#    http://configuration.development.bootifulpodcast.online \
#    $GITHUB_ENV


docker build -t $APP_NAME .
image_id=$(docker images -q $APP_NAME)
docker tag "${image_id}" gcr.io/${GCLOUD_PROJECT}/${APP_NAME}:latest
docker push gcr.io/${GCLOUD_PROJECT}/${APP_NAME}:latest

if [[ -n "${TAG_VERSION}"  ]] ;
then
    docker tag "${image_id}" gcr.io/${GCLOUD_PROJECT}/${APP_NAME}:${TAG_VERSION}
    docker push gcr.io/${GCLOUD_PROJECT}/${APP_NAME}:${TAG_VERSION}
fi
