package com.cardgame.service;

import com.cardgame.dto.CreateRoomDTO;
import com.cardgame.entity.Room;

import java.util.List;

/**
 * 房间服务：大厅列表、创建、加入、离开、开局。
 */
public interface RoomService {

    /** 大厅房间列表 */
    List<Room> listRooms();

    /** 创建房间 */
    Room createRoom(Long userId, CreateRoomDTO dto);

    /** 加入房间 */
    Room joinRoom(Long userId, String roomNo, String password);

    /** 离开房间 */
    void leaveRoom(Long userId, Long roomId);

    /** 房主开始对局，返回对局 id */
    Long startBattle(Long userId, Long roomId);

    /** 房间详情 */
    Room getRoom(Long roomId);
}
