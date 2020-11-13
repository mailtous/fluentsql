package com.artlongs.fluentsql.example;

import com.artlongs.fluentsql.core.DbUitls;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.sql2o.Sql2o;

import javax.sql.DataSource;


//@ImportResource("classpath:tx.xml")
@Configuration
@EnableTransactionManagement
public class Sql2oConfig implements TransactionManagementConfigurer,EnvironmentAware {

    private Sql2o sql2o;
    private Environment env;
    private static DataSource dataSource;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (null == dataSource) {
            String proPfix = "spring.datasource.";
            String url = env.getProperty(proPfix + "url");
            String username = env.getProperty(proPfix + "username");
            String password = env.getProperty(proPfix + "password");
            String dcn = env.getProperty(proPfix + "driver-class-name");
            int maxPoolSize = getInt(env.getProperty(proPfix + "hikari.maximum-pool-size"), 10);
            int minIdle = getInt(env.getProperty(proPfix + "hikari.minimum-idle"), 5);
            dataSource = DbUitls.getHikariDataSource(url, username, password, dcn, maxPoolSize, minIdle);
        }
        return dataSource;
    }


    @Bean
    @Primary
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }


    @Bean
    public Sql2o sql2o() {
        return new Sql2o(dataSource());
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    /*    @Bean("transactionManager")
    public DataSourceTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }*/

    //事务拦截器
//    @Bean("txAdvice")
//    public TransactionInterceptor txAdvice(@Qualifier("transactionManager") DataSourceTransactionManager txManager){
//
//        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
//        //*只读事务，不做更新操作*//*
//        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
//        readOnlyTx.setReadOnly(true);
//        readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED );
//        //*当前存在事务就使用当前事务，当前不存在事务就创建一个新的事务*//*
//        RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED,
//                Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
//        requiredTx.setTimeout(30);
//        Map<String, TransactionAttribute> txMap = new HashMap<>();
//        txMap.put("add*", requiredTx);
//        txMap.put("save*", requiredTx);
//        txMap.put("insert*", requiredTx);
//        txMap.put("update*", requiredTx);
//        txMap.put("delete*", requiredTx);
//        txMap.put("get*", readOnlyTx);
//        txMap.put("query*", readOnlyTx);
//        source.setNameMap( txMap );
//        return new TransactionInterceptor(txManager ,source) ;
//    }

    /*

     *//**切面拦截规则 参数会自动从容器中注入*//*
    @Bean
    public DefaultPointcutAdvisor defaultPointcutAdvisor(TransactionInterceptor txAdvice){
        DefaultPointcutAdvisor pointcutAdvisor = new DefaultPointcutAdvisor();
        pointcutAdvisor.setAdvice(txAdvice);
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution (* com.artlongs.*.*(..))");
        pointcutAdvisor.setPointcut(pointcut);
        return pointcutAdvisor;
    }

*/
/*    @Bean("sql2oTransactionManager")
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }*/


/*
    @Bean
    public DataSourceTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
*/

/*    @Bean
    public Sql2o getSql2o(DataSource dataSource) {
        this.sql2o = new Sql2o(dataSource);
        return sql2o;
    }*/



    private int getInt(Object s, int def) {
        if (null == s) return def;
        return Integer.valueOf("" + s);
    }

}


