package fluentsql.jdbc;

import com.artlongs.fluentsql.core.DbUitls;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Func :
 *
 * @author: leeton on 2019/6/27.
 */
public class JdbcBuilder {

    private static NamedParameterJdbcTemplate jdbcTemplate;

    public static NamedParameterJdbcTemplate buildOfHikariCP(String url, String username, String pwd, String driverClassName, int maxPoolSize, int minIdle) {//使用外部的数据库连接池
        if (null == jdbcTemplate) {
            DataSource source = DbUitls.getHikariDataSource(url, username, pwd, driverClassName, maxPoolSize, minIdle);
            jdbcTemplate = new NamedParameterJdbcTemplate(source);
        }
        return jdbcTemplate;
    }

    public static NamedParameterJdbcTemplate buildOf(DataSource dataSource) {
        if (null == jdbcTemplate) {
            jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }
}
