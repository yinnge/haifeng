package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.WithdrawRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface WithdrawRecordMapper extends BaseMapper<WithdrawRecord> {

    @Delete("DELETE FROM t_withdraw_record WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Select("SELECT * FROM t_withdraw_record WHERE id = #{id}")
    WithdrawRecord selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("UPDATE t_withdraw_record SET is_deleted = false, updated_at = #{updatedAt} WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);

    @Update("UPDATE t_withdraw_record SET status = #{status}, operator_id = #{operatorId}, operator_name = #{operatorName}, remark = #{remark}, updated_at = #{updatedAt} WHERE id = #{id} AND status = #{expectedStatus}")
    int updateStatusCas(@Param("id") Long id,
                        @Param("expectedStatus") String expectedStatus,
                        @Param("status") String status,
                        @Param("operatorId") Long operatorId,
                        @Param("operatorName") String operatorName,
                        @Param("remark") String remark,
                        @Param("updatedAt") OffsetDateTime updatedAt);
}
