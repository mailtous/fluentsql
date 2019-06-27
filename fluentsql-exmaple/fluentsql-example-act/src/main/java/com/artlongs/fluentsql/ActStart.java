package com.artlongs.fluentsql;


import act.Act;
import act.conf.AppConfig;
import com.artlongs.fluentsql.core.DbUitls;
import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import fluentsql.jdbc.Lq;
import org.osgl.$;
import org.osgl.mvc.annotation.GetAction;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Func :
 *
 * @author: leeton on 2019/4/1.
 */
public class ActStart {


    private NamedParameterJdbcTemplate jdbcTemplate;

    @GetAction("/test/lq/user/{id}")
    public User getUserById(Integer id) {
        Query lq = new Lq<User>(User.class, getJdbc()).andEq(User::getId, id);
        User user = lq.to();
        return user;
    }

    @GetAction("/test/lq/adduser")
    public User addUser() {
        User user = new User();
        user.setId(8);
        user.setUserName("jack");
        user.setDeptId(1);
        user.setMoney(new BigDecimal(1000.22));
        user.setCreateTime(new Date());
        new Lq<User>(User.class, getJdbc()).toSave(user);

        return user;
    }

    public NamedParameterJdbcTemplate getJdbc() {
        if (null == jdbcTemplate) {
            AppConfig conf = Act.appConfig();

/*        db.h2.driver=org.h2.Driver
        db.h2.url: jdbc:h2:mem:test
        db.h2.schema: classpath:/db/schema-h2.sql
        db.h2.data: classpath:/db/data-h2.sql
        db.h2.username: sa
        db.h2.password:*/

            //h2
/*        String driver = conf.get("db.h2.driver").toString();
        String url = conf.get("db.h2.url").toString();
        String username = conf.get("db.h2.username").toString();
        String password = conf.get("db.h2.password").toString();
        int maxpoolsize = $.convert(conf.get("hikari.maxpoolsize")).toInt();
        int minidle = $.convert(conf.get("hikari.minidle")).toInt();*/
            //mysql
            String driver = conf.get("db.default.driver").toString();
            String url = conf.get("db.default.url").toString();
            String username = conf.get("db.default.username").toString();
            String password = conf.get("db.default.password").toString();
            //hikari
            int maxpoolsize = $.convert(conf.get("hikari.maxpoolsize")).toInt();
            int minidle = $.convert(conf.get("hikari.minidle")).toInt();

            DataSource dataSource = DbUitls.getHikariDataSource(url, username, password, driver, maxpoolsize, minidle);
            jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Act.start("fluent sql example", "com.artlongs");
    }
}
