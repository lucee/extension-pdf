cls
SET JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot
call mvn package
if %errorlevel% neq 0 exit /b %errorlevel%
set testLabels=pdf
set testFilter=
set testAdditional=d:\work\lucee-extensions\extension-pdf\tests
set testServices=mysql

SET JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot

ant -buildfile "d:\work\script-runner" -DluceeVersionQuery="7.1/alpha/light" -Dwebroot="d:\work\lucee7.1\test" -Dexecute="/bootstrap-tests.cfm" -DextensionDir="d:\work\lucee-extensions\extension-pdf\target" -Dextensions="B737ABC4-D43F-4D91-8E8E973E37C40D1B" -Ddebugger="false" -DuniqueWorkingDir="true"

