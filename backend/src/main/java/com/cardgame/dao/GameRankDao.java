package com.cardgame.dao;

import com.cardgame.entity.GameRank;

import java.util.List;

/**
 * game_rank 表数据访问接口：继承 {@link BaseDao} 已有的通用 CRUD，这里处理段位换算相关的查询。
 */
public interface GameRankDao extends BaseDao<GameRank> {

    /** 按 rank_id 升序返回全部 19 个段位 */
    List<GameRank> findAllOrderByRankId();

    /** 根据累计积分反查所属段位：need_total_score 不超过 score 的最高段位 */
    GameRank findRankByScore(int score);

    /** 查询下一级段位，已是最高段位（山海尊）时返回 null */
    GameRank findNextRank(Integer rankId);
}
