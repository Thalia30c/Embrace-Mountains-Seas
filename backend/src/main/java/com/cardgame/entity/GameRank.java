package com.cardgame.entity;

import com.cardgame.dao.Column;
import com.cardgame.dao.Table;

/**
 * 游戏等级段位表 game_rank 对应的实体，共 19 个段位（山稚 → 山海尊）。
 *
 * <p>字段：rank_id / rank_name / sub_rank / need_total_score / remark</p>
 */
@Table("game_rank")
public class GameRank {

    /** 段位顺序 1-19 */
    @Column(value = "rank_id", id = true)
    private Integer rankId;

    /** 段位名称，如 山稚 */
    @Column("rank_name")
    private String rankName;

    /** 小段：三段 / 二段 / 一段 */
    @Column("sub_rank")
    private String subRank;

    /** 升级到本段位满所需累计积分 */
    @Column("need_total_score")
    private Integer needTotalScore;

    /** 备注 */
    @Column("remark")
    private String remark;

    public Integer getRankId() { return rankId; }
    public void setRankId(Integer rankId) { this.rankId = rankId; }
    public String getRankName() { return rankName; }
    public void setRankName(String rankName) { this.rankName = rankName; }
    public String getSubRank() { return subRank; }
    public void setSubRank(String subRank) { this.subRank = subRank; }
    public Integer getNeedTotalScore() { return needTotalScore; }
    public void setNeedTotalScore(Integer needTotalScore) { this.needTotalScore = needTotalScore; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    /** 段位全称，如 山稚三段 */
    public String getFullName() {
        return rankName + subRank;
    }

    @Override
    public String toString() {
        return "GameRank{rankId=" + rankId + ", rankName='" + rankName + "', subRank='" + subRank
                + "', needTotalScore=" + needTotalScore + '}';
    }
}
