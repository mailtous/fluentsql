package com.artlongs.fluentsql.core;

/**
 * Func : 字段的拼写风格
 *
 * @author: leeton on 2019/6/18.
 */
public enum Spell {
    UNDERLINE,CAMEL;
    public static String getAs(String as, Spell spell, String underline, String camel) {
        if (null != as && !"".equals(as)) {
            return as;
        }
        if (Spell.UNDERLINE == spell) {
            return underline;
        }
        if (Spell.CAMEL == spell) {
            return camel;
        }
        return "";
    }
}