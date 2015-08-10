
cd ..\bin

java -Xmx4096m -classpath ".;..\lib\sdedit-4.01.jar;..\lib\soot-2.5.0.jar;..\lib\json-simple-1.1.1.jar" sequenceDiagramGenerator.UI.TestUI -aa -jars D:\gradschool\supratik\findbugs-3.0.1build\lib\findbugs.jar -startswith edu.umd.cs.findbugs.config.CommandLine;edu.umd.cs.findbugs.FindBugsCommandLine;edu.umd.cs.gui2 -outdir D:\gradschool\supratik\outdir3
cd ..\scripts
