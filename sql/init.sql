-- 智能实训评价系统 建表脚本
-- 数据库名: training_eval

CREATE DATABASE IF NOT EXISTS training_eval DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE training_eval;

-- ==================== 三张用户表 ====================

-- 管理员表
CREATE TABLE admin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 教师表
CREATE TABLE teacher (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    department VARCHAR(100) DEFAULT NULL COMMENT '所属院系',
    title VARCHAR(50) DEFAULT NULL COMMENT '职称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 学生表
CREATE TABLE student (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    student_no VARCHAR(30) DEFAULT NULL COMMENT '学号',
    class_name VARCHAR(50) DEFAULT NULL COMMENT '班级',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ==================== 业务表 ====================

-- 课程表
CREATE TABLE course (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    description TEXT COMMENT '课程描述',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID(关联teacher表)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1进行中 0已结束',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 实训任务表
CREATE TABLE training_task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    requirements TEXT COMMENT '实训要求',
    deadline DATETIME DEFAULT NULL COMMENT '截止时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1进行中 2已结束 0草稿',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训任务表';

-- 实训成果表
CREATE TABLE training_result (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    student_id BIGINT NOT NULL COMMENT '学生ID(关联student表)',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_type VARCHAR(20) NOT NULL COMMENT '文件类型: docx/pdf/jpg/png',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    parsed_content TEXT COMMENT '解析后的文本内容',
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待核查 1已核查 2已评价',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实训成果表';

-- 评价指标表
CREATE TABLE evaluation_indicator (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '指标名称',
    description TEXT COMMENT '指标描述',
    category VARCHAR(50) DEFAULT NULL COMMENT '指标分类',
    default_weight DECIMAL(5,2) NOT NULL DEFAULT 20.00 COMMENT '默认权重(%)',
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置: 1是 0否',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价指标表';

-- 评价记录表
CREATE TABLE evaluation_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    result_id BIGINT NOT NULL COMMENT '实训成果ID',
    indicator_id BIGINT NOT NULL COMMENT '评价指标ID',
    ai_score DECIMAL(5,2) DEFAULT NULL COMMENT 'AI评分',
    teacher_score DECIMAL(5,2) DEFAULT NULL COMMENT '教师评分',
    final_score DECIMAL(5,2) DEFAULT NULL COMMENT '最终得分',
    ai_comment TEXT COMMENT 'AI评语',
    teacher_comment TEXT COMMENT '教师评语',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_result_id (result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价记录表';

-- 评价报告表
CREATE TABLE evaluation_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    student_id BIGINT NOT NULL COMMENT '学生ID(关联student表)',
    total_score DECIMAL(5,2) DEFAULT NULL COMMENT '总评分',
    report_data TEXT COMMENT '报告数据(JSON)',
    export_format VARCHAR(10) DEFAULT NULL COMMENT '导出格式: excel/pdf',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价报告表';

-- 核查记录表
CREATE TABLE check_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    result_id BIGINT NOT NULL COMMENT '实训成果ID',
    check_type VARCHAR(50) NOT NULL COMMENT '核查类型: completeness/logic/match',
    check_result TINYINT NOT NULL DEFAULT 0 COMMENT '核查结果: 0待核查 1通过 2存在问题',
    issue_description TEXT COMMENT '问题描述',
    suggestion TEXT COMMENT '改进建议',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_result_id (result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核查记录表';

-- 课程-学生关联表
CREATE TABLE course_student (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_id BIGINT NOT NULL COMMENT '学生ID(关联student表)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_student (course_id, student_id),
    KEY idx_course_id (course_id),
    KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程-学生关联表';

-- ==================== 初始数据 ====================

-- 管理员 (密码: admin123)
INSERT INTO admin (username, password, real_name, email, phone) VALUES
('admin', 'admin123', '系统管理员', 'admin@qcby.com', '13800000001');

-- 教师 (密码: 123456)
INSERT INTO teacher (username, password, real_name, email, phone, department, title) VALUES
('teacher01', '123456', '张老师', 'zhang@qcby.com', '13800000002', '计算机学院', '副教授'),
('teacher02', '123456', '李老师', 'li@qcby.com', '13800000003', '软件学院', '讲师');

-- 学生 (密码: 123456)
INSERT INTO student (username, password, real_name, email, phone, student_no, class_name) VALUES
('student01', '123456', '王同学', 'wang@stu.com', '13800000004', '2024010001', '计科2401'),
('student02', '123456', '赵同学', 'zhao@stu.com', '13800000005', '2024010002', '计科2401'),
('student03', '123456', '陈同学', 'chen@stu.com', '13800000006', '2024010003', '软件2401');

-- 课程 (teacher_id 关联 teacher 表)
INSERT INTO course (name, description, teacher_id, status) VALUES
('Java程序设计', 'Java语言基础与面向对象编程', 1, 1),
('Web前端开发', 'HTML/CSS/JavaScript前端技术', 2, 1);

-- 学生选课 (student_id 关联 student 表)
INSERT INTO course_student (course_id, student_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2);

-- 默认评价指标
INSERT INTO evaluation_indicator (name, description, category, default_weight, is_system) VALUES
('代码质量', '代码规范性、可读性、注释完整性', '技术实现', 25.00, 1),
('文档规范性', '文档格式、内容完整性、表述清晰度', '文档规范', 25.00, 1),
('功能实现度', '功能需求覆盖率、核心功能完成情况', '功能实现', 30.00, 1),
('创新与拓展', '创新点、额外功能、技术深度', '综合能力', 20.00, 1);