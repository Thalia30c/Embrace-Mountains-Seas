package com.cardgame.dao;

import com.cardgame.entity.BattleDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 对局明细数据访问接口。
 */
@Repository
public interface BattleDetailDao extends BaseDao<BattleDetail> {

    /** 查询对局的全部操作明细 */
    List<BattleDetail> findByBattleId(Long battleId);
}
