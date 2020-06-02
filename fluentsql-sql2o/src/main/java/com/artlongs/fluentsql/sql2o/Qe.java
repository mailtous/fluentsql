package com.artlongs.fluentsql.sql2o;

import com.artlongs.fluentsql.core.*;
import com.artlongs.fluentsql.core.mock.Dept;
import com.artlongs.fluentsql.core.mock.User;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

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

    private Sql2o sql2o;

    public Qe() {
    }

    public Qe(Class<T> clz) {
        init(clz, getTableName(clz), null);
    }

    public Qe(String mainTableName) {
        init(null, mainTableName, null);
    }


    public Qe(Class<T> clz, Sql2o sql2o) {
        init(clz, getTableName(clz), sql2o);
    }

    public Qe(Sql2o sql2o) {
        init(null, null, sql2o);
    }

    protected Qe<T> init(Class<T> clz, String mainTableName, Sql2o jdbcTemplate) {
        this.id = hashCode();
        this.clz = clz;
        this.mainTableName = mainTableName;
        this.sql2o = jdbcTemplate;
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
        T obj = single(build(), params, tClass);
        return obj;
    }

    public Long toCount() {
        String sql = this.count();
        return single(sql, new HashMap<>(), Long.class);
    }

    public List<T> toList() {
        return toList(this.clz);
    }

    public List toList(Class tClass) {
        return getList(buildSymbolsql(), this.params, tClass);
    }

    public boolean toDel() {
        checkProvider(sql2o);
        return write(delete().buildSymbolsql(), params) > 0;
    }

    public int toUpdate(Object entity) {
        checkProvider(sql2o);
        String symbolSql = update(entity).buildSymbolsql();
        int rows = write(symbolSql, params);
        return rows;
    }

    @Override
    public int toBatchInsert(List<?> batchValues) {
        if(null == batchValues || batchValues.size() ==0) return 0;
        checkProvider(sql2o);
        String insertSql = buildBatchInsertSql().buildSymbolsql();
        Assert.isTrue(insertSql.toLowerCase().indexOf("where")!=-1,"Batch INSERT canot include [WHERR] condition. " + insertSql);
        Connection con = null;
        try {
            con = sql2o.open();
            Query q = con.createQuery(insertSql);
            q.setAutoDeriveColumnNames(true);
            for (Object insertObj : batchValues) {
                Map<String, ?> fieldMap = new HashMap<>(32);
                BeanMapUtils.copyTo(insertObj, fieldMap);
                for (String key : fieldMap.keySet()) {
                    q.addParameter(StringKit.toUnderline(key), fieldMap.get(key));
                }
                q.addToBatch();
                fieldMap.clear();
            }
            q.executeBatch();
            return batchValues.size();
        } catch (Exception ex) {
            logger.warning(insertSql);
            throw new Sql2oException("BATCH INSERT ERROR: "+ex);
        }

    }
    /*    public int[] toUpdate(String symbolsql, Map<String, ?>[] batchValues) {//批量更新,symbolsql:还未设值的sql
        checkProvider(sql2o);
        int[] rows = sql2o.batchUpdate(symbolsql, batchValues);
        for (Map<String, ?> batchValue : batchValues) {
            clearMap(batchValue);
        }
        symbolsql = null;
        return rows;
    }*/

    public int toSave(Object entity) {
        checkProvider(sql2o);
        String symbolSql = save(entity).buildSymbolsql();
        int rows = write(symbolSql, params);
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
        checkProvider(sql2o);
        if (this.limit.length()>0) {
            throw new RuntimeException("分页参数不要通过[limit]传入.");
        }
        String countSql = this.count();
        Long count = single(countSql, new HashMap<>(), Long.class);
        List<T> list = new ArrayList<>();
        if (count > 0) {
            long pageNumber = page.getPageNumber();
            long pageSize = page.getPageSize();
            boolean offsetStartZero = false;
            long offset = (pageNumber - 1) * pageSize + (offsetStartZero ? 0 : 1);
            String pageSql = getMysqlLimit(this.buildSymbolsql(), offset, pageSize);
            list = getList(pageSql, params,clazz);
        }
        page.setTotal(count);
        page.setItems(list);
        return page;
    }

    public List getList(String sql, Map<String, Object> params, Class<T> tClass) {
        checkProvider(sql2o);
        List<T> list = new ArrayList<>();
        try (Connection con = sql2o.open()) {
            Query q = con.createQuery(sql);
            setSql2oParam(q, params);
            q.setAutoDeriveColumnNames(true);
            list = q.executeAndFetch(tClass);
        }
        // 手动清理内存是种好习惯 :)
        clear();
        return 0 == list.size() ? new ArrayList<>() : list;
    }

    public <T> T single(String sql, Map<String, Object> params, Class tClass) {
        checkProvider(sql2o);
        Object result = null;
        try (Connection con = sql2o.open()) {
            Query q = con.createQuery(sql);
            setSql2oParam(q, params);
            q.setAutoDeriveColumnNames(true);
            result = q.executeAndFetchFirst(tClass);
        }
        // 手动清理内存是种好习惯 :)
        clear();
        return (T)result;
    }

    public int write(String sql, Map<String, Object> params) {
        checkProvider(sql2o);
        Connection con = null;
        try {
            con = sql2o.beginTransaction();
            Query q = con.createQuery(sql);
            setSql2oParam(q, params);
            q.setAutoDeriveColumnNames(true);
            q.executeUpdate();
            con.commit();
        } catch (Exception ex) {
            throw new Sql2oException("写入数据库出错:"+ex);
        }
        // 手动清理内存是种好习惯 :)
        clear();
        return con.getResult();
    }

    private void checkProvider(Sql2o sql2o) {
        Assert.isNull(sql2o,"Sql2o 不能为 NULL,请先传入.");
    }

    private void setSql2oParam(Query query, Map<String,Object> parms) {
        if (null != parms && parms.size() > 0) {
            for (String k : parms.keySet()) {
                String key = k.replace(":", "");
                query.addParameter(key, parms.get(k));
            }
        }
    }

    // ====== 集成查询方法 END ====================================================================================================


   public static void main(String[] args) throws Exception {
       String sql = new Qe(User.class)
               .select("user_name")
//            .andIn("dept_id", new Qe(Dept.class).select("id").andGt("id", 0))
               .sum("id", Dept.class)
//                .sumCase("id", 1, "money", "money")
               .leftJoin(Dept.class)
               .andLike("user_name", "alice")
               .andIn("id", new Integer[]{1, 2, 3})
               .andBetween("create_date", new Date(), new Date())
               .group("dept_id")
               .having("dept_id", Opt.GT, 0)
               .asc("dept_id")
               .desc("user_name")
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

       String joinSelf = new Qe(User.class)
               .select("id")
               .selectAs("t2", "user_name", "")
               .joinSelf("t2", "pid")
               .build();

       System.out.println("sql=" + joinSelf);

       // 找出购买价格比小明高的用户及价格
       String joinSelf2 = new Qe(User.class)
               .select("price")
               .select("user_name")
               .fromAs("t1")
               .fromAs("t2")
               .andEq("t2.user_name", "小明")
               .andLt("t2.price", "t1.price")
               .build();

       System.out.println("sql=" + joinSelf2);
       User u = new User();
       u.setId(100);
       u.setUserName("alice");

       User u2 = new User();
       u.setId(200);
       u.setUserName("alice200");
       List<User> userList = new ArrayList<>();
       userList.add(u);
       userList.add(u2);

       String updateSql = new Qe(User.class).update(u).build();
       System.err.println(updateSql);


   }



}
