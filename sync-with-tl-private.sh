#!/bin/sh

rsync --delete -arvzi ../tl-sbt-plugins/src/main/scala/trafficland/opensource/ src/main/scala/trafficland/opensource/
rsync --delete -arvzi ../tl-sbt-plugins/src/test/scala/trafficland/opensource/ src/test/scala/trafficland/opensource/
rsync --delete -arvzi ../tl-sbt-plugins/bin/ bin/