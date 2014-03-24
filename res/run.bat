@ECHO OFF

cd ..

java -server -Xms256m -Xmx1g -Djava.ext.dirs=lib -jar build/dist/Csv2Lucene-0.0.1.jar res/test
