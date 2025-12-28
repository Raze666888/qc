@echo off
echo ========================================
echo 管理员系统验证脚本
echo ========================================
echo.

echo [1/4] 检查后端服务...
netstat -ano | findstr :8081 >nul
if %errorlevel% == 0 (
    echo ✓ 后端服务正在运行 (端口 8081)
) else (
    echo ✗ 后端服务未运行！
    echo   请先启动后端: cd qc-backend ^&^& java -jar target/questionnaire-backend-1.0.0.jar
    pause
    exit /b 1
)
echo.

echo [2/4] 检查前端服务...
netstat -ano | findstr :8080 >nul
if %errorlevel% == 0 (
    echo ✓ 前端服务正在运行 (端口 8080)
) else (
    echo ✗ 前端服务未运行！
    echo   请先启动前端: cd qc-front ^&^& npm run serve
    pause
    exit /b 1
)
echo.

echo [3/4] 测试后端API...
curl -s http://localhost:8081/admin/users >nul
if %errorlevel% == 0 (
    echo ✓ 用户管理API正常工作
) else (
    echo ✗ 用户管理API测试失败！
    pause
    exit /b 1
)
echo.

echo [4/4] 测试其他API...
curl -s http://localhost:8081/admin/dashboard >nul
if %errorlevel% == 0 (
    echo ✓ 仪表盘API正常工作
) else (
    echo ✗ 仪表盘API测试失败！
)

curl -s http://localhost:8081/admin/questionnaires >nul
if %errorlevel% == 0 (
    echo ✓ 问卷管理API正常工作
) else (
    echo ✗ 问卷管理API测试失败！
)

curl -s http://localhost:8081/admin/banners >nul
if %errorlevel% == 0 (
    echo ✓ 轮播图管理API正常工作
) else (
    echo ✗ 轮播图管理API测试失败！
)
echo.

echo ========================================
echo 验证完成！
echo ========================================
echo.
echo 请执行以下步骤完成前端验证：
echo.
echo 1. 打开浏览器访问: http://localhost:8080/login
echo 2. 登录管理员账号:
echo    账号: 17836900831
echo    密码: aa111111
echo.
echo 3. 登录后按 F12 打开开发者工具
echo 4. 在控制台执行以下命令清除缓存:
echo    localStorage.clear()
echo    location.reload()
echo.
echo 5. 测试各个模块功能
echo.
echo ========================================
pause
