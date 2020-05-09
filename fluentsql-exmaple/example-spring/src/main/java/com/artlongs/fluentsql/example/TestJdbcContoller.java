package com.artlongs.fluentsql.example;

import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import com.artlongs.fluentsql.jdbc.Lq;
import com.artlongs.fluentsql.jdbc.Qe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Func :
 *
 * @author: leeton on 2019/6/25.
 */
@Controller
@RequestMapping("/test/jdbc/")
public class TestJdbcContoller {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @GetMapping("user/{id}")
    @ResponseBody
    public User getUserById(@PathVariable Integer id) {
        Query lq = new Lq<User>(User.class, jdbcTemplate).andEq(User::getId,id);
        User user = lq.to();
        return user;
    }

    @GetMapping("user")
    @ResponseBody
    public List<User> getUserList() {
        return new Lq<User>(User.class, jdbcTemplate).toList();
    }

    @GetMapping("adduser/{id}")
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

    @GetMapping("batch/add/user")
    @ResponseBody
    public String batchAddUser() {
        User user = new User();
        user.setId(7);
        user.setUserName("jack");
        user.setDeptId(1);
        user.setMoney(new BigDecimal(1000.1));
        user.setCreateTime(new Date());

        User user2 = new User();
        user2.setId(8);
        user2.setUserName("jack2");
        user2.setDeptId(2);
        user2.setMoney(new BigDecimal(2000.2));
        user2.setCreateTime(new Date());
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);

        Integer nums = new Qe(User.class,jdbcTemplate).toBatchInsert(userList);

        return "insert recodes:" + nums;
    }

}
