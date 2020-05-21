package com.artlongs.fluentsql.core;

/**
 * Func : SQl 的 Function
 * 注意:如果使用在子查询里则要指定拼写为 Spell.UNDERLINE
 * @author: leeton on 2019/6/18.
 */
public class Qf<T> {
    public String sql = "";
    private Attr.Property<T, ?> field;
    private Object defaultVal;
    private String as;
    private FUNC func;
    private Spell spell;

    public static <T> Qf ifnull(Attr.Property<T, ?> field) {
        return build(field, 0, null, FUNC.IFNULL, Spell.CAMEL);
    }

    public static <T> Qf ifnull(Attr.Property<T, ?> field, String as) {
        return build(field, 0, as, FUNC.IFNULL, null);
    }

    public static <T> Qf ifnull(Attr.Property<T, ?> field, Spell spell) {
        return build(field, 0, null, FUNC.IFNULL, spell);
    }

    public static <T> Qf ifnull(Attr.Property<T, ?> field, Object defVal, Spell spell) {
        return build(field, defVal, null, FUNC.IFNULL, spell);
    }

    public static <T> Qf ifnull(Attr.Property<T, ?> field, Object defaultVal, String as) {
        return build(field, defaultVal, as, FUNC.IFNULL, null);
    }

    public static <T> Qf sum(Attr.Property<T, ?> field) {
        return build(field,0,null, FUNC.SUM, Spell.CAMEL);
    }

    public static <T> Qf sum(Attr.Property<T, ?> field, String as) {
        return build(field, 0, as, FUNC.SUM, null);
    }

    public static <T> Qf sum(Attr.Property<T, ?> field, Spell spell) {
        return build(field, 0, null, FUNC.SUM, spell);
    }
    public static <T> Qf max(Attr.Property<T, ?> field) {
        return build(field,0,null, FUNC.MAX, Spell.CAMEL);
    }

    public static <T> Qf max(Attr.Property<T, ?> field, String as) {
        return build(field, 0, as, FUNC.MAX, null);
    }

    public static <T> Qf max(Attr.Property<T, ?> field, Spell spell) {
        return build(field, 0, null, FUNC.MAX, spell);
    }

    public static <T> Qf min(Attr.Property<T, ?> field) {
        return build(field, 0, null, FUNC.MIN, Spell.CAMEL);
    }

    public static <T> Qf min(Attr.Property<T, ?> field, String as) {
        return build(field, 0, as, FUNC.MIN, null);
    }

    public static <T> Qf min(Attr.Property<T, ?> field, Spell spell) {
        return build(field, null, null, FUNC.MIN, spell);
    }

    public static <T> Qf countAs(Attr.Property<T, ?> field,String as) {
        return build(field, null, as, FUNC.COUNT, null);
    }

    private static <T> Qf build(Attr.Property<T, ?> field, Object defaultVal, String as, FUNC func, Spell spell) {
        Qf qf = new Qf();
        qf.field = field;
        qf.defaultVal = defaultVal;
        qf.as = as;
        qf.func = func;
        qf.sql = sql(field, defaultVal, as, func, spell);
        return qf;
    }

    private static <T> Qf build(Qf qf, Object defaultVal, String as, FUNC func, Spell spell) {
        qf.defaultVal = defaultVal;
        qf.as = as;
        qf.func = func;
        qf.sql = sql(qf.field, defaultVal, as, func, spell);
        return qf;
    }

    private static <T> String sql(Attr.Property<T, ?> field, Object defaultVal, String as, FUNC sFunc, Spell spell) {
        StringBuffer funcSql = new StringBuffer(32);
        if (FUNC.COUNT == sFunc) {
            return funcSql.append(" COUNT(1) AS ").append(as).toString();
        }
        //
        Attr attr = new Attr<>(field);
        String underline_column = attr.getColumn();
        String camel_column = attr.getName();
        String table = attr.getTableName();
        as = Spell.getAs(as, spell, underline_column, camel_column);
        if (null != defaultVal) {
            funcSql.append("IFNULL(");
            funcSql.append(sFunc.name()).append("(").append(table).append(".").append(underline_column).append(")");
            funcSql.append(",").append(defaultVal);
            funcSql.append(") AS ").append(as);
        }else {
            funcSql.append(sFunc.name()).append("(").append(table).append(".").append(underline_column);
            funcSql.append(") AS ").append(as);
        }
        return funcSql.toString();
    }

    public enum FUNC {
        IFNULL,SUM, MAX, MIN, COUNT
    }


}
