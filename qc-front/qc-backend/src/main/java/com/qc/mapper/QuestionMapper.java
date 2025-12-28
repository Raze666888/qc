package com.qc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
