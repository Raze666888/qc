package com.qc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.entity.Questionnaire;
import com.qc.entity.User;
import com.qc.exception.BusinessException;
import com.qc.mapper.QuestionnaireMapper;
import com.qc.mapper.UserMapper;
import com.qc.vo.PageResult;
import com.qc.vo.QuestionnaireVO;
import com.qc.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    // ==================== 用户管理 ====================

    public PageResult<UserVO> getUserPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：用户名、手机号、邮箱
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getEmail, keyword));
        }

        // 状态筛选（未删除/已删除）
        if (status != null) {
            wrapper.eq(User::getDeleted, status);
        }

        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> result = userMapper.selectPage(page, wrapper);

        List<UserVO> userVOList = result.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), userVOList);
    }

    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 禁用/启用用户（通过deleted字段实现）
        user.setDeleted(status);
        userMapper.updateById(user);
    }

    // ==================== 问卷审核 ====================

    public PageResult<QuestionnaireVO> getAuditPage(Integer pageNum, Integer pageSize, Integer auditStatus) {
        Page<Questionnaire> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Questionnaire> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的问卷
        wrapper.eq(Questionnaire::getStatus, 1);

        // 审核状态筛选
        if (auditStatus != null) {
            wrapper.eq(Questionnaire::getAuditStatus, auditStatus);
        }

        wrapper.orderByDesc(Questionnaire::getCreateTime);

        IPage<Questionnaire> result = questionnaireMapper.selectPage(page, wrapper);

        List<QuestionnaireVO> voList = result.getRecords().stream().map(q -> {
            QuestionnaireVO vo = new QuestionnaireVO();
            BeanUtils.copyProperties(q, vo);

            // 查询创建者信息
            User creator = userMapper.selectById(q.getUserId());
            if (creator != null) {
                vo.setUserId(creator.getId());
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), voList);
    }

    public void auditQuestionnaire(Long id, Integer auditStatus, String reason) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        if (questionnaire.getAuditStatus() != 0) {
            throw new BusinessException("该问卷已审核");
        }

        // 更新审核状态
        questionnaire.setAuditStatus(auditStatus);

        // 审核不通过则自动暂停问卷
        if (auditStatus == 2) {
            questionnaire.setStatus(2); // 暂停
        }

        questionnaireMapper.updateById(questionnaire);

        // TODO: 发送通知给创建者（可通过站内信或邮件）
        log.info("问卷审核完成 - ID: {}, 状态: {}, 原因: {}", id, auditStatus, reason);
    }

    // ==================== 统计数据 ====================

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new java.util.HashMap<>();

        // 总用户数
        Long totalUsers = userMapper.selectCount(null);

        // 今日新增用户
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreateTime, java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        Long todayUsers = userMapper.selectCount(todayUserWrapper);

        // 总问卷数
        Long totalQuestionnaires = questionnaireMapper.selectCount(null);

        // 待审核问卷数
        LambdaQueryWrapper<Questionnaire> auditWrapper = new LambdaQueryWrapper<>();
        auditWrapper.eq(Questionnaire::getStatus, 1).eq(Questionnaire::getAuditStatus, 0);
        Long pendingAudit = questionnaireMapper.selectCount(auditWrapper);

        stats.put("totalUsers", totalUsers);
        stats.put("todayUsers", todayUsers);
        stats.put("totalQuestionnaires", totalQuestionnaires);
        stats.put("pendingAudit", pendingAudit);

        return stats;
    }
}
