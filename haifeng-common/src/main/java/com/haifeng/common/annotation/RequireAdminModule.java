package com.haifeng.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员模块权限注解
 * 用于标记需要特定模块权限的管理员接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdminModule {

    /**
     * 模块编码，对应 sys_module.module_code
     */
    String value();
}
