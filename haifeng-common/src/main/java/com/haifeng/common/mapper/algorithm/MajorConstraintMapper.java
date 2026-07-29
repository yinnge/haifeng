package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.algorithm.MajorConstraint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface MajorConstraintMapper extends BaseMapper<MajorConstraint> {
    @Select("SELECT COUNT(*) FROM t_major_constraint WHERE major_code = #{majorCode} AND constraint_code = #{constraintCode} AND is_deleted = FALSE")
    int countByBusinessKey(@Param("majorCode") String majorCode, @Param("constraintCode") String constraintCode);

    @Select("SELECT * FROM t_major_constraint WHERE major_code = #{majorCode} AND constraint_code = #{constraintCode} AND is_deleted = TRUE LIMIT 1")
    MajorConstraint selectDeletedByBusinessKey(@Param("majorCode") String majorCode, @Param("constraintCode") String constraintCode);

    @Select("<script>" +
            "SELECT CONCAT(major_code, '_', constraint_code) AS business_key " +
            "FROM t_major_constraint " +
            "WHERE is_deleted = FALSE AND " +
            "<foreach collection='keys' item='k' open='(' separator=') OR (' close=')'>" +
            "major_code = #{k.majorCode} AND constraint_code = #{k.constraintCode}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistingKeys(@Param("keys") List<Map<String, Object>> keys);

    @Select({"<script>",
            "SELECT * FROM t_major_constraint",
            "WHERE is_deleted = TRUE AND (",
            "<foreach collection='keys' item='k' separator=' OR '>",
            "(major_code = #{k.majorCode} AND constraint_code = #{k.constraintCode})",
            "</foreach>",
            ")</script>"})
    List<MajorConstraint> selectDeletedByKeys(@Param("keys") List<Map<String, Object>> keys);

    @Update("<script>" +
            "UPDATE t_major_constraint SET is_deleted = TRUE WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT * FROM t_major_constraint " +
            "<where>" +
            "<if test='params.isDeleted != null'>AND is_deleted = #{params.isDeleted}</if>" +
            "<if test='params.majorCode != null and params.majorCode != \"\"'>" +
            "AND major_code = #{params.majorCode}</if>" +
            "<if test='params.majorName != null and params.majorName != \"\"'>" +
            "AND major_name = #{params.majorName}</if>" +
            "<if test='params.constraintCode != null and params.constraintCode != \"\"'>" +
            "AND constraint_code = #{params.constraintCode}</if>" +
            "<if test='params.constraintName != null and params.constraintName != \"\"'>" +
            "AND constraint_name = #{params.constraintName}</if>" +
            "</where> ORDER BY major_code ASC, id ASC" +
            "</script>")
    IPage<MajorConstraint> selectPageCustom(Page<?> page, @Param("params") Map<String, Object> params);

    @Select("SELECT * FROM t_major_constraint WHERE id = #{id}")
    MajorConstraint selectByIdCustom(@Param("id") Long id);

    @Update("UPDATE t_major_constraint SET is_deleted = #{isDeleted}, version = version + 1 WHERE id = #{id} AND version = #{version}")
    int updateIsDeleted(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted, @Param("version") Integer version);

    @Delete("DELETE FROM t_major_constraint WHERE id = #{id}")
    int deletePhysical(@Param("id") Long id);
}
