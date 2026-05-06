package com.haifeng.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标注需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作描述
     */
    String action() default "";
}
