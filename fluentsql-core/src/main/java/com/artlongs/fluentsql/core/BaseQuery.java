package com.artlongs.fluentsql.core;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.artlongs.fluentsql.core.BaseQuery.Opt.*;

/**
 * Func :
 *
 * @author: leeton on 2019/6/21.
 */
public abstract class BaseQuery<T> implements Query{

    protected static final Logger logger = Logger.getLogger("BaseQuery");
    protected Integer id = 0;
    protected Class<T> clz;
    protected String mainTableName = "";
    protected String template = "{insert}{update}{del}{select}{sum}{casethen}{sumcasethen}{count}{from}{join}{where}{subselect}{group}{having}{order}{limit}";
    protected StringBuffer del = new StringBuffer(128);
    protected StringBuffer count = new StringBuffer(128);
    protected StringBuffer sum = new StringBuffer(128);
    protected StringBuffer casethen = new StringBuffer(128);
    protected StringBuffer sumcasethen = new StringBuffer(128);
    protected StringBuffer select = new StringBuffer(128);
    protected StringBuffer subselect = new StringBuffer(128);
    protected StringBuffer from = new StringBuffer(64);
    protected StringBuffer join = new StringBuffer(196);
    protected StringBuffer where = new StringBuffer(196);
    protected StringBuffer group = new StringBuffer(128);
    protected StringBuffer having = new StringBuffer(64);
    protected StringBuffer order = new StringBuffer(32);
    protected StringBuffer limit = new StringBuffer(32);
    protected StringBuffer update = new StringBuffer(196);
    protected StringBuffer insert = new StringBuffer(196);
    protected String symbolsql = "";  // 还没有设值的 sql
    protected Map<String, Object> params = new HashMap<>(7);
    protected boolean checkSqlHack = true;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //时间日期统一用这种格式
    private String link; // 连接符 AND/OR

    public BaseQuery() {
    }

    public BaseQuery(Class<T> clz) {
        init(clz, getTableName(clz));
    }

    protected BaseQuery<T> init(Class<T> clz, String mainTableName) {
        this.id = hashCode();
        this.clz = clz;
        this.mainTableName = mainTableName;
        return this;
    }

    abstract public String count();

    public BaseQuery<T> select(String... vals) {
        if (vals.length > 0) {
            for (String v : vals) {
                selectAs(mainTableName, v, null);
            }
        }
        return this;
    }

    public BaseQuery<T> selectAs(Class otherClz, String v, String as) {
        selectAs(getTableName(otherClz), v, as);
        return this;
    }

    public BaseQuery<T> selectAs(String column, String as) {
        return selectAs(mainTableName, column, as);
    }

    public BaseQuery<T> select(Attr.Property<T, ?> func, Spell spell) {
        return select(spell, func);
    }

    public BaseQuery<T> select(Spell spell, Attr.Property<T, ?> func) {
        Attr f = new Attr(func);
        String underline_column = f.getColumn();
        String camel_column = f.getName();
        if (underline_column.equals(camel_column)) {
            return selectAs(mainTableName, underline_column, null);
        }
        String as = Spell.getAs(null, spell, underline_column, f.getName());
        selectAs(mainTableName, underline_column, as);
        return this;
    }

    public BaseQuery<T> select(Attr attr, Spell spell) {
        return select(spell, attr);
    }

    public BaseQuery<T> select(Spell spell, Attr attr) {
        String column = attr.getColumn();
        String as = Spell.getAs(null, spell, column, attr.getName());
        selectAs(attr.getTableName(), column, as);
        return this;
    }

    public BaseQuery<T> select(Spell spell, Attr... attrs) {
        for (Attr attr : attrs) {
            String column = attr.getColumn();
            String as = Spell.getAs(null, spell, column, attr.getName());
            selectAs(attr.getTableName(), column, as);
        }
        return this;
    }

    public BaseQuery<T> selectAs(String table, String column, String as) {
        if (this.select.length() == 0) {
            this.select.append(SELECT.symbol);
        }
        this.select.append(table).append(".").append(column);
        if (null != as && "" != as) {
            this.select.append(AS.symbol).append(as);
        }
        this.select.append(",");
        return this;
    }

