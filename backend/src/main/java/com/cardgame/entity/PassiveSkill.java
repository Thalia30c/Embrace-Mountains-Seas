package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 被动技能汇总表 passive_skill 对应的实体，主键 code 为 P01-P10，非自增。
 *
 * <p>字段：code / skill_name / effect_type / target / effect_value / duration_round / effect_desc</p>
 */
@Table(value = "passive_skill", autoIncrement = false)
public class PassiveSkill {

    /** 被动技能编码 P01-P10 */
    @Column(value = "code", id = true)
    private String code;

    /** 被动技能名称 */
    @Column("skill_name")
    private String skillName;

    /** 效果类型：A 进攻增益 / D 防御增益 / H 治疗 / R 清除 / N 减益 / B 封锁 / C 复制 / E 反弹 */
    @Column("effect_type")
    private String effectType;

    /** 作用目标：S 己方 / E 对手 */
    @Column("target")
    private String target;

    /** 效果数值，百分比存整数，如 50 */
    @Column("effect_value")
    private Integer effectValue;

    /** 持续回合：0 立即 / 1 瞬发 / 2 持续 2 回合 */
    @Column("duration_round")
    private Integer durationRound;

    /** 效果文字说明 */
    @Column("effect_desc")
    private String effectDesc;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getEffectType() { return effectType; }
    public void setEffectType(String effectType) { this.effectType = effectType; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public Integer getEffectValue() { return effectValue; }
    public void setEffectValue(Integer effectValue) { this.effectValue = effectValue; }
    public Integer getDurationRound() { return durationRound; }
    public void setDurationRound(Integer durationRound) { this.durationRound = durationRound; }
    public String getEffectDesc() { return effectDesc; }
    public void setEffectDesc(String effectDesc) { this.effectDesc = effectDesc; }

    @Override
    public String toString() {
        return "PassiveSkill{code='" + code + "', skillName='" + skillName + "', effectType='" + effectType
                + "', target='" + target + "', effectValue=" + effectValue + ", durationRound=" + durationRound + '}';
    }
}
