package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CompetitionDetailMapper extends BaseMapper<CompetitionDetail> {

    @Select("SELECT * FROM t_competition_detail WHERE competition_id = #{competitionId}")
    CompetitionDetail findByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据 competitionId 查询未软删除的竞赛详情
     * Service 层任务2接口2 专用
     */
    @Select("SELECT * FROM t_competition_detail " +
            "WHERE competition_id = #{competitionId} AND is_deleted = FALSE")
    CompetitionDetail findActiveByCompetitionId(@Param("competitionId") Long competitionId);

    @Delete("DELETE FROM t_competition_detail WHERE competition_id = #{competitionId}")
    int deleteByCompetitionId(@Param("competitionId") Long competitionId);

    @Update("UPDATE t_competition_detail SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);
}
