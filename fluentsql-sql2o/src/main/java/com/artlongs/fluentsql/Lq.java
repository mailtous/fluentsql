package com.artlongs.fluentsql;

import com.artlongs.fluentsql.core.Attr;
import com.artlongs.fluentsql.core.mock.Dept;
import com.artlongs.fluentsql.core.mock.User;
import org.sql2o.Sql2o;

public class Lq<T> extends Qe<T> {
    public Lq() {
    }

    public Lq(Class<T> clz) {
        super(clz);
    }

    public Lq(Class<T> clz, Sql2o sql2o) {
        super(clz, sql2o);
    }

    public Lq(Sql2o sql2o) {
        super(sql2o);
    }

    public static void main(String[] args) throws Exception {
        String sql = new Lq<>(User.class)
                .select(User::getUserName)
                .select(new Attr<>(Dept::getDeptName))
                .leftJoin(Dept.class)
                .andLike(User::getUserName, "jack")
                .asc(User::getDeptId)
                .desc(User::getUserName)
                .group(User::getDeptId)
                .having(User::getDeptId, Qe.Opt.GT, 0)
                .build();

        System.out.println("sql=" + sql);
    }


}
