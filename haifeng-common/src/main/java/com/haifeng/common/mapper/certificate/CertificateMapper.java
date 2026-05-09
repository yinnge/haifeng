package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.Certificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CertificateMapper extends BaseMapper<Certificate> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_certificate WHERE cert_name = #{certName} AND is_deleted = FALSE)")
    boolean existsByCertName(@Param("certName") String certName);
}
