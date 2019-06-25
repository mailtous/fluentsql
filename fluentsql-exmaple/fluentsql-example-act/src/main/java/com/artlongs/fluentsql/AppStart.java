package com.artlongs.fluentsql;


import act.Act;
import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import fluentsql.jdbc.Lq;
import org.osgl.mvc.annotation.GetAction;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import java.util.Date;

/**
 * Func :
 *
 * @author: leeton on 2019/4/1.
 */
public class AppStart {

    @Inject
    NamedParameterJdbcTemplate jdbcTemplate;

    @GetAction("/test/lq/user/{id}")
    public User getUserById(Integer id) {
        Query<User> lq = new Lq<User>(User.class, jdbcTemplate).andEq(User::getId,1);
        User user = lq.to();
        return user;
    }

    @GetAction("/test/lq/adduser")
    public User addUser() {
        User user = new User();
        user.setId(1);
        user.setName("jack");
        user.setDeptId(1);
        user.setDeptId(1);
        user.setCreateDate(new Date());
        new Lq<User>(User.class, jdbcTemplate).save(user);

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