    public BaseQuery<T> select(Qf... qfs) {
        if (this.select.length() == 0) {
            this.select.append(SELECT.symbol);
        }
        for (Qf qf : qfs) {
            this.select.append(qf.sql).append(",");
        }
        return this;
    }

    public BaseQuery<T> fromSubSelect(BaseQuery<T> subSelect) {
        fromSubSelect(subSelect, subSelect.mainTableName);
        return this;
    }

    public BaseQuery<T> fromSubSelect(BaseQuery<T> subSelect, String as) {
        this.from.append(FROM.symbol).append("(");
        this.from.append(subSelect.build());
        this.from.append(")");
        if (StringKit.isNotBlank(as)) {
            this.from.append(AS.symbol).append(as);
            this.mainTableName = as;
        }
        return this;
    }

    public BaseQuery<T> fromAs(String as) {
        if(0 == this.from.length()){
            this.from.append(FROM.symbol);
            this.from.append("`").append(this.mainTableName).append("`");
        }else {
            this.from.append(" ,");
            this.from.append(" `").append(this.mainTableName).append("`");
        }
        this.from.append(AS.symbol).append(as);
        return this;
    }

    public BaseQuery<T> sum(String column, Class... otherClz) {
        return sumAs(column, column, getTablePrev(otherClz));
    }

    public BaseQuery<T> sumAs(String column, String as, String... table) {
        if (this.select.length() == 0 && this.sum.length() == 0) {
            this.sum.append("SELECT ");
        }
        if (select.length() > 0 && sum.length() == 0) {
            sum.append(" ,");
        }
        this.sum.append(" SUM(");
        if (null != table && table.length > 0) {
            this.sum.append(table[0]).append(".");
        }
        this.sum.append(column).append(") AS ").append(as).append(",");
        return this;
    }

    public BaseQuery<T> sumCase(String caseField, Object eqVal, String sumField, String asFiled, Class... otherClz) {
        if (select.length() == 0 && sumcasethen.length() == 0) {
            sumcasethen.append("SELECT ");
        }
        if (select.length() > 0 && sumcasethen.length() == 0) {
            sumcasethen.append(" ,");
        }
        sumcasethen.append(" SUM(IFNULL(");
        String table = getTablePrev(otherClz);
        sumcasethen.append("CASE ")
                .append(table).append(".").append(caseField)
                .append(" WHEN ").append(eqVal)
                .append(" THEN ")
                .append(table).append(".").append(sumField)
                .append(" END, 0)) AS ").append(asFiled)
                .append(",");
        return this;
    }

    public BaseQuery<T> caseAs(String caseField, Object eqVal, String targetField, String asFiled, Class... otherClz) {
        if (select.length() == 0 && casethen.length() == 0) {
            casethen.append("SELECT ");
        }
        if (select.length() > 0 && casethen.length() == 0) {
            casethen.append(" ,");
        }
        casethen.append(" (CASE ");
        String table = getTablePrev(otherClz);
        casethen.append(table).append(".").append(caseField)
                .append(" WHEN ").append(eqVal)
                .append(" THEN ")
                .append(table).append(".").append(targetField)
                .append(" END) AS ").append(asFiled)
                .append(",");
        return this;
    }

    public BaseQuery delete() {
        this.del.append(DEL.symbol).append(FROM.symbol).append(mainTableName);
        return this;
    }

    public BaseQuery<T> leftJoin(String joinTableName, String joinTableKey, String mainTableKey) {
        if (null == mainTableName || "".equals(mainTableName)) throw new RuntimeException("主表不能为空。");
        this.join
                .append(LEFTJOIN.symbol)
                .append("`")
                .append(joinTableName)
                .append("`")
                .append(ON.symbol)
                .append(this.mainTableName)
                .append(".")
                .append(mainTableKey)
                .append(" = ")
                .append(joinTableName)
                .append(".")
                .append(joinTableKey);
        return this;
    }

