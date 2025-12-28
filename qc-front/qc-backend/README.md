# 问卷系统后端

## 技术栈
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Druid
- JWT
- Lombok

## 项目结构
```
qc-backend/
├── src/main/java/com/qc/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── dto/            # 数据传输对象
│   ├── entity/         # 实体类
│   ├── exception/      # 异常处理
│   ├── mapper/         # MyBatis Mapper
│   ├── service/        # 业务逻辑
│   ├── utils/          # 工具类
│   ├── vo/             # 视图对象
│   └── QuestionnaireApplication.java
└── src/main/resources/
    ├── mapper/         # MyBatis XML
    ├── db/             # 数据库脚本
    └── application.yml # 配置文件
```

## 启动步骤

### 1. 创建数据库
```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

### 2. 修改配置
编辑 `src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/questionnaire_db
    username: root
    password: your_password
```

### 3. 启动项目
```bash
mvn spring-boot:run
```

## API接口文档

### 用户管理
- POST `/user/register` - 用户注册
- POST `/user/login` - 用户登录
- GET `/user/info` - 获取用户信息
- PUT `/user/update` - 更新用户信息

### 问卷管理
- POST `/questionnaire/create-with-questions` - 创建问卷（含问题）
- PUT `/questionnaire/update-with-questions/{id}` - 更新问卷
- POST `/questionnaire/publish` - 发布问卷
- POST `/questionnaire/updateStatus` - 更新问卷状态
- POST `/questionnaire/delete` - 删除问卷
- GET `/questionnaire/page` - 分页查询问卷
- GET `/questionnaire/detail/{id}` - 获取问卷详情

### 答卷管理
- POST `/answer/submit` - 提交答卷
- GET `/answer/list/{questionnaireId}` - 获取答卷列表
- GET `/answer/statistics/{questionnaireId}` - 获取答卷统计

## 默认账号
- 管理员: admin / admin
