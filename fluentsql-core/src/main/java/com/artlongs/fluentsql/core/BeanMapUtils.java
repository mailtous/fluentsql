package com.artlongs.fluentsql.core;


import org.joda.time.DateTime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

/**
 * Func :
 *
 * @author: leeton on 2019/6/14.
 */
public class BeanMapUtils {
    private static boolean skip_null = true;  //跳过空值
    private static boolean ign_camel = false; //忽略驼峰
    private static boolean ign_underline = false; //忽略下划线
    private static boolean spell_fuzzy_match = false; //模糊匹配的模式

    public static <T> T copyTo(Object source, T target, String... ignList) {
        copy(source, target, ignList);
        return (T) target;
    }

    public static BeanMapUtils builder(){
        return new BeanMapUtils();
    }

    public static BeanMapUtils fuzzy(){
        return BeanMapUtils.builder().setSpellFuzzyMatch(true);
    }

    public BeanMapUtils setSkipNullVal(boolean tf) {
        this.skip_null = tf ;
        return this;
    }
    public BeanMapUtils setIgnCamel(boolean tf) {
        this.ign_camel = tf ;
        return this;
    }

    public BeanMapUtils setIgnUnderline(boolean tf) {
        this.ign_underline = tf;
        return this;
    }
    public BeanMapUtils setSpellFuzzyMatch(boolean tf) {
        this.spell_fuzzy_match = tf;
        return this;
    }

    public void c(Object source, Object target, String... ignList){
        if (null == source || target == null) {//为空,则不进行COPY属性
            return;
        }
        if (target instanceof Map) {
            toMap(source, (Map<String, Object>) target, ignList);
            return;
        }
        if (source instanceof Map) {
            fromMap((Map<String, Object>) source, target, ignList);
            return;
        }
        toPojo(source, target, ignList);
    }

    public <T> T cpTo(Object source, T target, String... ignList) {
        c(source, target, ignList);
        return (T) target;
    }


    /**
         * COPY 属性(对象与MAP通用)
         *
         * @param source  原类
         * @param target  目标类
         * @param ignList 忽略列表
         */
    public static void copy(Object source, Object target, String... ignList) {
        BeanMapUtils.builder().c(source, target, ignList);
    }

    private static void toPojo(Object source, Object target, String[] ignList) {
        Set<Field> trageFieldList = getFields(target.getClass());
        Set<Field> sourceFieldList = getFields(source.getClass());
        if (sourceFieldList.isEmpty() || trageFieldList.isEmpty()) {
            throw new RuntimeException("trageFieldList OR sourceFieldList is EMPTY !");
        }
        if (sourceFieldList.size() > 0) {
            for (Field sField : sourceFieldList) {
                if (isFilterAttr(Arrays.asList(ignList), sField.getName())) continue;
                Object value = getFieldValue(source, sField);
                if (skip_null && null == value) continue; //跳过空值
                Field field = getFieldByName(trageFieldList, sField.getName());
                if (null != field) {
                    setFieldValue(target, field, value);
                }
            }
        }
    }

    private static void toMap(Object source, Map<String, Object> targetMap, String... ignList) {
        Set<Field> sourceFieldList = getFields(source.getClass());
        if (sourceFieldList.isEmpty()) {
            throw new RuntimeException("trageFieldList is EMPTY !");
        }
        if (sourceFieldList.size() > 0) {
            for (Field sField : sourceFieldList) {
                if (isFilterAttr(Arrays.asList(ignList), sField.getName())) continue;
                Object value = getFieldValue(source, sField);
                if (skip_null && null == value) continue;
                targetMap.put(sField.getName(), value);
            }
        }
    }

    private static void fromMap(Map<String, Object> sourceMap, Object target, String... ignList) {
        Set<Field> targetFields = getFields(target.getClass());
        if (targetFields.isEmpty()) {
            throw new RuntimeException("trageFieldList is EMPTY !");
        }
        for (String key : sourceMap.keySet()) {
            if (isFilterAttr(Arrays.asList(ignList), key)) continue;
            Object val = sourceMap.get(key);
            if(skip_null && null==val) continue;
            Field field = getFieldByName(targetFields, key);
            if (null != field) {
                setFieldValue(target, field, val);
            }
        }
    }

    private static boolean isFilterAttr(List<String> filterList, String currentAttr) {
        for (String name : filterList) {
            if (name.equals(currentAttr)) return true;
        }
        return false;
    }


    public static Field getFieldByName(Set<Field> fields, String name) {
        for (Field field : fields) {
            String[] arr = new String[]{field.getName(), name};
            if(ign_camel){
                lowerCase(arr);
            }
            if (ign_underline) {
                ignUnderline(arr);
            }
            if(spell_fuzzy_match){
                spellFuzzyMatch(arr);
            }
            if (arr[0].equals(arr[1])) {
                return field;
            }
        }
        return null;
    }

    private static void lowerCase(String[] arr) {//全转为小写
        arr[0] = arr[0].toLowerCase();
        arr[1] = arr[1].toLowerCase();
    }
    private static void ignUnderline(String[] arr) {//去掉下划线
        arr[0] = StringKit.deCodeUnderlined(arr[0]);
        arr[1] = StringKit.deCodeUnderlined(arr[1]);
    }
    private static void spellFuzzyMatch(String[] arr) {//模糊拼写区别
        ignUnderline(arr);
        lowerCase(arr);
    }

