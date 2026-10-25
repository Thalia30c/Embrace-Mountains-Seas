package com.cardgame.dao;

import com.cardgame.entity.Deck;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 卡组数据访问接口。
 */
@Repository
public interface DeckDao extends BaseDao<Deck> {

    /** 查询用户的全部牌组 */
    List<Deck> findByUserId(Long userId);

    /** 查询用户的默认牌组 */
    Deck findDefaultByUserId(Long userId);
}
