import com.cardgame.dao.*;
import com.cardgame.dao.impl.*;
import com.cardgame.entity.*;
import com.mysql.cj.jdbc.Driver;
import com.cardgame.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.List;

public class DaoSmoke {

    public static void main(String[] args) throws Exception {
        String password = System.getenv("DB_PASSWORD") == null ? "" : System.getenv("DB_PASSWORD");
        SimpleDriverDataSource ds = new SimpleDriverDataSource(
                new Driver(),
                "jdbc:mysql://localhost:3306/mountains_and_seas?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
                System.getenv("DB_USERNAME") == null ? "root" : System.getenv("DB_USERNAME"), password);
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        UserDao userDao = new UserDaoImpl(jdbc);
        CardDao cardDao = new CardDaoImpl(jdbc);
        WarehouseDao warehouseDao = new WarehouseDaoImpl(jdbc);
        GameRankDao gameRankDao = new GameRankDaoImpl(jdbc);
        PassiveSkillDao skillDao = new PassiveSkillDaoImpl(jdbc);

        System.out.println("user count=" + userDao.count());
        System.out.println("findByAccount=" + userDao.findByAccount("zhangsan"));
        System.out.println("existsByAccount(lisi)=" + userDao.existsByAccount("lisi"));
        System.out.println("top2=" + userDao.findTopByScore(2));

        System.out.println("card count=" + cardDao.count());
        System.out.println("card by level 玄=" + cardDao.findByLevel("玄").size());
        System.out.println("compound=" + cardDao.findCompoundCards());
        System.out.println("counterTo(12)=" + cardDao.findCounterTo(12));
        System.out.println("chain(1,5)=" + cardDao.findCounterChain(1, 5));
        System.out.println("card 1=" + cardDao.selectById(1));

        System.out.println("rank by score 700=" + gameRankDao.findRankByScore(700));
        System.out.println("next rank 6=" + gameRankDao.findNextRank(6));
        System.out.println("ranks=" + gameRankDao.findAllOrderByRankId().size());

        System.out.println("skill P01=" + skillDao.findByCode("P01"));
        System.out.println("skills target S=" + skillDao.findByTarget("S"));

        System.out.println("warehouse 1=" + warehouseDao.selectById(1));
        System.out.println("cardIds(1)=" + warehouseDao.findCardIds(1));

        PageResult<Card> page = cardDao.selectPage(2, 10);
        System.out.println("page total=" + page.getTotal() + " records=" + page.getRecords().size() + " totalPage=" + page.getTotalPage());

        List<Card> bySql = cardDao.selectBySql("SELECT * FROM `card` WHERE `hp` >= ? ORDER BY `hp` DESC LIMIT ?", 14, 3);
        System.out.println("bySql=" + bySql);

        int n = userDao.addScore(1, 10);
        System.out.println("addScore rows=" + n + " score=" + userDao.selectById(1).getScore());
        userDao.addScore(1, -10);

        Warehouse wh = new Warehouse();
        wh.setCardList("1,2,3");
        int rows = warehouseDao.insert(wh);
        System.out.println("insert warehouse rows=" + rows + " newId=" + wh.getWarehouseId());
        wh.setCardList("1,2,3,24");
        warehouseDao.updateById(wh);
        System.out.println("after update=" + warehouseDao.findCardIds(wh.getWarehouseId()));
        warehouseDao.appendCard(wh.getWarehouseId(), 5);
        System.out.println("after append=" + warehouseDao.findCardIds(wh.getWarehouseId()));
        warehouseDao.removeCard(wh.getWarehouseId(), 2);
        System.out.println("after remove=" + warehouseDao.findCardIds(wh.getWarehouseId()));
        System.out.println("delete rows=" + warehouseDao.deleteById(wh.getWarehouseId()));
        System.out.println("SMOKE OK");
    }
}
