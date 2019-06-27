package com.artlongs.fluentsql.core;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DbUitls {
    private String protocol;
    private String dbType;
    private String provider;
    private String ip;
    private String port;
    private String dbName;
    private String driverClassName;

    public DbUitls() {
    }

    public static DbUitls parser(String dburl) {
        DbUitls urlInfo = new DbUitls();
        String url = dburl.toLowerCase();
        String[] urlbox = dburl.split(":");
        if(url.contains("oracle")) {
            //jdbc:oracle:thin:@localhost:1521:orcl
            urlInfo.protocol = urlbox[0];
            urlInfo.provider = urlbox[1];
            urlInfo.dbType = urlbox[2];
            urlInfo.ip = urlbox[3].replace("@","");
            urlInfo.port = urlbox[4];
            urlInfo.dbName = urlbox[5];
            urlInfo.driverClassName = DbType.itemOf(urlInfo.provider).className;
        }else if(url.contains("microsoft")){
            //jdbc:microsoft:sqlserver://<server_name>:<port>
            urlInfo.protocol = urlbox[0];
            urlInfo.provider = urlbox[1];
            urlInfo.dbType = urlbox[2];
            urlInfo.ip = urlbox[3].replace("//","");
            urlInfo.port = urlbox[4];
            urlInfo.dbName = urlbox[5];
            urlInfo.driverClassName = DbType.itemOf(urlInfo.dbType).className;
        } else if(url.contains("h2") && url.contains("mem")){
            //jdbc:h2:mem:test
            urlInfo.protocol = urlbox[0];
            urlInfo.dbType = urlbox[1];
            urlInfo.ip = "";
            urlInfo.port = "";
            urlInfo.dbName = urlbox[3];
            urlInfo.driverClassName = DbType.itemOf(urlInfo.dbType).className;
        } else {
            urlInfo.protocol = urlbox[0];
            urlInfo.dbType = urlbox[1];
            urlInfo.ip = urlbox[2];
            urlInfo.port = urlbox[3];
            urlInfo.dbName = urlbox[4];
            urlInfo.driverClassName = DbType.itemOf(urlInfo.dbType).className;
        }
        return urlInfo;
    }

    public static DataSource getHikariDataSource(String url, String username, String pwd, String driverClassName, int maxPoolSize, int minIdle){
        HikariDataSource datasource = new HikariDataSource();
        datasource.setJdbcUrl(url);
        if(null == driverClassName || "".equals(driverClassName)){
            DbUitls dbUitls = DbUitls.parser(url);
            driverClassName = dbUitls.getDriverClassName();
        }
        datasource.setDriverClassName(driverClassName);
        datasource.setUsername(username);
        datasource.setPassword(pwd);
        datasource.setMaximumPoolSize(maxPoolSize);
        datasource.setMinimumIdle(minIdle);
        return datasource;
    }

    public enum DbType{
        NOTFOUND("",""),
        MYSQL("mysql","com.mysql.jdbc.Driver"),
        PGSQL("postgresql","org.postgresql.Driver"),
        H2("h2","org.h2.Driver"),
        ORACLE("oracle","oracle.jdbc.driver.OracleDriver"),
        MSSQL("sqlserver","com.microsoft.jdbc.sqlserver.SQLServerDriver"),
        DB2("mysql","com.ibm.db2.jdbc.app.DB2Driver"),
        ;
        public String type;
        public String className;

        DbType(String type, String className) {
            this.type = type;
            this.className = className;
        }

        public static DbType itemOf(String type) {
            for (DbType item : DbType.values()) {
                if(item.type.equalsIgnoreCase(type)) return item;
            }
            return NOTFOUND;
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
}