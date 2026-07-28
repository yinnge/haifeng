package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.certificate.Competition;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_competition WHERE comp_name = #{compName} AND is_deleted = FALSE)")
    boolean existsByCompName(@Param("compName") String compName);

    @Select("SELECT * FROM t_competition WHERE comp_name = #{compName} AND is_deleted = FALSE LIMIT 1")
    Competition findByCompName(@Param("compName") String compName);

    @Select("<script>" +
            "SELECT * FROM t_competition" +
            "<where>" +
            "<if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>" +
            "<if test='compName != null and compName != \"\"'>AND comp_name LIKE CONCAT('%', #{compName}, '%')</if>" +
            "<if test='compLevel != null and compLevel != \"\"'>AND comp_level = #{compLevel}</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<Competition> selectPageIgnoreLogicDelete(Page<Competition> page,
                                                   @Param("isDeleted") Boolean isDeleted,
                                                   @Param("compName") String compName,
                                                   @Param("compLevel") String compLevel);

    @Select("SELECT * FROM t_competition WHERE id = #{id}")
    Competition findByIdIgnoreLogicDelete(@Param("id") Long id);

    @Update("UPDATE t_competition SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);

    @Update("UPDATE t_competition SET comp_name = #{compName}, comp_level = #{compLevel}, " +
            "registration_time = #{registrationTime}, updated_at = NOW() WHERE id = #{id}")
    int updateIgnoreLogicDelete(Competition competition);

    @Delete("DELETE FROM t_competition WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("<script>" +
            "DELETE FROM t_competition WHERE id IN" +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int physicalDeleteBatchByIds(@Param("ids") List<Long> ids);
}
