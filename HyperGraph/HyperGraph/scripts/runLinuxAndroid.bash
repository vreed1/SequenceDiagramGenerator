#!/bin/bash

cd ../bin
rm -rf output
mkdir output
rm -rf outputsad
mkdir outputsad
java -classpath ".:../lib/sdedit-4.01.jar:../lib/soot-2.5.0.jar" sequenceDiagramGenerator.UI.TestUI -a -jars /home/brian/Desktop/gradschool/apks/testout.jar -startmethod org.adblockplus.android.AdBlockPlus.showAppDetails -outdir outputsad
cd ../scripts
