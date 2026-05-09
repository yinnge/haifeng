package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.SubjectReqDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubjectReqDictMapper extends BaseMapper<SubjectReqDict> {

    @Select("SELECT requirement_level FROM t_subject_req_dict WHERE code = #{code} LIMIT 1")
    Short selectLevelByCode(@Param("code") String code);
}
