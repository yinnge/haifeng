package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.config.JsonbTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CompetitionDetailMapper extends BaseMapper<CompetitionDetail> {

    /**
     * 根据 competitionId 查询竞赛详情（含已删除，忽略逻辑删除）。
     * 自定义 @Select 绕过 MyBatis-Plus 全局逻辑删除（MP 内置 selectOne 会自动追加 is_deleted=false，
     * 导致已禁用的详情查不到、启用时无法恢复），@Results 给 8 个 JSONB/数组字段套 typeHandler。
     */
    @Select("SELECT * FROM t_competition_detail WHERE competition_id = #{competitionId} LIMIT 1")
    @Results({
            @Result(column = "basic_info", property = "basicInfo", typeHandler = JsonbTypeHandler.class),
            @Result(column = "awards", property = "awards", typeHandler = StringListTypeHandler.class),
            @Result(column = "purposes", property = "purposes", typeHandler = StringListTypeHandler.class),
            @Result(column = "competition_rules", property = "competitionRules", typeHandler = JsonbTypeHandler.class),
            @Result(column = "scoring_criteria", property = "scoringCriteria", typeHandler = StringListTypeHandler.class),
            @Result(column = "notices", property = "notices", typeHandler = StringListTypeHandler.class),
            @Result(column = "process_guide", property = "processGuide", typeHandler = JsonbTypeHandler.class),
            @Result(column = "awards_display", property = "awardsDisplay", typeHandler = JsonbTypeHandler.class)
    })
    CompetitionDetail findByCompetitionId(@Param("competitionId") Long competitionId);

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

    /**
     * 更新竞赛详情（绕过 MyBatis-Plus 全局逻辑删除，只更新非空字段）。
     * 自定义 @Update 替代 MP 内置 updateById：后者会自动注入 WHERE is_deleted=false，
     * 导致已禁用（is_deleted=true）的详情更新 0 行、改动静默丢失。
     */
    @Update("<script>" +
            "UPDATE t_competition_detail SET " +
            "<if test='detail.basicInfo != null'>basic_info = #{detail.basicInfo, typeHandler=com.haifeng.common.config.JsonbTypeHandler},</if>" +
            "<if test='detail.awards != null'>awards = #{detail.awards, typeHandler=com.haifeng.common.config.StringListTypeHandler},</if>" +
            "<if test='detail.background != null'>background = #{detail.background},</if>" +
            "<if test='detail.purposes != null'>purposes = #{detail.purposes, typeHandler=com.haifeng.common.config.StringListTypeHandler},</if>" +
            "<if test='detail.competitionRules != null'>competition_rules = #{detail.competitionRules, typeHandler=com.haifeng.common.config.JsonbTypeHandler},</if>" +
            "<if test='detail.scoringCriteria != null'>scoring_criteria = #{detail.scoringCriteria, typeHandler=com.haifeng.common.config.StringListTypeHandler},</if>" +
            "<if test='detail.notices != null'>notices = #{detail.notices, typeHandler=com.haifeng.common.config.StringListTypeHandler},</if>" +
            "<if test='detail.processGuide != null'>process_guide = #{detail.processGuide, typeHandler=com.haifeng.common.config.JsonbTypeHandler},</if>" +
            "<if test='detail.awardsDisplay != null'>awards_display = #{detail.awardsDisplay, typeHandler=com.haifeng.common.config.JsonbTypeHandler},</if>" +
            "updated_at = NOW() " +
            "WHERE id = #{detail.id}" +
            "</script>")
    int updateIgnoreLogicDelete(@Param("detail") CompetitionDetail detail);
}
