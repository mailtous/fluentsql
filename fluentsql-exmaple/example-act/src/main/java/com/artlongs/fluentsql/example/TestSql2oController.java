package com.artlongs.fluentsql.example;

import act.controller.annotation.UrlContext;
import act.util.JsonView;
import com.artlongs.fluentsql.core.Page;
import com.artlongs.fluentsql.core.mock.User;
import com.artlongs.fluentsql.sql2o.Lq;
import org.osgl.mvc.annotation.GetAction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Func :
 *
 * @author: leeton on 2019/6/28.
 */
@UrlContext("/test/sql2o")
public class TestSql2oController extends BaseController {

    @GetAction("user/{id}")
    @JsonView
    public User getUserById(Integer id) {
        User user = new Lq<User>(User.class, getSql2o()).andEq(User::getId, id).to();
        return user;
    }

    @GetAction("user")
    @JsonView
    public List<User> getUserList() {
        List<User> userList = new Lq(User.class, getSql2o()).toList();
        return userList;
    }

    @GetAction("user/page")
    @JsonView
    public Page getUserPage(Integer pageNumber) {
        Page page = new Page().setPageNumber(pageNumber);
        new Lq<User>(User.class, getSql2o()).andGt(User::getId, 1).toPage(page);
        return page;
    }

    @GetAction("adduser")
    @JsonView
    public User addUser() {
        User user = new User();
        user.setId(8);
        user.setUserName("jack");
        user.setDeptId(1);
        user.setMoney(new BigDecimal(1000.22));
        user.setCreateTime(new Date());
        new Lq<User>(User.class, getSql2o()).toSave(user);
        return user;
    }


}
