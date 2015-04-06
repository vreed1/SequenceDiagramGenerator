#!/bin/bash

cd ../bin
rm -rf output
mkdir output
rm -rf outputmb
mkdir outputmb
java -classpath ".:../lib/sdedit-4.01.jar:../lib/soot-2.5.0.jar" sequenceDiagramGenerator.UI.TestUI -a -jars /home/brian/Desktop/gradschool/apks/testout.jar -startmethod org.adblockplus.libadblockplus.JsEngine.setLogSystem -outdir outputmb
cd ../scripts
