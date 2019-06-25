package fluentsql.jdbc;

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


}
