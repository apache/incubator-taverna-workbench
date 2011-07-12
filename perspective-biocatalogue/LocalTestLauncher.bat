@echo off
echo.
echo.
del biocatalogue-perspective-local-launch.jar
echo Deleted old JAR file if it was there.
cd target\classes
jar cfM ..\..\biocatalogue-perspective-local-launch.jar *.*
cd ..\..
echo JAR assembly done, launching app...

java -cp .;.\biocatalogue-perspective-local-launch.jar;c:/Users/Sergey/.m2/repository/net/sf/taverna/t2/workbench/ui-api/0.2/ui-api-0.2.jar;c:\Users\Sergey\.m2\repository\net\sf\taverna\t2\lang\ui\1.0\ui-1.0.jar;c:\Users\Sergey\.m2\repository\net\sf\taverna\t2\ui-components\workflow-view\1.0\workflow-view-1.0.jar;c:\Users\Sergey\.m2\repository\net\sf\taverna\t2\ui-activities\wsdl-activity-ui\0.7\wsdl-activity-ui-0.7.jar;C:\Users\Sergey\.m2\repository\log4j\log4j\1.2.13\log4j-1.2.13.jar;C:\Users\Sergey\.m2\repository\net\sf\taverna\t2\workbench\commons-icons\0.2\commons-icons-0.2.jar;c:\Users\Sergey\.m2\repository\BrowserLauncher2\BrowserLauncher2\1.3\BrowserLauncher2-1.3.jar;C:\Users\Sergey\.m2\repository\jdom\jdom\1.0\jdom-1.0.jar;"c:\Program Files\Java\xmlbeans-2.4.0\lib\xbean.jar";"c:\Program Files\Java\xmlbeans-2.4.0\lib\jsr173_1.0_api.jar";.\lib\lablib-checkboxtree-3.1.jar;.\lib\core-renderer.jar;.\lib\commons-lang-2.4.jar net.sf.taverna.t2.ui.perspectives.biocatalogue.TestJFrameForLocalLaunch

del biocatalogue-perspective-local-launch.jar
echo Cleanup done - deleted old the JAR file that was used for the current launch.