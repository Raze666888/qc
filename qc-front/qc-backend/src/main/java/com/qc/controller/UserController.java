package com.qc.controller;

import com.qc.dto.LoginDTO;
import com.qc.dto.UserDTO;
import com.qc.service.UserService;
import com.qc.utils.JwtUtil;
import com.qc.vo.Result;
import com.qc.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody UserDTO userDTO) {
        Map<String, Object> result = userService.register(userDTO);
        return Result.success(result);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.login(loginDTO);
        return Result.success(result);
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @PutMapping("/update")
    public Result<Void> update(HttpServletRequest request, @RequestBody UserDTO userDTO) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        userService.updateUser(userId, userDTO);
        return Result.success();
    }
}
