package com.haifeng.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要Pro会员（专业版及以上）注解
 * 用于标记需要Pro或VIP会员才能访问的方法或类
 * 权限层级: normal < pro < vip
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePro {
}
