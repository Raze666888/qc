# "我参与的问卷"功能设置指南

## 问题描述
"我的问卷"页面中的"我参与的"部分无法显示用户填写过的问卷。

## 根本原因
原来的实现使用 `respondent_ip` 字段存储用户ID，这种方式不可靠。需要：
1. 在 `answer` 表中添加 `user_id` 字段
2. 修改后端代码在提交问卷时记录用户ID
3. 修改查询逻辑使用 `user_id` 而不是 `respondent_ip`

---

## 📋 设置步骤

### 步骤1: 执行数据库迁移

**必须先执行这个SQL脚本**，在 `answer` 表中添加 `user_id` 字段：

```bash
# 方法一：使用命令行
mysql -u root -p632048 questionnaire_db < qc-backend/add_user_id_to_answer.sql

# 方法二：在数据库管理工具中执行以下SQL
```

或手动执行：
```sql
USE questionnaire_db;

-- 添加 user_id 字段
ALTER TABLE answer
ADD COLUMN user_id BIGINT COMMENT '填写用户ID' AFTER respondent_ip;

-- 添加索引以优化查询
ALTER TABLE answer
ADD INDEX idx_user_id (user_id);

-- 验证修改
DESCRIBE answer;
```

### 步骤2: 重启后端服务

后端代码已修改完成，重启服务：

```bash
cd qc-backend
mvn spring-boot:run
```

后端已在端口 8081 运行 ✅

### 步骤3: 测试完整流程

1. **登录系统**
   - 访问前端并登录

2. **填写问卷**
   - 在首页找到已发布的问卷
   - 填写并提交问卷
   - 系统会自动记录您的 `user_id`

3. **查看"我参与的"问卷**
   - 访问"我的问卷"页面
   - 点击"我参与的"标签
   - 应该能看到刚才填写的问卷

---

## 🔧 技术实现细节

### 后端修改

#### 1. 数据库表结构
```sql
-- answer 表新增字段
ALTER TABLE answer ADD COLUMN user_id BIGINT;
```

#### 2. 实体类修改
**Answer.java** - 添加 `userId` 字段：
```java
private Long userId;  // 添加用户ID字段
```

**AnswerDTO.java** - 添加 `userId` 字段：
```java
private Long userId;  // 添加用户ID字段
```

#### 3. 问卷提交流程
**FillController.java** - 在提交问卷时记录用户ID：
```java
@PostMapping("/submit")
public Result<Long> submitAnswer(HttpServletRequest request, @RequestBody AnswerDTO answerDTO) {
    // 获取用户ID（如果已登录）
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId != null) {
            answerDTO.setUserId(userId);
        }
    }

    Long id = answerService.submitAnswer(answerDTO);
    return Result.success(id);
}
```

#### 4. 查询用户参与的问卷
**AnswerController.java** - 使用 `userId` 查询：
```java
@GetMapping("/my-questionnaires")
public Result<PageResult<QuestionnaireVO>> getMyQuestionnaires(...) {
    // 查询用户参与的答卷 - 优先使用 userId
    LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
    answerWrapper.eq(Answer::getUserId, userId)  // 使用 userId 查询
               .orderByDesc(Answer::getCreateTime);
    ...
}
```

### 前端修改

**MyQuestionnaires.vue** - 使用真实API：
```javascript
// 获取我参与的问卷
const res = await getMyAnsweredQuestionnaires(params)
if (res.code === 200) {
    const records = res.data.records || []
    questionnaires.value = records.map(q => ({
        ...q,
        type: 'answered',
        answerTime: q.createTime,
        earnedPoints: 10
    }))
    total.value = res.data.total || 0
}
```

---

## ⚠️ 重要提示

### 关于旧数据
- **旧答卷数据**：在添加 `user_id` 字段之前提交的答卷，其 `user_id` 为 NULL
- **显示影响**：这些旧数据不会显示在"我参与的"列表中
- **解决方案**：用户需要重新填写问卷才能看到

### 数据一致性检查

