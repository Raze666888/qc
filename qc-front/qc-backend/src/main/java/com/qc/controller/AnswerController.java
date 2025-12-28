package com.qc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.dto.AnswerDTO;
import com.qc.entity.Answer;
import com.qc.entity.AnswerDetail;
import com.qc.entity.Question;
import com.qc.entity.QuestionOption;
import com.qc.entity.Questionnaire;
import com.qc.mapper.AnswerDetailMapper;
import com.qc.mapper.AnswerMapper;
import com.qc.mapper.QuestionMapper;
import com.qc.mapper.QuestionOptionMapper;
import com.qc.mapper.QuestionnaireMapper;
import com.qc.service.AnswerService;
import com.qc.utils.JwtUtil;
import com.qc.vo.PageResult;
import com.qc.vo.QuestionVO;
import com.qc.vo.QuestionnaireVO;
import com.qc.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/answer")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private AnswerDetailMapper answerDetailMapper;

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping("/submit")
    public Result<Long> submit(@RequestBody AnswerDTO answerDTO) {
        Long id = answerService.submitAnswer(answerDTO);
        return Result.success(id);
    }

    @GetMapping("/list/{questionnaireId}")
    public Result<List<Map<String, Object>>> list(@PathVariable Long questionnaireId) {
        List<Map<String, Object>> result = answerService.getAnswerList(questionnaireId);
        return Result.success(result);
    }

    @GetMapping("/statistics/{questionnaireId}")
    public Result<Map<String, Object>> statistics(@PathVariable Long questionnaireId) {
        Map<String, Object> result = answerService.getStatistics(questionnaireId);
        return Result.success(result);
    }

    /**
     * 获取用户参与的问卷列表（重写版）
     * 支持按状态筛选，返回填写时间
     */
    @GetMapping("/my-questionnaires")
    public Result<PageResult<QuestionnaireVO>> getMyQuestionnaires(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        // 查询用户参与的答卷
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getUserId, userId)
                   .orderByDesc(Answer::getCreateTime);

        Page<Answer> page = new Page<>(pageNum, pageSize);
        IPage<Answer> answerPage = answerMapper.selectPage(page, answerWrapper);

        // 获取问卷ID列表
        List<Long> questionnaireIds = answerPage.getRecords().stream()
                .map(Answer::getQuestionnaireId)
                .distinct()
                .collect(Collectors.toList());

        if (questionnaireIds.isEmpty()) {
            return Result.success(new PageResult<>(0L, List.of()));
        }

        // 查询问卷详情
        LambdaQueryWrapper<Questionnaire> questionnaireWrapper = new LambdaQueryWrapper<>();
        questionnaireWrapper.in(Questionnaire::getId, questionnaireIds)
                           .eq(Questionnaire::getDeleted, 0);

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            questionnaireWrapper.like(Questionnaire::getTitle, keyword);
        }

        // 状态筛选（进行中=1, 已结束=2）
        if (status != null) {
            questionnaireWrapper.eq(Questionnaire::getStatus, status);
        }

        List<Questionnaire> questionnaires = questionnaireMapper.selectList(questionnaireWrapper);

        // 创建问卷ID到答卷的映射（用于获取填写时间）
        Map<Long, Answer> questionnaireToAnswer = answerPage.getRecords().stream()
                .collect(Collectors.toMap(Answer::getQuestionnaireId, a -> a, (a1, a2) -> a1));

        // 转换为VO，包含填写时间
        List<QuestionnaireVO> voList = questionnaires.stream().map(q -> {
            QuestionnaireVO vo = new QuestionnaireVO();
            vo.setId(q.getId());
            vo.setTitle(q.getTitle());
            vo.setDescription(q.getDescription());
            vo.setStatus(q.getStatus());
            vo.setDeadline(q.getDeadline());
            vo.setCreateTime(q.getCreateTime());

            // 设置填写时间（从answer表中获取）
            Answer answer = questionnaireToAnswer.get(q.getId());
            if (answer != null) {
                vo.setAnswerTime(answer.getCreateTime());
            }

            return vo;
        }).collect(Collectors.toList());

        // 按填写时间倒序排列
        voList.sort((a, b) -> {
            if (a.getAnswerTime() == null) return 1;
            if (b.getAnswerTime() == null) return -1;
            return b.getAnswerTime().compareTo(a.getAnswerTime());
        });

        return Result.success(new PageResult<>(answerPage.getTotal(), voList));
    }

    /**
     * 获取用户对特定问卷的答案（包含问卷内容和用户的回答）
     */
    @GetMapping("/my-answer/{questionnaireId}")
    public Result<Map<String, Object>> getMyAnswer(
            @PathVariable Long questionnaireId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未授权");
        }

        // 查询用户对该问卷的答卷
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getUserId, userId)
                   .eq(Answer::getQuestionnaireId, questionnaireId)
                   .orderByDesc(Answer::getCreateTime)
                   .last("LIMIT 1");

        Answer answer = answerMapper.selectOne(answerWrapper);
        if (answer == null) {
            return Result.error(404, "未找到答卷记录");
        }

        // 查询问卷详情
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null || questionnaire.getDeleted() == 1) {
            return Result.error(404, "问卷不存在");
        }

        // 查询问卷的所有问题
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getQuestionnaireId, questionnaireId)
                      .eq(Question::getDeleted, 0)
                      .orderByAsc(Question::getSort);
        List<Question> questions = questionMapper.selectList(questionWrapper);

        // 查询所有问题的选项
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<QuestionOption> allOptions = List.of();
        if (!questionIds.isEmpty()) {
            LambdaQueryWrapper<QuestionOption> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.in(QuestionOption::getQuestionId, questionIds)
                        .eq(QuestionOption::getDeleted, 0)
                        .orderByAsc(QuestionOption::getSort);
            allOptions = questionOptionMapper.selectList(optionWrapper);
        }

        // 创建questionId到选项列表的映射
        Map<Long, List<QuestionOption>> questionOptionsMap = new HashMap<>();
        for (QuestionOption option : allOptions) {
            questionOptionsMap.computeIfAbsent(option.getQuestionId(), k -> new ArrayList<>()).add(option);
        }

        // 查询用户的所有答案详情
        LambdaQueryWrapper<AnswerDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(AnswerDetail::getAnswerId, answer.getId());
        List<AnswerDetail> answerDetails = answerDetailMapper.selectList(detailWrapper);

        // 创建questionId到答案详情的映射
        Map<Long, List<AnswerDetail>> questionAnswersMap = new HashMap<>();
        for (AnswerDetail detail : answerDetails) {
            questionAnswersMap.computeIfAbsent(detail.getQuestionId(), k -> new ArrayList<>()).add(detail);
        }

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("questionnaire", questionnaire);
        result.put("answer", answer);

        // 构建问题列表，包含用户的答案
        List<Map<String, Object>> questionsWithAnswers = new ArrayList<>();
        for (Question question : questions) {
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("id", question.getId());
            questionMap.put("type", question.getType());
            questionMap.put("content", question.getContent());
            questionMap.put("sort", question.getSort());
            questionMap.put("isRequired", question.getIsRequired());

            // 添加选项
            List<QuestionOption> options = questionOptionsMap.getOrDefault(question.getId(), new ArrayList<>());
            questionMap.put("options", options);

            // 获取用户对该问题的答案
            List<AnswerDetail> userAnswers = questionAnswersMap.getOrDefault(question.getId(), new ArrayList<>());

            if (question.getType() == 0 || question.getType() == 1) {
                // 单选题或多选题
                List<Long> selectedOptionIds = userAnswers.stream()
                    .map(AnswerDetail::getOptionId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                questionMap.put("selectedOptions", selectedOptionIds);
            } else {
                // 文本题
                List<String> textAnswers = userAnswers.stream()
                    .map(AnswerDetail::getTextContent)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                questionMap.put("textAnswers", textAnswers);
            }

            questionsWithAnswers.add(questionMap);
        }

        result.put("questions", questionsWithAnswers);
        return Result.success(result);
    }
}
