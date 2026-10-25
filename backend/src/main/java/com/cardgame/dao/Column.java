package com.cardgame.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段与数据库列的映射注解。不加时按驼峰转下划线推导，如 createTime -> create_time。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /** 列名 */
    String value() default "";

    /** 是否为主键 */
    boolean id() default false;

    /** 插入时是否忽略该列 */
    boolean insertable() default true;

    /** 更新时是否忽略该列 */
    boolean updatable() default true;
}
