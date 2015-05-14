#!/bin/bash

runOnce ()
{
	echo "***START$3***"
    rm -rf ../../../outputmany$3
    rm -rf ../../../outputmany$3temp
    mkdir ../../../outputmany$3
	java -classpath ".:../lib/sdedit-4.01.jar:../lib/soot-2.5.0.jar:../lib/json-simple-1.1.1.jar" sequenceDiagramGenerator.UI.TestUI -aa -jars $1 -classpath /home/brian/Desktop/gradschool/hypergraph/android.jar -startswith $2 -outdir ../../../outputmany$3 -debugfile /home/brian/Desktop/gradschool/hypergraph/dbgout/debugout$3.txt -perffile /home/brian/Desktop/gradschool/hypergraph/newperf2/perfout$3_$4.csv -kvalue 20000 -rmode bymessages
}


runSet()
{
	echo "SET $1"
	echo **newrun** >> out.txt
	echo "$1_1"
	runOnce /home/brian/Desktop/gradschool/hypergraph/testout.jar org.adblockplus.android $1 0 >> out.txt 2>&1
	
	echo "$1_2"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/APG-1.0.9-release.jar org.thialfihar.android.apg.ui $1 1 >> out.txt 2>&1
	
	echo "$1_3"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/ConnectBot-git-master-2013-11-01_20-34-19.jar org.connectbot $1 2 >> out.txt 2>&1
	
	echo "$1_4"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/CSipSimple-latest-armeabi-v7a.jar com.csipsimple.service $1 3 >> out.txt 2>&1
	
	echo "$1_5"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/fennec-37.0.2.en-US.android-arm.jar com.squareup.picasso $1 4 >> out.txt 2>&1
	
	
	
	echo "$1_6"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/jitsi-android-258.jar org.jitsi.service $1 5 >> out.txt 2>&1
	
	echo "$1_7"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/k9-4.903-release.jar com.fsck.k9.service $1 6 >> out.txt 2>&1
	
	echo "$1_8"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/linphone-android-2.0.2.jar org.linphone.core $1 7 >> out.txt 2>&1
	
	echo "$1_9"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/orbot-latest.jar a.a.a.a $1 8 >> out.txt 2>&1
	
	echo "$1_10"
	runOnce /home/brian/Desktop/gradschool/hypergraph/apks/Sipdroid-3.4.jar org.sipdroid.net $1 9 >> out.txt 2>&1
	
	
	
	echo "***newset" >> out.txt
	
	echo "$1_11"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/AcDisplay-v3.6.1-release-dex2jar.jar com.achep.acdisplay.services $1 10 >> out.txt 2>&1
	
	echo "$1_12"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/ac-Stopwatch_Timer-2.1.2beta-dex2jar.jar com.achep.stopwatch $1 11 >> out.txt 2>&1
	
	echo "$1_13"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/Active_Notify_v1.42-dex2jar.jar com.aky.peek.notification $1 12 >> out.txt 2>&1
	
	echo "$1_14"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/AdAway-release_Build-Apr.09.2015-dex2jar.jar org.adaway $1 13 >> out.txt 2>&1
	
	echo "$1_15"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/AppOps-dex2jar.jar com.ssrij.appops $1 14 >> out.txt 2>&1
	
	
	
	echo "$1_16"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/BBDroidUnlocker-dex2jar.jar ir.irtci $1 15 >> out.txt 2>&1
	
	echo "$1_17"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/BetterBatteryStats_xdaedition_2.1.0.0_B3-dex2jar.jar com.asksven.betterbatterystats $1 16 >> out.txt 2>&1
	
	echo "$1_18"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/BetterWifiOnOff_2.1.0.0-dex2jar.jar com.asksven.betterwifionoff.data $1 17 >> out.txt 2>&1
	
	echo "$1_19"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/ColorClock-dex2jar.jar com.brianco.colorclock $1 18 >> out.txt 2>&1
	
	echo "$1_20"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/com.amaze.filemanager_10-dex2jar.jar com.amaze.filemanager.adapters $1 19 >> out.txt 2>&1
	
	
	
	echo "$1_21"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/CompleteLinuxInstallerv2-8-dex2jar.jar com.zpwebsites.linuxonandroid $1 20 >> out.txt 2>&1
	
	echo "$1_22"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/cpuspyplus_realgpp_0.5.60+-dex2jar.jar com.cpuspy $1 21 >> out.txt 2>&1
	
	echo "$1_23"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/DeskClock-dex2jar.jar com $1 22 >> out.txt 2>&1
	
	echo "$1_24"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/FifteenPuzzle-2.1-dex2jar.jar com $1 23 >> out.txt 2>&1
	
	echo "$1_25"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/Fontster-1.2-dex2jar.jar com.chromium.fontinstaller $1 24 >> out.txt 2>&1
	
	
	echo "$1_26"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/HaloShortcuts_v1_021213-dex2jar.jar com $1 25 >> out.txt 2>&1
	
	echo "$1_27"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/HeadsUp-v3.1-release-dex2jar.jar com.achep.headsup $1 26 >> out.txt 2>&1
	
	echo "$1_28"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/JellyBeanClock-dex2jar.jar com $1 27 >> out.txt 2>&1
	
	echo "$1_29"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/MockGPSPath.1.2.1-dex2jar.jar com.rc $1 28 >> out.txt 2>&1
	
	echo "$1_30"
	runOnce /home/brian/Desktop/gradschool/hypergraph/newapkjars/RootVerifier-Beta-dex2jar.jar com $1 29 >> out.txt 2>&1
}


cd ../bin

for i in `seq 0 29`;
do
 runSet $i
done

cd ../scripts
