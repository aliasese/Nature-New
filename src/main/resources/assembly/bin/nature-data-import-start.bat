@echo off & setlocal enabledelayedexpansion

set APP="nature-data-import-fat"

cd ../
goto start_normal

:start_normal
java -jar -Xms128m -Xmx256m -Xmn128m -XX:SurvivorRatio=4 -XX:NewRatio=2 -XX:PermSize=64m -XX:MaxPermSize=128m -Xss256k -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods %APP%.jar

goto end

@pause