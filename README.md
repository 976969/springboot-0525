# 培训评估管理系统

基于 Spring Boot + Vue 3 的培训评估管理系统,支持管理员、教师、学生三种角色。

## 🚀 技术栈

### 后端
- Spring Boot 2.6.13
- MyBatis + PageHelper
- Sa-Token 权限认证
- MySQL 5.0
- Druid 连接池

### 前端
- Vue 3 (Composition API)
- Element Plus
- Axios
- Vue Router
- Vite

## 📋 功能特性

- ✅ 多角色权限管理(管理员/教师/学生)
- ✅ 课程管理(增删改查、分页)
- ✅ 培训任务管理
- ✅ 在线评价与指标管理
- ✅ 报表中心
- ✅ 考勤管理
- ✅ 成果上传与下载
- ✅ 大模型AI辅助(阿里云通义千问)

## 🛠️ 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 5.0+

### 1. 数据库配置

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE training_eval DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 导入初始化脚本
mysql -u root -p training_eval < sql/init.sql
```

### 2. 后端启动

```bash
# 1. 配置环境变量
cp .env.example .env
# 编辑 .env 文件,填写数据库密码和API密钥

# 2. 启动后端
cd springboot_0525
mvn spring-boot:run

# 后端运行在: http://localhost:8080/api
```

### 3. 前端启动

```bash
# 启动前端
cd frontend
npm install
npm run dev

# 前端运行在: http://localhost:5173
```

### 4. 访问系统

浏览器打开: http://localhost:5173

**默认账号:**
- 管理员: `admin` / `123456`
- 教师: `teacher01` / `123456`
- 学生: `student01` / `123456`

## ⚙️ 配置说明

### 环境变量(.env文件)

```env
# 数据库配置
DB_USERNAME=root
DB_PASSWORD=你的数据库密码

# 文件上传路径
UPLOAD_PATH=./uploads

# 大模型API密钥(可选)
LLM_API_KEY=你的API密钥
```

### 配置文件

主要配置在 `src/main/resources/application.yml`,敏感信息已提取到环境变量。

## 📁 项目结构

```
springboot_0525/
├── src/main/java/
│   └── com/qcby/springboot_0525/
│       ├── controller/      # 控制器
│       ├── service/         # 业务逻辑
│       ├── mapper/          # 数据访问
│       ├── entity/          # 实体类
│       ├── common/          # 公共组件
│       ├── config/          # 配置类
│       └── util/            # 工具类
├── src/main/resources/
│   ├── mapper/              # MyBatis XML
│   └── application.yml      # 配置文件
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── views/           # 页面
│   │   ├── components/      # 组件
│   │   ├── router/          # 路由
│   │   ├── stores/          # 状态管理
│   │   └── utils/           # 工具函数
│   └── package.json
└── sql/                     # 数据库脚本
```

## 🔐 安全说明

- ⚠️ **不要提交 `.env` 文件到Git**
- ⚠️ **定期更换API密钥**
- ⚠️ **生产环境使用HTTPS**
- ⚠️ **数据库密码使用强密码**

## 📝 开发规范

### Git Commit 规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 分支管理

```
master          - 主分支(稳定版本)
└── dev         - 开发分支
     ├── feature-xxx  - 功能分支
     └── fix-xxx      - Bug修复
```

## 🤝 团队协作

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/xxx`)
3. 提交修改 (`git commit -m 'feat: add xxx'`)
4. 推送到分支 (`git push origin feature/xxx`)
5. 创建 Pull Request

## 📄 许可证

MIT License

## 📮 联系方式

如有问题,请提交 Issue 或联系项目维护者。
