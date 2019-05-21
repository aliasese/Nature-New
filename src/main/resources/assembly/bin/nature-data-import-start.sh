#!/usr/bin/env bash

set -e

APP="nature-data-import"

PIDS=`ps -ef | grep java | grep "$APP" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $APP already started!"
    echo "PID: $PIDS"
    exit 1
fi

java -jar -Xms128m -Xmx256m -Xmn128m -XX:SurvivorRatio=4 -XX:NewRatio=2 -XX:PermSize=64m -XX:MaxPermSize=128m -Xss256k -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods %APP%.jar