# 管理员登录问题修复指南

## 问题描述
管理员账号登录后显示"请以管理员身份登录"或"获取统计数据失败"

## 已修复的文件

### 1. LoginPage.vue
**修复内容：**
- 添加了role字段到localStorage
- 根据角色正确跳转到对应页面

```javascript
// 存储role字段
localStorage.setItem('role', userData.role === 1 ? 'admin' : 'user')

// 根据角色跳转
if (userData.role === 1) {
  ElMessage.success(`欢迎管理员${userData.username}，登录成功！`)
  router.push('/admin')
} else {
  ElMessage.success(`欢迎${userData.nickname || userData.username}，登录成功！`)
  const redirect = route.query.redirect || '/home'
  router.push(redirect)
}
```

### 2. AdminIndex.vue
**修复内容：**
- 简化验证逻辑，直接从localStorage读取role
- 移除重复的用户信息请求

```javascript
onMounted(() => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (!token) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  if (role !== 'admin') {
    ElMessage.error('您不是管理员，无法访问该页面')
    router.push('/home')
    return
  }

  username.value = localStorage.getItem('username') || '管理员'
  avatarUrl.value = `https://api.dicebear.com/7.x/avataaars/svg?seed=${username.value}`
})
```

## 测试步骤

### 1. 清除浏览器缓存
```bash
# 在浏览器中打开开发者工具(F12)
# 在Console中运行：
localStorage.clear()
```

### 2. 登录测试
1. 访问 `http://localhost:8080/login`
2. 输入管理员账号：
   - 账号：`17836900831`
   - 密码：`aa111111`
3. 点击登录

### 3. 验证跳转
- 应该自动跳转到 `http://localhost:8080/admin`
- 不应该再显示"请以管理员身份登录"的错误

### 4. 检查localStorage
在浏览器控制台运行：
```javascript
console.log('Token:', localStorage.getItem('token'))
console.log('Role:', localStorage.getItem('role'))
console.log('Username:', localStorage.getItem('username'))
```

应该看到：
```
Token: eyJhbGciOiJIUzI1NiJ9... (JWT token)
Role: admin
Username: 17836900831
```

## 常见问题排查

### 问题1：仍然显示"请以管理员身份登录"
**解决方案：**
1. 确保已清除所有localStorage数据
2. 重新登录
3. 检查网络请求，确认登录接口返回的role字段值为1

### 问题2：登录后跳转到用户首页
**解决方案：**
1. 检查数据库中管理员账号的role字段是否为1
```sql
SELECT username, role FROM sys_user WHERE username = '17836900831';
```

2. 如果不是1，更新为1：
```sql
UPDATE sys_user SET role = 1 WHERE username = '17836900831';
```

### 问题3：获取统计数据失败
**解决方案：**
1. 确保后端服务正在运行（端口8081）
2. 检查浏览器控制台的网络请求
3. 确认token是否正确传递

## 数据库验证

### 验证管理员账号
```sql
-- 检查管理员信息
SELECT id, username, phone, role FROM sys_user WHERE username = '17836900831';

-- 确保role字段为1（管理员）
-- role = 0: 普通用户
-- role = 1: 管理员
```

### 验证密码
```sql
-- 管理员密码是：aa111111
-- MD5: 85bf5831e593431e882887e077400b7f
SELECT username, password, role FROM sys_user WHERE username = '17836900831';
```

## 后端日志检查

如果问题仍然存在，检查后端日志：

```bash
# 查看Spring Boot日志
cd qc-backend
tail -f spring.log
```

查找关键信息：
- 登录请求：`POST /user/login`
- Token生成：`generateToken`
- 用户信息：`getUserInfo`

## 调试技巧

### 在浏览器控制台调试
```javascript
// 1. 检查当前路由
console.log('当前路由:', window.location.href)

// 2. 检查localStorage
console.log('所有存储:', {...localStorage})

// 3. 手动测试登录API
fetch('http://localhost:8081/user/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    account: '17836900831',
    password: 'aa111111'
  })
})
.then(res => res.json())
.then(data => console.log('登录结果:', data))
```

### 查看网络请求
1. 打开浏览器开发者工具（F12）
2. 切换到Network标签
3. 登录并查看请求：
   - `/user/login` - 登录接口
   - `/admin/dashboard` - 管理员仪表盘

## 完整登录流程

### 成功流程
1. 用户输入账号密码
2. 前端调用 `/user/login`
3. 后端验证并返回：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "17836900831",
      "role": 1,
      ...
    }
  }
}
```
4. 前端存储信息到localStorage：
   - token
   - userId
   - username
   - role = 'admin'
5. 判断role === 1，跳转到 `/admin`
6. AdminIndex验证role === 'admin'
7. 加载管理员仪表盘

### 失败场景
1. **role不是1** → 跳转到用户首页
2. **localStorage的role不是'admin'** → 显示"不是管理员"错误
3. **token无效** → 跳转回登录页

## 文件清单

### 前端文件
- ✅ LoginPage.vue - 登录页面
- ✅ AdminIndex.vue - 管理员布局
- ✅ AdminDashboard.vue - 管理员仪表盘
- ✅ router/index.js - 路由配置

### 后端文件
- ✅ UserController.java - 用户控制器
- ✅ UserService.java - 用户服务
- ✅ JwtUtil.java - JWT工具类
- ✅ UserVO.java - 用户视图对象

## 联系方式

如果问题仍然存在，请提供：
1. 浏览器控制台的完整错误信息
2. 网络请求的详细信息
3. 后端日志相关片段
4. 数据库中管理员账号的完整信息

---

最后更新：2025-12-28
