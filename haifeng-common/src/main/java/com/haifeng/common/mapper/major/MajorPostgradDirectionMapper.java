package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.major.MajorPostgradDirection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface MajorPostgradDirectionMapper extends BaseMapper<MajorPostgradDirection> {

    @Select("SELECT COUNT(*) > 0 FROM t_major_postgrad_direction WHERE major_id = #{majorId} AND postgrad_major_id = #{postgradMajorId}")
    boolean existsByRelation(@Param("majorId") Long majorId, @Param("postgradMajorId") Long postgradMajorId);

    /**
     * 接口1：给定本科专业 id，分页返回关联的考研专业（id + 名称）
     * 走 idx_mpd_major 索引 → 主键回表 t_postgrad_major → status 过滤
     */
    @Select("SELECT pm.id AS id, pm.major_name AS postgradMajorName " +
            "FROM t_major_postgrad_direction mpd " +
            "JOIN t_postgrad_major pm ON pm.id = mpd.postgrad_major_id " +
            "WHERE mpd.major_id = #{majorId} AND pm.status = 1 " +
            "ORDER BY mpd.sort_order ASC, pm.id DESC")
    IPage<Map<String, Object>> selectPostgradMajorsByMajorId(
            Page<?> page,
            @Param("majorId") Long majorId);

    /**
     * 接口2：给定考研专业 id，分页返回关联的本科专业（id + 名称）
     * 走 idx_mpd_postgrad 索引 → 主键回表 t_major → status 过滤
     */
    @Select("SELECT m.id AS id, m.major_name AS majorName " +
            "FROM t_major_postgrad_direction mpd " +
            "JOIN t_major m ON m.id = mpd.major_id " +
            "WHERE mpd.postgrad_major_id = #{postgradMajorId} AND m.status = 1 " +
            "ORDER BY mpd.sort_order ASC, m.id DESC")
    IPage<Map<String, Object>> selectMajorsByPostgradMajorId(
            Page<?> page,
            @Param("postgradMajorId") Long postgradMajorId);
}
