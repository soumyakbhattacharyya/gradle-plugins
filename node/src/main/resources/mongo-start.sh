#!/bin/bash

set -e
set -o pipefail

echo "Starting MongoDB [127.0.0.1:${mongoPort}]"
echo "mongod --version" : [`mongod --version | head -1`]
echo "mongo  --version" : [`mongo --version`]

mkdir -p ${mongoDBPath}

if [ ${mongoRunning} ];
then
    echo "Mongo [127.0.0.1:${mongoPort}] is already running"
else
    mongod --fork --port ${mongoPort} --logpath ${mongoLogpath} --dbpath ${mongoDBPath}
    sleep ${sleep}

    if [ ${mongoRunning} ];
    then
        echo "Mongo [127.0.0.1:${mongoPort}] has started"
    else
        echo "Mongo [127.0.0.1:${mongoPort}] has failed to start"
        exit 1
    fi
fi

mongo --eval "db" --port ${mongoPort}