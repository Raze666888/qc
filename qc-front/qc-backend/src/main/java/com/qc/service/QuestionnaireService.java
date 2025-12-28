package com.qc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.dto.*;
import com.qc.entity.*;
import com.qc.exception.BusinessException;
import com.qc.mapper.*;
import com.qc.vo.OptionVO;
import com.qc.vo.PageResult;
import com.qc.vo.QuestionVO;
import com.qc.vo.QuestionnaireVO;
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
public class QuestionnaireService {

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    private static final Map<Integer, String> CATEGORY_MAP = new HashMap<Integer, String>() {{
        put(1, "学术科研");
        put(2, "生活消费");
        put(3, "心理健康");
        put(4, "娱乐游戏");
        put(5, "工作就业");
    }};

    private static final Map<Integer, String> QUESTION_TYPE_MAP = new HashMap<Integer, String>() {{
        put(0, "单选题");
        put(1, "多选题");
        put(2, "文本题");
    }};

    @Transactional(rollbackFor = Exception.class)
    public Long createWithQuestions(Long userId, Map<String, Object> params) {
        try {
            log.info("开始创建问卷, userId: {}, params: {}", userId, params);

            Map<String, Object> questionnaireData = (Map<String, Object>) params.get("questionnaireDTO");
            List<Map<String, Object>> questionList = (List<Map<String, Object>>) params.get("questionList");

            log.info("问卷基本信息: {}", questionnaireData);
            log.info("问题列表数量: {}", questionList != null ? questionList.size() : 0);

            Questionnaire questionnaire = new Questionnaire();
            questionnaire.setUserId(userId);
            questionnaire.setTitle(questionnaireData.get("title") != null ? questionnaireData.get("title").toString() : "");
            questionnaire.setDescription(questionnaireData.get("description") != null ? questionnaireData.get("description").toString() : null);
            questionnaire.setCategory(questionnaireData.get("category") != null ? Integer.valueOf(questionnaireData.get("category").toString()) : null);
            // 处理 deadline 字段
            if (questionnaireData.get("deadline") != null && !questionnaireData.get("deadline").toString().isEmpty()) {
                try {
                    questionnaire.setDeadline(java.time.LocalDateTime.parse(questionnaireData.get("deadline").toString()));
                } catch (Exception e) {
                    log.warn("日期解析失败: {}", questionnaireData.get("deadline"));
                }
            }
            questionnaire.setIsAnonymous(questionnaireData.get("isAnonymous") != null ? Integer.valueOf(questionnaireData.get("isAnonymous").toString()) : 0);
            questionnaire.setStatus(0); // 草稿
            questionnaire.setAuditStatus(0); // 待审核

            log.info("准备插入问卷: title={}, userId={}", questionnaire.getTitle(), questionnaire.getUserId());
            questionnaireMapper.insert(questionnaire);
            log.info("问卷插入成功, ID: {}", questionnaire.getId());

            // 保存问题和选项
            if (!CollectionUtils.isEmpty(questionList)) {
                log.info("开始保存问题和选项, 问题数量: {}", questionList.size());
                saveQuestions(questionnaire.getId(), questionList);
            }

            return questionnaire.getId();
        } catch (Exception e) {
            log.error("创建问卷失败", e);
            throw new BusinessException("创建问卷失败: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateWithQuestions(Long userId, Long id, Map<String, Object> params) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        if (!questionnaire.getUserId().equals(userId)) {
            throw new BusinessException("无权限修改此问卷");
        }

        if (questionnaire.getStatus() == 1) {
            throw new BusinessException("已发布的问卷不能修改");
        }

        Map<String, Object> questionnaireData = (Map<String, Object>) params.get("questionnaireDTO");
        List<Map<String, Object>> questionList = (List<Map<String, Object>>) params.get("questionList");

        // 直接设置属性,避免 BeanUtils 的问题
        if (questionnaireData.get("title") != null) {
            questionnaire.setTitle(questionnaireData.get("title").toString());
        }
        if (questionnaireData.get("description") != null) {
            questionnaire.setDescription(questionnaireData.get("description").toString());
        }
        if (questionnaireData.get("category") != null) {
            questionnaire.setCategory(Integer.valueOf(questionnaireData.get("category").toString()));
        }
        if (questionnaireData.get("deadline") != null && !questionnaireData.get("deadline").toString().isEmpty()) {
            try {
                questionnaire.setDeadline(java.time.LocalDateTime.parse(questionnaireData.get("deadline").toString()));
            } catch (Exception e) {
                log.warn("日期解析失败: {}", questionnaireData.get("deadline"));
            }
        } else {
            questionnaire.setDeadline(null);
        }
        if (questionnaireData.get("isAnonymous") != null) {
            questionnaire.setIsAnonymous(Integer.valueOf(questionnaireData.get("isAnonymous").toString()));
        }

        questionnaireMapper.updateById(questionnaire);

        // 删除旧问题和选项
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getQuestionnaireId, id);
        List<Question> oldQuestions = questionMapper.selectList(questionWrapper);
        oldQuestions.forEach(q -> {
            LambdaQueryWrapper<QuestionOption> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(QuestionOption::getQuestionId, q.getId());
            questionOptionMapper.delete(optionWrapper);
        });
        questionMapper.delete(questionWrapper);

        // 保存新问题和选项
        if (!CollectionUtils.isEmpty(questionList)) {
            saveQuestions(id, questionList);
        }
    }

