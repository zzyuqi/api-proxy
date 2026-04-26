@echo off
chcp 65001 >nul
echo ========================================
echo   API 中转系统 - 启动脚本
echo ========================================

set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.6.7-hotspot
set MAVEN_HOME=D:\idea\IntelliJ IDEA 2024.3.5\plugins\maven\lib\maven3

echo 正在启动服务...
echo.
echo 管理API: http://localhost:8080/api/admin
echo 中转API: http://localhost:8080/api/proxy/{path}
echo Swagger:  http://localhost:8080/swagger-ui.html
echo H2控制台: http://localhost:8080/h2-console
echo.
echo 首次使用请先POST /api/admin/init 创建管理员账号
echo.

"%JAVA_HOME%\bin\java" -jar target\api-proxy-1.0.0.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo JAR文件不存在，尝试通过Maven启动...
    call "%MAVEN_HOME%\bin\mvn" spring-boot:run
)

pause
