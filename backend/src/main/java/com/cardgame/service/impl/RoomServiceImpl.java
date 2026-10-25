package com.cardgame.service.impl;

import com.cardgame.common.BizException;
import com.cardgame.common.ResultCode;
import com.cardgame.dao.BattleDao;
import com.cardgame.dao.QueryWrapper;
import com.cardgame.dao.RoomDao;
import com.cardgame.dto.CreateRoomDTO;
import com.cardgame.entity.Battle;
import com.cardgame.entity.Room;
import com.cardgame.entity.User;
import com.cardgame.service.BattleService;
import com.cardgame.service.RoomService;
import com.cardgame.service.UserService;
import com.cardgame.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间服务实现。
 *
 * <p>房间成员暂存在内存中（单机演示够用）；正式部署建议换成 Redis 或单独的房间成员表。</p>
 */
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private BattleDao battleDao;

    @Autowired
    private BattleService battleService;

    @Autowired
    private UserService userService;

    /** roomId -> 成员 userId 集合 */
    private final Map<Long, Set<Long>> members = new ConcurrentHashMap<>();

    @Override
    public List<Room> listRooms() {
        return roomDao.findWaitingRooms();
    }

    @Override
    public Room createRoom(Long userId, CreateRoomDTO dto) {
        User user = userService.getById(userId);
        Room room = new Room();
        room.setRoomNo(generateUniqueRoomNo());
        room.setOwnerId(userId);
        room.setOwnerName(user.getNickname());
        room.setName(dto.getName() == null || dto.getName().isBlank()
                ? user.getNickname() + "的房间" : dto.getName());
        room.setMode(dto.getMode() == null ? "casual" : dto.getMode());
        room.setStatus("waiting");
        room.setMaxPlayer(dto.getMaxPlayer() == null ? 2 : Math.min(dto.getMaxPlayer(), 4));
        room.setCurrentPlayer(1);
        room.setPassword(dto.getPassword());
        roomDao.insert(room);

        members.computeIfAbsent(room.getId(), k -> ConcurrentHashMap.newKeySet()).add(userId);
        return room;
    }

    @Override
    public Room joinRoom(Long userId, String roomNo, String password) {
        Room room = roomDao.findByRoomNo(roomNo);
        if (room == null) {
            throw new BizException(ResultCode.NOT_FOUND, "房间不存在");
        }
        if (!"waiting".equals(room.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "房间已开局");
        }
        if (room.getPassword() != null && !room.getPassword().isBlank()
                && !room.getPassword().equals(password)) {
            throw new BizException(ResultCode.PARAM_ERROR, "房间密码错误");
        }
        Set<Long> set = members.computeIfAbsent(room.getId(), k -> ConcurrentHashMap.newKeySet());
        if (set.contains(userId)) {
            return room;
        }
        if (set.size() >= room.getMaxPlayer()) {
            throw new BizException(ResultCode.ROOM_FULL);
        }
        set.add(userId);
        room.setCurrentPlayer(set.size());
        roomDao.updateById(room);
        return room;
    }

    @Override
    public void leaveRoom(Long userId, Long roomId) {
        Room room = roomDao.selectById(roomId);
        if (room == null) {
            throw new BizException(ResultCode.NOT_FOUND, "房间不存在");
        }
        Set<Long> set = members.get(roomId);
        if (set != null) {
            set.remove(userId);
            room.setCurrentPlayer(set.size());
        }
        // 房主离开则解散房间
        if (room.getOwnerId().equals(userId)) {
            members.remove(roomId);
            room.setStatus("finished");
            roomDao.updateById(room);
            return;
        }
        roomDao.updateById(room);
    }

    @Override
    public Long startBattle(Long userId, Long roomId) {
        Room room = roomDao.selectById(roomId);
        if (room == null) {
            throw new BizException(ResultCode.NOT_FOUND, "房间不存在");
        }
        if (!room.getOwnerId().equals(userId)) {
            throw new BizException(ResultCode.NOT_ROOM_OWNER);
        }
        Set<Long> set = members.get(roomId);
        if (set == null || set.size() < 2) {
            throw new BizException(ResultCode.CONFLICT, "至少需要 2 名玩家才能开局");
        }
        if (!"waiting".equals(room.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "房间已开局");
        }

        Long battleId = battleService.startBattle(roomId, set);
        room.setStatus("playing");
        room.setBattleId(battleId);
        roomDao.updateById(room);
        return battleId;
    }

    @Override
    public Room getRoom(Long roomId) {
        Room room = roomDao.selectById(roomId);
        if (room == null) {
            throw new BizException(ResultCode.NOT_FOUND, "房间不存在");
        }
        return room;
    }

    /** 房间成员 id 列表 */
    public Set<Long> getMembers(Long roomId) {
        return members.getOrDefault(roomId, Set.of());
    }

    /** 生成不重复的 6 位房间号 */
    private String generateUniqueRoomNo() {
        for (int i = 0; i < 20; i++) {
            String no = CommonUtil.generateRoomNo();
            if (roomDao.findByRoomNo(no) == null) {
                return no;
            }
        }
        throw new BizException(ResultCode.SERVER_ERROR, "房间号生成失败，请稍后重试");
    }
}
