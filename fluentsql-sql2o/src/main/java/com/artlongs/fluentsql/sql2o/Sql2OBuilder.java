package com.artlongs.fluentsql.sql2o;

import com.artlongs.fluentsql.core.DbUitls;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

/**
 * Func : 创建 Sql2o
 *
 * @author: leeton on 2019/6/26.
 */
public class Sql2OBuilder {

    /**
     * 使用 SQL2O 自己的数据库连接池,来创建 SQL2O
     * @param jdbcurl
     * @param username
     * @param pwd
     * @return
     */
    public static Sql2o build(String jdbcurl,String username,String pwd) {
        return new Sql2o(jdbcurl, username, pwd);
    }

    /**
     * 使用外部的数据库连接池
     * @param dataSource
     * @return
     */
    public static Sql2o build(DataSource dataSource) {
        return new Sql2o(dataSource);
    }

    /**
     * 使用Hikari数据库连接池
     * @param url
     * @param username
     * @param pwd
     * @param driverClassName
     * @param maxPoolSize
     * @param minIdle
     * @return
     */
    public static Sql2o buildOfHikariCP(String url,String username,String pwd,String driverClassName,int maxPoolSize,int minIdle) {
        DataSource source = DbUitls.getHikariDataSource(url, username, pwd, driverClassName,maxPoolSize, minIdle);
        return new Sql2o(source);
    }

}
