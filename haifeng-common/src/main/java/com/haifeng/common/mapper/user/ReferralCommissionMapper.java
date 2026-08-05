package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.common.entity.user.ReferralCommission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface ReferralCommissionMapper extends BaseMapper<ReferralCommission> {

    @Delete("DELETE FROM t_referral_commission WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Select("SELECT *, is_deleted AS deleted FROM t_referral_commission WHERE id = #{id}")
    ReferralCommission selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("UPDATE t_referral_commission SET is_deleted = false, status = 'active', updated_at = #{updatedAt} WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("updatedAt") OffsetDateTime updatedAt);

    @Update("UPDATE t_referral_commission SET referrer_name = #{referrerName}, referrer_phone = #{referrerPhone}, " +
            "updated_at = #{updatedAt} WHERE referrer_id = #{referrerId} AND is_deleted = false")
    int updateReferrerInfo(@Param("referrerId") Long referrerId,
                           @Param("referrerName") String referrerName,
                           @Param("referrerPhone") String referrerPhone,
                           @Param("updatedAt") OffsetDateTime updatedAt);

    @Update("UPDATE t_referral_commission SET referee_name = #{refereeName}, referee_phone = #{refereePhone}, " +
            "updated_at = #{updatedAt} WHERE referee_id = #{refereeId} AND is_deleted = false")
    int updateRefereeInfo(@Param("refereeId") Long refereeId,
                          @Param("refereeName") String refereeName,
                          @Param("refereePhone") String refereePhone,
                          @Param("updatedAt") OffsetDateTime updatedAt);

    /**
     * 分页查询所有记录（包括已禁用的），绕过 @TableLogic 自动过滤
     */
    @Select("SELECT * FROM t_referral_commission ${ew.customSqlSegment}")
    IPage<ReferralCommission> selectPageIgnoreDeleted(
            IPage<ReferralCommission> page,
            @Param("ew") LambdaQueryWrapper<ReferralCommission> wrapper);
}
