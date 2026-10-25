package com.cardgame.dao;

import com.cardgame.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 用户数据访问接口。
 *
 * <p>继承 {@link BaseDao} 获得通用增删改查能力，只额外声明业务特有的查询。</p>
 */
@Repository
public interface UserDao extends BaseDao<User> {

    /** 按用户名查询 */
    User findByUsername(String username);

    /** 按 openid 查询 */
    User findByOpenid(String openid);

    /** 排行榜：按胜场数降序取前 N 名 */
    List<User> findTopByWinCount(int limit);
}
