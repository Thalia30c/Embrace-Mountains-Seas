package com.cardgame.dao;

import com.cardgame.entity.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 房间数据访问实现。
 */
@Repository
public class RoomDaoImpl extends BaseDaoImpl<Room> implements RoomDao {

    public RoomDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, Room.class);
    }

    @Override
    public Room findByRoomNo(String roomNo) {
        return selectOne(QueryWrapper.<Room>create().eq("room_no", roomNo));
    }

    @Override
    public List<Room> findWaitingRooms() {
        return selectList(QueryWrapper.<Room>create()
                .eq("status", "waiting")
                .orderByDesc("id"));
    }

    @Override
    public List<Room> findByOwnerId(Long ownerId) {
        return selectList(QueryWrapper.<Room>create()
                .eq("owner_id", ownerId)
                .orderByDesc("id"));
    }
}
