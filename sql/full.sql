-- ============================================================
-- 智能实训教学智能评价系统 - 完整版数据库脚本
-- 数据库名: training_eval
-- 使用方式: 在MySQL中直接执行本文件即可一键完成建库+建表+插入数据
-- 共14张表: admin, teacher, student, course, course_student,
--   training_task, training_result, evaluation_indicator,
--   evaluation_record, evaluation_report, check_record,
--   class_schedule, banner, verification_code
-- ============================================================

-- 创建数据库(如不存在)
CREATE DATABASE IF NOT EXISTS training_eval DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE training_eval;

-- 关闭外键检查, 方便按任意顺序删表重建
SET FOREIGN_KEY_CHECKS = 0;


-- ================================================================
--                          第一部分: 表结构
-- ================================================================

-- ==================== 一、用户相关表(3张) ====================

-- 1. 管理员表
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `avatar`      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 2. 教师表
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `avatar`      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `department`  VARCHAR(100) DEFAULT NULL            COMMENT '所属院系',
    `title`       VARCHAR(50)  DEFAULT NULL            COMMENT '职称',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 3. 学生表
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `avatar`      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `student_no`  VARCHAR(30)  DEFAULT NULL            COMMENT '学号',
    `class_name`  VARCHAR(50)  DEFAULT NULL            COMMENT '班级',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username`   (`username`),
    UNIQUE KEY `uk_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';


-- ==================== 二、课程与任务相关表(3张) ====================

-- 4. 课程表
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100) NOT NULL                COMMENT '课程名称',
    `description` TEXT                                 COMMENT '课程描述',
    `teacher_id`  BIGINT       NOT NULL                COMMENT '授课教师ID(关联teacher表)',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1进行中 0已结束',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 5. 课程-学生关联表
