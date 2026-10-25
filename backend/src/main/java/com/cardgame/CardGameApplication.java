package com.cardgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 卡牌竞技游戏后端启动类。
 *
 * <p>在 IntelliJ IDEA 中直接运行 main 方法即可启动，默认端口 8080。</p>
 *
 * <p>默认走内存模式（card.data-source=memory），不需要数据库即可启动。
 * 需要连数据库时启动参数加 --card.data-source=mysql 即可。</p>
 */
@SpringBootApplication
public class CardGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardGameApplication.class, args);
    }
}
