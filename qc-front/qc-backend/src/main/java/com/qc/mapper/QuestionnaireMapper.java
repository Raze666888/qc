package com.qc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.entity.Questionnaire;
import com.qc.vo.QuestionnaireVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {

    IPage<QuestionnaireVO> selectQuestionnairePage(Page<?> page, @Param("userId") Long userId, @Param("status") Integer status);
}
