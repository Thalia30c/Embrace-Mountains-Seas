package com.cardgame.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体类表名注解。不加时按类名驼峰转下划线推导，如 UserCard -> t_user_card。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    /** 表名 */
    String value();

    /** 主键是否自增，默认 true */
    boolean autoIncrement() default true;
}
