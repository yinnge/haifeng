package com.haifeng.common.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.resource.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    /**
     * 显式统计「存在且未逻辑删除」的文件数；原生 SQL，不依赖 @TableLogic 注解的自动过滤。
     */
    @Select("SELECT COUNT(*) FROM t_file_info WHERE id = #{id} AND is_deleted = false")
    int countActiveById(@Param("id") Long id);
}
