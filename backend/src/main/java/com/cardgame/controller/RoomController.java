package com.cardgame.controller;

import com.cardgame.common.Result;
import com.cardgame.dto.CreateRoomDTO;
import com.cardgame.entity.Room;
import com.cardgame.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 房间接口：大厅、创建、加入、离开、开局。
 */
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    /** 大厅房间列表 */
    @GetMapping("/list")
    public Result<List<Room>> list() {
        return Result.ok(roomService.listRooms());
    }

    /** 创建房间 */
    @PostMapping("/create")
    public Result<Room> create(@RequestAttribute("userId") Long userId,
                               @Valid @RequestBody CreateRoomDTO dto) {
        return Result.ok(roomService.createRoom(userId, dto));
    }

    /** 通过房间号加入 */
    @PostMapping("/join")
    public Result<Room> join(@RequestAttribute("userId") Long userId,
                             @RequestParam String roomNo,
                             @RequestParam(required = false) String password) {
        return Result.ok(roomService.joinRoom(userId, roomNo, password));
    }

    /** 离开房间 */
    @PostMapping("/{id}/leave")
    public Result<Void> leave(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        roomService.leaveRoom(userId, id);
        return Result.ok();
    }

    /** 房主开始对局 */
    @PostMapping("/{id}/start")
    public Result<Long> start(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        return Result.ok(roomService.startBattle(userId, id));
    }

    /** 房间详情 */
    @GetMapping("/{id}")
    public Result<Room> detail(@PathVariable Long id) {
        return Result.ok(roomService.getRoom(id));
    }
}
