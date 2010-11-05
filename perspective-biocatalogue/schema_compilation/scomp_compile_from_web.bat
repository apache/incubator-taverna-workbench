@echo off

REM -src . -- put source files here
REM -srconly -- only sources, no compiling of java classes, no jar bundling
REM -compiler -- where to find javac
REM -javasource -- which JAVA version to aim for (1.5 uses generics)
REM -dl -- allows download of referenced schemas

scomp -src . -srconly -compiler "%JAVA_HOME%\bin\javac.exe" -javasource 1.5 -dl http://sandbox.biocatalogue.org/2009/xml/rest/schema-v1.xsd