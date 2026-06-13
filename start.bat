@echo off
chcp 65001 >nul
echo ========================================
echo   智能实训评价系统 - 一键启动脚本
echo ========================================
echo.

echo [1/3] 检查环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Java，请先安装 JDK 1.8 或 17+
    pause
    exit /b 1
)
echo ✅ Java 环境正常

mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Maven，请先安装 Maven 3.6+
    pause
    exit /b 1
)
echo ✅ Maven 环境正常

node -v >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Node.js，请先安装 Node.js 16+
    pause
    exit /b 1
)
echo ✅ Node.js 环境正常

echo.
echo [2/3] 启动后端服务...
start "后端服务" cmd /k "mvn spring-boot:run"
echo ✅ 后端启动中...
timeout /t 5 /nobreak >nul

echo.
echo [3/3] 启动前端服务...
cd frontend
start "前端服务" cmd /k "npm run dev"
cd ..
echo ✅ 前端启动中...

echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo 📱 访问地址：http://localhost:5173
echo.
echo 👤 测试账号：
echo    管理员：admin / admin123
echo    教  师：teacher01 / 123456
echo    学  生：student01 / 123456
echo.
echo 💡 提示：
echo    1. 请确保 MySQL 已启动
echo    2. 请确保已执行 sql/init.sql
echo    3. 两个命令行窗口请勿关闭
echo.
pause
