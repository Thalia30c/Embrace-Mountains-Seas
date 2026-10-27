package com.cardgame.dao.impl;

import com.cardgame.dao.GameRankDao;
import com.cardgame.entity.GameRank;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * game_rank 表数据访问实现：通用能力来自 {@link BaseDaoImpl}，这里实现段位换算相关的查询。
 */
@Repository
public class GameRankDaoImpl extends BaseDaoImpl<GameRank> implements GameRankDao {

    public GameRankDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, GameRank.class);
    }

    @Override
    public List<GameRank> findAllOrderByRankId() {
        return selectAll("rank_id ASC");
    }

    @Override
    public GameRank findRankByScore(int score) {
        List<GameRank> list = selectBySql("SELECT " + meta().allColumnsSql() + " FROM " + table()
                + " WHERE `need_total_score` <= ? ORDER BY `rank_id` DESC LIMIT 1", score);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public GameRank findNextRank(Integer rankId) {
        List<GameRank> list = selectBySql("SELECT " + meta().allColumnsSql() + " FROM " + table()
                + " WHERE `rank_id` > ? ORDER BY `rank_id` ASC LIMIT 1", rankId);
        return list.isEmpty() ? null : list.get(0);
    }
}