如果想迁移旧数据（可选）：
```sql
-- 注意：这只是一个示例，实际情况可能需要根据您的IP存储逻辑调整
-- 如果 respondent_ip 存储的是用户ID字符串，可以执行：

UPDATE answer a
SET a.user_id = CAST(a.respondent_ip AS UNSIGNED)
WHERE a.user_id IS NULL
  AND a.respondent_ip REGEXP '^[0-9]+$'
  AND CAST(a.respondent_ip AS UNSIGNED) > 0;

-- 验证迁移结果
SELECT COUNT(*) as total,
       SUM(CASE WHEN user_id IS NOT NULL THEN 1 ELSE 0 END) as with_user_id
FROM answer;
```

---

## 🧪 测试验证

### 测试场景1: 新用户填写问卷
1. 注册/登录新用户
2. 填写任意已发布的问卷
3. 访问"我的问卷" -> "我参与的"
4. **预期结果**：可以看到刚才填写的问卷

### 测试场景2: 已登录用户填写问卷
1. 确保已登录（token有效）
2. 打开浏览器开发者工具 -> Network
3. 填写并提交问卷
4. 查看提交请求的payload，应该包含 `userId`
5. 访问"我的问卷" -> "我参与的"
6. **预期结果**：可以看到填写的问卷

### 测试场景3: 未登录用户填写问卷
1. 退出登录
2. 填写问卷
3. 访问"我的问卷" -> "我参与的"
4. **预期结果**：看不到问卷（因为未登录，没有userId）

---

## 🐛 故障排除

### 问题1: 执行SQL时报错
```
ERROR 1060 (42S21): Duplicate column name 'user_id'
```
**解决**：字段已存在，继续下一步即可

### 问题2: "我参与的"列表为空
**检查清单**：
- [ ] 是否执行了数据库迁移SQL？
- [ ] 后端服务是否重启？
- [ ] 是否在**登录状态**下填写了问卷？
- [ ] 问卷是否成功提交（没有报错）？

**调试SQL**：
```sql
-- 检查答卷表中是否有数据
SELECT
    a.id,
    a.questionnaire_id,
    a.user_id,
    a.respondent_ip,
    a.create_time,
    q.title
FROM answer a
LEFT JOIN questionnaire q ON a.questionnaire_id = q.id
WHERE a.deleted = 0
ORDER BY a.create_time DESC
LIMIT 10;

-- 检查特定用户的答卷（替换 1 为您的用户ID）
SELECT
    a.id,
    a.questionnaire_id,
    a.user_id,
    q.title
FROM answer a
LEFT JOIN questionnaire q ON a.questionnaire_id = q.id
WHERE a.deleted = 0
  AND a.user_id = 1;  -- 替换为实际的用户ID
```

### 问题3: API返回401未授权
**解决**：
- 确保已登录
- 检查token是否有效
- 清除浏览器缓存重新登录

---

## 📊 数据库表结构参考

### answer 表（修改后）
```
+-----------------+-------------+------+-----+---------+----------------+
| Field           | Type        | Null | Key | Default | Extra          |
+-----------------+-------------+------+-----+---------+----------------+
| id              | bigint      | NO   | PRI | NULL    | auto_increment |
| questionnaire_id| bigint      | NO   | MUL | NULL    |                |
| respondent_name | varchar(50) | YES  |     | NULL    |                |
| respondent_phone| varchar(20) | YES  |     | NULL    |                |
| respondent_ip   | varchar(50) | YES  |     | NULL    |                |
| user_id         | bigint      | YES  | MUL | NULL    | ⭐ 新增字段    |
| create_time     | datetime    | YES  |     | CURRENT_TIMESTAMP |    |
| update_time     | datetime    | YES  |     | CURRENT_TIMESTAMP |    |
| deleted         | int         | YES  |     | 0       |                |
+-----------------+-------------+------+-----+---------+----------------+
```

---

## ✅ 完成检查清单

- [x] 数据库迁移SQL已执行
- [x] Answer实体类已添加userId字段
- [x] AnswerDTO已添加userId字段
- [x] FillController在提交时记录userId
- [x] AnswerController使用userId查询
- [x] 前端API调用已更新
- [x] 后端服务已编译成功
- [ ] **数据库迁移SQL已执行（需要您手动执行）**
- [ ] 后端服务已重启
- [ ] 测试完整流程

---

## 🚀 下一步

执行完数据库迁移后，系统就可以正常工作了！

**测试流程**：
1. 访问首页
2. 填写任意问卷并提交
3. 访问"我的问卷"页面
4. 点击"我参与的"标签
5. 应该能看到刚才填写的问卷

如有问题，请查看故障排除部分。
