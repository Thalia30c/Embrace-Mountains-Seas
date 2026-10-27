package com.cardgame.dao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体字段对应的数据库表列。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /** 列名，留空时按字段名驼峰转下划线推导，如 userAccount -> user_account */
    String value() default "";

    /** 是否为主键 */
    boolean id() default false;
}
