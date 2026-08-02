package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CompetitionDetailMapper extends BaseMapper<CompetitionDetail> {

    /**
     * 根据 competitionId 查询竞赛详情（含已删除）。
     * 走 MP 内置 selectOne，autoResultMap 才会给 8 个 JSONB/数组字段套 typeHandler。
     */
    default CompetitionDetail findByCompetitionId(Long competitionId) {
        return selectOne(new LambdaQueryWrapper<CompetitionDetail>()
                .eq(CompetitionDetail::getCompetitionId, competitionId)
                .last("LIMIT 1"));
    }

    /**
     * 根据 competitionId 查询未软删除的竞赛详情
     * Service 层任务2接口2 专用
     */
    default CompetitionDetail findActiveByCompetitionId(Long competitionId) {
        return selectOne(new LambdaQueryWrapper<CompetitionDetail>()
                .eq(CompetitionDetail::getCompetitionId, competitionId)
                .eq(CompetitionDetail::getIsDeleted, false)
                .last("LIMIT 1"));
    }

    @Delete("DELETE FROM t_competition_detail WHERE competition_id = #{competitionId}")
    int deleteByCompetitionId(@Param("competitionId") Long competitionId);

    @Update("UPDATE t_competition_detail SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);
}
