package com.cardgame.dao;

import com.cardgame.entity.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 房间数据访问接口。
 */
@Repository
public interface RoomDao extends BaseDao<Room> {

    /** 按房间号查询 */
    Room findByRoomNo(String roomNo);

    /** 查询大厅房间列表（等待中的房间） */
    List<Room> findWaitingRooms();

    /** 查询用户创建的房间 */
    List<Room> findByOwnerId(Long ownerId);
}
