package com.artlongs.fluentsql;

import org.sql2o.Sql2o;

import javax.sql.DataSource;

/**
 * Func :
 *
 * @author: leeton on 2019/6/26.
 */
public class Sql2OBuilder {
    private static Sql2o sql2o =  null;

    /**
     * 使用 SQL2O 自己的数据库连接池,来创建 SQL2O
     * @param jdbcurl
     * @param username
     * @param pwd
     * @return
     */
    public static Sql2o build(String jdbcurl,String username,String pwd) { //使用 sql2o 自己的数据库连接池
        //new Sql2o("jdbc:postgresql://localhost/Sql2oTestDb", "root", "123456");
        if (null == sql2o) {
            sql2o = new Sql2o(jdbcurl, username, pwd);
        }
        return sql2o;
    }

    /**
     * 使用外部的数据库连接池
     * @param dataSource
     * @return
     */
    public static Sql2o build(DataSource dataSource) {//使用外部的数据库连接池
        if (null == sql2o) {
            sql2o = new Sql2o(dataSource);
        }
        return sql2o;
    }

/*    private DataSource getDataSource(){ // 创建数据源
        HikariDataSource datasource = new HikariDataSource();
        datasource.setJdbcUrl(propertyResolver.getProperty("url"));
        datasource.setDriverClassName(propertyResolver.getProperty("driver-class-name"));
        datasource.setUsername(propertyResolver.getProperty("username"));
        datasource.setPassword(propertyResolver.getProperty("password"));
        datasource.setMaximumPoolSize(Integer.valueOf(propertyResolver.getProperty("hikari.maximum-pool-size")));
        datasource.setMinimumIdle(Integer.valueOf(propertyResolver.getProperty("hikari.minimum-idle")));
    }*/


}
