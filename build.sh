mvn clean compile package install -DskipTests
cat self-exec.sh target/stallion-core-.1-SNAPSHOT-jar-with-dependencies.jar > stallion
chmod 700 stallion
cp stallion /usr/local/bin/stallion
chmod 700 /usr/local/bin/stallion
cp target/stallion-core-.1-SNAPSHOT-jar-with-dependencies.jar /usr/local/bin/stallion.jar



