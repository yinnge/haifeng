package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.dto.user.FavoriteTypeCountDTO;
import com.haifeng.common.entity.user.MemberFavorite;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberFavoriteMapper extends BaseMapper<MemberFavorite> {

    /**
     * 幂等插入：已存在同一会员对同一目标的收藏时直接忽略
     */
    @Insert("INSERT INTO t_member_favorite (id, member_id, target_type, target_id, created_at, updated_at) " +
            "VALUES (#{id}, #{memberId}, #{targetType}, #{targetId}, NOW(), NOW()) " +
            "ON CONFLICT (member_id, target_type, target_id) DO NOTHING")
    int insertIgnoreConflict(@Param("id") Long id,
                             @Param("memberId") Long memberId,
                             @Param("targetType") String targetType,
                             @Param("targetId") Long targetId);

    @Select("SELECT target_type, COUNT(*) AS cnt FROM t_member_favorite " +
            "WHERE member_id = #{memberId} GROUP BY target_type")
    List<FavoriteTypeCountDTO> countByType(@Param("memberId") Long memberId);
}
