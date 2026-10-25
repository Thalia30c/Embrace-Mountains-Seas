package com.cardgame.dao;

import com.cardgame.entity.Battle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对局数据访问实现。
 */
@Repository
public class BattleDaoImpl extends BaseDaoImpl<Battle> implements BattleDao {

    public BattleDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Battle.class);
    }

    @Override
    public List<Battle> findByUserId(Long userId) {
        // 参与过该房间即视为参与过对局，通过房间 owner 或明细表反查
        return selectList(QueryWrapper.<Battle>create()
                .eq("winner_id", userId)
                .orderByDesc("start_time"));
    }

    @Override
    public Battle findByRoomId(Long roomId) {
        return selectOne(QueryWrapper.<Battle>create()
                .eq("room_id", roomId)
                .orderByDesc("id"));
    }
}
