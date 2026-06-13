@echo off
chcp 65001 >nul
echo ========================================
echo   智能实训评价系统 - 停止服务
echo ========================================
echo.

echo 正在停止后端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    taskkill /F /PID %%a >nul 2>&1
    echo ✅ 后端服务已停止 (PID: %%a)
)

echo 正在停止前端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173') do (
    taskkill /F /PID %%a >nul 2>&1
    echo ✅ 前端服务已停止 (PID: %%a)
)

echo.
echo ========================================
echo   服务已停止
echo ========================================
pause
