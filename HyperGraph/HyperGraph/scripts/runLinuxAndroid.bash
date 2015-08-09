#!/bin/bash

cd ../bin
rm -rf output
mkdir output
rm -rf outputsad
mkdir outputsad
java -classpath ".:../lib/sdedit-4.01.jar:../lib/soot-2.5.0.jar:../lib/json-simple-1.1.1.jar" sequenceDiagramGenerator.UI.TestUI -a -jars /home/brian/Desktop/gradschool/apks/testout.jar -startmethod org.adblockplus.android.AdblockPlus.showAppDetails(android.content.Context) -outdir outputsad
cd ../scripts
