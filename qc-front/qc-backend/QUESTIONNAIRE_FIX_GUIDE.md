# 问卷系统修复指南

## 问题说明

### 问题1: 截止日期显示"未设置截止时间"
**原因**: 数据库中的 `deadline` 字段为 NULL
**解决**: 在创建问卷时设置截止时间，或通过 SQL 更新

### 问题2: 点击"开始填写问卷"显示"问卷未发布或已暂停"
**原因**: 问卷的 `status` 字段不是 1（已发布状态）
**解决**: 需要将问卷状态更新为 1

---

## 快速修复步骤

### 步骤1: 连接到数据库并执行SQL

```bash
# 连接到MySQL数据库
mysql -u root -p questionnaire_db
```

或者在您的数据库管理工具（如 Navicat、phpMyAdmin 等）中执行以下SQL：

### 步骤2: 查看当前问卷状态

```sql
USE questionnaire_db;

-- 查看所有问卷
SELECT
    id,
    title,
    status,
    CASE status
        WHEN 0 THEN '草稿'
        WHEN 1 THEN '已发布'
        WHEN 2 THEN '已暂停'
        WHEN 3 THEN '已删除'
    END as status_text,
    deadline,
    create_time
FROM questionnaire
WHERE deleted = 0
ORDER BY create_time DESC;
```

### 步骤3: 发布问卷ID=12的问卷

```sql
-- 发布问卷（状态改为1，审核状态改为1）
UPDATE questionnaire
SET status = 1,
    audit_status = 1
WHERE id = 12;

-- 可选：同时设置截止时间（例如：30天后）
UPDATE questionnaire
SET deadline = DATE_ADD(NOW(), INTERVAL 30 DAY)
WHERE id = 12 AND deadline IS NULL;

-- 验证更新
SELECT id, title, status, deadline, create_time
FROM questionnaire
WHERE id = 12;
```

### 步骤4: 重新编译并重启后端

```bash
cd qc-backend
mvn clean compile
mvn spring-boot:run
```

---

## 测试流程

### 1. 测试问卷详情页
1. 打开浏览器访问: `http://localhost:8080/home`
2. 点击任意问卷卡片
3. **预期结果**：
   - ✅ 显示问卷标题、描述
   - ✅ 显示所有问题
   - ✅ 显示截止时间（如果设置了）
   - ✅ 显示"开始填写问卷"按钮（已发布状态）

### 2. 测试问卷填写
1. 在问卷详情页点击"开始填写问卷"
2. **预期结果**：
   - ✅ 跳转到填写页面
   - ✅ 显示所有问题
   - ✅ 可以选择/输入答案
   - ✅ 点击"提交问卷"后保存成功

### 3. 测试统计数据
1. 登录后访问创作者中心
2. 点击问卷的"统计"按钮
3. **预期结果**：
   - ✅ 显示总答卷数
   - ✅ 显示每个选项的选择人数
   - ✅ 显示饼图和进度条

---

## 批量操作SQL

### 发布所有草稿问卷
```sql
UPDATE questionnaire
SET status = 1, audit_status = 1
WHERE status = 0 AND deleted = 0;
```

### 给所有问卷设置30天截止时间
```sql
UPDATE questionnaire
SET deadline = DATE_ADD(create_time, INTERVAL 30 DAY)
WHERE deadline IS NULL AND deleted = 0;
```

### 删除所有测试答卷（重新测试统计功能）
```sql
-- 先查看要删除的答卷
SELECT COUNT(*) FROM answer WHERE deleted = 0;

-- 删除答卷详情
DELETE FROM answer_detail
WHERE answer_id IN (
    SELECT id FROM answer WHERE deleted = 0
);

-- 删除答卷
DELETE FROM answer WHERE deleted = 0;
```

---

## 常见问题

### Q1: 为什么显示"问卷不存在"？
**A**: 可能的原因：
1. 问卷ID不存在
2. 问卷被删除（deleted=1）
3. 后端服务未启动

**解决**:
```sql
-- 检查问卷是否存在
SELECT * FROM questionnaire WHERE id = 12;
```

### Q2: 为什么提交答案失败？
**A**: 可能的原因：
1. 问卷未发布（status!=1）
2. 问卷已截止（deadline < NOW()）
3. 同一IP已经填写过（数据库限制）

**解决**:
```sql
-- 检查问卷状态和截止时间
SELECT id, title, status, deadline,
       CASE
           WHEN deadline < NOW() THEN '已截止'
           ELSE '未截止'
       END as deadline_status
FROM questionnaire
WHERE id = 12;

-- 查看是否已有答卷
SELECT COUNT(*) as answer_count
FROM answer
WHERE questionnaire_id = 12 AND deleted = 0;

-- 清除IP限制（删除已有答卷）
DELETE FROM answer WHERE questionnaire_id = 12;
```

### Q3: 统计数据不准确？
**A**: 需要重新提交答卷或清除旧数据

**解决**:
```sql
-- 删除该问卷的所有答卷和详情
DELETE FROM answer_detail
WHERE answer_id IN (
    SELECT id FROM answer
    WHERE questionnaire_id = 12 AND deleted = 0
);

DELETE FROM answer
WHERE questionnaire_id = 12 AND deleted = 0;
```

---

## 问卷状态说明

| status值 | 状态名称 | 说明 | 能否编辑 | 能否填写 | 能否删除 |
|---------|---------|------|---------|---------|---------|
| 0 | 草稿 | 创建后默认状态 | ✅ 是 | ❌ 否 | ✅ 是 |
| 1 | 已发布 | 需要手动发布 | ❌ 否 | ✅ 是 | ❌ 否 |
| 2 | 已暂停 | 暂停收集 | ❌ 否 | ❌ 否 | ✅ 是 |
| 3 | 已删除 | 软删除状态 | ❌ 否 | ❌ 否 | ❌ 否 |

---

## API接口说明

### 1. 获取问卷详情（填写用）
```
GET /fill/{id}
返回: {
  code: 200,
  data: {
    id: 12,
    title: "问卷标题",
    status: 1,  // 必须是1才能填写
    deadline: "2025-01-27 15:00:00",
    questions: [...]
  }
}
```

### 2. 提交问卷答案
```
POST /fill/submit
请求体: {
  questionnaireId: 12,
  respondentName: "",
  respondentPhone: "",
  details: [
    { questionId: 1, optionId: 1 },
    { questionId: 2, optionId: 3 }
  ]
}
返回: {
  code: 200,
  data: 1  // 答卷ID
}
```

### 3. 获取统计数据
```
GET /answer/statistics/12
返回: {
  code: 200,
  data: {
    totalAnswers: 5,
    questionStats: [
      {
        questionId: 1,
        optionStats: [
          {
            optionId: 1,
            optionContent: "选项A",
            count: 3,
            percentage: 60
          }
        ]
      }
    ]
  }
}
```

---

## 后续建议

1. **在创建问卷时自动发布**: 修改 `QuestionnaireCreate.vue`，添加"并发布"选项
2. **添加问卷预览功能**: 允许用户在发布前预览问卷
3. **优化截止时间提示**: 在问卷详情页显示"还剩X天截止"
4. **添加分享功能**: 生成问卷分享链接
