function alexandria {
  java -jar c:/lib/alexandria.jar $* |sed -e "s/java -jar alexandria.jar/alexandria/";
}

function alexandria-rebuild {
  (cd ~/workspaces/alexandria-markup/ && mvn install && cd ~/workspaces/alexandria-markup-server/ && mvn package && cp alexandria-markup-lmnl-server/target/alexandria-markup-lmnl-server-2.0-SNAPSHOT.jar /c/lib/alexandria.jar;)
}
