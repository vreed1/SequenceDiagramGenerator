
cd ..\bin
java -classpath ".;..\lib\sdedit-4.01.jar;..\lib\soot-2.5.0.jar" sequenceDiagramGenerator.UI.TestUI -c -jars ..\..\..\TestFiles\TestAnalysis\bin\Test.jar -startmethod Main.main -outfile out.pdf
java -classpath ".;..\lib\sdedit-4.01.jar;..\lib\soot-2.5.0.jar" sequenceDiagramGenerator.UI.TestUI -c -jars ..\..\..\TestFiles\TestAnalysis\bin\Test.jar -startmethod Recursion.RecMain.main -outfile out2.pdf
cd ..\scripts
