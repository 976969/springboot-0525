# Git 提交前检查清单

## ✅ 必须检查的项目

### 1. 敏感信息
- [ ] `.env` 文件没有提交(已在.gitignore中)
- [ ] `application.yml` 中没有硬编码密码
- [ ] API密钥使用环境变量
- [ ] 数据库密码使用环境变量

### 2. 大文件
- [ ] `uploads/` 目录没有提交(已在.gitignore中)
- [ ] `target/` 目录没有提交(已在.gitignore中)
- [ ] `node_modules/` 没有提交(已在.gitignore中)

### 3. IDE配置
- [ ] `.idea/` 没有提交(已在.gitignore中)
- [ ] `.vscode/` 没有提交(已在.gitignore中)
- [ ] `*.iml` 文件没有提交

### 4. 内部文档
- [ ] `介绍/` 文件夹没有提交(已在.gitignore中)
- [ ] `sql/test_data.sql` 没有提交(已在.gitignore中)

### 5. 代码质量
- [ ] 代码可以正常编译
- [ ] 没有明显的bug
- [ ] Commit信息符合规范

## 📝 Commit 信息规范

格式: `类型: 简短描述`

### 类型说明
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式(不影响功能)
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链变动

### 示例
✅ 好:
```
feat: 添加用户分页查询功能
fix: 修复教师端数据过滤bug
docs: 更新README文档
refactor: 重构Service层代码
```

❌ 坏:
```
更新代码
fix bug
123
修改了一些东西
```

## 🔍 提交前命令

```bash
# 1. 查看修改了哪些文件
git status

# 2. 查看具体修改内容
git diff

# 3. 查看暂存的文件
git diff --cached

# 4. 检查.gitignore是否生效
git status --ignored

# 5. 提交
git add .
git commit -m "feat: 你的提交信息"

# 6. 推送
git push origin 分支名
```

## ⚠️ 常见错误

### 错误1: 提交了敏感信息
```bash
# 立即修改密码!
# 然后从Git历史中删除
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all
git push origin master --force
```

### 错误2: 提交了大文件
```bash
# 从Git中删除(保留本地文件)
git rm -r --cached uploads/
git commit -m "chore: 移除上传文件"
git push
```

### 错误3: 提交了.idea等IDE配置
```bash
git rm -r --cached .idea/
git commit -m "chore: 移除IDE配置"
git push
```

## 📋 新项目初始化检查

- [ ] 创建 `.gitignore` 文件
- [ ] 创建 `.env.example` 模板
- [ ] 创建 `README.md` 文档
- [ ] 移除所有敏感信息
- [ ] 测试克隆后能否正常启动
