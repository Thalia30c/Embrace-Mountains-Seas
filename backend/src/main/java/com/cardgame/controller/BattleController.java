package com.cardgame.controller;

import com.cardgame.common.Result;
import com.cardgame.dto.PlayCardDTO;
import com.cardgame.entity.BattleDetail;
import com.cardgame.service.BattleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 对战接口。
 */
@RestController
@RequestMapping("/battle")
public class BattleController {

    @Autowired
    private BattleService battleService;

    /** 对局实时状态 */
    @GetMapping("/{id}/state")
    public Result<Map<String, Object>> state(@RequestAttribute("userId") Long userId,
                                             @PathVariable Long id) {
        return Result.ok(battleService.getBattleState(id, userId));
    }

    /** 出牌 */
    @PostMapping("/play")
    public Result<Void> play(@RequestAttribute("userId") Long userId,
                             @Valid @RequestBody PlayCardDTO dto) {
        battleService.playCard(userId, dto);
        return Result.ok();
    }

    /** 抽牌 */
    @PostMapping("/{id}/draw")
    public Result<Map<String, Object>> draw(@RequestAttribute("userId") Long userId,
                                            @PathVariable Long id) {
        return Result.ok(battleService.drawCard(userId, id));
    }

    /** 过牌 */
    @PostMapping("/{id}/pass")
    public Result<Void> pass(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        battleService.pass(userId, id);
        return Result.ok();
    }

    /** 认输 */
    @PostMapping("/{id}/surrender")
    public Result<Void> surrender(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        battleService.surrender(userId, id);
        return Result.ok();
    }

    /** 对局回放明细 */
    @GetMapping("/{id}/details")
    public Result<List<BattleDetail>> details(@PathVariable Long id) {
        return Result.ok(battleService.getDetails(id));
    }
}
