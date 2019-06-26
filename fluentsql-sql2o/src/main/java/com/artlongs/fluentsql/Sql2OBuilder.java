package com.artlongs.fluentsql;

import com.artlongs.fluentsql.core.DbUrlParser;
import com.zaxxer.hikari.HikariDataSource;
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
    public static Sql2o buildOfHikariCP(String url,String username,String pwd,String driverClassName,int maxPoolSize,int minIdle) {//使用外部的数据库连接池
        if (null == sql2o) {
            DataSource source = getHikariDataSource(url, username, pwd, driverClassName,maxPoolSize, minIdle);
            sql2o = new Sql2o(source);
        }
        return sql2o;
    }

    private static DataSource getHikariDataSource(String url,String username,String pwd,String driverClassName,int maxPoolSize,int minIdle){
        HikariDataSource datasource = new HikariDataSource();
        datasource.setJdbcUrl(url);
        if(null == driverClassName || "".equals(driverClassName)){
            DbUrlParser dbUrlParser = DbUrlParser.parser(url);
            driverClassName = dbUrlParser.getDriverClassName();
        }
        datasource.setDriverClassName(driverClassName);
        datasource.setUsername(username);
        datasource.setPassword(pwd);
        datasource.setMaximumPoolSize(maxPoolSize);
        datasource.setMinimumIdle(minIdle);
        return datasource;
    }


}
