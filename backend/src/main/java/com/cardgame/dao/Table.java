package com.cardgame.dao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体对应的数据库表名。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    /** 表名 */
    String value();

    /** 主键是否自增，自增时 insert 不写主键列并回填数据库生成的主键 */
    boolean autoIncrement() default true;
}
