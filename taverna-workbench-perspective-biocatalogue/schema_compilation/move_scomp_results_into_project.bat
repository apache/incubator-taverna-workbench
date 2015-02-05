@echo off

echo This will replace the JAR file with compiled API classes
pause
del ..\lib\biocatalogue_api_classes.jar
move biocatalogue_api_classes.jar ..\lib\
echo JAR file replaced
echo.

REM replace the sources of API classes
echo This will delete *ALL* files in \src\main\java\org\biocatalogue
echo                                 \src\main\java\org\purl
echo                                 \src\main\java\org\w3
pause

rd /S /Q ..\src\main\java\org\biocatalogue
rd /S /Q ..\src\main\java\org\purl
rd /S /Q ..\src\main\java\org\w3

move /Y org\biocatalogue ..\src\main\java\org
move /Y org\purl ..\src\main\java\org
move /Y org\w3 ..\src\main\java\org
rd org

echo Sources of API classes replaced
echo.

echo Done!
pause