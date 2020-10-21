package com.artlongs.fluentsql.example;

import com.artlongs.fluentsql.core.mock.User;
import com.artlongs.fluentsql.sql2o.Lq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sql2o.Sql2o;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Func :
 *
 * @author: leeton on 2020/9/27.
 */
@Service
public class UserService {

    @Autowired
    private Sql2o sql2o;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Transactional(propagation = Propagation.REQUIRED)
    public User addUser(Integer id) {
       User user = null;
/*       DefaultTransactionDefinition dt = new DefaultTransactionDefinition();
       dt.setName("user-22");
       dt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
       TransactionStatus status = transactionManager.getTransaction(dt);*/

       //
       user = new User();
       user.setId(id);
       user.setUserName("jack");
       user.setDeptId(1);
       user.setAge(18);
       user.setMoney(new BigDecimal(1000.22));
       user.setCreateTime(new Date());
       new Lq<User>(User.class, sql2o).toSave(user);
       //测试事务回滚
       int i = 1 / 0;
//       transactionManager.rollback(status);

       return user;
   }


}