    /**
     * 约定:关联从表的字段名默认为: 从表字+"_id",全小写 underline 风格
     * eg:User 关联部门 Dept 则为--> dept_id
     * @param joinTableClass
     * @return
     */
    public BaseQuery<T> leftJoin(Class joinTableClass) {
        String joinTableName = getTableName(joinTableClass);
        String mainJoinKey = joinTableName + "_id";
        leftJoin(joinTableClass, "id",mainJoinKey);
        return this;
    }

    public BaseQuery<T> leftJoin(Class<T> clz, String joinTableKey, String mainTableKey) {
        return leftJoin(getTableName(clz), joinTableKey, mainTableKey);
    }

    public BaseQuery<T> leftJoin(Class<T> clz, String mainTableKey) {
        return leftJoin(getTableName(clz), "id", mainTableKey);
    }

    /**
     * 生成自关联语句,与 fromAs 一起使用才行,即主表的别名由 fromAs 指定.
     * 不推荐使用,因为双变成使用了硬代码.
     * @param alias   关联表的别名
     * @param joinKey 关联的字段
     * @return
     */
    public BaseQuery<T> joinSelf(String alias,String joinKey) {
        this.join.append(LEFTJOIN.symbol)
                .append("`")
                .append(this.mainTableName)
                .append("` ")
                .append(alias)
                .append(ON.symbol)
                .append(this.mainTableName)
                .append(".")
                .append("id")
                .append(" = ")
                .append(alias)
                .append(".")
                .append(joinKey);
        return this;
    }


    // =============== and ==============================

    /**
     * 多重子条件时使用
     */
   abstract public BaseQuery<T> condition();
    public BaseQuery<T> c() {
        return condition();
    }

    public BaseQuery<T> and(BaseQuery<T>... manyQe) {
        addManyCondition(AND.symbol, manyQe);
        return this;
    }

    public BaseQuery<T> or(BaseQuery<T>... manyQe) {
        addManyCondition(Opt.OR.symbol, manyQe);
        return this;
    }

