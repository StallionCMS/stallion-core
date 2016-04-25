Don't read this. These notes are almost certainly out of date and will just lead you astray.

Requirements
----------------------------------------

* Java 1.8u40 or above
* maven
* mysql, for database backed sites (postgres support in development)

Create a new site
----------------------------------------

* Create a new directory that will contain your site. Use the path to that directory for the "-targetPath=" parameter in the following command:

```
mvn compile exec:java -Dexec.mainClass=io.stallion.boot.MainRunner "-Dexec.args=new  -targetPath=/path/to/your/stallion/application -logLevel=FINER "
```

Run Stallion Locally for Development
----------------------------------------

```
mvn compile exec:java -Dexec.mainClass=io.stallion.boot.MainRunner "-Dexec.args=serve -autoReload -targetPath=/path/to/your/stallion/application -logLevel=FINER  -port=8090 -devMode=true -env=local"
```

Go to http://localhost:8090 and you will see your site.



Performance Profiling
--------------------------------------------------------------------------------



* From the command line, start jvisualvm: `>jvisualvm`
* Start mvn with JMX options:
```mvn compile exec:java -Dcom.sun.management.jmxremote.port=9876 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false  -Dexec.mainClass="io.stallion.boot.MainRunner" -Dexec.args="serve -targetPath=/Users/username/your-site -logLevel=INFO"```
* In VisualVM, right click on "Local", Add JMX connection, then enter the connectionas localhost:9876 (or whatever JMXREmote port you chose in the command above)
* Open the connection added above
* Click on the "Sampler" tab
* Click on the checkbox on the right to show the settings panel
* Use following entries for "Do not profile": ```java.*, javax.*,
sun.*, sunw.*, com.sun.*,
com.apple.*, apple.awt.*, apple.laf.*, org.eclipse.*, sun.*,de.odysseus.*,
org.apache.maven.*, org.codehaus.*```
* Click the button "Sample:" "CPU"
* Use apache bench to generate requests against the server: `ab -n 10000 -c 1 http://localhost:8090/` You can usually install apache bench via the apache utils package via apt-get or brew.


### optional

* Click on the "Profiler" tab
* Profile "CPU"
* Click on the checkbox on the right to show the settings panel
* Use the same exclusion rules as above
* Click the Profile: "CPU" button to start profiling


