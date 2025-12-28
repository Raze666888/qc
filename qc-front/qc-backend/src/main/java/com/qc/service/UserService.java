package com.qc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.dto.LoginDTO;
import com.qc.dto.UserDTO;
import com.qc.entity.User;
import com.qc.exception.BusinessException;
import com.qc.mapper.UserMapper;
import com.qc.utils.JwtUtil;
import com.qc.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Map<String, Object> register(UserDTO userDTO) {
        // 校验手机号/邮箱格式
        if (!PHONE_PATTERN.matcher(userDTO.getPhone()).matches() && !EMAIL_PATTERN.matcher(userDTO.getEmail()).matches()) {
            throw new BusinessException("手机号或邮箱格式不正确");
        }

        // 校验密码长度
        if (userDTO.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (userDTO.getPhone() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, userDTO.getPhone());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setRole(0); // 默认普通用户
        user.setPoints(0);
        userMapper.insert(user);

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", getUserVO(user));

        return result;
    }

    public Map<String, Object> login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, loginDTO.getAccount())
               .or()
               .eq(User::getUsername, loginDTO.getAccount());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        // 验证密码
        String encodedPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(encodedPassword)) {
            throw new BusinessException("密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", getUserVO(user));

        return result;
    }

    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return getUserVO(user);
    }

    public void updateUser(Long userId, UserDTO userDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (userDTO.getNickname() != null) {
            user.setNickname(userDTO.getNickname());
        }

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            if (userDTO.getPassword().length() < 6) {
                throw new BusinessException("密码长度不能少于6位");
            }
            user.setPassword(DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes(StandardCharsets.UTF_8)));
        }

        userMapper.updateById(user);
    }

    private UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
