package com.cardgame.controller;

import com.cardgame.common.Result;
import com.cardgame.entity.Deck;
import com.cardgame.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 牌组接口。
 */
@RestController
@RequestMapping("/deck")
public class DeckController {

    @Autowired
    private DeckService deckService;

    /** 我的牌组列表（含卡牌明细） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestAttribute("userId") Long userId) {
        return Result.ok(deckService.listMyDecks(userId));
    }

    /** 创建牌组 */
    @PostMapping("/create")
    public Result<Deck> create(@RequestAttribute("userId") Long userId,
                               @RequestParam(required = false) String name) {
        return Result.ok(deckService.createDeck(userId, name));
    }

    /** 删除牌组 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        deckService.deleteDeck(userId, id);
        return Result.ok();
    }

    /** 设为默认牌组 */
    @PostMapping("/{id}/default")
    public Result<Deck> setDefault(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        return Result.ok(deckService.setDefault(userId, id));
    }

    /** 牌组加卡 */
    @PostMapping("/{id}/card")
    public Result<Void> addCard(@RequestAttribute("userId") Long userId,
                                @PathVariable Long id,
                                @RequestParam Long cardId) {
        deckService.addCard(userId, id, cardId);
        return Result.ok();
    }

    /** 牌组移除卡 */
    @DeleteMapping("/{id}/card/{cardId}")
    public Result<Void> removeCard(@RequestAttribute("userId") Long userId,
                                    @PathVariable Long id,
                                    @PathVariable Long cardId) {
        deckService.removeCard(userId, id, cardId);
        return Result.ok();
    }
}
