#!/bin/bash

runOnce ()
{
	echo "***START$3***"
    rm -rf ../outputmany$3
    mkdir ../outputmany$3
	java -classpath ".:../lib/sdedit-4.01.jar:../lib/soot-2.5.0.jar:../lib/json-simple-1.1.1.jar" sequenceDiagramGenerator.UI.TestUI -aa -jars $1 -classpath /home/brian/Desktop/gradschool/hypergraph/android.jar -startswith $2 -outdir ../outputmany$3 -debugfile /home/brian/Desktop/gradschool/hypergraph/debugout$3.txt -perffile /home/brian/Desktop/gradschool/hypergraph/perfout.csv -kvalue 1000 -rmode bymessages
}

cd ../bin

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/APG-1.0.9-release.jar org.thialfihar.android.apg 1 > out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/ConnectBot-git-master-2013-11-01_20-34-19.jar org.connectbot 2 >> out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/CSipSimple-latest-armeabi-v7a.jar com.csipsimple.service 3 >> out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/fennec-37.0.2.en-US.android-arm.jar com.squareup.picasso 4 >> out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/jitsi-android-258.jar org.jitsi.service 5 >> out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/k9-4.903-release.jar com.fsck.k9.activity.fragment 6 >> out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/linphone-android-2.0.2.jar org.linphone 7 >> out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/orbot-latest.jar a.a.a.a 8 >> out.txt 2>&1

runOnce /home/brian/Desktop/gradschool/hypergraph/apks/Sipdroid-3.4.jar org.sipdroid 9 >> out.txt 2>&1

cd ../scripts
