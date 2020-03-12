<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->


## Taverna Project Retired

> tl;dr: The Taverna code base is **no longer maintained** 
> and is provided here for archival purposes.

From 2014 till 2020 this code base was maintained by the 
[Apache Incubator](https://incubator.apache.org/) project _Apache Taverna (incubating)_
(see [web archive](https://web.archive.org/web/20200312133332/https://taverna.incubator.apache.org/)
and [podling status](https://incubator.apache.org/projects/taverna.html)).

In 2020 the Taverna community 
[voted](https://lists.apache.org/thread.html/r559e0dd047103414fbf48a6ce1bac2e17e67504c546300f2751c067c%40%3Cdev.taverna.apache.org%3E)
to **retire** Taverna as a project and withdraw the code base from the Apache Software Foundation. 

This code base remains available under the Apache License 2.0 
(see _License_ below), but is now simply called 
_Taverna_ rather than ~~Apache Taverna (incubating)~~.

While the code base is no longer actively maintained, 
Pull Requests are welcome to the 
[GitHub organization taverna](http://github.com/taverna/), 
which may infrequently be considered by remaining 
volunteer caretakers.


### Previous releases

Releases 2015-2018 during incubation at Apache Software Foundation
are available from the ASF Download Archive <https://archive.apache.org/dist/incubator/taverna/>

Releases 2014 from the University of Manchester are on BitBucket <https://bitbucket.org/taverna/>

Releases 2009-2013 from myGrid are on LaunchPad <https://launchpad.net/taverna/>

Releases 2003-2009 are on SourceForge <https://sourceforge.net/projects/taverna/files/taverna/>

Binary JARs for Taverna are available from 
Maven Central <https://repo.maven.apache.org/maven2/org/apache/taverna/>
or the myGrid Maven repository <https://repository.mygrid.org.uk/>



# Taverna Workbench

Desktop UI for designing and running
[Taverna](https://web.archive.org/web/*/https://taverna.incubatorTT.apache.org/) workflows.

Note that this module relies on other
[Taverna modules](https://web.archive.org/web/*/https://taverna.incubatorTT.apache.org/code) for
the actual workflow execution.


## License

(c) 2007-2014 University of Manchester

(c) 2014-2020 Apache Software Foundation

This product includes software developed at The
[Apache Software Foundation](http://www.apache.org/).

Licensed under the
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0), see the file
[LICENSE](LICENSE) for details.

The file [NOTICE](NOTICE) contain any additional attributions and
details about embedded third-party libraries and source code.


# Contribute

Any contributions received are assumed to be covered by the [Apache License
2.0](https://www.apache.org/licenses/LICENSE-2.0). 


## Prerequisites

* Java 1.7 or newer (tested with OpenJDK 1.8)
* [Apache Maven](https://maven.apache.org/download.html) 3.2.5 or newer (older
  versions probably also work)


# Building

To build, use

    mvn clean install

This will build each module and run their tests.


## Building on Windows

If you are building on Windows, ensure you unpack this source code
to a folder with a [short path name](http://stackoverflow.com/questions/1880321/why-does-the-260-character-path-length-limit-exist-in-windows) 
lenght, e.g. `C:\src` - as 
Windows has a [limitation on the total path length](https://msdn.microsoft.com/en-us/library/aa365247%28VS.85%29.aspx#maxpath) 
which might otherwise
prevent this code from building successfully.


## Skipping tests

To skip the tests (these can be timeconsuming), use:

    mvn clean install -DskipTests


If you are modifying this source code independent of the
Taverna project, you may not want to run the
[Rat Maven plugin](https://creadur.apache.org/rat/apache-rat-plugin/)
that enforces Apache headers in every source file - to disable it, try:

    mvn clean install -Drat.skip=true
