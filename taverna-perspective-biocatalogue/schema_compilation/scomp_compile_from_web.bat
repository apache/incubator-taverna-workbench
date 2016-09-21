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

REM -src . -- put source files here
REM -srconly -- only sources, no compiling of java classes, no jar bundling
REM -compiler -- where to find javac
REM -javasource -- which JAVA version to aim for (1.5 uses generics)
REM -dl -- allows download of referenced schemas

scomp -src . -srconly -compiler "%JAVA_HOME%\bin\javac.exe" -javasource 1.5 -dl http://www.biocatalogue.org/2009/xml/rest/schema-v1.xsd