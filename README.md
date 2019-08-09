# trawelltag
Sample Project to consume trawelltag's CreatePolicy API

# Steps to run
* cd trawelltag
* mvn clean install -Dmaven.test.skip=true package
* mvn exec:java -Dexec.mainClass=com.trawelltag.insurance.AesCipherWithUnirest
