# 管理员系统快速测试指南

## 🚀 快速开始

### 1. 确认服务运行状态

```bash
# 检查后端是否运行
netstat -ano | findstr :8081

# 检查前端是否运行
netstat -ano | findstr :8080
```

如果服务未运行，请启动它们：

```bash
# 启动后端（在qc-backend目录）
java -jar target/questionnaire-backend-1.0.0.jar

# 启动前端（在qc-front目录）
npm run serve
```

### 2. 登录管理员账号

1. 访问: `http://localhost:8080/login`
2. 输入账号: `17836900831`
3. 输入密码: `aa111111`
4. 点击登录
5. 应该自动跳转到 `http://localhost:8080/admin`

---

## ✅ 测试清单

### 模块1: 仪表盘

**路径**: `/admin`

**检查项**:
- [ ] 显示总用户数
- [ ] 显示今日新增用户
- [ ] 显示待审核问卷数
- [ ] 显示总问卷数

**预期结果**:
```
总用户数: 4
今日新增: 4
待审核: 6
问卷总数: 9
```

---

### 模块2: 个人中心

**路径**: `/admin/profile`

**功能测试**:

1. **查看基本信息**
   - [ ] 显示用户名: 17836900831
   - [ ] 显示昵称: 系统管理员
   - [ ] 显示手机号: 17836900831
   - [ ] 显示角色: 管理员

2. **修改基本信息**
   - [ ] 修改邮箱为测试邮箱
   - [ ] 点击保存
   - [ ] 显示"修改成功"提示
   - [ ] 刷新页面，邮箱已更新

3. **修改密码**
   - [ ] 切换到"修改密码"标签
   - [ ] 输入原密码: aa111111
   - [ ] 输入新密码: test123456
   - [ ] 点击确认修改
   - [ ] 显示"密码修改成功"
   - [ ] 退出登录，用新密码重新登录
   - [ ] 登录成功

**API测试**:
```bash
# 获取管理员信息（需要token）
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8081/admin/profile

# 修改密码（需要token）
curl -X PUT -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"aa111111","newPassword":"test123456"}' \
  http://localhost:8081/admin/password
```

---

### 模块3: 用户管理

**路径**: `/admin/user-manage`

**功能测试**:

1. **查看用户列表**
   - [ ] 显示4个用户
   - [ ] 显示用户ID、用户名、手机号、邮箱、角色
   - [ ] 分页正常工作

2. **搜索功能**
   - [ ] 输入关键词: "178"
   - [ ] 点击搜索
   - [ ] 显示匹配的用户

3. **启用/禁用用户**
   - [ ] 选择一个普通用户（非管理员）
   - [ ] 点击"禁用"按钮
   - [ ] 确认操作
   - [ ] 用户状态变为"已禁用"
   - [ ] 再次点击"启用"
   - [ ] 用户状态恢复为"正常"

**API测试**:
```bash
# 获取用户列表
curl http://localhost:8081/admin/users

# 搜索用户
curl "http://localhost:8081/admin/users?keyword=178"

# 更新用户状态
curl -X PUT -H "Content-Type: application/json" \
  -d '{"userId":3,"status":1}' \
  http://localhost:8081/admin/user/status
```

---

### 模块4: 问卷管理

**路径**: `/admin/questionnaire-manage`

**功能测试**:

1. **查看问卷列表**
   - [ ] 显示多个问卷
   - [ ] 显示问卷标题、分类、状态、审核状态、创建时间

2. **筛选功能**
   - [ ] 选择状态: "已发布"
   - [ ] 点击筛选
   - [ ] 只显示已发布的问卷
   - [ ] 选择审核状态: "待审核"
   - [ ] 只显示待审核的问卷

3. **查看详情**
   - [ ] 点击某个问卷的"查看详情"
   - [ ] 弹窗显示问卷完整信息
   - [ ] 显示所有问题和选项

4. **发布/下架**
   - [ ] 选择一个草稿状态的问卷
   - [ ] 点击"发布"
   - [ ] 确认操作
   - [ ] 问卷状态变为"已发布"
   - [ ] 点击"下架"
   - [ ] 问卷状态变为"已下架"

