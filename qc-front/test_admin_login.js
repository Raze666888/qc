// 测试管理员登录
const testAdminLogin = async () => {
  const response = await fetch('http://localhost:8081/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      account: '17836900831',
      password: 'aa111111'
    })
  });

  const data = await response.json();
  console.log('Login Response:', JSON.stringify(data, null, 2));

  if (data.code === 200) {
    console.log('User Role:', data.data.user.role);
    console.log('Is Admin:', data.data.user.role === 1);
  }
};

// 在浏览器控制台运行：testAdminLogin()
