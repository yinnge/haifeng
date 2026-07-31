package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    @Update("UPDATE t_member SET referrer_username = #{referrerUsername}, updated_at = #{updatedAt} " +
            "WHERE referrer_id = #{referrerId} AND is_deleted = false")
    int updateReferrerUsername(@Param("referrerId") Long referrerId,
                               @Param("referrerUsername") String referrerUsername,
                               @Param("updatedAt") OffsetDateTime updatedAt);
}
