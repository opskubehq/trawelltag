# trawelltag
Sample Project to consume trawelltag's CreatePolicy API

# Important
Please follow *AesCipherWithUnirest.java* for working code, everything else(s) are experiments
# Steps to run
* cd trawelltag
* mvn clean install -Dmaven.test.skip=true package
* mvn exec:java -Dexec.mainClass=com.trawelltag.insurance.AesCipherWithUnirest
