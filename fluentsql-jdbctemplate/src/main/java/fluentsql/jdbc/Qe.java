package fluentsql.jdbc;

import com.artlongs.fluentsql.core.BaseQuery;
import com.artlongs.fluentsql.core.LambdaQuery;
import com.artlongs.fluentsql.core.Page;
import com.artlongs.fluentsql.core.mock.Dept;
import com.artlongs.fluentsql.core.mock.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;
import java.util.logging.Logger;

/**
 * Function: 查询表达式
 *
 * @Autor: leeton
 * @Date : 2/27/18
 */
public class Qe<T> extends LambdaQuery<T> {
    protected static final Logger logger = Logger.getLogger("Qe");

    private NamedParameterJdbcTemplate jdbcTemplate;

    public Qe() {
    }

    public Qe(Class<T> clz) {
        init(clz, getTableName(clz), null);
    }

    public Qe(String mainTableName) {
        init(null, mainTableName, null);
    }


    public Qe(Class<T> clz, NamedParameterJdbcTemplate jdbcTemplate) {
        init(clz, getTableName(clz), jdbcTemplate);
    }

    public Qe(NamedParameterJdbcTemplate jdbcTemplate) {
        init(null, null, jdbcTemplate);
    }

    protected Qe<T> init(Class<T> clz, String mainTableName, NamedParameterJdbcTemplate jdbcTemplate) {
        this.id = hashCode();
        this.clz = clz;
        this.mainTableName = mainTableName;
        this.jdbcTemplate = jdbcTemplate;
        return this;
    }

    @Override
    public BaseQuery<T> condition() {
        Qe q = new Qe(this.clz);
        q.clear();
        q.where = new StringBuffer(" ");
        return q;
    }

    @Override
    public String count() {
        Qe qe = new Qe(this.mainTableName);
        qe.where = this.where;
        qe.join = this.join;
        qe.params.putAll(this.params);
        qe.count.append("SELECT COUNT(1)");
        return qe.build();
    }

    // ====== 集成查询方法 BEGIN ====================================================================================================
    public T to() {
        return to(this.clz);
    }

    public T to(Class tClass) {
        List<T> list = getList(buildSymbolsql(), toJdbcParams(params), tClass);
        return 0 == list.size() ? null : list.get(0);
    }

    public Long toCount() {
        String sql = this.count();
        return single(sql, new HashMap<>(), Long.class);
    }

    public List<T> toList() {
        return toList(this.clz);
    }

    public List toList(Class tClass) {
        return getList(buildSymbolsql(), toJdbcParams(this.params), tClass);
    }

    public boolean toDel() {
        checkJdbc(jdbcTemplate);
        return jdbcTemplate.update(delete().buildSymbolsql(), toJdbcParams(params)) > 0;
    }

    public int toUpdate(Object entity) {
        checkJdbc(jdbcTemplate);
        String symbolSql = update(entity).buildSymbolsql();
        Map<String, Object> jdbdParams = toJdbcParams(params);
        int rows = jdbcTemplate.update(symbolSql, jdbdParams);
        clearMap(jdbdParams);
        symbolSql = null;
        return rows;
    }

    public int[] toUpdate(String symbolsql, Map<String, ?>[] batchValues) {//批量更新,symbolsql:还未设值的sql
        checkJdbc(jdbcTemplate);
        int[] rows = jdbcTemplate.batchUpdate(symbolsql, batchValues);
        for (Map<String, ?> batchValue : batchValues) {
            clearMap(batchValue);
        }
        symbolsql = null;
        return rows;
    }

    public int toSave(Object entity) {
        checkJdbc(jdbcTemplate);
        String symbolSql = save(entity).buildSymbolsql();
        Map<String, Object> jdbdParams = toJdbcParams(params);
        int rows = jdbcTemplate.update(symbolSql, jdbdParams);
        clearMap(jdbdParams);
        symbolSql = null;
        return rows;
    }

