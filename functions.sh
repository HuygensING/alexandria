function alexandria {
   java -jar c:/lib/alexandria.jar $* |sed -e "s/java -jar c:\/lib\/alexandria.jar/alexandria/";
 }

function alexandria-rebuild {
   (cd ~/workspaces/alexandria-markup/alexandria-markup && maven install && cd ~/workspaces/alexandria-markup/alexandria-markup-server/ && maven package && cp alexandria-markup-lmnl-server/target/alexandria-markup-lmnl-server-2.0-SNAPSHOT.jar /c/lib/alexandria.jar;)
 }