    public BaseQuery<T> andEq(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.EQ, column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andNe(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.NE, column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andLt(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.LT, column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andGt(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.GT, column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andLe(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.LE, column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andGe(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.GE, column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andIn(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.IN, column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andBetween(String column, Object v1, Object v2, Class... otherClz) {
        addBetween(column, v1, v2, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andLike(String column, Object val, Class... otherClz) {
        addLike(column, val, AND.name(), otherClz);
        return this;
    }

    public BaseQuery<T> andIsnull(String column, Class... otherClz) {
        this.where.append(AND.symbol).append(column).append(ISNULL.symbol);
        return this;
    }

    public BaseQuery<T> andNotnull(String column, Class... otherClz) {
        this.where.append(AND.symbol).append(column).append(NOTNULL.symbol);
        return this;
    }

    // =============== OR CONDITION ==============================

    public BaseQuery<T> orEq(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.EQ, column, val, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orNe(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.NE, column, val, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orLt(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.LT, column, val, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orGt(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.GT, column, val, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orLe(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.LE, column, val, OR.name());
        return this;
    }

    public BaseQuery<T> orGe(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.GE, column, val, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orIn(String column, Object val, Class... otherClz) {
        addWhereSql(Opt.IN, column, val, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orBetween(String column, Object v1, Object v2, Class... otherClz) {
        addBetween(column, v1, v2, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orLike(String column, Object val, Class... otherClz) {
        addLike(column, val, OR.name(), otherClz);
        return this;
    }

    public BaseQuery<T> orIsnull(String column, Class... otherClz) {
        String table = getTablePrev(otherClz);
        this.where.append(OR.symbol).append(table).append(".").append(column).append(ISNULL.symbol);
        return this;
    }

    public BaseQuery<T> orNotnull(String column, Class... otherClz) {
        String table = getTablePrev(otherClz);
        this.where.append(OR.symbol).append(table).append(".").append(column).append(NOTNULL.symbol);
        return this;
    }

    public BaseQuery<T> asc(String... val) {
        addOrderBy(ASC.symbol, val);
        return this;
    }

    public BaseQuery<T> desc(String... val) {
        addOrderBy(DESC.symbol, val);
        return this;
    }

    private void addOrderBy(String opt, String... val) {
        if (order.length() == 0) {
            order.append(ORDER.symbol);
        }
        for (String v : val) {
            order.append(mainTableName).append(".").append(v).append(opt).append(",");
        }
    }

    public BaseQuery<T> group(String... columns) {
        this.group.append(GROUP.symbol);
        for (String v : columns) {
            group.append(mainTableName).append(".").append(v).append(",");
        }
        group.deleteCharAt(group.length() - 1);
        return this;
    }

    public BaseQuery<T> having(String column, Opt opt, Object val) {
        if (this.group.length() > 0) {
            this.having.append(HAVING.symbol)
                    .append("(")
                    .append(mainTableName)
                    .append(".")
                    .append(column)
                    .append(opt.symbol)
                    .append(val)
                    .append(")");
        }
        return this;
    }

    public BaseQuery<T> limit(int val1, int val2) {
        this.limit.append(LIMIT.symbol).append(val1).append(",").append(val2);
        return this;
    }

    public BaseQuery<T> update(Object entity) {
        if (where.length() == 0) throw new RuntimeException("UPDATE 之前必须有 WHERE 条件以避免大范围变更数据.");
        this.update.append(UPDATE.symbol).append("`").append(getTableName(this.clz)).append("`").append(" SET ");
        this.update.append(getKeyValCondition(entity, ":_up_"));
        return this;
    }

    public BaseQuery<T> save(Object entity) {
        this.update.append(INSERT.symbol).append("`").append(getTableName(this.clz)).append("`").append(" SET ");
        this.update.append(getKeyValCondition(entity, ":_add_"));
        return this;
    }

    private StringBuffer getKeyValCondition(Object entity, String keyPrev) {
        StringBuffer sql = new StringBuffer(128);
        Map<String, Object> fieldMap = new HashMap<String, Object>(20);
        BeanMapUtils.copy(entity, fieldMap);
        for (String k : fieldMap.keySet()) {
            String _k = StringKit.enCodeUnderlined(k);
            sql.append(_k).append("=").append(keyPrev).append(_k).append(", ");
            addParams(keyPrev + _k, fieldMap.get(k));
        }
        sql.deleteCharAt(sql.length() - 2);
        return sql;
    }

    protected String getTableName(Class clz) {
        return Attr.getRealTableName(clz);
    }

    protected String getMysqlLimit(String sql, long offset, long pageSize) {
        offset = PageKit.mysqlOffset(false, offset);
        StringBuilder builder = new StringBuilder(sql);
        builder.append(" limit ").append(offset).append(" , ").append(pageSize);
        return builder.toString();
    }

    protected void clearMap(Map params) {
        if (null != params) {
            params.clear();
            params = null;
        }
    }

    /**
     * PropertyFilter 转为 Qe
     **/
    public BaseQuery<T> whereOfFilters(List<PropertyFilter> filterList) {
        for (PropertyFilter filter : filterList) {
            if (filter.isMulti() && filter.getFilters().size()>1) {
                builWhereOfBothFilter(this, filter);
            } else {
                builWhereOfFilter(this, filter);
            }
        }
        return this;
    }

    private BaseQuery<T> builWhereOfBothFilter(BaseQuery<T> qe, PropertyFilter filter) {
        PropertyFilter left = filter.getFilters().get(0);
        PropertyFilter rigth = filter.getFilters().get(1);
        if (filter.isRoundAnd()) {
            qe.and(builWhereOfFilter(this.c(), left), builWhereOfFilter(this.c(), rigth));
        }
        if (filter.isRoundOr()) {
            qe.or(builWhereOfFilter(this.c(), left), builWhereOfFilter(this.c(), rigth));
        }
        return qe;
    }

    private BaseQuery builWhereOfFilter(BaseQuery qe, PropertyFilter filter) {
        if(filter == null) return qe;
        String filed = StringKit.enCodeUnderlined(filter.getFieldName());
        PropertyFilter.MatchType matchType = filter.getMatchType();
        Object[] vv = filter.getValues();
        Object v1 = null;
        Object v2 = null;
        if(vv.length == 1){
            if(null == vv[0] && matchType != PropertyFilter.MatchType.ISNULL && matchType != PropertyFilter.MatchType.NOTNULL) return qe;
            v1 = StringKit.enCodeUnderlined(String.valueOf(vv[0]));
        }
        if(vv.length ==2){
            v2 = StringKit.enCodeUnderlined(String.valueOf(vv[1]));
        }
        switch (matchType) {
            case EQ:
                return qe = (filter.isAnd()) ? andEq(filed, v1) : orEq(filed, v1);
            case NE:
                return qe = (filter.isAnd()) ? andNe(filed, v1) : orNe(filed, v1);
            case LE:
                return qe = (filter.isAnd()) ? andLe(filed, v1) : orLe(filed, v1);
            case GE:
                return qe = (filter.isAnd()) ? andGe(filed, v1) : orGe(filed, v1);
            case GT:
                return qe = (filter.isAnd()) ? andGt(filed, v1) : orGt(filed, v1);
            case LT:
                return qe = (filter.isAnd()) ? andLt(filed, v1) : orLt(filed, v1);
            case ISNULL:
                return qe = (filter.isAnd()) ? andIsnull(filed) : orIsnull(filed);
            case NOTNULL:
                return qe = (filter.isAnd()) ? andNotnull(filed) : orNotnull(filed);
            case IN:
                return qe = (filter.isAnd()) ? andIn(filed, v1) : orIn(filed, v1);
            case BETWEEN:
                return qe = (filter.isAnd()) ? andBetween(filed, v1, v2) : orBetween(filed, v1, v2);
            case LIKE:
                return qe = (filter.isAnd()) ? andLike(filed, v1) : orLike(filed, v1);
            case LIKESTART:
                return qe = (filter.isAnd()) ? andLike(filed, v1) : orLike(filed, v1);
            case LIKEANYWHERE:
                return qe = (filter.isAnd()) ? andLike(filed, v1) : orLike(filed, v1);
            case LIKEISTART:
                return qe = (filter.isAnd()) ? andLike(filed, v1) : orLike(filed, v1);
            case LIKEIANYWHERE:
                return qe = (filter.isAnd()) ? andLike(filed, v1) : orLike(filed, v1);
        }
        return qe;
    }

    private void addWhereSql(Opt opt, String k, Object val, String link, Class... otherClz) {
        this.link = link;
        if (null == val || "".equals(val)) return;
        if (this.where.length() > 0) {
            this.where.append(link);
        }
        StringBuffer key = new StringBuffer().append(":").append(link.toLowerCase()).append(opt.name().toLowerCase()).append("_").append(k);
        //
        this.where.append("(");
        if (k.indexOf(".") != -1) {// 手动指定了表名,eg: t1.age = 18
            this.where.append(k);
        }else {
            String table = getTablePrev(otherClz);
            this.where.append(table).append(".").append(k);
        }
        this.where.append(opt.symbol).append(key).append(") ");
        //
        val = buildSubSelect(val);
        //
        addParams(key.toString(), val);
    }

    private Object buildSubSelect(Object val) {
        if (val instanceof BaseQuery) {
            this.checkSqlHack = false;
            return "(" + ((BaseQuery) val).build() + ")";
        }
        return val;
    }

    private String getTablePrev(Class[] otherClz) {
        String table = mainTableName;
        if (null != otherClz && otherClz.length > 0) {
            table = getTableName(otherClz[0]);
        }
        return table;
    }

    private void addParams(String key, Object v1) {
        this.params.put(key, v1);
    }

    public Map<String, Object> toJdbcParams(Map<String, Object> params) {
        Map<String, Object> jMap = new HashMap<>(params.size());
        for (String k : params.keySet()) {
            jMap.put(k.replace(":", ""), params.get(k));
        }
        return jMap;
    }

    private void addBetween(String column, Object v1, Object v2, String link, Class... otherClz) {
        this.link = link;
        if (null == v1 || "".equals(v1)) return;
        if (null == v2 || "".equals(v2)) return;
        StringBuffer key1 = new StringBuffer().append(":").append(link.toLowerCase()).append("_between_").append(column).append("_v1");
        StringBuffer key2 = new StringBuffer().append(":").append(link.toLowerCase()).append("_between_").append(column).append("_v2");
        String table = getTablePrev(otherClz);
        if (this.where.length() > 0) {
            this.where.append(link);
        }
        this.where.append("(")
                .append(table).append(".").append(column)
                .append(BETWEEN.symbol)
                .append(key1).append(" AND ").append(key2);
        this.where.append(")");
        addParams(key1.toString(), v1);
        addParams(key2.toString(), v2);
        key1 = null;
        key2 = null;
    }

    private void addLike(String column, Object val, String link, Class... otherClz) {
        this.link = link;
        if (null == val || "".equals(val)) return;
        if (this.where.length() > 0) {
            this.where.append(link);
        }
        String table = getTablePrev(otherClz);
        StringBuffer key = new StringBuffer().append(":").append(link.toLowerCase()).append("_like_").append(column);
        this.where.append("(")
                .append(LIKE.symbol).append("(")
                .append(table).append(".").append(column)
                .append(",")
                .append(key)
                .append(")>0) ");
        addParams(key.toString(), val);
        key = null;
    }

    private void addManyCondition(String link, BaseQuery... manyQe) {
        this.where.append(link).append("(");
        if (manyQe.length > 0) {
            for (int i = 0; i < manyQe.length; i++) {
                BaseQuery qe = manyQe[i];
                String sql = settingParams(qe.where.toString(), qe.params);
                if (i == 0) {
                    sql = sql.replace("AND(", "(").replace("OR(", "(");
                }
                if (i > 0) {
                    if (sql.indexOf("AND(") == -1 && sql.indexOf("OR(") == -1) {
                        this.where.append(qe.link).append(sql);
                        continue;
                    }
                }
                this.where.append(sql);
            }
        }
        this.where.append(") ");
    }

    public enum Opt {
        EQ("="),
        NE("!="),
        LT("<"),
        GT(">"),
        LE("<="),
        GE(">="),
        IN(" IN "),
        ISNULL(" IS NULL"),
        NOTNULL(" IS NOT NULL"),
        BETWEEN(" BETWEEN "),
        LIKE("LOCATE "),
        SELECT("SELECT "),
        FROM(" FROM "),
        LEFTJOIN(" LEFT JOIN "),
        ON(" ON "),
        WHERE(" WHERE "),
        DEL("DELETE "),
        AS(" AS "),
        ORDER(" ORDER BY "),
        GROUP(" GROUP BY "),
        HAVING(" HAVING "),
        ASC(" ASC"),
        DESC(" DESC"),
        LIMIT(" LIMIT "),
        AND(" AND "),
        OR(" OR "),
        UPDATE(" UPDATE "),
        INSERT(" INSERT INTO ");

        public String symbol;

        Opt(String symbol) {
            this.symbol = symbol;
        }
    }

    private String changeValOfType(Object val) {
        if (null == val) return "";
        Collection list = new ArrayList();
        StringBuffer v = new StringBuffer();
        if (val.getClass().isArray()) {
            Object[] vv = (Object[]) val;
            v.append("(");
            for (Object o : vv) {
                v.append("'").append(o).append("'");
                v.append(",");
            }
            v.deleteCharAt(v.length() - 1);
            v.append(")");
            return v.toString();
        }
        if (val instanceof Collection) {
            list = (Collection) val;
            v.append("(");
            for (Object o : list) {
                v.append("'").append(o).append("'");
                v.append(",");
            }
            v.deleteCharAt(v.length() - 1);
            v.append(")");
            return v.toString();
        }

        if (val instanceof Date) {
            v.append("'").append(sdf.format(val)).append("'");
            return v.toString();
        }
        if (val instanceof String && isNotSubSelect(val)) {
            v.append("'").append(val).append("'");
            return v.toString();
        }
        if (val instanceof BigDecimal) {
            v.append("'").append(((BigDecimal) val).doubleValue()).append("'");
            return v.toString();
        }

        if (v.length() == 0) {
            v.append(val);
        }
        return v.toString();
    }

    private boolean isNotSubSelect(Object v) {
        return !((""+v).toUpperCase().startsWith("(SELECT"));
    }

    private static final String hack_str = "/(\\\\%27)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|union|update|and|or|delete|insert|trancate|" +
            "into|substr|ascii|declare|exec|execute|count|master|into|drop|information_schema.columns|table_schema)\\b)";

    private static final Pattern SQL_HACK_PATTERN = Pattern.compile(hack_str, Pattern.CASE_INSENSITIVE);

    private boolean isSQLHack(String v) {
        if (checkSqlHack) {
            return SQL_HACK_PATTERN.matcher(v).find();
        }
        return false;
    }

    private StringBuffer buildWhereSql() {
        if (where.length() > 0 && where.indexOf("WHERE") == -1) {
            where.insert(0, "(");
            where.insert(0, WHERE.symbol);
            where.append(")");
        }
        return where;
    }

    public String buildSymbolsql() {
        String sql = this.template;
        if (isReadSql()) {
            sql = replaceTag(sql, "{select}", buildSelect());
            sql = replaceTag(sql, "{from}", buildFrom());
        } else {
            sql = sql.replace("{select}", "").replace("{from}", "");
        }
        sql = replaceTag(sql, "{del}", del);
        sql = replaceTag(sql, "{count}", count);
        sql = replaceTag(sql, "{join}", join);
        sql = replaceTag(sql, "{where}", buildWhereSql());
        sql = replaceTag(sql, "{subselect}", subselect);
        sql = replaceTag(sql, "{sum}", sum);
        sql = replaceTag(sql, "{casethen}", casethen);
        sql = replaceTag(sql, "{sumcasethen}", sumcasethen);
        sql = replaceTag(sql, "{group}", group);
        sql = replaceTag(sql, "{having}", having);
        sql = replaceTag(sql, "{order}", order);
        sql = replaceTag(sql, "{limit}", limit);
        //
        sql = replaceTag(sql, "{update}", update);
        sql = replaceTag(sql, "{insert}", insert);
        this.symbolsql = sql;
        sql=null;
        return this.symbolsql;
    }

    public String build() {
        String sql = settingParams(buildSymbolsql());
        clear();
        return sql;
    }

    private boolean isReadSql() {
        return (this.update.length() == 0 && this.insert.length() == 0);
    }

    private StringBuffer buildSelect() {
        if (this.select.length() == 0 && this.count.length() == 0) {
            this.select.append(" SELECT * ");
        }
        return this.select;
    }

    private String replaceTag(String sql, String tag, StringBuffer content) {
        if (content.length() > 0) {
            if (String.valueOf(content.charAt(content.length() - 1)).equals(",")) {
                content.deleteCharAt(content.length() - 1);
            }
            sql = sql.replace(tag, content);
        } else {
            sql = sql.replace(tag, "");
        }
        return sql;
    }

    private String settingParams(String symbolsql) {
        return settingParams(symbolsql, this.params);
    }

    private String settingParams(String symbolsql, Map<String, Object> params) {
        for (String key : params.keySet()) {
            String v = ""+params.get(key);
            if (!isFieldVal(key,params.get(key))) {// key/val 都没有带别名,才转换了.
                v = changeValOfType(params.get(key));
            }
            Assert.isTrue(isSQLHack(v), "WARN FIND SQL HACK :" + v);
            symbolsql = symbolsql.replace(key, v);
        }
        return symbolsql;
    }

    private boolean isFieldVal(String key, Object v) {
        return (key.indexOf(".") != -1 && v instanceof String && v.toString().indexOf(".") != -1);
    }

    private StringBuffer buildFrom() {
        if (0 == this.from.length()) {
            this.from.append(FROM.symbol);
            if (null != mainTableName && !"".equals(mainTableName)) {
                this.from.append("`").append(mainTableName).append("`").append(" AS ").append(mainTableName).append(" ");
            }
        }
        return this.from;
    }

    public void clear() {
        del = null;
        count = null;
        sum = null;
        casethen = null;
        sumcasethen = null;
        select = null;
        subselect = null;
        from = null;
        join = null;
        where = null;
        group = null;
        having = null;
        order = null;
        limit = null;
        link = null;
        clearMap(params);
    }



}
