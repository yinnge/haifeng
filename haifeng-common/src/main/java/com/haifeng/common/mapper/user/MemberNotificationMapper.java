package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.MemberNotification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface MemberNotificationMapper extends BaseMapper<MemberNotification> {

    @Delete("DELETE FROM t_member_notification WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Select("SELECT * FROM t_member_notification WHERE id = #{id}")
    MemberNotification selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("UPDATE t_member_notification SET is_deleted = false, updated_at = #{updatedAt} WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);
}
