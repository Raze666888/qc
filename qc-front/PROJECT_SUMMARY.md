# 问卷调查系统 - 完整项目总结

## 项目概述

一个功能完整的在线问卷调查平台，包含用户端、管理端和完整的后端API服务。

## 技术栈

### 前端
- Vue 3.4.21 (Composition API)
- Vue Router 4.3.0
- Element Plus 2.6.0
- Axios 1.6.8
- ECharts (图表可视化)

### 后端
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Druid 数据库连接池
- JWT 身份认证
- Lombok 简化开发

## 项目结构

```
qc/
├── qc-front/                      # Vue3前端项目
│   ├── src/
│   │   ├── api/                  # API接口封装
│   │   ├── components/           # 公共组件
│   │   ├── views/                # 页面组件
│   │   │   ├── admin/           # 管理员页面
│   │   │   │   ├── AdminDashboard.vue      # 管理员仪表盘
│   │   │   │   ├── AdminUserManage.vue      # 用户管理
│   │   │   │   └── SurveyAudit.vue          # 问卷审核
│   │   │   ├── LoginPage.vue               # 登录页
│   │   │   ├── RegisterPage.vue            # 注册页
│   │   │   ├── CreatorCenter.vue           # 创作者中心
│   │   │   ├── QuestionnaireCreate.vue     # 问卷创建/编辑
│   │   │   ├── QuestionnaireFill.vue       # 问卷填写
│   │   │   └── QuestionnaireStats.vue      # 答卷统计
│   │   ├── router/               # 路由配置
│   │   ├── utils/                # 工具函数
│   │   ├── App.vue
│   │   └── main.js
│   └── package.json
│
└── qc-backend/                   # Spring Boot后端项目
    ├── src/main/java/com/qc/
    │   ├── config/              # 配置类
    │   │   ├── CorsConfig.java            # 跨域配置
    │   │   └── MyMetaObjectHandler.java   # 自动填充
    │   ├── controller/          # 控制器
    │   │   ├── UserController.java        # 用户管理
    │   │   ├── QuestionnaireController.java # 问卷管理
    │   │   ├── AnswerController.java      # 答卷管理
    │   │   ├── FillController.java        # 问卷填写
    │   │   └── AdminController.java       # 管理员
    │   ├── service/             # 业务逻辑
    │   │   ├── UserService.java
    │   │   ├── QuestionnaireService.java
    │   │   ├── AnswerService.java
    │   │   └── AdminService.java
    │   ├── mapper/              # 数据访问层
    │   │   ├── UserMapper.java
    │   │   ├── QuestionnaireMapper.java
    │   │   ├── QuestionMapper.java
    │   │   ├── QuestionOptionMapper.java
    │   │   ├── AnswerMapper.java
    │   │   └── AnswerDetailMapper.java
    │   ├── entity/              # 实体类
    │   │   ├── User.java
    │   │   ├── Questionnaire.java
    │   │   ├── Question.java
    │   │   ├── QuestionOption.java
    │   │   ├── Answer.java
    │   │   └── AnswerDetail.java
    │   ├── dto/                 # 数据传输对象
    │   │   ├── UserDTO.java
    │   │   ├── LoginDTO.java
    │   │   ├── QuestionnaireDTO.java
    │   │   ├── QuestionDTO.java
    │   │   ├── OptionDTO.java
    │   │   ├── AnswerDTO.java
    │   │   ├── AuditDTO.java
    │   │   └── AnswerDetailDTO.java
    │   ├── vo/                  # 视图对象
    │   │   ├── Result.java      # 统一响应
    │   │   ├── UserVO.java
    │   │   ├── QuestionnaireVO.java
    │   │   ├── QuestionVO.java
    │   │   ├── OptionVO.java
    │   │   └── PageResult.java
    │   ├── utils/               # 工具类
    │   │   └── JwtUtil.java     # JWT工具
    │   ├── exception/           # 异常处理
    │   │   ├── GlobalExceptionHandler.java
    │   │   └── BusinessException.java
    │   └── QuestionnaireApplication.java
    └── src/main/resources/
        ├── mapper/              # MyBatis XML
        ├── db/                  # 数据库脚本
        │   └── schema.sql       # 建表脚本
        └── application.yml      # 配置文件
```

## 功能模块清单

### ✅ 2.1 用户管理模块

| 功能 | 状态 | API接口 |
|------|------|---------|
| 用户注册 | ✅ | POST /user/register |
| 用户登录 | ✅ | POST /user/login |
| 获取用户信息 | ✅ | GET /user/info |
| 更新用户信息 | ✅ | PUT /user/update |
| 记住密码 | ✅ | LocalStorage |

### ✅ 2.2 问卷管理模块