    private void saveQuestions(Long questionnaireId, List<Map<String, Object>> questionList) {
        try {
            log.info("开始保存问题, questionnaireId: {}, 问题数量: {}", questionnaireId, questionList.size());

            for (int i = 0; i < questionList.size(); i++) {
                Map<String, Object> questionData = questionList.get(i);
                Map<String, Object> questionDTO = (Map<String, Object>) questionData.get("questionDTO");
                List<Map<String, Object>> optionDTOs = (List<Map<String, Object>>) questionData.get("optionDTOs");

                log.info("处理第 {} 个问题, questionDTO: {}", i + 1, questionDTO);

                // 创建问题对象
                Question question = new Question();
                question.setQuestionnaireId(questionnaireId);

                // 从 Map 中提取属性
                if (questionDTO.get("content") != null) {
                    question.setContent(questionDTO.get("content").toString());
                } else {
                    question.setContent(""); // 默认值
                }

                if (questionDTO.get("type") != null) {
                    question.setType(Integer.valueOf(questionDTO.get("type").toString()));
                } else {
                    question.setType(0); // 默认单选
                }

                if (questionDTO.get("isRequired") != null) {
                    question.setIsRequired(Integer.valueOf(questionDTO.get("isRequired").toString()));
                } else {
                    question.setIsRequired(0); // 默认非必填
                }

                if (questionDTO.get("sort") != null) {
                    question.setSort(Integer.valueOf(questionDTO.get("sort").toString()));
                } else {
                    question.setSort(i + 1); // 默认排序
                }

                // 处理多选题的最大可选数量
                if (questionDTO.get("maxSelectNum") != null) {
                    question.setMaxSelectNum(Integer.valueOf(questionDTO.get("maxSelectNum").toString()));
                }

                // 处理文本题的输入框类型
                if (questionDTO.get("inputType") != null) {
                    question.setInputType(Integer.valueOf(questionDTO.get("inputType").toString()));
                }

                log.info("准备插入问题: content={}, type={}", question.getContent(), question.getType());
                questionMapper.insert(question);
                log.info("问题插入成功, ID: {}", question.getId());

                // 保存选项
                if (!CollectionUtils.isEmpty(optionDTOs)) {
                    log.info("开始保存选项, 选项数量: {}", optionDTOs.size());
                    for (Map<String, Object> optionData : optionDTOs) {
                        QuestionOption option = new QuestionOption();
                        option.setQuestionId(question.getId());

                        if (optionData.get("content") != null) {
                            option.setContent(optionData.get("content").toString());
                        }

                        if (optionData.get("sort") != null) {
                            option.setSort(Integer.valueOf(optionData.get("sort").toString()));
                        }

                        // 处理跳转问题ID
                        if (optionData.get("jumpQuestionId") != null) {
                            Object jumpId = optionData.get("jumpQuestionId");
                            if (jumpId != null && !jumpId.toString().equals("null") && !jumpId.toString().isEmpty()) {
                                option.setJumpQuestionId(Long.valueOf(jumpId.toString()));
                            }
                        }

                        questionOptionMapper.insert(option);
                    }
                }
            }

            log.info("所有问题保存完成");
        } catch (Exception e) {
            log.error("保存问题失败", e);
            throw new BusinessException("保存问题失败: " + e.getMessage());
        }
    }

