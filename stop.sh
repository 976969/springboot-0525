#!/bin/bash

echo "========================================"
echo "  智能实训评价系统 - 停止服务"
echo "========================================"
echo ""

echo "正在停止后端服务..."
if [ -f .backend.pid ]; then
    BACKEND_PID=$(cat .backend.pid)
    kill $BACKEND_PID 2>/dev/null
    echo "✅ 后端服务已停止 (PID: $BACKEND_PID)"
    rm -f .backend.pid
else
    echo "⚠️  未找到后端进程"
fi

echo "正在停止前端服务..."
if [ -f .frontend.pid ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    kill $FRONTEND_PID 2>/dev/null
    echo "✅ 前端服务已停止 (PID: $FRONTEND_PID)"
    rm -f .frontend.pid
else
    echo "⚠️  未找到前端进程"
fi

echo ""
echo "========================================"
echo "  服务已停止"
echo "========================================"
