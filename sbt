#!/bin/sh
# attempts to execute ~/.sbtrc then <project>/.sbtrc
java \
  -Xms512M \
  -Xmx1536M \
  -Xss1M \
  -XX:+CMSClassUnloadingEnabled \
  -XX:MaxPermSize=384M \
  -jar `dirname $0`/sbt-launch.jar \
  "$@"
