package com.haifeng.common.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.system.AdminLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface AdminLogMapper extends BaseMapper<AdminLog> {

    /**
     * 批量硬删除
     */
    @Delete("<script>" +
            "DELETE FROM admin_logs WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchHardDelete(@Param("ids") List<Long> ids);

    /**
     * 删除指定时间之前的日志
     */
    @Delete("DELETE FROM admin_logs WHERE created_at < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") OffsetDateTime beforeTime);

    /**
     * 删除全部日志
     */
    @Delete("DELETE FROM admin_logs")
    int deleteAll();

    /** 同步管理员用户名到日志的 admin_name 冗余字段 */
    @Update("UPDATE admin_logs SET admin_name = #{adminName} WHERE admin_id = #{adminId}")
    int updateAdminName(@Param("adminId") Long adminId, @Param("adminName") String adminName);
}
