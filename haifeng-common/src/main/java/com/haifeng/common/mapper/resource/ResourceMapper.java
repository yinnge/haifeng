package com.haifeng.common.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.resource.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE t_resource SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = false")
    int incrementViewCount(@Param("id") Long id);

    @Update("<script>" +
            "UPDATE t_resource SET is_deleted = true, updated_at = #{now} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_deleted = false" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids, @Param("now") OffsetDateTime now);

    @Select("SELECT DISTINCT category FROM t_resource WHERE is_deleted = false AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategories();

    /**
     * 更新isDeleted状态（绕过全局逻辑删除）
     */
    @Update("UPDATE t_resource SET is_deleted = #{isDeleted}, updated_at = #{now} WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted, @Param("now") OffsetDateTime now);

    /**
     * 分页查询（忽略逻辑删除，可查询全部数据）
     */
    @Select("<script>" +
            "SELECT * FROM t_resource" +
            "<where>" +
            "<if test='resourceName != null and resourceName != \"\"'>AND resource_name LIKE CONCAT('%', #{resourceName}, '%')</if>" +
            "<if test='category != null and category != \"\"'>AND category LIKE CONCAT('%', #{category}, '%')</if>" +
            "<if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>" +
            "</where>" +
            "ORDER BY sort_order ASC, updated_at DESC" +
            "</script>")
    IPage<Resource> selectPageIgnoreLogicDelete(Page<Resource> page,
                                               @Param("resourceName") String resourceName,
                                               @Param("category") String category,
                                               @Param("isDeleted") Boolean isDeleted);

    /**
     * 根据ID查询（忽略逻辑删除，可查询已禁用数据）
     */
    @Select("SELECT * FROM t_resource WHERE id = #{id}")
    Resource findByIdIgnoreLogicDelete(@Param("id") Long id);

    /**
     * 更新资源信息（忽略逻辑删除，可更新已禁用数据）
     */
    @Update("<script>" +
            "UPDATE t_resource" +
            "<set>" +
            "<if test='resourceName != null'>resource_name = #{resourceName},</if>" +
            "<if test='coverUrl != null'>cover_url = #{coverUrl},</if>" +
            "<if test='description != null'>description = #{description},</if>" +
            "<if test='resourceUrl != null'>resource_url = #{resourceUrl},</if>" +
            "<if test='accessCode != null'>access_code = #{accessCode},</if>" +
            "<if test='category != null'>category = #{category},</if>" +
            "<if test='fileType != null'>file_type = #{fileType},</if>" +
            "updated_at = #{updatedAt}" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateIgnoreLogicDelete(@Param("id") Long id,
                                @Param("resourceName") String resourceName,
                                @Param("coverUrl") String coverUrl,
                                @Param("description") String description,
                                @Param("resourceUrl") String resourceUrl,
                                @Param("accessCode") String accessCode,
                                @Param("category") String category,
                                @Param("fileType") String fileType,
                                @Param("updatedAt") OffsetDateTime updatedAt);
}