    public Page<T> toPage(Page page) {
        return toPage(this.clz, page);
    }

    public Page<T> toPage(Class clz, Page page) {
        page = getPage(clz, page);
        return page;
    }

    /**
     * 返回 PAGE 查询结果集
     * 注意不要传入 limit 语句
     *
     * @param clazz
     * @param page
     * @return
     */
    private Page<T> getPage(Class<T> clazz, Page page) {
        checkJdbc(jdbcTemplate);
        if (this.limit.length()>0) {
            throw new RuntimeException("分页参数不要通过[limit]传入.");
        }
        String countSql = this.count();
        List<Long> countList = jdbcTemplate.queryForList(countSql, new HashMap<>(), Long.class);
        Long count = countList.get(0);
        List<T> list = null;
        if (count == 0) {
            list = Collections.emptyList();
        } else {
            long pageNumber = page.getPageNumber();
            long pageSize = page.getPageSize();
            boolean offsetStartZero = false;
            long offset = (pageNumber - 1) * pageSize + (offsetStartZero ? 0 : 1);
            String pageSql = getMysqlLimit(this.buildSymbolsql(), offset, pageSize);
            Map<String, Object> jdbdParams = toJdbcParams(params);
            list = jdbcTemplate.query(pageSql, jdbdParams, new BeanPropertyRowMapper<>(clazz));
            clearMap(jdbdParams);
        }
        page.setTotal(count);
        page.setItems(list);
        clearMap(params);
        return page;
    }

    public List getList(String sql, Map<String, Object> params, Class<T> tClass) {
        checkJdbc(jdbcTemplate);
        List<T> list = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(tClass));
        clearMap(params);
        return 0 == list.size() ? new ArrayList<>() : list;
    }

    public <T> T single(String sql, Map<String, Object> params, Class tClass) {
        checkJdbc(jdbcTemplate);
        List<T> list = jdbcTemplate.query(sql, params, new SingleColumnRowMapper<>(tClass));
        clearMap(params);
        return list.size() == 0 ? null : list.get(0);
    }

    public <T> T getObj(String sql, Map<String, Object> params, Class tClass) {
        List<T> list = getList(sql, params, tClass);
        return list.size() == 0 ? null : list.get(0);
    }

    private void checkJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
        if (null == jdbcTemplate) throw new RuntimeException("NamedParameterJdbcTemplate 不能为 NULL,请先传入.");
    }

    // ====== 集成查询方法 END ====================================================================================================


   public static void main(String[] args) throws Exception {
       String sql = new Qe(User.class)
               .select("user_uame")
//            .andIn("dept_id", new Qe(Dept.class).select("id").andGt("id", 0))
               .sum("id", Dept.class)
//                .sumCase("id", 1, "money", "money")
               .leftJoin(Dept.class)
               .andLike("user_uame", "alice")
               .andIn("id", new Integer[]{1, 2, 3})
               .andBetween("create_date", new Date(), new Date())
               .group("dept_id")
               .having("dept_id", Opt.GT, 0)
               .asc("dept_id")
               .desc("user_uame")
               .build();

        System.out.println("sql=" + sql);

     /*   String and_sql = new Qe().eq(SysUser.Dao.userName, "linton").and(new Qe<SysUser>().eq(SysUser.Dao.userName, "alice")).sql();
        System.out.println("and_sql=" + and_sql);


        String or = new Qe().eq(SysUser.Dao.deptId, 1).or(new Qe().eq(SysUser.Dao.deptId, 2)).sql();
        System.out.println("or=" + or);
*/

/*
        String between = new Qe().andBetween(SysUser.Dao.createDate, new Date(), new Date()).build();
        System.out.println("between=" + between);
*/

/*        String del = new Qe(SysUser.class).del().andEq("dept_id", 1).build();
        System.out.println("del= " + del);

        String count = new Qe(SysUser.class).andEq("dept_id", 1).count();
        System.out.println("count= " + count);*/


    }



}
