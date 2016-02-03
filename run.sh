export MAVEN_OPTS="-Xmx2g -Xms2g"
mvn compile
mvn exec:java -Dexec.mainClass="de.hpi.ir.bingo.SearchEngineTest"
