@echo off

goto LicenseEnd

	Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership. The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
:LicenseEnd

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