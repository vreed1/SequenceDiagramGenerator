
cd ..\bin
java -classpath ".;..\lib\sdedit-4.01.jar;..\lib\soot-2.5.0.jar;..\lib\json-simple-1.1.1.jar;" sequenceDiagramGenerator.UI.TestUI -t -testpath ..\test\windowstest\ -testjson testinfo.json > testoutput.txt
cd ..\scripts
