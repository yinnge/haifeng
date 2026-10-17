package com.haifeng.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要老师身份注解
 * 用于标记需要老师身份才能访问的方法或类。
 * 老师身份叠加在会员体系之上，独立于 member_type，由管理员授予/撤销。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireTeacher {
}
