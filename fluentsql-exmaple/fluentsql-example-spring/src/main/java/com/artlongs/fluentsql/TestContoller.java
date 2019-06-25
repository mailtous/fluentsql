package com.artlongs.fluentsql;

import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import fluentsql.jdbc.Lq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Func :
 *
 * @author: leeton on 2019/6/25.
 */
@Controller
@RequestMapping("/test/lq/")
public class TestContoller {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @GetMapping("/user/{id}")
    @ResponseBody
    public User getUserById(@PathVariable Integer id) {
        Query<User> lq = new Lq<User>(User.class, jdbcTemplate).andEq(User::getId,id);
        User user = lq.to();
        return user;
    }

    @GetMapping("/adduser/{id}")
    @ResponseBody
    public User addUser(@PathVariable Integer id) {
        User user = new User();
        user.setId(id);
        user.setUserName("jack");
        user.setDeptId(1);
        user.setMoney(new BigDecimal(1000.22));
        user.setCreateTime(new Date());
        new Lq<User>(User.class, jdbcTemplate).toSave(user);

        return user;
    }

}
