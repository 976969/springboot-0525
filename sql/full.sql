-- ============================================================
-- 智能实训教学评价系统 - 完整数据库脚本 (training_eval)
-- 以当前数据库导出为准，可一键执行完成建库+建表+数据插入
-- ============================================================
CREATE DATABASE IF NOT EXISTS training_eval DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE training_eval;

/*
 Navicat Premium Data Transfer

 Source Server         : 1201
 Source Server Type    : MySQL
 Source Server Version : 50740
 Source Host           : localhost:3306
 Source Schema         : training_eval

 Target Server Type    : MySQL
 Target Server Version : 50740
 File Encoding         : 65001

 Date: 17/07/2026 12:48:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'admin123', '系统管理员', '/api/uploads/avatar/admin_1_1783419445816.webp', 'admin@qcby.com', '13800000001', 1, '2026-07-02 14:41:06', '2026-07-07 18:17:25');

-- ----------------------------
-- Table structure for ai_practice
-- ----------------------------
DROP TABLE IF EXISTS `ai_practice`;
CREATE TABLE `ai_practice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `questions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目数据(JSON数组)',
  `answers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '学生答案(JSON)',
  `score` decimal(5, 2) DEFAULT NULL COMMENT '得分',
  `total_score` decimal(5, 2) DEFAULT 100.00 COMMENT '总分',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI练习题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_practice
-- ----------------------------
INSERT INTO `ai_practice` VALUES (1, 1, 1, 'Java程序设计', '[\n    {\"id\":1,\"question\":\"Java语言的编译结果是什么？\",\"options\":{\"A\":\".java\",\"B\":\".class\",\"C\":\".jar\",\"D\":\".exe\"},\"answer\":\"B\",\"explanation\":\"Java源代码经过编译后生成字节码文件，文件扩展名为.class。\"},\n    {\"id\":2,\"question\":\"下列哪个不是Java的基本数据类型？\",\"options\":{\"A\":\"int\",\"B\":\"float\",\"C\":\"String\",\"D\":\"double\"},\"answer\":\"C\",\"explanation\":\"String是对象类型，不是基本数据类型。\"},\n    {\"id\":3,\"question\":\"Java中创建对象的关键字是什么？\",\"options\":{\"A\":\"new\",\"B\":\"create\",\"C\":\"instance\",\"D\":\"object\"},\"answer\":\"A\",\"explanation\":\"使用new关键字可以创建对象。\"},\n    {\"id\":4,\"question\":\"Java中的抽象类使用哪个关键字来定义？\",\"options\":{\"A\":\"abstract\",\"B\":\"interface\",\"C\":\"final\",\"D\":\"static\"},\"answer\":\"A\",\"explanation\":\"abstract关键字用于定义抽象类。\"},\n    {\"id\":5,\"question\":\"Java中用于实现线程同步的方法是什么？\",\"options\":{\"A\":\"start()\",\"B\":\"run()\",\"C\":\"sleep()\",\"D\":\"synchronized\"},\"answer\":\"D\",\"explanation\":\"synchronized关键字用于实现线程同步。\"},\n    {\"id\":6,\"question\":\"下列哪个是Java中构造函数的特点？\",\"options\":{\"A\":\"返回类型是void\",\"B\":\"函数名与类名不同\",\"C\":\"不能使用return语句返回值\",\"D\":\"必须有返回值类型\"},\"answer\":\"C\",\"explanation\":\"构造函数用于初始化对象，没有返回类型，不能用return语句返回值。\"},\n    {\"id\":7,\"question\":\"Java中接口使用哪个关键字来定义？\",\"options\":{\"A\":\"abstract\",\"B\":\"interface\",\"C\":\"class\",\"D\":\"implements\"},\"answer\":\"B\",\"explanation\":\"interface关键字用于定义接口。\"},\n    {\"id\":8,\"question\":\"下列哪个类是所有Java类的直接或间接父类？\",\"options\":{\"A\":\"Object\",\"B\":\"java.lang.Object\",\"C\":\"Class\",\"D\":\"String\"},\"answer\":\"B\",\"explanation\":\"所有Java类都是Object类的子类，而Object类定义在java.lang包内。\"},\n    {\"id\":9,\"question\":\"以下哪个方法可以用来判断对象是否为空？\",\"options\":{\"A\":\"isEmpty()\",\"B\":\"isNull()\",\"C\":\"equals()\",\"D\":\"==\"},\"answer\":\"D\",\"explanation\":\"使用\\\"==\\\"来判断引用是否指向同一个对象，或者使用equals()方法判断对象内容是否相等。\"},\n    {\"id\":10,\"question\":\"Java中异常处理的finally块有什么特性？\",\"options\":{\"A\":\"总是被执行\",\"B\":\"只在try块执行后执行\",\"C\":\"只在catch块执行后执行\",\"D\":\"在任何情况下都不会被执行\"},\"answer\":\"A\",\"explanation\":\"finally块中的代码无论是否发生异常，都会被执行。\"}\n]', '{\"1\":\"A\",\"2\":\"C\",\"3\":\"A\",\"4\":\"A\",\"5\":\"D\",\"6\":\"A\",\"7\":\"B\",\"8\":\"B\",\"9\":\"A\",\"10\":\"A\"}', 70.00, 100.00, '2026-07-02 14:50:35');
INSERT INTO `ai_practice` VALUES (2, 1, 3, '数据库原理', '[{\"id\":1,\"question\":\"在MySQL中，以下哪个关键字用于定义一个字段不允许为空？\",\"options\":{\"A\":\"DEFAULT\",\"B\":\"NULL\",\"C\":\"NOT NULL\",\"D\":\"AUTO_INCREMENT\"},\"answer\":\"C\",\"explanation\":\"NOT NULL关键字用于定义一个字段不允许为空。\"},\n{\"id\":2,\"question\":\"以下哪个MySQL存储引擎支持事务处理？\",\"options\":{\"A\":\"MyISAM\",\"B\":\"InnoDB\",\"C\":\"MEMORY\",\"D\":\"CSV\"},\"answer\":\"B\",\"explanation\":\"InnoDB存储引擎支持事务处理。\"},\n{\"id\":3,\"question\":\"在MySQL中，用于创建数据库的SQL语句是？\",\"options\":{\"A\":\"CREATE TABLE\",\"B\":\"CREATE DATABASE\",\"C\":\"INSERT INTO\",\"D\":\"UPDATE TABLE\"},\"answer\":\"B\",\"explanation\":\"CREATE DATABASE语句用于创建数据库。\"},\n{\"id\":4,\"question\":\"在MySQL中，以下哪个关键词用于创建一个索引？\",\"options\":{\"A\":\"INDEX\",\"B\":\"CREATE INDEX\",\"C\":\"ADD INDEX\",\"D\":\"ALTER INDEX\"},\"answer\":\"B\",\"explanation\":\"CREATE INDEX语句用于创建一个索引。\"},\n{\"id\":5,\"question\":\"在MySQL中，以下哪个命令用于查看表的结构？\",\"options\":{\"A\":\"SHOW STRUCTURE\",\"B\":\"DESCRIBE\",\"C\":\"DISPLAY\",\"D\":\"SHOW DATABASE\"},\"answer\":\"B\",\"explanation\":\"DESCRIBE语句用于查看表的结构。\"},\n{\"id\":6,\"question\":\"在MySQL中，用于删除表的正确命令是？\",\"options\":{\"A\":\"DELETE TABLE\",\"B\":\"DROP TABLE\",\"C\":\"REMOVE TABLE\",\"D\":\"ERASE TABLE\"},\"answer\":\"B\",\"explanation\":\"DROP TABLE语句用于删除表。\"},\n{\"id\":7,\"question\":\"在MySQL中，以下哪个关键字用于定义一个字段为自动增长？\",\"options\":{\"A\":\"INCREMENT\",\"B\":\"AUTO INCREMENT\",\"C\":\"AUTO_INCREMENT\",\"D\":\"DEFAULT AUTO_INCREMENT\"},\"answer\":\"C\",\"explanation\":\"AUTO_INCREMENT关键字用于定义一个字段为自动增长。\"},\n{\"id\":8,\"question\":\"在MySQL中，用于插入数据的正确命令是？\",\"options\":{\"A\":\"INSERT INTO\",\"B\":\"ADD INTO\",\"C\":\"INSERT FROM\",\"D\":\"LOAD INTO\"},\"answer\":\"A\",\"explanation\":\"INSERT INTO语句用于插入数据。\"},\n{\"id\":9,\"question\":\"在MySQL中，用于更新数据的正确命令是？\",\"options\":{\"A\":\"UPDATE TABLE\",\"B\":\"MODIFY\",\"C\":\"REPLACE\",\"D\":\"UPDATE\"},\"answer\":\"D\",\"explanation\":\"UPDATE语句用于更新数据。\"},\n{\"id\":10,\"question\":\"在MySQL中，用于删除数据的正确命令是？\",\"options\":{\"A\":\"DELETE\",\"B\":\"REMOVE\",\"C\":\"DISCARD\",\"D\":\"CLEAR\"},\"answer\":\"A\",\"explanation\":\"DELETE语句用于删除数据。\"}]', NULL, NULL, 100.00, '2026-07-02 22:16:02');
INSERT INTO `ai_practice` VALUES (3, 1, 2, 'Web前端开发', '[\n{\"id\":1,\"question\":\"在HTML中，用于定义文档类型的是哪个标签？\",\"options\":{\"A\":\"<html>\",\"B\":\"<head>\",\"C\":\"<!DOCTYPE>\",\"D\":\"<body>\"},\"answer\":\"C\",\"explanation\":\"<!DOCTYPE>声明用于告诉浏览器当前页面使用的HTML版本，必须位于文档的第一行。\"},\n{\"id\":2,\"question\":\"CSS中，以下哪个属性用于设置元素的内边距？\",\"options\":{\"A\":\"margin\",\"B\":\"padding\",\"C\":\"border\",\"D\":\"outline\"},\"answer\":\"B\",\"explanation\":\"padding属性定义元素内容与边框之间的空间，即内边距；margin是外边距。\"},\n{\"id\":3,\"question\":\"JavaScript中，以下哪个方法可以将字符串转换为整数？\",\"options\":{\"A\":\"parseInt()\",\"B\":\"parseFloat()\",\"C\":\"toString()\",\"D\":\"Number()\"},\"answer\":\"A\",\"explanation\":\"parseInt()函数解析字符串并返回整数；parseFloat()返回浮点数；Number()可转换数字但更严格。\"},\n{\"id\":4,\"question\":\"在HTML中，哪个标签用于创建超链接？\",\"options\":{\"A\":\"<link>\",\"B\":\"<a>\",\"C\":\"<href>\",\"D\":\"<url>\"},\"answer\":\"B\",\"explanation\":\"<a>标签（锚点标签）用于定义超链接，通过href属性指定链接目标。\"},\n{\"id\":5,\"question\":\"CSS盒模型中，哪个属性用于设置边框的样式？\",\"options\":{\"A\":\"border-style\",\"B\":\"border-color\",\"C\":\"border-width\",\"D\":\"border-radius\"},\"answer\":\"A\",\"explanation\":\"border-style属性定义边框的样式（如solid、dashed等），其他属性分别控制颜色、宽度和圆角。\"},\n{\"id\":6,\"question\":\"JavaScript中，用于声明常量的关键字是什么？\",\"options\":{\"A\":\"var\",\"B\":\"let\",\"C\":\"const\",\"D\":\"static\"},\"answer\":\"C\",\"explanation\":\"const用于声明常量，其值在声明后不可重新赋值；var和let用于变量声明。\"},\n{\"id\":7,\"question\":\"以下哪个CSS选择器的优先级最高？\",\"options\":{\"A\":\"类选择器（.class）\",\"B\":\"ID选择器（#id）\",\"C\":\"元素选择器（div）\",\"D\":\"通配选择器（*）\"},\"answer\":\"B\",\"explanation\":\"ID选择器的优先级高于类选择器、元素选择器和通配选择器，内联样式优先级更高。\"},\n{\"id\":8,\"question\":\"在HTML表单中，哪个输入类型用于创建提交按钮？\",\"options\":{\"A\":\"text\",\"B\":\"password\",\"C\":\"submit\",\"D\":\"button\"},\"answer\":\"C\",\"explanation\":\"type=\\\"submit\\\"创建提交按钮，用于将表单数据发送到服务器；type=\\\"button\\\"为普通按钮。\"},\n{\"id\":9,\"question\":\"JavaScript中，以下哪个方法可以向数组末尾添加一个元素？\",\"options\":{\"A\":\"pop()\",\"B\":\"push()\",\"C\":\"shift()\",\"D\":\"unshift()\"},\"answer\":\"B\",\"explanation\":\"push()方法在数组末尾添加一个或多个元素，并返回新长度；pop()移除末尾元素。\"},\n{\"id\":10,\"question\":\"CSS中，如何使文本居中对齐？\",\"options\":{\"A\":\"text-align: center\",\"B\":\"align: center\",\"C\":\"vertical-align: middle\",\"D\":\"float: center\"},\"answer\":\"A\",\"explanation\":\"text-align属性用于设置文本的水平对齐方式，center值使文本居中；其他选项不正确。\"}\n]', '{\"1\":\"A\",\"2\":\"B\",\"3\":\"A\",\"4\":\"B\",\"5\":\"A\",\"6\":\"A\",\"7\":\"A\",\"8\":\"C\",\"9\":\"B\",\"10\":\"A\"}', 70.00, 100.00, '2026-07-16 22:56:32');

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '内容描述',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片地址',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '跳转链接',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序(越小越靠前)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1上架 0下架',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, '111', '111111', '/api/banner-image/368737d8b0a2418994ffa46e52c330bf.jpg', NULL, 0, 0, '2026-07-02 15:07:51', '2026-07-07 18:22:50');
INSERT INTO `banner` VALUES (2, '222', '222', '/api/banner-image/651c01eefe044492a2fd062a66499883.webp', NULL, 0, 0, '2026-07-02 15:08:02', '2026-07-07 18:22:50');
INSERT INTO `banner` VALUES (3, '111', '1111', '/api/banner-image/93ea3b7fc497463dbcf5f0c5af9ba1d6.jpg', NULL, 0, 1, '2026-07-07 18:22:48', '2026-07-07 18:22:48');
INSERT INTO `banner` VALUES (4, '222', '2222', '/api/banner-image/7b319868dcbc4415bef56c374be3d716.webp', NULL, 0, 1, '2026-07-07 18:23:00', '2026-07-07 18:23:00');

-- ----------------------------
-- Table structure for class_schedule
-- ----------------------------
DROP TABLE IF EXISTS `class_schedule`;
CREATE TABLE `class_schedule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `teacher_id` bigint(20) NOT NULL COMMENT '教师ID',
  `day_of_week` tinyint(4) NOT NULL COMMENT '星期几: 1周一 ~ 7周日',
  `start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '开始时间(如 08:00)',
  `end_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结束时间(如 09:40)',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上课地点',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE,
  INDEX `idx_day_of_week`(`day_of_week`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程表(上课时间安排)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of class_schedule
-- ----------------------------
INSERT INTO `class_schedule` VALUES (1, 1, 1, 1, '08:00', '09:40', '教学楼A-301', '记得带学生证', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (2, 1, 1, 3, '10:00', '11:40', '教学楼A-301', '收作业: Java基础语法练习', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (3, 2, 1, 2, '14:00', '15:40', '实验室B-202', '实验课: 面向对象编程', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (4, 2, 1, 4, '14:00', '15:40', '教学楼A-305', NULL, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (5, 3, 1, 5, '08:00', '09:40', '教学楼A-301', '收作业: 异常处理练习', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (6, 4, 1, 1, '10:00', '11:40', '教学楼A-203', NULL, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (7, 5, 1, 3, '14:00', '15:40', '实验室B-101', '实验课: 响应式网页设计', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (8, 6, 2, 1, '14:00', '15:40', '教学楼C-102', '收作业: Python基础练习', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (9, 7, 2, 2, '08:00', '09:40', '教学楼C-102', NULL, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (10, 8, 2, 2, '10:00', '11:40', '实验室D-301', '实验课: 数据结构实践', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (11, 9, 2, 4, '08:00', '09:40', '教学楼C-105', NULL, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (12, 10, 2, 4, '10:00', '11:40', '教学楼C-102', '收作业: 数据库设计', '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (13, 11, 2, 5, '14:00', '15:40', '教学楼C-102', NULL, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `class_schedule` VALUES (14, 1, 1, 6, '08:00', '09:40', '', '', '2026-07-07 18:59:28', '2026-07-07 18:59:28');

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '课程描述',
  `teacher_id` bigint(20) NOT NULL COMMENT '授课教师ID(关联teacher表)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1进行中 0已结束',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (1, 'Java程序设计', 'Java语言基础与面向对象编程', 1, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `course` VALUES (2, 'Web前端开发', 'HTML/CSS/JavaScript前端技术', 2, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `course` VALUES (3, '数据库原理', 'MySQL数据库设计与应用', 1, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `course` VALUES (4, '软件工程', '软件开发过程与方法', 1, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `course` VALUES (5, '移动应用开发', 'Android应用开发', 2, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `course` VALUES (7, '111', '111', 1, 1, '2026-07-16 18:44:55', '2026-07-16 18:44:55');

-- ----------------------------
-- Table structure for course_student
-- ----------------------------
DROP TABLE IF EXISTS `course_student`;
CREATE TABLE `course_student`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID(关联student表)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_student`(`course_id`, `student_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程-学生关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_student
-- ----------------------------
INSERT INTO `course_student` VALUES (1, 1, 1, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (2, 1, 2, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (3, 1, 3, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (4, 2, 1, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (5, 2, 2, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (6, 3, 1, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (7, 3, 2, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (8, 3, 3, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (9, 3, 4, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (10, 3, 5, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (11, 4, 1, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (12, 4, 3, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (13, 4, 5, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (14, 4, 6, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (15, 4, 7, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (16, 5, 2, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (17, 5, 4, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (18, 5, 6, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (19, 5, 8, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (20, 5, 9, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (21, 5, 10, '2026-07-02 14:41:06');
INSERT INTO `course_student` VALUES (22, 7, 1, '2026-07-16 18:52:45');

-- ----------------------------
-- Table structure for evaluation_indicator
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_indicator`;
CREATE TABLE `evaluation_indicator`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指标名称',
  `teacher_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '归属教师ID，0表示系统模板',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '指标描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '指标分类',
  `default_weight` decimal(5, 2) NOT NULL DEFAULT 20.00 COMMENT '当前权重',
  `original_weight` decimal(5, 2) NOT NULL DEFAULT 20.00 COMMENT '原始权重（用于重置）',
  `is_system` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否系统内置: 1是 0否',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评价指标表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_indicator
-- ----------------------------
INSERT INTO `evaluation_indicator` VALUES (1, '代码质量', 0, '代码规范性、可读性、注释完整性', '技术实现', 25.00, 25.00, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `evaluation_indicator` VALUES (2, '文档规范性', 0, '文档格式、内容完整性、表述清晰度', '文档规范', 25.00, 25.00, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `evaluation_indicator` VALUES (3, '功能实现度', 0, '功能需求覆盖率、核心功能完成情况', '功能实现', 30.00, 30.00, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `evaluation_indicator` VALUES (4, '创新与拓展', 0, '创新点、额外功能、技术深度', '综合能力', 20.00, 20.00, 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `evaluation_indicator` VALUES (7, '代码质量', 1, '代码质量', '技术实现', 17.00, 25.00, 1, '2026-07-02 14:42:24', '2026-07-16 21:37:26');
INSERT INTO `evaluation_indicator` VALUES (8, '文档规范性', 1, '文档规范性', '文档规范', 17.00, 25.00, 1, '2026-07-02 14:42:24', '2026-07-16 21:37:26');
INSERT INTO `evaluation_indicator` VALUES (9, '功能实现度', 1, '功能实现度', '功能实现', 17.00, 30.00, 1, '2026-07-02 14:42:24', '2026-07-16 21:37:26');
INSERT INTO `evaluation_indicator` VALUES (10, '创新与拓展', 1, '创新与拓展', '综合能力', 17.00, 20.00, 1, '2026-07-02 14:42:24', '2026-07-16 21:37:26');
INSERT INTO `evaluation_indicator` VALUES (11, '团队协作', 1, '团队协作', '团队协作', 16.00, 15.00, 0, '2026-07-02 14:42:24', '2026-07-16 21:37:26');
INSERT INTO `evaluation_indicator` VALUES (12, '项目展示', 1, '项目展示', '综合能力', 16.00, 10.00, 0, '2026-07-02 14:42:24', '2026-07-16 21:37:26');

-- ----------------------------
-- Table structure for evaluation_record
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_record`;
CREATE TABLE `evaluation_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `result_id` bigint(20) NOT NULL COMMENT '实训成果ID',
  `indicator_id` bigint(20) NOT NULL COMMENT '评价指标ID',
  `ai_score` decimal(5, 2) DEFAULT NULL COMMENT 'AI评分',
  `teacher_score` decimal(5, 2) DEFAULT NULL COMMENT '教师评分',
  `final_score` decimal(5, 2) DEFAULT NULL COMMENT '最终得分',
  `ai_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'AI评语',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '教师评语',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `score_ratio` decimal(3, 1) DEFAULT 5.0 COMMENT '评分比重(AI权重,默认5:5)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_result_id`(`result_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评价记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_record
-- ----------------------------
INSERT INTO `evaluation_record` VALUES (1, 4, 1, 90.00, NULL, 93.00, 'HTML/CSS代码规范，结构清晰', NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (2, 4, 2, 95.00, NULL, 95.50, '文档格式规范，内容详实，图文并茂', NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (3, 4, 3, 88.00, NULL, 89.00, '响应式效果实现良好，适配多种设备', NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (4, 4, 4, 85.00, NULL, 86.00, '使用了现代CSS特性，有一定创新', NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (5, 1, 1, 85.00, NULL, 86.50, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (6, 1, 2, 82.00, NULL, 83.50, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (7, 1, 3, 88.00, NULL, 89.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (8, 1, 4, 75.00, NULL, 77.50, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (9, 2, 1, 90.00, NULL, 91.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (10, 2, 2, 88.00, NULL, 88.50, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (11, 2, 3, 92.00, NULL, 92.50, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (12, 2, 4, 85.00, NULL, 86.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (13, 3, 1, 82.00, NULL, 83.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (14, 3, 2, 80.00, NULL, 81.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (15, 3, 3, 85.00, NULL, 86.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (16, 3, 4, 78.00, NULL, 79.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (17, 5, 1, 88.00, NULL, 89.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (18, 5, 2, 85.00, NULL, 86.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (19, 5, 3, 90.00, NULL, 91.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (20, 5, 4, 82.00, NULL, 83.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (21, 6, 1, 83.00, NULL, 84.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (22, 6, 2, 81.00, NULL, 82.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (23, 6, 3, 86.00, NULL, 87.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (24, 6, 4, 79.00, NULL, 80.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (25, 7, 1, 91.00, NULL, 95.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (26, 7, 2, 89.00, NULL, 97.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (27, 7, 3, 93.00, NULL, 97.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (28, 7, 4, 87.00, NULL, 88.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (29, 9, 1, 86.00, NULL, 87.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (30, 9, 2, 84.00, NULL, 85.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (31, 9, 3, 89.00, NULL, 90.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (32, 9, 4, 81.00, NULL, 82.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (33, 11, 1, 84.00, NULL, 85.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (34, 11, 2, 82.00, NULL, 83.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (35, 11, 3, 87.00, NULL, 88.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (36, 11, 4, 80.00, NULL, 81.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (37, 12, 1, 89.00, NULL, 90.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (38, 12, 2, 87.00, NULL, 88.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (39, 12, 3, 92.00, NULL, 93.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (40, 12, 4, 85.00, NULL, 86.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (41, 13, 1, 87.00, NULL, 88.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (42, 13, 2, 85.00, NULL, 86.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (43, 13, 3, 90.00, NULL, 91.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (44, 13, 4, 83.00, NULL, 84.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (45, 15, 1, 82.00, NULL, 83.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (46, 15, 2, 80.00, NULL, 81.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (47, 15, 3, 85.00, NULL, 86.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (48, 15, 4, 78.00, NULL, 79.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (49, 17, 1, 90.00, NULL, 91.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (50, 17, 2, 88.00, NULL, 89.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (51, 17, 3, 93.00, NULL, 94.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (52, 17, 4, 86.00, NULL, 87.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (53, 19, 1, 85.00, NULL, 86.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (54, 19, 2, 83.00, NULL, 84.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (55, 19, 3, 88.00, NULL, 89.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (56, 19, 4, 81.00, NULL, 82.00, NULL, NULL, '2026-07-02 14:41:06', '2026-07-16 19:31:36', 5.0);
INSERT INTO `evaluation_record` VALUES (63, 33, 7, 0.00, NULL, 0.00, '学生提交的实训成果内容与实训要求（1111）完全不符，且评价指标为“代码质量”，但提交内容为《习近平谈治国理政》学习心得体会，未涉及任何编程代码、软件开发或技术实现。因此，无法依据代码质量指标进行评分，得分为0分。建议学生重新提交符合实训要求的代码相关成果。', NULL, '2026-07-16 21:50:21', '2026-07-16 21:50:21', 5.0);
INSERT INTO `evaluation_record` VALUES (64, 33, 8, 60.00, NULL, 60.00, '学生提交的实训成果是一篇关于《习近平谈治国理政》的学习心得体会，内容详实、感悟深刻，展现了良好的理论素养和思考深度。然而，从‘文档规范性’这一评价指标来看，存在明显不足：首先，实训要求明确为‘1111’，但学生提交的是一篇完整的文章，未遵循该具体要求，导致内容与要求不符；其次，文档缺乏标题、作者、日期、格式规范等基本要素，也未按标准文档结构（如摘要、正文、结论）组织；此外，文中部分段落存在重复和冗余（如多次出现‘统筹发展和安全’、‘科技’等内容），结构不够清晰。因此，尽管内容本身质量较高，但规范性严重不足，影响了整体评分。', NULL, '2026-07-16 21:50:23', '2026-07-16 21:50:23', 5.0);
INSERT INTO `evaluation_record` VALUES (65, 33, 9, 0.00, NULL, 0.00, '该学生提交的是一篇《习近平谈治国理政》的学习心得体会，属于思想政治类文章，与实训要求“1111”完全不符。实训要求未明确具体内容，但通常涉及软件开发、系统实现等技术性任务，而本成果未展示任何功能实现。因此，功能实现度为0分。建议学生仔细阅读实训要求，提交与任务直接相关的技术成果。', NULL, '2026-07-16 21:50:25', '2026-07-16 21:50:25', 5.0);
INSERT INTO `evaluation_record` VALUES (66, 33, 10, 85.00, NULL, 85.00, '该学生的实训成果在‘创新与拓展’方面表现良好。内容紧扣《习近平谈治国理政》的学习心得，展现了深入的理论思考和系统的知识整合。学生能够从思想定位、学习方法、学习感悟、理论贡献和实践价值等多个维度进行拓展分析，体现了较强的逻辑性和知识迁移能力。特别是在‘科技对社会进步的推动作用’部分，学生结合了公共卫生、经济安全、文化创新等具体案例，将理论联系实际，具有一定的创新视角。然而，部分内容与实训要求‘1111’关联不够紧密，需进一步明确创新点与拓展方向。建议在后续实训中更聚焦于原创性见解或应用性拓展。', NULL, '2026-07-16 21:50:28', '2026-07-16 21:50:28', 5.0);
INSERT INTO `evaluation_record` VALUES (67, 33, 11, 85.00, NULL, 85.00, '该学生对《习近平谈治国理政》的学习心得体现了较强的独立思考能力和理论联系实际的水平，能从思想定位、学习方法、学习感悟、理论贡献、实践价值等多个维度进行系统阐述，逻辑清晰、内容充实。但评价指标为‘团队协作’，而实训成果内容为个人心得体会，未涉及任何团队协作相关描述或实践，因此无法依据该指标进行评分。建议在后续实训中明确体现团队分工、协作过程、角色贡献等要素。', NULL, '2026-07-16 21:50:30', '2026-07-16 21:50:30', 5.0);
INSERT INTO `evaluation_record` VALUES (68, 33, 12, 85.00, NULL, 85.00, '学生实训成果展现了扎实的理论学习能力和深刻的思考深度，对《习近平谈治国理政》的核心思想、学习方法、实践价值进行了系统梳理和独到分析。评语结构清晰，逻辑严密，语言表达准确且有感染力。然而，项目展示要求可能更侧重于实践性或技术性应用，而本成果以心得体会为主，缺乏具体项目或技术实现的展示，与实训要求‘1111’的指向性不够匹配，因此扣分。建议在后续实训中明确项目展示的实操部分，将理论感悟与实际项目开发或技术应用相结合，以更全面地体现综合能力。', NULL, '2026-07-16 21:50:33', '2026-07-16 21:50:33', 5.0);

-- ----------------------------
-- Table structure for evaluation_report
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_report`;
CREATE TABLE `evaluation_report`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID(关联student表)',
  `total_score` decimal(5, 2) DEFAULT NULL COMMENT '总评分',
  `teacher_score` decimal(5, 2) DEFAULT NULL COMMENT '教师评分',
  `teacher_score_ratio` decimal(3, 1) DEFAULT 0.5 COMMENT '教师评分比重',
  `teacher_id` bigint(20) DEFAULT NULL COMMENT '教师ID',
  `report_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '报告数据(JSON)',
  `export_format` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '导出格式: excel/pdf',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0草稿(教师评分中) 1已发布(学生可见)',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '教师评语',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评价报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation_report
-- ----------------------------
INSERT INTO `evaluation_report` VALUES (1, 1, 1, 86.63, 88.00, 0.5, 1, '{\"task\":\"Java基础语法练习\",\"student\":\"王同学\",\"score\":86.63,\"rank\":\"top10%\"}', NULL, '2026-05-25 10:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (2, 2, 1, 89.50, 91.00, 0.5, 1, '{\"task\":\"面向对象编程\",\"student\":\"王同学\",\"score\":89.50,\"rank\":\"top5%\"}', NULL, '2026-06-01 11:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (3, 3, 1, 82.25, 84.00, 0.5, 1, '{\"task\":\"异常处理与IO\",\"student\":\"王同学\",\"score\":82.25,\"rank\":\"top20%\"}', NULL, '2026-06-08 14:00:00', 0, NULL);
INSERT INTO `evaluation_report` VALUES (4, 6, 1, 83.25, 85.00, 0.5, 2, '{\"task\":\"CSS样式设计\",\"student\":\"王同学\",\"score\":83.25,\"rank\":\"top15%\"}', NULL, '2026-06-05 16:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (5, 9, 1, 86.00, 87.00, 0.5, 1, '{\"task\":\"E-R图设计\",\"student\":\"王同学\",\"score\":86.00,\"rank\":\"top12%\"}', NULL, '2026-05-30 17:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (6, 1, 2, 84.50, 86.00, 0.5, 1, '{\"task\":\"Java基础语法练习\",\"student\":\"赵同学\",\"score\":84.50,\"rank\":\"top15%\"}', NULL, '2026-05-26 12:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (7, 2, 2, 87.00, 89.00, 0.5, 1, '{\"task\":\"面向对象编程\",\"student\":\"赵同学\",\"score\":87.00,\"rank\":\"top10%\"}', NULL, '2026-06-02 13:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (8, 5, 2, 85.50, 87.00, 0.5, 2, '{\"task\":\"HTML页面设计\",\"student\":\"赵同学\",\"score\":85.50,\"rank\":\"top12%\"}', NULL, '2026-05-29 14:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (9, 11, 2, 84.25, 86.00, 0.5, 2, '{\"task\":\"Activity生命周期\",\"student\":\"赵同学\",\"score\":84.25,\"rank\":\"top18%\"}', NULL, '2026-06-04 15:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (10, 1, 3, 84.50, 86.00, 0.5, 1, '{\"task\":\"Java基础语法练习\",\"student\":\"陈同学\",\"score\":84.50,\"rank\":\"top15%\"}', NULL, '2026-05-24 16:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (11, 3, 3, 82.25, 84.00, 0.5, 1, '{\"task\":\"异常处理与IO\",\"student\":\"陈同学\",\"score\":82.25,\"rank\":\"top20%\"}', NULL, '2026-06-07 17:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (12, 5, 3, 88.00, 90.00, 0.5, 2, '{\"task\":\"HTML页面设计\",\"student\":\"陈同学\",\"score\":88.00,\"rank\":\"top7%\"}', NULL, '2026-05-31 18:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (13, 7, 3, 91.00, 93.00, 0.5, 1, '{\"task\":\"JavaScript交互\",\"student\":\"陈同学\",\"score\":91.00,\"rank\":\"top3%\"}', NULL, '2026-06-09 19:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (14, 9, 3, 85.25, 87.00, 0.5, 1, '{\"indicators\":[{\"aiComment\":null,\"finalScore\":86.00,\"indicatorName\":\"代码质量\",\"aiScore\":85.00,\"teacherComment\":null,\"teacherScore\":87.00},{\"aiComment\":null,\"finalScore\":84.00,\"indicatorName\":\"文档规范性\",\"aiScore\":83.00,\"teacherComment\":null,\"teacherScore\":85.00},{\"aiComment\":null,\"finalScore\":89.00,\"indicatorName\":\"功能实现度\",\"aiScore\":88.00,\"teacherComment\":null,\"teacherScore\":90.00},{\"aiComment\":null,\"finalScore\":82.00,\"indicatorName\":\"创新与拓展\",\"aiScore\":81.00,\"teacherComment\":null,\"teacherScore\":83.00}],\"basicInfo\":{\"fileName\":\"E-R图设计_陈同学.docx\",\"submitTime\":1779780000000,\"totalScore\":85.25}}', NULL, '2026-06-01 20:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (15, 1, 4, 83.50, 85.00, 0.5, 1, '{\"task\":\"Java基础语法练习\",\"student\":\"刘同学\",\"score\":83.50,\"rank\":\"top18%\"}', NULL, '2026-05-27 21:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (16, 5, 5, 86.50, 88.00, 0.5, 2, '{\"task\":\"HTML页面设计\",\"student\":\"杨同学\",\"score\":86.50,\"rank\":\"top11%\"}', NULL, '2026-05-30 22:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (17, 6, 5, 84.00, 86.00, 0.5, 2, '{\"task\":\"CSS样式设计\",\"student\":\"杨同学\",\"score\":84.00,\"rank\":\"top16%\"}', NULL, '2026-06-06 23:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (18, 9, 6, 85.00, 87.00, 0.5, 2, '{\"task\":\"E-R图设计\",\"student\":\"黄同学\",\"score\":85.00,\"rank\":\"top13%\"}', NULL, '2026-06-02 00:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (19, 11, 7, 86.75, 88.00, 0.5, 1, '{\"task\":\"Activity生命周期\",\"student\":\"周同学\",\"score\":86.75,\"rank\":\"top9%\"}', NULL, '2026-06-05 01:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (20, 1, 8, 82.00, 84.00, 0.5, 1, '{\"task\":\"Java基础语法练习\",\"student\":\"吴同学\",\"score\":82.00,\"rank\":\"top22%\"}', NULL, '2026-05-28 02:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (21, 5, 9, 87.50, 89.00, 0.5, 2, '{\"task\":\"HTML页面设计\",\"student\":\"徐同学\",\"score\":87.50,\"rank\":\"top6%\"}', NULL, '2026-05-31 03:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (22, 11, 10, 85.50, 87.00, 0.5, 2, '{\"task\":\"Activity生命周期\",\"student\":\"孙同学\",\"score\":85.50,\"rank\":\"top12%\"}', NULL, '2026-06-06 04:00:00', 1, NULL);
INSERT INTO `evaluation_report` VALUES (26, 18, 1, 52.50, 100.00, 0.7, 1, '{\"indicators\":[{\"aiComment\":\"学生提交的实训成果内容与实训要求（1111）完全不符，且评价指标为“代码质量”，但提交内容为《习近平谈治国理政》学习心得体会，未涉及任何编程代码、软件开发或技术实现。因此，无法依据代码质量指标进行评分，得分为0分。建议学生重新提交符合实训要求的代码相关成果。\",\"finalScore\":0.00,\"indicatorName\":\"代码质量\",\"aiScore\":0.00,\"teacherComment\":null,\"teacherScore\":null},{\"aiComment\":\"学生提交的实训成果是一篇关于《习近平谈治国理政》的学习心得体会，内容详实、感悟深刻，展现了良好的理论素养和思考深度。然而，从‘文档规范性’这一评价指标来看，存在明显不足：首先，实训要求明确为‘1111’，但学生提交的是一篇完整的文章，未遵循该具体要求，导致内容与要求不符；其次，文档缺乏标题、作者、日期、格式规范等基本要素，也未按标准文档结构（如摘要、正文、结论）组织；此外，文中部分段落存在重复和冗余（如多次出现‘统筹发展和安全’、‘科技’等内容），结构不够清晰。因此，尽管内容本身质量较高，但规范性严重不足，影响了整体评分。\",\"finalScore\":60.00,\"indicatorName\":\"文档规范性\",\"aiScore\":60.00,\"teacherComment\":null,\"teacherScore\":null},{\"aiComment\":\"该学生提交的是一篇《习近平谈治国理政》的学习心得体会，属于思想政治类文章，与实训要求“1111”完全不符。实训要求未明确具体内容，但通常涉及软件开发、系统实现等技术性任务，而本成果未展示任何功能实现。因此，功能实现度为0分。建议学生仔细阅读实训要求，提交与任务直接相关的技术成果。\",\"finalScore\":0.00,\"indicatorName\":\"功能实现度\",\"aiScore\":0.00,\"teacherComment\":null,\"teacherScore\":null},{\"aiComment\":\"该学生的实训成果在‘创新与拓展’方面表现良好。内容紧扣《习近平谈治国理政》的学习心得，展现了深入的理论思考和系统的知识整合。学生能够从思想定位、学习方法、学习感悟、理论贡献和实践价值等多个维度进行拓展分析，体现了较强的逻辑性和知识迁移能力。特别是在‘科技对社会进步的推动作用’部分，学生结合了公共卫生、经济安全、文化创新等具体案例，将理论联系实际，具有一定的创新视角。然而，部分内容与实训要求‘1111’关联不够紧密，需进一步明确创新点与拓展方向。建议在后续实训中更聚焦于原创性见解或应用性拓展。\",\"finalScore\":85.00,\"indicatorName\":\"创新与拓展\",\"aiScore\":85.00,\"teacherComment\":null,\"teacherScore\":null},{\"aiComment\":\"该学生对《习近平谈治国理政》的学习心得体现了较强的独立思考能力和理论联系实际的水平，能从思想定位、学习方法、学习感悟、理论贡献、实践价值等多个维度进行系统阐述，逻辑清晰、内容充实。但评价指标为‘团队协作’，而实训成果内容为个人心得体会，未涉及任何团队协作相关描述或实践，因此无法依据该指标进行评分。建议在后续实训中明确体现团队分工、协作过程、角色贡献等要素。\",\"finalScore\":85.00,\"indicatorName\":\"团队协作\",\"aiScore\":85.00,\"teacherComment\":null,\"teacherScore\":null},{\"aiComment\":\"学生实训成果展现了扎实的理论学习能力和深刻的思考深度，对《习近平谈治国理政》的核心思想、学习方法、实践价值进行了系统梳理和独到分析。评语结构清晰，逻辑严密，语言表达准确且有感染力。然而，项目展示要求可能更侧重于实践性或技术性应用，而本成果以心得体会为主，缺乏具体项目或技术实现的展示，与实训要求‘1111’的指向性不够匹配，因此扣分。建议在后续实训中明确项目展示的实操部分，将理论感悟与实际项目开发或技术应用相结合，以更全面地体现综合能力。\",\"finalScore\":85.00,\"indicatorName\":\"项目展示\",\"aiScore\":85.00,\"teacherComment\":null,\"teacherScore\":null}],\"basicInfo\":{\"fileName\":\"《习近平谈治国理政》学习心得体会（王旭光）(1).docx\",\"submitTime\":1784199218000,\"totalScore\":52.50}}', NULL, '2026-07-16 21:12:41', 1, '111');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `student_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学号',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '班级',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, 'student01', '123456', '王同学', '/api/uploads/avatar/student_1_1783420288564.webp', 'wang@stu.com', '13800000004', '2024010001', '计科2401', 1, '2026-07-02 14:41:06', '2026-07-07 18:31:28');
INSERT INTO `student` VALUES (2, 'student02', '123456', '赵同学', NULL, 'zhao@stu.com', '13800000005', '2024010002', '计科2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (3, 'student03', '123456', '陈同学', NULL, 'chen@stu.com', '13800000006', '2024010003', '软件2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (4, 'student04', '123456', '刘同学', NULL, 'liu@stu.com', '13800000007', '2024010004', '计科2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (5, 'student05', '123456', '杨同学', NULL, 'yang@stu.com', '13800000008', '2024010005', '计科2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (6, 'student06', '123456', '黄同学', NULL, 'huang@stu.com', '13800000009', '2024010006', '软件2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (7, 'student07', '123456', '周同学', NULL, 'zhou@stu.com', '13800000010', '2024010007', '软件2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (8, 'student08', '123456', '吴同学', NULL, 'wu@stu.com', '13800000011', '2024010008', '计科2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (9, 'student09', '123456', '徐同学', NULL, 'xu@stu.com', '13800000012', '2024010009', '软件2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');
INSERT INTO `student` VALUES (10, 'student10', '123456', '孙同学', NULL, 'sun@stu.com', '13800000013', '2024010010', '计科2401', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属院系',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '职称',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, 'teacher01', '123456', '张老师', '/api/uploads/avatar/teacher_1_1783419732797.jpg', 'zhang@qcby.com', '13800000002', '计算机学院', '副教授', 1, '2026-07-02 14:41:06', '2026-07-07 18:22:12');
INSERT INTO `teacher` VALUES (2, 'teacher02', '123456', '李老师', NULL, 'li@qcby.com', '13800000003', '软件学院', '讲师', 1, '2026-07-02 14:41:06', '2026-07-02 14:41:06');

-- ----------------------------
-- Table structure for training_result
-- ----------------------------
DROP TABLE IF EXISTS `training_result`;
CREATE TABLE `training_result`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID(关联student表)',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件存储路径',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始文件名',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件类型: docx/pdf/jpg/png',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小(字节)',
  `parsed_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '解析后的文本内容',
  `upload_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态: 0待核查 1已核查 2已评价',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_student`(`task_id`, `student_id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '实训成果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of training_result
-- ----------------------------
INSERT INTO `training_result` VALUES (1, 1, 1, 'upload/result/task1_student1.docx', 'Java基础语法练习_王同学.docx', 'docx', 45678, NULL, '2026-05-20 14:30:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (2, 2, 1, 'upload/result/task2_student1.docx', '面向对象编程_王同学.docx', 'docx', 78901, NULL, '2026-05-27 16:45:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (3, 3, 1, 'upload/result/task3_student1.pdf', '异常处理与IO_王同学.pdf', 'pdf', 234567, NULL, '2026-06-05 10:20:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (4, 4, 1, 'upload/result/task4_student1.docx', '集合框架应用_王同学.docx', 'docx', 56789, NULL, '2026-06-12 15:30:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (5, 6, 1, 'upload/result/task6_student1.docx', 'CSS样式设计_王同学.docx', 'docx', 67890, NULL, '2026-05-29 13:45:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (6, 7, 1, 'upload/result/task7_student1.pdf', 'JavaScript交互_王同学.pdf', 'pdf', 456789, NULL, '2026-06-07 16:30:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (7, 9, 1, 'upload/result/task9_student1.docx', 'E-R图设计_王同学.docx', 'docx', 89012, NULL, '2026-05-25 09:15:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (8, 1, 2, 'upload/result/task1_student2.docx', 'Java基础语法练习_赵同学.docx', 'docx', 51234, NULL, '2026-05-21 10:00:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (9, 2, 2, 'upload/result/task2_student2.docx', '面向对象编程_赵同学.docx', 'docx', 82345, NULL, '2026-05-28 14:20:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (10, 5, 2, 'upload/result/task5_student2.pdf', 'HTML页面设计_赵同学.pdf', 'pdf', 289012, NULL, '2026-05-23 15:30:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (11, 6, 2, 'upload/result/task6_student2.docx', 'CSS样式设计_赵同学.docx', 'docx', 71234, NULL, '2026-05-30 11:45:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (12, 11, 2, 'upload/result/task11_student2.docx', 'Activity生命周期_赵同学.docx', 'docx', 93456, NULL, '2026-06-01 13:20:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (13, 12, 2, 'upload/result/task12_student2.pdf', '数据存储_赵同学.pdf', 'pdf', 378901, NULL, '2026-06-08 17:00:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (14, 1, 3, 'upload/result/task1_student3.docx', 'Java基础语法练习_陈同学.docx', 'docx', 48901, NULL, '2026-05-19 16:00:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (15, 3, 3, 'upload/result/task3_student3.pdf', '异常处理与IO_陈同学.pdf', 'pdf', 267890, NULL, '2026-06-04 11:30:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (16, 5, 3, 'upload/result/task5_student3.pdf', 'HTML页面设计_陈同学.pdf', 'pdf', 312345, NULL, '2026-05-24 14:00:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (17, 7, 3, 'upload/result/task7_student3.pdf', 'JavaScript交互_陈同学.pdf', 'pdf', 423456, NULL, '2026-06-06 10:45:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (18, 8, 3, 'upload/result/task8_student3.docx', '需求分析文档_陈同学.docx', 'docx', 134567, NULL, '2026-06-10 16:15:00', 0, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (19, 9, 3, 'upload/result/task9_student3.docx', 'E-R图设计_陈同学.docx', 'docx', 95678, NULL, '2026-05-26 15:20:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (20, 10, 3, 'upload/result/task10_student3.docx', '数据库建模_陈同学.docx', 'docx', 112345, NULL, '2026-06-03 09:40:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (21, 1, 4, 'upload/result/task1_student4.docx', 'Java基础语法练习_刘同学.docx', 'docx', 52345, NULL, '2026-05-22 13:00:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (22, 2, 4, 'upload/result/task2_student4.docx', '面向对象编程_刘同学.docx', 'docx', 85678, NULL, '2026-05-29 15:30:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (23, 5, 5, 'upload/result/task5_student5.pdf', 'HTML页面设计_杨同学.pdf', 'pdf', 298765, NULL, '2026-05-25 10:20:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (24, 6, 5, 'upload/result/task6_student5.docx', 'CSS样式设计_杨同学.docx', 'docx', 74567, NULL, '2026-06-01 14:45:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (25, 9, 6, 'upload/result/task9_student6.docx', 'E-R图设计_黄同学.docx', 'docx', 91234, NULL, '2026-05-27 11:15:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (26, 10, 6, 'upload/result/task10_student6.docx', '数据库建模_黄同学.docx', 'docx', 108765, NULL, '2026-06-04 16:30:00', 0, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (27, 11, 7, 'upload/result/task11_student7.docx', 'Activity生命周期_周同学.docx', 'docx', 96789, NULL, '2026-06-02 13:45:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (28, 12, 7, 'upload/result/task12_student7.pdf', '数据存储_周同学.pdf', 'pdf', 389012, NULL, '2026-06-09 17:20:00', 0, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (29, 1, 8, 'upload/result/task1_student8.docx', 'Java基础语法练习_吴同学.docx', 'docx', 49876, NULL, '2026-05-23 14:10:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (30, 5, 9, 'upload/result/task5_student9.pdf', 'HTML页面设计_徐同学.pdf', 'pdf', 305432, NULL, '2026-05-26 12:00:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (31, 11, 10, 'upload/result/task11_student10.docx', 'Activity生命周期_孙同学.docx', 'docx', 98765, NULL, '2026-06-03 15:00:00', 1, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (32, 4, 3, 'upload/result/task4_student3.pdf', '陈同学_响应式网页设计.pdf', 'pdf', 520000, '响应式网页设计报告\n\n设计说明：\n本次设计采用移动端优先（Mobile First）的设计理念。\n\n技术栈：\n- HTML5语义化标签\n- CSS3 Flexbox和Grid布局\n- CSS3动画和过渡效果\n- Media Query媒体查询\n\n页面结构：\n1. 顶部导航栏\n2. 轮播图区域\n3. 内容展示区\n4. 侧边栏\n5. 页脚信息\n\n响应式断点：\n- 手机端：< 768px\n- 平板端：768px - 1024px\n- PC端：> 1024px\n\n特色功能：\n- 平滑滚动\n- 懒加载图片\n- 触摸手势支持', '2026-05-29 17:38:14', 0, '2026-07-02 14:41:06');
INSERT INTO `training_result` VALUES (33, 18, 1, 'result/b7a10841ea1449319704368752117f78.docx', '《习近平谈治国理政》学习心得体会（王旭光）(1).docx', 'docx', 17808, '深学细悟治国理政智慧笃行实干勇担时代使命——《习近平谈治国理政》学习心得体会\n《习近平谈治国理政》第一至四卷，是新时代中国共产党人将马克思主义基本原理与中国具体实际、中华优秀传统文化深度融合的思想结晶，是引领新时代党和国家事业破浪前行的行动纲领，更是当代中国马克思主义、二十一世纪马克思主义的集中体现。近期，我以“读原著、学原文、悟原理”为根本方法，逐篇研读著作中的重要论述、重大部署和实践要求，每一次品读都能感受到真理光芒的指引、思想伟力的震撼和为民情怀的温暖。这部著作不仅系统回应了新时代党和国家事业发展的重大理论与实践课题，更为我们认清时代方位、把握发展大势、践行使命担当提供了根本遵循，是终身受益的理论教材和行动指南。\n思想定位：三重融合下的理论高峰\n《习近平谈治国理政》不是一般性的政策汇编，而是新时代理论创新的集大成之作。它的原创性贡献集中体现在三重融合：\n\n与马克思主义基本原理相融合：坚持辩证唯物主义和历史唯物主义，对共产党执政规律、社会主义建设规律、人类社会发展规律作出了新的深刻揭示。例如，提出“中国式现代化”是对马克思主义现代化理论的重大发展。\n\n与中国具体实际相融合：立足社会主义初级阶段这个最大国情，直面发展不平衡不充分、外部环境急剧变化等现实挑战，所有论述都有极强的现实针对性，绝非抽象说教。\n\n与中华优秀传统文化相融合：大量运用“大道之行也，天下为公”“民惟邦本”“苟日新日日新”等传统智慧，赋予马克思主义鲜明的中国特色、中国风格、中国气派，使其在中华大地上更深地扎根、更广地传播。\n\n正是这三重融合，使这部著作成为当代中国马克思主义、二十一世纪马克思主义的集中体现——它既是中国的，也是时代的；既回答了“中国之问”，也回答了“世界之问”。\n\n学习方法：返璞归真的“笨功夫”才是真捷径\n“读原著、学原文、悟原理”九个字，看似朴素，实则蕴含着理论学习的根本规律。\n读原著：避免从二手解读、碎片化金句中“拼凑”理解，而是直接面对作者的原初表述。原著中的每一个判断，都有特定的语境和问题指向，脱离原著容易断章取义。\n学原文：逐字逐句、逐篇逐章地精读，不跳、不略、不猜。比如，学习“全过程人民民主”这一重要论述，只有通读相关讲话原文，才能理解它是对西方“选举式民主”的超越，而不仅仅是一个新提法。\n\n悟原理：不止于记住结论，而要追问“为什么这样讲”“针对什么问题”“遵循什么逻辑”。悟原理的过程，就是把书本上的“他者之思”转化为自己头脑中的“认知框架”的过程。\n用户强调“逐篇研读重要论述、重大部署和实践要求”，正是这一方法的扎实落地。在信息碎片化时代，这种“笨功夫”反而是通向真知最快的捷径。\n学习感悟：三重触动与内心的共鸣\n用户用三组短语描述了品读过程中的真实感受，值得细致体会：\n真理光芒的指引：读原著时，常有“拨云见日”之感。比如面对“百年未有之大变局”的复杂判断，书中系统分析了变在何处、如何应对，让读者不再迷茫，而是看清了历史大势和主攻方向。这就是真理的“照明”功能。\n思想伟力的震撼：书中很多论述极具穿透力。例如“得罪千百人、不负十四亿”的反腐败决心，“绿水青山就是金山银山”的生态观重构，“把中国人的饭碗牢牢端在自己手中”的安全底线。这些话语背后是系统的战略擘画，其力量感让人不由自主地产生敬畏与信服。要牢固树立底线思维，增强忧患意识，加强对各类风险的分析研判，制定防范化解预案，做到早发现、早预警、早处置。重点防范化解经济金融、安全生产、公共卫生、社会稳定等领域风险，增强斗争精神，提高斗争本领，敢于直面矛盾、较真碰硬，坚决同各种风险挑战作斗争，维护国家主权、安全、发展利益。\n\n为民情怀的温暖：习近平总书记反复强调“人民对美好生活的向往就是我们的奋斗目标”，“我将无我，不负人民”。读原著时，能真切感受到一种从内心深处流淌出的对普通百姓的关切。这种温度，让理论不再是冷冰冰的条文，而是带着心跳的承诺。\n理论贡献：系统回应两大课题\n《习近平谈治国理政》不是零散的即兴讲话，而是系统性地回答了两大课题：\n我们党在新时代面临着一系列带有根本性的理论课题：究竟要坚持和发展什么样的中国特色社会主义，又该怎样坚持和发展？社会主义现代化强国的目标形态是什么，实现路径在哪里？长期执政的马克思主义政党应具备哪些特质，如何持续推进自身建设？《习近平谈治国理政》通过对“十个明确”“十四个坚持”“十三个方面成就”的深刻阐述，为这些时代之问提供了清晰的理论回应与实践指引。\n重大实践课题：如何打赢脱贫攻坚战？如何防范化解重大风险？如何突破“卡脖子”技术？如何实现碳达峰碳中和？如何统筹发展和安全？每一个实践难题，书中都有原则性指导和对策性思路。这种“理论不仅解释世界，而且直接指导改变世界”的特征，是这部著作最突出的实践品格。统筹发展和安全是新时代治国理政的重大原则，科学技术为防范化解各类风险提供了重要支撑。在公共卫生安全领域，新冠疫情防控中核酸检测技术、疫苗研发技术的快速突破，以及大数据流调、健康码溯源等数字技术的应用，构建了 “预防 — 检测 — 救治” 全链条防控体系，最大限度保护了人民群众生命安全；在经济安全领域，金融科技的发展推动建立了智能风控系统，通过大数据分析实时监测信贷风险、市场波动，有效防范了系统性金融风险；在国家安全领域，量子通信、卫星导航等技术的突破，构建了自主可控的安全保障体系，为维护国家主权、安全、发展利益提供了技术支撑。科技的风险防控作用，既保障了社会稳定运行，也为社会进步创造了安全环境。\n充分发挥科技对社会进步的推动作用，需要构建良好的制度环境与创新生态。要坚持党对科技事业的全面领导，发挥新型举国体制优势，集中力量攻克 “卡脖子” 技术难题，保障国家科技安全；要深化科技体制改革，完善科技评价体系、成果转化机制，激发科研人员的创新活力；要加强科技人才培养，构建多层次、高素质的科技人才队伍，为科技创新提供人才支撑；要推进科技国际合作，积极参与全球科技治理，吸收借鉴国际先进技术成果，推动科技成果共享。只有将技术创新与制度创新、文化创新相结合，才能形成科技推动社会进步的强大合力。科学技术重构了文化生产、传播与消费的全链条，推动文化产业高质量发展。在内容生产领域，人工智能、大数据技术能够精准分析用户需求，实现文化产品的个性化创作，如 AI 生成内容、互动影视等新业态不断涌现，丰富了文化产品供给；在传播渠道领域，短视频平台、直播平台等数字媒体的兴起，打破了传统媒体的传播壁垒，普通人成为文化传播的主体，形成了多元开放的文化传播格局；在消费模式领域，线上文化消费、数字藏品等新业态快速发展，2024 年我国数字文化消费规模突破 2.5 万亿元，成为文化产业增长的新动力。科技推动下的文化创新，不仅满足了人民群众日益增长的精神文化需求，更增强了中华文化的影响力和软实力，为社会进步注入了精神动力。\n\n\n\n实践价值：三个“提供”与终身受益\n用户指出这部著作是“根本遵循”“理论教材”“行动指南”，具体展开来看：\n认清时代方位：通过学习，能够理解我们正处于“两个一百年”奋斗目标的历史交汇期，理解中国所处的历史坐标与世界坐标，不妄自尊大也不妄自菲薄。空谈误国，实干兴邦。在新时代，我们要坚持实干为要，把学习成果转化为干事创业的实际行动，以务实的作风、扎实的工作推动各项任务落到实处。\n把握发展大势：书中反复强调“保持历史耐心和战略定力”“善于在危机中育先机、于变局中开新局”。这些论述帮助读者跳出短期波动的干扰，看清长期向好的基本面。无论是维护国家安全和社会稳定，还是推动构建人类命运共同体，著作中都给出了清晰的方向指引和科学的方法路径。例如，面对复杂的国际环境，著作提出 “加快构建新发展格局，着力推动高质量发展”，为我国在变局中开新局提供了战略指引；创新是民族进步的灵魂，是国家兴旺发达的不竭动力。在新时代，我们要勇于开拓创新，敢于突破思维定势和路径依赖，以创新的思路、创新的举措破解发展难题、推动事业发展。要始终保持昂扬向上的精神状态，勇于担当、善于作为，在各自的岗位上攻坚克难、奋勇争先，以实际行动为全面建设社会主义现代化国家添砖加瓦。要敢于直面挑战、勇于战胜困难，在应对风险挑战中锤炼意志、增长才干，以永不懈怠的精神状态和一往无前的奋斗姿态，书写新时代的奋斗篇章。\n\n\n总之，《习近平谈治国理政》是一部蕴含着深邃思想、磅礴力量和真挚情怀的经典著作，是我们认识世界、改造世界的强大思想武器，也是我们提升自身素养、做好本职工作的必修课、常修课。在今后的工作和学习中，我将把学习《习近平谈治国理政》作为一项长期的政治任务，持续深学细悟、常学常新。\n', '2026-07-16 18:53:38', 2, '2026-07-16 18:53:38');

-- ----------------------------
-- Table structure for training_task
-- ----------------------------
DROP TABLE IF EXISTS `training_task`;
CREATE TABLE `training_task`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '任务描述',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '实训要求',
  `deadline` datetime(0) DEFAULT NULL COMMENT '截止时间',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 1进行中 2已结束 0草稿',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '实训任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of training_task
-- ----------------------------
INSERT INTO `training_task` VALUES (1, 1, 'Java基础语法练习', '完成Java基本数据类型、运算符、流程控制等基础练习', '1. 编写至少5个程序\n2. 包含注释\n3. 代码规范', '2026-07-23 15:59:59', 1, '2026-05-01 09:00:00', '2026-07-06 22:23:05');
INSERT INTO `training_task` VALUES (2, 1, '面向对象编程', '实现一个简单的学生管理系统,包含增删改查功能', '1. 使用类和对象\n2. 实现封装\n3. 提供测试用例', '2026-06-25 23:59:59', 1, '2026-05-08 09:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (3, 1, '异常处理与IO', '文件读写操作,实现数据持久化', '1. 使用try-catch\n2. 读写文件\n3. 处理异常', '2026-06-30 23:59:59', 1, '2026-05-15 09:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (4, 1, '集合框架应用', '使用List、Map等集合实现通讯录', '1. 选择合适的集合\n2. 实现基本功能\n3. 优化性能', '2026-07-05 23:59:59', 1, '2026-05-22 09:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (5, 2, 'HTML页面设计', '设计个人主页,包含导航、内容区、页脚', '1. 使用语义化标签\n2. 页面美观\n3. 响应式布局', '2026-06-18 23:59:59', 1, '2026-05-03 10:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (6, 2, 'CSS样式设计', '为个人主页添加完整样式', '1. 使用Flex布局\n2. 动画效果\n3. 媒体查询', '2026-06-23 23:59:59', 1, '2026-05-10 10:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (7, 2, 'JavaScript交互', '实现轮播图、表单验证等交互', '1. 原生JS\n2. 事件处理\n3. 异步操作', '2026-06-28 23:59:59', 1, '2026-05-17 10:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (8, 3, 'E-R图设计', '设计图书馆管理系统的E-R图', '1. 识别实体\n2. 确定关系\n3. 绘制规范', '2026-06-22 23:59:59', 1, '2026-05-05 11:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (9, 3, '数据库建模', '将E-R图转换为关系模式', '1. 规范化设计\n2. 消除冗余\n3. 完整性约束', '2026-06-27 23:59:59', 1, '2026-05-12 11:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (10, 3, 'SQL查询练习', '编写复杂SQL查询', '1. 多表连接\n2. 子查询\n3. 聚合函数', '2026-07-02 23:59:59', 1, '2026-05-19 11:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (11, 4, '需求分析文档', '编写软件需求规格说明书', '1. 功能需求\n2. 非功能需求\n3. 用例图', '2026-06-24 23:59:59', 1, '2026-05-07 14:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (12, 4, '系统设计文档', '编写软件设计文档', '1. 架构设计\n2. 模块设计\n3. 接口设计', '2026-06-29 23:59:59', 1, '2026-05-14 14:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (13, 5, 'Activity生命周期', '实现多Activity应用', '1. 生命周期回调\n2. Intent传值\n3. 界面跳转', '2026-06-26 23:59:59', 1, '2026-05-09 15:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (14, 5, '数据存储', '实现本地数据持久化', '1. SharedPreferences\n2. SQLite\n3. 文件存储', '2026-07-01 23:59:59', 1, '2026-05-16 15:00:00', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (15, 1, '项目一：学生信息管理系统', '使用Java实现一个完整的学生信息管理系统，包括增删改查功能', '1. 使用面向对象设计\n2. 包含完整的类图\n3. 实现数据持久化\n4. 编写详细文档', '2026-06-29 23:59:59', 1, '2026-05-29 17:38:14', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (16, 1, '项目二：图书管理系统', '开发一个图书管理系统，支持借阅、归还、查询等功能', '1. 实现用户权限管理\n2. 支持多条件查询\n3. 包含异常处理\n4. 代码注释完整', '2026-06-30 23:59:59', 1, '2026-05-29 17:38:14', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (17, 2, '实验一：响应式网页设计', '使用HTML5和CSS3创建一个响应式网页', '1. 适配移动端和PC端\n2. 使用Flexbox/Grid布局\n3. 包含动画效果\n4. 语义化标签', '2026-06-20 23:59:59', 1, '2026-05-29 17:38:14', '2026-07-02 14:41:06');
INSERT INTO `training_task` VALUES (18, 7, '1111', '1111', '1111', '2026-07-30 16:00:00', 1, '2026-07-16 18:45:11', '2026-07-16 18:45:11');

-- ----------------------------
-- Table structure for verification_code
-- ----------------------------
DROP TABLE IF EXISTS `verification_code`;
CREATE TABLE `verification_code`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `target` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标地址（邮箱或手机号）',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '验证码（6位数字）',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型: email / phone',
  `used` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已使用: 0未使用 1已使用',
  `expire_time` datetime(0) NOT NULL COMMENT '过期时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target`(`target`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '验证码表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
