package com.artlongs.fluentsql.example;

import com.artlongs.fluentsql.core.DbUitls;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

/**
 * Func :
 *
 * @author: leeton on 2020/9/27.
 */
//@Component
public class Sql2oConn implements ImportBeanDefinitionRegistrar,EnvironmentAware {

    Environment env;
    private DataSource dataSource;
    private Sql2o sql2o;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
//        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(DataSourceTransactionManager.class);
//        BeanDefinitionBuilder bdb1 = BeanDefinitionBuilder.rootBeanDefinition(Sql2oConn.class);
        BeanDefinitionBuilder bdb2 = BeanDefinitionBuilder.rootBeanDefinition(HikariDataSource.class);
//        dataSource = master(env);
//        bdb.addPropertyValue("dataSource",dataSource);
//        bdb1.addPropertyValue("dataSource",dataSource);
//        bdb2.addPropertyValue("dataSource",dataSource);
//        registry.registerBeanDefinition("dataSourceTransactionManager", bdb.getBeanDefinition());
//        registry.registerBeanDefinition("sql2oConn", bdb1.getBeanDefinition());
//        registry.registerBeanDefinition("dataSource", bdb2.getBeanDefinition());
    }

    private DataSource master(Environment env) {
        String proPfix = "spring.datasource.";
        String url = env.getProperty(proPfix + "url");
        String username = env.getProperty(proPfix + "username");
        String password = env.getProperty(proPfix + "password");
        String dcn = env.getProperty(proPfix + "driver-class-name");
        int maxPoolSize = getInt(env.getProperty(proPfix + "hikari.maximum-pool-size"), 10);
        int minIdle = getInt(env.getProperty(proPfix + "hikari.minimum-idle"), 5);
        dataSource = DbUitls.getHikariDataSource(url, username, password, dcn, maxPoolSize, minIdle);
        this.sql2o = new Sql2o(dataSource);
        return dataSource;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    private int getInt(Object s, int def) {
        if (null == s) return def;
        return Integer.valueOf("" + s);
    }

    public Sql2o getSql2o() {
        master(env);
        return sql2o;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
