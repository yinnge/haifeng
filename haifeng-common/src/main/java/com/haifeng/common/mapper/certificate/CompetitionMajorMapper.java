package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.CompetitionMajor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompetitionMajorMapper extends BaseMapper<CompetitionMajor> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_competition_major WHERE competition_id = #{competitionId} AND major_id = #{majorId})")
    boolean existsByCompetitionIdAndMajorId(@Param("competitionId") Long competitionId, @Param("majorId") Long majorId);

    @Delete("DELETE FROM t_competition_major WHERE competition_id = #{competitionId}")
    int deleteByCompetitionId(@Param("competitionId") Long competitionId);
}
