package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.WithdrawRecord;
import com.haifeng.common.handler.AESEncryptTypeHandler;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface WithdrawRecordMapper extends BaseMapper<WithdrawRecord> {

    @Delete("DELETE FROM t_withdraw_record WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    /**
     * 查询提现记录（含已删除）。wechat_id 是 AES 加密列，
     * 自定义 @Select 不会自动套用实体上的 AESEncryptTypeHandler，
     * 必须通过 @Results 显式声明该列的 typeHandler，否则返回密文。
     */
    @Select("SELECT *, is_deleted AS deleted FROM t_withdraw_record WHERE id = #{id}")
    @Results({
            @Result(column = "wechat_id", property = "wechatId", typeHandler = AESEncryptTypeHandler.class)
    })
    WithdrawRecord selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("UPDATE t_withdraw_record SET is_deleted = false, updated_at = #{updatedAt} WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);

    @Update("UPDATE t_withdraw_record SET member_name = #{memberName}, phone = #{phone}, " +
            "wechat_id = #{wechatId, typeHandler=com.haifeng.common.handler.AESEncryptTypeHandler}, " +
            "wechat_id_index = #{wechatIdIndex}, updated_at = #{updatedAt} " +
            "WHERE member_id = #{memberId} AND is_deleted = false")
    int updateMemberInfo(@Param("memberId") Long memberId,
                         @Param("memberName") String memberName,
                         @Param("phone") String phone,
                         @Param("wechatId") String wechatId,
                         @Param("wechatIdIndex") String wechatIdIndex,
                         @Param("updatedAt") OffsetDateTime updatedAt);

    /** 同步管理员用户名到提现记录的 operator_name 冗余字段 */
    @Update("UPDATE t_withdraw_record SET operator_name = #{operatorName}, updated_at = #{updatedAt} " +
            "WHERE operator_id = #{operatorId} AND is_deleted = false")
    int updateOperatorName(@Param("operatorId") Long operatorId,
                           @Param("operatorName") String operatorName,
                           @Param("updatedAt") OffsetDateTime updatedAt);

    @Update("UPDATE t_withdraw_record SET status = #{status}, operator_id = #{operatorId}, operator_name = #{operatorName}, remark = #{remark}, updated_at = #{updatedAt} WHERE id = #{id} AND status = #{expectedStatus}")
    int updateStatusCas(@Param("id") Long id,
                        @Param("expectedStatus") String expectedStatus,
                        @Param("status") String status,
                        @Param("operatorId") Long operatorId,
                        @Param("operatorName") String operatorName,
                        @Param("remark") String remark,
                        @Param("updatedAt") OffsetDateTime updatedAt);
}
