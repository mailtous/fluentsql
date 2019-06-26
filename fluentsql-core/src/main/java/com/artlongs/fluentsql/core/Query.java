package com.artlongs.fluentsql.core;

import java.util.List;

/**
 * Func :
 *
 * @author: leeton on 2019/6/21.
 */
public interface Query {
    <T> T to();

    <T> T to(Class<T> tClass);

    Long toCount();

    <T> List<T> toList();

    <T>List toList(Class<T> tClass);

    boolean toDel();

    int toUpdate(Object entity);

    int toSave(Object entity);

    <T> Page<T> toPage(Page<T> page);

    <T> Page<T> toPage(Class<T> clz, Page<T> page);
}
