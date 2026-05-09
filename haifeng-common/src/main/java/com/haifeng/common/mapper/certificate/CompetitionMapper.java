package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_competition WHERE comp_name = #{compName} AND is_deleted = FALSE)")
    boolean existsByCompName(@Param("compName") String compName);

    @Select("SELECT * FROM t_competition WHERE comp_name = #{compName} AND is_deleted = FALSE LIMIT 1")
    Competition findByCompName(@Param("compName") String compName);
}
