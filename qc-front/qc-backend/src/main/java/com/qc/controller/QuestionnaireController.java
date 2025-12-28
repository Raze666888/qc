package com.qc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.service.QuestionnaireService;
import com.qc.utils.JwtUtil;
import com.qc.vo.PageResult;
import com.qc.vo.QuestionnaireVO;
import com.qc.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping("/create-with-questions")
    public Result<Long> createWithQuestions(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        Long id = questionnaireService.createWithQuestions(userId, params);
        return Result.success(id);
    }

    @PutMapping("/update-with-questions/{id}")
    public Result<Void> updateWithQuestions(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        questionnaireService.updateWithQuestions(userId, id, params);
        return Result.success();
    }

    @PostMapping("/publish")
    public Result<Void> publish(HttpServletRequest request, @RequestBody Map<String, Long> params) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        Long id = params.get("id");
        questionnaireService.publishQuestionnaire(id);
        return Result.success();
    }

    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        Long id = ((Integer) params.get("id")).longValue();
        Integer status = (Integer) params.get("status");
        questionnaireService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> delete(HttpServletRequest request, @RequestBody Map<String, Long> params) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        Long id = params.get("id");
        questionnaireService.deleteQuestionnaire(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<QuestionnaireVO>> page(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        PageResult<QuestionnaireVO> result = questionnaireService.getQuestionnairePage(pageNum, pageSize, status, userId);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<QuestionnaireVO> detail(@PathVariable Long id) {
        QuestionnaireVO vo = questionnaireService.getQuestionnaireDetail(id);
        return Result.success(vo);
    }
}
