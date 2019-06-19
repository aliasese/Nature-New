@echo off & setlocal enabledelayedexpansion
SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0_211\jdk1.8.0_211
set CLASSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;
set PATH=%JAVA_HOME%\bin;

set APP="nature-data-import-fat"

cd ../
goto start_normal

:start_normal
java -jar -Xms128m -Xmx256m -Xmn128m -XX:SurvivorRatio=4 -XX:NewRatio=2 -XX:PermSize=64m -XX:MaxPermSize=128m -Xss256k -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods %APP%.jar

@pause