package com.cardgame.dao;

import com.cardgame.entity.Battle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 对局数据访问接口。
 */
@Repository
public interface BattleDao extends BaseDao<Battle> {

    /** 查询用户的对局历史 */
    List<Battle> findByUserId(Long userId);

    /** 查询房间当前的对局 */
    Battle findByRoomId(Long roomId);
}
