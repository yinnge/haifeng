package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.certificate.CompetitionMajor;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CompetitionMajorMapper extends BaseMapper<CompetitionMajor> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_competition_major WHERE competition_id = #{competitionId} AND major_id = #{majorId} AND is_deleted = FALSE)")
    boolean existsByCompetitionIdAndMajorId(@Param("competitionId") Long competitionId, @Param("majorId") Long majorId);

    @Update("UPDATE t_competition_major SET is_deleted = TRUE WHERE competition_id = #{competitionId} AND is_deleted = FALSE")
    int softDeleteByCompetitionId(@Param("competitionId") Long competitionId);

    @Update("UPDATE t_competition_major SET is_deleted = FALSE WHERE competition_id = #{competitionId} AND is_deleted = TRUE")
    int enableByCompetitionId(@Param("competitionId") Long competitionId);

    @Delete("DELETE FROM t_competition_major WHERE competition_id = #{competitionId}")
    int deleteByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 分页查询竞赛-专业关联（绕过 MyBatis-Plus 逻辑删除）
     */
    @Select("<script>" +
            "SELECT * FROM t_competition_major" +
            "<where>" +
            "<if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>" +
            "<if test='competitionName != null and competitionName != \"\"'>AND competition_name LIKE CONCAT('%', #{competitionName}, '%')</if>" +
            "<if test='majorName != null and majorName != \"\"'>AND major_name LIKE CONCAT('%', #{majorName}, '%')</if>" +
            "<if test='competitionId != null'>AND competition_id = #{competitionId}</if>" +
            "<if test='majorId != null'>AND major_id = #{majorId}</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<CompetitionMajor> selectPageIgnoreLogicDelete(Page<CompetitionMajor> page,
                                                        @Param("isDeleted") Boolean isDeleted,
                                                        @Param("competitionName") String competitionName,
                                                        @Param("majorName") String majorName,
                                                        @Param("competitionId") Long competitionId,
                                                        @Param("majorId") Long majorId);

    @Select("SELECT * FROM t_competition_major WHERE id = #{id}")
    CompetitionMajor findByIdIgnoreLogicDelete(@Param("id") Long id);

    @Update("UPDATE t_competition_major SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);

    @Delete("DELETE FROM t_competition_major WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("<script>" +
            "DELETE FROM t_competition_major WHERE id IN" +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int physicalDeleteBatchByIds(@Param("ids") List<Long> ids);

    /**
     * 任务2接口3：分页查询某竞赛关联的专业（id + name）
     * 走 idx_cm_competition 索引
     */
    @Select("SELECT cm.major_id AS \"majorId\", cm.major_name AS \"majorName\" " +
            "FROM t_competition_major cm " +
            "WHERE cm.competition_id = #{competitionId} " +
            "ORDER BY cm.id ASC NULLS LAST")
    IPage<Map<String, Object>> selectMajorsByCompetitionId(
            Page<?> page,
            @Param("competitionId") Long competitionId);

    /**
     * 任务3：分页查询某专业关联的竞赛（id + name）
     * 走 idx_cm_major 索引
     */
    @Select("SELECT cm.competition_id AS \"competitionId\", cm.competition_name AS \"competitionName\" " +
            "FROM t_competition_major cm " +
            "WHERE cm.major_id = #{majorId} " +
            "ORDER BY cm.id ASC NULLS LAST")
    IPage<Map<String, Object>> selectCompetitionsByMajorId(
            Page<?> page,
            @Param("majorId") Long majorId);
}
