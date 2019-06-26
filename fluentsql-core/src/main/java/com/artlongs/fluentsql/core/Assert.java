package com.artlongs.fluentsql.core;

/**
 * Func : trueThenThrow
 *
 * @author: leeton on 2019/6/21.
 */
public class Assert {
    public static void isNull(Object val, String throwMsg) {
        trueThenThrow(null == val, throwMsg);
    }

    public static void isBlank(Object val, String throwMsg) {
        trueThenThrow(null == val || String.valueOf(val).trim().length() == 0, throwMsg);
    }

    public static void isNotFound(Object val, String throwMsg) {
        trueThenThrow(null == val || val.equals("false") || val.equals(-1), throwMsg);
    }

    public static void isFalse(boolean test, String throwMsg) {
        trueThenThrow(false==test, throwMsg);
    }

    public static void isTrue(boolean test, String throwMsg) {
        trueThenThrow(test, throwMsg);
    }

    public static void trueThenThrow(boolean test, String throwMsg) {
        if (test) {
            throw new RuntimeException(throwMsg);
        }
    }

}
