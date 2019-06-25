package com.artlongs.fluentsql;


import act.Act;
import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import fluentsql.jdbc.Lq;
import org.osgl.mvc.annotation.GetAction;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Func :
 *
 * @author: leeton on 2019/4/1.
 */
public class AppStart {

    @Inject
    NamedParameterJdbcTemplate jdbcTemplate;

    @GetAction("/user/{id}")
    public User getUserById(Integer id) {
        Query<User> lq = new Lq<User>(User.class, jdbcTemplate).andEq(User::getId,id);
        User user = lq.to();
        return user;
    }

    @GetAction("/adduser")
    public User addUser() {
        User user = new User();
        user.setId(8);
        user.setUserName("jack");
        user.setDeptId(1);
        user.setMoney(new BigDecimal(1000.22));
        user.setCreateTime(new Date());
        new Lq<User>(User.class, jdbcTemplate).toSave(user);

        return user;
    }


    /**
     * @param args
     * @throws Exception
     */
      public static void main(String[] args) throws Exception {
        Act.start("fluent sql example","com.artlongs");
    }
}