    private static String replacePrefix(String name) {
        if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        }
        if (name.startsWith("is")) {
            name = name.substring(2);
        }
        return name;
    }

    public static Set<Field> getFields(Class clz) {
        Set<Field> fieldList = new HashSet<>();
        getFieldsIter(clz, fieldList);
        return fieldList;
    }

    /**
     * 遍历取得类的属性（包含父类属性）
     *
     * @param clz
     * @param fieldCache
     */
    private static void getFieldsIter(Class clz, Set<Field> fieldCache) {
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (findIgnoreAnno(field)) continue;
            fieldCache.add(field);
        }
        if (null != clz.getSuperclass() && (!clz.getSuperclass().getName().equalsIgnoreCase("java.lang.Object"))) {
            getFieldsIter(clz.getSuperclass(), fieldCache);
        }
    }

    /**
     * 遍历取得类的所有方法
     *
     * @param clz
     * @param cache
     */
    public static Set<Method> getAllMethod(Class clz, Set<Method> cache) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isFinal(method.getModifiers())) continue;
            if (Modifier.isStatic(method.getModifiers())) continue;
            cache.add(method);
        }
        if (null != clz.getSuperclass() && (!clz.getSuperclass().getName().equalsIgnoreCase("java.lang.Object"))) {
            getAllMethod(clz.getSuperclass(), cache);
        }

        return cache;
    }

    private static boolean findIgnoreAnno(Field field) {
        Annotation[] annoList = field.getAnnotations();
        for (Annotation annotation : annoList) {
            if ("JsonIgnore".equalsIgnoreCase(annotation.annotationType().getSimpleName())) return true;
            if ("JsonBackReference".equalsIgnoreCase(annotation.annotationType().getSimpleName())) return true;
            if ("transient".equalsIgnoreCase(annotation.annotationType().getSimpleName())) return true;
        }
        return false;
    }

    /**
     * 按规则过滤方法
     *
     * @param method
     * @return
     */
    private static boolean isSkipMethod(Method method) {
        if (Modifier.isStatic(method.getModifiers())) return true;
        if (Modifier.isFinal(method.getModifiers())) return true;
        if (method.getName().equalsIgnoreCase("toString")) return true;
        if (method.getName().equalsIgnoreCase("hashCode")) return true;
        return false;
    }

    private static boolean isTransientAnno(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if ("transient".equalsIgnoreCase(annotation.annotationType().getSimpleName())) return true;
        }
        return false;
    }

    /**
     * 对属性设值
     *
     * @param targetInst  目标类实例,不是 xx.class
     * @param field
     * @param fieldValue
     */
    public static void setFieldValue(Object targetInst, Field field, Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(targetInst, getValOfBaseType(field.getType(),fieldValue));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得属性的值
     *
     * @param targetClz 目标类
     * @param field
     * @param <T>
     * @return
     */
    public static <T> T getFieldValue(Object targetClz, Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(targetClz);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getValOfBaseType(Class<?> c, Object val) {
        String v = "" + val;
        if (c == int.class || c == Integer.class)
            return Integer.parseInt(v);
        else if (c == long.class || c == Long.class)
            return Long.parseLong(v);
        else if (c == float.class || c == Float.class)
            return Float.parseFloat(v);
        else if (c == double.class || c == Double.class) {
            return Double.parseDouble(v);
        }
        else if (c == Date.class) {
            return DateTime.parse(v).toDate();
        }
        else if (c == DateTime.class) {
            return DateTime.parse(v);
        }
        else if (c == BigDecimal.class) {
            return BigDecimal.valueOf(Double.valueOf(v));
        }
        return v;
    }


    public static void main(String[] args) {

        class Foo {
            private Integer id;
            private String userName;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Foo{");
                sb.append("id=").append(id);
                sb.append(", userName='").append(userName).append('\'');
                sb.append('}');
                return sb.toString();
            }
        }

        class Du{
            private Integer id;
            private String user_name;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public String getUser_name() {
                return user_name;
            }

            public void setUser_name(String user_name) {
                this.user_name = user_name;
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Du{");
                sb.append("id=").append(id);
                sb.append(", user_name='").append(user_name).append('\'');
                sb.append('}');
                return sb.toString();
            }
        }

        class Boo {
            private Integer id;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Boo{");
                sb.append("id=").append(id);
                sb.append('}');
                return sb.toString();
            }
        }
        Foo foo = new Foo();
        Foo foo2 = new Foo();
        Foo foo3 = new Foo();
        Map<String, Object> tMap = new HashMap<>();

        foo.setId(111);
        foo.setUserName("alice");
        System.err.println("foo1= " + foo.toString());

        BeanMapUtils.copy(foo, foo2);
        System.err.println("foo1 ->foo2 = " + foo2.toString());
        //
        BeanMapUtils.copy(foo, tMap);
        System.err.println("foo -> tMap = " + tMap.toString());

        BeanMapUtils.copy(tMap, foo3);
        System.err.println("tMap -> foo = " + foo3.toString());

        Boo boo = new Boo();
        BeanMapUtils.copy(foo, boo);
        System.err.println("foo -> boo = " + boo.toString());

        boo.setId(999);
        BeanMapUtils.copy(boo, foo);
        System.err.println("boo -> foo = " + foo.toString());


        BeanMapUtils.copy(tMap, boo);
        System.err.println("tMap -> boo = " + boo.toString());

        Du du = new Du();

        BeanMapUtils.builder().setSpellFuzzyMatch(true).cpTo(foo,du);
        System.err.println("foo -> du = " + du.toString());



    }
}
