package com.qc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.entity.Answer;
import com.qc.entity.AnswerDetail;
import com.qc.entity.Question;
import com.qc.entity.QuestionOption;
import com.qc.entity.Questionnaire;
import com.qc.exception.BusinessException;
import com.qc.mapper.AnswerDetailMapper;
import com.qc.mapper.AnswerMapper;
import com.qc.mapper.QuestionMapper;
import com.qc.mapper.QuestionOptionMapper;
import com.qc.mapper.QuestionnaireMapper;
import com.qc.vo.OptionStatisticsVO;
import com.qc.vo.QuestionStatisticsVO;
import com.qc.vo.StatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private AnswerDetailMapper answerDetailMapper;

    /**
     * 获取问卷统计数据
     */
    public StatisticsVO getQuestionnaireStatistics(Long questionnaireId) {
        // 检查问卷是否存在
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        // 获取答卷总数
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getQuestionnaireId, questionnaireId)
                    .eq(Answer::getDeleted, 0);
        Long totalAnswers = answerMapper.selectCount(answerWrapper);

        // 获取所有问题
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getQuestionnaireId, questionnaireId)
                      .eq(Question::getDeleted, 0)
                      .orderByAsc(Question::getSort);
        List<Question> questions = questionMapper.selectList(questionWrapper);

        // 构建统计数据
        StatisticsVO statisticsVO = new StatisticsVO();
        statisticsVO.setQuestionnaireId(questionnaireId);
        statisticsVO.setTitle(questionnaire.getTitle());
        statisticsVO.setTotalAnswers(totalAnswers.intValue());

        List<QuestionStatisticsVO> questionStatisticsList = new ArrayList<>();

        for (Question question : questions) {
            // 只统计选择题（单选和多选）
            if (question.getType() == 0 || question.getType() == 1) {
                QuestionStatisticsVO questionStatistics = new QuestionStatisticsVO();
                questionStatistics.setQuestionId(question.getId());
                questionStatistics.setContent(question.getContent());
                questionStatistics.setType(question.getType());

                // 获取该问题的所有选项
                LambdaQueryWrapper<QuestionOption> optionWrapper = new LambdaQueryWrapper<>();
                optionWrapper.eq(QuestionOption::getQuestionId, question.getId())
                            .eq(QuestionOption::getDeleted, 0)
                            .orderByAsc(QuestionOption::getSort);
                List<QuestionOption> options = questionOptionMapper.selectList(optionWrapper);

                List<OptionStatisticsVO> optionStatisticsList = new ArrayList<>();

                for (QuestionOption option : options) {
                    // 统计每个选项被选择的次数
                    LambdaQueryWrapper<AnswerDetail> detailWrapper = new LambdaQueryWrapper<>();
                    detailWrapper.eq(AnswerDetail::getQuestionId, question.getId())
                               .eq(AnswerDetail::getOptionId, option.getId())
                               .eq(AnswerDetail::getDeleted, 0);

                    Long count = answerDetailMapper.selectCount(detailWrapper);

                    OptionStatisticsVO optionStatistics = new OptionStatisticsVO();
                    optionStatistics.setOptionId(option.getId());
                    optionStatistics.setContent(option.getContent());
                    optionStatistics.setCount(count.intValue());

                    // 计算百分比
                    double percentage = totalAnswers > 0 ? (count * 100.0 / totalAnswers) : 0;
                    optionStatistics.setPercentage(Math.round(percentage * 100.0) / 100.0); // 保留两位小数

                    optionStatisticsList.add(optionStatistics);
                }

                questionStatistics.setOptions(optionStatisticsList);
                questionStatisticsList.add(questionStatistics);
            }
        }

        statisticsVO.setQuestionStatistics(questionStatisticsList);
        return statisticsVO;
    }
}
