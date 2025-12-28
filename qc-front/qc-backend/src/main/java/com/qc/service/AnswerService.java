package com.qc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.dto.AnswerDTO;
import com.qc.dto.AnswerDetailDTO;
import com.qc.entity.*;
import com.qc.exception.BusinessException;
import com.qc.mapper.*;
import com.qc.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnswerService {

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

    @Transactional(rollbackFor = Exception.class)
    public Long submitAnswer(AnswerDTO answerDTO) {
        // 检查问卷是否存在
        Questionnaire questionnaire = questionnaireMapper.selectById(answerDTO.getQuestionnaireId());
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        if (questionnaire.getStatus() != 1) {
            throw new BusinessException("问卷未发布或已暂停");
        }

        // 检查是否截止
        if (questionnaire.getDeadline() != null && questionnaire.getDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("问卷已截止");
        }

        // 检查是否已填写过问卷
        // 优先使用userId检查（针对已登录用户）
        if (answerDTO.getUserId() != null) {
            LambdaQueryWrapper<Answer> userCheckWrapper = new LambdaQueryWrapper<>();
            userCheckWrapper.eq(Answer::getQuestionnaireId, answerDTO.getQuestionnaireId())
                          .eq(Answer::getUserId, answerDTO.getUserId())
                          .eq(Answer::getDeleted, 0);
            if (answerMapper.selectCount(userCheckWrapper) > 0) {
                throw new BusinessException("您已经填写过该问卷");
            }
        } else {
            // 未登录用户，使用IP地址检查（简单限制）
            LambdaQueryWrapper<Answer> ipCheckWrapper = new LambdaQueryWrapper<>();
            ipCheckWrapper.eq(Answer::getQuestionnaireId, answerDTO.getQuestionnaireId())
                         .eq(Answer::getRespondentIp, answerDTO.getRespondentIp())
                         .eq(Answer::getDeleted, 0);
            if (answerMapper.selectCount(ipCheckWrapper) > 0) {
                throw new BusinessException("您已经填写过该问卷");
            }
        }

        // 创建答卷
        Answer answer = new Answer();
        BeanUtils.copyProperties(answerDTO, answer);
        answerMapper.insert(answer);

        // 保存答题详情
        if (!CollectionUtils.isEmpty(answerDTO.getDetails())) {
            for (AnswerDetailDTO detailDTO : answerDTO.getDetails()) {
                AnswerDetail detail = new AnswerDetail();
                detail.setAnswerId(answer.getId());
                detail.setQuestionId(detailDTO.getQuestionId());

                if (detailDTO.getOptionId() != null) {
                    detail.setOptionId(detailDTO.getOptionId());
                }
                if (detailDTO.getTextContent() != null) {
                    detail.setTextContent(detailDTO.getTextContent());
                }

                answerDetailMapper.insert(detail);
            }
        }

        return answer.getId();
    }

    public List<Map<String, Object>> getAnswerList(Long questionnaireId) {
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Answer::getQuestionnaireId, questionnaireId)
               .orderByDesc(Answer::getCreateTime);
        List<Answer> answers = answerMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Answer answer : answers) {
            Map<String, Object> answerMap = new HashMap<>();
            answerMap.put("id", answer.getId());
            answerMap.put("respondentName", answer.getRespondentName());
            answerMap.put("respondentPhone", answer.getRespondentPhone());
            answerMap.put("createTime", answer.getCreateTime());

            // 查询答题详情
            LambdaQueryWrapper<AnswerDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(AnswerDetail::getAnswerId, answer.getId());
            List<AnswerDetail> details = answerDetailMapper.selectList(detailWrapper);

            List<Map<String, Object>> detailList = new ArrayList<>();
            for (AnswerDetail detail : details) {
                Map<String, Object> detailMap = new HashMap<>();
                detailMap.put("questionId", detail.getQuestionId());

                // 查询问题内容
                Question question = questionMapper.selectById(detail.getQuestionId());
                if (question != null) {
                    detailMap.put("questionContent", question.getContent());
                    detailMap.put("questionType", question.getType());

                    if (question.getType() == 2) {
                        // 文本题
                        detailMap.put("answer", detail.getTextContent());
                    } else {
                        // 选择题，查询选项内容
                        if (detail.getOptionId() != null) {
                            QuestionOption option = questionOptionMapper.selectById(detail.getOptionId());
                            if (option != null) {
                                detailMap.put("answer", option.getContent());
                            }
                        }
                    }
                }
                detailList.add(detailMap);
            }

            answerMap.put("details", detailList);
            result.add(answerMap);
        }

        return result;
    }

    public Map<String, Object> getStatistics(Long questionnaireId) {
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        // 查询所有问题
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getQuestionnaireId, questionnaireId)
                      .orderByAsc(Question::getSort);
        List<Question> questions = questionMapper.selectList(questionWrapper);

        // 总答卷数
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getQuestionnaireId, questionnaireId);
        long totalAnswers = answerMapper.selectCount(answerWrapper);

        List<Map<String, Object>> questionStats = new ArrayList<>();

        for (Question question : questions) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("questionId", question.getId());
            stat.put("content", question.getContent());
            stat.put("type", question.getType());

            if (question.getType() == 2) {
                // 文本题统计
                LambdaQueryWrapper<AnswerDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.eq(AnswerDetail::getQuestionId, question.getId())
                            .isNotNull(AnswerDetail::getTextContent);
                List<AnswerDetail> textDetails = answerDetailMapper.selectList(detailWrapper);

                List<String> textAnswers = textDetails.stream()
                        .map(AnswerDetail::getTextContent)
                        .collect(Collectors.toList());
                stat.put("textAnswers", textAnswers);
            } else {
                // 选择题统计
                LambdaQueryWrapper<QuestionOption> optionWrapper = new LambdaQueryWrapper<>();
                optionWrapper.eq(QuestionOption::getQuestionId, question.getId())
                            .orderByAsc(QuestionOption::getSort);
                List<QuestionOption> options = questionOptionMapper.selectList(optionWrapper);

                List<Map<String, Object>> optionStats = new ArrayList<>();
                for (QuestionOption option : options) {
                    // 统计该选项被选择的次数
                    LambdaQueryWrapper<AnswerDetail> detailWrapper = new LambdaQueryWrapper<>();
                    detailWrapper.eq(AnswerDetail::getQuestionId, question.getId())
                                .eq(AnswerDetail::getOptionId, option.getId());
                    long selectCount = answerDetailMapper.selectCount(detailWrapper);

                    Map<String, Object> optionStat = new HashMap<>();
                    optionStat.put("optionId", option.getId());
                    optionStat.put("optionContent", option.getContent());  // 前端期望的字段名
                    optionStat.put("count", selectCount);  // 前端期望的字段名

                    if (totalAnswers > 0) {
                        double percentage = (selectCount * 100.0) / totalAnswers;
                        optionStat.put("percentage", Math.round(percentage));  // 前端期望整数百分比
                    } else {
                        optionStat.put("percentage", 0);
                    }

                    optionStats.add(optionStat);
                }

                stat.put("optionStats", optionStats);  // 前端期望的字段名
            }

            questionStats.add(stat);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalAnswers", totalAnswers);
        result.put("questionStats", questionStats);  // 前端期望的字段名

        return result;
    }
}
