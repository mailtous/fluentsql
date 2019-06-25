package com.artlongs.fluentsql.core;

import static sun.jvm.hotspot.utilities.Assert.that;

/**
 * Func :
 *
 * @author: leeton on 2019/6/21.
 */
public class Assert {
    public static void isNull(Object val, String throwMsg) {
        that(null == val, throwMsg);
    }

    public static void isBlank(Object val, String throwMsg) {
        that(null == val || String.valueOf(val).trim().length() == 0, throwMsg);
    }

    public static void isNotFound(Object val, String throwMsg) {
        that(null == val || val.equals("false") || val.equals(-1), throwMsg);
    }

    public static void isFalse(boolean test, String throwMsg) {
        that(test, throwMsg);
    }

    public static void isTrue(boolean test, String throwMsg) {
        that(false==test, throwMsg);
    }



}
