#!/usr/bin/env bash
function alexandria {
  java -jar c:/lib/alexandria.jar $* |sed -e "s/java -jar alexandria.jar/alexandria/";
}

function alexandria-rebuild {
  (cd ~/workspaces/alexandria-markup/ && mvn install && cd ~/workspaces/alexandria-markup-server/ && mvn package && cp alexandria-markup-server/target/alexandria-markup-server-2.0-SNAPSHOT.jar /c/lib/alexandria.jar;)
}
