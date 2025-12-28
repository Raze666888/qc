package com.qc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qc.dto.AuditDTO;
import com.qc.dto.BannerDTO;
import com.qc.dto.UserDTO;
import com.qc.service.AdminService;
import com.qc.service.BannerService;
import com.qc.service.QuestionnaireService;
import com.qc.service.StatisticsService;
import com.qc.service.UserService;
import com.qc.utils.JwtUtil;
import com.qc.vo.BannerVO;
import com.qc.vo.OptionStatisticsVO;
import com.qc.vo.PageResult;
import com.qc.vo.QuestionStatisticsVO;
import com.qc.vo.QuestionnaireDetailVO;
import com.qc.vo.QuestionnaireVO;
import com.qc.vo.Result;
import com.qc.vo.StatisticsVO;
import com.qc.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private BannerService bannerService;

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

    // ==================== 个人中心 ====================

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/profile")
    public Result<UserVO> getProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 更新当前管理员信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        userService.updateUser(userId, userDTO);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new com.qc.exception.BusinessException("原密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new com.qc.exception.BusinessException("新密码长度不能少于6位");
        }

        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    // ==================== 问卷管理 ====================

    /**
     * 分页查询所有问卷（管理员视角）
     */
    @GetMapping("/questionnaires")
    public Result<IPage<QuestionnaireVO>> getQuestionnairePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus) {
        IPage<QuestionnaireVO> result = questionnaireService.getAdminQuestionnairePage(pageNum, pageSize, keyword, status, auditStatus);
        return Result.success(result);
    }

    /**
     * 获取问卷详情（管理员视角）
     */
    @GetMapping("/questionnaires/{id}")
    public Result<QuestionnaireDetailVO> getQuestionnaireDetail(@PathVariable Long id) {
        QuestionnaireDetailVO detail = questionnaireService.getQuestionnaireDetailAdmin(id);
        return Result.success(detail);
    }

    /**
     * 发布/下架问卷
     */
    @PutMapping("/questionnaires/{id}/status")
    public Result<Void> updateQuestionnaireStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        Integer status = Integer.valueOf(params.get("status").toString());
        questionnaireService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除问卷
     */
    @DeleteMapping("/questionnaires/{id}")
    public Result<Void> deleteQuestionnaire(@PathVariable Long id) {
        questionnaireService.deleteQuestionnaire(id);
        return Result.success();
    }

    // ==================== 问卷统计 ====================

    /**
     * 获取问卷统计数据
     */
    @GetMapping("/questionnaires/{id}/statistics")
    public Result<StatisticsVO> getQuestionnaireStatistics(@PathVariable Long id) {
        StatisticsVO statistics = statisticsService.getQuestionnaireStatistics(id);
        return Result.success(statistics);
    }

    // ==================== 轮播图管理 ====================

    /**
     * 分页查询轮播图
     */
    @GetMapping("/banners")
    public Result<IPage<BannerVO>> getBannerPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        IPage<BannerVO> result = bannerService.getBannerPage(pageNum, pageSize, status);
        return Result.success(result);
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/banners/{id}")
    public Result<BannerVO> getBannerDetail(@PathVariable Long id) {
        BannerVO banner = bannerService.getBannerById(id);
        return Result.success(banner);
    }

    /**
     * 创建轮播图
     */
    @PostMapping("/banners")
    public Result<Void> createBanner(@RequestBody BannerDTO bannerDTO) {
        bannerService.createBanner(bannerDTO);
        return Result.success();
    }

    /**
     * 更新轮播图
     */
    @PutMapping("/banners/{id}")
    public Result<Void> updateBanner(
            @PathVariable Long id,
            @RequestBody BannerDTO bannerDTO) {
        bannerService.updateBanner(id, bannerDTO);
        return Result.success();
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/banners/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success();
    }

    /**
     * 切换轮播图状态
     */
    @PutMapping("/banners/{id}/toggle")
    public Result<Void> toggleBannerStatus(@PathVariable Long id) {
        bannerService.toggleBannerStatus(id);
        return Result.success();
    }

    /**
     * 获取所有启用的轮播图（公开接口，无需管理员权限）
     */
    @GetMapping("/banners/active")
    public Result<List<BannerVO>> getActiveBanners() {
        List<BannerVO> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }
}
