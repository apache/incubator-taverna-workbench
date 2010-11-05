@echo off

REM -src . -- put source files here
REM -compiler -- where to find javac
REM -javasource -- which JAVA version to aim for (1.5 uses generics)
REM -out -- specifies the name of the target JAR file
REM -dl -- allows download of referenced schemas

scomp -src . -compiler "%JAVA_HOME%\bin\javac.exe" -javasource 1.5 -out biocatalogue_api_classes.jar -dl http://sandbox.biocatalogue.org/2009/xml/rest/schema-v1.xsd