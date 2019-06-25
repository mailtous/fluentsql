package com.artlongs.fluentsql.core.mock;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Func :
 *
 * @author: leeton on 2019/6/21.
 */
public class User {
    private Integer id;
    private String userName;
    private BigDecimal money;
    private Integer deptId;
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