5. **删除问卷**
   - [ ] 选择一个测试问卷
   - [ ] 点击"删除"
   - [ ] 二次确认
   - [ ] 问卷从列表中移除

**API测试**:
```bash
# 获取问卷列表
curl http://localhost:8081/admin/questionnaires

# 筛选已发布的问卷
curl "http://localhost:8081/admin/questionnaires?status=1"

# 获取问卷详情
curl http://localhost:8081/admin/questionnaires/15

# 发布问卷
curl -X PUT -H "Content-Type: application/json" \
  -d '{"status":1}' \
  http://localhost:8081/admin/questionnaires/15/status

# 删除问卷
curl -X DELETE http://localhost:8081/admin/questionnaires/15
```

---

### 模块5: 问卷统计

**路径**: `/admin/questionnaire-statistics`

**功能测试**:

1. **选择问卷**
   - [ ] 点击下拉选择框
   - [ ] 显示多个问卷选项
   - [ ] 选择一个有答卷的问卷

2. **查看统计**
   - [ ] 显示总答卷数
   - [ ] 显示问题列表
   - [ ] 每个问题显示饼图
   - [ ] 饼图显示选项分布
   - [ ] 下方显示数据表格
   - [ ] 表格显示选项内容、选择人数、占比
   - [ ] 显示进度条可视化

3. **切换问题**
   - [ ] 点击不同问题
   - [ ] 饼图更新显示对应问题
   - [ ] 数据表格更新

**API测试**:
```bash
# 获取问卷统计
curl http://localhost:8081/admin/questionnaires/15/statistics

# 预期返回:
{
  "code": 200,
  "data": {
    "questionnaireId": 15,
    "title": "888888",
    "totalAnswers": 5,
    "questionStatistics": [...]
  }
}
```

---

### 模块6: 轮播图管理

**路径**: `/admin/banner-manage`

**功能测试**:

1. **查看轮播图列表**
   - [ ] 显示轮播图列表（可能为空）

2. **新增轮播图**
   - [ ] 点击"新增轮播图"
   - [ ] 填写表单:
     - 标题: "测试轮播图"
     - 描述: "这是一个测试"
     - 图片URL: "https://example.com/image.jpg"
     - 链接URL: "https://example.com"
     - 排序: 1
     - 状态: 启用
   - [ ] 点击确定
   - [ ] 轮播图添加成功
   - [ ] 列表显示新增的轮播图

3. **编辑轮播图**
   - [ ] 点击某个轮播图的"编辑"
   - [ ] 修改标题
   - [ ] 点击确定
   - [ ] 轮播图更新成功

4. **启用/禁用**
   - [ ] 点击某个轮播图的"启用/禁用"开关
   - [ ] 状态切换成功

5. **删除轮播图**
   - [ ] 点击某个轮播图的"删除"
   - [ ] 二次确认
   - [ ] 轮播图从列表移除

**API测试**:
```bash
# 获取轮播图列表
curl http://localhost:8081/admin/banners

# 创建轮播图
curl -X POST -H "Content-Type: application/json" \
  -d '{
    "title":"测试轮播图",
    "description":"测试描述",
    "imageUrl":"https://example.com/image.jpg",
    "linkUrl":"https://example.com",
    "orderNum":1,
    "status":1
  }' \
  http://localhost:8081/admin/banners

# 获取启用的轮播图（公开接口）
curl http://localhost:8081/admin/banners/active

# 更新轮播图
curl -X PUT -H "Content-Type: application/json" \
  -d '{
    "title":"更新的标题",
    "description":"更新的描述"
  }' \
  http://localhost:8081/admin/banners/1

# 删除轮播图
curl -X DELETE http://localhost:8081/admin/banners/1

# 切换状态
curl -X PUT http://localhost:8081/admin/banners/1/toggle
```

---

## 🔍 问题排查

### 如果出现404错误

1. **检查后端是否运行**
   ```bash
   netstat -ano | findstr :8081
   ```

2. **重启后端服务**
   ```bash
   # 停止现有进程
   taskkill //F //PID <进程ID>

   # 重新启动
   cd qc-backend
   java -jar target/questionnaire-backend-1.0.0.jar
   ```

