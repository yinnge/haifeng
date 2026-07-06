package com.haifeng.common.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.system.ModelProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ModelProviderMapper extends BaseMapper<ModelProvider> {

    @Select("""
            SELECT id, api_key, model_name, provider_name, type, description, status, created_at, updated_at
            FROM t_model_provider
            WHERE status = 1
            ORDER BY id ASC
            """)
    List<ModelProvider> findAllEnabled();

    @Select("""
            SELECT id, api_key, model_name, provider_name, type, description, status, created_at, updated_at
            FROM t_model_provider
            WHERE provider_name = #{providerName}
              AND status = 1
            ORDER BY id ASC
            """)
    List<ModelProvider> findEnabledByProvider(@Param("providerName") String providerName);

    @Select("""
            SELECT id, api_key, model_name, provider_name, type, description, status, created_at, updated_at
            FROM t_model_provider
            WHERE type = #{type}
              AND status = 1
            ORDER BY id ASC
            """)
    List<ModelProvider> findAllEnabledByType(@Param("type") String type);

    @Select("""
            SELECT id, api_key, model_name, provider_name, type, description, status, created_at, updated_at
            FROM t_model_provider
            WHERE provider_name = #{providerName}
              AND type = #{type}
              AND status = 1
            ORDER BY id ASC
            """)
    List<ModelProvider> findEnabledByProviderAndType(@Param("providerName") String providerName, @Param("type") String type);
}
