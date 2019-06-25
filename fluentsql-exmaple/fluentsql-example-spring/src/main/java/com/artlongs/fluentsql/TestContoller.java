package com.artlongs.fluentsql;

import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import fluentsql.jdbc.Lq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * Func :
 *
 * @author: leeton on 2019/6/25.
 */
@Controller
@RequestMapping("/test/lq")
public class TestContoller {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @RequestMapping("/test/lq/user/{id}")
    public User getUserById(Integer id) {
        Query<User> lq = new Lq<User>(User.class, jdbcTemplate).andEq(User::getId,1);
        User user = lq.to();
        return user;
    }

    @RequestMapping("/test/lq/adduser")
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

}
