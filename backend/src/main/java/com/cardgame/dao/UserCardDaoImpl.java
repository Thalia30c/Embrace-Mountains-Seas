package com.cardgame.dao;

import com.cardgame.entity.UserCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户卡牌收藏数据访问实现。
 */
@Repository
public class UserCardDaoImpl extends BaseDaoImpl<UserCard> implements UserCardDao {

    public UserCardDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, UserCard.class);
    }

    @Override
    public List<UserCard> findByUserId(Long userId) {
        return selectList(QueryWrapper.<UserCard>create()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    @Override
    public UserCard findByUserIdAndCardId(Long userId, Long cardId) {
        return selectOne(QueryWrapper.<UserCard>create()
                .eq("user_id", userId)
                .eq("card_id", cardId));
    }

    @Override
    public List<Long> findCardIdsByUserId(Long userId) {
        return queryMaps("SELECT card_id FROM t_user_card WHERE user_id = ?", userId)
                .stream()
                .map(m -> ((Number) m.get("card_id")).longValue())
                .toList();
    }
}
