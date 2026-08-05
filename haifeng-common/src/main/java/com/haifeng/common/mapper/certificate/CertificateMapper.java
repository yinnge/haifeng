package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.config.StringListTypeHandler;
import com.haifeng.common.entity.certificate.Certificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface CertificateMapper extends BaseMapper<Certificate> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_certificate WHERE cert_name = #{certName} AND is_deleted = FALSE)")
    boolean existsByCertName(@Param("certName") String certName);

    @Select("SELECT DISTINCT category FROM t_certificate WHERE is_deleted = FALSE AND category IS NOT NULL ORDER BY category")
    List<String> listCategories();

    /**
     * 忽略逻辑删除，查询所有数据（启用+禁用）
     * exam_requirements 是数组列，自定义 @Select 不套 typeHandler，
     * 必须用 @Results 显式声明 StringListTypeHandler。
     */
    @Select("<script>" +
            "SELECT * FROM t_certificate" +
            "<where>" +
            "<if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>" +
            "<if test='certName != null and certName != \"\"'>AND cert_name LIKE CONCAT('%', #{certName}, '%')</if>" +
            "<if test='category != null and category != \"\"'>AND category = #{category}</if>" +
            "<if test='certLevel != null and certLevel != \"\"'>AND cert_level = #{certLevel}</if>" +
            "<if test='applicableMajor != null and applicableMajor != \"\"'>AND applicable_major LIKE CONCAT('%', #{applicableMajor}, '%')</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    @Results({
            @Result(column = "exam_requirements", property = "examRequirements", typeHandler = StringListTypeHandler.class)
    })
    IPage<Certificate> selectPageIgnoreLogicDelete(Page<Certificate> page,
                                                   @Param("isDeleted") Boolean isDeleted,
                                                   @Param("certName") String certName,
                                                   @Param("category") String category,
                                                   @Param("certLevel") String certLevel,
                                                   @Param("applicableMajor") String applicableMajor);

    /**
     * 忽略逻辑删除，根据ID查询（可用于查询已禁用的数据）。
     * 走 MP 内置 selectOne，autoResultMap 才会给 exam_requirements 套 StringListTypeHandler。
     */
    default Certificate findByIdIgnoreLogicDelete(Long id) {
        return selectOne(new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getId, id)
                .last("LIMIT 1"));
    }

    /**
     * 忽略逻辑删除，直接更新is_deleted字段
     */
    @Update("UPDATE t_certificate SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);

    /**
     * 忽略逻辑删除，更新证书数据（不自动追加 is_deleted=false 条件）
     */
    @Update("<script>" +
            "UPDATE t_certificate" +
            "<set>" +
            "<if test='certName != null'>cert_name = #{certName},</if>" +
            "<if test='category != null'>category = #{category},</if>" +
            "<if test='certLevel != null'>cert_level = #{certLevel},</if>" +
            "<if test='applicableMajor != null'>applicable_major = #{applicableMajor},</if>" +
            "<if test='registrationTime != null'>registration_time = #{registrationTime},</if>" +
            "<if test='examTime != null'>exam_time = #{examTime},</if>" +
            "<if test='examFee != null'>exam_fee = #{examFee},</if>" +
            "<if test='certIntro != null'>cert_intro = #{certIntro},</if>" +
            "<if test='examRequirements != null'>exam_requirements = #{examRequirements, typeHandler=com.haifeng.common.config.StringListTypeHandler},</if>" +
            "<if test='examArrangement != null'>exam_arrangement = #{examArrangement},</if>" +
            "<if test='officialWebsite != null'>official_website = #{officialWebsite},</if>" +
            "updated_at = NOW()" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateByIdIgnoreLogicDelete(@Param("id") Long id,
                                    @Param("certName") String certName,
                                    @Param("category") String category,
                                    @Param("certLevel") String certLevel,
                                    @Param("applicableMajor") String applicableMajor,
                                    @Param("registrationTime") String registrationTime,
                                    @Param("examTime") String examTime,
                                    @Param("examFee") Integer examFee,
                                    @Param("certIntro") String certIntro,
                                    @Param("examRequirements") List<String> examRequirements,
                                    @Param("examArrangement") String examArrangement,
                                    @Param("officialWebsite") String officialWebsite);

    /**
     * 忽略逻辑删除，批量更新is_deleted字段
     */
    @Update("<script>" +
            "UPDATE t_certificate SET is_deleted = #{isDeleted}, updated_at = NOW()" +
            "WHERE id IN" +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateIsDeletedByIds(@Param("ids") List<Long> ids, @Param("isDeleted") Boolean isDeleted);

    /**
     * 忽略逻辑删除，物理删除证书（硬删除）
     */
    @Update("<script>" +
            "DELETE FROM t_certificate WHERE id IN" +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int physicalDeleteBatchByIds(@Param("ids") List<Long> ids);
}
