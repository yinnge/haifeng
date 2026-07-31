package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.dto.user.BroadcastNotificationResult;
import com.haifeng.common.entity.user.MemberNotification;
import com.haifeng.common.vo.user.NotificationRecordVO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface MemberNotificationMapper extends BaseMapper<MemberNotification> {

    @Delete("DELETE FROM t_member_notification WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Select("SELECT *, is_deleted AS deleted FROM t_member_notification WHERE id = #{id}")
    MemberNotification selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("UPDATE t_member_notification SET is_deleted = false, updated_at = #{updatedAt} WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);

    /**
     * 逻辑删除：绕过 @TableLogic 对 updateById 的限制，直接设置 is_deleted = true
     */
    @Update("UPDATE t_member_notification SET is_deleted = true, updated_at = #{updatedAt} WHERE id = #{id} AND is_deleted = false")
    int logicalDeleteById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);

    /**
     * 整批撤回群发公告：将同一 broadcast_id 的全部记录置为已删除（禁用）
     */
    @Update("UPDATE t_member_notification SET is_deleted = true, updated_at = #{updatedAt} " +
            "WHERE broadcast_id = #{broadcastId} AND is_deleted = false")
    int revokeBroadcast(@Param("broadcastId") Long broadcastId, @Param("updatedAt") OffsetDateTime updatedAt);

    /**
     * 整批恢复群发公告：将同一 broadcast_id 的全部已删除记录恢复
     */
    @Update("UPDATE t_member_notification SET is_deleted = false, updated_at = #{updatedAt} " +
            "WHERE broadcast_id = #{broadcastId} AND is_deleted = true")
    int restoreBroadcast(@Param("broadcastId") Long broadcastId, @Param("updatedAt") OffsetDateTime updatedAt);

    /**
     * 统计某 broadcast_id 下的记录数（用于校验批次是否存在）
     */
    @Select("SELECT COUNT(*) FROM t_member_notification WHERE broadcast_id = #{broadcastId}")
    long countByBroadcastId(@Param("broadcastId") Long broadcastId);

    /**
     * 群发通知分页查询：按 broadcast_id+title+content+created_at 分组，返回一条 + 总人数 + 已禁用人数
     * 注：不按 is_deleted 过滤，以便 admin 端能展示「正常 / 部分禁用 / 完全禁用」状态
     */
    @Select("SELECT MIN(id) as id, broadcast_id, notification_type, title, content, created_at, " +
            "COUNT(*)::int as recipient_count, " +
            "SUM(CASE WHEN is_deleted THEN 1 ELSE 0 END)::int as disabled_count " +
            "FROM t_member_notification " +
            "WHERE notification_type = 'system_notice' " +
            "GROUP BY broadcast_id, notification_type, title, content, created_at " +
            "ORDER BY created_at DESC")
    List<BroadcastNotificationResult> selectBroadcastPage(RowBounds rowBounds);

    /**
     * 群发通知分组总数（含已禁用批次）
     */
    @Select("SELECT COUNT(*) FROM ( " +
            "SELECT 1 FROM t_member_notification " +
            "WHERE notification_type = 'system_notice' " +
            "GROUP BY broadcast_id, title, content, created_at" +
            ") t")
    long countBroadcastGroups();

    /**
     * 查询所有通知（含已禁用），绕过 @TableLogic 自动过滤
     * 使用 NotificationRecordVO 接收结果，避免 @TableLogic 干扰字段映射
     */
    @Select("<script>" +
            "SELECT id, member_id, notification_type, title, content, " +
            "is_read, read_at, is_deleted as disabled, created_at, updated_at " +
            "FROM t_member_notification " +
            "WHERE 1=1 " +
            "<if test='notificationType != null'> AND notification_type = #{notificationType} </if> " +
            "<if test='memberId != null'> AND member_id = #{memberId} </if> " +
            "<if test='isRead != null'> AND is_read = #{isRead} </if> " +
            "ORDER BY created_at DESC" +
            "</script>")
    List<NotificationRecordVO> selectAllWithDisabled(
            @Param("notificationType") String notificationType,
            @Param("memberId") Long memberId,
            @Param("isRead") Boolean isRead,
            RowBounds rowBounds);

    /**
     * 查询所有通知总数（含已禁用），绕过 @TableLogic 自动过滤
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_member_notification " +
            "WHERE 1=1 " +
            "<if test='notificationType != null'> AND notification_type = #{notificationType} </if> " +
            "<if test='memberId != null'> AND member_id = #{memberId} </if> " +
            "<if test='isRead != null'> AND is_read = #{isRead} </if>" +
            "</script>")
    long countAllWithDisabled(
            @Param("notificationType") String notificationType,
            @Param("memberId") Long memberId,
            @Param("isRead") Boolean isRead);
}
