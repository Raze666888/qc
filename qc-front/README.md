# 问卷调查系统

一个功能完整的在线问卷调查平台，包含前端和后端完整实现。

## 项目结构

```
qc/
├── qc-front/          # Vue3前端项目
│   ├── src/
│   │   ├── api/      # API接口封装
│   │   ├── views/    # 页面组件
│   │   ├── router/   # 路由配置
│   │   └── utils/    # 工具函数
│   └── package.json
│
└── qc-backend/        # Spring Boot后端项目
    ├── src/main/java/com/qc/
    │   ├── config/       # 配置类
    │   ├── controller/   # 控制器
    │   ├── service/      # 业务逻辑
    │   ├── mapper/       # 数据访问层
    │   ├── entity/       # 实体类
    │   ├── dto/          # 数据传输对象
    │   ├── vo/           # 视图对象
    │   ├── utils/        # 工具类
    │   └── exception/    # 异常处理
    └── src/main/resources/
        ├── mapper/       # MyBatis XML
        └── db/           # 数据库脚本
```

## 功能特性

### 2.1 用户管理模块
- ✅ 用户注册（手机号/邮箱）
- ✅ 用户登录（支持记住密码）
- ✅ 个人中心（修改昵称、密码）
- ✅ 角色管理（普通用户/管理员）

### 2.2 问卷管理模块
- ✅ 问卷创建（标题、描述、类型、截止时间、匿名设置）
- ✅ 问卷编辑（草稿状态可修改）
- ✅ 问卷发布/暂停
- ✅ 问卷删除（软删除，7天回收期）
- ✅ 问卷列表（状态筛选、分页查询）

### 2.3 问题管理模块
- ✅ 问题添加（单选、多选、文本）
- ✅ 问题编辑/删除
- ✅ 问题排序（上移/下移）
- ✅ 选项管理（添加、删除、排序）
- ✅ 逻辑跳转（单选题支持跳转规则）

### 2.4 答卷管理模块
- ✅ 问卷填写（一题一页展示）
- ✅ 答卷提交（验证必填项、防重复提交）
- ✅ 答卷查看（列表展示、详情查看）
- ✅ 答卷统计（选择题饼图/柱状图、文本题列表）

## 技术栈

### 前端
- Vue 3.4.21
- Vue Router 4.3.0
- Element Plus 2.6.0
- Axios 1.6.8
- ECharts (图表展示)

### 后端
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Druid (连接池)
- JWT (身份认证)
- Lombok

## 快速开始

### 1. 数据库初始化

```bash
# 连接MySQL
mysql -u root -p

# 执行建表脚本
source qc-backend/src/main/resources/db/schema.sql
```

### 2. 启动后端

```bash
cd qc-backend

# 修改配置文件
# 编辑 src/main/resources/application.yml
# 修改数据库连接信息

# 启动项目
mvn spring-boot:run

# 或使用IDE运行 QuestionnaireApplication.java
```

默认管理员账号：admin / admin

### 3. 启动前端

```bash
cd qc-front

# 安装依赖
npm install

# 启动开发服务器
npm run serve

# 打包生产版本
npm run build
```

前端访问地址：http://localhost:8081

## API接口文档

### 用户管理
| 接口 | 方法 | 描述 |
|------|------|------|
| `/user/register` | POST | 用户注册 |
| `/user/login` | POST | 用户登录 |
| `/user/info` | GET | 获取用户信息 |
| `/user/update` | PUT | 更新用户信息 |

### 问卷管理
| 接口 | 方法 | 描述 |
|------|------|------|
| `/questionnaire/create-with-questions` | POST | 创建问卷（含问题） |
| `/questionnaire/update-with-questions/{id}` | PUT | 更新问卷 |
| `/questionnaire/publish` | POST | 发布问卷 |
| `/questionnaire/updateStatus` | POST | 更新问卷状态 |
| `/questionnaire/delete` | POST | 删除问卷 |
| `/questionnaire/page` | GET | 分页查询问卷 |
| `/questionnaire/detail/{id}` | GET | 获取问卷详情 |