DROP TABLE IF EXISTS `course_student`;
CREATE TABLE `course_student` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_id`   BIGINT   NOT NULL                COMMENT '课程ID',
    `student_id`  BIGINT   NOT NULL                COMMENT '学生ID(关联student表)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_student` (`course_id`, `student_id`),
    KEY `idx_course_id`  (`course_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程-学生关联表';

-- 6. 实训任务表
DROP TABLE IF EXISTS `training_task`;
CREATE TABLE `training_task` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_id`    BIGINT       NOT NULL                COMMENT '课程ID',
    `title`        VARCHAR(200) NOT NULL                COMMENT '任务标题',
    `description`  TEXT                                 COMMENT '任务描述',
    `requirements` TEXT                                 COMMENT '实训要求',
    `deadline`     DATETIME     DEFAULT NULL            COMMENT '截止时间',
    `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1进行中 2已结束 0草稿',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训任务表';


-- ==================== 三、成果与评价相关表(5张) ====================

-- 7. 实训成果表
DROP TABLE IF EXISTS `training_result`;
CREATE TABLE `training_result` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`        BIGINT       NOT NULL                COMMENT '任务ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID(关联student表)',
    `file_path`      VARCHAR(500) NOT NULL                COMMENT '文件存储路径',
    `file_name`      VARCHAR(200) NOT NULL                COMMENT '原始文件名',
    `file_type`      VARCHAR(20)  NOT NULL                COMMENT '文件类型: docx/pdf/jpg/png',
    `file_size`      BIGINT       DEFAULT 0               COMMENT '文件大小(字节)',
    `parsed_content` TEXT                                 COMMENT '解析后的文本内容',
    `upload_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `status`         TINYINT      NOT NULL DEFAULT 0      COMMENT '状态: 0待核查 1已核查 2已评价',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_student` (`task_id`, `student_id`),
    KEY `idx_task_id`    (`task_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训成果表';

-- 8. 评价指标表（含 teacher_id 实现教师维度隔离）
DROP TABLE IF EXISTS `evaluation_indicator`;
CREATE TABLE `evaluation_indicator` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`           VARCHAR(100) NOT NULL                COMMENT '指标名称',
    `teacher_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '归属教师ID，0表示系统模板',
    `description`    TEXT                                 COMMENT '指标描述',
    `category`       VARCHAR(50)  DEFAULT NULL            COMMENT '指标分类',
    `default_weight` DECIMAL(5,2) NOT NULL DEFAULT 20.00  COMMENT '当前权重',
    `original_weight` DECIMAL(5,2) NOT NULL DEFAULT 20.00 COMMENT '原始权重（用于重置）',
    `is_system`      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否系统内置: 1是 0否',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价指标表';

-- 9. 评价记录表（含 score_ratio 评分比重字段）
DROP TABLE IF EXISTS `evaluation_record`;
CREATE TABLE `evaluation_record` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `result_id`       BIGINT       NOT NULL                COMMENT '实训成果ID',
    `indicator_id`    BIGINT       NOT NULL                COMMENT '评价指标ID',
    `ai_score`        DECIMAL(5,2) DEFAULT NULL            COMMENT 'AI评分',
    `teacher_score`   DECIMAL(5,2) DEFAULT NULL            COMMENT '教师评分',
    `final_score`     DECIMAL(5,2) DEFAULT NULL            COMMENT '最终得分',
    `score_ratio`     DECIMAL(3,1) DEFAULT 5.0             COMMENT '评分比重(AI权重,默认5:5)',
    `ai_comment`      TEXT                                 COMMENT 'AI评语',
    `teacher_comment` TEXT                                 COMMENT '教师评语',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_result_id` (`result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价记录表';

-- 10. 评价报告表
DROP TABLE IF EXISTS `evaluation_report`;
CREATE TABLE `evaluation_report` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`       BIGINT       NOT NULL                COMMENT '任务ID',
    `student_id`    BIGINT       NOT NULL                COMMENT '学生ID(关联student表)',
    `total_score`   DECIMAL(5,2) DEFAULT NULL            COMMENT '总评分',
    `report_data`   TEXT                                 COMMENT '报告数据(JSON)',
    `export_format` VARCHAR(10)  DEFAULT NULL            COMMENT '导出格式: excel/pdf',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id`    (`task_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价报告表';

-- 11. 核查记录表
DROP TABLE IF EXISTS `check_record`;
CREATE TABLE `check_record` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `result_id`         BIGINT      NOT NULL                COMMENT '实训成果ID',
    `check_type`        VARCHAR(50) NOT NULL                COMMENT '核查类型: completeness/logic/match',
    `check_result`      TINYINT     NOT NULL DEFAULT 0      COMMENT '核查结果: 0待核查 1通过 2存在问题',
    `issue_description` TEXT                                COMMENT '问题描述',
    `suggestion`        TEXT                                COMMENT '改进建议',
    `create_time`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_result_id` (`result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核查记录表';


-- ==================== 四、其他表(2张) ====================

-- 12. 课程表(上课时间安排)
DROP TABLE IF EXISTS `class_schedule`;
CREATE TABLE `class_schedule` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_id`   BIGINT       NOT NULL                COMMENT '课程ID',
    `teacher_id`  BIGINT       NOT NULL                COMMENT '教师ID',
    `day_of_week` TINYINT      NOT NULL                COMMENT '星期几: 1周一 ~ 7周日',
    `start_time`  VARCHAR(10)  NOT NULL                COMMENT '开始时间(如 08:00)',
    `end_time`    VARCHAR(10)  NOT NULL                COMMENT '结束时间(如 09:40)',
    `location`    VARCHAR(100) DEFAULT NULL            COMMENT '上课地点',
    `remark`      TEXT         DEFAULT NULL            COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id`   (`course_id`),
    KEY `idx_teacher_id`  (`teacher_id`),
    KEY `idx_day_of_week` (`day_of_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表(上课时间安排)';

-- 13. 轮播图表
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(100) DEFAULT NULL            COMMENT '标题',
    `content`     TEXT                                 COMMENT '内容描述',
    `image_url`   VARCHAR(500) DEFAULT NULL            COMMENT '图片地址',
    `link_url`    VARCHAR(500) DEFAULT NULL            COMMENT '跳转链接',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序(越小越靠前)',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1上架 0下架',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 14. 验证码表
DROP TABLE IF EXISTS `verification_code`;
CREATE TABLE `verification_code` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `target`      VARCHAR(100) NOT NULL                COMMENT '目标地址（邮箱或手机号）',
    `code`        VARCHAR(10)  NOT NULL                COMMENT '验证码（6位数字）',
    `type`        VARCHAR(20)  NOT NULL                COMMENT '类型: email / phone',
    `used`        TINYINT      NOT NULL DEFAULT 0      COMMENT '是否已使用: 0未使用 1已使用',
    `expire_time` DATETIME     NOT NULL                COMMENT '过期时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_target` (`target`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';


-- ================================================================
--                          第二部分: 测试数据
-- ================================================================

-- ==================== 一、用户数据 ====================

-- 1.1 管理员 (密码: admin123)
INSERT INTO `admin` (`username`, `password`, `real_name`, `email`, `phone`) VALUES
('admin', 'admin123', '系统管理员', 'admin@qcby.com', '13800000001');

-- 1.2 教师 (密码: 123456)
INSERT INTO `teacher` (`username`, `password`, `real_name`, `email`, `phone`, `department`, `title`) VALUES
('teacher01', '123456', '张老师', 'zhang@qcby.com', '13800000002', '计算机学院', '副教授'),
('teacher02', '123456', '李老师', 'li@qcby.com',    '13800000003', '软件学院',   '讲师');

-- 1.3 学生 (密码: 123456, 共10名)
INSERT INTO `student` (`username`, `password`, `real_name`, `email`, `phone`, `student_no`, `class_name`) VALUES
('student01', '123456', '王同学', 'wang@stu.com',   '13800000004', '2024010001', '计科2401'),
('student02', '123456', '赵同学', 'zhao@stu.com',   '13800000005', '2024010002', '计科2401'),
('student03', '123456', '陈同学', 'chen@stu.com',   '13800000006', '2024010003', '软件2401'),
('student04', '123456', '刘同学', 'liu@stu.com',    '13800000007', '2024010004', '计科2401'),
('student05', '123456', '杨同学', 'yang@stu.com',   '13800000008', '2024010005', '计科2401'),
('student06', '123456', '黄同学', 'huang@stu.com',  '13800000009', '2024010006', '软件2401'),
('student07', '123456', '周同学', 'zhou@stu.com',   '13800000010', '2024010007', '软件2401'),
('student08', '123456', '吴同学', 'wu@stu.com',     '13800000011', '2024010008', '计科2401'),
('student09', '123456', '徐同学', 'xu@stu.com',     '13800000012', '2024010009', '软件2401'),
('student10', '123456', '孙同学', 'sun@stu.com',    '13800000013', '2024010010', '计科2401');


-- ==================== 二、课程与选课数据 ====================

-- 2.1 课程 (共5门)
INSERT INTO `course` (`name`, `description`, `teacher_id`, `status`) VALUES
('Java程序设计',   'Java语言基础与面向对象编程',    1, 1),
('Web前端开发',    'HTML/CSS/JavaScript前端技术',    2, 1),
('数据库原理',     'MySQL数据库设计与应用',          1, 1),
('软件工程',       '软件开发过程与方法',             1, 1),
('移动应用开发',   'Android应用开发',               2, 1);

-- 2.2 学生选课
INSERT INTO `course_student` (`course_id`, `student_id`) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5),
(4, 1), (4, 3), (4, 5), (4, 6), (4, 7),
(5, 2), (5, 4), (5, 6), (5, 8), (5, 9), (5, 10);


-- ==================== 三、评价指标 ====================
-- teacher_id=0 表示系统默认模板，教师首次登录时会自动复制一份私有副本

INSERT INTO `evaluation_indicator` (`name`, `teacher_id`, `description`, `category`, `default_weight`, `original_weight`, `is_system`) VALUES
('代码质量',   0, '代码规范性、可读性、注释完整性',                   '技术实现', 25.00, 25.00, 1),
('文档规范性', 0, '文档格式、内容完整性、表述清晰度',                 '文档规范', 25.00, 25.00, 1),
('功能实现度', 0, '功能需求覆盖率、核心功能完成情况',                 '功能实现', 30.00, 30.00, 1),
('创新与拓展', 0, '创新点、额外功能、技术深度',                       '综合能力', 20.00, 20.00, 1),
('团队协作',   0, '分工明确度、沟通记录、协作工具使用、代码合并规范', '团队协作', 15.00, 15.00, 0),
('项目展示',   0, '演示效果、PPT质量、表达能力、问题回答',           '综合能力', 10.00, 10.00, 0);


-- ==================== 四、实训任务 ====================

-- 课程1: Java程序设计 (4个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(1, 'Java基础语法练习', '完成Java基本数据类型、运算符、流程控制等基础练习',     '1. 编写至少5个程序\n2. 包含注释\n3. 代码规范',           '2026-06-20 23:59:59', 1, '2026-05-01 09:00:00'),
(1, '面向对象编程',     '实现一个简单的学生管理系统,包含增删改查功能',           '1. 使用类和对象\n2. 实现封装\n3. 提供测试用例',         '2026-06-25 23:59:59', 1, '2026-05-08 09:00:00'),
(1, '异常处理与IO',     '文件读写操作,实现数据持久化',                           '1. 使用try-catch\n2. 读写文件\n3. 处理异常',             '2026-06-30 23:59:59', 1, '2026-05-15 09:00:00'),
(1, '集合框架应用',     '使用List、Map等集合实现通讯录',                         '1. 选择合适的集合\n2. 实现基本功能\n3. 优化性能',       '2026-07-05 23:59:59', 1, '2026-05-22 09:00:00');

-- 课程2: Web前端开发 (3个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(2, 'HTML页面设计',     '设计个人主页,包含导航、内容区、页脚',                   '1. 使用语义化标签\n2. 页面美观\n3. 响应式布局',         '2026-06-18 23:59:59', 1, '2026-05-03 10:00:00'),
(2, 'CSS样式设计',      '为个人主页添加完整样式',                                 '1. 使用Flex布局\n2. 动画效果\n3. 媒体查询',             '2026-06-23 23:59:59', 1, '2026-05-10 10:00:00'),
(2, 'JavaScript交互',   '实现轮播图、表单验证等交互',                             '1. 原生JS\n2. 事件处理\n3. 异步操作',                   '2026-06-28 23:59:59', 1, '2026-05-17 10:00:00');

-- 课程3: 数据库原理 (3个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(3, 'E-R图设计',       '设计图书馆管理系统的E-R图',                               '1. 识别实体\n2. 确定关系\n3. 绘制规范',                 '2026-06-22 23:59:59', 1, '2026-05-05 11:00:00'),
(3, '数据库建模',       '将E-R图转换为关系模式',                                   '1. 规范化设计\n2. 消除冗余\n3. 完整性约束',             '2026-06-27 23:59:59', 1, '2026-05-12 11:00:00'),
(3, 'SQL查询练习',      '编写复杂SQL查询',                                         '1. 多表连接\n2. 子查询\n3. 聚合函数',                   '2026-07-02 23:59:59', 1, '2026-05-19 11:00:00');

-- 课程4: 软件工程 (2个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(4, '需求分析文档',     '编写软件需求规格说明书',                                   '1. 功能需求\n2. 非功能需求\n3. 用例图',                 '2026-06-24 23:59:59', 1, '2026-05-07 14:00:00'),
(4, '系统设计文档',     '编写软件设计文档',                                         '1. 架构设计\n2. 模块设计\n3. 接口设计',                 '2026-06-29 23:59:59', 1, '2026-05-14 14:00:00');

-- 课程5: 移动应用开发 (2个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(5, 'Activity生命周期', '实现多Activity应用',                                       '1. 生命周期回调\n2. Intent传值\n3. 界面跳转',           '2026-06-26 23:59:59', 1, '2026-05-09 15:00:00'),
(5, '数据存储',         '实现本地数据持久化',                                       '1. SharedPreferences\n2. SQLite\n3. 文件存储',           '2026-07-01 23:59:59', 1, '2026-05-16 15:00:00');

-- 补充: 经典项目任务(用于报告展示)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(1, '项目一：学生信息管理系统', '使用Java实现一个完整的学生信息管理系统，包括增删改查功能', '1. 使用面向对象设计\n2. 包含完整的类图\n3. 实现数据持久化\n4. 编写详细文档', '2026-06-29 23:59:59', 1, '2026-05-29 17:38:14'),
(1, '项目二：图书管理系统',     '开发一个图书管理系统，支持借阅、归还、查询等功能',         '1. 实现用户权限管理\n2. 支持多条件查询\n3. 包含异常处理\n4. 代码注释完整',   '2026-06-30 23:59:59', 1, '2026-05-29 17:38:14'),
(2, '实验一：响应式网页设计',   '使用HTML5和CSS3创建一个响应式网页',                       '1. 适配移动端和PC端\n2. 使用Flexbox/Grid布局\n3. 包含动画效果\n4. 语义化标签', '2026-06-20 23:59:59', 1, '2026-05-29 17:38:14');


-- ==================== 五、实训成果(学生提交记录) ====================
-- 注意: 每个(task_id, student_id)组合只能有一条记录(唯一约束)

-- 学生1: 王同学 - 提交7个任务
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1, 1, 'uploads/result/task1_student1.docx', 'Java基础语法练习_王同学.docx', 'docx', 45678,  '2026-05-20 14:30:00', 2),
(2, 1, 'uploads/result/task2_student1.docx', '面向对象编程_王同学.docx',     'docx', 78901,  '2026-05-27 16:45:00', 2),
(3, 1, 'uploads/result/task3_student1.pdf',  '异常处理与IO_王同学.pdf',      'pdf',  234567, '2026-06-05 10:20:00', 2),
(4, 1, 'uploads/result/task4_student1.docx', '集合框架应用_王同学.docx',     'docx', 56789,  '2026-06-12 15:30:00', 1),
(6, 1, 'uploads/result/task6_student1.docx', 'CSS样式设计_王同学.docx',      'docx', 67890,  '2026-05-29 13:45:00', 2),
(7, 1, 'uploads/result/task7_student1.pdf',  'JavaScript交互_王同学.pdf',    'pdf',  456789, '2026-06-07 16:30:00', 1),
(9, 1, 'uploads/result/task9_student1.docx', 'E-R图设计_王同学.docx',        'docx', 89012,  '2026-05-25 09:15:00', 2);

-- 学生2: 赵同学 - 提交6个任务
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1,  2, 'uploads/result/task1_student2.docx',  'Java基础语法练习_赵同学.docx', 'docx', 51234,  '2026-05-21 10:00:00', 2),
(2,  2, 'uploads/result/task2_student2.docx',  '面向对象编程_赵同学.docx',     'docx', 82345,  '2026-05-28 14:20:00', 2),
(5,  2, 'uploads/result/task5_student2.pdf',   'HTML页面设计_赵同学.pdf',      'pdf',  289012, '2026-05-23 15:30:00', 2),
(6,  2, 'uploads/result/task6_student2.docx',  'CSS样式设计_赵同学.docx',      'docx', 71234,  '2026-05-30 11:45:00', 1),
(11, 2, 'uploads/result/task11_student2.docx', 'Activity生命周期_赵同学.docx', 'docx', 93456,  '2026-06-01 13:20:00', 2),
(12, 2, 'uploads/result/task12_student2.pdf',  '数据存储_赵同学.pdf',          'pdf',  378901, '2026-06-08 17:00:00', 1);

-- 学生3: 陈同学 - 提交8个任务
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1,  3, 'uploads/result/task1_student3.docx',  'Java基础语法练习_陈同学.docx', 'docx', 48901,  '2026-05-19 16:00:00', 2),
(3,  3, 'uploads/result/task3_student3.pdf',   '异常处理与IO_陈同学.pdf',      'pdf',  267890, '2026-06-04 11:30:00', 2),
(5,  3, 'uploads/result/task5_student3.pdf',   'HTML页面设计_陈同学.pdf',      'pdf',  312345, '2026-05-24 14:00:00', 2),
(7,  3, 'uploads/result/task7_student3.pdf',   'JavaScript交互_陈同学.pdf',    'pdf',  423456, '2026-06-06 10:45:00', 2),
(8,  3, 'uploads/result/task8_student3.docx',  '需求分析文档_陈同学.docx',     'docx', 134567, '2026-06-10 16:15:00', 0),
(9,  3, 'uploads/result/task9_student3.docx',  'E-R图设计_陈同学.docx',        'docx', 95678,  '2026-05-26 15:20:00', 2),
(10, 3, 'uploads/result/task10_student3.docx', '数据库建模_陈同学.docx',       'docx', 112345, '2026-06-03 09:40:00', 1);

-- 特殊成果: 含解析内容的成果(用于智能核查和AI评分演示)
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `parsed_content`, `upload_time`, `status`) VALUES
(4, 3, 'uploads/result/task4_student3.pdf', '陈同学_响应式网页设计.pdf', 'pdf', 520000,
'响应式网页设计报告\n\n设计说明：\n本次设计采用移动端优先（Mobile First）的设计理念。\n\n技术栈：\n- HTML5语义化标签\n- CSS3 Flexbox和Grid布局\n- CSS3动画和过渡效果\n- Media Query媒体查询\n\n页面结构：\n1. 顶部导航栏\n2. 轮播图区域\n3. 内容展示区\n4. 侧边栏\n5. 页脚信息\n\n响应式断点：\n- 手机端：< 768px\n- 平板端：768px - 1024px\n- PC端：> 1024px\n\n特色功能：\n- 平滑滚动\n- 懒加载图片\n- 触摸手势支持',
'2026-05-29 17:38:14', 1);

-- 学生4~10: 更多提交记录
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1,  4,  'uploads/result/task1_student4.docx',   'Java基础语法练习_刘同学.docx', 'docx', 52345,  '2026-05-22 13:00:00', 2),
(2,  4,  'uploads/result/task2_student4.docx',   '面向对象编程_刘同学.docx',     'docx', 85678,  '2026-05-29 15:30:00', 1),
(5,  5,  'uploads/result/task5_student5.pdf',    'HTML页面设计_杨同学.pdf',      'pdf',  298765, '2026-05-25 10:20:00', 2),
(6,  5,  'uploads/result/task6_student5.docx',   'CSS样式设计_杨同学.docx',      'docx', 74567,  '2026-06-01 14:45:00', 2),
(9,  6,  'uploads/result/task9_student6.docx',   'E-R图设计_黄同学.docx',        'docx', 91234,  '2026-05-27 11:15:00', 2),
(10, 6,  'uploads/result/task10_student6.docx',  '数据库建模_黄同学.docx',       'docx', 108765, '2026-06-04 16:30:00', 1),
(11, 7,  'uploads/result/task11_student7.docx',  'Activity生命周期_周同学.docx', 'docx', 96789,  '2026-06-02 13:45:00', 2),
(12, 7,  'uploads/result/task12_student7.pdf',   '数据存储_周同学.pdf',          'pdf',  389012, '2026-06-09 17:20:00', 0),
(1,  8,  'uploads/result/task1_student8.docx',   'Java基础语法练习_吴同学.docx', 'docx', 49876,  '2026-05-23 14:10:00', 2),
(5,  9,  'uploads/result/task5_student9.pdf',    'HTML页面设计_徐同学.pdf',      'pdf',  305432, '2026-05-26 12:00:00', 2),
(11, 10, 'uploads/result/task11_student10.docx', 'Activity生命周期_孙同学.docx', 'docx', 98765,  '2026-06-03 15:00:00', 1);


-- ==================== 六、核查记录 ====================

INSERT INTO `check_record` (`result_id`, `check_type`, `check_result`, `issue_description`, `suggestion`) VALUES
(4, 'completeness', 1, '', '文档结构完整，包含系统概述、功能模块、技术实现等必要章节'),
(4, 'logic',        1, '', '逻辑清晰，从设计理念到技术实现层层递进'),
(4, 'match',        1, '', '完全符合响应式设计要求');


-- ==================== 七、评价记录 ====================

-- 成果ID=4: 陈同学_响应式网页设计 (含AI评语和教师评语)
INSERT INTO `evaluation_record` (`result_id`, `indicator_id`, `ai_score`, `teacher_score`, `final_score`, `score_ratio`, `ai_comment`, `teacher_comment`) VALUES
(4, 1, 90.00, 93.00, 93.00, 5.0, 'HTML/CSS代码规范，结构清晰',                     '代码质量优秀，语义化做得很好'),
(4, 2, 95.00, 96.00, 95.50, 5.0, '文档格式规范，内容详实，图文并茂',               '文档质量很高，可以作为范例'),
(4, 3, 88.00, 90.00, 89.00, 5.0, '响应式效果实现良好，适配多种设备',               '响应式设计做得很好'),
(4, 4, 85.00, 87.00, 86.00, 5.0, '使用了现代CSS特性，有一定创新',                   '可以尝试使用CSS预处理器如SASS');

-- 批量评价记录(简洁模式, 无评语)
INSERT INTO `evaluation_record` (`result_id`, `indicator_id`, `ai_score`, `teacher_score`, `final_score`, `score_ratio`) VALUES
(1, 1, 85.00, 88.00, 86.50, 5.0), (1, 2, 82.00, 85.00, 83.50, 5.0), (1, 3, 88.00, 90.00, 89.00, 5.0), (1, 4, 75.00, 80.00, 77.50, 5.0),
(2, 1, 90.00, 92.00, 91.00, 5.0), (2, 2, 88.00, 89.00, 88.50, 5.0), (2, 3, 92.00, 93.00, 92.50, 5.0), (2, 4, 85.00, 87.00, 86.00, 5.0),
(3, 1, 82.00, 84.00, 83.00, 5.0), (3, 2, 80.00, 82.00, 81.00, 5.0), (3, 3, 85.00, 87.00, 86.00, 5.0), (3, 4, 78.00, 80.00, 79.00, 5.0),
(5, 1, 88.00, 90.00, 89.00, 5.0), (5, 2, 85.00, 87.00, 86.00, 5.0), (5, 3, 90.00, 92.00, 91.00, 5.0), (5, 4, 82.00, 84.00, 83.00, 5.0),
(6, 1, 83.00, 85.00, 84.00, 5.0), (6, 2, 81.00, 83.00, 82.00, 5.0), (6, 3, 86.00, 88.00, 87.00, 5.0), (6, 4, 79.00, 81.00, 80.00, 5.0),
(7, 1, 91.00, 95.00, 95.00, 5.0), (7, 2, 89.00, 97.00, 97.00, 5.0), (7, 3, 93.00, 97.00, 97.00, 5.0), (7, 4, 87.00, 89.00, 88.00, 5.0),
(9, 1, 86.00, 88.00, 87.00, 5.0), (9, 2, 84.00, 86.00, 85.00, 5.0), (9, 3, 89.00, 91.00, 90.00, 5.0), (9, 4, 81.00, 83.00, 82.00, 5.0),
(11, 1, 84.00, 86.00, 85.00, 5.0), (11, 2, 82.00, 84.00, 83.00, 5.0), (11, 3, 87.00, 89.00, 88.00, 5.0), (11, 4, 80.00, 82.00, 81.00, 5.0),
(12, 1, 89.00, 91.00, 90.00, 5.0), (12, 2, 87.00, 89.00, 88.00, 5.0), (12, 3, 92.00, 94.00, 93.00, 5.0), (12, 4, 85.00, 87.00, 86.00, 5.0),
(13, 1, 87.00, 89.00, 88.00, 5.0), (13, 2, 85.00, 87.00, 86.00, 5.0), (13, 3, 90.00, 92.00, 91.00, 5.0), (13, 4, 83.00, 85.00, 84.00, 5.0),
(15, 1, 82.00, 84.00, 83.00, 5.0), (15, 2, 80.00, 82.00, 81.00, 5.0), (15, 3, 85.00, 87.00, 86.00, 5.0), (15, 4, 78.00, 80.00, 79.00, 5.0),
(17, 1, 90.00, 92.00, 91.00, 5.0), (17, 2, 88.00, 90.00, 89.00, 5.0), (17, 3, 93.00, 95.00, 94.00, 5.0), (17, 4, 86.00, 88.00, 87.00, 5.0),
(19, 1, 85.00, 87.00, 86.00, 5.0), (19, 2, 83.00, 85.00, 84.00, 5.0), (19, 3, 88.00, 90.00, 89.00, 5.0), (19, 4, 81.00, 83.00, 82.00, 5.0);


-- ==================== 八、评价报告 ====================

INSERT INTO `evaluation_report` (`task_id`, `student_id`, `total_score`, `report_data`, `export_format`, `create_time`) VALUES
(1,  1,  86.63, '{"task":"Java基础语法练习","student":"王同学","score":86.63,"rank":"top10%"}',  NULL, '2026-05-25 10:00:00'),
(2,  1,  89.50, '{"task":"面向对象编程","student":"王同学","score":89.50,"rank":"top5%"}',        NULL, '2026-06-01 11:00:00'),
(3,  1,  82.25, '{"task":"异常处理与IO","student":"王同学","score":82.25,"rank":"top20%"}',       NULL, '2026-06-08 14:00:00'),
(6,  1,  83.25, '{"task":"CSS样式设计","student":"王同学","score":83.25,"rank":"top15%"}',        NULL, '2026-06-05 16:00:00'),
(9,  1,  86.00, '{"task":"E-R图设计","student":"王同学","score":86.00,"rank":"top12%"}',          NULL, '2026-05-30 17:00:00'),
(1,  2,  84.50, '{"task":"Java基础语法练习","student":"赵同学","score":84.50,"rank":"top15%"}',   NULL, '2026-05-26 12:00:00'),
(2,  2,  87.00, '{"task":"面向对象编程","student":"赵同学","score":87.00,"rank":"top10%"}',        NULL, '2026-06-02 13:00:00'),
(5,  2,  85.50, '{"task":"HTML页面设计","student":"赵同学","score":85.50,"rank":"top12%"}',        NULL, '2026-05-29 14:00:00'),
(11, 2,  84.25, '{"task":"Activity生命周期","student":"赵同学","score":84.25,"rank":"top18%"}',    NULL, '2026-06-04 15:00:00'),
(1,  3,  84.50, '{"task":"Java基础语法练习","student":"陈同学","score":84.50,"rank":"top15%"}',   NULL, '2026-05-24 16:00:00'),
(3,  3,  82.25, '{"task":"异常处理与IO","student":"陈同学","score":82.25,"rank":"top20%"}',       NULL, '2026-06-07 17:00:00'),
(5,  3,  88.00, '{"task":"HTML页面设计","student":"陈同学","score":88.00,"rank":"top7%"}',         NULL, '2026-05-31 18:00:00'),
(7,  3,  91.00, '{"task":"JavaScript交互","student":"陈同学","score":91.00,"rank":"top3%"}',       NULL, '2026-06-09 19:00:00'),
(9,  3,  86.00, '{"task":"E-R图设计","student":"陈同学","score":86.00,"rank":"top12%"}',           NULL, '2026-06-01 20:00:00'),
(1,  4,  83.50, '{"task":"Java基础语法练习","student":"刘同学","score":83.50,"rank":"top18%"}',   NULL, '2026-05-27 21:00:00'),
(5,  5,  86.50, '{"task":"HTML页面设计","student":"杨同学","score":86.50,"rank":"top11%"}',        NULL, '2026-05-30 22:00:00'),
(6,  5,  84.00, '{"task":"CSS样式设计","student":"杨同学","score":84.00,"rank":"top16%"}',        NULL, '2026-06-06 23:00:00'),
(9,  6,  85.00, '{"task":"E-R图设计","student":"黄同学","score":85.00,"rank":"top13%"}',           NULL, '2026-06-02 00:00:00'),
(11, 7,  86.75, '{"task":"Activity生命周期","student":"周同学","score":86.75,"rank":"top9%"}',     NULL, '2026-06-05 01:00:00'),
(1,  8,  82.00, '{"task":"Java基础语法练习","student":"吴同学","score":82.00,"rank":"top22%"}',   NULL, '2026-05-28 02:00:00'),
(5,  9,  87.50, '{"task":"HTML页面设计","student":"徐同学","score":87.50,"rank":"top6%"}',         NULL, '2026-05-31 03:00:00'),
(11, 10, 85.50, '{"task":"Activity生命周期","student":"孙同学","score":85.50,"rank":"top12%"}',    NULL, '2026-06-06 04:00:00');


-- ==================== 九、课程表(上课时间安排) ====================

-- 张老师(teacher_id=1)的课程安排
INSERT INTO `class_schedule` (`course_id`, `teacher_id`, `day_of_week`, `start_time`, `end_time`, `location`, `remark`) VALUES
(1, 1, 1, '08:00', '09:40', '教学楼A-301', '记得带学生证'),
(1, 1, 3, '10:00', '11:40', '教学楼A-301', '收作业: Java基础语法练习'),
(2, 1, 2, '14:00', '15:40', '实验室B-202', '实验课: 面向对象编程'),
(2, 1, 4, '14:00', '15:40', '教学楼A-305', NULL),
(3, 1, 5, '08:00', '09:40', '教学楼A-301', '收作业: 异常处理练习'),
(4, 1, 1, '10:00', '11:40', '教学楼A-203', NULL),
(5, 1, 3, '14:00', '15:40', '实验室B-101', '实验课: 响应式网页设计');

-- 李老师(teacher_id=2)的课程安排
INSERT INTO `class_schedule` (`course_id`, `teacher_id`, `day_of_week`, `start_time`, `end_time`, `location`, `remark`) VALUES
(6,  2, 1, '14:00', '15:40', '教学楼C-102', '收作业: Python基础练习'),
(7,  2, 2, '08:00', '09:40', '教学楼C-102', NULL),
(8,  2, 2, '10:00', '11:40', '实验室D-301', '实验课: 数据结构实践'),
(9,  2, 4, '08:00', '09:40', '教学楼C-105', NULL),
(10, 2, 4, '10:00', '11:40', '教学楼C-102', '收作业: 数据库设计'),
(11, 2, 5, '14:00', '15:40', '教学楼C-102', NULL);


-- ==================== 十、轮播图数据 ====================

INSERT INTO `banner` (`title`, `content`, `image_url`, `link_url`, `sort`, `status`) VALUES
('欢迎来到智能实训评价系统', '基于AI技术的实训作业自动评分与智能评价平台', '/api/uploads/banner/hero.png', NULL, 1, 1),
('AI智能评分', '上传作业后一键AI评分，多维度指标自动评估', '/api/uploads/banner/hero.png', NULL, 2, 1),
('数据报表中心', '生成详细评价报告，支持Excel/PDF导出', '/api/uploads/banner/hero.png', NULL, 3, 1);


-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- 数据统计验证
-- ============================================================
SELECT '管理员'     AS `表名`, COUNT(*) AS `记录数` FROM `admin`
UNION ALL SELECT '教师',       COUNT(*) FROM `teacher`
UNION ALL SELECT '学生',       COUNT(*) FROM `student`
UNION ALL SELECT '课程',       COUNT(*) FROM `course`
UNION ALL SELECT '选课记录',   COUNT(*) FROM `course_student`
UNION ALL SELECT '实训任务',   COUNT(*) FROM `training_task`
UNION ALL SELECT '实训成果',   COUNT(*) FROM `training_result`
UNION ALL SELECT '评价指标',   COUNT(*) FROM `evaluation_indicator`
UNION ALL SELECT '评价记录',   COUNT(*) FROM `evaluation_record`
UNION ALL SELECT '评价报告',   COUNT(*) FROM `evaluation_report`
UNION ALL SELECT '核查记录',   COUNT(*) FROM `check_record`
UNION ALL SELECT '课程表',     COUNT(*) FROM `class_schedule`
UNION ALL SELECT '轮播图',     COUNT(*) FROM `banner`
UNION ALL SELECT '验证码',     COUNT(*) FROM `verification_code`;

-- ============================================================
-- 完整版脚本执行完毕!
-- 共13张表, 全部数据已就绪
-- 
-- 登录账号:
--   管理员: admin / admin123
--   教师:   teacher01 / 123456  teacher02 / 123456
--   学生:   student01~student10 / 123456
-- ============================================================
-- ============================================================
-- 智能实训教学智能评价系统 - 完整版数据库脚本
-- 数据库名: training_eval
-- 使用方式: 在MySQL中直接执行本文件即可一键完成建库+建表+插入数据
-- 说明: 本文件 = init.sql + test_data.sql 的合并版本
-- ============================================================

-- 创建数据库(如不存在)
CREATE DATABASE IF NOT EXISTS training_eval DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE training_eval;

-- 关闭外键检查, 方便按任意顺序删表重建
SET FOREIGN_KEY_CHECKS = 0;


-- ================================================================
--                          第一部分: 表结构
-- ================================================================

-- ==================== 一、用户相关表(3张) ====================

-- 1. 管理员表
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `avatar`      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 2. 教师表
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `avatar`      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `department`  VARCHAR(100) DEFAULT NULL            COMMENT '所属院系',
    `title`       VARCHAR(50)  DEFAULT NULL            COMMENT '职称',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 3. 学生表
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `avatar`      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `student_no`  VARCHAR(30)  DEFAULT NULL            COMMENT '学号',
    `class_name`  VARCHAR(50)  DEFAULT NULL            COMMENT '班级',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username`   (`username`),
    UNIQUE KEY `uk_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';


-- ==================== 二、课程与任务相关表(3张) ====================

-- 4. 课程表
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(100) NOT NULL                COMMENT '课程名称',
    `description` TEXT                                 COMMENT '课程描述',
    `teacher_id`  BIGINT       NOT NULL                COMMENT '授课教师ID(关联teacher表)',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1进行中 0已结束',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 5. 课程-学生关联表
DROP TABLE IF EXISTS `course_student`;
CREATE TABLE `course_student` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_id`   BIGINT   NOT NULL                COMMENT '课程ID',
    `student_id`  BIGINT   NOT NULL                COMMENT '学生ID(关联student表)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_student` (`course_id`, `student_id`),
    KEY `idx_course_id`  (`course_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程-学生关联表';

-- 6. 实训任务表
DROP TABLE IF EXISTS `training_task`;
CREATE TABLE `training_task` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_id`    BIGINT       NOT NULL                COMMENT '课程ID',
    `title`        VARCHAR(200) NOT NULL                COMMENT '任务标题',
    `description`  TEXT                                 COMMENT '任务描述',
    `requirements` TEXT                                 COMMENT '实训要求',
    `deadline`     DATETIME     DEFAULT NULL            COMMENT '截止时间',
    `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1进行中 2已结束 0草稿',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训任务表';


-- ==================== 三、成果与评价相关表(5张) ====================

-- 7. 实训成果表
DROP TABLE IF EXISTS `training_result`;
CREATE TABLE `training_result` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`        BIGINT       NOT NULL                COMMENT '任务ID',
    `student_id`     BIGINT       NOT NULL                COMMENT '学生ID(关联student表)',
    `file_path`      VARCHAR(500) NOT NULL                COMMENT '文件存储路径',
    `file_name`      VARCHAR(200) NOT NULL                COMMENT '原始文件名',
    `file_type`      VARCHAR(20)  NOT NULL                COMMENT '文件类型: docx/pdf/jpg/png',
    `file_size`      BIGINT       DEFAULT 0               COMMENT '文件大小(字节)',
    `parsed_content` TEXT                                 COMMENT '解析后的文本内容',
    `upload_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `status`         TINYINT      NOT NULL DEFAULT 0      COMMENT '状态: 0待核查 1已核查 2已评价',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_student` (`task_id`, `student_id`),
    KEY `idx_task_id`    (`task_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训成果表';

-- 8. 评价指标表
DROP TABLE IF EXISTS `evaluation_indicator`;
CREATE TABLE `evaluation_indicator` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`           VARCHAR(100) NOT NULL                COMMENT '指标名称',
    `teacher_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '归属教师ID，0表示系统模板',
    `description`    TEXT                                 COMMENT '指标描述',
    `category`       VARCHAR(50)  DEFAULT NULL            COMMENT '指标分类',
    `default_weight` DECIMAL(5,2) NOT NULL DEFAULT 20.00  COMMENT '当前权重',
    `original_weight` DECIMAL(5,2) NOT NULL DEFAULT 20.00 COMMENT '原始权重（用于重置）',
    `is_system`      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否系统内置: 1是 0否',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价指标表';

-- 9. 评价记录表
DROP TABLE IF EXISTS `evaluation_record`;
CREATE TABLE `evaluation_record` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `result_id`       BIGINT       NOT NULL                COMMENT '实训成果ID',
    `indicator_id`    BIGINT       NOT NULL                COMMENT '评价指标ID',
    `ai_score`        DECIMAL(5,2) DEFAULT NULL            COMMENT 'AI评分',
    `teacher_score`   DECIMAL(5,2) DEFAULT NULL            COMMENT '教师评分',
    `final_score`     DECIMAL(5,2) DEFAULT NULL            COMMENT '最终得分',
    `ai_comment`      TEXT                                 COMMENT 'AI评语',
    `teacher_comment` TEXT                                 COMMENT '教师评语',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_result_id` (`result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价记录表';

-- 10. 评价报告表
DROP TABLE IF EXISTS `evaluation_report`;
CREATE TABLE `evaluation_report` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`       BIGINT       NOT NULL                COMMENT '任务ID',
    `student_id`    BIGINT       NOT NULL                COMMENT '学生ID(关联student表)',
    `total_score`   DECIMAL(5,2) DEFAULT NULL            COMMENT '总评分',
    `report_data`   TEXT                                 COMMENT '报告数据(JSON)',
    `export_format` VARCHAR(10)  DEFAULT NULL            COMMENT '导出格式: excel/pdf',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id`    (`task_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价报告表';

-- 11. 核查记录表
DROP TABLE IF EXISTS `check_record`;
CREATE TABLE `check_record` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `result_id`         BIGINT      NOT NULL                COMMENT '实训成果ID',
    `check_type`        VARCHAR(50) NOT NULL                COMMENT '核查类型: completeness/logic/match',
    `check_result`      TINYINT     NOT NULL DEFAULT 0      COMMENT '核查结果: 0待核查 1通过 2存在问题',
    `issue_description` TEXT                                COMMENT '问题描述',
    `suggestion`        TEXT                                COMMENT '改进建议',
    `create_time`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_result_id` (`result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核查记录表';


-- 12. 课程表(上课时间安排)
DROP TABLE IF EXISTS `class_schedule`;
CREATE TABLE `class_schedule` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_id`   BIGINT       NOT NULL                COMMENT '课程ID',
    `teacher_id`  BIGINT       NOT NULL                COMMENT '教师ID',
    `day_of_week` TINYINT      NOT NULL                COMMENT '星期几: 1周一 ~ 7周日',
    `start_time`  VARCHAR(10)  NOT NULL                COMMENT '开始时间(如 08:00)',
    `end_time`    VARCHAR(10)  NOT NULL                COMMENT '结束时间(如 09:40)',
    `location`    VARCHAR(100) DEFAULT NULL            COMMENT '上课地点',
    `remark`      TEXT         DEFAULT NULL            COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id`   (`course_id`),
    KEY `idx_teacher_id`  (`teacher_id`),
    KEY `idx_day_of_week` (`day_of_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表(上课时间安排)';


-- ================================================================
--                          第二部分: 测试数据
-- ================================================================

-- ==================== 一、用户数据 ====================

-- 1.1 管理员 (密码: admin123)
INSERT INTO `admin` (`username`, `password`, `real_name`, `email`, `phone`) VALUES
('admin', 'admin123', '系统管理员', 'admin@qcby.com', '13800000001');

-- 1.2 教师 (密码: 123456)
INSERT INTO `teacher` (`username`, `password`, `real_name`, `email`, `phone`, `department`, `title`) VALUES
('teacher01', '123456', '张老师', 'zhang@qcby.com', '13800000002', '计算机学院', '副教授'),
('teacher02', '123456', '李老师', 'li@qcby.com',    '13800000003', '软件学院',   '讲师');

-- 1.3 学生 (密码: 123456, 共10名)
INSERT INTO `student` (`username`, `password`, `real_name`, `email`, `phone`, `student_no`, `class_name`) VALUES
('student01', '123456', '王同学', 'wang@stu.com',   '13800000004', '2024010001', '计科2401'),
('student02', '123456', '赵同学', 'zhao@stu.com',   '13800000005', '2024010002', '计科2401'),
('student03', '123456', '陈同学', 'chen@stu.com',   '13800000006', '2024010003', '软件2401'),
('student04', '123456', '刘同学', 'liu@stu.com',    '13800000007', '2024010004', '计科2401'),
('student05', '123456', '杨同学', 'yang@stu.com',   '13800000008', '2024010005', '计科2401'),
('student06', '123456', '黄同学', 'huang@stu.com',  '13800000009', '2024010006', '软件2401'),
('student07', '123456', '周同学', 'zhou@stu.com',   '13800000010', '2024010007', '软件2401'),
('student08', '123456', '吴同学', 'wu@stu.com',     '13800000011', '2024010008', '计科2401'),
('student09', '123456', '徐同学', 'xu@stu.com',     '13800000012', '2024010009', '软件2401'),
('student10', '123456', '孙同学', 'sun@stu.com',    '13800000013', '2024010010', '计科2401');


-- ==================== 二、课程与选课数据 ====================

-- 2.1 课程 (共5门)
INSERT INTO `course` (`name`, `description`, `teacher_id`, `status`) VALUES
('Java程序设计',   'Java语言基础与面向对象编程',    1, 1),
('Web前端开发',    'HTML/CSS/JavaScript前端技术',    2, 1),
('数据库原理',     'MySQL数据库设计与应用',          1, 1),
('软件工程',       '软件开发过程与方法',             1, 1),
('移动应用开发',   'Android应用开发',               2, 1);

-- 2.2 学生选课
INSERT INTO `course_student` (`course_id`, `student_id`) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5),
(4, 1), (4, 3), (4, 5), (4, 6), (4, 7),
(5, 2), (5, 4), (5, 6), (5, 8), (5, 9), (5, 10);


-- ==================== 三、评价指标 ====================
-- teacher_id=0 表示系统默认模板，老师首次登录时会自动复制

INSERT INTO `evaluation_indicator` (`name`, `teacher_id`, `description`, `category`, `default_weight`, `original_weight`, `is_system`) VALUES
('代码质量',   0, '代码规范性、可读性、注释完整性',                   '技术实现', 25.00, 25.00, 1),
('文档规范性', 0, '文档格式、内容完整性、表述清晰度',                 '文档规范', 25.00, 25.00, 1),
('功能实现度', 0, '功能需求覆盖率、核心功能完成情况',                 '功能实现', 30.00, 30.00, 1),
('创新与拓展', 0, '创新点、额外功能、技术深度',                       '综合能力', 20.00, 20.00, 1),
('团队协作',   0, '分工明确度、沟通记录、协作工具使用、代码合并规范', '团队协作', 15.00, 15.00, 0),
('项目展示',   0, '演示效果、PPT质量、表达能力、问题回答',           '综合能力', 10.00, 10.00, 0);


-- ==================== 四、实训任务 ====================

-- 课程1: Java程序设计 (4个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(1, 'Java基础语法练习', '完成Java基本数据类型、运算符、流程控制等基础练习',     '1. 编写至少5个程序\n2. 包含注释\n3. 代码规范',           '2026-06-20 23:59:59', 1, '2026-05-01 09:00:00'),
(1, '面向对象编程',     '实现一个简单的学生管理系统,包含增删改查功能',           '1. 使用类和对象\n2. 实现封装\n3. 提供测试用例',         '2026-06-25 23:59:59', 1, '2026-05-08 09:00:00'),
(1, '异常处理与IO',     '文件读写操作,实现数据持久化',                           '1. 使用try-catch\n2. 读写文件\n3. 处理异常',             '2026-06-30 23:59:59', 1, '2026-05-15 09:00:00'),
(1, '集合框架应用',     '使用List、Map等集合实现通讯录',                         '1. 选择合适的集合\n2. 实现基本功能\n3. 优化性能',       '2026-07-05 23:59:59', 1, '2026-05-22 09:00:00');

-- 课程2: Web前端开发 (3个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(2, 'HTML页面设计',     '设计个人主页,包含导航、内容区、页脚',                   '1. 使用语义化标签\n2. 页面美观\n3. 响应式布局',         '2026-06-18 23:59:59', 1, '2026-05-03 10:00:00'),
(2, 'CSS样式设计',      '为个人主页添加完整样式',                                 '1. 使用Flex布局\n2. 动画效果\n3. 媒体查询',             '2026-06-23 23:59:59', 1, '2026-05-10 10:00:00'),
(2, 'JavaScript交互',   '实现轮播图、表单验证等交互',                             '1. 原生JS\n2. 事件处理\n3. 异步操作',                   '2026-06-28 23:59:59', 1, '2026-05-17 10:00:00');

-- 课程3: 数据库原理 (3个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(3, 'E-R图设计',       '设计图书馆管理系统的E-R图',                               '1. 识别实体\n2. 确定关系\n3. 绘制规范',                 '2026-06-22 23:59:59', 1, '2026-05-05 11:00:00'),
(3, '数据库建模',       '将E-R图转换为关系模式',                                   '1. 规范化设计\n2. 消除冗余\n3. 完整性约束',             '2026-06-27 23:59:59', 1, '2026-05-12 11:00:00'),
(3, 'SQL查询练习',      '编写复杂SQL查询',                                         '1. 多表连接\n2. 子查询\n3. 聚合函数',                   '2026-07-02 23:59:59', 1, '2026-05-19 11:00:00');

-- 课程4: 软件工程 (2个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(4, '需求分析文档',     '编写软件需求规格说明书',                                   '1. 功能需求\n2. 非功能需求\n3. 用例图',                 '2026-06-24 23:59:59', 1, '2026-05-07 14:00:00'),
(4, '系统设计文档',     '编写软件设计文档',                                         '1. 架构设计\n2. 模块设计\n3. 接口设计',                 '2026-06-29 23:59:59', 1, '2026-05-14 14:00:00');

-- 课程5: 移动应用开发 (2个任务)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(5, 'Activity生命周期', '实现多Activity应用',                                       '1. 生命周期回调\n2. Intent传值\n3. 界面跳转',           '2026-06-26 23:59:59', 1, '2026-05-09 15:00:00'),
(5, '数据存储',         '实现本地数据持久化',                                       '1. SharedPreferences\n2. SQLite\n3. 文件存储',           '2026-07-01 23:59:59', 1, '2026-05-16 15:00:00');

-- 补充: 经典项目任务(用于报告展示)
INSERT INTO `training_task` (`course_id`, `title`, `description`, `requirements`, `deadline`, `status`, `create_time`) VALUES
(1, '项目一：学生信息管理系统', '使用Java实现一个完整的学生信息管理系统，包括增删改查功能', '1. 使用面向对象设计\n2. 包含完整的类图\n3. 实现数据持久化\n4. 编写详细文档', '2026-06-29 23:59:59', 1, '2026-05-29 17:38:14'),
(1, '项目二：图书管理系统',     '开发一个图书管理系统，支持借阅、归还、查询等功能',         '1. 实现用户权限管理\n2. 支持多条件查询\n3. 包含异常处理\n4. 代码注释完整',   '2026-06-30 23:59:59', 1, '2026-05-29 17:38:14'),
(2, '实验一：响应式网页设计',   '使用HTML5和CSS3创建一个响应式网页',                       '1. 适配移动端和PC端\n2. 使用Flexbox/Grid布局\n3. 包含动画效果\n4. 语义化标签', '2026-06-20 23:59:59', 1, '2026-05-29 17:38:14');


-- ==================== 五、实训成果(学生提交记录) ====================
-- 注意: 每个(task_id, student_id)组合只能有一条记录(唯一约束)

-- 学生1: 王同学 - 提交7个任务
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1, 1, 'upload/result/task1_student1.docx', 'Java基础语法练习_王同学.docx', 'docx', 45678,  '2026-05-20 14:30:00', 2),
(2, 1, 'upload/result/task2_student1.docx', '面向对象编程_王同学.docx',     'docx', 78901,  '2026-05-27 16:45:00', 2),
(3, 1, 'upload/result/task3_student1.pdf',  '异常处理与IO_王同学.pdf',      'pdf',  234567, '2026-06-05 10:20:00', 2),
(4, 1, 'upload/result/task4_student1.docx', '集合框架应用_王同学.docx',     'docx', 56789,  '2026-06-12 15:30:00', 1),
(6, 1, 'upload/result/task6_student1.docx', 'CSS样式设计_王同学.docx',      'docx', 67890,  '2026-05-29 13:45:00', 2),
(7, 1, 'upload/result/task7_student1.pdf',  'JavaScript交互_王同学.pdf',    'pdf',  456789, '2026-06-07 16:30:00', 1),
(9, 1, 'upload/result/task9_student1.docx', 'E-R图设计_王同学.docx',        'docx', 89012,  '2026-05-25 09:15:00', 2);

-- 学生2: 赵同学 - 提交6个任务
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1,  2, 'upload/result/task1_student2.docx',  'Java基础语法练习_赵同学.docx', 'docx', 51234,  '2026-05-21 10:00:00', 2),
(2,  2, 'upload/result/task2_student2.docx',  '面向对象编程_赵同学.docx',     'docx', 82345,  '2026-05-28 14:20:00', 2),
(5,  2, 'upload/result/task5_student2.pdf',   'HTML页面设计_赵同学.pdf',      'pdf',  289012, '2026-05-23 15:30:00', 2),
(6,  2, 'upload/result/task6_student2.docx',  'CSS样式设计_赵同学.docx',      'docx', 71234,  '2026-05-30 11:45:00', 1),
(11, 2, 'upload/result/task11_student2.docx', 'Activity生命周期_赵同学.docx', 'docx', 93456,  '2026-06-01 13:20:00', 2),
(12, 2, 'upload/result/task12_student2.pdf',  '数据存储_赵同学.pdf',          'pdf',  378901, '2026-06-08 17:00:00', 1);

-- 学生3: 陈同学 - 提交7个任务
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1,  3, 'upload/result/task1_student3.docx',  'Java基础语法练习_陈同学.docx', 'docx', 48901,  '2026-05-19 16:00:00', 2),
(3,  3, 'upload/result/task3_student3.pdf',   '异常处理与IO_陈同学.pdf',      'pdf',  267890, '2026-06-04 11:30:00', 2),
(5,  3, 'upload/result/task5_student3.pdf',   'HTML页面设计_陈同学.pdf',      'pdf',  312345, '2026-05-24 14:00:00', 2),
(7,  3, 'upload/result/task7_student3.pdf',   'JavaScript交互_陈同学.pdf',    'pdf',  423456, '2026-06-06 10:45:00', 2),
(8,  3, 'upload/result/task8_student3.docx',  '需求分析文档_陈同学.docx',     'docx', 134567, '2026-06-10 16:15:00', 0),
(9,  3, 'upload/result/task9_student3.docx',  'E-R图设计_陈同学.docx',        'docx', 95678,  '2026-05-26 15:20:00', 2),
(10, 3, 'upload/result/task10_student3.docx', '数据库建模_陈同学.docx',       'docx', 112345, '2026-06-03 09:40:00', 1);

-- 学生4~10: 更多提交记录
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `upload_time`, `status`) VALUES
(1,  4,  'upload/result/task1_student4.docx',   'Java基础语法练习_刘同学.docx', 'docx', 52345,  '2026-05-22 13:00:00', 2),
(2,  4,  'upload/result/task2_student4.docx',   '面向对象编程_刘同学.docx',     'docx', 85678,  '2026-05-29 15:30:00', 1),
(5,  5,  'upload/result/task5_student5.pdf',    'HTML页面设计_杨同学.pdf',      'pdf',  298765, '2026-05-25 10:20:00', 2),
(6,  5,  'upload/result/task6_student5.docx',   'CSS样式设计_杨同学.docx',      'docx', 74567,  '2026-06-01 14:45:00', 2),
(9,  6,  'upload/result/task9_student6.docx',   'E-R图设计_黄同学.docx',        'docx', 91234,  '2026-05-27 11:15:00', 2),
(10, 6,  'upload/result/task10_student6.docx',  '数据库建模_黄同学.docx',       'docx', 108765, '2026-06-04 16:30:00', 1),
(11, 7,  'upload/result/task11_student7.docx',  'Activity生命周期_周同学.docx', 'docx', 96789,  '2026-06-02 13:45:00', 2),
(12, 7,  'upload/result/task12_student7.pdf',   '数据存储_周同学.pdf',          'pdf',  389012, '2026-06-09 17:20:00', 0),
(1,  8,  'upload/result/task1_student8.docx',   'Java基础语法练习_吴同学.docx', 'docx', 49876,  '2026-05-23 14:10:00', 2),
(5,  9,  'upload/result/task5_student9.pdf',    'HTML页面设计_徐同学.pdf',      'pdf',  305432, '2026-05-26 12:00:00', 2),
(11, 10, 'upload/result/task11_student10.docx', 'Activity生命周期_孙同学.docx', 'docx', 98765,  '2026-06-03 15:00:00', 1);

-- 特殊成果: 含解析内容的成果(用于智能核查演示)
INSERT INTO `training_result` (`task_id`, `student_id`, `file_path`, `file_name`, `file_type`, `file_size`, `parsed_content`, `upload_time`, `status`) VALUES
(4, 3, 'upload/result/task4_student3.pdf', '陈同学_响应式网页设计.pdf', 'pdf', 520000,
'响应式网页设计报告\n\n设计说明：\n本次设计采用移动端优先（Mobile First）的设计理念。\n\n技术栈：\n- HTML5语义化标签\n- CSS3 Flexbox和Grid布局\n- CSS3动画和过渡效果\n- Media Query媒体查询\n\n页面结构：\n1. 顶部导航栏\n2. 轮播图区域\n3. 内容展示区\n4. 侧边栏\n5. 页脚信息\n\n响应式断点：\n- 手机端：< 768px\n- 平板端：768px - 1024px\n- PC端：> 1024px\n\n特色功能：\n- 平滑滚动\n- 懒加载图片\n- 触摸手势支持',
'2026-05-29 17:38:14', 1);


-- ==================== 六、核查记录 ====================

INSERT INTO `check_record` (`result_id`, `check_type`, `check_result`, `issue_description`, `suggestion`) VALUES
(4, 'completeness', 1, '', '文档结构完整，包含系统概述、功能模块、技术实现等必要章节'),
(4, 'logic',        1, '', '逻辑清晰，从设计理念到技术实现层层递进'),
(4, 'match',        1, '', '完全符合响应式设计要求');


-- ==================== 七、评价记录 ====================

-- 成果ID=4: 陈同学_响应式网页设计 (含AI评语和教师评语)
INSERT INTO `evaluation_record` (`result_id`, `indicator_id`, `ai_score`, `teacher_score`, `final_score`, `ai_comment`, `teacher_comment`) VALUES
(4, 1, 90.00, 93.00, 93.00, 'HTML/CSS代码规范，结构清晰',                     '代码质量优秀，语义化做得很好'),
(4, 2, 95.00, 96.00, 95.50, '文档格式规范，内容详实，图文并茂',               '文档质量很高，可以作为范例'),
(4, 3, 88.00, 90.00, 89.00, '响应式效果实现良好，适配多种设备',               '响应式设计做得很好'),
(4, 4, 85.00, 87.00, 86.00, '使用了现代CSS特性，有一定创新',                   '可以尝试使用CSS预处理器如SASS');

-- 批量评价记录(简洁模式, 无评语)
INSERT INTO `evaluation_record` (`result_id`, `indicator_id`, `ai_score`, `teacher_score`, `final_score`) VALUES
(1, 1, 85.00, 88.00, 86.50), (1, 2, 82.00, 85.00, 83.50), (1, 3, 88.00, 90.00, 89.00), (1, 4, 75.00, 80.00, 77.50),
(2, 1, 90.00, 92.00, 91.00), (2, 2, 88.00, 89.00, 88.50), (2, 3, 92.00, 93.00, 92.50), (2, 4, 85.00, 87.00, 86.00),
(3, 1, 82.00, 84.00, 83.00), (3, 2, 80.00, 82.00, 81.00), (3, 3, 85.00, 87.00, 86.00), (3, 4, 78.00, 80.00, 79.00),
(5, 1, 88.00, 90.00, 89.00), (5, 2, 85.00, 87.00, 86.00), (5, 3, 90.00, 92.00, 91.00), (5, 4, 82.00, 84.00, 83.00),
(6, 1, 83.00, 85.00, 84.00), (6, 2, 81.00, 83.00, 82.00), (6, 3, 86.00, 88.00, 87.00), (6, 4, 79.00, 81.00, 80.00),
(7, 1, 91.00, 95.00, 95.00), (7, 2, 89.00, 97.00, 97.00), (7, 3, 93.00, 97.00, 97.00), (7, 4, 87.00, 89.00, 88.00),
(9, 1, 86.00, 88.00, 87.00), (9, 2, 84.00, 86.00, 85.00), (9, 3, 89.00, 91.00, 90.00), (9, 4, 81.00, 83.00, 82.00),
(11, 1, 84.00, 86.00, 85.00), (11, 2, 82.00, 84.00, 83.00), (11, 3, 87.00, 89.00, 88.00), (11, 4, 80.00, 82.00, 81.00),
(12, 1, 89.00, 91.00, 90.00), (12, 2, 87.00, 89.00, 88.00), (12, 3, 92.00, 94.00, 93.00), (12, 4, 85.00, 87.00, 86.00),
(13, 1, 87.00, 89.00, 88.00), (13, 2, 85.00, 87.00, 86.00), (13, 3, 90.00, 92.00, 91.00), (13, 4, 83.00, 85.00, 84.00),
(15, 1, 82.00, 84.00, 83.00), (15, 2, 80.00, 82.00, 81.00), (15, 3, 85.00, 87.00, 86.00), (15, 4, 78.00, 80.00, 79.00),
(17, 1, 90.00, 92.00, 91.00), (17, 2, 88.00, 90.00, 89.00), (17, 3, 93.00, 95.00, 94.00), (17, 4, 86.00, 88.00, 87.00),
(19, 1, 85.00, 87.00, 86.00), (19, 2, 83.00, 85.00, 84.00), (19, 3, 88.00, 90.00, 89.00), (19, 4, 81.00, 83.00, 82.00);


-- ==================== 八、评价报告 ====================

INSERT INTO `evaluation_report` (`task_id`, `student_id`, `total_score`, `report_data`, `export_format`, `create_time`) VALUES
(1,  1,  86.63, '{"task":"Java基础语法练习","student":"王同学","score":86.63,"rank":"top10%"}',  NULL, '2026-05-25 10:00:00'),
(2,  1,  89.50, '{"task":"面向对象编程","student":"王同学","score":89.50,"rank":"top5%"}',        NULL, '2026-06-01 11:00:00'),
(3,  1,  82.25, '{"task":"异常处理与IO","student":"王同学","score":82.25,"rank":"top20%"}',       NULL, '2026-06-08 14:00:00'),
(6,  1,  83.25, '{"task":"CSS样式设计","student":"王同学","score":83.25,"rank":"top15%"}',        NULL, '2026-06-05 16:00:00'),
(9,  1,  86.00, '{"task":"E-R图设计","student":"王同学","score":86.00,"rank":"top12%"}',          NULL, '2026-05-30 17:00:00'),
(1,  2,  84.50, '{"task":"Java基础语法练习","student":"赵同学","score":84.50,"rank":"top15%"}',   NULL, '2026-05-26 12:00:00'),
(2,  2,  87.00, '{"task":"面向对象编程","student":"赵同学","score":87.00,"rank":"top10%"}',        NULL, '2026-06-02 13:00:00'),
(5,  2,  85.50, '{"task":"HTML页面设计","student":"赵同学","score":85.50,"rank":"top12%"}',        NULL, '2026-05-29 14:00:00'),
(11, 2,  84.25, '{"task":"Activity生命周期","student":"赵同学","score":84.25,"rank":"top18%"}',    NULL, '2026-06-04 15:00:00'),
(1,  3,  84.50, '{"task":"Java基础语法练习","student":"陈同学","score":84.50,"rank":"top15%"}',   NULL, '2026-05-24 16:00:00'),
(3,  3,  82.25, '{"task":"异常处理与IO","student":"陈同学","score":82.25,"rank":"top20%"}',       NULL, '2026-06-07 17:00:00'),
(5,  3,  88.00, '{"task":"HTML页面设计","student":"陈同学","score":88.00,"rank":"top7%"}',         NULL, '2026-05-31 18:00:00'),
(7,  3,  91.00, '{"task":"JavaScript交互","student":"陈同学","score":91.00,"rank":"top3%"}',       NULL, '2026-06-09 19:00:00'),
(9,  3,  86.00, '{"task":"E-R图设计","student":"陈同学","score":86.00,"rank":"top12%"}',           NULL, '2026-06-01 20:00:00'),
(1,  4,  83.50, '{"task":"Java基础语法练习","student":"刘同学","score":83.50,"rank":"top18%"}',   NULL, '2026-05-27 21:00:00'),
(5,  5,  86.50, '{"task":"HTML页面设计","student":"杨同学","score":86.50,"rank":"top11%"}',        NULL, '2026-05-30 22:00:00'),
(6,  5,  84.00, '{"task":"CSS样式设计","student":"杨同学","score":84.00,"rank":"top16%"}',           NULL, '2026-06-06 23:00:00'),
(9,  6,  85.00, '{"task":"E-R图设计","student":"黄同学","score":85.00,"rank":"top13%"}',           NULL, '2026-06-02 00:00:00'),
(11, 7,  86.75, '{"task":"Activity生命周期","student":"周同学","score":86.75,"rank":"top9%"}',     NULL, '2026-06-05 01:00:00'),
(1,  8,  82.00, '{"task":"Java基础语法练习","student":"吴同学","score":82.00,"rank":"top22%"}',   NULL, '2026-05-28 02:00:00'),
(5,  9,  87.50, '{"task":"HTML页面设计","student":"徐同学","score":87.50,"rank":"top6%"}',         NULL, '2026-05-31 03:00:00'),
(11, 10, 85.50, '{"task":"Activity生命周期","student":"孙同学","score":85.50,"rank":"top12%"}',    NULL, '2026-06-06 04:00:00');


-- ==================== 九、课程表 ====================

-- 张老师(teacher_id=1)的课程安排
INSERT INTO `class_schedule` (`course_id`, `teacher_id`, `day_of_week`, `start_time`, `end_time`, `location`, `remark`) VALUES
(1, 1, 1, '08:00', '09:40', '教学楼A-301', '记得带学生证'),
(1, 1, 3, '10:00', '11:40', '教学楼A-301', '收作业: Java基础语法练习'),
(2, 1, 2, '14:00', '15:40', '实验室B-202', '实验课: 面向对象编程'),
(2, 1, 4, '14:00', '15:40', '教学楼A-305', NULL),
(3, 1, 5, '08:00', '09:40', '教学楼A-301', '收作业: 异常处理练习'),
(4, 1, 1, '10:00', '11:40', '教学楼A-203', NULL),
(5, 1, 3, '14:00', '15:40', '实验室B-101', '实验课: 响应式网页设计');

-- 李老师(teacher_id=2)的课程安排
INSERT INTO `class_schedule` (`course_id`, `teacher_id`, `day_of_week`, `start_time`, `end_time`, `location`, `remark`) VALUES
(6,  2, 1, '14:00', '15:40', '教学楼C-102', '收作业: Python基础练习'),
(7,  2, 2, '08:00', '09:40', '教学楼C-102', NULL),
(8,  2, 2, '10:00', '11:40', '实验室D-301', '实验课: 数据结构实践'),
(9,  2, 4, '08:00', '09:40', '教学楼C-105', NULL),
(10, 2, 4, '10:00', '11:40', '教学楼C-102', '收作业: 数据库设计'),
(11, 2, 5, '14:00', '15:40', '教学楼C-102', NULL);


-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- 数据统计验证
-- ============================================================
SELECT '管理员'     AS `表名`, COUNT(*) AS `记录数` FROM `admin`
UNION ALL SELECT '教师',       COUNT(*) FROM `teacher`
UNION ALL SELECT '学生',       COUNT(*) FROM `student`
UNION ALL SELECT '课程',       COUNT(*) FROM `course`
UNION ALL SELECT '选课记录',   COUNT(*) FROM `course_student`
UNION ALL SELECT '实训任务',   COUNT(*) FROM `training_task`
UNION ALL SELECT '实训成果',   COUNT(*) FROM `training_result`
UNION ALL SELECT '评价指标',   COUNT(*) FROM `evaluation_indicator`
UNION ALL SELECT '评价记录',   COUNT(*) FROM `evaluation_record`
UNION ALL SELECT '评价报告',   COUNT(*) FROM `evaluation_report`
UNION ALL SELECT '核查记录',   COUNT(*) FROM `check_record`
UNION ALL SELECT '课程表',     COUNT(*) FROM `class_schedule`;

-- ============================================================
-- 完整版脚本执行完毕!
-- 共14张表, 全部数据已就绪
-- ============================================================