    public void publishQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        if (questionnaire.getStatus() == 1) {
            throw new BusinessException("问卷已发布");
        }

        // 检查是否有问题
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getQuestionnaireId, id);
        long count = questionMapper.selectCount(wrapper);
        if (count == 0) {
            throw new BusinessException("请至少添加一个问题再发布");
        }

        questionnaire.setStatus(1);
        questionnaire.setLink(generateLink(id));
        questionnaireMapper.updateById(questionnaire);
    }

    private String generateLink(Long id) {
        return "http://localhost:8080/fill/" + id;
    }

    public void updateStatus(Long id, Integer status) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }
        questionnaire.setStatus(status);
        questionnaireMapper.updateById(questionnaire);
    }

    public void deleteQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        if (questionnaire.getStatus() == 1) {
            throw new BusinessException("已发布的问卷不能删除，请先暂停");
        }

        questionnaireMapper.deleteById(id);
    }

    public PageResult<QuestionnaireVO> getQuestionnairePage(Integer pageNum, Integer pageSize, Integer status, Long userId) {
        Page<QuestionnaireVO> page = new Page<>(pageNum, pageSize);
        IPage<QuestionnaireVO> result = questionnaireMapper.selectQuestionnairePage(page, userId, status);

        List<QuestionnaireVO> records = result.getRecords().stream().map(qvo -> {
            qvo.setCategoryName(CATEGORY_MAP.get(qvo.getCategory()));
            return qvo;
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), records);
    }

    public QuestionnaireVO getQuestionnaireDetail(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BusinessException("问卷不存在");
        }

        QuestionnaireVO vo = new QuestionnaireVO();
        BeanUtils.copyProperties(questionnaire, vo);
        vo.setCategoryName(CATEGORY_MAP.get(questionnaire.getCategory()));

        // 查询问题列表
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getQuestionnaireId, id)
                       .orderByAsc(Question::getSort);
        List<Question> questions = questionMapper.selectList(questionWrapper);

        List<QuestionVO> questionVOList = new ArrayList<>();
        for (Question question : questions) {
            QuestionVO questionVO = new QuestionVO();
            BeanUtils.copyProperties(question, questionVO);
            questionVO.setTypeName(QUESTION_TYPE_MAP.get(question.getType()));

            // 查询选项列表
            LambdaQueryWrapper<QuestionOption> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(QuestionOption::getQuestionId, question.getId())
                        .orderByAsc(QuestionOption::getSort);
            List<QuestionOption> options = questionOptionMapper.selectList(optionWrapper);

            List<OptionVO> optionVOList = options.stream().map(opt -> {
                OptionVO optionVO = new OptionVO();
                BeanUtils.copyProperties(opt, optionVO);
                return optionVO;
            }).collect(Collectors.toList());

            questionVO.setOptions(optionVOList);
            questionVOList.add(questionVO);
        }

        vo.setQuestions(questionVOList);
        return vo;
    }
}
