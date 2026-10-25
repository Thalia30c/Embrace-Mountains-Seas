package com.cardgame.controller;

import com.cardgame.common.PageResult;
import com.cardgame.common.Result;
import com.cardgame.dto.UserCardVO;
import com.cardgame.entity.Card;
import com.cardgame.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 卡牌图鉴接口。
 */
@RestController
@RequestMapping("/card")
public class CardController {

    @Autowired
    private CardService cardService;

    /** 分页查询卡牌图鉴 */
    @GetMapping("/page")
    public Result<PageResult<Card>> page(@RequestParam(required = false) String type,
                                          @RequestParam(required = false) String rarity,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return Result.ok(cardService.pageCards(type, rarity, page, size));
    }

    /** 全部卡牌 */
    @GetMapping("/list")
    public Result<List<Card>> list() {
        return Result.ok(cardService.listAll());
    }

    /** 卡牌详情 */
    @GetMapping("/{id}")
    public Result<Card> detail(@PathVariable Long id) {
        return Result.ok(cardService.getById(id));
    }

    /** 我拥有的卡牌 */
    @GetMapping("/mine")
    public Result<List<UserCardVO>> mine(@RequestAttribute("userId") Long userId) {
        return Result.ok(cardService.listUserCards(userId));
    }
}
