
cd ..\bin
java -classpath ".;..\lib\sdedit-4.01.jar;..\lib\soot-2.5.0.jar;..\lib\json-simple-1.1.1.jar;" sequenceDiagramGenerator.UI.TestUI -aa -jars D:\gradschool\hypergraph\apks\testout.jar -startswith org.adblockplus -outdir outputtest > output.txt
cd ..\scripts
