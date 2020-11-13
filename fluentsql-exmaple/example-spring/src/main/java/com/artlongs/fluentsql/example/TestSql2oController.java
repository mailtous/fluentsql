package com.artlongs.fluentsql.example;

import com.artlongs.fluentsql.core.Page;
import com.artlongs.fluentsql.core.Query;
import com.artlongs.fluentsql.core.mock.User;
import com.artlongs.fluentsql.sql2o.Lq;
import com.artlongs.fluentsql.sql2o.Qe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
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

    @Resource
    private Sql2oConfig sql2oConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int getInt(Object s, int def) {
        if(null == s) return def;
        return Integer.valueOf(""+s);
    }

    @GetMapping("hello")
    @ResponseBody
    public String hello() {
        return "OK";
    }

    @GetMapping("/create/table")
    @ResponseBody
    public String createTable() {
        String sql = "CREATE TABLE `user`" +
                "(" +
                "id BIGINT(20) NOT NULL COMMENT '主键ID'," +
                "dept_id BIGINT(20) NOT NULL COMMENT '部门ID'," +
                "user_name VARCHAR(30) NULL DEFAULT NULL COMMENT '名称'," +
                "age INT(3) NULL DEFAULT NULL COMMENT '年龄'," +
                "money DECIMAL (13,3) NULL DEFAULT NULL COMMENT 'DECIMAL'," +
                "role INT(2) NULL DEFAULT NULL COMMENT '测试'," +
                "phone VARCHAR(13) NULL DEFAULT NULL COMMENT '手机号码'," +
                "create_time DATETIME NULL DEFAULT NULL COMMENT '日期'," +
                "PRIMARY KEY (id)" +
                ")";
        jdbcTemplate.execute(sql);
        return sql;
    }

    @GetMapping("user/{id}")
    @ResponseBody
    public User getUserById(@PathVariable Integer id) {
        Query lq = new Lq<User>(User.class, sql2oConfig.sql2o()).andEq(User::getId,id);
        User user = lq.to();

        return user;
    }

    @GetMapping("user")
    @ResponseBody
    public List<User> getUserList() {
        List<User> userList = new Lq(User.class,  sql2oConfig.sql2o()).toList();
        return userList;
    }

    @GetMapping("user/page")
    @ResponseBody
    public Page<User> getUserPage(Page<User> page) {
        page.setPageSize(10);
        page.setPageNumber(1);
        new Lq<User>(User.class,  sql2oConfig.sql2o()).andGt(User::getId,1).toPage(page);

        return page;
    }

    @GetMapping("adduser/{id}")
    @ResponseBody
    @Transactional
    public User addUser(@PathVariable Integer id) {
        User user = new User();
        user.setId(id);
        user.setUserName("jack");
        user.setDeptId(1);
        user.setMoney(new BigDecimal(1000.22));
        user.setCreateTime(new Date());
        new Lq<User>(User.class, sql2oConfig.sql2o()).toSave(user);
        int i=1/0;
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

        for (User u : userList) {
            List<User> insertList = new ArrayList<>();
            insertList.add(u);
            Integer nums = new Qe(User.class, sql2oConfig.sql2o()).toBatchInsert(insertList);
            insertList.clear();

        }



        return "insert recodes:" ;
    }






}
