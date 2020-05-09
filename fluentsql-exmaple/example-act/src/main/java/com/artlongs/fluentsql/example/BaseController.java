package com.artlongs.fluentsql.example;

import act.Act;
import act.conf.AppConfig;
import com.artlongs.fluentsql.core.DbUitls;
import org.osgl.$;
import org.osgl.mvc.annotation.Before;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

/**
 * Func :
 *
 * @author: leeton on 2019/6/28.
 */
public class BaseController {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private Sql2o sql2o;
    //
    AppConfig conf = Act.appConfig();

    //h2
   /* String driver = conf.get("db.h2.driver").toString();
    String url = conf.get("db.h2.url").toString();
    String username = conf.get("db.h2.username").toString();
    String password = conf.get("db.h2.password").toString();
    Integer maxpoolsize = $.convert(conf.get("hikari.maxpoolsize")).toInt();
    Integer minidle = $.convert(conf.get("hikari.minidle")).toInt();*/

    //mysql
    String driver = conf.get("db.default.driver").toString();
    String url = conf.get("db.default.url").toString();
    String username = conf.get("db.default.username").toString();
    String password = conf.get("db.default.password").toString();
    Integer maxpoolsize = $.convert(conf.get("hikari.maxpoolsize")).toInt();
    Integer minidle = $.convert(conf.get("hikari.minidle")).toInt();

    @Before
    public void createTable() {
        getJdbc().getJdbcTemplate().execute("DROP TABLE IF EXISTS `user`");

        String sql = "CREATE TABLE `user`\n" +
                "(\n" +
                "\tid BIGINT(20) NOT NULL COMMENT '主键ID',\n" +
                "\tdept_id BIGINT(20) NOT NULL COMMENT '部门ID',\n" +
                "\tuser_name VARCHAR(30) NULL DEFAULT NULL COMMENT '名称',\n" +
                "\tage INT(3) NULL DEFAULT NULL COMMENT '年龄',\n" +
                "\tmoney DECIMAL (13,3) NULL DEFAULT NULL COMMENT 'DECIMAL',\n" +
                "\trole INT(2) NULL DEFAULT NULL COMMENT '测试',\n" +
                "\tphone VARCHAR(13) NULL DEFAULT NULL COMMENT '手机号码',\n" +
                "\tcreate_time DATETIME NULL DEFAULT NULL COMMENT '日期',\n" +
                "\tPRIMARY KEY (id)\n" +
                ")";
        getJdbc().getJdbcTemplate().execute(sql);

        String inset = "INSERT INTO user (id, dept_id, user_name, age, money, role, phone,create_time) VALUES\n" +
                "(0, 1, '雷锋', 18, 100, 1, '10010','2017-1-1 1:1:1'),\n" +
                "(1, 1, '三毛', 28, 100, 1, '10086','2017-1-1 1:1:1'),\n" +
                "(2, 1, '小马', 18, 100, 1, '10000','2017-1-1 1:1:1'),\n" +
                "(3, 2, '麻花', 18, 100, 1, '10000','2017-1-1 1:1:1'),\n" +
                "(4, 2, '东狗', 28, 100, 1, '10086','2017-1-1 1:1:1'),\n" +
                "(5, 1, '王五', 28, 100, 1, '10010','2017-1-1 1:1:1')";
        getJdbc().getJdbcTemplate().execute(inset);
    }


    public NamedParameterJdbcTemplate getJdbc() {
        if (null == jdbcTemplate) {
            AppConfig conf = Act.appConfig();
            DataSource dataSource = DbUitls.getHikariDataSource(url, username, password, driver, maxpoolsize, minidle);
            jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

    public Sql2o getSql2o() {
        if (null == sql2o) {
            DataSource dataSource = DbUitls.getHikariDataSource(url, username, password, driver, maxpoolsize, minidle);
            sql2o = new Sql2o(dataSource);
        }
        return sql2o;
    }
}
