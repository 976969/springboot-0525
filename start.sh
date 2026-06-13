#!/bin/bash

echo "========================================"
echo "  智能实训评价系统 - 一键启动脚本"
echo "========================================"
echo ""

echo "[1/4] 检查环境..."
if ! command -v java &> /dev/null; then
    echo "❌ 未检测到 Java，请先安装 JDK 1.8 或 17+"
    exit 1
fi
echo "✅ Java 环境正常"

if ! command -v mvn &> /dev/null; then
    echo "❌ 未检测到 Maven，请先安装 Maven 3.6+"
    exit 1
fi
echo "✅ Maven 环境正常"

if ! command -v node &> /dev/null; then
    echo "❌ 未检测到 Node.js，请先安装 Node.js 16+"
    exit 1
fi
echo "✅ Node.js 环境正常"

if ! command -v mysql &> /dev/null; then
    echo "⚠️  未检测到 MySQL 客户端，请确保 MySQL 服务已启动"
else
    echo "✅ MySQL 客户端正常"
fi

echo ""
echo "[2/4] 检查数据库..."
mysql -u root -e "USE training_eval;" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "⚠️  数据库 training_eval 不存在"
    echo "请先执行: mysql -u root -p < sql/init.sql"
    read -p "是否继续启动？(y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✅ 数据库正常"
fi

echo ""
echo "[3/4] 启动后端服务..."
nohup mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo "✅ 后端已启动 (PID: $BACKEND_PID)"
echo "   日志文件: backend.log"

echo "等待后端启动..."
sleep 8

echo ""
echo "[4/4] 启动前端服务..."
cd frontend
nohup npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..
echo "✅ 前端已启动 (PID: $FRONTEND_PID)"
echo "   日志文件: frontend.log"

echo ""
echo "========================================"
echo "  启动完成！"
echo "========================================"
echo ""
echo "📱 访问地址：http://localhost:5173"
echo ""
echo "👤 测试账号："
echo "   管理员：admin / admin123"
echo "   教  师：teacher01 / 123456"
echo "   学  生：student01 / 123456"
echo ""
echo "💡 提示："
echo "   1. 查看后端日志: tail -f backend.log"
echo "   2. 查看前端日志: tail -f frontend.log"
echo "   3. 停止服务: kill $BACKEND_PID $FRONTEND_PID"
echo ""

# 保存 PID 到文件
echo "$BACKEND_PID" > .backend.pid
echo "$FRONTEND_PID" > .frontend.pid

echo "按 Ctrl+C 退出（服务将在后台继续运行）"
tail -f frontend.log
