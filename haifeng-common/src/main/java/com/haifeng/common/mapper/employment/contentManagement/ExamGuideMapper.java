package com.haifeng.common.mapper.employment.contentManagement;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;

@Mapper
public interface ExamGuideMapper extends BaseMapper<ExamGuide> {

    /**
     * 物理删除（真实 DELETE FROM，绕过 MyBatis-Plus 逻辑删除拦截）
     */
    @Delete("DELETE FROM t_exam_guide WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("<script>DELETE FROM t_exam_guide WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int physicalDeleteBatchIds(@Param("ids") Collection<Long> ids);

    /**
     * 分页查询（自定义 SQL，绕过 MyBatis-Plus 逻辑删除自动过滤，支持 status 状态筛选）
     */
    IPage<ExamGuide> selectGuidePage(Page<ExamGuide> page,
                                     @Param("title") String title,
                                     @Param("subtitle") String subtitle,
                                     @Param("guideCategory") String guideCategory,
                                     @Param("guideType") String guideType,
                                     @Param("isTop") Boolean isTop,
                                     @Param("status") Integer status);

    /**
     * 按 id 查询（自定义 SQL，不受逻辑删除过滤，禁用记录也能查到——详情/编辑用）
     */
    ExamGuide selectGuideById(@Param("id") Long id);

    /**
     * 更新禁用状态（自定义 @Update，绕开 MyBatis-Plus 逻辑删除自动注入的 WHERE is_deleted=false，
     * 否则对已禁用记录（is_deleted=true）无法更新）
     */
    @Update("UPDATE t_exam_guide SET is_deleted = #{isDeleted} WHERE id = #{id}")
    int updateIsDeleted(@Param("id") Long id, @Param("isDeleted") boolean isDeleted);

    /**
     * 动态更新（自定义 SQL，绕开逻辑删除过滤，禁用记录也可修改）
     */
    int updateGuide(ExamGuide entity);

    /**
     * 阅读量 +1（原子更新，并发安全）
     */
    @Update("UPDATE t_exam_guide SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = false")
    int incrementViewCount(@Param("id") Long id);
}
