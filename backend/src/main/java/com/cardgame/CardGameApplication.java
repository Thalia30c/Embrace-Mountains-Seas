package com.cardgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 山海卡牌游戏后端启动类，负责扫描 com.cardgame 下的 DAO / Service / Controller 组件。
 */
@SpringBootApplication
public class CardGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardGameApplication.class, args);
    }
}
