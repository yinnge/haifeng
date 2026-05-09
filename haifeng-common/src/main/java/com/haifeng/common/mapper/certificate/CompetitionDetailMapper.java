package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompetitionDetailMapper extends BaseMapper<CompetitionDetail> {

    @Select("SELECT * FROM t_competition_detail WHERE competition_id = #{competitionId}")
    CompetitionDetail findByCompetitionId(@Param("competitionId") Long competitionId);
}
