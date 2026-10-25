package com.cardgame.dao;

import com.cardgame.entity.BattleDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对局明细数据访问实现。
 */
@Repository
public class BattleDetailDaoImpl extends BaseDaoImpl<BattleDetail> implements BattleDetailDao {

    public BattleDetailDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, BattleDetail.class);
    }

    @Override
    public List<BattleDetail> findByBattleId(Long battleId) {
        return selectList(QueryWrapper.<BattleDetail>create()
                .eq("battle_id", battleId)
                .orderByAsc("round")
                .orderByAsc("id"));
    }
}
