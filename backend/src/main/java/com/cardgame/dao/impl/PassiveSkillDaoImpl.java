package com.cardgame.dao.impl;

import com.cardgame.dao.PassiveSkillDao;
import com.cardgame.entity.PassiveSkill;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * passive_skill 表数据访问实现：通用能力来自 {@link BaseDaoImpl}，这里实现被动技能相关的业务查询。
 */
@Repository
public class PassiveSkillDaoImpl extends BaseDaoImpl<PassiveSkill> implements PassiveSkillDao {

    public PassiveSkillDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, PassiveSkill.class);
    }

    @Override
    public PassiveSkill findByCode(String code) {
        return selectById(code);
    }

    @Override
    public List<PassiveSkill> findByEffectType(String effectType) {
        return selectList("effect_type", effectType, "code ASC", 0);
    }

    @Override
    public List<PassiveSkill> findByTarget(String target) {
        return selectList("target", target, "code ASC", 0);
    }

    @Override
    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }
}
