package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.certificate.CompetitionMajor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface CompetitionMajorMapper extends BaseMapper<CompetitionMajor> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_competition_major WHERE competition_id = #{competitionId} AND major_id = #{majorId})")
    boolean existsByCompetitionIdAndMajorId(@Param("competitionId") Long competitionId, @Param("majorId") Long majorId);

    @Delete("DELETE FROM t_competition_major WHERE competition_id = #{competitionId}")
    int deleteByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 任务2接口3：分页查询某竞赛关联的专业（id + name）
     * 走 idx_cm_competition 索引
     */
    @Select("SELECT cm.major_id AS majorId, cm.major_name AS majorName " +
            "FROM t_competition_major cm " +
            "WHERE cm.competition_id = #{competitionId} " +
            "ORDER BY cm.id ASC")
    IPage<Map<String, Object>> selectMajorsByCompetitionId(
            Page<?> page,
            @Param("competitionId") Long competitionId);

    /**
     * 任务3：分页查询某专业关联的竞赛（id + name）
     * 走 idx_cm_major 索引
     */
    @Select("SELECT cm.competition_id AS competitionId, cm.competition_name AS competitionName " +
            "FROM t_competition_major cm " +
            "WHERE cm.major_id = #{majorId} " +
            "ORDER BY cm.id ASC")
    IPage<Map<String, Object>> selectCompetitionsByMajorId(
            Page<?> page,
            @Param("majorId") Long majorId);
}
