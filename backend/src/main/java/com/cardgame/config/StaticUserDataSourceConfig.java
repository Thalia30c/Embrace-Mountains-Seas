package com.cardgame.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 静态用户模式下的数据源配置。
 *
 * <p>静态模式下用户数据完全来自内存，数据库用不上。但 Spring Boot 默认的
 * DataSource 自动配置会在启动时创建连接池并连库，账号密码不对就会启动失败。</p>
 *
 * <p>这里提供一个「永不连库」的 DataSource：</p>
 * <ul>
 *     <li>启动时不产生任何数据库连接，账号密码没填也能启动</li>
 *     <li>若某处误用原生 SQL，会得到明确报错提示而不是连接超时</li>
 *     <li>切回数据库模式（card.use-static-user=false）时本配置不加载</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(name = "card.use-static-user", havingValue = "true", matchIfMissing = true)
public class StaticUserDataSourceConfig {

    /**
     * 不连库的 DataSource，仅用于满足 JdbcTemplate 的装配需求。
     */
    @Bean
    @Primary
    public DataSource staticModeDataSource() {
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException(
                        "当前为静态用户模式（card.use-static-user=true），不访问数据库。"
                                + "需要查库请把配置切回 false。");
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return getConnection();
            }

            @Override
            public PrintWriter getLogWriter() {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) {
                // 无需实现
            }

            @Override
            public void setLoginTimeout(int seconds) {
                // 无需实现
            }

            @Override
            public int getLoginTimeout() {
                return 0;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                throw new SQLFeatureNotSupportedException();
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                throw new SQLException("not a wrapper");
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) {
                return false;
            }
        };
    }

    /**
     * 基于上面 DataSource 的 JdbcTemplate，同样不会真的连库。
     */
    @Bean
    @Primary
    public JdbcTemplate staticModeJdbcTemplate(DataSource staticModeDataSource) {
        return new JdbcTemplate(staticModeDataSource);
    }
}