### 答卷管理
| 接口 | 方法 | 描述 |
|------|------|------|
| `/fill/{id}` | GET | 获取填写问卷数据 |
| `/fill/submit` | POST | 提交答卷 |
| `/answer/list/{questionnaireId}` | GET | 获取答卷列表 |
| `/answer/statistics/{questionnaireId}` | GET | 获取答卷统计 |

## 数据库设计

### 核心表结构

**sys_user** - 用户表
- id: 用户ID
- username: 用户名
- password: 密码（MD5加密）
- phone: 手机号
- email: 邮箱
- role: 角色（0-普通用户，1-管理员）
- points: 积分

**questionnaire** - 问卷表
- id: 问卷ID
- user_id: 创建者ID
- title: 标题
- description: 描述
- category: 类型（1-学术科研，2-生活消费，3-心理健康，4-娱乐游戏，5-工作就业）
- deadline: 截止时间
- is_anonymous: 是否匿名
- status: 状态（0-草稿，1-已发布，2-已暂停，3-已删除）
- audit_status: 审核状态

**question** - 问题表
- id: 问题ID
- questionnaire_id: 问卷ID
- content: 问题内容
- type: 类型（0-单选，1-多选，2-文本）
- is_required: 是否必填
- sort: 排序

**question_option** - 问题选项表
- id: 选项ID
- question_id: 问题ID
- content: 选项内容
- sort: 排序
- jump_question_id: 跳转问题ID

**answer** - 答卷表
- id: 答卷ID
- questionnaire_id: 问卷ID
- respondent_name: 答题者姓名
- respondent_phone: 答题者手机号
- respondent_ip: 答题者IP

**answer_detail** - 答卷详情表
- id: 详情ID
- answer_id: 答卷ID
- question_id: 问题ID
- option_id: 选项ID（选择题）
- text_content: 文本内容（文本题）

## 核心功能说明

### 1. 问卷创建流程
1. 填写问卷基本信息（标题、描述、类型、截止时间、匿名设置）
2. 添加问题（支持单选、多选、文本三种类型）
3. 为选择题添加选项（至少2个）
4. 可为单选题设置逻辑跳转规则
5. 保存为草稿或直接发布

### 2. 问卷填写流程
1. 访问问卷链接或扫描二维码
2. 查看问卷标题和描述
3. 逐题填写答案（支持必填验证）
4. 提交答卷（后端验证IP防重复）
5. 显示提交成功页面

### 3. 答卷统计功能
- **选择题**：饼图展示各选项占比、柱状图展示选择次数
- **文本题**：列表展示所有文本答案，支持关键词搜索
- **实时更新**：刷新查看最新统计数据

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080  # 后端服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/questionnaire_db
    username: root
    password: your_password  # 修改为实际密码

jwt:
  secret: questionnaire-system-secret-key-2024-very-long-and-secure
  expiration: 604800000  # Token有效期（7天）
```

### 前端配置 (src/main.js)

```javascript
const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080',  // 后端地址
    timeout: 30000,
})
```

## 注意事项

1. **CORS配置**：后端已配置跨域支持，前端可直接调用API
2. **JWT认证**：所有需要认证的接口需在请求头携带 `Authorization: Bearer {token}`
3. **密码安全**：密码采用MD5加密存储，建议生产环境使用BCrypt
4. **逻辑删除**：问卷删除采用软删除机制，deleted=1表示已删除
5. **IP限制**：同一IP地址只能填写一次问卷

## 开发计划

- [ ] 积分系统（填写问卷获得积分）
- [ ] 问卷模板功能
- [ ] 问卷分享到社交媒体
- [ ] 答卷数据导出（Excel）
- [ ] 问卷二维码生成
- [ ] 短信验证码登录
- [ ] 问卷审核流程
- [ ] 数据备份恢复

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue或Pull Request。
