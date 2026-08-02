package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.handler.AESEncryptTypeHandler;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.session.RowBounds;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface MemberOrderMapper extends BaseMapper<MemberOrder> {

    @Delete("DELETE FROM member_orders WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    /**
     * 查询订单（含已禁用/已删除）。wechat_id 是 AES 加密列，
     * 自定义 @Select 不会自动套用实体上的 AESEncryptTypeHandler，
     * 必须通过 @Results 显式声明该列的 typeHandler，否则返回密文。
     */
    @Select("SELECT *, is_deleted AS deleted FROM member_orders WHERE id = #{id}")
    @Results({
            @Result(column = "wechat_id", property = "wechatId", typeHandler = AESEncryptTypeHandler.class)
    })
    MemberOrder selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("UPDATE member_orders SET is_deleted = false, updated_at = #{updatedAt} WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);

    /** 软删除：绕过 @TableLogic 对 updateById 的限制 */
    @Update("UPDATE member_orders SET is_deleted = true, updated_at = #{updatedAt} WHERE id = #{id} AND is_deleted = false")
    int logicalDeleteById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);

    @Update("UPDATE member_orders SET member_name = #{memberName}, phone = #{phone}, " +
            "wechat_id = #{wechatId, typeHandler=com.haifeng.common.handler.AESEncryptTypeHandler}, " +
            "wechat_id_index = #{wechatIdIndex}, updated_at = #{updatedAt} " +
            "WHERE member_id = #{memberId} AND is_deleted = false")
    int updateMemberInfo(@Param("memberId") Long memberId,
                         @Param("memberName") String memberName,
                         @Param("phone") String phone,
                         @Param("wechatId") String wechatId,
                         @Param("wechatIdIndex") String wechatIdIndex,
                         @Param("updatedAt") OffsetDateTime updatedAt);

    /** 查询所有订单（含已禁用），绕过 @TableLogic 自动过滤 */
    @Select("<script>" +
            "SELECT * FROM member_orders WHERE 1=1 " +
            "<if test='phone != null and phone != \"\"'> AND phone LIKE CONCAT('%', #{phone}, '%') </if>" +
            "<if test='wechatIdIndex != null and wechatIdIndex != \"\"'> AND wechat_id_index = #{wechatIdIndex} </if>" +
            "<if test='operatorName != null and operatorName != \"\"'> AND operator_name LIKE CONCAT('%', #{operatorName}, '%') </if>" +
            "<if test='orderType != null and orderType != \"\"'> AND order_type = #{orderType} </if>" +
            "<if test='orderStatus != null and orderStatus != \"\"'> AND status = #{orderStatus} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    @Results({
            @Result(column = "wechat_id", property = "wechatId", typeHandler = AESEncryptTypeHandler.class)
    })
    List<MemberOrder> selectAllWithDisabled(
            @Param("phone") String phone,
            @Param("wechatIdIndex") String wechatIdIndex,
            @Param("operatorName") String operatorName,
            @Param("orderType") String orderType,
            @Param("orderStatus") String orderStatus,
            RowBounds rowBounds);

    /** 同步管理员用户名到订单的 operator_name 冗余字段 */
    @Update("UPDATE member_orders SET operator_name = #{operatorName}, updated_at = #{updatedAt} " +
            "WHERE operator_id = #{operatorId} AND is_deleted = false")
    int updateOperatorName(@Param("operatorId") Long operatorId,
                           @Param("operatorName") String operatorName,
                           @Param("updatedAt") OffsetDateTime updatedAt);

    /** 查询所有订单总数（含已禁用） */
    @Select("<script>" +
            "SELECT COUNT(*) FROM member_orders WHERE 1=1 " +
            "<if test='phone != null and phone != \"\"'> AND phone LIKE CONCAT('%', #{phone}, '%') </if>" +
            "<if test='wechatIdIndex != null and wechatIdIndex != \"\"'> AND wechat_id_index = #{wechatIdIndex} </if>" +
            "<if test='operatorName != null and operatorName != \"\"'> AND operator_name LIKE CONCAT('%', #{operatorName}, '%') </if>" +
            "<if test='orderType != null and orderType != \"\"'> AND order_type = #{orderType} </if>" +
            "<if test='orderStatus != null and orderStatus != \"\"'> AND status = #{orderStatus} </if>" +
            "</script>")
    long countAllWithDisabled(
            @Param("phone") String phone,
            @Param("wechatIdIndex") String wechatIdIndex,
            @Param("operatorName") String operatorName,
            @Param("orderType") String orderType,
            @Param("orderStatus") String orderStatus);
}