| 功能 | 状态 | API接口 |
|------|------|---------|
| 创建问卷 | ✅ | POST /questionnaire/create-with-questions |
| 编辑问卷 | ✅ | PUT /questionnaire/update-with-questions/{id} |
| 发布问卷 | ✅ | POST /questionnaire/publish |
| 暂停问卷 | ✅ | POST /questionnaire/updateStatus |
| 删除问卷 | ✅ | POST /questionnaire/delete |
| 分页查询 | ✅ | GET /questionnaire/page |
| 问卷详情 | ✅ | GET /questionnaire/detail/{id} |

### ✅ 2.3 问题管理模块

| 功能 | 状态 | 说明 |
|------|------|------|
| 添加单选题 | ✅ | 至少2个选项 |
| 添加多选题 | ✅ | 支持最多可选数量 |
| 添加文本题 | ✅ | 单行/多行输入框 |
| 编辑问题 | ✅ | 草稿状态可编辑 |
| 删除问题 | ✅ | 草稿状态可删除 |
| 问题排序 | ✅ | 上移/下移 |
| 选项管理 | ✅ | 添加/删除选项 |
| 逻辑跳转 | ✅ | 单选题支持跳转规则 |

### ✅ 2.4 答卷管理模块

| 功能 | 状态 | API接口 |
|------|------|---------|
| 问卷填写 | ✅ | GET /fill/{id} |
| 提交答卷 | ✅ | POST /fill/submit |
| 查看答卷列表 | ✅ | GET /answer/list/{questionnaireId} |
| 答卷统计 | ✅ | GET /answer/statistics/{questionnaireId} |
| 防重复提交 | ✅ | 基于IP地址 |
| 必填验证 | ✅ | 前端+后端双重验证 |

### ✅ 2.5 管理员模块

| 功能 | 状态 | API接口 |
|------|------|---------|
| 用户列表查询 | ✅ | GET /admin/users |
| 禁用/启用用户 | ✅ | PUT /admin/user/status |
| 问卷审核列表 | ✅ | GET /admin/audits |
| 审核通过 | ✅ | POST /admin/audit |
| 审核驳回 | ✅ | POST /admin/audit |
| 统计数据 | ✅ | GET /admin/dashboard |

## 数据库设计

### 核心表结构

#### sys_user (用户表)
```sql
- id: 用户ID（主键）
- username: 用户名（唯一）
- password: 密码（MD5加密）
- phone: 手机号（唯一）
- email: 邮箱
- nickname: 昵称
- role: 角色（0-普通用户，1-管理员）
- points: 积分
- create_time: 创建时间
- update_time: 更新时间
- deleted: 逻辑删除标记
```

#### questionnaire (问卷表)
```sql
- id: 问卷ID（主键）
- user_id: 创建者ID
- title: 问卷标题
- description: 问卷描述
- category: 问卷类型（1-学术科研，2-生活消费，3-心理健康，4-娱乐游戏，5-工作就业）
- deadline: 截止时间
- is_anonymous: 是否匿名（0-否，1-是）
- status: 状态（0-草稿，1-已发布，2-已暂停，3-已删除）
- audit_status: 审核状态（0-待审核，1-已通过，2-已驳回）
- link: 问卷链接
- create_time: 创建时间
- update_time: 更新时间
- deleted: 逻辑删除标记
```

#### question (问题表)
```sql
- id: 问题ID（主键）
- questionnaire_id: 问卷ID
- content: 问题内容
- type: 问题类型（0-单选，1-多选，2-文本）
- is_required: 是否必填（0-否，1-是）
- sort: 排序
- max_select_num: 最多可选数量（多选题）
- input_type: 输入框类型（0-单行，1-多行）
- create_time: 创建时间
- update_time: 更新时间
- deleted: 逻辑删除标记
```

#### question_option (问题选项表)
```sql
- id: 选项ID（主键）
- question_id: 问题ID
- content: 选项内容
- sort: 排序
- jump_question_id: 跳转问题ID
- create_time: 创建时间
- update_time: 更新时间
- deleted: 逻辑删除标记
```

#### answer (答卷表)
```sql
- id: 答卷ID（主键）
- questionnaire_id: 问卷ID
- respondent_name: 答题者姓名
- respondent_phone: 答题者手机号
- respondent_ip: 答题者IP
- create_time: 创建时间
- update_time: 更新时间
- deleted: 逻辑删除标记
```

#### answer_detail (答卷详情表)
```sql
- id: 详情ID（主键）
- answer_id: 答卷ID
- question_id: 问题ID
- option_id: 选项ID（选择题）
- text_content: 文本内容（文本题）
- create_time: 创建时间
- deleted: 逻辑删除标记
```

## 核心功能实现

### 1. JWT认证流程

```
用户登录
    ↓
验证账号密码
    ↓
生成JWT Token（包含userId、username、role）
    ↓
返回Token给前端
    ↓
前端存储Token（LocalStorage）
    ↓
后续请求携带Token（Authorization: Bearer {token}）
    ↓
后端拦截器验证Token
    ↓
解析Token获取用户信息
    ↓
放行请求
```

