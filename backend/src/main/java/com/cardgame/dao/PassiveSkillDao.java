package com.cardgame.dao;

import com.cardgame.entity.PassiveSkill;

import java.util.List;

/**
 * passive_skill 表数据访问接口：继承 {@link BaseDao} 已有的通用 CRUD，这里声明技能相关的业务查询。
 */
public interface PassiveSkillDao extends BaseDao<PassiveSkill> {

    /** 按技能编码查询：P01-P10 */
    PassiveSkill findByCode(String code);

    /** 按效果类型查询：A 进攻增益 / D 防御增益 / H 治疗 / R 清除 / N 减益 / B 封锁 / C 复制 / E 反弹 */
    List<PassiveSkill> findByEffectType(String effectType);

    /** 按作用目标查询：S 己方 / E 对手 */
    List<PassiveSkill> findByTarget(String target);
}
