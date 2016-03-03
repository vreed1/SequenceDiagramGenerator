#!/bin/bash

cd ../bin
rm -rf output
mkdir output
rm -rf outputmb
mkdir outputmb
java -classpath ".:../lib/sdedit-4.01.jar:../lib/soot-2.5.0.jar:../lib/json-simple-1.1.1.jar" sequenceDiagramGenerator.UI.TestUI -c -jars ../../../TestFiles/TestAnalysis/bin/Test.jar -startmethod "Main.main(java.lang.String[])" -outfile out.pdf
cd ../scripts
