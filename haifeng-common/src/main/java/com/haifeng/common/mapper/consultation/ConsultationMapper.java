package com.haifeng.common.mapper.consultation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.consultation.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;

@Mapper
public interface ConsultationMapper extends BaseMapper<Consultation> {

    /**
     * 标记已处理：带 status = 0 条件，重复点击不会重复更新，天然幂等。
     */
    @Update("UPDATE t_consultation SET status = 1, updated_at = #{updatedAt} " +
            "WHERE id = #{id} AND teacher_member_id = #{teacherMemberId} AND status = 0")
    int markHandled(@Param("id") Long id,
                    @Param("teacherMemberId") Long teacherMemberId,
                    @Param("updatedAt") OffsetDateTime updatedAt);
}
