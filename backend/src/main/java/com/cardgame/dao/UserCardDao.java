package com.cardgame.dao;

import com.cardgame.entity.UserCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 用户卡牌收藏数据访问接口。
 */
@Repository
public interface UserCardDao extends BaseDao<UserCard> {

    /** 查询用户拥有的全部卡牌记录 */
    List<UserCard> findByUserId(Long userId);

    /** 查询用户对某张卡的收藏记录 */
    UserCard findByUserIdAndCardId(Long userId, Long cardId);

    /** 查询用户收藏的卡牌 id 列表 */
    List<Long> findCardIdsByUserId(Long userId);
}
