package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.config.JsonbTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdmissionMajorScoreMapper extends BaseMapper<AdmissionMajorScore> {

    @Update("UPDATE t_admission_major_score SET is_deleted = #{isDeleted} WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Integer id, @Param("isDeleted") Boolean isDeleted);

    @Delete("DELETE FROM t_admission_major_score WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Integer id);

    @Delete("<script>DELETE FROM t_admission_major_score WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int physicalDeleteBatchIds(@Param("ids") List<Integer> ids);

    /**
     * 自定义全量更新（绕过 MP 全局逻辑删除过滤器）。
     */
    @Update("UPDATE t_admission_major_score SET " +
            "group_id = #{groupId}, major_id = #{majorId}, " +
            "major_code = #{majorCode}, major_name = #{majorName}, " +
            "education_level = #{educationLevel}, duration = #{duration}, " +
            "tuition = #{tuition}, description = #{description}, " +
            "history = #{history,typeHandler=com.haifeng.common.config.JsonbTypeHandler}, " +
            "constraints = #{constraints,typeHandler=com.haifeng.common.config.StringListTypeHandler}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateByIdCustom(AdmissionMajorScore entity);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_admission_major_score " +
            "WHERE group_id = #{groupId} AND major_code = #{majorCode} " +
            "AND is_deleted = FALSE " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId}" +
            "</if>" +
            "</script>")
    int countByGroupIdAndMajorCode(
            @Param("groupId") Integer groupId,
            @Param("majorCode") String majorCode,
            @Param("excludeId") Integer excludeId);

    /**
     * 分页查询（含已禁用）。
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_major_score " +
            "<where>" +
            "<if test='params.isDeleted != null'>AND is_deleted = #{params.isDeleted}</if>" +
            "<if test='params.groupId != null'>AND group_id = #{params.groupId}</if>" +
            "<if test='params.majorCode != null and params.majorCode != \"\"'>AND major_code LIKE CONCAT('%', #{params.majorCode}, '%')</if>" +
            "<if test='params.majorName != null and params.majorName != \"\"'>AND major_name LIKE CONCAT('%', #{params.majorName}, '%')</if>" +
            "<if test='params.educationLevel != null and params.educationLevel != \"\"'>AND education_level = #{params.educationLevel}</if>" +
            "</where>" +
            "ORDER BY major_code ASC, id ASC" +
            "</script>")
    @Results({
            @Result(column = "constraints", property = "constraints", typeHandler = StringListTypeHandler.class),
            @Result(column = "history", property = "history", typeHandler = JsonbTypeHandler.class)
    })
    IPage<AdmissionMajorScore> selectPageCustom(Page<?> page, @Param("params") Map<String, Object> params);

    @Select("SELECT * FROM t_admission_major_score WHERE id = #{id}")
    @Results({
            @Result(column = "constraints", property = "constraints", typeHandler = StringListTypeHandler.class),
            @Result(column = "history", property = "history", typeHandler = JsonbTypeHandler.class)
    })
    AdmissionMajorScore selectByIdCustom(@Param("id") Integer id);

    /**
     * 向 history jsonb 数组追加一条记录（不覆盖已有年份）
     */
    @Update("<script>" +
            "UPDATE t_admission_major_score SET " +
            "history = CASE " +
            "  WHEN history IS NULL OR history = '[]'::jsonb THEN " +
            "    #{newEntry,typeHandler=com.haifeng.common.config.JsonbTypeHandler} " +
            "  WHEN EXISTS (SELECT 1 FROM jsonb_array_elements(history) e WHERE (e->>'year')::int = #{year}) THEN " +
            "    history " +
            "  ELSE " +
            "    history || #{newEntry,typeHandler=com.haifeng.common.config.JsonbTypeHandler} " +
            "END, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}" +
            "</script>")
    int appendHistory(@Param("id") Integer id,
                      @Param("year") Integer year,
                      @Param("newEntry") Object newEntry);

    /**
     * 设置 history jsonb（全量覆盖）
     */
    @Update("UPDATE t_admission_major_score SET history = #{history,typeHandler=com.haifeng.common.config.JsonbTypeHandler}, updated_at = NOW() WHERE id = #{id}")
    int setHistory(@Param("id") Integer id,
                   @Param("history") Object history);

    /**
     * 按专业代码 + 专业组ID 批量查询（不受数据库主键变化影响）
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_major_score " +
            "WHERE group_id = #{groupId} AND is_deleted = FALSE " +
            "AND major_code IN " +
            "<foreach collection='majorCodes' item='code' open='(' separator=',' close=')'>#{code}</foreach>" +
            "</script>")
    @Results({
            @Result(column = "constraints", property = "constraints", typeHandler = StringListTypeHandler.class),
            @Result(column = "history", property = "history", typeHandler = JsonbTypeHandler.class)
    })
    List<AdmissionMajorScore> selectByGroupIdAndMajorCodes(
            @Param("groupId") Integer groupId,
            @Param("majorCodes") List<String> majorCodes);
}