### 2. 问卷发布流程

```
创建者点击"发布"
    ↓
前端验证（问题数量等）
    ↓
调用发布接口
    ↓
后端验证
    ↓
更新问卷状态（status=1, audit_status=0）
    ↓
生成问卷链接
    ↓
返回成功
    ↓
等待管理员审核
```

### 3. 问卷填写流程

```
用户访问问卷链接
    ↓
前端获取问卷数据（GET /fill/{id}）
    ↓
验证问卷状态（已发布且未截止）
    ↓
逐题展示（一题一页）
    ↓
前端验证必填项
    ↓
提交答卷（POST /fill/submit）
    ↓
后端验证（IP防重复、必填验证）
    ↓
保存答卷和详情
    ↓
返回成功
```

### 4. 答卷统计实现

```javascript
// 选择题统计
1. 统计总答卷数
2. 遍历每个选项，统计选择次数
3. 计算占比（选择次数/总答卷数 * 100%）
4. 返回饼图/柱状图数据

// 文本题统计
1. 查询所有文本答案
2. 按提交时间排序
3. 返回列表（支持关键词搜索）
```

## API响应格式

### 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 分页响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 100,
    "records": []
  }
}
```

### 错误响应格式

```json
{
  "code": 500,
  "msg": "错误描述"
}
```

## 安全措施

### 1. 密码安全
- MD5加密存储
- 密码长度≥6位
- 密码不在响应中返回

### 2. Token认证
- JWT Token有效期7天
- Token过期自动跳转登录
- 敏感操作验证Token

### 3. 数据验证
- 前端表单验证
- 后端参数校验（@Valid）
- SQL注入防护（MyBatis-Plus）

### 4. 权限控制
- 用户只能操作自己的数据
- 管理员权限验证
- 问卷状态控制（已发布不可编辑）

### 5. 防重复提交
- IP地址限制
- 数据库唯一索引
- 前端防抖处理

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080  # 后端端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/questionnaire_db
    username: root
    password: root  # 修改为实际密码
```

### 前端配置 (main.js)

```javascript
const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080',  // 后端地址
    timeout: 30000
})
```

## 启动步骤

### 1. 数据库初始化

```bash
mysql -u root -p < qc-backend/src/main/resources/db/schema.sql
```

### 2. 启动后端

```bash
cd qc-backend
mvn spring-boot:run
```

默认管理员账号: admin / admin

### 3. 启动前端

```bash
cd qc-front
npm install
npm run serve
```

前端访问地址: http://localhost:8081

## 待优化功能

### 1. 性能优化
- [ ] 接口缓存（Redis）
- [ ] 数据库索引优化
- [ ] 分页查询优化
- [ ] 前端懒加载

### 2. 功能增强
- [ ] 问卷模板功能
- [ ] 导出答卷数据（Excel）
- [ ] 问卷二维码生成
- [ ] 短信验证码登录
- [ ] 邮件通知功能
- [ ] 积分系统
- [ ] 问卷分享到社交媒体

### 3. 安全增强
- [ ] 密码强度检测
- [ ] 验证码功能
- [ ] 操作日志记录
- [ ] 敏感词过滤
- [ ] 文件上传限制

### 4. 用户体验
- [ ] 自动保存草稿
- [ ] 问卷预览功能
- [ ] 移动端适配优化
- [ ] 暗黑模式
- [ ] 多语言支持

## 技术亮点

### 1. 后端技术
- MyBatis-Plus简化CRUD操作
- JWT无状态认证
- 全局异常处理
- 统一响应格式
- 逻辑删除机制
- 自动填充字段

### 2. 前端技术
- Vue3 Composition API
- Element Plus组件库
- Axios请求拦截
- 路由守卫鉴权
- 响应式设计
- ECharts数据可视化

### 3. 数据库设计
- 合理的表结构设计
- 索引优化查询
- 逻辑删除保护数据
- 外键关联保证完整性

## 常见问题

### 1. 跨域问题
后端已配置CORS，支持跨域请求

### 2. Token过期
Token有效期7天，过期后需重新登录

### 3. 密码忘记
暂不支持找回密码，请联系管理员重置

### 4. 问卷删除
软删除机制，删除的数据保留7天

### 5. 答卷统计
实时更新，刷新即可查看最新数据

## 项目文档

- [README.md](./README.md) - 项目总体说明
- [ADMIN_API.md](./ADMIN_API.md) - 管理员模块API文档
- qc-backend/README.md - 后端项目说明
- qc-backend/src/main/resources/db/schema.sql - 数据库脚本

## 开发团队

本项目由个人独立开发完成，包含前后端全栈实现。

## 许可证

MIT License

## 联系方式

如有问题或建议，欢迎提交Issue或Pull Request。
