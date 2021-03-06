package com.artlongs.fluentsql.jdbc;

import com.artlongs.fluentsql.core.Attr;
import com.artlongs.fluentsql.core.mock.Dept;
import com.artlongs.fluentsql.core.mock.User;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class Lq<T> extends Qe<T> {
    public Lq() {
    }

    public Lq(Class<T> clz) {
        super(clz);
    }

    public Lq(Class<T> clz, NamedParameterJdbcTemplate jdbcTemplate) {
        super(clz, jdbcTemplate);
    }
    public Lq(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
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