3. **清除浏览器缓存**
   ```javascript
   // 在浏览器控制台执行
   localStorage.clear()
   location.reload()
   ```

### 如果出现权限错误

1. **检查localStorage**
   ```javascript
   // 在浏览器控制台执行
   console.log(localStorage.getItem('token'))
   console.log(localStorage.getItem('role'))  // 应该是 'admin'
   ```

2. **重新登录**
   - 退出登录
   - 清除localStorage
   - 重新登录管理员账号

### 如果前端页面空白

1. **检查前端是否运行**
   ```bash
   netstat -ano | findstr :8080
   ```

2. **检查浏览器控制台错误**
   - 按F12打开开发者工具
   - 查看Console标签的错误信息
   - 查看Network标签的请求状态

3. **重启前端服务**
   ```bash
   # Ctrl+C 停止当前服务
   npm run serve
   ```

---

## 📊 测试数据

### 管理员账号
```
用户名: 17836900831
密码: aa111111
角色: 管理员
```

### 测试用户
```
用户名: demo
密码: 任意6位以上密码
角色: 普通用户
```

### 测试问卷
- 应该有多个问卷可用于测试
- 建议先创建一个测试问卷并填写几份答卷
- 然后在问卷统计模块查看数据

---

## ✅ 测试完成检查表

**所有功能测试通过后，请确认以下内容**:

- [ ] 仪表盘数据显示正常
- [ ] 个人中心可以修改信息和密码
- [ ] 用户管理可以查看、搜索、启用/禁用用户
- [ ] 问卷管理可以查看、筛选、发布、删除问卷
- [ ] 问卷统计可以显示数据和图表
- [ ] 轮播图管理可以增删改查轮播图
- [ ] 所有API返回200状态码
- [ ] 没有控制台错误
- [ ] 页面跳转正常

---

## 🎯 性能测试

### 并发测试

```bash
# 使用Apache Bench测试仪表盘接口
ab -n 100 -c 10 http://localhost:8081/admin/dashboard

# 预期结果:
# 成功率: 100%
# 平均响应时间: < 500ms
```

### 数据量测试

1. 创建100个测试用户
2. 创建50个测试问卷
3. 为每个问卷创建10份答卷
4. 测试分页性能
5. 测试统计性能

---

## 📝 测试报告模板

```markdown
## 管理员系统测试报告

**测试日期**: 2025-XX-XX
**测试人员**: XXX
**环境**: 开发环境

### 功能测试

| 模块 | 测试项 | 状态 | 备注 |
|------|--------|------|------|
| 仪表盘 | 数据显示 | ✅/❌ | |
| 个人中心 | 修改信息 | ✅/❌ | |
| 个人中心 | 修改密码 | ✅/❌ | |
| 用户管理 | 查看列表 | ✅/❌ | |
| 用户管理 | 搜索 | ✅/❌ | |
| 用户管理 | 启用/禁用 | ✅/❌ | |
| 问卷管理 | 查看列表 | ✅/❌ | |
| 问卷管理 | 筛选 | ✅/❌ | |
| 问卷管理 | 发布/下架 | ✅/❌ | |
| 问卷管理 | 删除 | ✅/❌ | |
| 问卷统计 | 数据展示 | ✅/❌ | |
| 问卷统计 | 图表渲染 | ✅/❌ | |
| 轮播图 | 增删改查 | ✅/❌ | |

### API测试

| 接口 | 状态码 | 响应时间 | 状态 |
|------|--------|----------|------|
| GET /admin/dashboard | 200 | xx ms | ✅/❌ |
| GET /admin/users | 200 | xx ms | ✅/❌ |
| GET /admin/questionnaires | 200 | xx ms | ✅/❌ |
| ... | ... | ... | ... |

### 发现的问题

1. 问题描述
   - 复现步骤
   - 预期结果
   - 实际结果

### 总体评价

- 功能完整性: ⭐⭐⭐⭐⭐
- 易用性: ⭐⭐⭐⭐⭐
- 性能: ⭐⭐⭐⭐⭐
- 稳定性: ⭐⭐⭐⭐⭐

### 建议

1. 建议内容
2. 建议内容
```

---

**最后更新**: 2025-12-28
**快速测试指南 - 管理员系统 v1.0**
