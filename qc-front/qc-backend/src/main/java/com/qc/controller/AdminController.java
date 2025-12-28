package com.qc.controller;

import com.qc.dto.AuditDTO;
import com.qc.service.AdminService;
import com.qc.utils.JwtUtil;
import com.qc.vo.PageResult;
import com.qc.vo.QuestionnaireVO;
import com.qc.vo.Result;
import com.qc.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private void checkAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer role = jwtUtil.getRoleFromToken(token);
        if (role == null || role != 1) {
            throw new com.qc.exception.BusinessException(403, "无权限访问");
        }
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<UserVO> result = adminService.getUserPage(pageNum, pageSize, keyword, status);
        return Result.success(result);
    }

    @PutMapping("/user/status")
    public Result<Void> updateUserStatus(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        adminService.updateUserStatus(userId, status);
        return Result.success();
    }

    // ==================== 问卷审核 ====================

    @GetMapping("/audits")
    public Result<PageResult<QuestionnaireVO>> getAuditPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer auditStatus) {
        PageResult<QuestionnaireVO> result = adminService.getAuditPage(pageNum, pageSize, auditStatus);
        return Result.success(result);
    }

    @PostMapping("/audit")
    public Result<Void> auditQuestionnaire(@RequestBody AuditDTO auditDTO) {
        adminService.auditQuestionnaire(auditDTO.getId(), auditDTO.getAuditStatus(), auditDTO.getReason());
        return Result.success();
    }

    // ==================== 统计数据 ====================

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return Result.success(stats);
    }
}
