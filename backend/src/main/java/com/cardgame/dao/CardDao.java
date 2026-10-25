package com.cardgame.dao;

import com.cardgame.entity.Card;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 卡牌图鉴数据访问接口。
 */
@Repository
public interface CardDao extends BaseDao<Card> {

    /** 按类型过滤 */
    List<Card> findByType(String type);

    /** 按稀有度过滤 */
    List<Card> findByRarity(String rarity);

    /** 按 id 集合批量查询 */
    List<Card> findByIds(List<Long> ids);
}
