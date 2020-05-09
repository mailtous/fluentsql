package com.artlongs.fluentsql.example;

import com.artlongs.fluentsql.core.Page;
import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import com.artlongs.fluentsql.sql2o.Lq;
import com.artlongs.fluentsql.sql2o.Qe;
import com.artlongs.fluentsql.sql2o.Sql2OBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Func :
 *
 * @author: leeton on 2019/6/28.
 */
@Controller
@RequestMapping("/test/sql2o")
public class TestSql2oController {
    @Autowired
    Environment environment;
    @Autowired
    private Sql2o sql2o;

    @Bean
    public Sql2o buildSql2o() {
        String proPfix = "spring.datasource.";
        String url = environment.getProperty(proPfix + "url");
        String username = environment.getProperty(proPfix + "username");
        String password = environment.getProperty(proPfix + "password");
        String dcn = environment.getProperty(proPfix + "driver-class-name");
        int maxPoolSize = getInt(environment.getProperty(proPfix + "hikari.maximum-pool-size"),10);
        int minIdle = getInt(environment.getProperty(proPfix + "hikari.minimum-idle"), 5);
        Sql2o sql2o = Sql2OBuilder.buildOfHikariCP(url,username,password,dcn,maxPoolSize,minIdle);
//        Sql2o sql2o = Sql2OBuilder.build(url,username,password);
        return sql2o;
    }

    private int getInt(Object s, int def) {
        if(null == s) return def;
        return Integer.valueOf(""+s);
    }

    @GetMapping("hello")
    @ResponseBody
    public String hello() {
        return "OK";
    }

    @GetMapping("user/{id}")
    @ResponseBody
    public User getUserById(@PathVariable Integer id) {
        Query lq = new Lq<User>(User.class, sql2o).andEq(User::getId,id);
        User user = lq.to();
        return user;
    }

    @GetMapping("user")
    @ResponseBody
    public List<User> getUserList() {
        List<User> userList = new Lq(User.class, sql2o).toList();
        return userList;
    }

    @GetMapping("user/page")
    @ResponseBody
    public Page<User> getUserPage(Page<User> page) {
        page.setPageSize(2);
        new Lq<User>(User.class, sql2o).andGt(User::getId,1).toPage(page);
        return page;
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
        new Lq<User>(User.class, sql2o).toSave(user);

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

       Integer nums = new Qe(User.class,sql2o).toBatchInsert(userList);

        return "insert recodes:" + nums;
    }






}
