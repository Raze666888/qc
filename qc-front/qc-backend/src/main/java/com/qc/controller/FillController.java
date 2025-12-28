package com.qc.controller;

import com.qc.dto.AnswerDTO;
import com.qc.entity.Questionnaire;
import com.qc.mapper.QuestionMapper;
import com.qc.mapper.QuestionOptionMapper;
import com.qc.mapper.QuestionnaireMapper;
import com.qc.service.AnswerService;
import com.qc.vo.OptionVO;
import com.qc.vo.QuestionVO;
import com.qc.vo.QuestionnaireVO;
import com.qc.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fill")
public class FillController {

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private AnswerService answerService;

    @GetMapping("/{id}")
    public Result<QuestionnaireVO> getFillQuestionnaire(@PathVariable Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            return Result.error("问卷不存在");
        }

        if (questionnaire.getStatus() != 1) {
            return Result.error("问卷未发布或已暂停");
        }

        QuestionnaireVO vo = new QuestionnaireVO();
        vo.setId(questionnaire.getId());
        vo.setTitle(questionnaire.getTitle());
        vo.setDescription(questionnaire.getDescription());
        vo.setIsAnonymous(questionnaire.getIsAnonymous());

        // 查询问题列表
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.qc.entity.Question> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.qc.entity.Question::getQuestionnaireId, id)
               .orderByAsc(com.qc.entity.Question::getSort);
        List<com.qc.entity.Question> questions = questionMapper.selectList(wrapper);

        List<QuestionVO> questionVOList = new java.util.ArrayList<>();
        for (com.qc.entity.Question question : questions) {
            QuestionVO questionVO = new QuestionVO();
            questionVO.setId(question.getId());
            questionVO.setContent(question.getContent());
            questionVO.setType(question.getType());
            questionVO.setIsRequired(question.getIsRequired());
            questionVO.setInputType(question.getInputType());

            // 查询选项列表
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.qc.entity.QuestionOption> optionWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            optionWrapper.eq(com.qc.entity.QuestionOption::getQuestionId, question.getId())
                        .orderByAsc(com.qc.entity.QuestionOption::getSort);
            List<com.qc.entity.QuestionOption> options = questionOptionMapper.selectList(optionWrapper);

            List<OptionVO> optionVOList = new java.util.ArrayList<>();
            for (com.qc.entity.QuestionOption option : options) {
                OptionVO optionVO = new OptionVO();
                optionVO.setId(option.getId());
                optionVO.setContent(option.getContent());
                optionVOList.add(optionVO);
            }

            questionVO.setOptions(optionVOList);
            questionVOList.add(questionVO);
        }

        vo.setQuestions(questionVOList);
        return Result.success(vo);
    }

    @PostMapping("/submit")
    public Result<Long> submitAnswer(HttpServletRequest request, @RequestBody AnswerDTO answerDTO) {
        // 获取客户端IP
        String ip = getClientIp(request);
        answerDTO.setRespondentIp(ip);

        Long id = answerService.submitAnswer(answerDTO);
        return Result.success(id);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
